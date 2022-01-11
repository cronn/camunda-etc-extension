package de.cronn.camunda.dynamicmethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DynamicMethod<STATIC_ARGUMENTS extends DynamicMethod.StaticArguments> {

	private final Method method;
	private final List<ArgumentResolver<STATIC_ARGUMENTS>> argumentResolvers;

	public DynamicMethod(Method method, ArgumentResolverFactory<STATIC_ARGUMENTS>... argumentResolversFactories) {
		this.method = method;
		List<ArgumentResolver<STATIC_ARGUMENTS>> resolvers = new ArrayList<>();
		for (Parameter parameter : method.getParameters()) {
			Optional<ArgumentResolver<STATIC_ARGUMENTS>> argumentResolver = Arrays.stream(argumentResolversFactories)
				.map(factory -> factory.create(parameter))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
			if (argumentResolver.isEmpty()) {
				throw new IllegalStateException("Unsupported argument: " + parameter.getName());
			}
			resolvers.add(argumentResolver.get());
		}
		this.argumentResolvers = resolvers;
	}

	protected void invoke(Object target, STATIC_ARGUMENTS staticArguments) throws InvocationTargetException, IllegalAccessException {
		Object[] arguments = argumentResolvers.stream()
			.map(resolver -> resolver.resolve(staticArguments))
			.toArray();
		method.setAccessible(true);
		method.invoke(target, arguments);
	}

	protected static <STATIC_ARGUMENTS extends DynamicMethod.StaticArguments> ArgumentResolverFactory<STATIC_ARGUMENTS> onType(Class<?> type, ArgumentResolver<STATIC_ARGUMENTS> resolver) {
		return parameter -> parameter.getType().equals(type) ? Optional.of(resolver) : Optional.empty();
	}

	protected static <STATIC_ARGUMENTS extends DynamicMethod.StaticArguments> ArgumentResolverFactory<STATIC_ARGUMENTS> onAnnotation(Class<? extends Annotation> annotation, Function<Parameter, ArgumentResolver<STATIC_ARGUMENTS>> resolver) {
		return parameter -> parameter.isAnnotationPresent(annotation) ? Optional.of(resolver.apply(parameter)) : Optional.empty();
	}

	public interface StaticArguments {}

	public interface ArgumentResolver<STATIC_ARGUMENTS extends DynamicMethod.StaticArguments> {
		Object resolve(STATIC_ARGUMENTS staticArguments);
	}

	public interface ArgumentResolverFactory<STATIC_ARGUMENTS extends DynamicMethod.StaticArguments> {
		Optional<ArgumentResolver<STATIC_ARGUMENTS>> create(Parameter parameter);
	}

}

