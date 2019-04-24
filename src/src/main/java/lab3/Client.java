package lab3;

import static lab3.HttpUtil.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
	private String client_name;
	private String frontend_server_ip;

	private String[] topicArr = {"distributed_systems", "graduate_school", "lab3"};

	private File lookup_timeLog;
	private File search_timeLog;
	private File buy_timeLog;
	private File clientLog;

	private final ReentrantLock Lock = new ReentrantLock();

	//constructor
	public Client(String name, String frontend_server_ip){
		client_name = name;
		this.frontend_server_ip = frontend_server_ip;
		lookup_timeLog = new File("./time_logs/client_lookup_timeLog_"+client_name+".txt");
		search_timeLog = new File("./time_logs/client_search_timeLog_"+client_name+".txt");
		buy_timeLog = new File("./time_logs/client_buy_timeLog_"+client_name+".txt");
		clientLog = new File("./print_logs/client_log_"+client_name+".txt");
		this.start();
	}

	//allow client to send requests concurrently
	public void start(){
		Runnable beep = () -> {
			System.out.println("I'm client "+client_name);
			int counter = 0;
			Random rand = new Random();
			while(counter<1100){
				int mod = counter%11;
				if(mod==10) buyReq(1, 1);
				else if(mod<7) lookupReq(1);
				else searchReq(topicArr[0]);
				/*
				try{
					Thread.sleep(1000);
				} catch (InterruptedException e) {
	        		e.printStackTrace();
	    		}
	    		*/
	    		counter++;
			}
		};
		Thread t = new Thread(beep);
	    t.start();

	}

	public Map<String, Object> lookupReq(int id){
		String urlString =  "http://"+frontend_server_ip+":3800/lookup?id="+String.valueOf(id);

		long startTime = System.currentTimeMillis();
		Response serverResponse = request("GET",urlString);
		long endTime = System.currentTimeMillis();
		recordTime(endTime - startTime, lookup_timeLog);
		Map<String, Object> result = serverResponse.json();
		writeToLog("lookup:  bookid="+String.valueOf(id)+" title="+result.get("title")+" cur_quantity="+result.get("cur_quantity"));
		return serverResponse.json();
	}

	public Map<String, Object> searchReq(String topic){
		String urlString =  "http://"+frontend_server_ip+":3800/search?topic="+topic;

		long startTime = System.currentTimeMillis();
        Response serverResponse = request("GET",urlString);
		long endTime = System.currentTimeMillis();
		recordTime(endTime - startTime, search_timeLog);
		Map<String, Object> result = serverResponse.json();

		writeToLog("search: "+topic+" result= "+result.toString());
		return serverResponse.json();
	}

	public Map<String, Object> buyReq(int id, int quantity){

		String urlString =  "http://"+frontend_server_ip+":3800/buy?id="+String.valueOf(id)+"&quantity="+String.valueOf(quantity);

		long startTime = System.currentTimeMillis();
        Response serverResponse = request("GET",urlString);
		long endTime = System.currentTimeMillis();
		recordTime(endTime - startTime, buy_timeLog);
		Map<String, Object> result = serverResponse.json();
		writeToLog("buy: bookid="+String.valueOf(id)+" quantity="+String.valueOf(quantity)+" result="+result.get("result"));
		return serverResponse.json();
	}

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

	public void writeToLog(String s){
		Lock.lock();
		try{
			if(!clientLog.exists()){
				clientLog.createNewFile();
			}
			FileWriter fw = new FileWriter(clientLog, true);
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
