 
Feature: Tests the Student Query Service using a REST client. 

Scenario Outline: Tests out pagination capability
When I construct a REST request with header "x-chenile-tenant-id" and value "<tenantId>"
When I POST a REST request to URL "/q/students" with payload
"""
{
	"sortCriteria" :[
		{"name":"name","ascendingOrder": true}
	],
	"pageNum": 2,
	"numRowsInPage": 15
}
"""
Then the http status code is 200
And the top level code is 200
And success is true 
And the REST response key "numRowsReturned" is "15"
And the REST response key "currentPage" is "2"
And the REST response key "maxPages" is "2"
And the REST response key "list[0].row.name" is "Narendra"
And the REST response key "list[0].row.id" is "<pageFirstId>"
And the REST response key "list[14].row.name" is "Vikas"
And the REST response key "list[14].row.id" is "<pageLastId>"

Examples:
| tenantId | pageFirstId | pageLastId |
| tenant1 | 25 | 18 |
| tenant2 | 125 | 118 |

Scenario Outline: Test Likes query
When I construct a REST request with header "x-chenile-tenant-id" and value "<tenantId>"
When I POST a REST request to URL "/q/students" with payload
"""
{
	"filters" :{
		"name": "ja"
	}
}
"""
Then the http status code is 200
And the top level code is 200
And success is true 
And the REST response key "numRowsReturned" is "1"
And the REST response key "list[0].row.name" is "Vijay"
And the REST response key "list[0].row.id" is "<likeId>"

Examples:
| tenantId | likeId |
| tenant1 | 29 |
| tenant2 | 129 |

Scenario Outline: Test Specific - Test Contains with an array and sort descending
When I construct a REST request with header "x-chenile-tenant-id" and value "<tenantId>"
When I POST a REST request to URL "/q/students" with payload
"""
{
	"filters" :{
		"branch": [ "Gurgaon", "Jaipur", "Trivandrum"]
	},
	"sortCriteria" :[
		{"name":"branch","ascendingOrder": false}
	]
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "3"
And the REST response key "list[2].row.name" is "Kamala"
And the REST response key "list[0].row.id" is "<containsId1>"
And the REST response key "list[1].row.id" is "<containsId2>"
And the REST response key "list[2].row.id" is "<containsId3>"

Examples:
| tenantId | containsId1 | containsId2 | containsId3 |
| tenant1 | 16 | 21 | 8 |
| tenant2 | 116 | 121 | 108 |

Scenario Outline: Test Specific - Test Two params
When I construct a REST request with header "x-chenile-tenant-id" and value "<tenantId>"
When I POST a REST request to URL "/q/students" with payload
"""
{
	"filters" :{
		"branch": [ "Bangalore"],
		"name": "ka"
	},
	"sortCriteria" :[
		{"name":"name","ascendingOrder": true}
	]
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "2"
And the REST response key "list[0].row.id" is "<twoParamsId1>"
And the REST response key "list[1].row.id" is "<twoParamsId2>"
And the REST response key "list[0].row.name" is "Akash"
And the REST response key "list[1].row.name" is "Vikas"

Examples:
| tenantId | twoParamsId1 | twoParamsId2 |
| tenant1 | 5 | 18 |
| tenant2 | 105 | 118 |

Scenario Outline: Test Specific - Test Two params but with one of them just as a string
When I construct a REST request with header "x-chenile-tenant-id" and value "<tenantId>"
When I POST a REST request to URL "/q/students" with payload
"""
{
	"filters" :{
		"branch": "Bangalore",
		"name": "ka"
	},
	"sortCriteria" :[
		{"name":"name","ascendingOrder": true}
	]
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "2"
And the REST response key "list[0].row.id" is "<twoParamsStringId1>"
And the REST response key "list[1].row.id" is "<twoParamsStringId2>"
And the REST response key "list[0].row.name" is "Akash"
And the REST response key "list[1].row.name" is "Vikas"

Examples:
| tenantId | twoParamsStringId1 | twoParamsStringId2 |
| tenant1 | 5 | 18 |
| tenant2 | 105 | 118 |

Scenario: Default tenant when header missing
When I POST a REST request to URL "/q/students" with payload
"""
{
	"filters" :{
		"name": "ja"
	}
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "1"
And the REST response key "list[0].row.name" is "Vijay"
And the REST response key "list[0].row.id" is "29"

Scenario: Default tenant when header is empty
When I construct a REST request with header "x-chenile-tenant-id" and value ""
When I POST a REST request to URL "/q/students" with payload
"""
{
	"filters" :{
		"name": "ja"
	}
}
"""
Then the http status code is 200
And the top level code is 200
And success is true
And the REST response key "numRowsReturned" is "1"
And the REST response key "list[0].row.name" is "Vijay"
And the REST response key "list[0].row.id" is "29"
