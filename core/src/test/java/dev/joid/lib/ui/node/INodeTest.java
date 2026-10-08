package dev.joid.lib.ui.node;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.list.IndexedLinkedList;
import dev.joid.lib.utils.list.IndexedList;
import dev.joid.lib.utils.list.RecursiveIndexedElement;

public class INodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void handlesNoEventByDefault() {
		final INode node = new BareNode();
		final InternalContext context = InternalContext.create();
		node.mousePressed(0D, 0D, ClickType.LEFT, context);
		node.mouseDragged(0D, 0D, ClickType.LEFT, 16L, context);
		node.mouseReleased(0D, 0D, ClickType.LEFT, context);
		node.mouseScroll(0D, 0D, 1D, context);
		node.keyPressed('a', Key.A, context);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void drawsNothingByDefault() {
		final INode node = new BareNode();
		node.init(new EmptyUI());
		node.draw(0D, 0D);
		node.drawSkeleton(0D, 0D);
		node.update();
		node.detach();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	public static final class BareNode implements INode {

		@Override
		public int getIndex() {
			return 0;
		}

		@Override
		public IndexedList<? extends RecursiveIndexedElement> getChildren() {
			return new IndexedLinkedList<>();
		}

	}

	public static final class EmptyUI extends UI {}

}