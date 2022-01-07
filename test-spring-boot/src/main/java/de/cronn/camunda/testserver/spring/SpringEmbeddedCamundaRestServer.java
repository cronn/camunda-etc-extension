package de.cronn.camunda.testserver.spring;

import de.cronn.camunda.testserver.EmbeddedCamundaRestServer;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

public class SpringEmbeddedCamundaRestServer extends EmbeddedCamundaRestServer implements InitializingBean, DisposableBean {

	public SpringEmbeddedCamundaRestServer(@Value("${camunda.embedded.server.port:0}") int port, @Value("${camunda.embedded.bpmnFiles:}") String... bpmnClasspathFilesToDeploy) {
		super(port, bpmnClasspathFilesToDeploy);
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}
}
