package de.cronn.camunda.testserver;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl;
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class EmbeddedCamundaTest extends BpmnAwareTests {

	protected static EmbeddedCamundaRestServer camundaRestServer = new EmbeddedCamundaRestServer(0);
	protected ExternalTaskClient externalTaskClient;

	@BeforeAll
	static void startServer() throws Exception {
		camundaRestServer.start();
		AbstractAssertions.init(camundaRestServer.getProcessEngine());
	}

	@AfterAll
	static void stopServer() throws Exception {
		camundaRestServer.stop();
		AbstractAssertions.reset();
	}

	@BeforeEach
	void createExternalTaskClient() {
		externalTaskClient = new ExternalTaskClientBuilderImpl()
			.baseUrl("http://localhost:" + camundaRestServer.getPort() + "/engine-rest/")
			.disableAutoFetching()
			.build();
	}

	@AfterEach
	void stopExternalTaskClient() {
		externalTaskClient.stop();
	}

}
