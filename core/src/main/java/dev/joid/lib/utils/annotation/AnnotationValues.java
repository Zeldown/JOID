package dev.joid.lib.utils.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnnotationValues {

	public static <T> @NonNull T read(final Annotation annotation, final @NonNull T target) {
		return AnnotationValues.merge(target, null, annotation, true);
	}

	public static <T> @NonNull T merge(final @NonNull T target, final Annotation previous, final Annotation next) {
		return AnnotationValues.merge(target, previous, next, false);
	}

	private static <T> T merge(final T target, final Annotation previous, final Annotation next, final boolean all) {
		final Annotation annotation = next != null ? next : previous;
		if (annotation == null) {
			return target;
		}

		for (final Method attribute : annotation.annotationType().getDeclaredMethods()) {
			final Object before = AnnotationValues.value(attribute, previous);
			final Object after = AnnotationValues.value(attribute, next);
			if (after instanceof Annotation) {
				AnnotationValues.merge(AnnotationValues.invoke(AnnotationValues.find(target, attribute.getName()), target), (Annotation) before, (Annotation) after, all);
			} else if (all || !Objects.deepEquals(before, after)) {
				AnnotationValues.invoke(AnnotationValues.find(target, attribute.getName(), attribute.getReturnType()), target, after);
			}
		}
		return target;
	}

	private static Object value(final Method attribute, final Annotation annotation) {
		return annotation == null ? attribute.getDefaultValue() : AnnotationValues.invoke(attribute, annotation);
	}

	private static Method find(final Object target, final String name, final Class<?>... types) {
		try {
			return target.getClass().getMethod(name, types);
		} catch (final NoSuchMethodException exception) {
			throw new IllegalStateException("The class " + target.getClass().getName() + " has no public method " + name + "(...) for the annotation value " + name + ", add it", exception);
		}
	}

	private static Object invoke(final Method method, final Object target, final Object... arguments) {
		try {
			return method.invoke(target, arguments);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException("The method " + method.getName() + " of " + target.getClass().getName() + " failed for an annotation value (" + exception + ")", exception);
		}
	}

}