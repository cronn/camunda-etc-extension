package de.cronn.camunda;

import java.lang.reflect.Parameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.cronn.camunda.ExternalTaskHandler.ExternalTaskAndExternalTaskService;
import de.cronn.camunda.dynamicmethod.DynamicMethod;

class ArgumentResolvers {

	private ArgumentResolvers() {
		// util class
	}

	public static final DynamicMethod.ArgumentResolver<ExternalTaskAndExternalTaskService> EXTERNAL_TASK_RESOLVER = ExternalTaskAndExternalTaskService::getExternalTask;
	public static final DynamicMethod.ArgumentResolver<ExternalTaskAndExternalTaskService> EXTERNAL_TASK_SERVICE_RESOLVER = ExternalTaskAndExternalTaskService::getExternalTaskService;
	public static final DynamicMethod.ArgumentResolver<ExternalTaskAndExternalTaskService> CURRENT_EXTERNAL_TASK_RESOLVER = CurrentExternalTask::new;

	static class SimpleVariableResolver implements DynamicMethod.ArgumentResolver<ExternalTaskAndExternalTaskService> {

		private final Parameter parameter;

		public SimpleVariableResolver(Parameter parameter) {
			this.parameter = parameter;
		}

		@Override
		public Object resolve(ExternalTaskAndExternalTaskService args) {
			SimpleVariable annotation = parameter.getAnnotation(SimpleVariable.class);
			String variableName = annotation.value();
			return args.getExternalTask().getVariable(variableName);
		}
	}

	static class JsonVariableResolver implements DynamicMethod.ArgumentResolver<ExternalTaskAndExternalTaskService> {

		private final Parameter parameter;
		private final ObjectMapper objectMapper;

		public JsonVariableResolver(Parameter parameter, ObjectMapper objectMapper) {
			if (objectMapper == null) {
				throw new IllegalStateException(
					"No " + ObjectMapper.class.getSimpleName() + " supplied so " +
						JsonVariable.class.getSimpleName() + " is not supported"
				);
			}
			this.parameter = parameter;
			this.objectMapper = objectMapper;
		}

		@Override
		public Object resolve(ExternalTaskAndExternalTaskService args) {
			JsonVariable annotation = parameter.getAnnotation(JsonVariable.class);
			String variableName = annotation.value();
			String json = args.getExternalTask().getVariable(variableName);
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


}
