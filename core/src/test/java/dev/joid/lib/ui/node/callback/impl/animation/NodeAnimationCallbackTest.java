package dev.joid.lib.ui.node.callback.impl.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;

public class NodeAnimationCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeAnimationCallback<RectNode> callback = (node, animator, value) -> received.addAll(Arrays.asList(node, animator, value));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final TweenAnimator animator = TweenAnimator.create();
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, animator, 0.5F);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, animator, 0.5F);
		Assert.assertEquals(Arrays.asList(rect, animator, 0.5F), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeAnimationCallback<RectNode> callback = (node, animator, value) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), TweenAnimator.create(), 0.5F);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnlyWhenTheValueChanges() {
		final List<Object> received = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).animate(animator).onAnimate((node, tween, value) -> received.addAll(Arrays.asList(node, tween, value)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		animator.setValue(0.5F);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(rect, animator, 0.5F), received);
	}

	@Test
	public void followsATweenToItsTarget() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).animate(animator).onAnimate((node, tween, value) -> values.add(value));
		this.bridges.open(new NodeUI(rect)).frames(30);
		animator.sequence(100F, 1F).start();
		this.bridges.frames(20);
		Assert.assertTrue(values.size() > 2);
		for (int i = 1; i < values.size(); i++) {
			Assert.assertTrue(values.get(i) > values.get(i - 1));
		}
		Assert.assertEquals(1F, values.get(values.size() - 1), 0F);
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}