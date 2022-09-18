package one.papachi.nat4j.portmapper.upnp;

import one.papachi.nat4j.NatUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class Utils {

	static String performMSearch(InetAddress localAddress, InetAddress remoteAddress, String serviceType) {
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(remoteAddress, Constants.SSDP_PORT);
			String msearch = Constants.M_SEARCH.replace("<IP>", socketAddress.getAddress().getHostAddress()).replace("<PORT>", Integer.toString(socketAddress.getPort())).replace("<ST>", serviceType);
			byte[] request = msearch.getBytes(StandardCharsets.ISO_8859_1); 
			byte[] response = NatUtils.performDatagramRequest(new InetSocketAddress(localAddress, 0), socketAddress, request);
			return new String(response, StandardCharsets.ISO_8859_1);
		} catch (Exception e) {
		}
		return null;
	}

	static String getLocationFromResponse(String response) {
		try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("LOCATION: ")) {
					return line.substring("LOCATION: ".length());
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	static String getBodyFromResponse(byte[] response) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(response)));
			String line;
			while ((line = reader.readLine()) != null && !line.isEmpty());
			StringWriter writer = new StringWriter();
			reader.transferTo(writer);
			return writer.toString();
		} catch (Exception e) {
		}
		return null;
	}

	static String createSoapMessage(String action, String service, Map<String, String> parameters) {
		StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\r\n" +
                "<SOAP-ENV:Envelope " +
                "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<SOAP-ENV:Body>" +
                "<m:" + action + " xmlns:m=\"" + service + "\">");
        parameters.entrySet().forEach(e -> sb.append("<" + e.getKey() + ">" + e.getValue() + "</" + e.getKey() + ">"));
        sb.append("</m:" + action + ">");
        sb.append("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
        return sb.toString();
	}

	static int performHTTPRequest(String urlString, String service, String action, Map<String, String> inputParameters, Map<String, String> outputParameters) {
		int statusCode = 0;
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(new URL(urlString).toURI()).POST(BodyPublishers.ofString(Utils.createSoapMessage(action, service, inputParameters), StandardCharsets.UTF_8)).headers("Content-Type", "text/xml", "SOAPAction", "\"" + service + "#" + action + "\"").build();
			HttpResponse<String> response = HttpClient.newBuilder().followRedirects(Redirect.NEVER).build().send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
			statusCode = response.statusCode();
			String xml = response.body();
        	SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new StringReader(xml)), new DefaultHandler() {
        		StringBuilder sb;
        		@Override
        		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        			sb = new StringBuilder();
        		}
        		@Override
        		public void endElement(String uri, String localName, String qName) throws SAXException {
        			if (sb != null && sb.length() > 0) {
        				outputParameters.put(qName, sb.toString());
	        			sb = null;
        			}
        		}
        		@Override
        		public void characters(char[] ch, int start, int length) throws SAXException {
        			if (sb != null) {
        				sb.append(ch, start, length);
        			}
        		}
        	});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusCode;
	}
	
}
