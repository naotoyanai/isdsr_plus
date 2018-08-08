package ou.ist.de.srp.algo.ibsas_thres;

import it.unisa.dia.gas.jpbc.Element;

public class ISK {
	protected Element sk1;
	protected Element sk2;
	
	public String toString(){
		String ret="isk.sk1:"+sk1.toString()+"\n";
		ret+="isk.sk2:"+sk2.toString()+"\n";
		return ret;
	}
}
