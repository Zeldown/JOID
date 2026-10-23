package dev.joid.lib.bridge.thread;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import dev.joid.internal.JOIDWarnings;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.thread.ThreadUtils;

public class FrameThreadBridgeTest {

	@After
	public void forgetTheWarnings() {
		JOIDWarnings.forget();
	}

	@Test
	public void runsATaskFromAnotherThreadAtTheNextFrame() throws InterruptedException {
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
		BridgeHandler.THREAD.register(thread);
		try {
			Assert.assertTrue(thread.isRenderThread());
			ThreadUtils.runOnRenderThread(() -> ran.set(true));
			Assert.assertTrue(ran.get());
			thread.execute(() -> ran.set(false));
			Assert.assertFalse(ran.get());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void knowsOnlyTheThreadThatCreatedIt() throws InterruptedException {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final AtomicBoolean render = new AtomicBoolean(true);
		FrameThreadBridgeTest.fromWorker(() -> render.set(thread.isRenderThread()));
		Assert.assertFalse(render.get());
		Assert.assertTrue(thread.isRenderThread());
	}

	@Test
	public void runsTheTasksInTheOrderTheyCame() throws InterruptedException {
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
	public void keepsRunningTheTasksAfterOneThrows() throws InterruptedException {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final List<String> trace = new ArrayList<>();
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		FrameThreadBridgeTest.fromWorker(() -> {
			thread.execute(() -> {
				throw new IllegalStateException("broken");
			});
			thread.execute(() -> trace.add("next"));
		});
		System.setErr(new PrintStream(output, true));
		try {
			thread.runTasks();
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals(Collections.singletonList("next"), trace);
		final String text = new String(output.toByteArray(), StandardCharsets.UTF_8);
		Assert.assertTrue(text, text.startsWith("[JOID] A task sent to the render thread from another thread failed (java.lang.IllegalStateException: broken), the next tasks still run: catch the exception inside the task to handle it"));
	}

	@Test
	public void warnsOnceForEachPlaceATaskThrowsFrom() throws InterruptedException {
		final FrameThreadBridge thread = new FrameThreadBridge();
		final List<Runnable> failures = Arrays.asList(() -> {
			throw new IllegalStateException("first");
		}, () -> {
			throw new IllegalStateException("second");
		});
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		FrameThreadBridgeTest.fromWorker(() -> {
			for (int turn = 0; turn < 2; turn++) {
				failures.forEach(failure -> thread.execute(() -> failure.run()));
			}
		});
		System.setErr(new PrintStream(output, true));
		try {
			thread.runTasks();
		} finally {
			System.setErr(previous);
		}
		final String text = new String(output.toByteArray(), StandardCharsets.UTF_8);
		Assert.assertEquals(text, 2, text.split("\\[JOID\\] A task sent to the render thread", -1).length - 1);
		Assert.assertTrue(text, text.contains("(java.lang.IllegalStateException: first)"));
		Assert.assertTrue(text, text.contains("(java.lang.IllegalStateException: second)"));
	}

	private static void fromWorker(final Runnable task) throws InterruptedException {
		final Thread worker = ThreadUtils.daemonThread(task, "worker");
		worker.start();
		worker.join();
	}

}