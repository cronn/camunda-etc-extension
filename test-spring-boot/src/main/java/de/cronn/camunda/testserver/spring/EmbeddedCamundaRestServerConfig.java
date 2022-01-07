package de.cronn.camunda.testserver.spring;

import org.springframework.context.annotation.Import;

@Import({
	SpringEmbeddedCamundaRestServer.class,
	EmbeddedCamundaClientProperties.class
})
public class EmbeddedCamundaRestServerConfig {
}
