package dev.joid.lib.ui.node.callback.registry;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.utils.context.InternalContext;

public class NodeCallbackRegistryTest {

	@Test
	public void givesEachRegistrationTheNextId() {
		final int first = NodeCallbackRegistry.next(ValidCallback.class);
		final int second = NodeCallbackRegistry.next(ValidCallback.class);
		Assert.assertEquals(first + 1, second);
		Assert.assertSame(ValidCallback.class, NodeCallbackRegistry.get(first));
		Assert.assertSame(ValidCallback.class, NodeCallbackRegistry.get(second));
	}

	@Test
	public void findsTheIdOfARegisteredCallback() {
		final int id = NodeCallbackRegistry.next(OtherCallback.class);
		Assert.assertEquals(id, NodeCallbackRegistry.getId(OtherCallback.class));
	}

	@Test
	public void knowsNothingOfAnUnregisteredCallback() {
		Assert.assertEquals(-1, NodeCallbackRegistry.getId(LooseCallback.class));
		Assert.assertNull(NodeCallbackRegistry.get(-1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesACallbackThatIsNotAFunctionalInterface() {
		NodeCallbackRegistry.next(LooseCallback.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesACallbackWithoutPrePhase() {
		NodeCallbackRegistry.next(PostOnlyCallback.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesACallbackWithoutPostPhase() {
		NodeCallbackRegistry.next(PreOnlyCallback.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAPhaseReturningAValue() {
		NodeCallbackRegistry.next(ReturningCallback.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAPhaseWithoutContext() {
		NodeCallbackRegistry.next(ContextlessCallback.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAPhaseNotStartingWithANode() {
		NodeCallbackRegistry.next(NodelessCallback.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAPhaseWithoutTheContextSecond() {
		NodeCallbackRegistry.next(MisplacedContextCallback.class);
	}

	@Test
	public void asksForAPhaseReturningVoid() {
		try {
			NodeCallbackRegistry.next(ReturningCallback.class);
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().endsWith("PRE method must return void"));
		}
	}

	@FunctionalInterface
	private interface ValidCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final Node node, final InternalContext context) {}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface OtherCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final Node node, final InternalContext context, final int value) {}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context, final int value) {}

	}

	private interface LooseCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final Node node, final InternalContext context) {}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface PostOnlyCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface PreOnlyCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface ReturningCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default boolean pre(final Node node, final InternalContext context) {
			return true;
		}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface ContextlessCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final Node node) {}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface NodelessCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final String text, final InternalContext context) {}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

	@FunctionalInterface
	private interface MisplacedContextCallback extends NodeCallback {

		public void apply();

		@NodeCallbackMethod(Type.PRE)
		public default void pre(final Node node, final String text) {}

		@NodeCallbackMethod(Type.POST)
		public default void post(final Node node, final InternalContext context) {}

	}

}