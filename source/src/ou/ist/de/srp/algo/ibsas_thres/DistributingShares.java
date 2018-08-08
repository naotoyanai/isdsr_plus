package ou.ist.de.srp.algo.ibsas_thres;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

public class DistributingShares {
	protected Element a10;
	protected Element[] a1ij;
	protected Element a20;
	protected Element[] a2ij;
	
	protected void generateDistributingShares(int total, int shares, Pairing pairing) {
		Element[] secrets1=new Element[shares];
		Element[] secrets2=new Element[shares];
		for (int i = 0; i < shares; i++) {
			secrets1[i] = pairing.getZr().newRandomElement();
			secrets2[i] = pairing.getZr().newRandomElement();
		}
		a10=secrets1[0];
		a20=secrets2[0];
		a1ij=new Element[total+1];
		a2ij=new Element[total+1];
		for(int i=0;i<total;i++) {
			a1ij[i+1]=polynomialEvaluation(i + 1, shares, secrets1,pairing);
			a2ij[i+1]=polynomialEvaluation(i + 1, shares, secrets2,pairing);
		}
	}
	protected Element polynomialEvaluation(int x, int size, Element[] a,Pairing pairing) {
		Element y = pairing.getZr().newElement().setToZero();
		Element xx = pairing.getZr().newElement().setToOne();
		Element tmpa;
		for (int i = 0; i < size; i++) {
			Element xval = pairing.getZr().newElement().set(x);
			tmpa=a[i].duplicate().mul(xx);
			y=y.add(tmpa);
			xx=xx.mul(xval);
		}
		return y;
	}
}
