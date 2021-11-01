module one.papachi.nat4j.lib {
	requires java.xml;
	requires java.net.http;
	exports one.papachi.nat4j.lib;
	exports one.papachi.nat4j.lib.portmapper;
	exports one.papachi.nat4j.lib.portmapper.natpmp;
	exports one.papachi.nat4j.lib.portmapper.pcp;
	exports one.papachi.nat4j.lib.portmapper.upnp;
}