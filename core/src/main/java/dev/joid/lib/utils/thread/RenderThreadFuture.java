package dev.joid.lib.utils.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RenderThreadFuture<T> extends CompletableFuture<T> {

	private final CompletableFuture<T> source;

	public static <T> @NonNull RenderThreadFuture<T> from(final @NonNull CompletableFuture<T> source) {
		final RenderThreadFuture<T> future = new RenderThreadFuture<>(source);
		source.whenComplete((value, error) -> ThreadUtils.runOnRenderThread(() -> future.settle(value, error)));
		return future;
	}

	@Override
	public T join() {
		this.settleOnRenderThread();
		return super.join();
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		this.settleOnRenderThread();
		return super.get();
	}

	@Override
	public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!super.isDone() && BridgeHandler.THREAD.get().isRenderThread()) {
			try {
				this.settle(this.source.get(timeout, unit), null);
			} catch (final ExecutionException exception) {
				this.settle(null, exception.getCause());
			}
		}
		return super.get(timeout, unit);
	}

	private void settleOnRenderThread() {
		if (super.isDone() || !BridgeHandler.THREAD.get().isRenderThread()) {
			return;
		}

		try {
			this.settle(this.source.join(), null);
		} catch (final CompletionException exception) {
			this.settle(null, exception.getCause());
		}
	}

	private void settle(final T value, final Throwable error) {
		if (error != null) {
			super.completeExceptionally(error);
		} else {
			super.complete(value);
		}
	}

}