package de.cronn.camunda.testserver;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SampleEmbeddedCamundaTest extends EmbeddedCamundaTest {

	@BeforeAll
	static void deployBpmnFiles() {
		camundaRestServer.deployBpmnClasspathFiles("sampleExternalTask.bpmn");
	}

	@Test
	void shouldCompleteSampleExternalTask() {
		ProcessInstance process = runtimeService().startProcessInstanceByKey("sampleProcess");
		assertThat(process).isWaitingAt("sampleExternalTask");

		externalTaskClient.subscribe("sampleTopic")
			.handler((externalTask, externalTaskService) -> externalTaskService.complete(externalTask))
			.open();
		externalTaskClient.start();

		Awaitility.with().pollInSameThread().await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> assertThat(process).isEnded());

		assertThat(process).isEnded();
	}

}
