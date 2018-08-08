package ou.ist.de.srp.packet.dsrbase;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import ou.ist.de.srp.Constants;
import ou.ist.de.srp.packet.Packet;
import ou.ist.de.srp.packet.RouteStrage;


public class DSRBasePacket extends Packet {

	protected RouteInfo ri;
	

	public DSRBasePacket() {
		super();
		ri=new RouteInfo();
	}
	public RouteInfo getRi() {
		return ri;
	}

	public void setRi(RouteInfo ri) {
		this.ri = ri;
	}

	@Override
	protected int getExtraPacketLength() {
		// TODO Auto-generated method stub
		return Integer.BYTES+ri.dataLength();//Integer.BYTES represents an integer value for the number of nodes stored in ri
	}

	@Override
	protected ByteBuffer extraToByteArray(ByteBuffer bb) {
		// TODO Auto-generated method stub
		try {
			ri.toByteArray(bb);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return bb;
	}

	@Override
	protected void extraFromByteArray(ByteBuffer bb) {
		// TODO Auto-generated method stub
		try {
			ri.fromByteArray(bb);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public String toString() {
		String ret=super.toString();
		ret+= "ri:"+ri.toString();
		return ret;
	}
	public class RouteInfo extends RouteStrage<InetAddress>{

		public String getAddressByString(int index) {
			return al.get(index).getHostAddress();
		}
		public InetAddress get(int index) {
			return al.get(index);
		}
		public String[] getAddrArray(){
			if(al.isEmpty()){
				return null;
			}
			String[] ret=new String[al.size()];
			for(int i=0;i<al.size();i++){
				ret[i]=al.get(i).getHostAddress();
			}
			return ret;
		}
		public String getAddrSequence(){
			String ret="";
			if(al.isEmpty()){
				return ret;
			}
			for(int i=0;i<al.size();i++){
				ret+=al.get(i).getHostAddress();
			}
			return ret;
		}
		public boolean findNode(InetAddress addr) {
			for(InetAddress a:al) {
				if(addr.equals(a)) {
					return true;
				}
			}
			return false;
		}
		public int findNodeIndex(InetAddress addr) {
			int ret=-1;
			for(int i=al.size()-1;i>=0;i--) {
				if(addr.equals(al.get(i))) {
					System.out.println("i="+i+" addr="+addr.toString()+" comparison="+al.get(i).toString());
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
			for(InetAddress ina:al){
				ret+=ina.getHostAddress()+":";
			}
			return ret;
		}

		@Override
		public int dataLength() {
			// TODO Auto-generated method stub
			return al.size()*Constants.InetAddressLength;
		}
		@Override
		public ByteBuffer toByteArray(ByteBuffer bb) {
			// TODO Auto-generated method stub
						
			bb.putInt(getRiLength());
			for (int i = 0; i < al.size(); i++) {
				bb.put(al.get(i).getAddress());
			}
			return bb;
		}

		@Override
		public void fromByteArray(ByteBuffer bb) {
			// TODO Auto-generated method stub
			System.out.println("from byte array in dsr base route info");
			byte[] tmp;
			int length;
			al.clear();
			try {
				length=bb.getInt();
				for(int i=0;i<length;i++) {
					tmp=new byte[Constants.InetAddressLength];
					bb.get(tmp);
					add(InetAddress.getByAddress(tmp));
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}


