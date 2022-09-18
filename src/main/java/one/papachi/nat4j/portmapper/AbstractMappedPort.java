package one.papachi.nat4j.portmapper;

public abstract class AbstractMappedPort implements MappedPort {
	
	private final PortMapper portMapper;
	
	private final PortProtocol portProtocol;
	
	private final int internalPort;
	
	private final int externalPort;
	
	private final int lifetime;
	
	public AbstractMappedPort(PortMapper portMapper, PortProtocol portProtocol, int internalPort, int externalPort, int lifetime) {
		this.portMapper = portMapper;
		this.portProtocol = portProtocol;
		this.internalPort = internalPort;
		this.externalPort = externalPort;
		this.lifetime = lifetime;
	}
	
	@Override
	public PortMapper getPortMapper() {
		return portMapper;
	}
	
	@Override
	public PortProtocol getPortProtocol() {
		return portProtocol;
	}
	
	@Override
	public int getInternalPort() {
		return internalPort;
	}
	
	@Override
	public int getExternalPort() {
		return externalPort;
	}
	
	@Override
	public int getLifetime() {
		return lifetime;
	}
	
	@Override
	public boolean refreshMapping() {
		return getPortMapper().refreshPort(this);
	}
	
	@Override
	public void cancelMapping() {
		getPortMapper().unmapPort(this);
	}

	@Override
	public String toString() {
		return getClass().getName() + "(portMapper = " + portMapper + ", portProtocol = " + portProtocol + ", internalPort = " + internalPort + ", externalPort = " + externalPort + ", lifetime = " + lifetime + ")";
	}
	
}
