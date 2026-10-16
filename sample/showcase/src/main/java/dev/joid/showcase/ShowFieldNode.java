package dev.joid.showcase;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

public class ShowFieldNode extends TextFieldNode {

	private static final Color FILL         = Color.decode("#151327").lock();
	private static final Color FILL_FOCUSED = Color.decode("#1D1838").lock();

	private final Color         accent;
	private final TweenAnimator focus;

	protected ShowFieldNode(final double x, final double y, final double width, final Color accent) {
		super(x, y, width, 68);
		this.accent = accent;
		this.focus  = TweenAnimator.create(0F);
		super.onFocus(event -> this.focus.sequence(250F, 1F, TweenEquations.QUAD_OUT).start());
		super.onBlur(event -> this.focus.sequence(250F, 0F, TweenEquations.QUAD_OUT).start());
		super.background(ContainerNode.create(0, 0, 0, 0).layer((node, mouseX, mouseY) -> {
			final float f = this.focus.get();
			if (f > 0.01F) {
				DrawUtils.SHAPE.drawShadow(0D, 0D, node.getWidth(), node.getHeight(), this.accent.withAlpha(0.55F * f), 18F, 22F);
			}
			DrawUtils.SHAPE.drawRoundedRect(0D, 0D, node.getWidth(), node.getHeight(), ShowFieldNode.FILL.to(ShowFieldNode.FILL_FOCUSED, f), 18F);
			DrawUtils.SHAPE.drawRoundedBorder(0D, 0D, node.getWidth(), node.getHeight(), Color.WHITE.withAlpha(0.14F).to(this.accent, f), 18F, 2D);
		}));
	}

	public static ShowFieldNode create(final double x, final double y, final double width, final Color accent) {
		return new ShowFieldNode(x, y, width, accent);
	}

}