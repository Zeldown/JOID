package dev.joid.lib.ui.node.impl.control.toggle;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class BaseToggleNode<F, S> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private static final Map<Class<?>, Boolean> BOOLEAN_MAP = new ConcurrentHashMap<>();

	@Getter(AccessLevel.NONE)
	private final transient InteractiveValue<Boolean> toggleValue;

	private boolean           toggle;
	private ToggleState<F, S> state;

	protected BaseToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.toggleValue = InteractiveValue.create(super.getBindings(), "toggle", () -> this.toggle, (final Boolean toggle) -> this.toggle = toggle)
		.events(BaseToggleNode.BEFORE_CHANGE, (previous, toggle) -> NodeChangeEvent.Pre.create(this, previous, toggle), BaseToggleNode.CHANGE, (previous, toggle) -> NodeChangeEvent.Post.create(this, previous, toggle));
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.toggleValue.set(!this.toggleValue.get());
		event.consume();
	}

	public final <T> @NonNull T getValue() {
		if (this.state != null) {
			return (T) (this.toggle ? this.state.getToggle() : this.state.getBack());
		}

		if (!BaseToggleNode.isBoolean(this.getClass())) {
			throw new IllegalStateException("The " + this.getClass().getSimpleName() + " has no state, call state(toggle, back) before getValue()");
		}

		return (T) Boolean.valueOf(this.toggle);
	}

	public final <T extends BaseToggleNode<F, S>> @NonNull T state(final F toggle, final S back) {
		this.state = ToggleState.create(toggle, back);
		return (T) this;
	}

	public final <T extends BaseToggleNode<F, S>> @NonNull T toggle(final boolean toggle) {
		return this.toggleValue.start(toggle);
	}

	public final <T extends BaseToggleNode<F, S>> @NonNull T toggle(final @NonNull Signal<Boolean> toggle) {
		return this.toggleValue.bind(toggle);
	}

	public final <T extends BaseToggleNode<F, S>> @NonNull T change(final boolean toggle) {
		this.toggleValue.change(toggle);
		return (T) this;
	}

	public final <T extends BaseToggleNode<F, S>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.toggleValue.before(listener);
	}

	public final <T extends BaseToggleNode<F, S>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.toggleValue.on(listener);
	}

	private static boolean isBoolean(final @NonNull Class<?> clazz) {
		return BaseToggleNode.BOOLEAN_MAP.computeIfAbsent(clazz, type -> {
			final Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
			Class<?> current = type;
			while (current != BaseToggleNode.class) {
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

			final TypeVariable<?>[] parameters = BaseToggleNode.class.getTypeParameters();
			return typeMap.get(parameters[0]) == Boolean.class && typeMap.get(parameters[1]) == Boolean.class;
		});
	}

}