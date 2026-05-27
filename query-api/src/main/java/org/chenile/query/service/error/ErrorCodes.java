package org.chenile.query.service.error;

public enum ErrorCodes {
	COUNT_QUERY_DOES_NOT_RETURN_INT("Q720"),
	QUERY_ID_NOT_FOUND("Q700"),
	CANNOT_EXECUTE_QUERY("Q721"),
	CANNOT_EXECUTE_COUNT_QUERY("Q722");
	private final String subError;
	private ErrorCodes(String subError) {
		this.subError = subError;
	}

	public String getSubError() {
		return this.subError;
	}
}
