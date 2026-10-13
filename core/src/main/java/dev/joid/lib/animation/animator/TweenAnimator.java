package dev.joid.lib.animation.animator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.Timeline;
import dev.joid.lib.animation.tween.Tween;
import dev.joid.lib.animation.tween.TweenCallback;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.animation.tween.TweenManager;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class TweenAnimator extends Signal<Float> {

	private static final Set<TweenAnimator> RUNNING_SET = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private final Map<Integer, TweenAnimatorChannel> channelMap;

	private float[] values;
	private boolean parallel;
	private boolean automatic;

	@Setter
	private float speed;

	@Setter private long         lastUpdate;
	@Setter private Timeline     timeline;
	@Setter private TweenManager manager;

	private TweenAnimator(final float value) {
		super(value);
		this.channelMap = new HashMap<>();
		this.manager = new TweenManager();
		this.values = new float[] {value};
		this.speed = 1F;
	}

	public static @NonNull TweenAnimator create() {
		return new TweenAnimator(0F);
	}

	public static @NonNull TweenAnimator create(final float value) {
		return new TweenAnimator(value);
	}

	public @NonNull TweenAnimator start() {
		this.requireTimeline();
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.timeline.start(this.manager);
		this.automatic = UI.getCurrent() != null;
		if (this.automatic) {
			TweenAnimator.RUNNING_SET.add(this);
		} else {
			TweenAnimator.RUNNING_SET.remove(this);
		}
		return this;
	}

	public void clear() {
		final float[] previous = this.values;
		this.release();
		this.manager.killAll();
		this.manager.update(0F);
		this.values     = new float[1];
		this.parallel   = false;
		this.speed      = 1F;
		this.timeline   = null;
		this.lastUpdate = 0L;
		this.notifyValues(previous);
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value) {
		return this.sequence(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator sequence(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.killTimeline();
		this.timeline = Timeline.createSequence();
		this.parallel = false;
		this.push(duration, value, equation);
		return this;
	}

	public @NonNull TweenAnimator parallel(final float duration, final float value) {
		return this.parallel(duration, value, TweenEquations.LINEAR);
	}

	public @NonNull TweenAnimator parallel(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.killTimeline();
		this.timeline = Timeline.createParallel();
		this.parallel = true;
		this.push(duration, value, equation);
		return this;
	}

	public @NonNull TweenAnimator push(final float duration, final float value) {
		this.push(duration, value, TweenEquations.LINEAR);
		return this;
	}

	public @NonNull TweenAnimator push(final float duration, final float value, final @NonNull TweenEquation equation) {
		this.requireTimeline();
		this.timeline.push(Tween.to(this, this.parallel ? this.timeline.getChildren().size() : TweenAnimatorAccessor.ANIMATION_VALUE, duration).target(value).ease(equation));
		return this;
	}

	public @NonNull TweenAnimator setCallback(final @NonNull Consumer<@NonNull BaseTween<@NonNull ?>> callback) {
		this.requireTimeline();
		this.timeline.addCallback(TweenCallback.END, callback);
		return this;
	}

	public @NonNull TweenAnimator update() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.update(now - this.lastUpdate);
		this.lastUpdate = now;
		return this;
	}

	public @NonNull TweenAnimator update(final float delta) {
		Signal.batch(() -> this.manager.update(delta * this.speed));
		if (this.timeline != null && this.timeline.isFinished()) {
			this.timeline = null;
			this.release();
		}
		return this;
	}

	public @NonNull TweenAnimatorChannel channel(final int index) {
		return this.channelMap.computeIfAbsent(index, key -> new TweenAnimatorChannel(this.readValue(key)));
	}

	public void setValue(final float value) {
		this.setValue(TweenAnimatorAccessor.ANIMATION_VALUE, value);
	}

	public void setValue(final int index, final float value) {
		this.killTimeline();
		this.timeline = null;
		this.writeValue(index, value);
	}

	@Override
	public @NonNull TweenAnimator reset() {
		throw new UnsupportedOperationException("A TweenAnimator is read-only and takes the value of its animation: call clear() or setValue(...) instead");
	}

	@Override
	public @NonNull TweenAnimator set(final Float value) {
		throw new UnsupportedOperationException("A TweenAnimator is read-only and takes the value of its animation: call setValue(...) instead");
	}

	@Override
	public Float peek() {
		return this.values[TweenAnimatorAccessor.ANIMATION_VALUE];
	}

	@Override
	public boolean isPresent() {
		return super.get() != null;
	}

	public @NonNull Map<@NonNull Integer, @NonNull TweenAnimatorChannel> getChannelMap() {
		return Collections.unmodifiableMap(this.channelMap);
	}

	public @NonNull float[] getValues() {
		return this.values.clone();
	}

	public static void updateRunning() {
		final List<TweenAnimator> animatorList;
		synchronized (TweenAnimator.RUNNING_SET) {
			animatorList = new ArrayList<>(TweenAnimator.RUNNING_SET);
		}

		for (final TweenAnimator animator : animatorList) {
			animator.update();
		}
	}

	protected float readValue(final int index) {
		return index < this.values.length ? this.values[index] : 0F;
	}

	protected void writeValue(final int index, final float value) {
		final float previous = this.readValue(index);
		if (index >= this.values.length) {
			this.values = Arrays.copyOf(this.values, index + 1);
		}

		this.values[index] = value;
		this.notifyValue(index, previous);
	}

	private void release() {
		this.automatic = false;
		TweenAnimator.RUNNING_SET.remove(this);
	}

	private void killTimeline() {
		this.release();
		if (this.timeline != null) {
			this.timeline.kill();
		}
	}

	private void requireTimeline() {
		if (this.timeline == null) {
			throw new IllegalStateException("The animator has no timeline, call sequence(...) or parallel(...) first");
		}
	}

	private void notifyValue(final int index, final float previous) {
		final float value = this.readValue(index);
		if (Float.compare(previous, value) == 0) {
			return;
		}

		if (index == TweenAnimatorAccessor.ANIMATION_VALUE) {
			super.publish();
		}

		final TweenAnimatorChannel channel = this.channelMap.get(index);
		if (channel != null) {
			channel.update(value);
		}
	}

	private void notifyValues(final float[] previous) {
		Signal.batch(() -> {
			for (int index = 0; index < previous.length; index++) {
				this.notifyValue(index, previous[index]);
			}
		});
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return this == obj;
	}

}