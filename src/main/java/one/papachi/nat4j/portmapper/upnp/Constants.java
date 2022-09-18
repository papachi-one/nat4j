package one.papachi.nat4j.portmapper.upnp;

import java.net.InetAddress;

public class Constants {

	public static final int SSDP_PORT = 1900;
	
	public static InetAddress IPV4_ALL;

	public static InetAddress IPV4_MULTICAST_ADDRESS;
	
	public static InetAddress IPV6_ALL;

	public static InetAddress IPV6_MULTICAST_ADDRESS_LINK_LOCAL;
	
	public static InetAddress IPV6_MULTICAST_ADDRESS_SITE_LOCAL;
	
	public static InetAddress IPV6_MULTICAST_ADDRESS_ORGANIZATION_LOCAL;
	
	public static InetAddress IPV6_MULTICAST_ADDRESS_GLOBAL;
	
	static {
		try {
			IPV4_ALL = InetAddress.getByName("0.0.0.0");
			IPV4_MULTICAST_ADDRESS = InetAddress.getByName("239.255.255.250");
			IPV6_ALL = InetAddress.getByName("::");
			IPV6_MULTICAST_ADDRESS_LINK_LOCAL = InetAddress.getByName("[ff02::c]");
			IPV6_MULTICAST_ADDRESS_SITE_LOCAL = InetAddress.getByName("[ff05::c]");
			IPV6_MULTICAST_ADDRESS_ORGANIZATION_LOCAL = InetAddress.getByName("[ff08::c]");
			IPV6_MULTICAST_ADDRESS_GLOBAL = InetAddress.getByName("[ff0e::c]");
		} catch (Exception e) {
		}
	}

	public static final String M_SEARCH = "M-SEARCH * HTTP/1.1\r\nHOST: <IP>:<PORT>\r\nST: <ST>\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\n\r\n";
	
	public static final String ST_WANIPConnection = "urn:schemas-upnp-org:service:WANIPConnection:1";
	
	public static final String ST_WANPPPConnection = "urn:schemas-upnp-org:service:WANPPPConnection:1";
	
	public static final String ST_WANIPv6FirewallControl = "urn:schemas-upnp-org:service:WANIPv6FirewallControl:1";
	
}
