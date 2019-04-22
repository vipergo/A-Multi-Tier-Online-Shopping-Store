package lab3;

public class Book {
	private String title;
	private String topic;
	private int quantity;
	private double price;
	private int id;

	//constructor
	public Book(String title,String topic,double price,int id) {
		this.title=title;
		this.topic=topic;
		this.price=price;
		this.id = id;
	}

	//getters
	public String getTitle() {
		return title;
	}
	public String getTopic() {
		return topic;
	}
	public int getQuantity() {
		return quantity;
	}
	public double getPrice() {
		return price;
	}
	public int getID() {
		return id;
	}

	//setter
	public void updatePrice(int newPrice){
		price = newPrice;
	}
}
