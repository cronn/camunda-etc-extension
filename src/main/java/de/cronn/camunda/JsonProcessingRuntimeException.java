package de.cronn.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonProcessingRuntimeException extends RuntimeException {

	public JsonProcessingRuntimeException(JsonProcessingException cause) {
		super(cause);
	}
}
