package dev.joid.lib.ui.node.callback.impl.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public class NodeDrawCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeDrawCallback<RectNode> callback = (node, mouseX, mouseY) -> received.addAll(Arrays.asList(node, mouseX, mouseY));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, 3D, 4D);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeDrawCallback<RectNode> callback = (node, mouseX, mouseY) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 3D, 4D);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnEveryFrameWithTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onDraw((node, mouseX, mouseY) -> received.addAll(Arrays.asList(node, mouseX, mouseY)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		received.clear();
		this.bridges.move(900D, 700D).frames(2);
		Assert.assertEquals(Arrays.asList(rect, 900D, 700D, rect, 900D, 700D), received);
	}

	@Test
	public void firesOnceTheNodeDrewItself() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).onDraw((node, mouseX, mouseY) -> received.add(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).size()));
		this.bridges.open(new NodeUI(rect)).frames(30);
		Assert.assertFalse(received.isEmpty());
		Assert.assertFalse(received.contains(0));
	}

	@Test
	public void skipsTheOwnDrawingWhenThePrePhaseConsumesIt() {
		final List<Object> received = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).onDraw(new NodeDrawCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final double mouseX, final double mouseY) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context, final double mouseX, final double mouseY) {
				context.cancel();
			}

		});
		RectNode.create(10D, 10D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).attach(parent);
		this.bridges.open(new NodeUI(parent)).frames(30);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).size());
		Assert.assertTrue(received.isEmpty());
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