package de.cronn.camunda;

import java.util.concurrent.TimeUnit;

import de.cronn.camunda.testserver.EmbeddedCamundaRestServer;

import org.awaitility.Awaitility;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.camunda.bpm.engine.variable.VariableMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

abstract class ExternalTaskHandlerIntegrationTest extends BpmnAwareTests {

	protected static EmbeddedCamundaRestServer embeddedCamundaRestServer = new EmbeddedCamundaRestServer(0, "sampleExternalTask.bpmn");

	private ExternalTaskClient externalTaskClient;

	@BeforeAll
	static void startCamunda() throws Exception {
		embeddedCamundaRestServer.start();
		AbstractAssertions.init(embeddedCamundaRestServer.getProcessEngine());
	}

	@AfterAll
	static void stopCamunda() throws Exception {
		embeddedCamundaRestServer.stop();
	}

	@BeforeEach
	void createExternalTaskClient() {
		externalTaskClient = new ExternalTaskClientBuilderImpl()
			.baseUrl("http://localhost:" + embeddedCamundaRestServer.getPort() + "/engine-rest/")
			.disableAutoFetching()
			.build();
	}

	@AfterEach
	void stopExternalTaskClient() {
		externalTaskClient.stop();
	}

	void waitUntilProcessFinished(ProcessInstance process) {
		Awaitility.with().pollInSameThread().await().atMost(10, TimeUnit.SECONDS)
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
