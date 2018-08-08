package ou.ist.de.srp.algo.ibsas_thres;

import it.unisa.dia.gas.jpbc.Element;

public class ReceivedShares {
	protected Element a1i0g;
	protected Element a1ij;
	protected Element a2i0g;
	protected Element a2ij;
	protected Element a1jH;
	protected Element a2jH;
	
	public boolean allSharesReceived() {
		return a1i0g!=null&&a1ij!=null&&a2i0g!=null&&a2ij!=null;
	}
}
