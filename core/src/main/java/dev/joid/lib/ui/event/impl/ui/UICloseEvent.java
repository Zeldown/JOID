package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.ICancellableEvent;
import dev.joid.lib.ui.event.UIEvent;
import lombok.Getter;
import lombok.NonNull;

public abstract class UICloseEvent<U extends UI> extends UIEvent<U> {

	private UICloseEvent(final U ui) {
		super(ui);
	}

	@Getter
	public static final class Pre<U extends UI> extends UICloseEvent<U> implements ICancellableEvent {

		private boolean cancelled;

		private Pre(final U ui) {
			super(ui);
		}

		public static <U extends UI> @NonNull Pre<U> create(final @NonNull U ui) {
			return new Pre<>(ui);
		}

		@Override
		public void cancel() {
			super.checkWritable("cancel");
			this.cancelled = true;
		}

	}

	public static final class Post<U extends UI> extends UICloseEvent<U> {

		private Post(final U ui) {
			super(ui);
		}

		public static <U extends UI> @NonNull Post<U> create(final @NonNull U ui) {
			return new Post<>(ui);
		}

	}

}