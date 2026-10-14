package dev.joid.lib.ui.event.impl.node;

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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import lombok.AllArgsConstructor;

public class NodeAnimateEventTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void firesOnlyWhenTheValueChanges() {
		final List<Object> received = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).animate(animator).onAnimate(event -> received.addAll(Arrays.asList(event.getNode(), event.getAnimator(), event.getValue())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertTrue(received.isEmpty());
		animator.value(0.5F);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(rect, animator, 0.5F), received);
	}

	@Test
	public void followsATweenToItsTarget() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).animate(animator).onAnimate(event -> values.add(event.getValue()));
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