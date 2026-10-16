package dev.joid.demo.ui.animation;

import java.util.stream.Stream;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenManager;
import dev.joid.lib.animation.tween.TweenPaths;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoTween extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final TweenManager manager = new TweenManager();

	private float value;
	private long  lastUpdate;

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoTween.INK);
		final TweenAnimator back = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.BACK_INOUT).repeatYoyo(Tween.INFINITY, 0F).start();
		final TweenAnimator circ = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.CIRC_INOUT).repeatYoyo(Tween.INFINITY, 0F).start();
		final TweenAnimator expo = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.EXPO_OUT).repeatYoyo(Tween.INFINITY, 0F).start();
		final TweenAnimator quint = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.QUINT_INOUT).repeatYoyo(Tween.INFINITY, 0F).start();
		final TweenAnimator horizontal = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.QUAD_INOUT).repeatYoyo(Tween.INFINITY, 0F).start();
		final TweenAnimator vertical = TweenAnimator.create(0F).sequence(500F, 1F, TweenEquations.SINE_INOUT).push(500F, 1F).repeatYoyo(Tween.INFINITY, 0F).start();
		final TweenAnimator paused = TweenAnimator.create(0F);
		paused.timeline(Timeline.createSequence().push(paused.tween(600F).target(1F).ease(TweenEquations.QUAD_OUT)).pushPause(600F).push(paused.tween(600F).target(0F).ease(TweenEquations.QUAD_IN))).repeat(Tween.INFINITY, 300F).start();
		final TweenAnimator waypoints = TweenAnimator.create(0F);
		waypoints.timeline(Timeline.createSequence().push(waypoints.tween(2000F).waypoint(1F).waypoint(0.2F).target(1F).ease(TweenEquations.QUAD_INOUT).path(TweenPaths.catmullRom))).repeatYoyo(Tween.INFINITY, 0F).start();
		final IntegerSignal starts = new IntegerSignal(0);
		final IntegerSignal laps = new IntegerSignal(0);
		final TweenAnimator events = TweenAnimator.create(0F).sequence(1000F, 1F, TweenEquations.SINE_INOUT).repeatYoyo(Tween.INFINITY, 0F).onStart(event -> starts.increment()).onIterationEnd(event -> laps.increment()).start();
		this.manager.killAll();
		this.value = 0F;
		Tween.to(() -> this.value, value -> this.value = value, 1000F).target(1F).ease(TweenEquations.CUBIC_INOUT).repeatYoyo(Tween.INFINITY, 0F).start(this.manager);
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(back).onDetach(event -> back.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Back in out", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(circ).onDetach(event -> circ.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Circ in out", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(expo).onDetach(event -> expo.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Expo out", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(quint).onDetach(event -> quint.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Quint in out", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).x(() -> 260D * horizontal.get()).y(() -> 120D * vertical.get()).onDetach(event -> Stream.of(horizontal, vertical).forEach(TweenAnimator::clear)).attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Two animators", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(paused).onDetach(event -> paused.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Timeline pause", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).x(() -> 260D * this.value).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Tween manager", caption)).anchorX(Align.CENTER).attach(rect);
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
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(waypoints).onDetach(event -> waypoints.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Waypoints", caption)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(760, 720, 400, 260)
		.color(UIDemoTween.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 60, 320, 60)
			.color(Color.WHITE)
			.body(line -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoTween.INK).onAnimate(event -> event.getNode().x(260D * event.getValue())).animate(events).onDetach(event -> events.clear()).attach(line);
			})
			.attach(rect);
			TextNode.create(40, 162).text(Text.create("Starts: " + starts.get() + ", laps: " + laps.get(), caption)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Tween events", caption)).anchorX(Align.CENTER).attach(rect);
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