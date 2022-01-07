package de.cronn.camunda.testserver;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

class EmbeddedCamundaRestServerTest {

	private static HttpClient httpClient = HttpClient.newHttpClient();

	@Test
	void shouldUseRandomPortWhen0Requested() throws Throwable {
		doWithCamundaRestServer(
			0,
			Collections.emptyList(),
			(server) -> Assertions.assertThat(server.getPort()).isNotZero()
		);

	}

	@Test
	void shouldUseRequestedPort() throws Throwable {
		int port = tryToCreateServerSocket(0);
		doWithCamundaRestServer(
			port,
			Collections.emptyList(),
			(server) -> Assertions.assertThat(server.getPort()).isEqualTo(port)
		);
	}

	@Test
	void shouldStartCamundaRestServer() throws Throwable {
		doWithCamundaRestServer(
			0,
			Collections.emptyList(),
			(server) -> {
				HttpResponse<String> response = executeHttpGet(
					"http://localhost:" + server.getPort() + "/engine-rest/version"
				);
				Assertions.assertThat(response.statusCode()).isEqualTo(200);
				Assertions.assertThat(response.body()).isEqualTo("{\"version\":\"7.15.0\"}");
			}
		);
	}

	@Test
	void shouldStopCamundaRestServer() throws Throwable {
		AtomicInteger port = new AtomicInteger();
		AtomicReference<EmbeddedCamundaRestServer> camundaRestServer = new AtomicReference<>();
		doWithCamundaRestServer(
			0,
			Collections.emptyList(),
			(server) -> {
				port.set(server.getPort());
				camundaRestServer.set(server);
			}
		);

		Assertions.assertThatThrownBy(() -> new Socket("localhost", port.get()))
			.isNotNull()
			.isInstanceOf(ConnectException.class)
			.hasMessage("Connection refused (Connection refused)");
		Assertions.assertThat(camundaRestServer.get().getPort()).isZero();
	}

	@Test
	void shouldThrowOnLifecycleMisusage() throws Throwable {
		AtomicReference<EmbeddedCamundaRestServer> camundaRestServer = new AtomicReference<>();
		doWithCamundaRestServer(
			0,
			Collections.emptyList(),
			(server) -> {
				camundaRestServer.set(server);
				Assertions.assertThatThrownBy(server::start)
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("Server already running");
			}
		);

		Assertions.assertThatThrownBy(() -> camundaRestServer.get().stop())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Server is not running");
	}

	@Test
	void shouldDeployBpmnFiles() throws Throwable {
		doWithCamundaRestServer(
			0,
			Arrays.asList("sampleProcess1.bpmn", "sampleProcess2.bpmn"),
			(server) -> {
				HttpResponse<String> response = executeHttpGet(
					"http://localhost:" + server.getPort() + "/engine-rest/process-definition"
				);
				Assertions.assertThat(response.statusCode()).isEqualTo(200);
				Assertions.assertThat(response.body())
					.contains("\"resource\":\"sampleProcess1.bpmn\"")
					.contains("\"resource\":\"sampleProcess2.bpmn\"");
			}
		);
	}

	@Test
	void shouldCloseH2Database() throws Throwable {
		AtomicReference<String> h2DatabaseUrl = new AtomicReference<>();
		doWithCamundaRestServer(
			0,
			Collections.emptyList(),
			(server) -> {
				h2DatabaseUrl.set(server.inMemoryH2DatabaseUrl);
			}
		);

		Assertions.assertThatThrownBy(() -> DriverManager.getConnection(h2DatabaseUrl.get() + ";IFEXISTS=true"))
			.isNotNull()
			.isInstanceOf(SQLException.class)
			.hasMessageMatching("Database \"mem:EmbeddedCamundaRestServer_.+?\" not found, and IFEXISTS=true, so we cant auto-create it.*");
	}

	private HttpResponse<String> executeHttpGet(String uri) throws URISyntaxException, IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(new URI(uri))
			.GET()
			.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private static void doWithCamundaRestServer(int port, List<String> bpmnClasspathFilesToDeploy, ThrowingConsumer<EmbeddedCamundaRestServer> action) throws Throwable {
		EmbeddedCamundaRestServer camundaRestServer = new EmbeddedCamundaRestServer(port, bpmnClasspathFilesToDeploy.toArray(new String[0]));
		camundaRestServer.start();
		try {
			action.accept(camundaRestServer);
		} finally {
			camundaRestServer.stop();
		}
	}

	private static int tryToCreateServerSocket(int requested) throws IOException {
		try (ServerSocket serverSocket = new ServerSocket(requested)) {
			return serverSocket.getLocalPort();
		}
	}

}
