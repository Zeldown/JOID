package dev.joid.lib.utils.context;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class InternalContextTest {

	@Test
	public void startsRunningByDefault() {
		Assert.assertFalse(InternalContext.create().isCancelled());
		Assert.assertTrue(InternalContext.create(true).isCancelled());
		Assert.assertFalse(InternalContext.create(false).isCancelled());
	}

	@Test
	public void executesOnlyWhileRunning() {
		final AtomicInteger runs = new AtomicInteger();
		final InternalContext context = InternalContext.create();
		Assert.assertSame(context, context.execute(runs::incrementAndGet));
		Assert.assertSame(context, context.cancel());
		context.execute(runs::incrementAndGet);
		Assert.assertEquals(1, runs.get());
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void cancelsAfterRunningATask() {
		final AtomicInteger runs = new AtomicInteger();
		final InternalContext context = InternalContext.create();
		Assert.assertSame(context, context.cancel(runs::incrementAndGet));
		Assert.assertTrue(context.isCancelled());
		Assert.assertSame(context, context.cancel(runs::incrementAndGet));
		Assert.assertEquals(1, runs.get());
	}

	@Test
	public void cancelsAfterAnAssignmentOfFalse() {
		final boolean[] active = {true};
		final InternalContext context = InternalContext.create();
		Assert.assertSame(context, context.cancel(() -> active[0] = false));
		Assert.assertTrue(context.isCancelled());
		Assert.assertFalse(active[0]);
	}

	@Test
	public void cancelsWhenTheConditionHolds() {
		final InternalContext context = InternalContext.create();
		Assert.assertSame(context, context.cancelIf(() -> false));
		Assert.assertFalse(context.isCancelled());
		Assert.assertSame(context, context.cancelIf(() -> true));
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void skipsTheConditionOnceCancelled() {
		final AtomicInteger checks = new AtomicInteger();
		final InternalContext context = InternalContext.create(true);
		Assert.assertSame(context, context.cancelIf(() -> checks.incrementAndGet() > 0));
		Assert.assertEquals(0, checks.get());
	}

	@Test
	public void runsAgainAfterAReset() {
		final AtomicInteger runs = new AtomicInteger();
		final InternalContext context = InternalContext.create(true);
		Assert.assertSame(context, context.reset());
		Assert.assertFalse(context.isCancelled());
		context.execute(runs::incrementAndGet);
		Assert.assertEquals(1, runs.get());
	}

}