package org.chenile.query.service.error;

public enum ErrorCodes {
	QUERY_ID_NOT_FOUND(700);
	private final int subError;
	private ErrorCodes(int subError) {
		this.subError = subError;
	}

	public int getSubError() {
		return this.subError;
	}
}
