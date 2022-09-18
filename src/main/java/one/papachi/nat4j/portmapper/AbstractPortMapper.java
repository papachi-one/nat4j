package one.papachi.nat4j.portmapper;

import java.net.InetAddress;

public abstract class AbstractPortMapper implements PortMapper {
	
	protected final PortMapperProtocol portMapperProtocol;
	
	protected final InetAddress localAddress;
	
	protected final InetAddress gatewayAddress;
	
	protected final InetAddress externalAddress;
	
	protected AbstractPortMapper(PortMapperProtocol portMapperProtocol, InetAddress localAddress, InetAddress gatewayAddress, InetAddress externalAddress) {
		this.portMapperProtocol = portMapperProtocol;
		this.localAddress = localAddress;
		this.gatewayAddress = gatewayAddress;
		this.externalAddress = externalAddress;
	}

	@Override
	public PortMapperProtocol getPortMapperProtocol() {
		return portMapperProtocol;
	}

	@Override
	public InetAddress getLocalAddress() {
		return localAddress;
	}
	
	@Override
	public InetAddress getGatewayAddress() {
		return gatewayAddress;
	}
	
	@Override
	public InetAddress getExternalAddress() {
		return externalAddress;
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "(portMapperProtocol = " + portMapperProtocol + ", localAddress = " + localAddress + ", gatewayAddress = " + gatewayAddress + ", externalAddress = " + externalAddress + ")";
	}
	
}
