package de.cronn.camunda;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.google.common.base.Defaults;

class CurrentExternalTaskTest {

	public static Stream<Method> externalTaskMethods() {
		return Stream.of(ExternalTask.class.getDeclaredMethods());
	}

	public static Stream<Method> externalTaskServiceMethodsWithExternalTaskArgument() {
		return Stream.of(ExternalTaskService.class.getDeclaredMethods())
			.filter(method -> ArrayUtils.contains(method.getParameterTypes(), ExternalTask.class));
	}

	@MethodSource("externalTaskMethods")
	@ParameterizedTest
	void shouldDelegateAllExternalTaskMethods(Method externalTaskMethod) throws Exception {
		ExternalTask delegate = Mockito.mock(ExternalTask.class);
		CurrentExternalTask currentExternalTask = new CurrentExternalTask(null, delegate);

		Object[] arguments = createDummyArguments(externalTaskMethod);
		externalTaskMethod.invoke(currentExternalTask, arguments);

		externalTaskMethod.invoke(Mockito.verify(delegate), arguments);
		Mockito.verifyNoMoreInteractions(delegate);
	}

	@MethodSource("externalTaskServiceMethodsWithExternalTaskArgument")
	@ParameterizedTest
	void shouldDelegateExternalTaskServiceMethodsWithExternalTaskArgument(Method externalTaskServiceMethod) throws Exception {
		ExternalTaskService externalTaskService = Mockito.mock(ExternalTaskService.class);
		ExternalTask externalTask = Mockito.mock(ExternalTask.class);
		CurrentExternalTask currentExternalTask = new CurrentExternalTask(externalTaskService, externalTask);

		Object[] arguments = createDummyArguments(externalTaskServiceMethod);
		replaceFirst(arguments, ExternalTask.class::isInstance, externalTask);

		Method currentExternalTaskMethod = findCorrespondingCurrentExternalTaskMethod(externalTaskServiceMethod);
		Object[] argumentsWithoutExternalTask = removeFirst(arguments, ExternalTask.class::isInstance);

		currentExternalTaskMethod.invoke(currentExternalTask, argumentsWithoutExternalTask);

		externalTaskServiceMethod.invoke(Mockito.verify(externalTaskService), arguments);
		Mockito.verify(externalTask).getId();
		Mockito.verifyNoMoreInteractions(externalTask);
		Mockito.verifyNoMoreInteractions(externalTaskService);
	}

	private Method findCorrespondingCurrentExternalTaskMethod(Method externalTaskServiceMethod) throws NoSuchMethodException {
		return CurrentExternalTask.class.getDeclaredMethod(
			externalTaskServiceMethod.getName(),
			removeFirst(externalTaskServiceMethod.getParameterTypes(), o -> o.equals(ExternalTask.class))
		);
	}

	private <T> T[] removeFirst(T[] array, Predicate<Object> condition) {
		int indexToRemove = -1;
		for (int i = 0; i < array.length; i++) {
			if (condition.test(array[i])) {
				indexToRemove = i;
				break;
			}
		}
		return ArrayUtils.remove(array, indexToRemove);
	}

	private void replaceFirst(Object[] array, Predicate<Object> condition, Object value) {
		for (int i = 0; i < array.length; i++) {
			if (condition.test(array[i])) {
				array[i] = value;
				return;
			}
		}
	}

	private static Object[] createDummyArguments(Method method) {
		return Stream.of(method.getParameterTypes())
			.map(CurrentExternalTaskTest::createDummyValue)
			.toArray();
	}

	private static Object createDummyValue(Class<?> clazz) {
		Object defaultValue = Defaults.defaultValue(clazz);
		if (defaultValue != null) {
			return defaultValue;
		}
		if (clazz == String.class) {
			return UUID.randomUUID().toString();
		}
		return Mockito.mock(clazz);
	}

}
