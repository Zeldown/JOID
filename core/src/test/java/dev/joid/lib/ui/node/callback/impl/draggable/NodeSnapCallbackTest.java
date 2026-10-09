package dev.joid.lib.ui.node.callback.impl.draggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Phase;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import lombok.NonNull;

public class NodeSnapCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeSnapCallback<RectNode> callback = (node, snapNode) -> received.addAll(Arrays.asList(node, snapNode));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode target = RectNode.create(50D, 50D, 10D, 10D);
		final DispatchContext context = DispatchContext.create();
		callback.pre(rect, context, target);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, target);
		Assert.assertEquals(Arrays.asList(rect, target), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeSnapCallback<RectNode> callback = (node, snapNode) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), DispatchContext.create(true), RectNode.create(50D, 50D, 10D, 10D));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheTargetMovedOnTheNearestSnap() {
		final List<Object> received = new ArrayList<>();
		final RectNode near = RectNode.create(500D, 400D, 200D, 100D);
		final RectNode far = RectNode.create(1600D, 900D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(near, far)).onSnap((node, snapNode) -> received.addAll(Arrays.asList(node, snapNode, node.getTargetDragX(), node.getTargetDragY())));
		this.bridges.open(new NodeUI(near, far, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(450D, 350D).frames(1);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(rect, near, 500D, 400D), received);
	}

	@Test
	public void ignoresAReleaseAwayFromAnOverlapTarget() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(1200D, 700D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(DraggableSnapType.OVERLAP, target)).onSnap((node, snapNode) -> received.add(snapNode));
		this.bridges.open(new NodeUI(target, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals(100D, rect.getTargetDragX(), 0D);
		Assert.assertEquals(100D, rect.getTargetDragY(), 0D);
	}

	@Test
	public void keepsTheDropPositionWhenThePrePhaseConsumesTheSnap() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(1200D, 700D, 200D, 100D);
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free().snap(target)).onSnap(new NodeSnapCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node, final @NonNull Node snapNode) {
				received.add(snapNode);
			}

			@Override
			@NodeCallbackMethod(Phase.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull DispatchContext context, final @NonNull Node snapNode) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(target, rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(400D, 300D).frames(1);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertEquals(350D, rect.getTargetDragX(), 0D);
		Assert.assertEquals(250D, rect.getTargetDragY(), 0D);
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