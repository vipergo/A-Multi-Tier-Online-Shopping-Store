package lab3;
import java.util.ArrayList;
import java.util.HashSet;

public class ClusterClass {
	private ArrayList<Integer> cluster_list;
	private HashSet<Integer> cluster_set;

	ClusterClass(){
		cluster_list = new ArrayList<>();
		cluster_set = new HashSet<Integer>();
	}

	public void add(int id){
		if(!cluster_set.contains(id)){
			cluster_list.add(id);
			cluster_set.add(id);
		}
	}

	public int size(){
		return cluster_list.size();
	}

	public Integer get(int index){
		return cluster_list.get(index);
	}

	public void remove(int id){
		if(cluster_set.contains(id)){
			cluster_list.remove(new Integer(id));
			cluster_set.remove(id);
		}
		/*
		for(Integer x : cluster_list){
			System.out.println("===");
			System.out.println(x);
		}
		*/
	}
}
