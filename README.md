[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.cronn/camunda-etc-extension/badge.svg)](http://maven-badges.herokuapp.com/maven-central/de.cronn/camunda-etc-extension)
[![Apache 2.0](https://img.shields.io/github/license/cronn/camunda-etc-extension.svg)](http://www.apache.org/licenses/LICENSE-2.0)

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
    <version>0.1.0</version>
</dependency>
```
