package dev.joid.showcase;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tweenengine.TweenEquation;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ShowMotion extends ShowUI {

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-250, -250, 900, ShowUI.VIOLET.copyAlpha(0.38F), 9D, 0D, 50D);
		this.blob(1300, 500, 900, ShowUI.CYAN.copyAlpha(0.20F), 9D, 0.5D, 50D);

		this.glass(110, 100, 1700, 880, 32F).attach(this);
		TextNode.create(156, 144).text(Text.create("Motion", ShowUI.font(FontWeight.EXTRA_BOLD, 44F, ShowUI.TEXT))).attach(this);
		TextNode.create(158, 206).text(Text.create("Every property can ease: position, size, color, rotation.", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);

		this.row(0, "Linear", "LINEAR", TweenEquations.LINEAR, ShowUI.SKY, ShowUI.CYAN);
		this.row(1, "Smooth", "QUAD_INOUT", TweenEquations.QUAD_INOUT, ShowUI.VIOLET, ShowUI.SKY);
		this.row(2, "Overshoot", "BACK_OUT", TweenEquations.BACK_OUT, ShowUI.FUCHSIA, ShowUI.VIOLET);
		this.row(3, "Elastic", "ELASTIC_OUT", TweenEquations.ELASTIC_OUT, ShowUI.PINK, ShowUI.FUCHSIA);
		this.row(4, "Bounce", "BOUNCE_OUT", TweenEquations.BOUNCE_OUT, ShowUI.AMBER, ShowUI.ORANGE);
		this.row(5, "Expo", "EXPO_INOUT", TweenEquations.EXPO_INOUT, ShowUI.EMERALD, ShowUI.CYAN);
	}

	private void row(final int index, final String name, final String constant, final TweenEquation equation, final Color from, final Color to) {
		final double y = 300D + index * 110D;
		final TweenAnimator eased = TweenAnimator.create(0F).sequence(1500F, 1F, equation);
		eased.getTimeline().delay(300F + index * 90F).repeatYoyo(-1, 700F);
		eased.start();
		final TweenAnimator clock = TweenAnimator.create(0F).sequence(1500F, 1F);
		clock.getTimeline().delay(300F + index * 90F).repeatYoyo(-1, 700F);
		clock.start();

		TextNode.create(160, y - 16D).text(Text.create(name, ShowUI.font(FontWeight.SEMI_BOLD, 26F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(this);
		TextNode.create(160, y + 18D).text(Text.create(constant, ShowUI.font(FontWeight.MEDIUM, 15F, ShowUI.FAINT).letterSpacing(0.1F))).anchorY(Align.CENTER).attach(this);

		RectNode
		.create(420, y - 42D, 150, 84)
		.color(Color.BLACK.copyAlpha(0.22F))
		.effect(RoundedNodeEffect.create(14F))
		.layer((mouseX, mouseY) -> this.curve(434D, y - 30D, 122D, 60D, equation, clock.getValue(), from, to))
		.animate(clock)
		.attach(this);

		RectNode.create(640, y - 4D, 856, 8).color(Color.WHITE.copyAlpha(0.08F)).effect(RoundedNodeEffect.create(4F)).attach(this);
		RectNode
		.create(640, y - 4D, 0, 8)
		.color(from.copyAlpha(0F).toGradient(to))
		.<RectNode>width(() -> Math.max(0D, 28D + 800D * eased.getValue()))
		.effect(RoundedNodeEffect.create(4F))
		.attach(this);
		RectNode
		.create(640, y - 28D, 56, 56)
		.color(ShowUI.diagonal(from, to))
		.<RectNode>x(() -> 640D + 800D * eased.getValue())
		.effect(CircleNodeEffect.create())
		.effect(ShadowNodeEffect.create(to.copyAlpha(0.9F), 26F))
		.animate(eased)
		.attach(this);
	}

	private void curve(final double x, final double y, final double width, final double height, final TweenEquation equation, final float time, final Color from, final Color to) {
		final List<Vector2d> points = new ArrayList<>();
		for (int i = 0; i <= 60; i++) {
			final float t = i / 60F;
			points.add(new Vector2d(x + t * width, y + height - equation.compute(t) * height));
		}
		DrawUtils.SHAPE.drawLine(Color.WHITE.copyAlpha(0.10F), 2F, new Vector2d(x, y + height), new Vector2d(x + width, y + height));
		DrawUtils.SHAPE.drawLine(from.toGradient(to), 3F, points.toArray(new Vector2d[0]));
		DrawUtils.SHAPE.drawCircle(x + time * width, y + height - equation.compute(time) * height, Color.WHITE, 6D);
	}

}