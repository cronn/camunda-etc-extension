package de.cronn.camunda;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@FunctionalInterface
interface ArgumentResolver {

	Object resolve(ExternalTask externalTask, ExternalTaskService externalTaskService);

	ArgumentResolver EXTERNAL_TASK_RESOLVER = (externalTask, externalTaskService) -> externalTask;
	ArgumentResolver EXTERNAL_TASK_SERVICE_RESOLVER = (externalTask, externalTaskService) -> externalTaskService;
	ArgumentResolver CURRENT_EXTERNAL_TASK_RESOLVER = (externalTask, externalTaskService) -> new CurrentExternalTask(externalTaskService, externalTask);

	class SimpleVariableResolver implements ArgumentResolver {

		private final Parameter parameter;

		public SimpleVariableResolver(Parameter parameter) {
			this.parameter = parameter;
		}

		@Override
		public Object resolve(ExternalTask externalTask, ExternalTaskService externalTaskService) {
			SimpleVariable annotation = parameter.getAnnotation(SimpleVariable.class);
			String variableName = annotation.value();
			return externalTask.getVariable(variableName);
		}
	}

	class JsonVariableResolver implements ArgumentResolver {

		private final Parameter parameter;
		private final ObjectMapper objectMapper;

		public JsonVariableResolver(Parameter parameter, ObjectMapper objectMapper) {
			this.parameter = parameter;
			this.objectMapper = objectMapper;
		}

		@Override
		public Object resolve(ExternalTask externalTask, ExternalTaskService externalTaskService) {
			JsonVariable annotation = parameter.getAnnotation(JsonVariable.class);
			String variableName = annotation.value();
			String json = externalTask.getVariable(variableName);
			if (json == null) {
				return null;
			}
			try {
				return objectMapper.readValue(json, objectMapper.constructType(parameter.getType()));
			} catch (JsonProcessingException e) {
				throw new JsonProcessingRuntimeException(e);
			}
		}
	}

	static List<ArgumentResolver> createArgumentResolvers(Method handlerMethod, ObjectMapper objectMapper) {
		List<ArgumentResolver> resolvers = new ArrayList<>();
		for (int i = 0; i < handlerMethod.getParameterCount(); i++) {
			Parameter parameter = handlerMethod.getParameters()[i];
			if (parameter.getType().equals(ExternalTask.class)) {
				resolvers.add(EXTERNAL_TASK_RESOLVER);
			} else if (parameter.getType().equals(ExternalTaskService.class)) {
				resolvers.add(EXTERNAL_TASK_SERVICE_RESOLVER);
			} else if (parameter.getType().equals(CurrentExternalTask.class)) {
				resolvers.add(CURRENT_EXTERNAL_TASK_RESOLVER);
			} else if (parameter.isAnnotationPresent(SimpleVariable.class)) {
				resolvers.add(new SimpleVariableResolver(parameter));
			} else if (parameter.isAnnotationPresent(JsonVariable.class)) {
				if (objectMapper == null) {
					throw new IllegalStateException("No " + ObjectMapper.class.getSimpleName() + " supplied so JsonVariable is not supported");
				}
				resolvers.add(new JsonVariableResolver(parameter, objectMapper));
			} else {
				throw new IllegalStateException("Unsupported argument: " + parameter.getName());
			}
		}
		return resolvers;
	}

}
