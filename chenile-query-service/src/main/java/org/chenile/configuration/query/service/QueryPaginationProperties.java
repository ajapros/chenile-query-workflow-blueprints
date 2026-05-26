package org.chenile.configuration.query.service;

public class QueryPaginationProperties {
	private boolean countQueryEnabled = true;

	public boolean isCountQueryEnabled() {
		return countQueryEnabled;
	}

	public void setCountQueryEnabled(boolean countQueryEnabled) {
		this.countQueryEnabled = countQueryEnabled;
	}
}
