package dev.joid.lib.ui.node.impl.structure.checkbox;


import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class CheckboxNode extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private boolean checked;

	protected CheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.change(!this.checked);
		event.consume();
	}

	public final <T extends CheckboxNode> @NonNull T checked(final boolean checked) {
		return super.start("checked", checked, value -> this.change(value, false));
	}

	public final <T extends CheckboxNode> @NonNull T checked(final @NonNull Signal<Boolean> checked) {
		return super.bind("checked", checked, value -> this.change(value, false));
	}

	public final <T extends CheckboxNode> @NonNull T change(final boolean checked) {
		this.change(checked, true);
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return super.listen(CheckboxNode.BEFORE_CHANGE, listener);
	}

	public final <T extends CheckboxNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return super.listen(CheckboxNode.CHANGE, listener);
	}

	private void change(final boolean checked, final boolean refusable) {
		if (this.checked == checked) {
			return;
		}

		super.change(CheckboxNode.BEFORE_CHANGE, CheckboxNode.CHANGE, this.checked, checked, refusable, () -> {
			this.checked = checked;
			super.sync("checked", checked);
		});
	}

}