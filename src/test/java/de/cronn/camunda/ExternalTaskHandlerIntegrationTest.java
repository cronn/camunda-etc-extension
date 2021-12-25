package de.cronn.camunda;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import de.cronn.camunda.app.CamundaApp;

@SpringBootTest(classes = CamundaApp.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
abstract class ExternalTaskHandlerIntegrationTest extends BpmnAwareTests {

	private ExternalTaskClient externalTaskClient;

	@BeforeEach
	void createExternalTaskClient() {
		externalTaskClient = new ExternalTaskClientBuilderImpl()
			.baseUrl("http://localhost:8080/engine-rest/")
			.disableAutoFetching()
			.build();
	}

	@AfterEach
	void stopExternalTaskClient() {
		externalTaskClient.stop();
	}

	void waitUntilProcessFinished(ProcessInstance process) {
		Awaitility.await().atMost(10, TimeUnit.SECONDS)
			.untilAsserted(() -> assertThat(process).isEnded());
	}

	void subscribeAndStartHandler(org.camunda.bpm.client.task.ExternalTaskHandler handler) {
		externalTaskClient.subscribe("sampleTopic")
			.handler(handler)
			.open();
		externalTaskClient.start();
	}

	void subscribeStartHandlerAndWaitUntilProcessFinished(org.camunda.bpm.client.task.ExternalTaskHandler handler, ProcessInstance processInstance) {
		subscribeAndStartHandler(handler);
		waitUntilProcessFinished(processInstance);
	}

	ProcessInstance startProcess(VariableMap variables) {
		return runtimeService().startProcessInstanceByKey("sampleProcess", variables);
	}

}
