package dev.joid.lib.utils.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.thread.QueueThreadBridge;

public class RenderThreadFutureTest {

	private final QueueThreadBridge thread = new QueueThreadBridge();

	@Before
	public void useAQueuedThreadBridge() {
		BridgeHandler.THREAD.register(this.thread);
	}

	@After
	public void restoreTheThreadBridge() {
		BridgeHandler.THREAD.unregister(this.thread);
	}

	@Test
	public void givesTheValueOfItsSourceWithinATimeoutOnTheRenderThread() throws InterruptedException, ExecutionException, TimeoutException {
		final CompletableFuture<String> source = new CompletableFuture<>();
		final RenderThreadFuture<String> future = RenderThreadFuture.of(source);
		source.complete("font");
		this.thread.renderThread(true);
		Assert.assertEquals("font", future.get(1L, TimeUnit.SECONDS));
	}

	@Test
	public void givesTheFailureOfItsSourceWithinATimeoutOnTheRenderThread() throws InterruptedException, TimeoutException {
		final CompletableFuture<String> source = new CompletableFuture<>();
		final RenderThreadFuture<String> future = RenderThreadFuture.of(source);
		source.completeExceptionally(new IllegalStateException("The font cannot be read"));
		this.thread.renderThread(true);
		try {
			future.get(1L, TimeUnit.SECONDS);
			Assert.fail("The failure of the source must reach the caller");
		} catch (final ExecutionException expected) {
			Assert.assertEquals("The font cannot be read", expected.getCause().getMessage());
		}
	}

	@Test
	public void givesItsCallbacksTheFailureOfItsSourceWhenJoinedOnTheRenderThread() {
		final CompletableFuture<String> source = new CompletableFuture<>();
		final RenderThreadFuture<String> future = RenderThreadFuture.of(source);
		final Throwable[] received = new Throwable[1];
		future.whenComplete((value, error) -> received[0] = error);
		source.completeExceptionally(new IllegalStateException("The font cannot be read"));
		this.thread.renderThread(true);
		try {
			future.join();
			Assert.fail("The failure of the source must reach the caller");
		} catch (final CompletionException expected) {
			Assert.assertEquals("The font cannot be read", expected.getCause().getMessage());
		}
		Assert.assertTrue(String.valueOf(received[0]), received[0] instanceof IllegalStateException);
	}

}