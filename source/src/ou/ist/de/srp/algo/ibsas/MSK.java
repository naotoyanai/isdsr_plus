package ou.ist.de.srp.algo.ibsas;

import it.unisa.dia.gas.jpbc.Element;

public class MSK {
	protected Element a1;
	protected Element a2;
	
	public String toString(){
		String ret="msk.a1:"+a1.toString()+"\n";
		ret+="msk.a2:"+a2.toString()+"\n";
		return ret;
	}
}
