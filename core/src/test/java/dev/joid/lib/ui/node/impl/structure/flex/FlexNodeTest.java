package dev.joid.lib.ui.node.impl.structure.flex;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode.FlexDirection;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.Signal;

public class FlexNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void stacksTheChildrenOfAColumn() {
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 30D);
		final RectNode third = RectNode.create(0D, 0D, 100D, 40D);
		final FlexNode flex = FlexNode.vertical(10D, 20D, 300D).append(first, second, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(50D, second.getY(), 0D);
		Assert.assertEquals(80D, third.getY(), 0D);
		Assert.assertEquals(300D, flex.getWidth(), 0D);
		Assert.assertEquals(120D, flex.getHeight(), 0D);
	}

	@Test
	public void spacesTheChildrenOfAColumnByTheMargin() {
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 30D);
		final RectNode third = RectNode.create(0D, 0D, 100D, 40D);
		final FlexNode flex = FlexNode.vertical(10D, 20D, 300D).margin(10D).append(first, second, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(60D, second.getY(), 0D);
		Assert.assertEquals(100D, third.getY(), 0D);
		Assert.assertEquals(140D, flex.getHeight(), 0D);
	}

	@Test
	public void linesUpTheChildrenOfARow() {
		final RectNode first = RectNode.create(0D, 0D, 50D, 100D);
		final RectNode second = RectNode.create(0D, 0D, 30D, 100D);
		final RectNode third = RectNode.create(0D, 0D, 40D, 100D);
		final FlexNode flex = FlexNode.horizontal(10D, 20D, 100D).margin(8D).append(first, second, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(0D, first.getX(), 0D);
		Assert.assertEquals(58D, second.getX(), 0D);
		Assert.assertEquals(96D, third.getX(), 0D);
		Assert.assertEquals(136D, flex.getWidth(), 0D);
		Assert.assertEquals(100D, flex.getHeight(), 0D);
	}

	@Test
	public void addsTheOwnOffsetOfEachChild() {
		final RectNode first = RectNode.create(15D, 5D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 30D);
		final RectNode wide = RectNode.create(5D, 0D, 50D, 100D);
		this.bridges.open(new NodeUI(FlexNode.vertical(0D, 0D, 300D).append(first, second), FlexNode.horizontal(0D, 200D, 100D).append(wide)));
		Assert.assertEquals(5D, first.getY(), 0D);
		Assert.assertEquals(15D, first.getX(), 0D);
		Assert.assertEquals(50D, second.getY(), 0D);
		Assert.assertEquals(5D, wide.getX(), 0D);
	}

	@Test
	public void givesNoRoomToAHiddenChild() {
		final boolean[] shown = {false};
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D);
		final RectNode hidden = RectNode.create(0D, 0D, 100D, 30D).visible(node -> shown[0]);
		final RectNode third = RectNode.create(0D, 0D, 100D, 40D);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).margin(10D).append(first, hidden, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(60D, hidden.getY(), 0D);
		Assert.assertEquals(60D, third.getY(), 0D);
		Assert.assertEquals(100D, flex.getHeight(), 0D);
	}

	@Test
	public void makesRoomForAChildShownAgain() {
		final boolean[] shown = {false};
		final RectNode hidden = RectNode.create(0D, 0D, 30D, 100D).visible(node -> shown[0]);
		final RectNode last = RectNode.create(0D, 0D, 40D, 100D);
		final FlexNode flex = FlexNode.horizontal(0D, 0D, 100D).margin(10D).append(RectNode.create(0D, 0D, 50D, 100D), hidden, last);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(60D, last.getX(), 0D);
		shown[0] = true;
		this.bridges.frame();
		Assert.assertEquals(100D, last.getX(), 0D);
		Assert.assertEquals(140D, flex.getWidth(), 0D);
	}

	@Test
	public void alignsTheChildrenOfAColumnAcrossItsWidth() {
		final RectNode start = RectNode.create(20D, 0D, 100D, 50D);
		final RectNode center = RectNode.create(20D, 0D, 100D, 50D);
		final RectNode end = RectNode.create(20D, 0D, 100D, 50D);
		this.bridges.open(new NodeUI(FlexNode.vertical(0D, 0D, 300D).align(Align.START).append(start), FlexNode.vertical(0D, 100D, 300D).align(Align.CENTER).append(center), FlexNode.vertical(0D, 200D, 300D).align(Align.END).append(end)));
		Assert.assertEquals(0D, start.getX(), 0D);
		Assert.assertEquals(100D, center.getX(), 0D);
		Assert.assertEquals(200D, end.getX(), 0D);
	}

	@Test
	public void alignsTheChildrenOfARowAcrossItsHeight() {
		final RectNode start = RectNode.create(0D, 20D, 50D, 40D);
		final RectNode center = RectNode.create(0D, 20D, 50D, 40D);
		final RectNode end = RectNode.create(0D, 20D, 50D, 40D);
		this.bridges.open(new NodeUI(FlexNode.horizontal(0D, 0D, 100D).align(Align.START).append(start), FlexNode.horizontal(0D, 200D, 100D).align(Align.CENTER).append(center), FlexNode.horizontal(0D, 400D, 100D).align(Align.END).append(end)));
		Assert.assertEquals(0D, start.getY(), 0D);
		Assert.assertEquals(30D, center.getY(), 0D);
		Assert.assertEquals(60D, end.getY(), 0D);
	}

	@Test
	public void leavesTheCrossAxisAloneWithoutAlignment() {
		final RectNode column = RectNode.create(20D, 0D, 100D, 50D);
		final RectNode row = RectNode.create(0D, 20D, 50D, 40D);
		this.bridges.open(new NodeUI(FlexNode.vertical(0D, 0D, 300D).append(column), FlexNode.horizontal(0D, 200D, 100D).append(row)));
		Assert.assertEquals(20D, column.getX(), 0D);
		Assert.assertEquals(20D, row.getY(), 0D);
	}

	@Test
	public void shrinksToNothingWithoutChildren() {
		final FlexNode column = FlexNode.vertical(0D, 0D, 300D).margin(10D);
		final FlexNode row = FlexNode.horizontal(0D, 0D, 100D).margin(10D);
		this.bridges.open(new NodeUI(column, row));
		Assert.assertEquals(0D, column.getHeight(), 0D);
		Assert.assertEquals(300D, column.getWidth(), 0D);
		Assert.assertEquals(0D, row.getWidth(), 0D);
		Assert.assertEquals(100D, row.getHeight(), 0D);
	}

	@Test
	public void placesAChildAddedAfterTheLayout() {
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).margin(10D).append(RectNode.create(0D, 0D, 100D, 50D));
		this.bridges.open(new NodeUI(flex));
		final RectNode added = RectNode.create(0D, 0D, 100D, 30D);
		flex.append(added);
		this.bridges.frame();
		Assert.assertEquals(60D, added.getY(), 0D);
		Assert.assertEquals(90D, flex.getHeight(), 0D);
	}

	@Test
	public void closesTheGapOfARemovedChild() {
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 30D);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).margin(10D).append(first, second);
		this.bridges.open(new NodeUI(flex));
		flex.getChildren().remove(first);
		this.bridges.frame();
		Assert.assertEquals(0D, second.getY(), 0D);
		Assert.assertEquals(30D, flex.getHeight(), 0D);
	}

	@Test
	public void laysOutTheChildrenRebuiltByAWatchedBody() {
		final Signal<Integer> count = new Signal<>(1);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).margin(10D).watch(count, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY).body(node -> {
			for (int i = 0; i < count.get(); i++) {
				RectNode.create(0D, 0D, 100D, 50D).attach(node);
			}
		});
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(50D, flex.getHeight(), 0D);
		count.set(3);
		this.bridges.frame();
		Assert.assertEquals(3, flex.getChildren().size());
		Assert.assertEquals(120D, flex.getChildren().get(2).getY(), 0D);
		Assert.assertEquals(170D, flex.getHeight(), 0D);
	}

	@Test
	public void laysOutTheChildrenAgainOnAReload() {
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 30D);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).append(first, second);
		this.bridges.open(new NodeUI(flex));
		first.height(70D);
		flex.margin(5D).reload();
		Assert.assertEquals(75D, second.getY(), 0D);
		Assert.assertEquals(105D, flex.getHeight(), 0D);
	}

	@Test
	public void drawsEachChildOnItsSlot() {
		final FlexNode flex = FlexNode.vertical(10D, 20D, 300D).margin(10D).append(RectNode.create(0D, 0D, 100D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)), RectNode.create(0D, 0D, 100D, 30D).color(new Color(0.6F, 0.4F, 0.2F, 1F)));
		this.bridges.open(new NodeUI(flex)).frame();
		final Draw first = this.single(0.2F, 0.4F, 0.6F);
		final Draw second = this.single(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(10D, first.getLeft(), 1E-3D);
		Assert.assertEquals(20D, first.getTop(), 1E-3D);
		Assert.assertEquals(70D, first.getBottom(), 1E-3D);
		Assert.assertEquals(10D, second.getLeft(), 1E-3D);
		Assert.assertEquals(80D, second.getTop(), 1E-3D);
		Assert.assertEquals(110D, second.getBottom(), 1E-3D);
	}

	@Test
	public void drawsNothingItselfWhileLoading() {
		final FlexNode flex = FlexNode.vertical(10D, 20D, 300D).margin(10D).wait(node -> false).append(RectNode.create(0D, 0D, 100D, 50D), RectNode.create(0D, 0D, 100D, 30D));
		this.bridges.open(new NodeUI(flex)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(20D, draws.get(0).getTop(), 1E-3D);
		Assert.assertEquals(80D, draws.get(1).getTop(), 1E-3D);
		Assert.assertEquals(90D, flex.getHeight(), 0D);
	}

	@Test
	public void turnsIntoARowWithTheDirection() {
		final RectNode first = RectNode.create(0D, 0D, 50D, 100D);
		final RectNode second = RectNode.create(0D, 0D, 30D, 100D);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).direction(FlexDirection.ROW).append(first, second);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(50D, second.getX(), 0D);
		Assert.assertEquals(0D, second.getY(), 0D);
		Assert.assertEquals(80D, flex.getWidth(), 0D);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullDirection() {
		FlexNode.vertical(0D, 0D, 300D).direction((FlexDirection) null);
	}

	@Test
	public void readsItsSettings() {
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D);
		Assert.assertEquals(0D, flex.getMargin(), 0D);
		Assert.assertNull(flex.getAlign());
		Assert.assertSame(FlexDirection.COLUMN, flex.getDirection());
		flex.margin(10D).align(Align.END).direction(FlexDirection.ROW);
		Assert.assertEquals(10D, flex.getMargin(), 0D);
		Assert.assertSame(Align.END, flex.getAlign());
		Assert.assertSame(FlexDirection.ROW, flex.getDirection());
	}

	@Test
	public void returnsItselfFromEachSetter() {
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D);
		Assert.assertSame(flex, flex.margin(10D));
		Assert.assertSame(flex, flex.align(Align.CENTER));
		Assert.assertSame(flex, flex.align((Align) null));
		Assert.assertSame(flex, flex.direction(FlexDirection.ROW));
	}

	@Test
	public void offersAColumnAndARow() {
		Assert.assertArrayEquals(new FlexDirection[] {FlexDirection.COLUMN, FlexDirection.ROW}, FlexDirection.values());
		Assert.assertSame(FlexDirection.ROW, FlexDirection.valueOf("ROW"));
	}

	@Test
	public void putsTheChildrenOnOneLineOnceTurnedIntoARow() {
		final RectNode second = RectNode.create(0D, 0D, 50D, 50D);
		final FlexNode flex = FlexNode.vertical(0D, 0D, 300D).append(RectNode.create(0D, 0D, 50D, 50D), second);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(50D, second.getY(), 0D);
		flex.direction(FlexDirection.ROW);
		this.bridges.frame();
		Assert.assertEquals(50D, second.getX(), 0D);
		Assert.assertEquals(0D, second.getY(), 0D);
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	@UIData(background = false)
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