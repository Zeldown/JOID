package dev.joid.lib.utils.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.thread.QueueThreadBridge;

public class ThreadUtilsTest {

	@Test
	public void runsATaskAtOnceOnTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge().renderThread(true);
		final AtomicBoolean ran = new AtomicBoolean();
		BridgeHandler.THREAD.register(thread);
		try {
			ThreadUtils.runOnRenderThread(() -> ran.set(true));
			Assert.assertTrue(ran.get());
			Assert.assertTrue(thread.isIdle());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void postsATaskFromAnotherThreadToTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		final AtomicBoolean ran = new AtomicBoolean();
		BridgeHandler.THREAD.register(thread);
		try {
			ThreadUtils.runOnRenderThread(() -> ran.set(true));
			Assert.assertFalse(ran.get());
			thread.run();
			Assert.assertTrue(ran.get());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void createsANamedDaemonThread() throws InterruptedException {
		final AtomicBoolean ran = new AtomicBoolean();
		final Thread thread = ThreadUtils.daemonThread(() -> ran.set(true), "joid-worker");
		Assert.assertEquals("joid-worker", thread.getName());
		Assert.assertTrue(thread.isDaemon());
		thread.start();
		thread.join();
		Assert.assertTrue(ran.get());
	}

	@Test
	public void numbersTheThreadsOfAFactory() throws InterruptedException {
		final AtomicBoolean ran = new AtomicBoolean();
		final ThreadFactory factory = ThreadUtils.daemonFactory("joid-pool");
		final Thread first = factory.newThread(() -> ran.set(true));
		final Thread second = factory.newThread(() -> ran.set(true));
		Assert.assertEquals("joid-pool/1", first.getName());
		Assert.assertEquals("joid-pool/2", second.getName());
		Assert.assertTrue(first.isDaemon());
		Assert.assertTrue(second.isDaemon());
		first.start();
		first.join();
		Assert.assertTrue(ran.get());
	}

	@Test
	public void countsEachFactoryOnItsOwn() {
		final ThreadFactory factory = ThreadUtils.daemonFactory("joid-pool");
		factory.newThread(() -> {});
		Assert.assertEquals("joid-pool/2", factory.newThread(() -> {}).getName());
		Assert.assertEquals("joid-other/1", ThreadUtils.daemonFactory("joid-other").newThread(() -> {}).getName());
	}

}