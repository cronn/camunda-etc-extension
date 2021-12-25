package de.cronn.camunda;

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentExternalTask implements ExternalTask {

	private final Logger log = LoggerFactory.getLogger(CurrentExternalTask.class);

	private final ExternalTaskService externalTaskService;
	private final ExternalTask externalTask;

	public CurrentExternalTask(ExternalTaskService externalTaskService, ExternalTask externalTask) {
		this.externalTaskService = externalTaskService;
		this.externalTask = externalTask;
	}

	private void logAction(String action) {
		log.info("Executing '{}' action for external task with id={}", action, externalTask.getId());
	}

	private void logComplete() {
		logAction("complete");
	}

	private void logHandleFailure() {
		logAction("handle failure");
	}

	private void logHandleBpmnError() {
		logAction("handle bpmn error");
	}

	public void lock(long lockDuration) {
		logAction("lock");
		externalTaskService.lock(externalTask, lockDuration);
	}

	public void unlock() {
		logAction("unlock");
		externalTaskService.unlock(externalTask);
	}

	public void complete() {
		logComplete();
		externalTaskService.complete(externalTask);
	}

	public void complete(Map<String, Object> variables) {
		logComplete();
		externalTaskService.complete(externalTask, variables);
	}

	public void complete(Map<String, Object> variables, Map<String, Object> localVariables) {
		logComplete();
		externalTaskService.complete(externalTask, variables, localVariables);
	}

	public void handleFailure(String errorMessage, String errorDetails, int retries, long retryTimeout) {
		logHandleFailure();
		externalTaskService.handleFailure(externalTask, errorMessage, errorDetails, retries, retryTimeout);
	}

	public void handleFailure(String errorMessage, String errorDetails, int retries, long retryTimeout, Map<String, Object> variables, Map<String, Object> localVariables) {
		logHandleFailure();
		externalTaskService.handleFailure(externalTask.getId(), errorMessage, errorDetails, retries, retryTimeout, variables, localVariables);
	}

	public void handleBpmnError(String errorCode) {
		logHandleBpmnError();
		externalTaskService.handleBpmnError(externalTask, errorCode);
	}

	public void handleBpmnError(String errorCode, String errorMessage) {
		logHandleBpmnError();
		externalTaskService.handleBpmnError(externalTask, errorCode, errorMessage);
	}

	public void handleBpmnError(String errorCode, String errorMessage, Map<String, Object> variables) {
		logHandleBpmnError();
		externalTaskService.handleBpmnError(externalTask, errorCode, errorMessage, variables);
	}

	public void extendLock(long newDuration) {
		logAction("extend lock");
		externalTaskService.extendLock(externalTask, newDuration);
	}

	// ExternalTask interface:

	@Override
	public String getActivityId() {
		return externalTask.getActivityId();
	}

	@Override
	public String getActivityInstanceId() {
		return externalTask.getActivityInstanceId();
	}

	@Override
	public String getErrorMessage() {
		return externalTask.getErrorMessage();
	}

	@Override
	public String getErrorDetails() {
		return externalTask.getErrorDetails();
	}

	@Override
	public String getExecutionId() {
		return externalTask.getExecutionId();
	}

	@Override
	public String getId() {
		return externalTask.getId();
	}

	@Override
	public Date getLockExpirationTime() {
		return externalTask.getLockExpirationTime();
	}

	@Override
	public String getProcessDefinitionId() {
		return externalTask.getProcessDefinitionId();
	}

	@Override
	public String getProcessDefinitionKey() {
		return externalTask.getProcessDefinitionKey();
	}

	@Override
	public String getProcessDefinitionVersionTag() {
		return externalTask.getProcessDefinitionVersionTag();
	}

	@Override
	public String getProcessInstanceId() {
		return externalTask.getProcessInstanceId();
	}

	@Override
	public Integer getRetries() {
		return externalTask.getRetries();
	}

	@Override
	public String getWorkerId() {
		return externalTask.getWorkerId();
	}

	@Override
	public String getTopicName() {
		return externalTask.getTopicName();
	}

	@Override
	public String getTenantId() {
		return externalTask.getTenantId();
	}

	@Override
	public long getPriority() {
		return externalTask.getPriority();
	}

	@Override
	public <T> T getVariable(String variableName) {
		return externalTask.getVariable(variableName);
	}

	@Override
	public <T extends TypedValue> T getVariableTyped(String variableName) {
		return externalTask.getVariableTyped(variableName);
	}

	@Override
	public <T extends TypedValue> T getVariableTyped(String variableName, boolean deserializeObjectValue) {
		return externalTask.getVariableTyped(variableName, deserializeObjectValue);
	}

	@Override
	public Map<String, Object> getAllVariables() {
		return externalTask.getAllVariables();
	}

	@Override
	public VariableMap getAllVariablesTyped() {
		return externalTask.getAllVariablesTyped();
	}

	@Override
	public VariableMap getAllVariablesTyped(boolean deserializeObjectValues) {
		return externalTask.getAllVariablesTyped(deserializeObjectValues);
	}

	@Override
	public String getBusinessKey() {
		return externalTask.getBusinessKey();
	}

	@Override
	public String getExtensionProperty(String propertyKey) {
		return externalTask.getExtensionProperty(propertyKey);
	}

	@Override
	public Map<String, String> getExtensionProperties() {
		return externalTask.getExtensionProperties();
	}
}
