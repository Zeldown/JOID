package dev.joid.lib.ui.node.callback;

import java.lang.reflect.Method;
import java.util.Arrays;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class NodeCallbackObject<T extends NodeCallback> {

	private final T callback;
	private Method pre;
	private Method post;

	public NodeCallbackObject(final @NonNull T callback) {
		this.callback = callback;
		for (final Method method : callback.getClass().getMethods()) {
			final NodeCallbackMethod annotation = NodeCallbackObject.findAnnotation(method);
			if (annotation != null) {
				if (annotation.value() == NodeCallbackMethod.Type.PRE) {
					this.pre = method;
					this.pre.setAccessible(true);
				} else if (annotation.value() == NodeCallbackMethod.Type.POST) {
					this.post = method;
					this.post.setAccessible(true);
				}
			}
		}

		if (this.pre == null && this.post == null) {
			throw new IllegalArgumentException("Callback must have at least one method annotated with @NodeCallbackMethod in " + callback.getClass().getName());
		}
	}

	public void post(final Node node, final @NonNull InternalContext context, final Object... args) {
		if (this.post == null) {
			return;
		}

		final Object[] arguments = new Object[args.length + 2];
		arguments[0] = node;
		arguments[1] = context;
		System.arraycopy(args, 0, arguments, 2, args.length);
		try {
			this.post.invoke(this.callback, arguments);
		} catch (final Exception e) {
			System.out.println("Failed to invoke post method for callback " + this.callback.getClass().getName());
			final String[] paramTypes = new String[this.post.getParameterTypes().length];
			for (int i = 0; i < this.post.getParameterTypes().length; i++) {
				paramTypes[i] = this.post.getParameterTypes()[i].getName();
			}
			System.out.println("Method: " + String.join(", ", paramTypes));
			final String[] paramValues = new String[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				paramValues[i] = arguments[i] == null ? "null" : arguments[i].getClass().getSimpleName();
			}
			System.out.println("Values: " + String.join(", ", paramValues));
			e.printStackTrace();
		}
	}

	public void pre(final @NonNull Node node, final @NonNull InternalContext context, final Object... args) {
		if (this.pre == null) {
			return;
		}

		final Object[] arguments = new Object[args.length + 2];
		arguments[0] = node;
		arguments[1] = context;
		System.arraycopy(args, 0, arguments, 2, args.length);
		try {
			this.pre.invoke(this.callback, arguments);
		} catch (final Exception e) {
			System.out.println("Failed to invoke pre method for callback " + this.callback.getClass().getName());
			final String[] paramTypes = new String[this.pre.getParameterTypes().length];
			for (int i = 0; i < this.pre.getParameterTypes().length; i++) {
				paramTypes[i] = this.pre.getParameterTypes()[i].getSimpleName();
			}
			System.out.println("Method: " + String.join(", ", paramTypes));
			final String[] paramValues = new String[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				paramValues[i] = arguments[i] == null ? "null" : arguments[i].getClass().getSimpleName();
			}
			System.out.println("Values: " + String.join(", ", paramValues));
			e.printStackTrace();
		}
	}

	private static NodeCallbackMethod findAnnotation(final Method method) {
		if (method.isAnnotationPresent(NodeCallbackMethod.class)) {
			return method.getAnnotation(NodeCallbackMethod.class);
		}

		for (Class<?> type = method.getDeclaringClass(); type != null; type = type.getSuperclass()) {
			for (final Class<?> parent : type.getInterfaces()) {
				for (final Method declared : parent.getMethods()) {
					if (declared.isAnnotationPresent(NodeCallbackMethod.class) && declared.getName().equals(method.getName()) && Arrays.equals(declared.getParameterTypes(), method.getParameterTypes())) {
						return declared.getAnnotation(NodeCallbackMethod.class);
					}
				}
			}
		}

		return null;
	}

}