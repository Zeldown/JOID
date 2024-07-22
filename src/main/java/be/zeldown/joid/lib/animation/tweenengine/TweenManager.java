package be.zeldown.joid.lib.animation.tweenengine;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TweenManager {
	// -------------------------------------------------------------------------
	// Static API
	// -------------------------------------------------------------------------

	public static void setAutoRemove(final BaseTween<?> object, final boolean value) {
		object.isAutoRemoveEnabled = value;
	}

	public static void setAutoStart(final BaseTween<?> object, final boolean value) {
		object.isAutoStartEnabled = value;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	private final ArrayList<BaseTween<?>> objects = new ArrayList<>(20);
	private boolean isPaused = false;

	public TweenManager add(final BaseTween<?> object) {
		if (!this.objects.contains(object)) {
			this.objects.add(object);
		}

		if (object.isAutoStartEnabled) {
			object.start();
		}

		return this;
	}

	public boolean containsTarget(final Object target) {
		for (final BaseTween<?> obj : this.objects) {
			if (obj.containsTarget(target)) {
				return true;
			}
		}

		return false;
	}

	public boolean containsTarget(final Object target, final int tweenType) {
		for (final BaseTween<?> obj : this.objects) {
			if (obj.containsTarget(target, tweenType)) {
				return true;
			}
		}

		return false;
	}

	public void killAll() {
		for (final BaseTween<?> obj : this.objects) {
			obj.kill();
		}
	}

	public void killTarget(final Object target) {
		for (final BaseTween<?> obj : this.objects) {
			obj.killTarget(target);
		}
	}

	public void killTarget(final Object target, final int tweenType) {
		for (final BaseTween<?> obj : this.objects) {
			obj.killTarget(target, tweenType);
		}
	}

	public void ensureCapacity(final int minCapacity) {
		this.objects.ensureCapacity(minCapacity);
	}

	public void pause() {
		this.isPaused = true;
	}

	public void resume() {
		this.isPaused = false;
	}

	public void update(final float delta) {
		for (int i = this.objects.size() - 1; i >= 0; i--) {
			final BaseTween<?> obj = this.objects.get(i);
			if (obj.isFinished() && obj.isAutoRemoveEnabled) {
				this.objects.remove(i);
				obj.free();
			}
		}

		if (!this.isPaused) {
			if (delta >= 0) {
				for (final BaseTween<?> element : this.objects) {
					element.update(delta);
				}
			} else {
				for (int i = this.objects.size() - 1; i >= 0; i--) {
					this.objects.get(i).update(delta);
				}
			}
		}
	}

	public int size() {
		return this.objects.size();
	}

	public int getRunningTweensCount() {
		return TweenManager.getTweensCount(this.objects);
	}

	public int getRunningTimelinesCount() {
		return TweenManager.getTimelinesCount(this.objects);
	}

	public List<BaseTween<?>> getObjects() {
		return Collections.unmodifiableList(this.objects);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static int getTweensCount(final List<BaseTween<?>> objs) {
		int cnt = 0;
		for (final BaseTween<?> obj : objs) {
			if (obj instanceof Tween) {
				cnt += 1;
			} else {
				cnt += TweenManager.getTweensCount(((Timeline) obj).getChildren());
			}
		}

		return cnt;
	}

	private static int getTimelinesCount(final List<BaseTween<?>> objs) {
		int cnt = 0;
		for (final BaseTween<?> obj : objs) {
			if (obj instanceof Timeline) {
				cnt += 1 + TweenManager.getTimelinesCount(((Timeline) obj).getChildren());
			}
		}
		return cnt;
	}

}