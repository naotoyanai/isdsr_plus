package ou.ist.de.srp.algo.ibsas;


import java.io.Serializable;

public class MasterKey implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 777499020697297341L;
	public String paramFile;
	public Elements[] ale;
	
	public MasterKey(){
		
	}
	public MasterKey(int num){
		ale=new Elements[num];
	}
	public class Elements implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7795506650471853238L;
		public byte[] g;
		public byte[] a1;
		public byte[] a2;
	}
}
