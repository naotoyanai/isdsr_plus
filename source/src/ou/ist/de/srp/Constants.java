package ou.ist.de.srp;

public class Constants {
	public static int InetAddressLength=4;
	public static int PORT=30000;
	public static int RCVBUFFER=30000;
	public static String BROAD_CAST_ADDR="10.255.255.255";
	public static enum PktType{REQ,REP,ERR,DATA};
	public static byte REQ=0;
	public static byte REP=1;
	public static byte ERR=2;
	public static byte DATA=4;
	public static byte FRAGMENTED=8;
	public static long timer;
	public static boolean wait;
	
}
