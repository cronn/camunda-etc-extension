package de.cronn.camunda;

import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HandlerMethodLookupTest {

	private static class Handler extends ExternalTaskHandler {
		public Handler() {
			super(null);
		}

		@HandlerMethod
		void handle() { }
	}

	private final ExternalTask externalTask = Mockito.mock(ExternalTask.class);
	private final ExternalTaskService externalTaskService = Mockito.mock(ExternalTaskService.class);

	@Test
	void shouldFindHandlerMethod() {
		Handler handler = Mockito.spy(new Handler());

		handler.execute(externalTask, externalTaskService);

		Mockito.verify(handler).handle();
	}

	@Test
	void shouldFindPrivateHandlerMethod() {
		AtomicBoolean mutableBoolean = new AtomicBoolean(false);
		ExternalTaskHandler handler = Mockito.spy(new ExternalTaskHandler(null) {
			@HandlerMethod
			private void handle() { mutableBoolean.set(true); }
		});

		handler.execute(externalTask, externalTaskService);

		Assertions.assertThat(mutableBoolean).isTrue();
	}

	@Test
	void shouldThrowWhenMultipleMethodsAnnotated() {
		Assertions.assertThatThrownBy(
				() -> new Handler() {
					@HandlerMethod
					void handle2() { }
				})
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Expected exactly one method annotated with HandlerMethod but got: [handle2, handle]");
	}

	@Test
	void shouldThrowWhenHandlerMethodHasWrongReturnType() {
		Assertions.assertThatThrownBy(
				() -> new ExternalTaskHandler(null) {
					@HandlerMethod
					VariableMap handle() {
						return null;
					}
				})
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Handler method must return void");
	}

	@Test
	void shouldThrowWhenHandlerMethodHasUnsupportedArgument() {
		Assertions.assertThatThrownBy(
				() -> new ExternalTaskHandler(null) {
					@HandlerMethod
					void handle(String nonAnnotatedArgument) { }
				})
			.isNotNull()
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Unsupported argument: arg0");
	}
}
