package one.papachi.nat4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NatUtils {

	public static final boolean IS_OS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
	
	public static final boolean IS_OS_LINUX = System.getProperty("os.name").startsWith("Linux") || System.getProperty("os.name").startsWith("LINUX");
	
	public static final boolean IS_OS_MAC = System.getProperty("os.name").startsWith("Mac");

    public static byte[] runProcess(String... cmd) {
		byte[] byteArray = new byte[0];
		try {
			Process process = new ProcessBuilder(cmd).start();
			InputStream inputStream = new BufferedInputStream(process.getInputStream());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int i;
			while ((i = inputStream.read()) != -1) {
				outputStream.write(i);
			}
			process.waitFor();
			byteArray = outputStream.toByteArray();
		} catch (Exception e) {
		}
		return byteArray;
	}
	
	public static String processLines(String input, Function<String[], String> function) {
		try {
			BufferedReader reader = new BufferedReader(new StringReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] lineColumns = line.trim().split("\\s+");
				String result = function.apply(lineColumns);
				if (result != null && !result.isEmpty()) {
					return result;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static Inet4Address getDefaultGatewayIPv4() {
		if (IS_OS_WINDOWS) {
			return getDefaultGatewayIPv4Windows();
		} else if (IS_OS_LINUX) {
			return getDefaultGatewayIPv4Linux();
		} else if (IS_OS_MAC) {
			return getDefaultGatewayIPv4MacOS();
		}
		return null;
	}
	
	public static Inet4Address getDefaultGatewayIPv4Windows() {
		try {
			String output = new String(runProcess("netstat", "-rn"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 3 && "0.0.0.0".equals(lineColumns[0]) && "0.0.0.0".equals(lineColumns[1]) ? lineColumns[2] : null);
			return (Inet4Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet4Address getDefaultGatewayIPv4Linux() {
		try {
			String output = new String(runProcess("ip", "-f", "inet", "route"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 3 && "default".equals(lineColumns[0]) && "via".equals(lineColumns[1]) ? lineColumns[2] : null);
			return (Inet4Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet4Address getDefaultGatewayIPv4MacOS() {
		try {
			String output = new String(runProcess("netstat", "-f", "inet", "-rn"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 2 && "default".equals(lineColumns[0]) ? lineColumns[1] : null);
			return (Inet4Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet4Address getLocalAddressToDefaultGatewayIPv4() {
		if (IS_OS_WINDOWS) {
			return getLocalAddressToDefaultGatewayIPv4Windows();
		} else if (IS_OS_LINUX) {
			return getLocalAddressToDefaultGatewayIPv4Linux();
		} else if (IS_OS_MAC) {
			return getLocalAddressToDefaultGatewayIPv4MacOS();
		}
		return null;
	}
	
	public static Inet4Address getLocalAddressToDefaultGatewayIPv4Windows() {
		try {
			String output = new String(runProcess("netstat", "-rn"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 3 && "0.0.0.0".equals(lineColumns[0]) && "0.0.0.0".equals(lineColumns[1]) ? lineColumns[3] : null);
			return (Inet4Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet4Address getLocalAddressToDefaultGatewayIPv4Linux() {
		try {
			String output = new String(runProcess("ip", "-f", "inet", "route"), StandardCharsets.ISO_8859_1);
			String iface = processLines(output, lineColumns -> lineColumns.length >= 3 && "default".equals(lineColumns[0]) && "via".equals(lineColumns[1]) && "dev".equals(lineColumns[3]) ? lineColumns[4] : null);
			NetworkInterface networkInterface = NetworkInterface.getByName(iface);
			return (Inet4Address) networkInterface.inetAddresses().filter(a -> a instanceof Inet4Address).findFirst().orElse(null);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet4Address getLocalAddressToDefaultGatewayIPv4MacOS() {
		try {
			String output = new String(runProcess("netstat", "-f", "inet", "-rn"), StandardCharsets.ISO_8859_1);
			String iface = processLines(output, lineColumns -> lineColumns.length >= 2 && "default".equals(lineColumns[0]) ? lineColumns[3] : null);
			NetworkInterface networkInterface = NetworkInterface.getByName(iface);
			return (Inet4Address) networkInterface.inetAddresses().filter(a -> a instanceof Inet4Address).findFirst().orElse(null);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet6Address getDefaultGatewayIPv6() {
		if (IS_OS_WINDOWS) {
			return getDefaultGatewayIPv6Windows();
		} else if (IS_OS_LINUX) {
			return getDefaultGatewayIPv6Linux();
		} else if (IS_OS_MAC) {
			return getDefaultGatewayIPv6MacOS();
		}
		return null;
	}

	public static Inet6Address getDefaultGatewayIPv6Windows() {
		try {
			String output = new String(runProcess("netstat", "-rn"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 4 && "::/0".equals(lineColumns[2]) ? lineColumns[3] : null);
			return (Inet6Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet6Address getDefaultGatewayIPv6Linux() {
		try {
			String output = new String(runProcess("ip", "-f", "inet6", "route"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 3 && "default".equals(lineColumns[0]) && "via".equals(lineColumns[1]) ? lineColumns[2] : null);
			return (Inet6Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static Inet6Address getDefaultGatewayIPv6MacOS() {
		try {
			String output = new String(runProcess("netstat", "-f", "inet6", "-rn"), StandardCharsets.ISO_8859_1);
			String host = processLines(output, lineColumns -> lineColumns.length >= 2 && "default".equals(lineColumns[0]) ? lineColumns[1] : null);
			return (Inet6Address) InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static List<Inet6Address> getLocalAddressesToDefaultGatewayIPv6() {
		if (IS_OS_WINDOWS) {
			return getLocalAddressesToDefaultGatewayIPv6Windows();
		} else if (IS_OS_LINUX) {
			return getLocalAddressesToDefaultGatewayIPv6Linux();
		} else if (IS_OS_MAC) {
			return getLocalAddressesToDefaultGatewayIPv6MacOS();
		}
		return Collections.emptyList();
	}
	
	public static List<Inet6Address> getLocalAddressesToDefaultGatewayIPv6Windows() {
    	try {
			String output = new String(runProcess("netstat", "-rn"), StandardCharsets.ISO_8859_1);
			String interfaceNumber = processLines(output, lineColumns -> lineColumns.length >= 4 && "::/0".equals(lineColumns[2]) ? lineColumns[0] : null);
			NetworkInterface networkInterface = NetworkInterface.getByIndex(Integer.parseInt(interfaceNumber));
			return networkInterface.inetAddresses().filter(a -> a instanceof Inet6Address).map(a -> (Inet6Address) a).filter(a -> !a.isLoopbackAddress() && !a.isSiteLocalAddress() && !a.isMulticastAddress()).map(a -> (Inet6Address) getInetAddress(a.getAddress())).collect(Collectors.toList());
		} catch (Exception e) {
		}
		return Collections.emptyList();
	}
	
	public static List<Inet6Address> getLocalAddressesToDefaultGatewayIPv6Linux() {
		try {
			String output = new String(runProcess("ip", "-f", "inet6", "route"), StandardCharsets.ISO_8859_1);
			String iface = processLines(output, lineColumns -> lineColumns.length >= 3 && "default".equals(lineColumns[0]) && "via".equals(lineColumns[1]) && "dev".equals(lineColumns[3]) ? lineColumns[4] : null);
			NetworkInterface networkInterface = NetworkInterface.getByName(iface);
			return networkInterface.inetAddresses().filter(a -> a instanceof Inet6Address).map(a -> (Inet6Address) a).filter(a -> !a.isLoopbackAddress() && !a.isLinkLocalAddress() && !a.isSiteLocalAddress() && !a.isMulticastAddress()).collect(Collectors.toList());
		} catch (Exception e) {
		}
		return Collections.emptyList();
	}

	public static List<Inet6Address> getLocalAddressesToDefaultGatewayIPv6MacOS() {
		try {
			String output = new String(runProcess("netstat", "-f", "inet6", "-rn"), StandardCharsets.ISO_8859_1);
			String iface = processLines(output, lineColumns -> lineColumns.length >= 2 && "default".equals(lineColumns[0]) ? lineColumns[3] : null);
			NetworkInterface networkInterface = NetworkInterface.getByName(iface);
			return networkInterface.inetAddresses().filter(a -> a instanceof Inet6Address).map(a -> (Inet6Address) a).filter(a -> !a.isLoopbackAddress() && !a.isLinkLocalAddress() && !a.isSiteLocalAddress() && !a.isMulticastAddress()).collect(Collectors.toList());
		} catch (Exception e) {
		}
		return Collections.emptyList();
	}
	
	public static byte[] performDatagramRequest(InetSocketAddress localSocketAddress, InetSocketAddress remoteSocketAddress, byte[] request) {
		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.configureBlocking(false);
			channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			channel.bind(localSocketAddress != null ? localSocketAddress : new InetSocketAddress(0));
			channel.send(ByteBuffer.wrap(request), remoteSocketAddress);
			ByteBuffer buffer = ByteBuffer.allocate(65536);
			int slept = 0;
			while (channel.receive(buffer) == null && slept < 3000) {
				Thread.sleep(250);
				slept += 250;
				channel.send(ByteBuffer.wrap(request), remoteSocketAddress);
			}
			byte[] result = new byte[buffer.flip().remaining()];
			buffer.get(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] performSocketRequest(InetSocketAddress localSocketAddress, InetSocketAddress remoteSocketAddress, byte[] request) {
		try (SocketChannel channel = SocketChannel.open()) {
			channel.bind(localSocketAddress != null ? localSocketAddress : new InetSocketAddress(0));
			channel.connect(remoteSocketAddress);
			ByteBuffer writeBuffer = ByteBuffer.wrap(request);
			while (writeBuffer.hasRemaining()) {
				channel.write(writeBuffer);
			}
			channel.shutdownOutput();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ByteBuffer readBuffer = ByteBuffer.allocate(8 * 1024);
			while ((channel.read(readBuffer)) != -1) {
				outputStream.write(readBuffer.flip().array(), readBuffer.arrayOffset() + readBuffer.position(), readBuffer.remaining());
			}
			return outputStream.toByteArray();
		} catch (Exception e) {
		}
		return null;
	}
	
	public static <R> List<R> getResultFromTasks(Collection<Callable<R>> tasks) {
		List<R> result = new ArrayList<>(tasks.size());
		for (int i = 0; i < tasks.size(); i++) {
			result.add(null);
		}
		List<Thread> threads = new ArrayList<>(tasks.size());
		int index = 0;
		for (Callable<R> task : tasks) {
			int i = index++;
			threads.add(new Thread(() -> {
				try {
					result.set(i, task.call());
				} catch (Exception e) {
				}
			}));
		}
		threads.forEach(Thread::start);
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

	public static InetAddress getInetAddress(String host) {
		try {
			return InetAddress.getByName(host);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static InetAddress getInetAddress(byte[] host) {
		try {
			return InetAddress.getByAddress(host);
		} catch (Exception e) {
		}
		return null;
	}
	
}
