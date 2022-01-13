package de.cronn.camunda;

import java.lang.reflect.InvocationTargetException;

import de.cronn.camunda.dynamicmethod.DynamicMethod;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ExternalTaskHandler implements org.camunda.bpm.client.task.ExternalTaskHandler {
	protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());

	private final DynamicHandlerMethod handlerMethod;

	protected ExternalTaskHandler(ObjectMapper objectMapper) {
		handlerMethod = new DynamicHandlerMethod(DynamicHandlerMethod.findHandlerMethod(getClass()), objectMapper);
	}

	@Override
	public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
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

		try {
			handlerMethod.invoke(this, externalTask, externalTaskService);
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

	interface ExternalTaskAndExternalTaskService extends DynamicMethod.StaticArguments {
		ExternalTask getExternalTask();
		ExternalTaskService getExternalTaskService();
	}

}
