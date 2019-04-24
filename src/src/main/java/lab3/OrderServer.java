package lab3;

import static spark.Spark.*;
import static lab3.JsonUtil.*;
import static lab3.HttpUtil.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

public class OrderServer {

	private File orderLog;
	private File buy_timeLog;
	private String cat_server_ip;
	private String frontend_ip;

	//constructor
	public OrderServer(String cat_server_ip, String frontend_ip){
		this.cat_server_ip = cat_server_ip;
		this.frontend_ip = frontend_ip;
		//createOrderLogFile();
		//buy_timeLog = new File("./time_logs/orderServer_buy_timeLog.txt");
	}

	public void start() {
		port(3801);

		get("/buy",(req, res) ->{
			String quantity_str = req.queryParams("quantity");
			//int quantity = Integer.parseInt(quantity_str);
			String id_str = req.queryParams("id");

			Map<String,Object> result = new HashMap<String, Object>();

			//long startTime = System.currentTimeMillis();
			Response buyResponse = request("GET","http://"+cat_server_ip+":3154/buy?id="+id_str+"&quantity="+quantity_str);
			//long endTime = System.currentTimeMillis();
			//recordTime(endTime-startTime, buy_timeLog);

			if(buyResponse==null) {
				result.put("result", "fail");
				//writeToLog("Book id: "+id_str+" Quantity: "+quantity_str+" sell status: fail");
			} else {
				result = buyResponse.json();
				invalidFontendCache(id_str);
				//writeToLog("Book id: "+id_str+" Quantity: "+quantity_str+" after sell: "+result.get("cur_quantity")+" sell status: "+result.get("result"));
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

	public boolean invalidFontendCache(String id_str){
			Response resp = request("GET","http://"+this.frontend_ip+":3800/invalid?id="+id_str);
			if(resp==null) System.out.println("frontend is not up");
			else if(resp.status==200) return true;
			else return false;
			return true;
	}

	//create log to store the request messages
	public void createOrderLogFile(){
		orderLog = new File("./order_log.txt");
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
		try{
			FileWriter fw = new FileWriter("order_log.txt", true);
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

	//record the message latency
	public void recordTime(long timeUsed, File timeLog){
		try{
			if(!timeLog.exists()){
				timeLog.createNewFile();
			}
			FileWriter fw = new FileWriter(timeLog, true);
			fw.write(String.valueOf(timeUsed));
			fw.write(System.getProperty("line.separator"));
			fw.flush();
			fw.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
