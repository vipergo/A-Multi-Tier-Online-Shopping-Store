package lab3;

import static spark.Spark.*;
import static lab3.JsonUtil.*;
import static lab3.HttpUtil.*;

import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

public class OrderServer {

	private File orderLog;

	public String server_id;
	private int port;
	private String cat_server_ip;
	private String frontend_ip;

	private final ReentrantLock Lock = new ReentrantLock();

	//constructor
	public OrderServer(String server_id, String port, String cat_server_ip, String frontend_ip){
		this.server_id = server_id;
		this.cat_server_ip = cat_server_ip;
		this.frontend_ip = frontend_ip;
		this.port = Integer.valueOf(port);
		createOrderLogFile();
		//buy_timeLog = new File("./time_logs/orderServer_buy_timeLog.txt");
	}

	public void start() {
		port(this.port);

		get("/buy",(req, res) ->{
			String quantity_str = req.queryParams("quantity");
			//int quantity = Integer.parseInt(quantity_str);
			String id_str = req.queryParams("id");

			Map<String,Object> result = new HashMap<String, Object>();

			Response buyResponse = request("GET","http://"+cat_server_ip+"/buy?id="+id_str+"&quantity="+quantity_str);

			//record the order result and detect cat server failure
			if(buyResponse==null) {
				result.put("result", "fail");
				writeToLog("Book id: "+id_str+" Quantity: "+quantity_str+" sell status: fail");
			} else {
				result = buyResponse.json();
				invalidFontendCache(id_str);
				writeToLog("Book id: "+id_str+" Quantity: "+quantity_str+" after sell: "+result.get("cur_quantity")+" sell status: "+result.get("result"));
			}

			return result;
		},json());

		get("/heartbeat",(req, res) -> {
				return true;
		}, json());

		after((req, res) -> {
			res.type("application/json");
		});
	}

	//send to front end to invalidate cache
	public boolean invalidFontendCache(String id_str){
			Response resp = request("GET","http://"+this.frontend_ip+"/invalid?id="+id_str);
			if(resp==null) System.out.println("frontend is not up");
			else if(resp.status==200) return true;
			else return false;
			return true;
	}

	//create log to store the request messages
	public void createOrderLogFile(){
		orderLog = new File("./print_logs/order_log"+this.server_id+".txt");
		try{
			if(!orderLog.exists()){
				orderLog.createNewFile();
				System.out.println("File is created!");
			} else {
				orderLog.delete();
				orderLog.createNewFile();
				System.out.println("order log recreated!");
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	//write to the log file
	public void writeToLog(String s){
		Lock.lock();
		try{
			FileWriter fw = new FileWriter(orderLog, true);
			fw.write(s);
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			fw.write(" timeStamp: "+timeStamp);
			fw.write(System.getProperty("line.separator"));
			fw.flush();
			fw.close();
		} catch (IOException ex) {
            ex.printStackTrace();
        } finally {
        	Lock.unlock();
        }
	}
}
