package dev.joid.demo.ui.animation;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoAnimation extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoAnimation.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final IntegerSignal plays = new IntegerSignal(0);
		final TweenAnimator[] lines = {
			TweenAnimator.create(0F).sequence(1000F, 1F).repeatYoyo(BaseTween.INFINITY, 0F).start(),
			TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.QUAD_INOUT).repeatYoyo(BaseTween.INFINITY, 0F).start(),
			TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.BOUNCE_OUT).repeatYoyo(BaseTween.INFINITY, 0F).start(),
			TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.ELASTIC_OUT).repeatYoyo(BaseTween.INFINITY, 0F).start(),
			TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.CUBIC_OUT).repeat(BaseTween.INFINITY, 0F).start(),
			TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.CUBIC_INOUT).repeatYoyo(BaseTween.INFINITY, 500F).start(),
			TweenAnimator.create(0F).sequence(600F, 1F, TweenEquations.QUAD_OUT).push(600F, 0.3F, TweenEquations.QUAD_IN).repeat(BaseTween.INFINITY, 300F).start(),
			TweenAnimator.create(0F).sequence(1000F, 1F).repeatYoyo(BaseTween.INFINITY, 0F).speed(0.5F).start()
		};
		final String[] names = {"Linear", "Quad in out", "Bounce out", "Elastic out", "Repeat", "Yoyo with delay", "Sequence", "Half speed"};
		final TweenAnimator color = TweenAnimator.create(0F).sequence(1000F, 1F).repeatYoyo(BaseTween.INFINITY, 0F).start();
		final TweenAnimator size = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.SINE_INOUT).repeatYoyo(BaseTween.INFINITY, 0F).start();
		final TweenAnimator rotation = TweenAnimator.create(0F).sequence(2000F, 1F).repeat(BaseTween.INFINITY, 0F).start();
		final TweenAnimator once = TweenAnimator.create(0F);

		for (int i = 0; i < lines.length; i++) {
			final int index = i;
			RectNode
			.create(100 + index % 4 * 440, 40 + index / 4 * 340, 400, 260)
			.color(UIDemoAnimation.PLACEHOLDER)
			.body(rect -> {
				RectNode
				.create(40, 100, 320, 60)
				.color(Color.WHITE)
				.body(line -> {
					RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(lines[index]).onDetach(event -> lines[index].clear()).attach(line);
				})
				.attach(rect);
				TextNode.create(200, 275).text(Text.create(names[index], caption)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(140, 70, 120, 120).color(() -> UIDemoAnimation.INK.to(Color.WHITE, color.get())).onDetach(event -> color.clear()).attach(rect);
			TextNode.create(200, 275).text(Text.create("Color", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 100, 60, 60).color(UIDemoAnimation.INK).width(() -> 60D + 260D * size.get()).onDetach(event -> size.clear()).attach(rect);
			TextNode.create(200, 275).text(Text.create("Size", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoAnimation.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(150, 80, 100, 100)
			.color(UIDemoAnimation.INK)
			.effect(TransformNodeEffect.create(Transformation.create().rotate(() -> rotation.get() * 360D, Rotation.ROLL)))
			.onDetach(event -> rotation.clear())
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Rotation", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoAnimation.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(once).onDetach(event -> once.clear()).attach(line);
			})
			.attach(rect);
			RectNode
			.create(40, 150, 100, 50)
			.color(UIDemoAnimation.INK)
			.onClick(event -> once.sequence(1000F, once.get() < 0.5F ? 1F : 0F, TweenEquations.CUBIC_INOUT).onComplete(tween -> plays.increment()).start())
			.label(Text.create("Play", label))
			.attach(rect);
			RectNode
			.create(160, 150, 100, 50)
			.color(UIDemoAnimation.INK)
			.onClick(event -> once.value(0F))
			.label(Text.create("Stop", label))
			.attach(rect);
			TextNode.create(280, 162).text(Text.create("Ends: " + plays.get(), caption)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Play and stop", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}