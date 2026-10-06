package dev.joid.lib.ui.node.callback;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.callback.impl.mouse.NodeMousePressedCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeInitCallback;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;

import lombok.NonNull;

public class NodeCallbackObjectTest {

	@Test
	public void findsThePhasesOfACallback() {
		final NodeInitCallback<Node> callback = node -> {};
		final NodeCallbackObject<NodeInitCallback<Node>> object = new NodeCallbackObject<>(callback);
		Assert.assertSame(callback, object.getCallback());
		Assert.assertEquals("pre", object.getPre().getName());
		Assert.assertEquals("post", object.getPost().getName());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCallback() {
		new NodeCallbackObject<>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesACallbackWithoutPhase() {
		new NodeCallbackObject<>(new SilentCallback());
	}

	@Test
	public void passesTheNodeAndTheContextBeforeTheArguments() {
		final RecordingCallback callback = new RecordingCallback();
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		final NodeCallbackObject<RecordingCallback> object = new NodeCallbackObject<>(callback);
		object.pre(rect, context, "before", 1);
		object.post(rect, context, "after", 2);
		Assert.assertEquals(Arrays.asList(Arrays.asList("pre", rect, context, "before", 1), Arrays.asList("post", rect, context, "after", 2)), callback.calls);
	}

	@Test
	public void skipsAMissingPrePhase() {
		final RecordingPostCallback callback = new RecordingPostCallback();
		final NodeCallbackObject<RecordingPostCallback> object = new NodeCallbackObject<>(callback);
		Assert.assertNull(object.getPre());
		object.pre(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create());
		Assert.assertEquals(0, callback.calls);
		object.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create());
		Assert.assertEquals(1, callback.calls);
	}

	@Test
	public void skipsAMissingPostPhase() {
		final RecordingPreCallback callback = new RecordingPreCallback();
		final NodeCallbackObject<RecordingPreCallback> object = new NodeCallbackObject<>(callback);
		Assert.assertNull(object.getPost());
		object.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create());
		Assert.assertEquals(0, callback.calls);
		object.pre(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create());
		Assert.assertEquals(1, callback.calls);
	}

	@Test
	public void reportsAFailingPrePhaseWithoutThrowing() {
		final NodeCallbackObject<FailingCallback> object = new NodeCallbackObject<>(new FailingCallback());
		final String output = NodeCallbackObjectTest.capture(() -> object.pre(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create()));
		Assert.assertTrue(output, output.startsWith("[JOID] The pre phase of " + FailingCallback.class.getName() + " failed: java.lang.IllegalStateException: pre"));
	}

	@Test
	public void reportsAFailingPostPhaseWithoutThrowing() {
		final NodeCallbackObject<FailingCallback> object = new NodeCallbackObject<>(new FailingCallback());
		final String output = NodeCallbackObjectTest.capture(() -> object.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create()));
		Assert.assertTrue(output, output.startsWith("[JOID] The post phase of " + FailingCallback.class.getName() + " failed: java.lang.IllegalStateException: post"));
	}

	@Test
	public void reportsArgumentsThatDoNotFitThePhase() {
		final RecordingPostCallback callback = new RecordingPostCallback();
		final NodeCallbackObject<RecordingPostCallback> object = new NodeCallbackObject<>(callback);
		final String output = NodeCallbackObjectTest.capture(() -> object.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(), "extra"));
		Assert.assertTrue(output, output.startsWith("[JOID] The post phase of " + RecordingPostCallback.class.getName() + " failed: java.lang.IllegalArgumentException"));
		Assert.assertEquals(0, callback.calls);
	}

	@Test
	public void runsAPrePhaseOverriddenAsTheDocumentationShows() {
		final NodeMousePressedCallback<RectNode> callback = new NodeMousePressedCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final double mouseX, final double mouseY, final @NonNull ClickType clickType) {}

			@Override
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context, final double mouseX, final double mouseY, final @NonNull ClickType clickType) {
				context.cancel();
			}

		};
		final InternalContext context = InternalContext.create();
		new NodeCallbackObject<>(callback).pre(RectNode.create(0D, 0D, 10D, 10D), context, 3D, 4D, ClickType.LEFT);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void reportsAFailingPhaseCalledWithoutNode() {
		final NodeInitCallback<Node> callback = node -> {};
		final NodeCallbackObject<NodeInitCallback<Node>> object = new NodeCallbackObject<>(callback);
		final String output = NodeCallbackObjectTest.capture(() -> object.post(null, InternalContext.create()));
		Assert.assertTrue(output, output.startsWith("[JOID] The post phase of NodeInitCallback failed: java.lang.NullPointerException"));
	}

	@Test
	public void namesAFailingCallbackByItsInterface() {
		final NodeMousePressedCallback<RectNode> callback = (node, mouseX, mouseY, clickType) -> {
			throw new IllegalStateException("click");
		};
		final String output = NodeCallbackObjectTest.capture(() -> new NodeCallbackObject<>(callback).post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(), 3D, 4D, ClickType.LEFT));
		Assert.assertTrue(output, output.startsWith("[JOID] The post phase of NodeMousePressedCallback failed: java.lang.IllegalStateException: click"));
	}

	@Test
	public void keepsRunningTheNextCallbacksAfterAFailure() {
		final List<String> runs = new ArrayList<>();
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D).onUpdate(node -> {
			throw new IllegalStateException("update");
		}).onUpdate(node -> runs.add("second"));
		final String output = NodeCallbackObjectTest.capture(rect::onUpdate);
		Assert.assertTrue(output, output.contains("[JOID] The post phase of NodeUpdateCallback failed: java.lang.IllegalStateException: update"));
		Assert.assertEquals(Arrays.asList("second"), runs);
	}

	private static String capture(final Runnable runnable) {
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private static final class SilentCallback implements NodeCallback {}

	private static final class RecordingCallback implements NodeCallback {

		private final List<Object> calls = new ArrayList<>();

		@NodeCallbackMethod(Type.PRE)
		public void pre(final Node node, final InternalContext context, final String text, final int value) {
			this.calls.add(Arrays.asList("pre", node, context, text, value));
		}

		@NodeCallbackMethod(Type.POST)
		public void post(final Node node, final InternalContext context, final String text, final int value) {
			this.calls.add(Arrays.asList("post", node, context, text, value));
		}

	}

	private static final class RecordingPreCallback implements NodeCallback {

		private int calls;

		@NodeCallbackMethod(Type.PRE)
		public void pre(final Node node, final InternalContext context) {
			this.calls++;
		}

	}

	private static final class RecordingPostCallback implements NodeCallback {

		private int calls;

		@NodeCallbackMethod(Type.POST)
		public void post(final Node node, final InternalContext context) {
			this.calls++;
		}

	}

	private static final class FailingCallback implements NodeCallback {

		@NodeCallbackMethod(Type.PRE)
		public void pre(final Node node, final InternalContext context) {
			throw new IllegalStateException("pre");
		}

		@NodeCallbackMethod(Type.POST)
		public void post(final Node node, final InternalContext context) {
			throw new IllegalStateException("post");
		}

	}

}