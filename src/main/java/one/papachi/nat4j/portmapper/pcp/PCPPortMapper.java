package one.papachi.nat4j.portmapper.pcp;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import one.papachi.nat4j.NatUtils;
import one.papachi.nat4j.portmapper.AbstractPortMapper;
import one.papachi.nat4j.portmapper.MappedPort;
import one.papachi.nat4j.portmapper.PortMapper;
import one.papachi.nat4j.portmapper.PortMapperProtocol;
import one.papachi.nat4j.portmapper.PortProtocol;

public class PCPPortMapper extends AbstractPortMapper {
	
	public static void main(String[] args) throws Exception {
		List<PortMapper> find = find();
		find.forEach(System.out::println);
	}
	
	public static final int PORT = 5351;

	public static Optional<PortMapper> findIPv4() {
		PortMapper result = null;
		try {
			Inet4Address localAddress = NatUtils.getLocalAddressToDefaultGatewayIPv4();
			Inet4Address gatewayAddress = NatUtils.getDefaultGatewayIPv4();
			int lifetime = 60;
			int protocol = 6;
			int internalPort = 7;
			int suggestedExternalPort = 0;
			InetAddress suggestedExternalAddress = NatUtils.getInetAddress("0.0.0.0");
			AtomicReference<InetAddress> reference = new AtomicReference<>();
			ResultCode resultCode = map(localAddress, gatewayAddress, lifetime, Utils.generateNonce(), protocol, internalPort, suggestedExternalPort, suggestedExternalAddress, inetSocketAddress -> reference.setPlain(inetSocketAddress.getAddress()));
			InetAddress externalAddress = reference.getPlain();
			if (resultCode != ResultCode.NO_RESPONSE) {
				result = new PCPPortMapper(localAddress, gatewayAddress, externalAddress);
			}
		} catch (Exception e) {
		}
		return Optional.ofNullable(result);
	}
	
	public static List<PortMapper> findIPv6() {
		List<PortMapper> result = new LinkedList<>();
		List<Inet6Address> localAddresses = NatUtils.getLocalAddressesToDefaultGatewayIPv6();
		Inet6Address gatewayAddress = NatUtils.getDefaultGatewayIPv6();
		for (Inet6Address localAddress : localAddresses) {
			try {
				int lifetime = 60;
				int protocol = 6;
				int internalPort = 7;
				int suggestedExternalPort = 0;
				InetAddress suggestedExternalAddress = NatUtils.getInetAddress("::");
				AtomicReference<InetAddress> reference = new AtomicReference<>();
				ResultCode resultCode = map(localAddress, gatewayAddress, lifetime, Utils.generateNonce(), protocol, internalPort, suggestedExternalPort, suggestedExternalAddress, inetSocketAddress -> reference.setPlain(inetSocketAddress.getAddress()));
				InetAddress externalAddress = reference.getPlain();
				if (resultCode != ResultCode.NO_RESPONSE) {
					result.add(new PCPPortMapper(localAddress, gatewayAddress, externalAddress));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public static List<PortMapper> find() {
		List<PortMapper> result = new LinkedList<>();
		findIPv4().ifPresent(result::add);
		findIPv6().forEach(result::add);
		return result;
	}
	
	public PCPPortMapper(InetAddress localAddress, InetAddress gatewayAddress, InetAddress externalAddress) {
		super(PortMapperProtocol.PCP, localAddress, gatewayAddress, externalAddress);
	}

	@Override
	public Optional<MappedPort> mapPort(PortProtocol portProtocol, int internalPort, int externalPort, int lifetime) {
		MappedPort result = null;
		AtomicReference<InetSocketAddress> reference = new AtomicReference<>();
		byte[] nonce = Utils.generateNonce();
		ResultCode resultCode = map(localAddress, gatewayAddress, lifetime, nonce, portProtocol.getProtocolNumber(), internalPort, externalPort, null, reference::setPlain);
		if (resultCode == ResultCode.SUCCESS) {
			InetSocketAddress inetSocketAddress = reference.getPlain();
			InetAddress externalAddress = inetSocketAddress.getAddress();
			externalPort = inetSocketAddress.getPort();
			result = new PCPMappedPort(this, portProtocol, internalPort, externalPort, lifetime, externalAddress, nonce);
		}
		return Optional.ofNullable(result);
	}

	@Override
	public boolean refreshPort(MappedPort mappedPort) {
		PCPMappedPort pcpMappedPort = (PCPMappedPort) mappedPort;
		ResultCode resultCode = map(localAddress, gatewayAddress, pcpMappedPort.getLifetime(), pcpMappedPort.getNonce(), pcpMappedPort.getPortProtocol().getProtocolNumber(), pcpMappedPort.getInternalPort(), pcpMappedPort.getExternalPort(), pcpMappedPort.getExternalAddress(), null);
		return resultCode == ResultCode.SUCCESS;
	}

	@Override
	public void unmapPort(MappedPort mappedPort) {
		PCPMappedPort pcpMappedPort = (PCPMappedPort) mappedPort;
		map(localAddress, gatewayAddress, 10, pcpMappedPort.getNonce(), pcpMappedPort.getPortProtocol().getProtocolNumber(), pcpMappedPort.getInternalPort(), pcpMappedPort.getExternalPort(), pcpMappedPort.getExternalAddress(), null);
	}
	
	public static ResultCode map(InetAddress localAddress, InetAddress gatewayAddress, int requestedLifetime, byte[] nonce, int protocol, int internalPort, int externalPort, InetAddress externalAddress, Consumer<InetSocketAddress> consumer) {
		byte[] request = new byte[60];
		ByteBuffer buffer = ByteBuffer.wrap(request);
		buffer.put((byte) 2);
		buffer.put((byte) 1);
		buffer.putShort((short) 0);
		buffer.putInt(requestedLifetime);
		buffer.put(Utils.getIPv6AddressArray(localAddress));
		
		buffer.put(nonce != null && nonce.length == 12 ? nonce : new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
		buffer.put((byte) protocol);
		buffer.put(new byte[] {0, 0, 0});
		buffer.putShort((short) internalPort);
		buffer.putShort((short) externalPort);
		buffer.put(Utils.getIPv6AddressArray(externalAddress != null ? externalAddress : gatewayAddress));
		
		byte[] response = NatUtils.performDatagramRequest(new InetSocketAddress(localAddress, 0), new InetSocketAddress(gatewayAddress, PORT), request);
		if (response != null && response.length == 60 && response[0] == 2 && (response[1] & 0b0111_1111) == 1) {
			ResultCode resultCode = ResultCode.values()[response[3] & 0xFF];
			if (resultCode == ResultCode.SUCCESS) {
				int assignedExternalPort = ((response[41] & 0xFF) << 8) | (response[42] & 0xFF);
				byte[] address = new byte[16];
				System.arraycopy(response, 43, address, 0, 16);
				InetAddress assignedExternalAddress = NatUtils.getInetAddress(address);
				InetSocketAddress inetSocketAddress = new InetSocketAddress(assignedExternalAddress, assignedExternalPort);
				Optional.ofNullable(consumer).ifPresent(c -> c.accept(inetSocketAddress));
			}
			return resultCode;
		}
		return ResultCode.NO_RESPONSE;
	}

	@Override
	public String toString() {
		return "PCPPortMapper(localAddress = " + localAddress + ", gatewayAddress = " + gatewayAddress + ", externalAddress = " + externalAddress + ")";
	}

}
