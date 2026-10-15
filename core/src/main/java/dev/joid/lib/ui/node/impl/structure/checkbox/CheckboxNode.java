package dev.joid.lib.ui.node.impl.structure.checkbox;

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
public abstract class CheckboxNode extends Node {

	private static final EventType<NodeChangeEvent.Post<?, Boolean>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_CHANGE = EventType.create("beforeChange");

	@Getter(AccessLevel.NONE)
	private final transient InteractiveValue<Boolean> checkedValue;

	private Node box;
	private Node mark;

	private boolean checked;

	protected CheckboxNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.checkedValue = InteractiveValue.create(super.getBindings(), "checked", () -> this.checked, (final Boolean checked) -> this.checked = checked)
		.events(CheckboxNode.BEFORE_CHANGE, (previous, checked) -> NodeChangeEvent.Pre.create(this, previous, checked), CheckboxNode.CHANGE, (previous, checked) -> NodeChangeEvent.Post.create(this, previous, checked));

		super.<CheckboxNode>require("mark", "give it one with mark(...)", node -> node.mark != null);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.box != null) {
			this.box.layoutPosition(0D, 0D).layoutSize(super.getWidth(), super.getHeight());
		}

		this.mark.fit(super.getWidth(), super.getHeight());
		this.mark.layoutPosition((super.getWidth() - this.mark.getWidth()) / 2D, (super.getHeight() - this.mark.getHeight()) / 2D);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.checkedValue.set(!this.checkedValue.get());
		event.consume();
	}

	public final <T extends CheckboxNode> @NonNull T box(final @NonNull Node box) {
		this.box = super.part(this.box, box);
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T mark(final @NonNull Node mark) {
		this.mark = super.part(this.mark, mark.visible(() -> this.checked));
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T checked(final boolean checked) {
		return this.checkedValue.start(checked);
	}

	public final <T extends CheckboxNode> @NonNull T checked(final @NonNull Signal<Boolean> checked) {
		return this.checkedValue.bind(checked);
	}

	public final <T extends CheckboxNode> @NonNull T change(final boolean checked) {
		this.checkedValue.change(checked);
		return (T) this;
	}

	public final <T extends CheckboxNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.checkedValue.before(listener);
	}

	public final <T extends CheckboxNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.checkedValue.on(listener);
	}

}