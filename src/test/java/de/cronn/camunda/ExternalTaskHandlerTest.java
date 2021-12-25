package de.cronn.camunda;

import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExternalTaskHandlerTest {

	private final EngineClient engineClient = Mockito.mock(EngineClient.class);
	private final ExternalTask externalTask = Mockito.mock(ExternalTask.class);
	private final ExternalTaskService externalTaskService = new ExternalTaskServiceImpl(engineClient);

	@Test
	void shouldSupportExternalTaskAndExternalTaskClientServiceAsArguments() {
		AtomicReference<ExternalTask> externalTaskReference = new AtomicReference<>();
		AtomicReference<ExternalTaskService> externalTaskServiceReference = new AtomicReference<>();
		ExternalTaskHandler handler = new ExternalTaskHandler(null) {
			@HandlerMethod
			void handle(ExternalTask externalTask, ExternalTaskService externalTaskService) {
				externalTaskReference.set(externalTask);
				externalTaskServiceReference.set(externalTaskService);
			}
		};

		handler.execute(externalTask, externalTaskService);

		Assertions.assertThat(externalTaskReference.get()).isSameAs(externalTask);
		Assertions.assertThat(externalTaskServiceReference.get()).isSameAs(externalTaskService);
	}

	@Test
	void shouldSupportCurrentExternalTaskAsArgument() {
		AtomicReference<CurrentExternalTask> currentExternalTaskReference = new AtomicReference<>();
		ExternalTaskHandler handler = new ExternalTaskHandler(null) {
			@HandlerMethod
			void handle(CurrentExternalTask currentExternalTaskService) {
				currentExternalTaskReference.set(currentExternalTaskService);
			}
		};

		handler.execute(externalTask, externalTaskService);

		CurrentExternalTask actual = currentExternalTaskReference.get();
		Assertions.assertThat(actual).isNotNull();
	}

	@Test
	void shouldThrowIfRuntimeExceptionThrown() {
		RuntimeException runtimeException = new RuntimeException();
		ExternalTaskHandler handler = new ExternalTaskHandler(null) {
			@HandlerMethod
			void handle() {
				throw runtimeException;
			}
		};

		Assertions.assertThatThrownBy(() -> handler.execute(externalTask, externalTaskService))
			.isNotNull()
			.isSameAs(runtimeException);

		Mockito.verifyNoInteractions(engineClient);
	}

	@Test
	void shouldThrowIfExceptionThrown() {
		Exception exception = new Exception();
		ExternalTaskHandler handler = new ExternalTaskHandler(null) {
			@HandlerMethod
			void handle() throws Exception {
				throw exception;
			}
		};

		Assertions.assertThatThrownBy(() -> handler.execute(externalTask, externalTaskService))
			.isNotNull()
			.isInstanceOf(ExternalTaskHandler.WrappedException.class)
			.hasCauseReference(exception);

		Mockito.verifyNoInteractions(engineClient);
	}

}
