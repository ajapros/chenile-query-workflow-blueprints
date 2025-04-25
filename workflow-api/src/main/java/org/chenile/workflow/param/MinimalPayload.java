/**
 * 
 */
package org.chenile.workflow.param;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude
public class MinimalPayload implements Serializable {
	
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = -2314712304952305692L;
	private String comment;
	private Map<String,Object> attributes = new HashMap<>();

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key,value);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
}
