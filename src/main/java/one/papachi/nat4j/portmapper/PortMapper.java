package one.papachi.nat4j.portmapper;

import java.net.InetAddress;
import java.util.Optional;

public interface PortMapper {

	PortMapperProtocol getPortMapperProtocol();
	
	InetAddress getLocalAddress();
	
	InetAddress getGatewayAddress();
	
	InetAddress getExternalAddress();
	
	Optional<MappedPort> mapPort(PortProtocol portProtocol, int internalPort, int externalPort, int lifetime);
	
	boolean refreshPort(MappedPort mappedPort);

	void unmapPort(MappedPort mappedPort);
}
