package dev.joid.demo.ui.animation;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.animator.TweenAnimatorAccessor;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenManager;
import dev.joid.lib.animation.tween.TweenPaths;
import dev.joid.lib.animation.tween.primitive.MutableFloat;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoTween extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	private final MutableFloat value   = new MutableFloat(0F);
	private final TweenManager manager = new TweenManager();

	private long lastUpdate;

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoTween.INK);
		final TweenAnimator back = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.BACK_INOUT);
		back.getTimeline().repeatYoyo(-1, 0F);
		back.start();
		final TweenAnimator circ = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.CIRC_INOUT);
		circ.getTimeline().repeatYoyo(-1, 0F);
		circ.start();
		final TweenAnimator expo = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.EXPO_OUT);
		expo.getTimeline().repeatYoyo(-1, 0F);
		expo.start();
		final TweenAnimator quint = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.QUINT_INOUT);
		quint.getTimeline().repeatYoyo(-1, 0F);
		quint.start();
		final TweenAnimator parallel = TweenAnimator.create(0F).parallel(1000F, 1F, TweenEquations.QUAD_INOUT).push(500F, 1F, TweenEquations.SINE_INOUT);
		parallel.getTimeline().repeatYoyo(-1, 0F);
		parallel.start();
		final TweenAnimator paused = TweenAnimator.create(0F);
		paused.setTimeline(Timeline.createSequence().push(Tween.to(paused, TweenAnimatorAccessor.ANIMATION_VALUE, 600F).target(1F).ease(TweenEquations.QUAD_OUT)).pushPause(600F).push(Tween.to(paused, TweenAnimatorAccessor.ANIMATION_VALUE, 600F).target(0F).ease(TweenEquations.QUAD_IN)));
		paused.getTimeline().repeat(-1, 300F);
		paused.start();
		final TweenAnimator waypoints = TweenAnimator.create(0F);
		waypoints.setTimeline(Timeline.createSequence().push(Tween.to(waypoints, TweenAnimatorAccessor.ANIMATION_VALUE, 2000F).waypoint(1F).waypoint(0.2F).target(1F).path(TweenPaths.catmullRom)));
		waypoints.getTimeline().repeatYoyo(-1, 0F);
		waypoints.start();
		this.manager.killAll();
		this.value.setValue(0F);
		Tween.to(this.value, 0, 1000F).target(1F).ease(TweenEquations.CUBIC_INOUT).repeatYoyo(Tween.INFINITY, 0F).start(this.manager);
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();

		RectNode
		.create(100, 210, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(back).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Back in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 210, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(circ).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Circ in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 210, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(expo).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Expo out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 210, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(quint).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Quint in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 550, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 180)
			.color(Color.WHITE)
			.body(area -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).x(() -> 260D * parallel.getValue(0)).y(() -> 120D * parallel.getValue(1)).animate(parallel).attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Parallel timeline", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 550, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(paused).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Timeline pause", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 550, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).x(() -> 260D * this.value.floatValue()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Tween manager", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 550, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate((node, animator, value) -> node.x(260D * value)).animate(waypoints).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Waypoints", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

	@Override
	public void update() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.manager.update(now - this.lastUpdate);
		this.lastUpdate = now;
	}

}