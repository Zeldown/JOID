package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;

public class TweenManager {

	private final Object             lock    = new Object();
	private final List<BaseTween<?>> objects = new ArrayList<>();

	private boolean isPaused;

	public @NonNull TweenManager add(final @NonNull BaseTween<?> object) {
		synchronized (this.lock) {
			if (!this.objects.contains(object)) {
				this.objects.add(object);
			}
		}

		object.start();
		return this;
	}

	public @NonNull TweenManager killAll() {
		synchronized (this.lock) {
			for (final BaseTween<?> object : this.objects) {
				object.kill();
			}
		}

		return this;
	}

	public @NonNull TweenManager pause() {
		this.isPaused = true;
		return this;
	}

	public @NonNull TweenManager resume() {
		this.isPaused = false;
		return this;
	}

	public void update(final float delta) {
		synchronized (this.lock) {
			this.objects.removeIf(BaseTween::isFinished);

			if (!this.isPaused) {
				final List<BaseTween<?>> objectList = new ArrayList<>(this.objects);
				if (delta >= 0F) {
					for (final BaseTween<?> object : objectList) {
						object.update(delta);
					}
				} else {
					for (int i = objectList.size() - 1; i >= 0; i--) {
						objectList.get(i).update(delta);
					}
				}
			}
		}
	}

	public int size() {
		synchronized (this.lock) {
			return this.objects.size();
		}
	}

	public int getRunningTweensCount() {
		synchronized (this.lock) {
			return TweenManager.getTweensCount(this.objects);
		}
	}

	public int getRunningTimelinesCount() {
		synchronized (this.lock) {
			return TweenManager.getTimelinesCount(this.objects);
		}
	}

	public @NonNull List<BaseTween<?>> getObjects() {
		synchronized (this.lock) {
			return Collections.unmodifiableList(new ArrayList<>(this.objects));
		}
	}

	private static int getTweensCount(final List<BaseTween<?>> objects) {
		int count = 0;
		for (final BaseTween<?> object : objects) {
			if (object instanceof Tween) {
				count++;
			} else if (object instanceof Timeline) {
				count += TweenManager.getTweensCount(((Timeline) object).getChildren());
			}
		}
		return count;
	}

	private static int getTimelinesCount(final List<BaseTween<?>> objects) {
		int count = 0;
		for (final BaseTween<?> object : objects) {
			if (object instanceof Timeline) {
				count += 1 + TweenManager.getTimelinesCount(((Timeline) object).getChildren());
			}
		}
		return count;
	}

}