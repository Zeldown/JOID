package dev.joid.lib.ui.node.callback.impl.draggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public class NodeDragCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeAndConsumesTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeDragCallback<RectNode> callback = received::add;
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context);
		Assert.assertEquals(Collections.singletonList(rect), received);
		Assert.assertTrue(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeDragCallback<RectNode> callback = received::add;
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true));
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheDragStarted() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragStart(node -> received.addAll(Arrays.asList(node, node.isDragging())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(rect, true), received);
	}

	@Test
	public void waitsForALeftPressOverTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragStart(received::add);
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 10D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnEveryMoveOnceTheTargetFollowedTheMouse() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDrag(node -> received.addAll(Arrays.asList(node.getTargetDragX(), node.getTargetDragY())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(400D, 300D).frames(1);
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 16L);
		this.bridges.move(500D, 350D).frames(1);
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 16L);
		Assert.assertEquals(Arrays.asList(350D, 250D, 450D, 300D), received);
	}

	@Test
	public void ignoresAMoveWithoutDrag() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDrag(received::add);
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 16L);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void firesOnceTheDragEnded() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragEnd(node -> received.addAll(Arrays.asList(node, node.isDragging())));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(rect, false), received);
	}

	@Test
	public void keepsTheNodeStillWhenThePrePhaseConsumesTheStart() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDragStart(new NodeDragCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node) {
				received.add(node);
			}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertFalse(rect.isDragging());
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void keepsTheTargetWhenThePrePhaseConsumesAMove() {
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).draggable(DraggableProperty.free()).onDrag(new NodeDragCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode node) {}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull RectNode node, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(400D, 300D).frames(1);
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 16L);
		Assert.assertEquals(100D, rect.getTargetDragX(), 0D);
		Assert.assertEquals(100D, rect.getTargetDragY(), 0D);
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