package dev.joid.tool.docgen.source;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

	private int instances;
	private int failures;

	private DefaultReader(final Map<String, ApiType> types, final boolean runtime) {
		this.types   = types;
		this.runtime = runtime;
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
			final String source = this.sourceDefault(page.getType(), property);
			String value = source;
			if (instance != null && property.getGetter() != null && !"@parameter".equals(source)) {
				final Object read = DefaultReader.read(instance, property.getGetter());
				final String formatted = DefaultReader.format(read);
				if (formatted != null && (source == null || DefaultReader.isLiteral(read))) {
					value = formatted;
				}
			}
			property.setDefaultValue(value);
		}
	}

	private String sourceDefault(final ApiType type, final ApiProperty property) {
		final List<ApiType> candidates = new ArrayList<>();
		final ApiType declaring = this.types.get(property.getSetters().get(0).getDeclaringType());
		if (declaring != null) {
			candidates.add(declaring);
		}
		candidates.add(type);
		candidates.addAll(TypePage.ancestors(type, this.types));
		for (final ApiType candidate : candidates) {
			for (final String field : Arrays.asList(property.getName(), property.getName() + "Supplier")) {
				final String value = candidate.getSourceDefaults().get(field);
				if (value != null) {
					return value;
				}
			}
		}
		return null;
	}

	private Object instance(final ApiType type) {
		if (type.getKind() != TypeKind.CLASS || type.isAbstractType()) {
			return null;
		}

		final AtomicReference<Object> result = new AtomicReference<>();
		final Thread thread = new Thread(() -> {
			try {
				final Class<?> clazz = Class.forName(type.getPackageName() + "." + type.getName().replace('.', '$'));
				Method factory = null;
				for (final Method method : clazz.getMethods()) {
					if ("create".equals(method.getName()) && Modifier.isStatic(method.getModifiers()) && clazz.isAssignableFrom(method.getReturnType()) && (factory == null || method.getParameterCount() < factory.getParameterCount())) {
						factory = method;
					}
				}

				if (factory != null) {
					final Object[] arguments = new Object[factory.getParameterCount()];
					for (int i = 0; i < arguments.length; i++) {
						arguments[i] = DefaultReader.argument(factory.getParameterTypes()[i]);
					}
					result.set(factory.invoke(null, arguments));
				}
			} catch (final Throwable ignored) {
				result.set(null);
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
		} else {
			this.instances++;
		}
		return result.get();
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

	private static Object argument(final Class<?> type) {
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
		return type.isArray() ? Array.newInstance(type.getComponentType(), 0) : null;
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
		return null;
	}

}