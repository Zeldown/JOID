package dev.joid.showcase;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import lombok.NonNull;

public class ShowFieldNode extends TextFieldNode {

	private static final Color FILL         = Color.decode("#151327").lock();
	private static final Color FILL_FOCUSED = Color.decode("#1D1838").lock();

	private final Color         accent;
	private final TweenAnimator focus;

	protected ShowFieldNode(final double x, final double y, final double width, final Color accent) {
		super(x, y, width, 68);
		this.accent = accent;
		this.focus  = TweenAnimator.create(0F);
		super.<ShowFieldNode>onFocus(event -> event.getNode().focus.sequence(250F, 1F, TweenEquations.QUAD_OUT).start());
		super.<ShowFieldNode>onBlur(event -> event.getNode().focus.sequence(250F, 0F, TweenEquations.QUAD_OUT).start());
		super.background(RectNode
		.create(0, 0, 0, 0)
		.color(() -> ShowFieldNode.FILL.to(ShowFieldNode.FILL_FOCUSED, this.focus.get()))
		.effect(RoundedNodeEffect.create(18F))
		.effect(ShadowNodeEffect.create(this.accent, 22F).color(() -> this.accent.withAlpha(0.55F * this.focus.get())))
		.effect(BorderNodeEffect.create(this.accent, 2F).mode(BorderMode.IN).color(() -> Color.WHITE.withAlpha(0.14F).to(this.accent, this.focus.get()))));
	}

	public static @NonNull ShowFieldNode create(final double x, final double y, final double width, final @NonNull Color accent) {
		return new ShowFieldNode(x, y, width, accent);
	}

}