package one.papachi.nat4j.portmapper.natpmp;

import one.papachi.nat4j.portmapper.AbstractMappedPort;
import one.papachi.nat4j.portmapper.PortProtocol;

public class NatPmpMappedPort extends AbstractMappedPort {

	protected NatPmpMappedPort(NatPmpPortMapper portMapper, PortProtocol portProtocol, int internalPort, int externalPort, int lifetime) {
		super(portMapper, portProtocol, internalPort, externalPort, lifetime);
	}

}
