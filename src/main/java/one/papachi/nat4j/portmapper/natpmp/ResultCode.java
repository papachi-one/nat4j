package one.papachi.nat4j.portmapper.natpmp;

public enum ResultCode {

	SUCCESS(0), UNSUPPORTED_VERSION(1), NOT_AUTHORIZED(2), NETWORK_FAILURE(3), NO_RESOURCES(4), UNSUPPORTED_OPCODE(5), NO_RESPONSE(-1);
	
	private int resultCode;

	private ResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	
	public int getResultCode() {
		return resultCode;
	}
	
}
