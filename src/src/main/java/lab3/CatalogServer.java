package lab3;

import static spark.Spark.*;
import static lab3.JsonUtil.*;
import static lab3.HttpUtil.*;
import static lab3.dbUtil.*;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

class CatalogServer {
		//catalog side cache with strong consistency with DB
		private ConcurrentHashMap<Integer, Integer> cache = new ConcurrentHashMap<>();
		private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

		public String server_id;
		private String replica_ip;

		private File log;


		//constructor
		public CatalogServer(String server_id, String replica_ip) {
			this.server_id = server_id;
			this.replica_ip = replica_ip;
			initDB(server_id);
			sync();
			initCache();
			//createLogFile();
		}

		//start server
		public void start() {
			port(3154);

			//query by item number
			get("/lookup",(req, res) -> {
				String param = req.queryParams("id");
				int id = Integer.parseInt(param);
				Integer quan = query(id);
				//writeToLog("lookup bookid: "+param+" title: "+b.getTitle());
				Map<String,Object> result = new HashMap<String,Object>();
				result.put("cur_quantity", quan);
				return result;
			},json());

			//search by topic
			get("/search",(req, res) ->{
				String topic = req.queryParams("topic");
				//writeToLog("seach topic: "+topic);
				Map<String,Object> result = new HashMap<String,Object>();
				int[] id_list = queryByTopic(topic);
				query_batch(id_list, result);
				return result;
			},json());

			//buy by id and quantity
			get("/buy",(req, res) -> {
				String param1 = req.queryParams("id");
				int id = Integer.parseInt(param1);
				String param2 = req.queryParams("quantity");
				int quantity = Integer.parseInt(param2);
				Map<String,Object> result = new HashMap<String,Object>();
				result.put("id", param1);

				readWriteLock.writeLock().lock();
				int query_quan = cache.get(id);
				if(query_quan>=quantity){
					int new_quantity = query_quan - quantity;
					UpdateDB(id, new_quantity, this.server_id);
    				cache.put(id, new_quantity);
					result.put("cur_quantity", new_quantity);
					result.put("result", "success");
					readWriteLock.writeLock().unlock();
				} else {
					readWriteLock.writeLock().unlock();
					result.put("cur_quantity", query_quan);
					result.put("result", "outStock");
					//writeToLog("update bookId: "+param1+" quantity: "+param2+" Update Failed");
				}

				return result;
			},json());

			get("/synchronization",(req, res) -> {
				Map<String,Object> result = new HashMap<String,Object>();

				readWriteLock.readLock().lock();
				for(int i=1; i<8; i++){
					result.put(Integer.toString(i), cache.get(i));
				}
				readWriteLock.readLock().unlock();
				return result;
			}, json());

			get("/heartBeat",(req, res) -> {
				return true;
			}, json());

			//changes every response to application/json
			after((req, res) -> {
				  res.type("application/json");
				});
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

		//put search result into response obj
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

		public void sync(){
			Response syncRes = request("GET","http://"+this.replica_ip+":3154/synchronization");
			if(syncRes!=null){
				Map<String, Object> result = syncRes.json();
				readWriteLock.writeLock().lock();
				for(Map.Entry<String,Object> entry : result.entrySet()){
					Double v = (Double)entry.getValue();
					UpdateDB(Integer.parseInt(entry.getKey()), v.intValue(), this.server_id);
				}
				readWriteLock.writeLock().unlock();
			}
		}

		private void initCache(){
			for(int i=1; i<8; i++){
				cache.put(i,queryDB(i, this.server_id));
			}
		}

		//create log file to store printed messages
		public void createLogFile(){
			log = new File("./catalog_log"+this.server_id+".txt");
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
				FileWriter fw = new FileWriter("catalog_log"+this.server_id+".txt", true);
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

