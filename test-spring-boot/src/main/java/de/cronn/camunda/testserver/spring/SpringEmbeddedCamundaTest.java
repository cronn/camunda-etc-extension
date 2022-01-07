package de.cronn.camunda.testserver.spring;

import de.cronn.camunda.testserver.EmbeddedCamundaRestServer;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

@TestPropertySource(properties = {
	"camunda.bpm.client.disable-auto-fetching=true",
})
@Import(EmbeddedCamundaRestServerConfig.class)
public class SpringEmbeddedCamundaTest extends BpmnAwareTests {

	@Value("${camunda.embedded.test.waitTimeMs:10000}")
	private int waitTimeMs;

	@Autowired
	protected EmbeddedCamundaRestServer embeddedCamundaRestServer;

	@Autowired
	protected ExternalTaskClient externalTaskClient;

	@AfterAll
	protected static void resetAssertions() {
		AbstractAssertions.reset();
	}

	@AfterEach
	protected void stopExternalTaskClient() {
		externalTaskClient.stop();
	}

	@AfterEach
	protected void deleteAllProcessInstances() {
		RuntimeService runtimeService = embeddedCamundaRestServer.getProcessEngine().getRuntimeService();
		for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
			runtimeService().deleteProcessInstance(processInstance.getId(), "cleanup");
		}
	}

	protected void startExternalTaskClient() {
		externalTaskClient.start();
	}

	protected void waitUntilProcessEnded(ProcessInstance process) {
		await().untilAsserted(() -> assertThat(process).isEnded());
	}

	protected ConditionFactory await() {
		String pollingThreadName = SpringEmbeddedCamundaTest.class.getSimpleName() + "-awaitPollingThread";
		return Awaitility.with()
			.pollThread(runnable -> new Thread(null, runnable, pollingThreadName))
			.await()
			.atMost(waitTimeMs, TimeUnit.MILLISECONDS);
	}

}
