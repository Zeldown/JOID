package com.example.further;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;

// #region class
public class SwatchNode extends Node {

	private static final EventType<NodeChangeEvent.Pre<?, Boolean>>  BEFORE_SELECT = EventType.create("beforeSelect");
	private static final EventType<NodeChangeEvent.Post<?, Boolean>> SELECT        = EventType.create("onSelect");
	private static final EventType<NodeEvent<SwatchNode>>            RESET         = EventType.create("onReset");

	// #region transient
	private final transient InteractiveValue<Boolean> selectedValue;
	// #endregion

	private Color   color;
	private boolean selected;

	protected SwatchNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.color = Color.WHITE;
		this.selectedValue = InteractiveValue
		.create(super.getBindings(), "selected", () -> this.selected, value -> this.selected = value)
		.events(
			SwatchNode.BEFORE_SELECT, (previous, next) -> NodeChangeEvent.Pre.create(this, previous, next),
			SwatchNode.SELECT, (previous, next) -> NodeChangeEvent.Post.create(this, previous, next)
		);
	}

	public static SwatchNode create(final double x, final double y, final double size) {
		return new SwatchNode(x, y, size, size);
	}
	// #endregion

	// #region draw
	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.w(), super.h(), this.color);
		if (this.selected) {
			DrawUtils.SHAPE.drawBorder(super.getX(), super.getY(), super.w(), super.h(), Color.WHITE, 8D);
		}
	}
	// #endregion

	// #region input
	@Override
	public void mousePressed(final NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.selectedValue.set(!this.selectedValue.get());
		event.consume();
	}
	// #endregion

	// #region color
	public final <T extends SwatchNode> T color(final Color color) {
		return this.color(Signal.from(color));
	}

	public final <T extends SwatchNode> T color(final Supplier<Color> color) {
		return super.follow("color", color, value -> this.color = value);
	}
	// #endregion

	// #region selected
	public final <T extends SwatchNode> T selected(final boolean selected) {
		return this.selectedValue.start(selected);
	}

	public final <T extends SwatchNode> T selected(final Signal<Boolean> selected) {
		return this.selectedValue.bind(selected);
	}

	public final <T extends SwatchNode> T change(final boolean selected) {
		this.selectedValue.change(selected);
		return (T) this;
	}

	public final <T extends SwatchNode> T beforeSelect(final EventListener<NodeChangeEvent.Pre<T, Boolean>> listener) {
		return this.selectedValue.before(listener);
	}

	public final <T extends SwatchNode> T onSelect(final EventListener<NodeChangeEvent.Post<T, Boolean>> listener) {
		return this.selectedValue.on(listener);
	}
	// #endregion

	// #region reset
	public final <T extends SwatchNode> T reset() {
		this.selectedValue.change(false);
		super.fire(SwatchNode.RESET, () -> NodeEvent.create(this));
		return (T) this;
	}

	public final <T extends SwatchNode> T onReset(final EventListener<NodeEvent<T>> listener) {
		return super.listen(SwatchNode.RESET, listener);
	}
	// #endregion

	// #region getters
	public Color getColor() {
		return this.color;
	}

	public boolean isSelected() {
		return this.selected;
	}
	// #endregion

}