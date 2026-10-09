package dev.joid.lib.ui.node.callback;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class DispatchContextTest {

	@Test
	public void startsRunningByDefault() {
		Assert.assertFalse(DispatchContext.create().isCancelled());
		Assert.assertTrue(DispatchContext.create(true).isCancelled());
		Assert.assertFalse(DispatchContext.create(false).isCancelled());
	}

	@Test
	public void executesOnlyWhileRunning() {
		final AtomicInteger runs = new AtomicInteger();
		final DispatchContext context = DispatchContext.create();
		Assert.assertSame(context, context.execute(runs::incrementAndGet));
		Assert.assertSame(context, context.cancel());
		context.execute(runs::incrementAndGet);
		Assert.assertEquals(1, runs.get());
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void cancelsAfterRunningATask() {
		final AtomicInteger runs = new AtomicInteger();
		final DispatchContext context = DispatchContext.create();
		Assert.assertSame(context, context.cancel(runs::incrementAndGet));
		Assert.assertTrue(context.isCancelled());
		Assert.assertSame(context, context.cancel(runs::incrementAndGet));
		Assert.assertEquals(1, runs.get());
	}

	@Test
	public void cancelsAfterAnAssignmentOfFalse() {
		final boolean[] active = {true};
		final DispatchContext context = DispatchContext.create();
		Assert.assertSame(context, context.cancel(() -> active[0] = false));
		Assert.assertTrue(context.isCancelled());
		Assert.assertFalse(active[0]);
	}

	@Test
	public void cancelsWhenTheConditionHolds() {
		final DispatchContext context = DispatchContext.create();
		Assert.assertSame(context, context.cancelIf(() -> false));
		Assert.assertFalse(context.isCancelled());
		Assert.assertSame(context, context.cancelIf(() -> true));
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void skipsTheConditionOnceCancelled() {
		final AtomicInteger checks = new AtomicInteger();
		final DispatchContext context = DispatchContext.create(true);
		Assert.assertSame(context, context.cancelIf(() -> checks.incrementAndGet() > 0));
		Assert.assertEquals(0, checks.get());
	}

	@Test
	public void carriesThePathOfItsEvent() {
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode parent = RectNode.create(0D, 0D, 20D, 20D);
		final DispatchContext context = DispatchContext.create(Arrays.asList(child, parent));
		Assert.assertSame(child, context.getTarget());
		Assert.assertTrue(context.isOnPath(child));
		Assert.assertTrue(context.isOnPath(parent));
		Assert.assertFalse(context.isOnPath(RectNode.create(0D, 0D, 10D, 10D)));
		Assert.assertFalse(context.isCancelled());
		Assert.assertNull(DispatchContext.create().getTarget());
		Assert.assertTrue(DispatchContext.create(true).getPath().isEmpty());
	}

	@Test
	public void runsAgainAfterAReset() {
		final AtomicInteger runs = new AtomicInteger();
		final DispatchContext context = DispatchContext.create(true);
		Assert.assertSame(context, context.reset());
		Assert.assertFalse(context.isCancelled());
		context.execute(runs::incrementAndGet);
		Assert.assertEquals(1, runs.get());
	}

}