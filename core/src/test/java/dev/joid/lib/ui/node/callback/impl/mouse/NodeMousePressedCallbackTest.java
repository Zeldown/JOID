package dev.joid.lib.ui.node.callback.impl.mouse;

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
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;

public class NodeMousePressedCallbackTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void runsAfterTheNodeWithoutConsumingTheContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMousePressedCallback<RectNode> callback = (node, mouseX, mouseY, clickType) -> received.addAll(Arrays.asList(node, mouseX, mouseY, clickType));
		final RectNode rect = RectNode.create(0D, 0D, 10D, 10D);
		final InternalContext context = InternalContext.create();
		callback.pre(rect, context, 3D, 4D, ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
		Assert.assertFalse(context.isCancelled());
		callback.post(rect, context, 3D, 4D, ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(rect, 3D, 4D, ClickType.LEFT), received);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void ignoresAConsumedContext() {
		final List<Object> received = new ArrayList<>();
		final NodeMousePressedCallback<RectNode> callback = (node, mouseX, mouseY, clickType) -> received.add(node);
		callback.post(RectNode.create(0D, 0D, 10D, 10D), InternalContext.create(true), 3D, 4D, ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void receivesAPressAnywhereInTheUi() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onMousePressed((node, mouseX, mouseY, clickType) -> received.addAll(Arrays.asList(node, mouseX, mouseY, clickType)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 20D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertEquals(Arrays.asList(rect, 10D, 20D, ClickType.RIGHT), received);
	}

	@Test
	public void ignoresAPressOnAHiddenOrDisabledNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode hidden = RectNode.create(100D, 100D, 200D, 100D).visible(rect -> false).onMousePressed((node, mouseX, mouseY, clickType) -> received.add(node));
		final RectNode child = RectNode.create(0D, 0D, 20D, 20D).onMousePressed((node, mouseX, mouseY, clickType) -> received.add(node));
		hidden.append(child);
		final RectNode disabled = RectNode.create(400D, 100D, 200D, 100D).enabled(rect -> false).onMousePressed((node, mouseX, mouseY, clickType) -> received.add(node));
		this.bridges.open(new NodeUI(hidden, disabled)).frames(30);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
	}

	@Test
	public void letsEveryListenerAndTheClickBehindReceiveThePress() {
		final List<Object> received = new ArrayList<>();
		final RectNode back = RectNode.create(100D, 100D, 200D, 100D).onClick((node, mouseX, mouseY, clickType) -> received.add("click"));
		final RectNode first = RectNode.create(500D, 100D, 20D, 20D).onMousePressed((node, mouseX, mouseY, clickType) -> received.add("first"));
		final RectNode second = RectNode.create(600D, 100D, 20D, 20D).onMousePressed((node, mouseX, mouseY, clickType) -> received.add("second"));
		this.bridges.open(new NodeUI(back, first, second)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("second", "first", "click"), received);
	}

	@Test
	public void receivesAClickOnlyOverTheNode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onClick((node, mouseX, mouseY, clickType) -> received.addAll(Arrays.asList(node, mouseX, mouseY, clickType)));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(10D, 20D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(received.isEmpty());
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.MIDDLE);
		Assert.assertEquals(Arrays.asList(rect, 150D, 160D, ClickType.MIDDLE), received);
	}

	@Test
	public void givesTheClickToTheFrontNodeOnly() {
		final List<Object> received = new ArrayList<>();
		final RectNode back = RectNode.create(100D, 100D, 200D, 100D).onClick((node, mouseX, mouseY, clickType) -> received.add(node));
		final RectNode front = RectNode.create(120D, 120D, 100D, 50D).onClick((node, mouseX, mouseY, clickType) -> received.add(node));
		this.bridges.open(new NodeUI(back, front)).frames(30);
		this.bridges.move(150D, 140D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Collections.singletonList(front), received);
		this.bridges.move(280D, 180D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(front, back), received);
	}

	@Test
	public void runsEveryClickCallbackOfANode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onClick((node, mouseX, mouseY, clickType) -> received.add("first")).onClick((node, mouseX, mouseY, clickType) -> received.add("second"));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("first", "second"), received);
	}

	@Test
	public void runsThePressAndTheClickOfANode() {
		final List<Object> received = new ArrayList<>();
		final RectNode rect = RectNode.create(100D, 100D, 200D, 100D).onClick((node, mouseX, mouseY, clickType) -> received.add("click")).onMousePressed((node, mouseX, mouseY, clickType) -> received.add("press"));
		this.bridges.open(new NodeUI(rect)).frames(30);
		this.bridges.move(150D, 160D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("click", "press"), received);
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