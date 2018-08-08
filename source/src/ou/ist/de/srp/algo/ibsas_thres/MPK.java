package ou.ist.de.srp.algo.ibsas_thres;

import it.unisa.dia.gas.jpbc.Element;

public class MPK {
	protected Element g1;
	protected Element g2;
	protected Element g3;
	
	public String toString(){
		String ret="mpk.g1:"+g1.toString()+"\n";
		ret+="mpk.g2:"+g2.toString()+"\n";
		ret+="mpk.g3:"+g3.toString()+"\n";
		return ret;
	}
}
