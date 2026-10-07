package dev.joid.demo.ui.animation;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.render.modifier.Rotation;
import dev.joid.lib.render.modifier.Vector;
import dev.joid.lib.render.transform.operation.RotateOperation;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoAnimation extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoAnimation.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal plays = IntegerSignal.of(0);
		final TweenAnimator linear = TweenAnimator.create(0F).sequence(1000F, 1F);
		linear.getTimeline().repeatYoyo(-1, 0F);
		linear.start();
		final TweenAnimator quad = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.QUAD_INOUT);
		quad.getTimeline().repeatYoyo(-1, 0F);
		quad.start();
		final TweenAnimator bounce = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.BOUNCE_OUT);
		bounce.getTimeline().repeatYoyo(-1, 0F);
		bounce.start();
		final TweenAnimator elastic = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.ELASTIC_OUT);
		elastic.getTimeline().repeatYoyo(-1, 0F);
		elastic.start();
		final TweenAnimator repeat = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.CUBIC_OUT);
		repeat.getTimeline().repeat(-1, 0F);
		repeat.start();
		final TweenAnimator delay = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.CUBIC_INOUT);
		delay.getTimeline().repeatYoyo(-1, 500F);
		delay.start();
		final TweenAnimator sequence = TweenAnimator.create(0F).sequence(600F, 1F, TweenEquations.QUAD_OUT).push(600F, 0.3F, TweenEquations.QUAD_IN);
		sequence.getTimeline().repeat(-1, 300F);
		sequence.start();
		final TweenAnimator slow = TweenAnimator.create(0F).sequence(1000F, 1F);
		slow.getTimeline().repeatYoyo(-1, 0F);
		slow.start();
		slow.setSpeed(0.5F);
		final TweenAnimator color = TweenAnimator.create(0F).sequence(1000F, 1F);
		color.getTimeline().repeatYoyo(-1, 0F);
		color.start();
		final TweenAnimator size = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.SINE_INOUT);
		size.getTimeline().repeatYoyo(-1, 0F);
		size.start();
		final TweenAnimator rotation = TweenAnimator.create(0F).sequence(2000F, 1F);
		rotation.getTimeline().repeat(-1, 0F);
		rotation.start();
		final TweenAnimator once = TweenAnimator.create(0F);

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(linear).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Linear", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(quad).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Quad in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(bounce).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Bounce out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(elastic).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Elastic out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(repeat).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Repeat", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(delay).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Yoyo with delay", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(sequence).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Sequence", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(slow).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Half speed", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(140, 70, 120, 120).color(() -> UIDemoAnimation.INK.to(Color.WHITE, color.getValue())).animate(color).attach(rect);
			TextNode.create(200, 275).text(Text.create("Color", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 100, 60, 60).color(UIDemoAnimation.INK).width(() -> 60D + 260D * size.getValue()).animate(size).attach(rect);
			TextNode.create(200, 275).text(Text.create("Size", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(150, 80, 100, 100)
			.color(UIDemoAnimation.INK)
			.self(square -> square.effect(TransformNodeEffect.create(new RotateOperation(() -> rotation.getValue() * 360D, Rotation.ROLL, Vector.create(() -> square.getX() + 50D, () -> square.getY() + 50D)))))
			.animate(rotation)
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Rotation", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 60, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(once).attach(line);
			})
			.attach(rect);
			RectNode
			.create(40, 150, 120, 50)
			.color(UIDemoAnimation.INK)
			.onClick((node, mouseX, mouseY, clickType) -> once.sequence(1000F, once.getValue() < 0.5F ? 1F : 0F, TweenEquations.CUBIC_INOUT).setCallback(tween -> plays.increment()).start())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Play", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(190, 162).text(Text.create("Ends: " + plays.get(), caption)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Start on click", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}