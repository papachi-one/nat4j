package one.papachi.nat4j.portmapper;

public enum PortProtocol {

	TCP((byte) 2, 6), UDP((byte) 1, 17);
	
	private byte opCode;
	
	private int protocolNumber;

	private PortProtocol(byte opCode, int protocolNumber) {
		this.opCode = opCode;
		this.protocolNumber = protocolNumber;
	}
	
	public byte getOpCode() {
		return opCode;
	}
	
	public int getProtocolNumber() {
		return protocolNumber;
	}
	
}
