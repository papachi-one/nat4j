package one.papachi.nat4j.portmapper.upnp;

import one.papachi.nat4j.NatUtils;
import one.papachi.nat4j.portmapper.AbstractPortMapper;
import one.papachi.nat4j.portmapper.MappedPort;
import one.papachi.nat4j.portmapper.PortMapper;
import one.papachi.nat4j.portmapper.PortMapperProtocol;
import one.papachi.nat4j.portmapper.PortProtocol;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class UPnPPortMapper extends AbstractPortMapper {

	public static void main(String[] args) {
		List<PortMapper> find = find();
		find.forEach(System.out::println);
	}

	public static Optional<PortMapper> findIPv4() {
		UPnPPortMapper portMapper = null;
		try {
			Inet4Address defaultGatewayIPv4 = NatUtils.getDefaultGatewayIPv4();
			if (defaultGatewayIPv4 != null) {
				InetAddress localAddress = NatUtils.getLocalAddressToDefaultGatewayIPv4(), gatewayAddress = null, externalAddress = null;
				List<String> list = NatUtils.getResultFromTasks(List.of(
						() -> Utils.performMSearch(localAddress, Constants.IPV4_MULTICAST_ADDRESS, Constants.ST_WANIPConnection),
						() -> Utils.performMSearch(localAddress, Constants.IPV4_MULTICAST_ADDRESS, Constants.ST_WANPPPConnection)
						));
				String mSearchResponse = list.stream().filter(response -> {
					try {
						return InetAddress.getByName(new URL(Utils.getLocationFromResponse(response)).getHost()).equals(defaultGatewayIPv4);
					} catch (Exception e) {
					}
					return false;
				}).findAny().orElse(null);
				mSearchResponse = (mSearchResponse = Utils.performMSearch(localAddress, Constants.IPV4_MULTICAST_ADDRESS, Constants.ST_WANIPConnection)) != null ? mSearchResponse : Utils.performMSearch(localAddress, Constants.IPV4_MULTICAST_ADDRESS, Constants.ST_WANPPPConnection);
				URL url = new URL(Utils.getLocationFromResponse(mSearchResponse));
				String basePath = "http://" + url.getHost()  + ":" + url.getPort();
				byte[] request = ("GET " + url.getPath() + " HTTP/1.1\r\nHost: " + url.getHost()  + ":" + url.getPort() + " \r\n\r\n").getBytes(StandardCharsets.ISO_8859_1);
				byte[] response = NatUtils.performSocketRequest(new InetSocketAddress(0), new InetSocketAddress(gatewayAddress = InetAddress.getByName(url.getHost()), url.getPort()), request);
				String xml = Utils.getBodyFromResponse(response);
				int index, start, stop;
				if (xml != null && ((index = xml.indexOf(Constants.ST_WANIPConnection)) != -1 || (index = xml.indexOf(Constants.ST_WANPPPConnection)) != -1) && (start = xml.indexOf("<controlURL>", index)) != -1 && (stop = xml.indexOf("</controlURL>", start)) != -1) {
					String serviceType = xml.substring(index, xml.indexOf("</", index));
					String controlURL = basePath + xml.substring(start + "<controlURL>".length(), stop);
					AtomicReference<InetAddress> reference = new AtomicReference<>();
					getExternalIPAddress(controlURL, serviceType, reference::setPlain);
					externalAddress = reference.getPlain();
					portMapper = new UPnPPortMapper(localAddress, gatewayAddress, externalAddress, controlURL, serviceType);
				}
			}
		} catch (Exception e) {
		}
		return Optional.ofNullable(portMapper);
	}
	
	public static List<PortMapper> findIPv6() {
		List<PortMapper> result = new LinkedList<>();
		try {
			Inet6Address defaultGatewayIPv6 = NatUtils.getDefaultGatewayIPv6();
			if (defaultGatewayIPv6 != null) {
				List<Inet6Address> localAddresses = NatUtils.getLocalAddressesToDefaultGatewayIPv6();
				InetAddress gatewayAddress = null, externalAddress = null;
				List<String> list = NatUtils.getResultFromTasks(List.of(
						() -> Utils.performMSearch(Constants.IPV6_ALL, Constants.IPV6_MULTICAST_ADDRESS_LINK_LOCAL, Constants.ST_WANIPConnection),// TODO ST_WANIPv6FirewallControl
						() -> Utils.performMSearch(Constants.IPV6_ALL, Constants.IPV6_MULTICAST_ADDRESS_SITE_LOCAL, Constants.ST_WANIPv6FirewallControl),
						() -> Utils.performMSearch(Constants.IPV6_ALL, Constants.IPV6_MULTICAST_ADDRESS_ORGANIZATION_LOCAL, Constants.ST_WANIPv6FirewallControl),
						() -> Utils.performMSearch(Constants.IPV6_ALL, Constants.IPV6_MULTICAST_ADDRESS_GLOBAL, Constants.ST_WANIPv6FirewallControl)
						));
				String mSearchResponse = list.stream().filter(response -> {
					try {
						return InetAddress.getByName(new URL(Utils.getLocationFromResponse(response)).getHost()).equals(defaultGatewayIPv6);
					} catch (Exception e) {
					}
					return false;
				}).findAny().orElse(null);
				URL url = new URL(Utils.getLocationFromResponse(mSearchResponse));
				String basePath = "http://" + url.getHost()  + ":" + url.getPort();
				byte[] request = ("GET " + url.getPath() + " HTTP/1.1\r\nHost: " + url.getHost()  + ":" + url.getPort() + " \r\n\r\n").getBytes(StandardCharsets.ISO_8859_1);
				byte[] response = NatUtils.performSocketRequest(new InetSocketAddress(0), new InetSocketAddress(gatewayAddress = InetAddress.getByName(url.getHost()), url.getPort()), request);
				String xml = Utils.getBodyFromResponse(response);
				int index, start, stop;
				if (xml != null && ((index = xml.indexOf(Constants.ST_WANIPConnection)) != -1) && (start = xml.indexOf("<controlURL>", index)) != -1 && (stop = xml.indexOf("</controlURL>", start)) != -1) {
					String serviceType = xml.substring(index, xml.indexOf("</", index));
					String controlURL = basePath + xml.substring(start + "<controlURL>".length(), stop);
					AtomicReference<InetAddress> reference = new AtomicReference<>();
					getExternalIPAddress(controlURL, serviceType, reference::setPlain);
					externalAddress = reference.getPlain();
					for (Inet6Address localAddress : localAddresses) {
						result.add(new UPnPPortMapper(localAddress, gatewayAddress, externalAddress, controlURL, serviceType));
					}
				}
			}
		} catch (Exception e) {
		}
		return result;
	}
	
	public static List<PortMapper> find() {
		List<PortMapper> result = new LinkedList<>();
		findIPv4().ifPresent(result::add);
		findIPv6().forEach(result::add);
		return result;
	}
	
	private final String controlURL, serviceType;
	
	protected UPnPPortMapper(InetAddress localAddress, InetAddress gatewayAddress, InetAddress externalAddress, String controlURL, String serviceType) {
		super(PortMapperProtocol.UPNP, localAddress, gatewayAddress, externalAddress);
		this.controlURL = controlURL;
		this.serviceType = serviceType;
	}

	@Override
	public Optional<MappedPort> mapPort(PortProtocol portProtocol, int internalPort, int externalPort, int lifetime) {
		MappedPort mappedPort = null;
		ResultCode resultCode;
		if (gatewayAddress instanceof Inet6Address) {
			resultCode = addPinhole(controlURL, serviceType, "*", externalPort, localAddress.getHostAddress(), internalPort, portProtocol.getProtocolNumber(), lifetime, ignore -> {});
		} else {
			resultCode = addPortMapping(controlURL, serviceType, "*", externalPort, portProtocol.toString(), internalPort, localAddress.getHostAddress(), true, "", lifetime, ignore -> {});
		}
		if (resultCode == ResultCode.SUCCESS) {
			mappedPort = new UPnPMappedPort(this, portProtocol, internalPort, externalPort, lifetime);// TODO add specific data for unmapping
		}
		return Optional.ofNullable(mappedPort);
	}

	@Override
	public boolean refreshPort(MappedPort mappedPort) {
		if (gatewayAddress instanceof Inet6Address) {
			return updatePinhole(controlURL, serviceType, null, mappedPort.getLifetime()) == ResultCode.SUCCESS;// TODO null
		} else {
			return addPortMapping(controlURL, serviceType, "*", mappedPort.getExternalPort(), mappedPort.getPortProtocol().toString(), mappedPort.getInternalPort(), localAddress.getHostAddress(), true, "", mappedPort.getLifetime(), ignore -> {}) == ResultCode.SUCCESS;
		}
	}

	@Override
	public void unmapPort(MappedPort mappedPort) {
		if (gatewayAddress instanceof Inet6Address) {
			deletePinhole(controlURL, serviceType, null);// TODO null
		} else {
			deletePortMapping(controlURL, serviceType, "*", mappedPort.getExternalPort(), mappedPort.getPortProtocol().toString(), ignore -> {});
		}
	}
	
	public String getControlURL() {
		return controlURL;
	}
	
	public String getServiceType() {
		return serviceType;
	}

	public static ResultCode getExternalIPAddress(String controlURL, String serviceType, Consumer<InetAddress> consumer) {
		Map<String, String> parameters = Map.of();
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "GetExternalIPAddress", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		String address = result.get("NewExternalIPAddress");
		Optional.ofNullable(consumer).ifPresent(c -> c.accept(NatUtils.getInetAddress(address)));
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode getGenericPortMappingEntry(String controlURL, String serviceType, int portMappingIndex, Consumer<Map<String, String>> consumer) {
		Map<String, String> parameters = Map.of("NewPortMappingIndex", Integer.toString(portMappingIndex));
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "GetGenericPortMappingEntry", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		Optional.ofNullable(consumer).ifPresent(c -> c.accept(result));
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode getSpecificPortMappingEntry(String controlURL, String serviceType, String remoteHost, int externalPort, String protocol, Consumer<Map<String, String>> consumer) {
		Map<String, String> parameters = Map.of("NewRemoteHost", remoteHost, "NewExternalPort", Integer.toString(externalPort), "NewProtocol", protocol);
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "GetSpecificPortMappingEntry", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		Optional.ofNullable(consumer).ifPresent(c -> c.accept(result));
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode addPortMapping(String controlURL, String serviceType, String remoteHost, int externalPort, String protocol, int internalPort, String internalClient, boolean enabled, String portMappingDescription, int leaseDuration, Consumer<Map<String, String>> consumer) {
		Map<String, String> parameters = Map.of("NewRemoteHost", remoteHost, "NewExternalPort", Integer.toString(externalPort), "NewProtocol", protocol, "NewInternalPort", Integer.toString(internalPort), "NewInternalClient", internalClient, "NewEnabled", Integer.toString(enabled ? 1 : 0), "NewPortMappingDescription", portMappingDescription, "NewLeaseDuration", Integer.toString(leaseDuration));
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "AddPortMapping", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		Optional.ofNullable(consumer).ifPresent(c -> c.accept(result));
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode deletePortMapping(String controlURL, String serviceType, String remoteHost, int externalPort, String protocol, Consumer<Map<String, String>> consumer) {
		Map<String, String> parameters = Map.of("NewRemoteHost", remoteHost, "NewExternalPort", Integer.toString(externalPort), "NewProtocol", protocol);
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "DeletePortMapping", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		Optional.ofNullable(consumer).ifPresent(c -> c.accept(result));
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode addPinhole(String controlURL, String serviceType, String remoteHost, int remotePort, String internalClient, int internalPort, int protocol, int leaseTime, Consumer<String> consumer) {// 6 17
		Map<String, String> parameters = Map.of("RemoteHost", remoteHost, "RemotePort", Integer.toString(remotePort), "InternalClient", internalClient, "InternalPort", Integer.toString(internalPort), "Protocol", Integer.toString(protocol), "LeaseTime", Integer.toString(leaseTime));
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "AddPinhole", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		Optional.ofNullable(consumer).ifPresent(c -> c.accept(result.get("UniqueID")));
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode updatePinhole(String controlURL, String serviceType, String uniqueId, int newLeaseTime) {
		Map<String, String> parameters = Map.of("UniqueID", uniqueId, "NewLeaseTime", Integer.toString(newLeaseTime));
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "UpdatePinhole", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}
	
	public static ResultCode deletePinhole(String controlURL, String serviceType, String uniqueId) {
		Map<String, String> parameters = Map.of("UniqueID", uniqueId);
		Map<String, String> result = new HashMap<>();
		int statusCode = Utils.performHTTPRequest(controlURL, serviceType, "DeletePinhole", parameters, result);
		String errorCode = result.getOrDefault("errorCode", null);
		return ResultCode.getResultCode(errorCode != null ? Integer.valueOf(errorCode) : statusCode);
	}

	@Override
	public String toString() {
		return "UPnPPortMapper(localAddress = " + localAddress + ", gatewayAddress = " + gatewayAddress + ", externalAddress = " + externalAddress + ")";
	}

}
