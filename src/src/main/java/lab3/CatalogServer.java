package lab3;

import static spark.Spark.*;
import static lab3.JsonUtil.*;
import static lab3.HttpUtil.*;
import static lab3.dbUtil.*;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

class CatalogServer {
		//catalog side cache
		private ConcurrentHashMap<Integer, Integer> cache = new ConcurrentHashMap<>();
		private File log;
		private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

		private String replica_ip;

		//for catalog server synchronization
		private boolean sync = true;

		private boolean DB_accessing = true;

		//constructor
		public CatalogServer(String replica_ip) {
			createDB();
			//createLogFile();
			this.replica_ip = replica_ip;
		}

		//start server
		public void start() {
			port(3154);

			//query by item number
			get("/lookup",(req, res) -> {
				String param = req.queryParams("id");
				int id = Integer.parseInt(param);
				int quan = query(id);
				//writeToLog("lookup bookid: "+param+" title: "+b.getTitle());
				Map<String,Object> result = new HashMap<String,Object>();
				result.put(param, quan);

				return result;
			},json());

			//search by topic
			get("/search",(req, res) ->{
				String topic = req.queryParams("topic");
				//writeToLog("seach topic: "+topic);
				Map<String,Object> result = new HashMap<String,Object>();
				int[] id_list = queryByTopic(topic);

				return result;
			},json());

			//buy by id and quantity
			get("/update",(req, res) -> {
				String param1 = req.queryParams("id");
				int id = Integer.parseInt(param1);
				String param2 = req.queryParams("quantity");
				int quantity = Integer.parseInt(param2);
				Map<String,Object> result = new HashMap<String,Object>();

				int query_quan = cache.get(id);
				if(query_quan>quantity){
					int new_quantity = query_quan - quantity;

				} else {
					result.put("cur_quantity", query_quan);
					result.put("result", "fail");
					//writeToLog("update bookId: "+param1+" quantity: "+param2+" Update Failed");
				}

				/*
				if(updatedQuan>-1) {
					result.put("cur_quantity", updatedQuan);
					result.put("result", "success");
					//writeToLog("update bookId: "+param1+" quantity: "+param2+" stock: "+String.valueOf(updatedQuan));
				}
				else {

				}
				*/
				return result;
			},json());

			//changes every response to application/json
			after((req, res) -> {
				  res.type("application/json");
				});
		}

		//create database
		private void createDB(){
			initDB();
		}
		//get stock by id
		public Integer query(int id) {
			readWriteLock.readLock().lock();
			try{
				return cache.get(id);
			} finally {
				readWriteLock.readLock().unlock();
			}
		}

		public void query_batch(int[] id_list, Map<String,Object> result) {
			readWriteLock.readLock().lock();
			try{
				for(int i :id_list) {
					result.put(Integer.toString(i), cache.get(i));
				}
			} finally {
				readWriteLock.readLock().unlock();
			}
		}

		//get list of books' id by topic
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

		public void updateCache(int id, int new_quantity){
			cache.put(id, new_quantity);
		}

		public void update_DB(int id, int quantity){
			UpdateDB(id, quantity);
		}

		public boolean sync_replica(int id, int quantity){
			if(this.sync){
				System.out.println("try to sync");
    			Response replicaRes = request("GET","http://"+this.replica_ip+":3154/synchronization?id="+Integer.toString(id)+"&quantity="+Integer.toString(quantity));
				if(replicaRes==null){
	    			System.out.println("replica down");
	    			this.sync = false;
	    			return true;
				} else {
	    			Map<String,Object> replicaResObj = replicaRes.json();
	    			if(replicaResObj.get("result").equals("success")) return true;
	    			else return false;
	    		}
			} else return true;
		}

		//sell book by id, quantity
		public boolean update(int id, int quantity) {

    		if(sync_replica(id, quantity)){
    			System.out.println("sync success");
    			update_DB(id, quantity);
    			updateCache(id, quantity);
    			return true;
    		} else {
    			System.out.println("sync failed");
    			return false;
    		}
		}

		private void initCache(){
			cache.put(1,5);
			cache.put(2,4);
			cache.put(3,3);
			cache.put(4,2);
			cache.put(5,6);
			cache.put(6,7);
			cache.put(7,8);
		}

		//create log file to store printed messages
		public void createLogFile(){
			log = new File("./catalog_log.txt");
			try{
				if(!log.exists()){
					log.createNewFile();
					System.out.println("File is created!");
				} else {
					log.delete();
					log.createNewFile();
					System.out.println("catalog log recreated!");
				}
			} catch (IOException e){
				e.printStackTrace();
			}
		}

		//write printed message to log file
		public void writeToLog(String s){
			try{
				FileWriter fw = new FileWriter("catalog_log.txt", true);
				fw.write(s);
				String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
				fw.write(" timeStamp: "+timeStamp);
				fw.write(System.getProperty("line.separator"));
				fw.flush();
				fw.close();
			} catch (IOException ex) {
	            ex.printStackTrace();
	        }
		}
}

