package dev.joid.showcase;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ShowPaperFieldNode extends TextFieldNode {

	private final TweenAnimator focus;

	protected ShowPaperFieldNode(final double x, final double y, final double width) {
		super(x, y, width, 68);
		this.focus = TweenAnimator.create(0F);
		super.<ShowPaperFieldNode>onFocus(event -> event.getNode().focus.sequence(250F, 1F, TweenEquations.QUAD_OUT).start());
		super.<ShowPaperFieldNode>onBlur(event -> event.getNode().focus.sequence(250F, 0F, TweenEquations.QUAD_OUT).start());
		super.background(ContainerNode.create(0, 0, 0, 0).body(background -> {
			RectNode
			.create(8, 8, 0, 0)
			.color(() -> ShowPaperKit.INK.to(ShowPaperKit.TOMATO, this.focus.get()))
			.<RectNode>width(background::getWidth)
			.<RectNode>height(background::getHeight)
			.attach(background);
			RectNode
			.create(0, 0, 0, 0)
			.color(Color.WHITE)
			.<RectNode>width(background::getWidth)
			.<RectNode>height(background::getHeight)
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.attach(background);
		}));
	}

	public static ShowPaperFieldNode create(final double x, final double y, final double width) {
		return new ShowPaperFieldNode(x, y, width);
	}

}