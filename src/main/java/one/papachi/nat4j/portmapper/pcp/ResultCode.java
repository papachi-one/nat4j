package one.papachi.nat4j.portmapper.pcp;

public enum ResultCode {
	
	SUCCESS(0),
	UNSUPP_VERSION(1),
	NOT_AUTHORIZED(2),
	MALFORMED_REQUEST(3),
	UNSUPP_OPCODE(4),
	UNSUPP_OPTION(5),
	MALFORMED_OPTION(6),
	NETWORK_FAILURE(7),
	NO_RESOURCES(8),
	UNSUPP_PROTOCOL(9),
	USER_EX_QUOTA(10),
	CANNOT_PROVIDE_EXTERNAL(11),
	ADDRESS_MISMATCH(12),
	EXCESSIVE_REMOTE_PEERS(13),
	NO_RESPONSE(-1);
	
	
	private int resultCode;

	private ResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	public int getResultCode() {
		return resultCode;
	}
	
}
