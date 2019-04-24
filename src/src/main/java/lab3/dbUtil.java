package lab3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class dbUtil {
	//create the database with the table
	public static void initDB(String server_id) {
		Connection connection = null;
		try{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:lab3_"+server_id+".db");
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.

			statement.executeUpdate("drop table if exists book");
			statement.executeUpdate("create table book (id integer, title string, topic string, price double, quantity integer)");
			statement.executeUpdate("insert into book values(1, 'How to get a good grade in 677 in 20 minutes a day.', 'distributed_systems', 100, 2000)");
			statement.executeUpdate("insert into book values(2, 'RPCs for Dummies.', 'distributed_systems', 20, 2000)");
			statement.executeUpdate("insert into book values(3, 'Xen and the Art of Surviving Graduate School.', 'graduate_school', 200, 2000)");
			statement.executeUpdate("insert into book values(4, 'Cooking for the Impatient Graduate Student.', 'graduate_school', 250, 2000)");
			statement.executeUpdate("insert into book values(5, 'How to finish Project 3 on time', 'lab3', 1000, 2000)");
			statement.executeUpdate("insert into book values(6, 'Why theory classes are so hard', 'lab3', 1000, 2000)");
			statement.executeUpdate("insert into book values(7, 'Spring in the Pioneer Valley', 'lab3', 1000, 2000)");
			ResultSet rs = statement.executeQuery("select * from book");
			while(rs.next()){
				// read the result set
				System.out.println("tile = " + rs.getString("title")+"topic = " + rs.getString("topic")+"price = " + rs.getDouble("price")+"quantity= " +rs.getString("quantity"));
			}
		}
	    catch(SQLException e){
	      // if the error message is "out of memory",
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
	    finally{
	      try{
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e){
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
	}

	//allow catalog server to increase or decrease the stock
	public static boolean UpdateDB(int id, int quantity, String server_id) {
		Connection connection = null;
		try{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:lab3_"+server_id+".db");
			if(quantity>=0){
				PreparedStatement pstmt = connection.prepareStatement("UPDATE book SET quantity=? WHERE id=?");
				pstmt.setInt(1, quantity);
				pstmt.setInt(2, id);
				pstmt.executeUpdate();
				System.out.println("Update DB success");
			} else {
				System.out.println("something seriously wrong!");
			}
		}
	    catch(SQLException e){
	      // if the error message is "out of memory",
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
	    finally{
	      try{
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e){
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
		return true;
	}

	public static int queryDB(int id, String server_id) {
		Connection connection = null;
		int result = -1;
		try{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:lab3_"+server_id+".db");
			PreparedStatement pstmt = connection.prepareStatement("SELECT quantity FROM book WHERE id=?");
			pstmt.setInt(1, id);

			ResultSet rs = pstmt.executeQuery();
			result = rs.getInt("quantity");
		}catch(SQLException e){
	      // if the error message is "out of memory",
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
	    finally{
	      try{
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e){
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
		return result;
	}

	//allow catalog server to add more books
	public static int moreStock(int id, String server_id){
		Connection connection = null;
		int result = 0;
		try{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:lab3_"+server_id+".db");
			PreparedStatement pstmt = connection.prepareStatement("SELECT quantity FROM book WHERE id=?");
			pstmt.setInt(1, id);

			ResultSet rs = pstmt.executeQuery();
			int updatedVal = rs.getInt("quantity")+5;
			System.out.println("start add new stock");
			pstmt = connection.prepareStatement("UPDATE book SET quantity=? WHERE id=?");
			pstmt.setInt(1, updatedVal);
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			System.out.println("add new stock success");
			result = updatedVal;
		}
	    catch(SQLException e){
	      // if the error message is "out of memory",
	      // it probably means no database file is found
	      System.err.println(e.getMessage());
	    }
	    finally{
	      try{
	        if(connection != null)
	          connection.close();
	      }
	      catch(SQLException e){
	        // connection close failed.
	        System.err.println(e);
	      }
	    }
		return result;
	}

}

