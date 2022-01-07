package de.cronn.camunda.testserver.spring;

import de.cronn.camunda.sampleapp.Application;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
	classes = Application.class,
	properties = {
		"camunda.embedded.bpmnFiles=sample.bpmn"
	}
)
class SampleSpringEmbeddedCamundaTest extends SpringEmbeddedCamundaTest {

	@Test
	void shouldCompleteSampleExternalTask() {
		ProcessInstance process = runtimeService().startProcessInstanceByKey("sampleProcess");
		assertThat(process).isWaitingAt("sampleExternalTask");

		startExternalTaskClient();

		waitUntilProcessEnded(process);

		assertThat(process).isEnded();
	}
}
