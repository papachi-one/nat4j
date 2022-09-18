package one.papachi.nat4j.portmapper.pcp;

import java.net.InetAddress;

import one.papachi.nat4j.portmapper.AbstractMappedPort;
import one.papachi.nat4j.portmapper.PortProtocol;

public class PCPMappedPort extends AbstractMappedPort {

	private final InetAddress externalAddress;
	
	private final byte[] nonce;
	
	public PCPMappedPort(PCPPortMapper portMapper, PortProtocol portProtocol, int internalPort, int externalPort, int lifetime, InetAddress externalAddress, byte[] nonce) {
		super(portMapper, portProtocol, internalPort, externalPort, lifetime);
		this.externalAddress = externalAddress;
		this.nonce = nonce;
	}

	public InetAddress getExternalAddress() {
		return externalAddress;
	}
	
	public byte[] getNonce() {
		return nonce;
	}
	
}
