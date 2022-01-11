package de.cronn.camunda;

import static de.cronn.camunda.ArgumentResolvers.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.cronn.camunda.ExternalTaskHandler.ExternalTaskAndExternalTaskService;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;

import de.cronn.camunda.dynamicmethod.DynamicMethod;

class DynamicHandlerMethod extends DynamicMethod<ExternalTaskAndExternalTaskService> {

	public DynamicHandlerMethod(Method method, ObjectMapper objectMapper) {
		super(
			method,
			onType(ExternalTask.class, EXTERNAL_TASK_RESOLVER),
			onType(ExternalTaskService.class, EXTERNAL_TASK_SERVICE_RESOLVER),
			onType(CurrentExternalTask.class, CURRENT_EXTERNAL_TASK_RESOLVER),
			onAnnotation(
				SimpleVariable.class,
				parameter -> new SimpleVariableResolver(parameter)
			),
			onAnnotation(
				JsonVariable.class,
				parameter -> new JsonVariableResolver(parameter, objectMapper)
			)
		);
	}

	static Method findHandlerMethod(Class<?> clazz) {
		List<Method> annotatedMethods = MethodUtils.getMethodsListWithAnnotation(clazz, HandlerMethod.class, true, true);
		annotatedMethods.removeIf(Method::isSynthetic);
		if (annotatedMethods.size() != 1) {
			throw new IllegalStateException(
				String.format(
					"Expected exactly one method annotated with %s but got: %s",
					HandlerMethod.class.getSimpleName(),
					annotatedMethods.stream().map(Method::getName).collect(Collectors.joining(", ", "[", "]"))
				)
			);
		}
		Method handlerMethod = annotatedMethods.get(0);
		if (!void.class.equals(handlerMethod.getReturnType())) {
			throw new IllegalStateException("Handler method must return " + void.class.getSimpleName());
		}
		return handlerMethod;
	}

	public void invoke(Object target, ExternalTask externalTask, ExternalTaskService externalTaskService) throws InvocationTargetException, IllegalAccessException {
		invoke(target, new StaticArguments(externalTask, externalTaskService));
	}

	protected static class StaticArguments implements ExternalTaskAndExternalTaskService {
		private final ExternalTask externalTask;
		private final ExternalTaskService externalTaskService;

		public StaticArguments(ExternalTask externalTask, ExternalTaskService externalTaskService) {
			this.externalTask = externalTask;
			this.externalTaskService = externalTaskService;
		}

		@Override
		public ExternalTask getExternalTask() {
			return externalTask;
		}

		@Override
		public ExternalTaskService getExternalTaskService() {
			return externalTaskService;
		}
	}
}
