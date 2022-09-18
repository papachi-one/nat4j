# nat4j
Java port mapping library. Supports uPNP, NAT-PMP and PCP protocols and IPv4 and IPv6 protocols.
## Supported platforms
- Windows
- Linux
- macOS
## Sample code
```java
import one.papachi.nat4j.NatUtils;
import one.papachi.nat4j.portmapper.PortProtocol;
import one.papachi.nat4j.portmapper.natpmp.NatPmpPortMapper;
import one.papachi.nat4j.portmapper.pcp.PCPPortMapper;
import one.papachi.nat4j.portmapper.upnp.UPnPPortMapper;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.Optional;

class App {

    public static void main(String[] args) {
        Inet4Address defaultGatewayIPv4 = NatUtils.getDefaultGatewayIPv4();
        Inet4Address localAddressToDefaultGatewayIPv4 = NatUtils.getLocalAddressToDefaultGatewayIPv4();
        System.out.println("defaultGatewayIPv4 = " + defaultGatewayIPv4);
        System.out.println("localAddressToDefaultGatewayIPv4 = " + localAddressToDefaultGatewayIPv4);

        Inet6Address defaultGatewayIPv6 = NatUtils.getDefaultGatewayIPv6();
        List<Inet6Address> localAddressesToDefaultGatewayIPv6 = NatUtils.getLocalAddressesToDefaultGatewayIPv6();
        System.out.println("defaultGatewayIPv6 = " + defaultGatewayIPv6);
        System.out.println("localAddressesToDefaultGatewayIPv6 = " + localAddressesToDefaultGatewayIPv6);

        List<PortMapper> natPmpPortMappers = NatPmpPortMapper.find();
        List<PortMapper> uPnpPortMappers = UPnPPortMapper.find();
        List<PortMapper> pcpPortMappers = PCPPortMapper.find();

        List<PortMapper> ipv4PortMappers = List.of(natPmpPortMappers, uPnpPortMappers, pcpPortMappers).stream().flatMap(Collection::stream).filter(pm -> pm.getGatewayAddress().getAddress().length == 4).toList();
        List<PortMapper> ipv6PortMappers = List.of(natPmpPortMappers, uPnpPortMappers, pcpPortMappers).stream().flatMap(Collection::stream).filter(pm -> pm.getGatewayAddress().getAddress().length == 16).toList();

        PortProtocol protocol = PortProtocol.TCP;
        int internalPort = 8080;
        int externalPort = 80;
        int lifetime = 300;

        MappedPort mappedPortIPv4 = ipv4PortMappers.stream().map(portMapper -> portMapper.mapPort(protocol, internalPort, externalPort, lifetime)).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);
        MappedPort mappedPortIPv6 = ipv6PortMappers.stream().map(portMapper -> portMapper.mapPort(protocol, internalPort, externalPort, lifetime)).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

        System.out.println("mappedPortIPv4 = " + mappedPortIPv4);
        System.out.println("mappedPortIPv6 = " + mappedPortIPv6);
    }

}
```