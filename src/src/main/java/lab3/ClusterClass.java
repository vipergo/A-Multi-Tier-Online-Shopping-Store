package lab3;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

//for constant time removing and random selecting a cluster id
public class ClusterClass {
	private ArrayList<Integer> cluster_list;
	private HashSet<Integer> cluster_set;
	private final ReentrantLock Lock = new ReentrantLock();

	ClusterClass(){
		cluster_list = new ArrayList<>();
		cluster_set = new HashSet<Integer>();
	}

	public void add(int id){
		Lock.lock();
		try{
			if(!cluster_set.contains(id)){
				cluster_list.add(id);
				cluster_set.add(id);
			}
		} finally {
			Lock.unlock();
		}

	}

	public int size(){
		return cluster_list.size();
	}

	public Integer get(int index){
		return cluster_list.get(index);
	}

	public void remove(int id){
		Lock.lock();
		try{
			if(cluster_set.contains(id)){
				cluster_list.remove(new Integer(id));
				cluster_set.remove(id);
			}
		}finally {
			Lock.unlock();
		}
	}
}
