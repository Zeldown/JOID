package be.zeldown.joid.lib.animation.tweenengine;



import java.util.ArrayList;

public abstract class Pool<T> {

	private final ArrayList<T> objects;
	private final Callback<T> callback;

	protected abstract T create();

	public Pool(final int initCapacity, final Callback<T> callback) {
		this.objects = new ArrayList<>(initCapacity);
		this.callback = callback;
	}

	public T get() {
		final T obj = this.objects.isEmpty() ? this.create() : this.objects.remove(this.objects.size() - 1);
		if (this.callback != null) {
			this.callback.onUnPool(obj);
		}
		return obj;
	}

	public void free(final T obj) {
		if (!this.objects.contains(obj)) {
			if (this.callback != null) {
				this.callback.onPool(obj);
			}

			this.objects.add(obj);
		}
	}

	public void clear() {
		this.objects.clear();
	}

	public int size() {
		return this.objects.size();
	}

	public void ensureCapacity(final int minCapacity) {
		this.objects.ensureCapacity(minCapacity);
	}

	public interface Callback<T> {

		public void onPool(T obj);
		public void onUnPool(T obj);

	}

}