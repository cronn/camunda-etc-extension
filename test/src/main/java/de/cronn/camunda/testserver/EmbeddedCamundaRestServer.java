package de.cronn.camunda.testserver;


import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.rest.impl.CamundaRestResources;
import org.camunda.bpm.engine.rest.impl.FetchAndLockContextListener;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EmbeddedCamundaRestServer {

	private final List<String> bpmnClasspathFilesToDeploy;
	private final int port;

	private Server server;
	private ProcessEngine processEngine;
	final String inMemoryH2DatabaseUrl = "jdbc:h2:mem:" + EmbeddedCamundaRestServer.class.getSimpleName() + "_"+ UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false";

	public EmbeddedCamundaRestServer(int port, String... bpmnClasspathFilesToDeploy) {
		this.bpmnClasspathFilesToDeploy = Arrays.asList(bpmnClasspathFilesToDeploy);
		this.port = port;
	}

	public void start() throws Exception {
		if (server != null) {
			throw new IllegalStateException("Server already running");
		}
		processEngine = createProcessEngine();
		deployBpmnClasspathFiles(bpmnClasspathFilesToDeploy.toArray(String[]::new));
		startHttpServerWithCamundaRestApplication();
	}

	private void startHttpServerWithCamundaRestApplication() throws Exception {
		server = new Server(port);
		server.setHandler(createServletContextWithCamundaRestApplication());
		server.start();
	}

	private ServletContextHandler createServletContextWithCamundaRestApplication() {
		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.addEventListener(new FetchAndLockContextListener());
		CXFNonSpringServlet servlet = new CXFNonSpringJaxrsServlet();
		ServletHolder servletHolder = new ServletHolder(servlet);
		servletHolder.setInitParameter("jaxrs.serviceClasses", toString(CamundaRestResources.getResourceClasses()));
		servletHolder.setInitParameter("jaxrs.providers", toString(CamundaRestResources.getConfigurationClasses()));
		servletHolder.setInitParameter("jaxrs.address", "/engine-rest");
		context.addServlet(servletHolder, "/*");
		return context;
	}

	private ProcessEngine createProcessEngine() {
		StandaloneInMemProcessEngineConfiguration processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
		processEngineConfiguration.setProcessEnginePlugins(Arrays.asList(new SpinProcessEnginePlugin()));
		return processEngineConfiguration
			.setJdbcUrl(inMemoryH2DatabaseUrl)
			.setJobExecutorActivate(true)
			.buildProcessEngine();
	}

	public void deployBpmnClasspathFiles(String... bpmnClasspathFilesToDeploy) {
		if (bpmnClasspathFilesToDeploy.length > 0) {
			DeploymentBuilder deployment = processEngine.getRepositoryService()
				.createDeployment();
			for (String bpmnClasspathFileToDeploy : bpmnClasspathFilesToDeploy) {
				deployment.addClasspathResource(bpmnClasspathFileToDeploy);
			}
			deployment.deploy();
		}
	}

	private static String toString(Collection<Class<?>> classes) {
		return classes.stream()
			.map(Class::getName)
			.collect(Collectors.joining(","));
	}

	public int getPort() {
		if (server != null && server.isRunning()) {
			return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
		} else {
			return port;
		}
	}

	public ProcessEngine getProcessEngine() {
		return processEngine;
	}

	public void stop() throws Exception {
		stopHttpServer();
		processEngine.close();
		shutdownDatabase();

		server = null;
		processEngine = null;
	}

	private void stopHttpServer() throws Exception {
		if (server == null) {
			throw new IllegalStateException("Server is not running");
		}
		server.stop();
		server.join();
	}

	private void shutdownDatabase() throws SQLException {
		try (Connection connection = DriverManager.getConnection(inMemoryH2DatabaseUrl, "sa", "")) {
			try (Statement statement = connection.createStatement()) {
				statement.execute("SHUTDOWN");
			}
		}
	}
}
