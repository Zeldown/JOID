package dev.joid.base.opengl.binding;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlFakes {

	public static <T> @NonNull T fake(final @NonNull Class<T> type, final @NonNull Answer answer) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
			final Object value = answer.answer(method, args == null ? new Object[0] : args);
			if (value != null || !method.getReturnType().isPrimitive()) {
				return value;
			}
			return method.getReturnType() == boolean.class ? (Object) false : method.getReturnType() == long.class ? (Object) 0L : method.getReturnType() == void.class ? null : (Object) 0;
		});
	}

	@FunctionalInterface
	public interface Answer {

		public Object answer(final @NonNull Method method, final @NonNull Object[] args);

	}

}