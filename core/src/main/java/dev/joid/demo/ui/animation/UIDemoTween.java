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
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
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
		final TweenAnimator horizontal = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.QUAD_INOUT);
		horizontal.getTimeline().repeatYoyo(-1, 0F);
		horizontal.start();
		final TweenAnimator vertical = TweenAnimator.create(0F).sequence(500F, 1F, TweenEquations.SINE_INOUT).push(500F, 1F);
		vertical.getTimeline().repeatYoyo(-1, 0F);
		vertical.start();
		final TweenAnimator paused = TweenAnimator.create(0F);
		paused.setTimeline(Timeline.createSequence().push(Tween.to(paused, TweenAnimatorAccessor.ANIMATION_VALUE, 600F).target(1F).ease(TweenEquations.QUAD_OUT)).pushPause(600F).push(Tween.to(paused, TweenAnimatorAccessor.ANIMATION_VALUE, 600F).target(0F).ease(TweenEquations.QUAD_IN)));
		paused.getTimeline().repeat(-1, 300F);
		paused.start();
		final TweenAnimator waypoints = TweenAnimator.create(0F);
		waypoints.setTimeline(Timeline.createSequence().push(Tween.to(waypoints, TweenAnimatorAccessor.ANIMATION_VALUE, 2000F).waypoint(1F).waypoint(0.2F).target(1F).path(TweenPaths.catmullRom)));
		waypoints.getTimeline().repeatYoyo(-1, 0F);
		waypoints.start();
		final IntegerSignal starts = IntegerSignal.of(0);
		final IntegerSignal laps = IntegerSignal.of(0);
		final TweenAnimator events = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.SINE_INOUT);
		events.getTimeline().repeatYoyo(-1, 0F).onStart(event -> starts.increment()).onIterationEnd(event -> laps.increment());
		events.start();
		this.manager.killAll();
		this.value.setValue(0F);
		Tween.to(this.value, 0, 1000F).target(1F).ease(TweenEquations.CUBIC_INOUT).repeatYoyo(Tween.INFINITY, 0F).start(this.manager);
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(back).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Back in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(circ).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Circ in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(expo).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Expo out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(quint).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Quint in out", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 180)
			.color(Color.WHITE)
			.body(area -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).x(260D * horizontal.get()).y(120D * vertical.get()).attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Two animators", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(paused).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Timeline pause", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
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
		.create(1420, 380, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(waypoints).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Waypoints", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 60, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(events).attach(line);
			})
			.attach(rect);
			TextNode.create(40, 162).text(Text.create("Starts: " + starts.get() + ", laps: " + laps.get(), caption)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Tween events", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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