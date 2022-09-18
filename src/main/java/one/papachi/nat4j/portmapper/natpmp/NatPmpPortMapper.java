package one.papachi.nat4j.portmapper.natpmp;

import one.papachi.nat4j.NatUtils;
import one.papachi.nat4j.portmapper.AbstractPortMapper;
import one.papachi.nat4j.portmapper.MappedPort;
import one.papachi.nat4j.portmapper.PortMapper;
import one.papachi.nat4j.portmapper.PortMapperProtocol;
import one.papachi.nat4j.portmapper.PortProtocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class NatPmpPortMapper extends AbstractPortMapper {
	
	public static final int NAT_PMP_PORT = 5351;
	
	public static List<PortMapper> find() {
		PortMapper portMapper = null;
		InetAddress localAddress = NatUtils.getLocalAddressToDefaultGatewayIPv4();
		InetAddress gatewayAddress = NatUtils.getDefaultGatewayIPv4();
		AtomicReference<InetAddress> reference = new AtomicReference<>();
		ResultCode resultCode = getExternalAddressRequest(localAddress, gatewayAddress, reference::setPlain);
		InetAddress externalAddress = reference.getPlain();
		if (localAddress != null && gatewayAddress != null && resultCode != ResultCode.NO_RESPONSE) {
			portMapper = new NatPmpPortMapper(localAddress, gatewayAddress, externalAddress);
		}
		return portMapper == null ? Collections.emptyList() : Collections.singletonList(portMapper);
	}
	
	protected NatPmpPortMapper(InetAddress localAddress, InetAddress gatewayAddress, InetAddress externalAddress) {
		super(PortMapperProtocol.NAT_PMP, localAddress, gatewayAddress, externalAddress);
	}

	@Override
	public Optional<MappedPort> mapPort(PortProtocol portProtocol, int internalPort, int externalPort, int lifetime) {
		MappedPort mappedPort = null;
		if (mapRequest(localAddress, gatewayAddress, portProtocol.getOpCode(), internalPort, externalPort, lifetime) == ResultCode.SUCCESS) {
			mappedPort = new NatPmpMappedPort(this, portProtocol, internalPort, externalPort, lifetime);
		}
		return Optional.ofNullable(mappedPort);
	}

	@Override
	public boolean refreshPort(MappedPort mappedPort) {
		return mapRequest(localAddress, gatewayAddress, mappedPort.getPortProtocol().getOpCode(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), mappedPort.getLifetime()) == ResultCode.SUCCESS;
	}

	@Override
	public void unmapPort(MappedPort mappedPort) {
		mapRequest(localAddress, gatewayAddress, mappedPort.getPortProtocol().getOpCode(), mappedPort.getInternalPort(), mappedPort.getExternalPort(), 0);
	}
	
	public static ResultCode mapRequest(InetAddress localAddress, InetAddress gatewayAddress, byte opCode, int internalPort, int externalPort, int lifetime) {
		byte[] request = ByteBuffer.allocate(12).put((byte) 0).put(opCode).putShort((short) 0).putShort((short) internalPort).putShort((short) externalPort).putInt(lifetime).array();
		byte[] response = NatUtils.performDatagramRequest(new InetSocketAddress(localAddress, 0), new InetSocketAddress(gatewayAddress, NAT_PMP_PORT), request);
		if (response != null && response.length == 16 && response[0] == 0 && ((response[1] & 0xFF) - 128) == (opCode & 0xFF)) {
			return ResultCode.values()[((response[2] & 0xFF) << 8) | (response[3] & 0xFF)];
		}
		return ResultCode.NO_RESPONSE;
	}

	public static ResultCode getExternalAddressRequest(InetAddress localAddress, InetAddress gatewayAddress, Consumer<InetAddress> consumer) {
		byte[] response = NatUtils.performDatagramRequest(new InetSocketAddress(localAddress, 0), new InetSocketAddress(gatewayAddress, NAT_PMP_PORT), new byte[2]);
		if (response != null && response.length == 12 && response[0] == 0 && (response[1] & 0xFF) == 128) {
			byte[] addr = new byte[4];
			System.arraycopy(response, 8, addr, 0, 4);
			consumer.accept(NatUtils.getInetAddress(addr));
			ResultCode resultCode = ResultCode.values()[((response[2] & 0xFF) << 8) | (response[3] & 0xFF)];
			return resultCode;
		}
		return ResultCode.NO_RESPONSE;
	}

	@Override
	public String toString() {
		return "NatPmpPortMapper(localAddress = " + localAddress + ", gatewayAddress = " + gatewayAddress + ", externalAddress = " + externalAddress + ")";
	}

}
