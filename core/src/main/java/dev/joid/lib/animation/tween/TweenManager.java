package dev.joid.lib.animation.tween;

import java.util.ArrayList;
import java.util.List;

public class TweenManager {

	private final Object             lock    = new Object();
	private final List<BaseTween<?>> objects = new ArrayList<>();

	private boolean isPaused;

	public TweenManager add(final BaseTween<?> object) {
		synchronized (this.lock) {
			if (!this.objects.contains(object)) {
				this.objects.add(object);
			}
		}

		object.start();
		return this;
	}

	public void killAll() {
		synchronized (this.lock) {
			for (final BaseTween<?> object : this.objects) {
				object.kill();
			}
		}
	}

	public void pause() {
		this.isPaused = true;
	}

	public void resume() {
		this.isPaused = false;
	}

	public void update(final float delta) {
		synchronized (this.lock) {
			this.objects.removeIf(BaseTween::isFinished);

			if (!this.isPaused) {
				final List<BaseTween<?>> objectList = new ArrayList<>(this.objects);
				if (delta >= 0) {
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

}