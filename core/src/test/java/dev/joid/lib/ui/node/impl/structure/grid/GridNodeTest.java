package dev.joid.lib.ui.node.impl.structure.grid;

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
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;

public class GridNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void fillsARowBeforeStartingTheNext() {
		final GridNode grid = GridNode.create(10D, 20D, 350D, 100D);
		final RectNode[] cells = GridNodeTest.cells(grid, 5);
		this.bridges.open(new NodeUI(grid));
		GridNodeTest.assertPlaced(cells[0], 0D, 0D);
		GridNodeTest.assertPlaced(cells[1], 100D, 0D);
		GridNodeTest.assertPlaced(cells[2], 200D, 0D);
		GridNodeTest.assertPlaced(cells[3], 0D, 50D);
		GridNodeTest.assertPlaced(cells[4], 100D, 50D);
	}

	@Test
	public void spacesTheCellsByTheMargins() {
		final GridNode grid = GridNode.create(0D, 0D, 350D, 100D).horizontalMargin(10D).verticalMargin(5D);
		final RectNode[] cells = GridNodeTest.cells(grid, 4);
		this.bridges.open(new NodeUI(grid));
		GridNodeTest.assertPlaced(cells[1], 110D, 0D);
		GridNodeTest.assertPlaced(cells[2], 220D, 0D);
		GridNodeTest.assertPlaced(cells[3], 0D, 55D);
		Assert.assertEquals(10D, grid.getHorizontalMargin(), 0D);
		Assert.assertEquals(5D, grid.getVerticalMargin(), 0D);
	}

	@Test
	public void usesOneMarginOnBothAxes() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 100D).margin(20D);
		final RectNode[] cells = GridNodeTest.cells(grid, 3);
		this.bridges.open(new NodeUI(grid));
		GridNodeTest.assertPlaced(cells[1], 120D, 0D);
		GridNodeTest.assertPlaced(cells[2], 0D, 70D);
		Assert.assertEquals(20D, grid.getHorizontalMargin(), 0D);
		Assert.assertEquals(20D, grid.getVerticalMargin(), 0D);
	}

	@Test
	public void addsTheOwnOffsetOfEachChild() {
		final RectNode first = RectNode.create(5D, 5D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 50D);
		final GridNode grid = GridNode.create(0D, 0D, 350D, 100D).append(first, second);
		this.bridges.open(new NodeUI(grid));
		GridNodeTest.assertPlaced(first, 5D, 5D);
		GridNodeTest.assertPlaced(second, 100D, 0D);
	}

	@Test
	public void growsToContainItsRows() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D).verticalMargin(5D);
		GridNodeTest.cells(grid, 3);
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(105D, grid.getHeight(), 0D);
		Assert.assertEquals(250D, grid.getWidth(), 0D);
	}

	@Test
	public void keepsItsHeightWhileTheRowsFit() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 200D);
		GridNodeTest.cells(grid, 3);
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(200D, grid.getHeight(), 0D);
	}

	@Test
	public void keepsItsHeightWithAnOverflow() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D).overflow(OverflowProperty.HIDDEN);
		GridNodeTest.cells(grid, 3);
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(60D, grid.getHeight(), 0D);
	}

	@Test
	public void leavesAnEmptyGridAlone() {
		final GridNode grid = GridNode.create(10D, 20D, 250D, 60D);
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(60D, grid.getHeight(), 0D);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void placesAChildAddedAfterTheLayout() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D);
		GridNodeTest.cells(grid, 2);
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(60D, grid.getHeight(), 0D);
		final RectNode added = RectNode.create(0D, 0D, 100D, 50D);
		grid.append(added);
		this.bridges.frame();
		GridNodeTest.assertPlaced(added, 0D, 50D);
		Assert.assertEquals(100D, grid.getHeight(), 0D);
	}

	@Test
	public void movesTheCellsUpOnceAChildIsRemoved() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D);
		final RectNode[] cells = GridNodeTest.cells(grid, 3);
		this.bridges.open(new NodeUI(grid));
		grid.getChildren().remove(cells[0]);
		this.bridges.frame();
		GridNodeTest.assertPlaced(cells[1], 0D, 0D);
		GridNodeTest.assertPlaced(cells[2], 100D, 0D);
	}

	@Test
	public void laysOutItsCellsWhileLoading() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D).wait(node -> false);
		final RectNode[] cells = GridNodeTest.cells(grid, 3);
		this.bridges.open(new NodeUI(grid));
		Assert.assertFalse(grid.isMounted());
		GridNodeTest.assertPlaced(cells[2], 0D, 50D);
		Assert.assertEquals(100D, grid.getHeight(), 0D);
	}

	@Test
	public void drawsEachCellAtItsPlace() {
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F));
		final RectNode second = RectNode.create(0D, 0D, 100D, 50D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode third = RectNode.create(0D, 0D, 100D, 50D).color(new Color(0.4F, 0.6F, 0.2F, 1F));
		this.bridges.open(new NodeUI(GridNode.create(10D, 20D, 250D, 60D).margin(10D).append(first, second, third))).frame();
		final Draw drawn = this.single(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(120D, drawn.getLeft(), 1E-3D);
		Assert.assertEquals(20D, drawn.getTop(), 1E-3D);
		final Draw wrapped = this.single(0.4F, 0.6F, 0.2F);
		Assert.assertEquals(10D, wrapped.getLeft(), 1E-3D);
		Assert.assertEquals(80D, wrapped.getTop(), 1E-3D);
		Assert.assertEquals(130D, wrapped.getBottom(), 1E-3D);
	}

	@Test
	public void returnsItselfFromEachSetter() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D);
		Assert.assertSame(grid, grid.horizontalMargin(1D));
		Assert.assertSame(grid, grid.verticalMargin(2D));
		Assert.assertSame(grid, grid.margin(3D));
	}

	@Test
	public void keepsACellThatFitsExactlyOnItsRow() {
		final RectNode third = RectNode.create(0D, 0D, 100D, 50D);
		this.bridges.open(new NodeUI(GridNode.create(0D, 0D, 300D, 100D).append(RectNode.create(0D, 0D, 100D, 50D), RectNode.create(0D, 0D, 100D, 50D), third)));
		Assert.assertEquals(200D, third.getX(), 0D);
		Assert.assertEquals(0D, third.getY(), 0D);
	}

	@Test
	public void startsTheFirstRowAtTheTop() {
		final RectNode first = RectNode.create(0D, 0D, 100D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 100D, 50D);
		this.bridges.open(new NodeUI(GridNode.create(0D, 0D, 100D, 100D).append(first, second)));
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(50D, second.getY(), 0D);
	}

	@Test
	public void shrinksBackOnceItsCellsFitAgain() {
		final RectNode third = RectNode.create(0D, 0D, 100D, 50D);
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D).append(RectNode.create(0D, 0D, 100D, 50D), RectNode.create(0D, 0D, 100D, 50D), third);
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(100D, grid.getHeight(), 0D);
		grid.getChildren().remove(third);
		this.bridges.frame();
		Assert.assertEquals(60D, grid.getHeight(), 0D);
	}

	@Test
	public void startsANewRowBelowTheTallestCellOfThePreviousOne() {
		final RectNode tall = RectNode.create(0D, 0D, 100D, 80D);
		final RectNode wrapped = RectNode.create(0D, 0D, 100D, 50D);
		this.bridges.open(new NodeUI(GridNode.create(0D, 0D, 250D, 60D).verticalMargin(5D).append(RectNode.create(0D, 0D, 100D, 50D), tall, wrapped)));
		GridNodeTest.assertPlaced(wrapped, 0D, 85D);
	}

	@Test
	public void givesNoCellToAHiddenChild() {
		final RectNode hidden = RectNode.create(0D, 0D, 100D, 50D).visible(node -> false);
		final RectNode third = RectNode.create(0D, 0D, 100D, 50D);
		final GridNode grid = GridNode.create(0D, 0D, 250D, 50D).append(RectNode.create(0D, 0D, 100D, 50D), hidden, third);
		this.bridges.open(new NodeUI(grid));
		GridNodeTest.assertPlaced(third, 100D, 0D);
		Assert.assertEquals(50D, grid.getHeight(), 0D);
	}

	@Test
	public void growsByTheHeightOfEveryRow() {
		final GridNode grid = GridNode.create(0D, 0D, 250D, 60D).verticalMargin(5D).append(RectNode.create(0D, 0D, 100D, 50D), RectNode.create(0D, 0D, 100D, 80D), RectNode.create(0D, 0D, 100D, 20D));
		this.bridges.open(new NodeUI(grid));
		Assert.assertEquals(105D, grid.getHeight(), 0D);
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private static RectNode[] cells(final GridNode grid, final int count) {
		final RectNode[] cells = new RectNode[count];
		for (int i = 0; i < count; i++) {
			cells[i] = RectNode.create(0D, 0D, 100D, 50D).attach(grid);
		}
		return cells;
	}

	private static void assertPlaced(final Node node, final double x, final double y) {
		Assert.assertEquals(x, node.getX(), 0D);
		Assert.assertEquals(y, node.getY(), 0D);
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