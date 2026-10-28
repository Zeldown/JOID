package dev.joid.tool.docgen.source;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;

import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.TypeKind;
import dev.joid.tool.docgen.page.ApiProperty;
import dev.joid.tool.docgen.page.TypePage;
import lombok.Getter;

@Getter
public final class DefaultReader {

	private final Map<String, ApiType> types;
	private final boolean              runtime;
	private final Map<String, String>  failed;

	private int instances;
	private int failures;

	private DefaultReader(final Map<String, ApiType> types, final boolean runtime) {
		this.types   = types;
		this.runtime = runtime;
		this.failed  = new TreeMap<>();
	}

	public static DefaultReader create(final Map<String, ApiType> types, final boolean runtime) {
		return new DefaultReader(types, runtime);
	}

	public void apply(final TypePage page) {
		if (page.getProperties().isEmpty()) {
			return;
		}

		final Object instance = this.runtime ? this.instance(page.getType()) : null;
		for (final ApiProperty property : page.getProperties().values()) {
			final String source = this.sourceDefault(page.getType(), property, "");
			final String implicit = source == null ? this.sourceDefault(page.getType(), property, "#implicit") : null;
			String value = source == null ? implicit : source;
			if (instance != null && property.getGetter() != null && !"@parameter".equals(source)) {
				final Object read = DefaultReader.read(instance, property.getGetter());
				final String formatted = DefaultReader.format(read);
				if (formatted != null && (source == null || DefaultReader.isLiteral(read))) {
					value = formatted;
				} else if (implicit != null && read != DefaultReader.class) {
					value = null;
				}
			}
			property.setDefaultValue(value);
		}
	}

	private String sourceDefault(final ApiType type, final ApiProperty property, final String suffix) {
		final List<ApiType> candidates = new ArrayList<>();
		final ApiType declaring = this.types.get(property.getSetters().get(0).getDeclaringType());
		if (declaring != null) {
			candidates.add(declaring);
		}
		candidates.add(type);
		candidates.addAll(TypePage.ancestors(type, this.types));
		for (final ApiType candidate : candidates) {
			for (final String field : Arrays.asList(property.getName(), property.getName() + "Supplier")) {
				final String value = DefaultReader.sourceValue(candidate, field, suffix);
				if (value != null) {
					return value;
				}
			}

			final String x = DefaultReader.sourceValue(candidate, property.getName() + "X", suffix);
			if (x != null && x.equals(DefaultReader.sourceValue(candidate, property.getName() + "Y", suffix))) {
				return x;
			}
		}
		return null;
	}

	private Object instance(final ApiType type) {
		if (type.getKind() != TypeKind.CLASS || type.isAbstractType()) {
			return null;
		}

		final AtomicReference<Object> result = new AtomicReference<>();
		final AtomicReference<String> reason = new AtomicReference<>("no public factory nor constructor");
		final Thread thread = new Thread(() -> {
			try {
				final Class<?> clazz = Class.forName(type.getPackageName() + "." + type.getName().replace('.', '$'));
				result.set(DefaultReader.create(clazz, 0, reason));
			} catch (final Throwable throwable) {
				reason.set(String.valueOf(throwable));
			}
		}, "docgen-defaults");
		thread.setDaemon(true);
		thread.start();
		try {
			thread.join(3000L);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (result.get() == null) {
			this.failures++;
			this.failed.put(type.getFqcn(), thread.isAlive() ? "timed out after 3 s" : reason.get());
		} else {
			this.instances++;
		}
		return result.get();
	}

	private static Object create(final Class<?> clazz, final int depth, final AtomicReference<String> reason) throws ReflectiveOperationException {
		for (final Executable creator : DefaultReader.creators(clazz)) {
			final Object[] arguments = new Object[creator.getParameterCount()];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = DefaultReader.argument(creator.getParameterTypes()[i], depth);
			}

			try {
				return creator instanceof Method ? ((Method) creator).invoke(null, arguments) : ((Constructor<?>) creator).newInstance(arguments);
			} catch (final InvocationTargetException exception) {
				if (depth == 0) {
					reason.set(creator.getName() + ": " + exception.getCause());
				}
			}
		}
		return null;
	}

	private static List<Executable> creators(final Class<?> clazz) {
		final List<Executable> factories = new ArrayList<>();
		final List<Executable> others = new ArrayList<>();
		for (final Method method : clazz.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass() == clazz && clazz.isAssignableFrom(method.getReturnType())) {
				("create".equals(method.getName()) ? factories : others).add(method);
			}
		}
		factories.sort(Comparator.comparingInt(Executable::getParameterCount));

		final List<Executable> constructors = new ArrayList<>(Arrays.asList(clazz.getConstructors()));
		constructors.sort(Comparator.comparingInt(Executable::getParameterCount));
		others.sort(Comparator.comparingInt(Executable::getParameterCount));

		final List<Executable> creators = new ArrayList<>(factories);
		creators.addAll(constructors);
		creators.addAll(others);
		return creators;
	}

	private static Object read(final Object instance, final ApiMember getter) {
		try {
			final Method method = instance.getClass().getMethod(getter.getName());
			method.setAccessible(true);
			return method.invoke(instance);
		} catch (final Throwable ignored) {
			return DefaultReader.class;
		}
	}

	private static Object argument(final Class<?> type, final int depth) throws ReflectiveOperationException {
		if (type == boolean.class) {
			return false;
		}

		if (type == char.class) {
			return ' ';
		}

		if (type == long.class) {
			return 0L;
		}

		if (type == float.class) {
			return 0F;
		}

		if (type == double.class) {
			return 0D;
		}

		if (type == short.class) {
			return (short) 0;
		}

		if (type == byte.class) {
			return (byte) 0;
		}

		if (type.isPrimitive()) {
			return 0;
		}

		if (type == String.class) {
			return "";
		}

		if (type.isEnum()) {
			return type.getEnumConstants()[0];
		}

		if (type.isArray()) {
			return Array.newInstance(type.getComponentType(), 0);
		}

		if (type.isInterface()) {
			return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, arguments) -> DefaultReader.neutral(method.getReturnType()));
		}

		if (depth >= 2 || Modifier.isAbstract(type.getModifiers()) || type.getName().startsWith("java.")) {
			return null;
		}
		return DefaultReader.create(type, depth + 1, new AtomicReference<>());
	}

	private static Object neutral(final Class<?> type) throws ReflectiveOperationException {
		return type.isPrimitive() && type != void.class ? DefaultReader.argument(type, 2) : null;
	}

	private static String sourceValue(final ApiType type, final String field, final String suffix) {
		return suffix.isEmpty() || !type.getSourceDefaults().containsKey(field) ? type.getSourceDefaults().get(field + suffix) : null;
	}

	private static boolean isLiteral(final Object value) {
		return value == null || value instanceof Number || value instanceof Boolean || value instanceof String || value instanceof Character || value instanceof Enum;
	}

	private static String format(final Object value) {
		if (value == DefaultReader.class) {
			return null;
		}

		if (value == null) {
			return "null";
		}

		if (value instanceof Double || value instanceof Float) {
			final double number = ((Number) value).doubleValue();
			final String suffix = value instanceof Double ? "D" : "F";
			return (number == Math.rint(number) && !Double.isInfinite(number) ? String.valueOf((long) number) : String.valueOf(number)) + suffix;
		}

		if (value instanceof Long) {
			return value + "L";
		}

		if (value instanceof Number || value instanceof Boolean) {
			return String.valueOf(value);
		}

		if (value instanceof String) {
			return new Gson().toJson(value);
		}

		if (value instanceof Enum) {
			final Class<?> declaring = ((Enum<?>) value).getDeclaringClass();
			return declaring.getSimpleName() + "." + ((Enum<?>) value).name();
		}
		return DefaultReader.constant(value);
	}

	private static String constant(final Object value) {
		for (Class<?> clazz = value.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (final Field field : clazz.getFields()) {
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && DefaultReader.isConstant(field, value)) {
					return field.getDeclaringClass().getSimpleName() + "." + field.getName();
				}
			}
		}
		return null;
	}

	private static boolean isConstant(final Field field, final Object value) {
		try {
			return field.get(null) == value;
		} catch (final Throwable ignored) {
			return false;
		}
	}

}