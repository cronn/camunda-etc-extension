[![CI](https://github.com/cronn/camunda-etc-extension/workflows/CI/badge.svg)](https://github.com/cronn/camunda-etc-extension/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.cronn/camunda-etc-extension/badge.svg)](http://maven-badges.herokuapp.com/maven-central/de.cronn/camunda-etc-extension)
[![Apache 2.0](https://img.shields.io/github/license/cronn/camunda-etc-extension.svg)](http://www.apache.org/licenses/LICENSE-2.0)

[![Valid Gradle Wrapper](https://github.com/cronn/camunda-etc-extension/workflows/Validate%20Gradle%20Wrapper/badge.svg)](https://github.com/cronn/camunda-etc-extension/actions/workflows/gradle-wrapper-validation.yml)
[![Gradle Status](https://gradleupdate.appspot.com/cronn/camunda-etc-extension/status.svg)](https://gradleupdate.appspot.com/cronn/camunda-etc-extension/status)
# Cronn Camunda External Task Handler Extension

Extension of https://github.com/camunda/camunda-bpm-platform/tree/master/clients/java

## Features

 - easy extraction of process variables as java method arguments
 - less verbosity when executing external task actions

### 30 seconds tutorial

Instead of

```java
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;

class SampleHandler implements ExternalTaskHandler {
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String myVariable = externalTask.getVariable("myVariable");
        // ...
        externalTaskService.complete(externalTask);
    }
}
```

write this

```java
import de.cronn.camunda.CurrentExternalTask;
import de.cronn.camunda.ExternalTaskHandler;
import de.cronn.camunda.HandlerMethod;
import de.cronn.camunda.SimpleVariable;

class SampleHandler extends ExternalTaskHandler {
    public SampleHandler() {
        super(null);
    }

    @HandlerMethod
    public void handle(CurrentExternalTask currentExternalTask, 
                       @SimpleVariable("myVariable") String myVariable) {
        // ...
        currentExternalTask.complete();
    }
}
```

Each handler is expected to have exactly one method annotated with `@HandlerMethod`. This method has dynamic signature:

 - returns `void`
 - accepts multiple supported arguments:
     - `org.camunda.bpm.client.task.ExternalTask`
     - `org.camunda.bpm.client.task.ExternalTaskService`
     - `de.cronn.camunda.CurrentExternalTask`
     - process variables as simple values supported by Camunda (annotated with `@SimpleVariable`)
     - json process variables as object (annotated with `@JsonVariable`; this requires passing `ObjectMapper` in handler constructor)


## Usage
Add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>de.cronn</groupId>
    <artifactId>camunda-etc-extension</artifactId>
    <version>0.4.0</version>
</dependency>
```

## Bonus

There are two more artifacts: `de.cronn:camunda-etc-extension-test`
and `de.cronn:camunda-etc-extension-test-spring-boot`. They provide infrastructure for easy `ExternalTaskHandler` integration
testing. 
See:
 - for non Spring usage: [`EmbeddedCamundaTest`](test/src/main/java/de/cronn/camunda/testserver/EmbeddedCamundaTest.java)
and [`SampleEmbeddedCamundaTest`](test/src/test/java/de/cronn/camunda/testserver/SampleEmbeddedCamundaTest.java)
 - for Spring Boot use case: [`SpringEmbeddedCamundaTest`](test-spring-boot/src/main/java/de/cronn/camunda/testserver/spring/SpringEmbeddedCamundaTest.java)
and [`SampleSpringEmbeddedCamundaTest`](test-spring-boot/src/test/java/de/cronn/camunda/testserver/spring/SampleSpringEmbeddedCamundaTest.java)
