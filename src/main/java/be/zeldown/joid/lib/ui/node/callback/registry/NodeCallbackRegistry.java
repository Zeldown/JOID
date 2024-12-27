package be.zeldown.joid.lib.ui.node.callback.registry;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackMethod;
import be.zeldown.joid.lib.utils.context.InternalContext;

public class NodeCallbackRegistry {

	private static final Map<Integer, Class<? extends NodeCallback>> REGISTRY = new HashMap<>();

	private static int lastId = 0;

	public static int next(final Class<? extends NodeCallback> clazz) {
		if (!clazz.isAnnotationPresent(FunctionalInterface.class)) {
			throw new IllegalArgumentException(clazz + " must be a functional interface");
		}

		boolean foundPre = false;
		boolean foundPost = false;
		for (final Method method : clazz.getDeclaredMethods()) {
			if (NodeCallbackRegistry.validate(method, NodeCallbackMethod.Type.PRE)) {
				foundPre = true;
			} else if (NodeCallbackRegistry.validate(method, NodeCallbackMethod.Type.POST)) {
				foundPost = true;
			}
		}

		if (!foundPre || !foundPost) {
			throw new IllegalArgumentException(clazz + " must have a PRE and POST method annotated with @NodeCallbackMethod");
		}

		final int id = NodeCallbackRegistry.lastId++;
		NodeCallbackRegistry.REGISTRY.put(id, clazz);
		return id;
	}

	public static Class<? extends NodeCallback> get(final int id) {
		return NodeCallbackRegistry.REGISTRY.get(id);
	}

	public static int getId(final Class<? extends NodeCallback> clazz) {
		for (final Map.Entry<Integer, Class<? extends NodeCallback>> entry : NodeCallbackRegistry.REGISTRY.entrySet()) {
			if (entry.getValue() == clazz) {
				return entry.getKey();
			}
		}

		return -1;
	}

	private static boolean validate(final Method method, final NodeCallbackMethod.Type type) {
		if (method.isAnnotationPresent(NodeCallbackMethod.class)) {
			final NodeCallbackMethod annotation = method.getAnnotation(NodeCallbackMethod.class);
			if (annotation.value() == type) {
				if (method.getReturnType() != Void.TYPE) {
					throw new IllegalArgumentException(method.getDeclaringClass() + " " + type + " method must return a boolean");
				}

				if (method.getParameterCount() < 2) {
					throw new IllegalArgumentException(method.getDeclaringClass() + " " + type + " method must have at least two parameters");
				}

				if (!Node.class.isAssignableFrom(method.getParameterTypes()[0])) {
					throw new IllegalArgumentException(method.getDeclaringClass() + " " + type + " method first parameter must be a Node instance but is " + method.getParameterTypes()[0]);
				}

				if (InternalContext.class != method.getParameterTypes()[1]) {
					throw new IllegalArgumentException(method.getDeclaringClass() + " " + type + " method second parameter must be an InternalContext instance but is " + method.getParameterTypes()[1]);
				}

				return true;
			}
		}

		return false;
	}

}