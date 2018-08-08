package ou.ist.de.srp.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public abstract class RouteStrage<T> {
	protected ArrayList<T> al;

	public RouteStrage() {
		al = new ArrayList<T>();
	}
	public void add(T elem) {
		al.add(elem);
	}
	public T get(int index) {
		if(al.isEmpty()) {
			return null;
		}
		return al.get(index);
	}
	public int getRiLength() {
		return al.size();
	}
	public abstract int dataLength();
	public abstract ByteBuffer toByteArray(ByteBuffer bb);
	public abstract void fromByteArray(ByteBuffer bb);
	public boolean find(T elem) {
		if(al.isEmpty()) {
			return false;
		}
		for(T t:al) {
			if(elem.equals(t)) {
				return true;
			}
		}
		return false;
	}

	public int findIndex(T elem) {
		int ret=-1;
		for(int i=al.size()-1;i>=0;i--) {
			if(elem.equals(al.get(i))) {
				System.out.println("i="+i+" addr="+elem.toString()+" comparison="+al.get(i).toString());
				return i;
			}
		}
		
		return ret;
	}
	
	public String toString(){
		String ret="";
		
		if(al.isEmpty()){
			return null;
		}
		for(T ina:al){
			ret+=ina.toString()+":";
		}
		return ret;
	}
	
}
