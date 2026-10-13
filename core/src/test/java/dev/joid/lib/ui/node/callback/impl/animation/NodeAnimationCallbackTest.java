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
import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class NodeAnimationCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeAnimationCallback<RectNode> callback = (node, animator, value) -> received.addAll(Arrays.asList(node, animator, value));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final TweenAnimator animator = TweenAnimator.create();
		final DispatchContext context = DispatchContext.create();
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
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true), TweenAnimator.create(), 0.5F);
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
		new AnimatorUI(animator.sequence(100F, 1F)).load(1920D, 1080D);
		this.bridges.frames(20);
		Assert.assertTrue(values.size() > 2);
		for (int i = 1; i < values.size(); i++) {
			Assert.assertTrue(values.get(i) > values.get(i - 1));
		}
		Assert.assertEquals(1F, values.get(values.size() - 1), 0F);
	}

	@AllArgsConstructor
	@UIDataDebug(profiler = false, hotreload = false)
	public static final class AnimatorUI extends UI {

		private final TweenAnimator animator;

		@Override
		public void init() {
			this.animator.start();
		}

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