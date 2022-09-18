package one.papachi.nat4j.portmapper;

public interface MappedPort {

	PortMapper getPortMapper();
	
	PortProtocol getPortProtocol();
	
	int getInternalPort();
	
	int getExternalPort();
	
	int getLifetime();
	
	boolean refreshMapping();
	
	void cancelMapping();
	
}
