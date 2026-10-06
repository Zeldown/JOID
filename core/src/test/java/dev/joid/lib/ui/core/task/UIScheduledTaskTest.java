package dev.joid.lib.ui.core.task;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;

public class UIScheduledTaskTest {

	private final ManualClockBridge clock = ManualClockBridge.create(1000L);

	@Before
	public void useAManualClock() {
		BridgeHandler.CLOCK.register(this.clock);
	}

	@After
	public void restoreTheClock() {
		BridgeHandler.CLOCK.unregister(this.clock);
	}

	@Test
	public void keepsItsTaskAndItsTimings() {
		final Runnable runnable = () -> {};
		final UIScheduledTask task = new UIScheduledTask(runnable, 500L, 250L);
		Assert.assertSame(runnable, task.getTask());
		Assert.assertEquals(500L, task.getDelay());
		Assert.assertEquals(250L, task.getPeriod());
		Assert.assertEquals(1000L, task.getLastUpdate());
		Assert.assertEquals(1500L, task.getNextUpdate());
	}

	@Test
	public void waitsForItsDelayBeforeRunning() {
		final UIScheduledTask task = new UIScheduledTask(() -> {}, 500L, -1L);
		Assert.assertFalse(task.shouldRun());
		this.clock.advance(499L);
		Assert.assertFalse(task.shouldRun());
		this.clock.advance(1L);
		Assert.assertTrue(task.shouldRun());
	}

	@Test
	public void runsAtOnceWithoutDelay() {
		Assert.assertTrue(new UIScheduledTask(() -> {}, 0L, -1L).shouldRun());
	}

	@Test
	public void runsOnceWithoutPeriod() {
		final AtomicInteger runs = new AtomicInteger();
		final UIScheduledTask task = new UIScheduledTask(runs::incrementAndGet, 0L, -1L);
		Assert.assertFalse(task.isPeriodic());
		Assert.assertFalse(task.execute());
		Assert.assertEquals(1, runs.get());
		Assert.assertEquals(1000L, task.getNextUpdate());
	}

	@Test
	public void plansItsNextRunOnePeriodAfterEachRun() {
		final AtomicInteger runs = new AtomicInteger();
		final UIScheduledTask task = new UIScheduledTask(runs::incrementAndGet, 100L, 250L);
		this.clock.advance(130L);
		Assert.assertTrue(task.isPeriodic());
		Assert.assertTrue(task.execute());
		Assert.assertEquals(1, runs.get());
		Assert.assertEquals(1130L, task.getLastUpdate());
		Assert.assertEquals(1380L, task.getNextUpdate());
		this.clock.advance(249L);
		Assert.assertFalse(task.shouldRun());
		this.clock.advance(1L);
		Assert.assertTrue(task.shouldRun());
	}

	@Test
	public void repeatsOnEveryCheckWithAZeroPeriod() {
		final AtomicInteger runs = new AtomicInteger();
		final UIScheduledTask task = new UIScheduledTask(runs::incrementAndGet, 0L, 0L);
		Assert.assertTrue(task.isPeriodic());
		Assert.assertTrue(task.execute());
		Assert.assertTrue(task.shouldRun());
		Assert.assertTrue(task.execute());
		Assert.assertEquals(2, runs.get());
	}

}