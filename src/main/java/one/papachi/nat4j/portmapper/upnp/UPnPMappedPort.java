package one.papachi.nat4j.portmapper.upnp;

import one.papachi.nat4j.portmapper.AbstractMappedPort;
import one.papachi.nat4j.portmapper.PortProtocol;

public class UPnPMappedPort extends AbstractMappedPort {
	
	protected UPnPMappedPort(UPnPPortMapper portMapper, PortProtocol portProtocol, int internalPort, int externalPort, int lifetime) {
		super(portMapper, portProtocol, internalPort, externalPort, lifetime);
	}
	
}