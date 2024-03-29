package lab3;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;
import static lab3.JsonUtil.*;
import static lab3.HttpUtil.*;

public class FrontendServer {

	private String[][] servers_ip = new String[2][2];
	private int port;
	private ClusterClass available_cluster = new ClusterClass();
	private Random rand = new Random();

	private Map<Integer, Book> cache = new HashMap<Integer, Book>(7);
	private ConcurrentHashMap<Integer, Integer> cacheStock = new ConcurrentHashMap<Integer, Integer>(7);

	private final ReentrantLock Lock = new ReentrantLock();

	public FrontendServer(String port, String cat_server_ip0, String order_server_ip0,
		String cat_server_ip1, String order_server_ip1){
		this.servers_ip[0][0] = cat_server_ip0;
		this.servers_ip[0][1] = order_server_ip0;
		this.servers_ip[1][0] = cat_server_ip1;
		this.servers_ip[1][1] = order_server_ip1;
		this.port = Integer.valueOf(port);
		available_cluster.add(0);
		available_cluster.add(1);

		initCache();
		//lookup_timeLog = new File("./time_logs/frontend_lookup_timeLog.txt");
		//search_timeLog = new File("./time_logs/frontend_search_timeLog.txt");
		//buy_timeLog = new File("./time_logs/frontend_buy_timeLog.txt");
		heartBeat(0);
		heartBeat(1);
	}

	public int load_balancing(){
		//get new random cluster id based on the number of avaliable cluster
		int next_cluster_index = rand.nextInt(available_cluster.size());
		int next_cluster_id = available_cluster.get(next_cluster_index).intValue();
		System.out.println(next_cluster_id);
		return next_cluster_id;
	}

	public void report_crash(int id){
		available_cluster.remove(id);
	}

	public void start() {
		port(this.port);

		//receive and send buy request and response
		get("/buy",(req, res) ->{
			String quantity_str = req.queryParams("quantity");
			String id_str = req.queryParams("id");

			Map<String,Object> result = new HashMap<String,Object>();
			//lock for consistency
			Lock.lock();
			//send buy request to all replicas
			Response orderServer0Response = request("GET","http://"+servers_ip[0][1]+"/buy?id="+id_str+"&"+"quantity="+quantity_str);
			Response orderServer1Response = request("GET","http://"+servers_ip[1][1]+"/buy?id="+id_str+"&"+"quantity="+quantity_str);
			if(orderServer0Response!=null){
				Map<String,Object> response0 = orderServer0Response.json();
				String flag0 = (String)response0.get("result");
				if(flag0.equals("fail")) report_crash(0);
				else{
					result = response0;
				}
			} else if(orderServer1Response!=null){
				Map<String,Object> response1 = orderServer1Response.json();
				String flag1 = (String)response1.get("result");
				if(flag1.equals("fail")) report_crash(1);
				else{
					result = response1;
				}
			} else {
				System.out.println("both ordersever are done...");
			}
			Lock.unlock();

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

					Response catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+"/lookup?id="+param);
					//if the first request has no respsonse, report crash and re-send the request to another server
					if(catServerResponse==null){
						System.out.println("Catalog Server Down "+Integer.toString(target_cluster));
			    		report_crash(target_cluster);
			    		target_cluster = load_balancing();
			    		catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+"/lookup?id="+param);
					}

					Map<String,Object> response = catServerResponse.json();

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

			Map<String,Map<String, Object>> result = new HashMap<String,Map<String, Object>>();
			int[] search_ids = queryByTopic(topic);
			if(search_ids.length>0){
				//if all info can be fetch from the cache
				if(search_cache(search_ids)){
					for(int i : search_ids){
						HashMap<String,Object> book_info = new HashMap<String,Object>();
						book_from_cache(i, book_info, true);
						result.put(Integer.toString(i), book_info);
					}
				} else {
					//otherwise only put static info into the result object
					for(int i : search_ids){
						HashMap<String,Object> book_info = new HashMap<String,Object>();
						book_from_cache(i, book_info, false);
						result.put(Integer.toString(i), book_info);
					}
					int target_cluster = load_balancing();
					//ask cat server for missing stock info
					Response catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+"/search?topic="+topic);
					if(catServerResponse==null) {
						System.out.println("Catalog Server Down "+Integer.toString(target_cluster));
						report_crash(target_cluster);
						target_cluster = load_balancing();
						catServerResponse = request("GET","http://"+servers_ip[target_cluster][0]+"/search?topic="+topic);
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

		//invalidate cache
		get("/invalid",(req, res) ->{
			String id_str = req.queryParams("id");
			int id = Integer.parseInt(id_str);
			cacheStock.remove(id);
			return true;
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

	//get book ids for certain topic
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

	//check if all need info is in the cache
	private boolean search_cache(int[] index){
		for(int i : index){
			if(cacheStock.get(i)==null) return false;
		}
		return true;
	}

	//pack needed info in the json obj
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
		    	//System.out.println(available_cluster.size());
		    	try{
		    		Thread.sleep(5000);
		    	}catch (Exception e) {
				    System.out.println("Can't sleep?");
				    System.out.println(e);
				}

	    		Response heartBeatCat = request("GET","http://"+servers_ip[cluster_id][0]+"/heartBeat");

	    		if(heartBeatCat==null){
	    			report_crash(cluster_id);
	    		} else {
	    			available_cluster.add(cluster_id);
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
