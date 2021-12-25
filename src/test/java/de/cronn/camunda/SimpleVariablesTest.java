package de.cronn.camunda;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SimpleVariablesTest extends ExternalTaskHandlerIntegrationTest {

	@Nested
	class Objects {
		class SimpleVariablesExternalTaskHandler extends ExternalTaskHandler {

			volatile String stringValue;
			volatile Date dateValue;
			volatile Boolean booleanValue;
			volatile Double doubleValue;
			volatile Integer integerValue;
			volatile Long longValue;
			volatile Short shortValue;
			volatile byte[] byteArrayValue;
			volatile String nullValue;

			public SimpleVariablesExternalTaskHandler() {
				super(null);
			}

			@HandlerMethod
			void handle(ExternalTask externalTask, ExternalTaskService externalTaskService,
						@SimpleVariable("stringValue") String stringValue,
						@SimpleVariable("dateValue") Date dateValue,
						@SimpleVariable("booleanValue") Boolean booleanValue,
						@SimpleVariable("doubleValue") Double doubleValue,
						@SimpleVariable("integerValue") Integer integerValue,
						@SimpleVariable("longValue") Long longValue,
						@SimpleVariable("shortValue") Short shortValue,
						@SimpleVariable("byteArrayValue") byte[] byteArrayValue,
						@SimpleVariable("nullValue") String nullValue) {
				this.stringValue = stringValue;
				this.dateValue = dateValue;
				this.booleanValue = booleanValue;
				this.doubleValue = doubleValue;
				this.integerValue = integerValue;
				this.longValue = longValue;
				this.shortValue = shortValue;
				this.byteArrayValue = byteArrayValue;
				this.nullValue = nullValue;
				externalTaskService.complete(externalTask);
			}
		}

		@Test
		void shouldHandleSimpleVariables() {
			String stringValue = "value";
			Date dateValue = new Date();
			boolean booleanValue = true;
			double doubleValue = 2.34;
			int integerValue = 12345;
			long longValue = 123456789L;
			short shortValue = (short) 123;
			byte[] byteArrayValue = { 1, 2, 3, 4 };

			VariableMap variables = Variables
				.putValueTyped("stringValue", Variables.stringValue(stringValue))
				.putValueTyped("dateValue", Variables.dateValue(dateValue))
				.putValueTyped("booleanValue", Variables.booleanValue(booleanValue))
				.putValueTyped("doubleValue", Variables.doubleValue(doubleValue))
				.putValueTyped("integerValue", Variables.integerValue(integerValue))
				.putValueTyped("longValue", Variables.longValue(longValue))
				.putValueTyped("shortValue", Variables.shortValue(shortValue))
				.putValueTyped("byteArrayValue", Variables.byteArrayValue(byteArrayValue))
				.putValueTyped("nullValue", Variables.untypedNullValue());

			ProcessInstance process = startProcess(variables);

			SimpleVariablesExternalTaskHandler handler = new SimpleVariablesExternalTaskHandler();
			subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

			Assertions.assertThat(handler.stringValue).isEqualTo(stringValue);
			Assertions.assertThat(handler.dateValue).isEqualTo(dateValue);
			Assertions.assertThat(handler.booleanValue).isEqualTo(booleanValue);
			Assertions.assertThat(handler.doubleValue).isEqualTo(doubleValue);
			Assertions.assertThat(handler.integerValue).isEqualTo(integerValue);
			Assertions.assertThat(handler.longValue).isEqualTo(longValue);
			Assertions.assertThat(handler.shortValue).isEqualTo(shortValue);
			Assertions.assertThat(handler.byteArrayValue).isEqualTo(byteArrayValue);
			Assertions.assertThat(handler.nullValue).isNull();
		}

		@Test
		void shouldHandleSimpleVariables_withNull() {
			VariableMap variables = Variables
				.putValueTyped("stringValue", null)
				.putValueTyped("dateValue", null)
				.putValueTyped("booleanValue", null)
				.putValueTyped("doubleValue", null)
				.putValueTyped("integerValue", null)
				.putValueTyped("longValue", null)
				.putValueTyped("shortValue", null)
				.putValueTyped("byteArrayValue", null)
				.putValueTyped("nullValue", null);

			ProcessInstance process = startProcess(variables);

			SimpleVariablesExternalTaskHandler handler = new SimpleVariablesExternalTaskHandler();
			subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

			Assertions.assertThat(handler.stringValue).isNull();
			Assertions.assertThat(handler.dateValue).isNull();
			Assertions.assertThat(handler.booleanValue).isNull();
			Assertions.assertThat(handler.doubleValue).isNull();
			Assertions.assertThat(handler.integerValue).isNull();
			Assertions.assertThat(handler.longValue).isNull();
			Assertions.assertThat(handler.shortValue).isNull();
			Assertions.assertThat(handler.byteArrayValue).isNull();
			Assertions.assertThat(handler.nullValue).isNull();
		}
	}

	@Nested
	class Primitives {
		class SimplePrimitiveVariablesExternalTaskHandler extends ExternalTaskHandler {

			volatile boolean booleanValue;
			volatile double doubleValue;
			volatile int integerValue;
			volatile long longValue;
			volatile short shortValue;

			public SimplePrimitiveVariablesExternalTaskHandler() {
				super(null);
			}

			@HandlerMethod
			void handle(ExternalTask externalTask, ExternalTaskService externalTaskService,
						@SimpleVariable("booleanValue") boolean booleanValue,
						@SimpleVariable("doubleValue") double doubleValue,
						@SimpleVariable("integerValue") int integerValue,
						@SimpleVariable("longValue") long longValue,
						@SimpleVariable("shortValue") short shortValue) {
				this.booleanValue = booleanValue;
				this.doubleValue = doubleValue;
				this.integerValue = integerValue;
				this.longValue = longValue;
				this.shortValue = shortValue;
				externalTaskService.complete(externalTask);
			}
		}

		@Test
		void shouldHandleSimplePrimitiveVariables() {
			boolean booleanValue = true;
			double doubleValue = 2.34;
			int integerValue = 12345;
			long longValue = 123456789L;
			short shortValue = (short) 123;
			VariableMap variables = Variables
				.putValueTyped("booleanValue", Variables.booleanValue(booleanValue))
				.putValueTyped("doubleValue", Variables.doubleValue(doubleValue))
				.putValueTyped("integerValue", Variables.integerValue(integerValue))
				.putValueTyped("longValue", Variables.longValue(longValue))
				.putValueTyped("shortValue", Variables.shortValue(shortValue));

			ProcessInstance process = startProcess(variables);

			SimplePrimitiveVariablesExternalTaskHandler handler = new SimplePrimitiveVariablesExternalTaskHandler();
			subscribeStartHandlerAndWaitUntilProcessFinished(handler, process);

			Assertions.assertThat(handler.booleanValue).isEqualTo(booleanValue);
			Assertions.assertThat(handler.doubleValue).isEqualTo(doubleValue);
			Assertions.assertThat(handler.integerValue).isEqualTo(integerValue);
			Assertions.assertThat(handler.longValue).isEqualTo(longValue);
			Assertions.assertThat(handler.shortValue).isEqualTo(shortValue);
		}
	}

}
