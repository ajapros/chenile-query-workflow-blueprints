package org.chenile.query.model;

public class SearchPaginationInfo {
	private Boolean nextPageAvailable;
	private Boolean countQueryExecuted;
	private Boolean totalCountAvailable;

	public Boolean getNextPageAvailable() {
		return nextPageAvailable;
	}

	public void setNextPageAvailable(Boolean nextPageAvailable) {
		this.nextPageAvailable = nextPageAvailable;
	}

	public Boolean getCountQueryExecuted() {
		return countQueryExecuted;
	}

	public void setCountQueryExecuted(Boolean countQueryExecuted) {
		this.countQueryExecuted = countQueryExecuted;
	}

	public Boolean getTotalCountAvailable() {
		return totalCountAvailable;
	}

	public void setTotalCountAvailable(Boolean totalCountAvailable) {
		this.totalCountAvailable = totalCountAvailable;
	}
}
