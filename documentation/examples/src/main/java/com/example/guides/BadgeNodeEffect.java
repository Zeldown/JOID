package com.example.guides;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;

// #region class
public class BadgeNodeEffect extends NodeEffect<Node> {

	private Supplier<Color>   color;
	private Supplier<Boolean> shown;

	protected BadgeNodeEffect(final Supplier<Color> color) {
		this.color = color;
		this.shown = () -> true;
	}

	public static BadgeNodeEffect create(final Color color) {
		return new BadgeNodeEffect(Signal.from(color));
	}
	// #endregion

	// #region draw
	@Override
	public void draw(final Node node, final Runnable body) {
		body.run();
		DrawUtils.SHAPE.drawCircle(node.getX() + node.getWidth() - 6D, node.getY() + 6D, this.color.get(), 12D);
	}
	// #endregion

	// #region apply
	@Override
	public boolean shouldApply(final Node node) {
		return this.shown.get();
	}
	// #endregion

	// #region setters
	public final <E extends BadgeNodeEffect> E color(final Color color) {
		return this.color(Signal.from(color));
	}

	public final <E extends BadgeNodeEffect> E color(final Supplier<Color> color) {
		this.color = color;
		return (E) this;
	}

	public final <E extends BadgeNodeEffect> E shown(final boolean shown) {
		return this.shown(Signal.from(shown));
	}

	public final <E extends BadgeNodeEffect> E shown(final Supplier<Boolean> shown) {
		this.shown = shown;
		return (E) this;
	}
	// #endregion

}