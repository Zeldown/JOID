package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.ICancellableEvent;
import dev.joid.lib.ui.event.UIEvent;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UIChangeEvent<U extends UI, V> extends UIEvent<U> {

	private final V value;
	private final V previous;

	private UIChangeEvent(final U ui, final V previous, final V value) {
		super(ui);
		this.previous = previous;
		this.value = value;
	}

	@Getter
	public static final class Pre<U extends UI, V> extends UIChangeEvent<U, V> implements ICancellableEvent {

		private boolean cancelled;

		private Pre(final U ui, final V previous, final V value) {
			super(ui, previous, value);
		}

		public static <U extends UI, V> @NonNull Pre<U, V> create(final @NonNull U ui, final V previous, final V value) {
			return new Pre<>(ui, previous, value);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<U extends UI, V> extends UIChangeEvent<U, V> {

		private Post(final U ui, final V previous, final V value) {
			super(ui, previous, value);
		}

		public static <U extends UI, V> @NonNull Post<U, V> create(final @NonNull U ui, final V previous, final V value) {
			return new Post<>(ui, previous, value);
		}

	}

}