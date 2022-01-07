package de.cronn.camunda;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.spin.plugin.variable.SpinValues;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonVariablesTest extends ExternalTaskHandlerIntegrationTest {

	static class JsonVariableExternalTaskHandler extends ExternalTaskHandler {

		volatile SampleObject jsonValue;

		public JsonVariableExternalTaskHandler(ObjectMapper objectMapper) {
			super(objectMapper);
		}

		@HandlerMethod
		void handle(ExternalTask externalTask, ExternalTaskService externalTaskService,
					@JsonVariable("jsonVariable") SampleObject jsonValue) {
			this.jsonValue = jsonValue;
			externalTaskService.complete(externalTask);
		}
	}

	static class SampleObject {
		private String s;
		private Integer i;

		public SampleObject(String s, Integer i) {
			this.s = s;
			this.i = i;
		}

		public SampleObject() {
		}

		public String getS() {
			return s;
		}

		public void setS(String s) {
			this.s = s;
		}

		public Integer getI() {
			return i;
		}

		public void setI(Integer i) {
			this.i = i;
		}
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final SampleObject sampleObject = new SampleObject("value", 1234);

	@Test
	void shouldHandleJsonVariable() throws JsonProcessingException {
		String json = objectMapper.writeValueAsString(sampleObject);

		VariableMap variables = Variables
			.putValueTyped("jsonVariable", SpinValues.jsonValue(json).create());

		ProcessInstance process = startProcess(variables);

		JsonVariableExternalTaskHandler handler = new JsonVariableExternalTaskHandler(objectMapper);
		subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

		Assertions.assertThat(handler.jsonValue).usingRecursiveComparison().isEqualTo(sampleObject);
	}

	@Test
	void shouldHandleJsonVariableNull() {
		VariableMap variables = Variables
			.putValueTyped("jsonVariable", SpinValues.jsonValue((String) null).create());

		ProcessInstance process = startProcess(variables);

		JsonVariableExternalTaskHandler handler = new JsonVariableExternalTaskHandler(objectMapper);
		subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

		Assertions.assertThat(handler.jsonValue).isNull();
	}

	@Test
	void shouldHandleJsonVariableContainingNull() {
		VariableMap variables = Variables
			.putValueTyped("jsonVariable", SpinValues.jsonValue("null").create());

		ProcessInstance process = startProcess(variables);

		JsonVariableExternalTaskHandler handler = new JsonVariableExternalTaskHandler(objectMapper);
		subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

		Assertions.assertThat(handler.jsonValue).isNull();
	}

	@Test
	void shouldHandleStringVariableWithJsonContent() throws JsonProcessingException {
		String json = objectMapper.writeValueAsString(sampleObject);

		VariableMap variables = Variables
			.putValueTyped("jsonVariable", Variables.stringValue(json));

		ProcessInstance process = startProcess(variables);

		JsonVariableExternalTaskHandler handler = new JsonVariableExternalTaskHandler(objectMapper);
		subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

		Assertions.assertThat(handler.jsonValue).usingRecursiveComparison().isEqualTo(sampleObject);
	}

	@Test
	void shouldThrowWhenNoObjectMapperSuppliedAndJsonVariableRequested() {
		Assertions.assertThatThrownBy(() -> new JsonVariableExternalTaskHandler(null))
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("No ObjectMapper supplied so JsonVariable is not supported");
	}
}

