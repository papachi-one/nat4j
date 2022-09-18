package one.papachi.nat4j.portmapper.pcp;

import java.net.InetAddress;
import java.util.Random;

class Utils {

	public static byte[] getIPv6AddressArray(InetAddress inetAddress) {
		byte[] address = inetAddress.getAddress();
		return address.length == 16 ? address : new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xff, (byte) 0xff, address[0], address[1], address[2], address[3]};
	}
	
	public static byte[] generateNonce() {
		byte[] nonce = new byte[12];
		new Random().nextBytes(nonce);
		return nonce;
	}
	
}
