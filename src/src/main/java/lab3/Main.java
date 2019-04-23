package lab3;

public class Main {
	public static void main(String[] args) {
		/*
		if("CatalogServer".equals(args[0])){
			CatalogServer cat_server = new CatalogServer();
			cat_server.start();
		}
		else if("OrderServer".equals(args[0])){
			OrderServer o_server = new OrderServer(args[1]);
			o_server.start();
		}
		else if("FrontendServer".equals(args[0])){
			FrontendServer f_server = new FrontendServer(args[1], args[2]);
			f_server.start();
		}
		else if("client".equals(args[0])){
			for(int i=0; i<Integer.parseInt(args[2]); i++){
				client c1 = new client("client_lookup", args[1]);
				c1.start();
				client c2 = new client("client_search", args[1]);
				c2.start();
				client c3 = new client("client_buy", args[1]);
				c3.start();
			}
		} else {
			System.out.println("please type CatalogServer/OrderServer/FrontendServer/client");
		}
		*/
		//FrontendServer f_server = new FrontendServer("localhost", "localhost");
		//f_server.start();
		//CatalogServer cat_server = new CatalogServer("localhost", "localhost");
		//cat_server.start();
		OrderServer o_server = new OrderServer("localhost");
		o_server.start();
	}
}
