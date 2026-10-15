package dev.joid.lib.ui.node.impl.structure.toggle;

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
public abstract class ToggleNode<F, S> extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private static final Map<Class<?>, Boolean> BOOLEAN_MAP = new ConcurrentHashMap<>();

	@Getter(AccessLevel.NONE)
	private final transient InteractiveValue<Boolean> toggleValue;

	private Node knob;
	private Node track;

	private boolean           toggle;
	private ToggleState<F, S> state;

	protected ToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.toggleValue = InteractiveValue.create(super.getBindings(), "toggle", () -> this.toggle, (final Boolean toggle) -> this.toggle = toggle)
		.events(ToggleNode.BEFORE_CHANGE, (previous, toggle) -> NodeChangeEvent.Pre.create(this, previous, toggle), ToggleNode.CHANGE, (previous, toggle) -> NodeChangeEvent.Post.create(this, previous, toggle));

		super.<ToggleNode<F, S>>require("knob", "give it one with knob(...)", node -> node.knob != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.track != null) {
			this.track.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		this.knob.fit(super.getWidth() / 2D, super.getHeight());
		this.knob.layoutPosition(this.toggle ? super.getWidth() - this.knob.getWidth() : 0D, (super.getHeight() - this.knob.getHeight()) / 2D);
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

		if (!ToggleNode.isBoolean(this.getClass())) {
			throw new IllegalStateException("The " + this.getClass().getSimpleName() + " has no state, call state(toggle, back) before getValue()");
		}

		return (T) Boolean.valueOf(this.toggle);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T track(final @NonNull Node track) {
		this.track = super.part(this.track, track);
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T knob(final @NonNull Node knob) {
		this.knob = super.part(this.knob, knob);
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T state(final F toggle, final S back) {
		this.state = new ToggleState<>(toggle, back);
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final boolean toggle) {
		return this.toggleValue.start(toggle);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final @NonNull Signal<Boolean> toggle) {
		return this.toggleValue.bind(toggle);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T change(final boolean toggle) {
		this.toggleValue.change(toggle);
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.toggleValue.before(listener);
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.toggleValue.on(listener);
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