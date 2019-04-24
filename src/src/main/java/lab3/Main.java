package lab3;

public class Main {
	public static void main(String[] args) {
		if("CatalogServer".equals(args[0])){
												//String server_id, String replica_ip
			CatalogServer cat_server = new CatalogServer(args[1], args[2]);
			cat_server.start();
		}
		else if("OrderServer".equals(args[0])){
								//String server_id, String cat_server_ip, String frontend_ip
			OrderServer o_server = new OrderServer(args[1], args[2], args[3]);
			o_server.start();
		}
		else if("FrontendServer".equals(args[0])){
					//String cat_server_ip0, String order_server_ip0,
				//String cat_server_ip1, String order_server_ip1
			FrontendServer f_server = new FrontendServer(args[1], args[2], args[3], args[4]);
			f_server.start();
		}
		else if("Client".equals(args[0])){
				//String name, String frontend_server_ip
			for(int i=0; i<Integer.parseInt(args[2]); i++){
				new Client(Integer.toString(i), args[1]);
			}
		} else {
			System.out.println("please type CatalogServer/OrderServer/FrontendServer/Client");
		}
		//FrontendServer f_server = new FrontendServer("localhost", "localhost", "localhost", "localhost");
		//f_server.start();
		//CatalogServer cat_server = new CatalogServer("0", "localhost");
		//cat_server.start();
		//OrderServer o_server = new OrderServer("0", "localhost", "localhost");
		//o_server.start();
	}
}
