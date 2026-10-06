package dev.joid.lib.ui.node.impl.structure.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;
import dev.joid.lib.utils.click.ClickType;
import lombok.AllArgsConstructor;

public class SelectorNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<Node> changes = new ArrayList<>();

	private Selector selector;
	private RectNode first;
	private RectNode second;
	private RectNode third;

	@Before
	public void createAnOptionList() {
		this.first = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.1F, 0.3F, 0.5F, 1F));
		this.second = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.3F, 0.5F, 0.7F, 1F));
		this.third = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.5F, 0.7F, 0.9F, 1F));
		this.selector = new Selector().onChange((node, selected) -> this.changes.add(selected)).append(this.first, this.second, this.third);
	}

	@Test
	public void showsItsFirstOptionClosedByDefault() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		Assert.assertSame(this.first, this.selector.getSelected());
		Assert.assertTrue(this.selector.isSelected(this.first));
		Assert.assertFalse(this.selector.isActive());
		Assert.assertSame(SelectorDirection.DOWN, this.selector.getDirection());
		Assert.assertEquals(40D, this.selector.getHeight(), 0D);
		this.assertDrawn(0.1F, 0.3F, 0.5F, 100D, 140D);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).isEmpty());
		Assert.assertEquals(200D, this.first.getWidth(), 0D);
	}

	@Test
	public void opensDownwardsOnAClickOnItsSelection() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.click(150D, 120D);
		Assert.assertTrue(this.selector.isActive());
		Assert.assertEquals(120D, this.selector.getHeight(), 0D);
		this.assertDrawn(0.3F, 0.5F, 0.7F, 140D, 180D);
		this.assertDrawn(0.5F, 0.7F, 0.9F, 180D, 220D);
	}

	@Test
	public void opensUpwards() {
		Assert.assertSame(this.selector, this.selector.direction(SelectorDirection.UP).active(true));
		this.bridges.open(new NodeUI(this.selector)).frames(2);
		Assert.assertEquals(40D, this.selector.getHeight(), 0D);
		this.assertDrawn(0.3F, 0.5F, 0.7F, 60D, 100D);
		this.assertDrawn(0.5F, 0.7F, 0.9F, 20D, 60D);
	}

	@Test
	public void picksTheClickedOption() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(150D, 200D);
		Assert.assertSame(this.third, this.selector.getSelected());
		Assert.assertFalse(this.selector.isActive());
		Assert.assertEquals(Arrays.asList(this.third), this.changes);
		this.assertDrawn(0.5F, 0.7F, 0.9F, 100D, 140D);
	}

	@Test
	public void closesOnAClickOnItsSelection() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(150D, 120D);
		Assert.assertFalse(this.selector.isActive());
		Assert.assertSame(this.first, this.selector.getSelected());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void closesOnAClickBesideIt() {
		this.bridges.open(new NodeUI(this.selector.active(true))).frames(2);
		this.click(1000D, 1000D);
		Assert.assertFalse(this.selector.isActive());
		Assert.assertTrue(this.changes.isEmpty());
	}

	@Test
	public void staysClosedOnAClickBesideIt() {
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.click(1000D, 1000D);
		Assert.assertFalse(this.selector.isActive());
	}

	@Test
	public void showsAChosenOption() {
		Assert.assertSame(this.selector, this.selector.selected(this.second));
		this.bridges.open(new NodeUI(this.selector)).frame();
		this.assertDrawn(0.3F, 0.5F, 0.7F, 100D, 140D);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.1F, 0.3F, 0.5F).isEmpty());
	}

	@Test
	public void drawsNothingWithoutOptions() {
		final Selector empty = new Selector();
		this.bridges.open(new NodeUI(empty)).frame();
		this.click(150D, 120D);
		Assert.assertNull(empty.getSelected());
		Assert.assertFalse(empty.isActive());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void opensDownwardsOnlyInItsDownDirection() {
		Assert.assertTrue(SelectorDirection.DOWN.isDown());
		Assert.assertFalse(SelectorDirection.UP.isDown());
	}

	private void click(final double x, final double y) {
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.frame();
	}

	private void assertDrawn(final float red, final float green, final float blue, final double top, final double bottom) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(100D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(300D, draws.get(0).getRight(), 1E-3D);
		Assert.assertEquals(top, draws.get(0).getTop(), 1E-3D);
		Assert.assertEquals(bottom, draws.get(0).getBottom(), 1E-3D);
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class Selector extends SelectorNode {

		public Selector() {
			super(100D, 100D, 200D, 40D);
		}

		@Override
		public void drawBackground(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.2F, 0.4F, 0.6F, 1F));
		}

	}

}