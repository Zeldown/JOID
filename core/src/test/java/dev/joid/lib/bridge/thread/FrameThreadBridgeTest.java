package dev.joid.lib.bridge.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOIDWarnings;
import dev.joid.internal.TestUtils;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.thread.ThreadUtils;

public class FrameThreadBridgeTest {

	@After
	public void forgetTheWarnings() {
		JOIDWarnings.forget();
	}

	@Test
	public void runsATaskFromAnotherThreadAtTheNextFrame() {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final List<String> trace = Collections.synchronizedList(new ArrayList<>());
		BridgeHandler.THREAD.register(thread);
		try {
			FrameThreadBridgeTest.fromWorker(() -> ThreadUtils.runOnRenderThread(() -> trace.add(Thread.currentThread().getName())));
			Assert.assertTrue(trace.isEmpty());
			BridgeHandler.UI.fireFrame();
			Assert.assertEquals(Collections.singletonList(Thread.currentThread().getName()), trace);
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void runsATaskAtOnceOnTheRenderThread() {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final AtomicBoolean ran = new AtomicBoolean();
		Assert.assertTrue(thread.isRenderThread());
		thread.execute(() -> ran.set(true));
		Assert.assertTrue(ran.get());
	}

	@Test
	public void knowsOnlyTheThreadThatCreatedIt() {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final AtomicBoolean render = new AtomicBoolean(true);
		FrameThreadBridgeTest.fromWorker(() -> render.set(thread.isRenderThread()));
		Assert.assertFalse(render.get());
		Assert.assertTrue(thread.isRenderThread());
	}

	@Test
	public void runsTheTasksInTheOrderTheyCame() {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final List<String> trace = new ArrayList<>();
		FrameThreadBridgeTest.fromWorker(() -> {
			thread.execute(() -> trace.add("first"));
			thread.execute(() -> trace.add("second"));
			thread.execute(() -> trace.add("third"));
		});
		thread.runTasks();
		Assert.assertEquals(Arrays.asList("first", "second", "third"), trace);
		thread.runTasks();
		Assert.assertEquals(3, trace.size());
	}

	@Test
	public void keepsRunningTheTasksAfterOneThrows() {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final List<String> trace = new ArrayList<>();
		FrameThreadBridgeTest.fromWorker(() -> {
			thread.execute(() -> {
				throw new IllegalStateException("broken");
			});
			thread.execute(() -> trace.add("next"));
		});
		final String text = TestUtils.captureErr(() -> thread.runTasks());
		Assert.assertEquals(Collections.singletonList("next"), trace);
		Assert.assertTrue(text, text.startsWith("[JOID] A task sent to the render thread from another thread failed (java.lang.IllegalStateException: broken), the next tasks still run: catch the exception inside the task to handle it"));
	}

	@Test
	public void warnsOnceForEachPlaceATaskThrowsFrom() {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final List<Runnable> failures = Arrays.asList(() -> {
			throw new IllegalStateException("first");
		}, () -> {
			throw new IllegalStateException("second");
		});
		FrameThreadBridgeTest.fromWorker(() -> {
			for (int turn = 0; turn < 2; turn++) {
				failures.forEach(failure -> thread.execute(() -> failure.run()));
			}
		});
		final String text = TestUtils.captureErr(() -> thread.runTasks());
		Assert.assertEquals(text, 2, text.split("\\[JOID\\] A task sent to the render thread", -1).length - 1);
		Assert.assertTrue(text, text.contains("(java.lang.IllegalStateException: first)"));
		Assert.assertTrue(text, text.contains("(java.lang.IllegalStateException: second)"));
	}

	@Test
	public void warnsOnceWhenItsFramesRunOnAnotherThreadThanItsOwn() {
		final List<FrameThreadBridge> threads = new ArrayList<>();
		FrameThreadBridgeTest.fromWorker(() -> threads.add(new FrameThreadBridge()));
		final String text = TestUtils.captureErr(() -> {
			threads.get(0).runTasks();
			threads.get(0).runTasks();
		});
		Assert.assertEquals("[JOID] The FrameThreadBridge was created on the thread worker but its frames run on the thread " + Thread.currentThread().getName() + ": create it on the render thread, or JOID takes the render thread for another thread" + System.lineSeparator(), text);
		Assert.assertEquals("", TestUtils.captureErr(() -> new FrameThreadBridge().runTasks()));
	}

	private static void fromWorker(final Runnable task) {
		final Thread worker = ThreadUtils.daemonThread(task, "worker");
		worker.start();
		TestUtils.join(worker);
	}

}