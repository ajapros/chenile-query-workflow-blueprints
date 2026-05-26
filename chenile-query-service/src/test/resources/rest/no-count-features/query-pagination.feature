Feature: Tests query pagination with count query disabled.

Background:
When I construct a REST request with header "x-chenile-auth-user" and value "manager1"

Scenario: Disabled count query returns next page metadata
When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
When I POST a REST request to URL "/q/students" with payload
"""
{
	"sortCriteria" :[
		{"name":"name","ascendingOrder": true}
	],
	"pageNum": 1,
	"numRowsInPage": 29
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "29"
And the REST response key "currentPage" is "1"
And the REST response key "maxRows" is "0"
And the REST response key "maxPages" is "0"
And the REST response key "pagination.countQueryExecuted" is "false"
And the REST response key "pagination.totalCountAvailable" is "false"
And the REST response key "pagination.nextPageAvailable" is "true"
