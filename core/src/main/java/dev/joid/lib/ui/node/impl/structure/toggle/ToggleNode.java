package dev.joid.lib.ui.node.impl.structure.toggle;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class ToggleNode<F, S> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private static final Map<Class<?>, Boolean> BOOLEAN_MAP = new ConcurrentHashMap<>();

	private boolean           toggle;
	private ToggleState<F, S> state;

	protected ToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.change(!this.toggle);
		event.consume();
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final boolean toggle) {
		return super.bind("toggle", toggle, (final ToggleNode<F, S> node, final Boolean value) -> node.change(value, false));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final @NonNull Signal<Boolean> toggle) {
		return super.bind("toggle", toggle, (final ToggleNode<F, S> node, final Boolean value) -> node.change(value, false));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T state(final F toggle, final S back) {
		this.state = new ToggleState<>(toggle, back);
		return (T) this;
	}

	public final <T> @NonNull T getValue() {
		if (this.state != null) {
			return (T) (this.toggle ? this.state.getToggle() : this.state.getBack());
		}

		if (!ToggleNode.isBoolean(this.getClass())) {
			throw new IllegalStateException("The " + this.getClass().getSimpleName() + " has no state, call state(toggle, back) before getValue()");
		}

		return (T) Boolean.valueOf(this.toggle);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T change(final boolean toggle) {
		this.change(toggle, true);
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.beforeChange(EventPriority.NORMAL, listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T beforeChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return super.listen(ToggleNode.BEFORE_CHANGE, priority, listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.onChange(EventPriority.NORMAL, listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return super.listen(ToggleNode.CHANGE, priority, listener);
	}

	private void change(final boolean toggle, final boolean refusable) {
		if (this.toggle == toggle) {
			return;
		}

		super.change(ToggleNode.BEFORE_CHANGE, ToggleNode.CHANGE, this.toggle, toggle, refusable, () -> {
			this.toggle = toggle;
			super.sync("toggle", toggle);
		});
	}

	private static boolean isBoolean(final @NonNull Class<?> clazz) {
		return ToggleNode.BOOLEAN_MAP.computeIfAbsent(clazz, type -> {
			final Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
			Class<?> current = type;
			while (current != ToggleNode.class) {
				final Type superType = current.getGenericSuperclass();
				final Class<?> superClass = current.getSuperclass();
				if (superType instanceof ParameterizedType) {
					final Type[] arguments = ((ParameterizedType) superType).getActualTypeArguments();
					final TypeVariable<?>[] parameters = superClass.getTypeParameters();
					for (int i = 0; i < arguments.length; i++) {
						typeMap.put(parameters[i], typeMap.getOrDefault(arguments[i], arguments[i]));
					}
				}

				current = superClass;
			}

			final TypeVariable<?>[] parameters = ToggleNode.class.getTypeParameters();
			return typeMap.get(parameters[0]) == Boolean.class && typeMap.get(parameters[1]) == Boolean.class;
		});
	}

}