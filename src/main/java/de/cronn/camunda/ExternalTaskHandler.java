package de.cronn.camunda;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ExternalTaskHandler implements org.camunda.bpm.client.task.ExternalTaskHandler {
	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());

	private final Method handlerMethod;
	private final List<ArgumentResolver> argumentResolvers;

	protected ExternalTaskHandler(ObjectMapper objectMapper) {
		handlerMethod = findHandlerMethod(getClass());
		argumentResolvers = ArgumentResolver.createArgumentResolvers(handlerMethod, objectMapper);
	}

	private static Method findHandlerMethod(Class<?> clazz) {
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

	@Override
	public final void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
		if (log.isDebugEnabled()) {
			log.debug(
				"Executing external task with id={}, processInstanceId={}, variables={}",
				externalTask.getId(),
				externalTask.getProcessInstanceId(),
				externalTask.getAllVariables()
			);
		} else {
			log.info(
				"Executing external task with id={}, processInstanceId={}",
				externalTask.getId(),
				externalTask.getProcessInstanceId()
			);
		}

		Object[] arguments = argumentResolvers.stream()
			.map(argumentResolver -> argumentResolver.resolve(externalTask, externalTaskService))
			.toArray();
		try {
			handlerMethod.setAccessible(true);
			handlerMethod.invoke(this, arguments);
		} catch (InvocationTargetException e) {
			Throwable targetException = e.getTargetException();
			if (targetException instanceof RuntimeException) {
				throw (RuntimeException) targetException;
			} else {
				throw new WrappedException(targetException);
			}
		} catch (IllegalAccessException e) {
			throw new WrappedException(e);
		}
	}

	public static class WrappedException extends RuntimeException {

		public WrappedException(Throwable cause) {
			super(cause);
		}
	}

}
