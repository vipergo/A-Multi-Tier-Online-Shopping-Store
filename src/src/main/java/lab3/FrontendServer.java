package lab3;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;
import static lab3.JsonUtil.*;
import static lab3.HttpUtil.*;

public class FrontendServer {
	//private File lookup_timeLog;
	//private File search_timeLog;
	//private File buy_timeLog;
	private String[][] servers_ip = new String[2][2];
	private int down_cluster_id = 2;
	private Random rand = new Random();

	private Map<Integer, Book> cache = new HashMap<Integer, Book>(7);
	private ConcurrentHashMap<Integer, Integer> cacheStock = new ConcurrentHashMap<Integer, Integer>(7);

	private final ReentrantLock Lock = new ReentrantLock();

	public FrontendServer(String cat_server_ip0, String order_server_ip0){
		this.servers_ip[0][0] = cat_server_ip0;
		this.servers_ip[0][0] = order_server_ip0;
		initCache();
		//lookup_timeLog = new File("./time_logs/frontend_lookup_timeLog.txt");
		//search_timeLog = new File("./time_logs/frontend_search_timeLog.txt");
		//buy_timeLog = new File("./time_logs/frontend_buy_timeLog.txt");
		//heartBeat(0);
	}

	public int load_balancing(){
		if(this.down_cluster_id==2) return rand.nextInt(2);
		else if(this.down_cluster_id==0) return 1;
		else return 0;
	}

	public void report_crash(int id){
		this.down_cluster_id = id;
	}

	public void start() {
		port(3800);

		//receive and send buy request and response
		get("/buy",(req, res) ->{
			String quantity_str = req.queryParams("quantity");
			String id_str = req.queryParams("id");
			int id = Integer.parseInt(id_str);
			int quantity = Integer.parseInt(quantity_str);
			/*
			long startTime = System.currentTimeMillis();
			Response orderServerResponse = request("GET","http://"+order_server_ip+":3801/buy?id="+id_str+"&"+"quantity="+quantity_str);
			long endTime = System.currentTimeMillis();
			//recordTime(endTime-startTime, buy_timeLog);
			*/
			Map<String,Object> result = new HashMap<String,Object>();
			Response orderServerResponse = request("GET","http://"+order_server_ip+":3801/buy?id="+id_str+"&"+"quantity="+quantity_str);
			Response orderServerResponse = request("GET","http://"+order_server_ip+":3801/buy?id="+id_str+"&"+"quantity="+quantity_str);
			return result;
		},json());

		//receive and send lookup request and response
		get("/lookup",(req, res) -> {
			String param = req.queryParams("id");
			int id = Integer.parseInt(param);
			/*
			long startTime = System.currentTimeMillis();
			Response catServerResponse = request("GET","http://"+cat_server_ip+":3154/lookup?id="+param);
			long endTime = System.currentTimeMillis();
			//recordTime(endTime-startTime, lookup_timeLog);
			*/
			Map<String,Object> result = new HashMap<String,Object>();
			Book targetBook = queryBookInfo(id);
			if(targetBook!=null){
				Integer bookStock = queryCacheStock(id);
				if(bookStock==null){
					int target_cluster = load_balancing();

					Response catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+":3154/lookup?id="+param);
					if(catServerResponse==null){
						System.out.println("Catalog Server Down "+Integer.toString(target_cluster));
			    		report_crash(target_cluster);
			    		target_cluster = load_balancing();
			    		catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+":3154/lookup?id="+param);
					}

					Map<String,Object> response = catServerResponse.json();
					System.out.println(response.get("cur_quantity"));
					int cur_quan = ((Double)response.get("cur_quantity")).intValue();
					result.put("cur_quantity", cur_quan);
					cacheStock.put(id, cur_quan);


				} else {
					result.put("cur_quantity", bookStock);
				}
				result.put("title", targetBook.getTitle());
				result.put("price", targetBook.getPrice());
				result.put("topic", targetBook.getTopic());
			} else {
				res.redirect("/404");
			}


			return result;
		},json());

		//receive and send search request and response
		get("/search",(req, res) ->{
			String topic = req.queryParams("topic");
			/*
			long startTime = System.currentTimeMillis();
			Response catServerResponse = request("GET","http://"+cat_server_ip+":3154/search?topic="+topic);
			long endTime = System.currentTimeMillis();
			//recordTime(endTime-startTime, search_timeLog);
			*/
			Map<String,Map<String, Object>> result = new HashMap<String,Map<String, Object>>();
			int[] search_ids = queryByTopic(topic);
			if(search_ids.length>0){
				if(search_cache(search_ids)){
					for(int i : search_ids){
						HashMap<String,Object> book_info = new HashMap<String,Object>();
						book_from_cache(i, book_info, true);
						result.put(Integer.toString(i), book_info);
					}
				} else {
					for(int i : search_ids){
						HashMap<String,Object> book_info = new HashMap<String,Object>();
						book_from_cache(i, book_info, false);
						result.put(Integer.toString(i), book_info);
					}
					int target_cluster = load_balancing();

					Response catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+":3154/search?topic="+topic);
					if(catServerResponse==null) {
						System.out.println("Catalog Server Down "+Integer.toString(target_cluster));
						report_crash(target_cluster);
						catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+":3154/search?topic="+topic);
					}
					Map<String, Object> resObj = catServerResponse.json();
					for(Map.Entry<String,Object> entry : resObj.entrySet()){
						result.get(entry.getKey()).put("cur_quantity", entry.getValue());
					}



				}
			} else{
				res.redirect("/404");
			}


			return result;
		},json());

		get("/404",(req, res) ->{
			return "no such page!";
		},json());

		after((req, res) -> {
			res.type("application/json");
		});
	}

	public Book queryBookInfo(int id) {
		return cache.get(id);
	}

	public Integer queryCacheStock(int id){
		return cacheStock.get(id);
	}

	//for simplicity just hardcode
	private int[] queryByTopic(String topic){
		if(topic.equals("distributed_systems")){
			int[] arr = {1,2};
			return arr;
		} else if(topic.equals("graduate_school")){
			int[] arr = {3,4};
			return arr;
		} else if(topic.equals("lab3")){
			int[] arr = {5,6,7};
			return arr;
		} else return new int[0];
	}

	private boolean search_cache(int[] index){
		for(int i : index){
			if(cacheStock.get(i)==null) return false;
		}
		return true;
	}

	private void book_from_cache(int i, HashMap<String, Object> result, boolean stock_cache){
		Book targetBook = queryBookInfo(i);
		result.put("title", targetBook.getTitle());
		result.put("price", targetBook.getPrice());
		result.put("topic", targetBook.getTopic());
		if(stock_cache) result.put("cur_quantity", Integer.toString(queryCacheStock(i)));
	}

	private void heartBeat(int cluster_id){
		Runnable heart = () -> {
		    while(true){
		    	System.out.println(down_cluster_id);
		    	try{
		    		Thread.sleep(5000);
		    	}catch (Exception e) {
				    System.out.println("Can't sleep?");
				    System.out.println(e);
				}

	    		Response heartBeatCat = request("GET","http://"+servers_ip[cluster_id][0]+":3154/heartBeat");
	    		Response heartBeatOrder = request("GET","http://"+servers_ip[cluster_id][1]+":3801/heartBeat");
	    		if(heartBeatCat==null || heartBeatOrder==null){
	    			report_crash(cluster_id);
	    		} else {
	    			if(down_cluster_id==cluster_id) down_cluster_id=2;
	    		}
		    }
		};
	    Thread t = new Thread(heart);
	    t.start();
	}

	private void initCache(){
		cache.put(1, new Book("How to get a good grade in 677 in 20 minutes a day.", "distributed_systems",100, 1));
		cache.put(2, new Book("RPCs for Dummies.", "distributed_systems",20, 2));
		cache.put(3, new Book("Xen and the Art of Surviving Graduate School.", "graduate_school",200, 3));
		cache.put(4, new Book("Cooking for the Impatient Graduate Student.", "graduate_school", 250, 4));
		cache.put(5, new Book("How to finish Project 3 on time", "lab3", 500, 5));
		cache.put(6, new Book("Why theory classes are so hard", "lab3", 500, 6));
		cache.put(7, new Book("Spring in the Pioneer Valley", "lab3", 500, 7));
	}
}
