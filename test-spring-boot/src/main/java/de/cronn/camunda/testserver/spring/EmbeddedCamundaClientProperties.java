package de.cronn.camunda.testserver.spring;

import de.cronn.camunda.testserver.EmbeddedCamundaRestServer;

import org.camunda.bpm.client.spring.boot.starter.ClientProperties;
import org.springframework.context.annotation.Primary;

@Primary
public class EmbeddedCamundaClientProperties extends ClientProperties {

	private final EmbeddedCamundaRestServer embeddedCamundaRestServer;

	public EmbeddedCamundaClientProperties(EmbeddedCamundaRestServer embeddedCamundaRestServer) {
		this.embeddedCamundaRestServer = embeddedCamundaRestServer;
	}

	@Override
	public void setBaseUrl(String baseUrl) {
		// no, we don't support baseUrl property
	}

	@Override
	public String getBaseUrl() {
		return "http://localhost:" + embeddedCamundaRestServer.getPort() + "/engine-rest/";
	}
}
