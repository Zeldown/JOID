package dev.joid.lib.ui.node.impl.structure.toggle;

import java.util.function.Supplier;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.ISignalSubscriber;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.toggle.callback.NodeToggleChangeCallback;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class ToggleNode<F, S> extends Node {

	public static final int CALLBACK_CHANGE = NodeCallbackRegistry.next(NodeToggleChangeCallback.class);

	private boolean                    toggle;
	private Signal<Boolean>            signal;
	private ToggleState<F, S>          state;
	private ISignalSubscriber<Boolean> subscription;

	protected ToggleNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (context.isCancelled() || !super.isHovered(mouseX, mouseY)) {
			return;
		}

		context.cancel(() -> this.change(!this.toggle, context));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final boolean toggle) {
		return this.toggle(Signal.from(toggle));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T toggle(final @NonNull Supplier<Boolean> toggle) {
		return super.follow("toggle", toggle, value -> this.change(value, DispatchContext.create()));
	}

	public final <T extends ToggleNode<F, S>> @NonNull T state(final F toggle, final S back) {
		this.state = new ToggleState<>(toggle, back);
		return (T) this;
	}

	public final <T> @NonNull T getValue() {
		return (T) (this.toggle ? this.state.getToggle() : this.state.getBack());
	}

	public final <T extends ToggleNode<F, S>> @NonNull T signal(final @NonNull Signal<Boolean> signal) {
		this.signal = super.writable(signal);
		this.subscription = super.rebind(this.subscription, signal, value -> this.change(value, DispatchContext.create()));
		return (T) this;
	}

	public final <T extends ToggleNode<F, S>> @NonNull T onChange(final @NonNull NodeToggleChangeCallback<T, F, S> callback) {
		super.registerCallback(ToggleNode.CALLBACK_CHANGE, callback);
		return (T) this;
	}

	private void change(final boolean toggle, final DispatchContext context) {
		if (this.toggle == toggle) {
			return;
		}

		super.executeCallback(ToggleNode.CALLBACK_CHANGE, context, () -> {
			this.toggle = toggle;
			super.sync(this.signal, toggle);
		}, toggle);
	}

}