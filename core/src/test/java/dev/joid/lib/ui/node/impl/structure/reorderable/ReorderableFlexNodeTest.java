package dev.joid.lib.ui.node.impl.structure.reorderable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import dev.joid.lib.ui.node.callback.NodeCallbackMethod;
import dev.joid.lib.ui.node.callback.NodeCallbackMethod.Type;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode.FlexDirection;
import dev.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderStartCallback;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;

import lombok.NonNull;

public class ReorderableFlexNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void stacksTheChildrenOfAColumn() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(60D, second.getY(), 0D);
		Assert.assertEquals(120D, third.getY(), 0D);
		Assert.assertEquals(200D, flex.getWidth(), 0D);
		Assert.assertEquals(170D, flex.getHeight(), 0D);
		Assert.assertSame(FlexDirection.COLUMN, flex.getDirection());
	}

	@Test
	public void linesUpTheChildrenOfARow() {
		final RectNode first = RectNode.create(0D, 0D, 80D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 80D, 50D);
		final RectNode third = RectNode.create(0D, 0D, 80D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.horizontal(100D, 100D, 50D).margin(10D);
		flex.append(first, second, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(0D, first.getX(), 0D);
		Assert.assertEquals(90D, second.getX(), 0D);
		Assert.assertEquals(180D, third.getX(), 0D);
		Assert.assertEquals(260D, flex.getWidth(), 0D);
		Assert.assertEquals(50D, flex.getHeight(), 0D);
		Assert.assertSame(FlexDirection.ROW, flex.getDirection());
	}

	@Test
	public void addsTheOwnOffsetOfEachChild() {
		final RectNode first = RectNode.create(15D, 5D, 200D, 50D);
		final RectNode second = ReorderableFlexNodeTest.item();
		this.bridges.open(new NodeUI(ReorderableFlexNodeTest.column(100D, 100D, first, second)));
		Assert.assertEquals(5D, first.getY(), 0D);
		Assert.assertEquals(15D, first.getX(), 0D);
		Assert.assertEquals(60D, second.getY(), 0D);
	}

	@Test
	public void givesNoRoomToAHiddenChild() {
		final RectNode hidden = ReorderableFlexNodeTest.item().visible(node -> false);
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item(), hidden, third);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(60D, hidden.getY(), 0D);
		Assert.assertEquals(60D, third.getY(), 0D);
		Assert.assertEquals(110D, flex.getHeight(), 0D);
	}

	@Test
	public void alignsTheChildrenOfAColumnAcrossItsWidth() {
		final RectNode start = RectNode.create(20D, 0D, 100D, 50D);
		final RectNode center = RectNode.create(20D, 0D, 100D, 50D);
		final RectNode end = RectNode.create(20D, 0D, 100D, 50D);
		this.bridges.open(new NodeUI(ReorderableFlexNode.vertical(0D, 0D, 300D).align(Align.START).append(start), ReorderableFlexNode.vertical(0D, 100D, 300D).align(Align.CENTER).append(center), ReorderableFlexNode.vertical(0D, 200D, 300D).align(Align.END).append(end)));
		Assert.assertEquals(0D, start.getX(), 0D);
		Assert.assertEquals(100D, center.getX(), 0D);
		Assert.assertEquals(200D, end.getX(), 0D);
	}

	@Test
	public void alignsTheChildrenOfARowAcrossItsHeight() {
		final RectNode start = RectNode.create(0D, 20D, 50D, 40D);
		final RectNode center = RectNode.create(0D, 20D, 50D, 40D);
		final RectNode end = RectNode.create(0D, 20D, 50D, 40D);
		this.bridges.open(new NodeUI(ReorderableFlexNode.horizontal(0D, 0D, 100D).align(Align.START).append(start), ReorderableFlexNode.horizontal(0D, 200D, 100D).align(Align.CENTER).append(center), ReorderableFlexNode.horizontal(0D, 400D, 100D).align(Align.END).append(end)));
		Assert.assertEquals(0D, start.getY(), 0D);
		Assert.assertEquals(30D, center.getY(), 0D);
		Assert.assertEquals(60D, end.getY(), 0D);
	}

	@Test
	public void leavesTheCrossAxisAloneWithoutAlignment() {
		final RectNode column = RectNode.create(20D, 0D, 100D, 50D);
		final RectNode row = RectNode.create(0D, 20D, 50D, 40D);
		this.bridges.open(new NodeUI(ReorderableFlexNode.vertical(0D, 0D, 300D).append(column), ReorderableFlexNode.horizontal(0D, 200D, 100D).append(row)));
		Assert.assertEquals(20D, column.getX(), 0D);
		Assert.assertEquals(20D, row.getY(), 0D);
	}

	@Test
	public void shrinksToNothingWithoutChildren() {
		final ReorderableFlexNode column = ReorderableFlexNode.vertical(0D, 0D, 300D).margin(10D);
		final ReorderableFlexNode row = ReorderableFlexNode.horizontal(0D, 0D, 100D).margin(10D);
		this.bridges.open(new NodeUI(column, row));
		Assert.assertEquals(0D, column.getHeight(), 0D);
		Assert.assertEquals(0D, row.getWidth(), 0D);
	}

	@Test
	public void placesAChildAddedAfterTheLayout() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex));
		final RectNode added = ReorderableFlexNodeTest.item();
		flex.append(added);
		this.bridges.frame();
		Assert.assertEquals(60D, added.getY(), 0D);
		Assert.assertEquals(110D, flex.getHeight(), 0D);
	}

	@Test
	public void turnsIntoARowWithTheDirection() {
		final RectNode first = RectNode.create(0D, 0D, 80D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 80D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second).direction(FlexDirection.ROW);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(90D, second.getX(), 0D);
		Assert.assertEquals(0D, second.getY(), 0D);
		Assert.assertEquals(170D, flex.getWidth(), 0D);
	}

	@Test
	public void keepsItsSettings() {
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(0D, 0D, 300D);
		Assert.assertEquals(0D, flex.getMargin(), 0D);
		Assert.assertNull(flex.getAlign());
		Assert.assertTrue(flex.isAutoDrag());
		Assert.assertSame(flex, flex.margin(10D));
		Assert.assertSame(flex, flex.align(Align.END));
		Assert.assertSame(flex, flex.auto(false));
		Assert.assertSame(flex, flex.direction(FlexDirection.ROW));
		Assert.assertEquals(10D, flex.getMargin(), 0D);
		Assert.assertSame(Align.END, flex.getAlign());
		Assert.assertFalse(flex.isAutoDrag());
		Assert.assertSame(FlexDirection.ROW, flex.getDirection());
	}

	@Test
	public void laysOutItsChildrenWhileLoading() {
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item(), second).wait(node -> false);
		this.bridges.open(new NodeUI(flex));
		Assert.assertFalse(flex.isMounted());
		Assert.assertEquals(60D, second.getY(), 0D);
		Assert.assertEquals(110D, flex.getHeight(), 0D);
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullDirection() {
		ReorderableFlexNode.vertical(0D, 0D, 300D).direction(null);
	}

	@Test
	public void findsTheIndexOfAChild() {
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item(), second);
		Assert.assertEquals(1, flex.getChildIndex(second));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesTheIndexOfAStranger() {
		ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item()).getChildIndex(ReorderableFlexNodeTest.item());
	}

	@Test
	public void startsADragOnALeftPressOverAChild() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertTrue(flex.isDragging(first));
		Assert.assertSame(first, flex.getReorderedNode());
		Assert.assertEquals(0, flex.getInitialIndex());
		Assert.assertEquals(0, flex.getCurrentIndex());
		Assert.assertFalse(flex.isReleasing());
		Assert.assertEquals(150D, flex.getDragStartMouseX(), 0D);
		Assert.assertEquals(120D, flex.getDragStartMouseY(), 0D);
		Assert.assertEquals(Integer.MAX_VALUE, first.getZindex());
	}

	@Test
	public void consumesThePressThatStartsADrag() {
		final List<Object> clicks = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 400D).onClick((node, mouseX, mouseY, clickType) -> clicks.add(mouseY));
		ReorderableFlexNodeTest.column(0D, 0D, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item()).attach(parent);
		this.bridges.open(new NodeUI(parent));
		this.press(150D, 120D);
		Assert.assertTrue(clicks.isEmpty());
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.press(150D, 400D);
		Assert.assertEquals(Collections.singletonList(400D), clicks);
	}

	@Test
	public void ignoresARightPress() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex)).move(150D, 120D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertNull(flex.getReorderedNode());
	}

	@Test
	public void ignoresAPressBesideTheChildren() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 400D);
		Assert.assertNull(flex.getReorderedNode());
	}

	@Test
	public void ignoresPressesWithoutAutoDrag() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item()).auto(false);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertNull(flex.getReorderedNode());
	}

	@Test
	public void ignoresPressesWhileDisabled() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item()).enabled(node -> false);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertNull(flex.getReorderedNode());
	}

	@Test
	public void ignoresAPressConsumedByTheChild() {
		final List<Object> clicks = new ArrayList<>();
		final RectNode first = ReorderableFlexNodeTest.item().onClick((node, mouseX, mouseY, clickType) -> clicks.add(node));
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertEquals(Collections.singletonList(first), clicks);
		Assert.assertNull(flex.getReorderedNode());
	}

	@Test
	public void followsTheMouseAlongTheMainAxis() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(170D, 150D).frames(60);
		Assert.assertEquals(30D, first.getY(), 0D);
		Assert.assertEquals(0D, first.getX(), 0D);
		Assert.assertEquals(30D, flex.getDraggedCurrent(), 0D);
		Assert.assertEquals(20D, flex.getDragOffset(), 0D);
	}

	@Test
	public void keepsTheDraggedChildInsideTheList() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(150D, 1000D).frames(60);
		Assert.assertEquals(120D, first.getY(), 0D);
		Assert.assertEquals(2, flex.getCurrentIndex());
		this.bridges.move(150D, -500D).frames(60);
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(0, flex.getCurrentIndex());
	}

	@Test
	public void makesRoomForTheDraggedChild() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second, third);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(150D, 150D).frames(60);
		Assert.assertEquals(1, flex.getCurrentIndex());
		Assert.assertEquals(Arrays.asList(second, first, third), flex.getLogicalOrder());
		Assert.assertEquals(0D, second.getY(), 0D);
		Assert.assertEquals(120D, third.getY(), 0D);
		Assert.assertEquals(170D, flex.getHeight(), 0D);
	}

	@Test
	public void takesTheFirstSlotOnceAChildPassesHalfOfIt() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second, third);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 180D);
		this.bridges.move(150D, 145D).frames(60);
		Assert.assertEquals(0, flex.getCurrentIndex());
		Assert.assertEquals(Arrays.asList(second, first, third), flex.getLogicalOrder());
	}

	@Test
	public void keepsItsSlotUntilAChildPassesHalfOfTheNextOne() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(150D, 145D).frames(60);
		Assert.assertEquals(0, flex.getCurrentIndex());
		Assert.assertEquals(Arrays.asList(first, second), flex.getLogicalOrder());
	}

	@Test
	public void dropsTheChildOnItsNewSlot() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second, third);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(150D, 150D).frames(60);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertTrue(flex.isReleasing());
		Assert.assertTrue(flex.isDragging(first));
		this.bridges.frames(60);
		Assert.assertFalse(flex.isReleasing());
		Assert.assertNull(flex.getReorderedNode());
		Assert.assertEquals(Arrays.asList(second, first, third), flex.getChildren().ordered());
		Assert.assertEquals(60D, first.getY(), 0D);
		Assert.assertEquals(1, flex.getChildIndex(first));
		Assert.assertTrue(flex.getLogicalOrder().isEmpty());
		Assert.assertTrue(flex.getChildCurrent().isEmpty());
	}

	@Test
	public void dropsTheDraggedChildWhenDetached() {
		final List<Integer> ends = new ArrayList<>();
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second, third).onReorderEnd((node, child, oldIndex, newIndex) -> ends.addAll(Arrays.asList(oldIndex, newIndex)));
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(150D, 150D).frames(60);
		flex.onDetach();
		Assert.assertNull(flex.getReorderedNode());
		Assert.assertFalse(flex.isReleasing());
		Assert.assertEquals(Arrays.asList(second, first, third), flex.getChildren().ordered());
		Assert.assertEquals(Arrays.asList(0, 1), ends);
	}

	@Test
	public void endsTheReorderOfARemovedChild() {
		final List<Integer> ends = new ArrayList<>();
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final int zindex = first.getZindex();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second).onReorderEnd((node, child, oldIndex, newIndex) -> ends.addAll(Arrays.asList(oldIndex, newIndex)));
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertTrue(flex.isDragging(first));
		flex.remove(first);
		this.bridges.frame();
		Assert.assertNull(flex.getReorderedNode());
		Assert.assertTrue(flex.getLogicalOrder().isEmpty());
		Assert.assertEquals(Arrays.asList(second), flex.getChildren().ordered());
		Assert.assertEquals(zindex, first.getZindex());
		Assert.assertEquals(Arrays.asList(0, -1), ends);
	}

	@Test
	public void putsTheChildBackWhenReleasedInPlace() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(10);
		Assert.assertNull(flex.getReorderedNode());
		Assert.assertEquals(Arrays.asList(first, second), flex.getChildren().ordered());
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(60D, second.getY(), 0D);
	}

	@Test
	public void drawsTheChildrenWhereTheyLand() {
		final RectNode first = ReorderableFlexNodeTest.item().color(new Color(0.2F, 0.4F, 0.6F, 1F));
		final RectNode second = ReorderableFlexNodeTest.item().color(new Color(0.6F, 0.4F, 0.2F, 1F));
		this.bridges.open(new NodeUI(ReorderableFlexNodeTest.column(100D, 100D, first, second)));
		this.press(150D, 120D);
		this.bridges.move(150D, 200D).frames(60);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		final Draw dropped = this.single(0.2F, 0.4F, 0.6F);
		final Draw risen = this.single(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(160D, dropped.getTop(), 1E-3D);
		Assert.assertEquals(210D, dropped.getBottom(), 1E-3D);
		Assert.assertEquals(100D, risen.getTop(), 1E-3D);
		Assert.assertEquals(100D, risen.getLeft(), 1E-3D);
	}

	@Test
	public void reordersARow() {
		final RectNode first = RectNode.create(0D, 0D, 80D, 50D);
		final RectNode second = RectNode.create(0D, 0D, 80D, 50D);
		final RectNode third = RectNode.create(0D, 0D, 80D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.horizontal(100D, 100D, 50D).margin(10D);
		flex.append(first, second, third);
		this.bridges.open(new NodeUI(flex));
		this.press(110D, 120D);
		this.bridges.move(170D, 140D).frames(60);
		Assert.assertEquals(60D, first.getX(), 0D);
		Assert.assertEquals(0D, first.getY(), 0D);
		Assert.assertEquals(0D, second.getX(), 0D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(second, first, third), flex.getChildren().ordered());
		Assert.assertEquals(90D, first.getX(), 0D);
	}

	@Test
	public void skipsTheHiddenChildrenWhileDragging() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode hidden = ReorderableFlexNodeTest.item().visible(node -> false);
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, hidden, third);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 170D);
		Assert.assertTrue(flex.isDragging(third));
		this.bridges.move(150D, 110D).frames(60);
		Assert.assertEquals(0, flex.getCurrentIndex());
		Assert.assertEquals(60D, first.getY(), 0D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(third, first, hidden), flex.getChildren().ordered());
		Assert.assertEquals(0D, third.getY(), 0D);
		Assert.assertEquals(110D, flex.getHeight(), 0D);
	}

	@Test
	public void putsTheChildBackBehindAHiddenOne() {
		final RectNode hidden = ReorderableFlexNodeTest.item().visible(node -> false);
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, hidden, first, second);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 170D);
		Assert.assertTrue(flex.isDragging(second));
		this.bridges.frames(5);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertNull(flex.getReorderedNode());
		Assert.assertEquals(Arrays.asList(hidden, first, second), flex.getChildren().ordered());
		Assert.assertEquals(60D, second.getY(), 0D);
	}

	@Test
	public void relaysTheDragToTheChild() {
		final List<String> events = new ArrayList<>();
		final RectNode first = ReorderableFlexNodeTest.item().onDragStart(node -> events.add("start")).onDrag(node -> events.add("drag")).onDragEnd(node -> events.add("end"));
		this.bridges.open(new NodeUI(ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item())));
		this.press(150D, 120D);
		Assert.assertEquals(Collections.singletonList("start"), events);
		this.bridges.move(150D, 150D).frames(3);
		Assert.assertEquals(Arrays.asList("start", "drag", "drag", "drag"), events);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals("end", events.get(events.size() - 1));
		Assert.assertEquals(1, events.stream().filter("end"::equals).count());
	}

	@Test
	public void startsADragFromCode() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second, third).auto(false);
		this.bridges.open(new NodeUI(flex)).move(150D, 180D).frames(2);
		Assert.assertSame(flex, flex.startDrag(second));
		Assert.assertTrue(flex.isDragging(second));
		Assert.assertEquals(1, flex.getInitialIndex());
		Assert.assertEquals(180D, flex.getDragStartMouseY(), 0D);
		this.bridges.move(150D, 240D).frames(60);
		Assert.assertSame(flex, flex.endDrag());
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(first, third, second), flex.getChildren().ordered());
	}

	@Test
	public void startsADragFromCodeBeforeBeingOpened() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first);
		flex.startDrag(first);
		Assert.assertTrue(flex.isDragging(first));
		Assert.assertEquals(0D, flex.getDragStartMouseX(), 0D);
		Assert.assertEquals(0D, flex.getDragStartMouseY(), 0D);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesToDragAStranger() {
		ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item()).startDrag(ReorderableFlexNodeTest.item());
	}

	@Test
	public void keepsTheFirstDrag() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertSame(flex, flex.startDrag(second));
		this.press(150D, 180D);
		Assert.assertTrue(flex.isDragging(first));
		Assert.assertFalse(flex.isDragging(second));
	}

	@Test
	public void endsADragOnlyOnce() {
		final List<Object> ends = new ArrayList<>();
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item()).onReorderEnd((node, child, oldIndex, newIndex) -> ends.add(child));
		Assert.assertSame(flex, flex.endDrag());
		Assert.assertFalse(flex.isReleasing());
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		flex.endDrag().endDrag();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(10);
		Assert.assertEquals(Collections.singletonList(first), ends);
	}

	@Test
	public void scrollsItsParentDownNearItsBottom() {
		final RectNode parent = this.scrollingColumn();
		this.press(150D, 120D);
		this.bridges.move(150D, 240D).frames(10);
		Assert.assertTrue(parent.getTargetScrollY() < 0D);
		Assert.assertTrue(parent.getTargetScrollY() > -140D);
		Assert.assertEquals(0D, parent.getTargetScrollX(), 0D);
	}

	@Test
	public void scrollsItsParentUpNearItsTop() {
		final RectNode parent = this.scrollingColumn();
		this.press(150D, 120D);
		this.bridges.move(150D, 240D).frames(20);
		final double scrolled = parent.getTargetScrollY();
		this.bridges.move(150D, 110D).frames(2);
		Assert.assertTrue(parent.getTargetScrollY() > scrolled);
	}

	@Test
	public void scrollsFasterDeeperInTheEdge() {
		final RectNode parent = this.scrollingColumn();
		this.press(150D, 120D);
		this.bridges.move(150D, 200D).frames(5);
		final double shallow = parent.getTargetScrollY();
		this.bridges.move(150D, 245D).frames(5);
		Assert.assertTrue(shallow < 0D);
		Assert.assertTrue(parent.getTargetScrollY() - shallow < shallow * 5D);
	}

	@Test
	public void waitsForTheMouseToMoveBeforeScrolling() {
		final RectNode parent = this.scrollingColumn();
		this.press(150D, 230D);
		this.bridges.frames(10);
		Assert.assertEquals(0D, parent.getTargetScrollY(), 0D);
		this.bridges.move(150D, 240D).frames(10);
		Assert.assertTrue(parent.getTargetScrollY() < 0D);
	}

	@Test
	public void leavesTheScrollAloneAwayFromTheEdges() {
		final RectNode parent = this.scrollingColumn();
		this.press(150D, 120D);
		this.bridges.move(150D, 175D).frames(10);
		Assert.assertEquals(0D, parent.getTargetScrollY(), 0D);
	}

	@Test
	public void scrollsItsParentSidewaysForARow() {
		final ReorderableFlexNode flex = ReorderableFlexNode.horizontal(0D, 0D, 50D).margin(10D);
		for (int i = 0; i < 5; i++) {
			flex.append(RectNode.create(0D, 0D, 80D, 50D));
		}
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).overflow(OverflowProperty.SCROLL).append(flex);
		this.bridges.open(new NodeUI(parent));
		this.press(110D, 120D);
		this.bridges.move(290D, 120D).frames(20);
		final double scrolled = parent.getTargetScrollX();
		Assert.assertTrue(scrolled < 0D);
		Assert.assertEquals(0D, parent.getTargetScrollY(), 0D);
		this.bridges.move(110D, 120D).frames(2);
		Assert.assertTrue(parent.getTargetScrollX() > scrolled);
	}

	@Test
	public void leavesTheScrollOfARowAloneAwayFromTheEdges() {
		final ReorderableFlexNode flex = ReorderableFlexNode.horizontal(0D, 0D, 50D).margin(10D);
		for (int i = 0; i < 5; i++) {
			flex.append(RectNode.create(0D, 0D, 80D, 50D));
		}
		final RectNode parent = RectNode.create(100D, 100D, 200D, 100D).overflow(OverflowProperty.SCROLL).append(flex);
		this.bridges.open(new NodeUI(parent));
		this.press(110D, 120D);
		this.bridges.move(200D, 120D).frames(10);
		Assert.assertEquals(0D, parent.getTargetScrollX(), 0D);
	}

	@Test
	public void leavesAParentScrollingAcrossARowAlone() {
		final ReorderableFlexNode flex = ReorderableFlexNode.horizontal(0D, 0D, 300D).margin(10D);
		flex.append(RectNode.create(0D, 0D, 80D, 300D), RectNode.create(0D, 0D, 80D, 300D));
		final RectNode parent = RectNode.create(100D, 100D, 200D, 150D).overflow(OverflowProperty.SCROLL).append(flex);
		this.bridges.open(new NodeUI(parent));
		Assert.assertTrue(parent.hasOverflowY());
		this.press(110D, 120D);
		this.bridges.move(295D, 245D).frames(10);
		Assert.assertEquals(0D, parent.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, parent.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesAParentScrollingOnTheOtherAxisAlone() {
		final RectNode first = RectNode.create(0D, 0D, 400D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(0D, 0D, 400D).margin(10D).append(first, RectNode.create(0D, 0D, 400D, 50D));
		final RectNode parent = RectNode.create(100D, 100D, 200D, 150D).overflow(OverflowProperty.SCROLL).append(flex);
		final RectNode grandParent = RectNode.create(0D, 0D, 1000D, 200D).overflow(OverflowProperty.SCROLL).append(parent);
		this.bridges.open(new NodeUI(grandParent));
		Assert.assertTrue(parent.hasOverflowX());
		Assert.assertTrue(grandParent.hasOverflowY());
		this.press(150D, 120D);
		this.bridges.move(150D, 195D).frames(10);
		Assert.assertTrue(flex.isDragging(first));
		Assert.assertEquals(0D, parent.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, parent.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, grandParent.getTargetScrollY(), 0D);
	}

	@Test
	public void looksForAScrollingAncestor() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(0D, 0D, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item());
		final RectNode fitting = RectNode.create(0D, 0D, 200D, 400D).overflow(OverflowProperty.SCROLL).append(flex);
		final RectNode plain = RectNode.create(0D, 0D, 200D, 400D).append(fitting);
		final RectNode ancestor = RectNode.create(100D, 100D, 200D, 150D).overflow(OverflowProperty.SCROLL).append(plain);
		this.bridges.open(new NodeUI(ancestor));
		this.press(150D, 120D);
		this.bridges.move(150D, 240D).frames(10);
		Assert.assertEquals(0D, fitting.getTargetScrollY(), 0D);
		Assert.assertTrue(ancestor.getTargetScrollY() < 0D);
	}

	@Test
	public void appendsAChildAfterTheOthersOnceReordered() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item());
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.move(150D, 150D).frames(60);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		final RectNode added = ReorderableFlexNodeTest.item();
		flex.append(added);
		this.bridges.frame();
		Assert.assertEquals(3, flex.getChildIndex(added));
		Assert.assertEquals(180D, added.getY(), 0D);
	}

	@Test
	public void keepsTheOrderWhenTheChildAfterAHiddenOneIsClicked() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode hidden = ReorderableFlexNodeTest.item().visible(node -> false);
		final RectNode third = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, hidden, third);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 170D);
		this.bridges.frames(5);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(first, hidden, third), flex.getChildren().ordered());
	}

	@Test
	public void keepsTheListStillWhenThePrePhaseConsumesTheStart() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item()).onReorderStart(new NodeReorderStartCallback() {

			@Override
			public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child) {}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		Assert.assertFalse(flex.isDragging(first));
	}

	@Test
	public void letsThePressThroughWhenThePrePhaseRefusesTheStart() {
		final List<Object> clicks = new ArrayList<>();
		final RectNode parent = RectNode.create(100D, 100D, 200D, 400D).onClick((node, mouseX, mouseY, clickType) -> clicks.add(mouseY));
		ReorderableFlexNodeTest.column(0D, 0D, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item()).onReorderStart(new NodeReorderStartCallback() {

			@Override
			public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child) {}

			@Override
			@NodeCallbackMethod(Type.PRE)
			public void pre(final @NonNull ReorderableFlexNode node, final @NonNull InternalContext context, final @NonNull Node child) {
				context.cancel();
			}

		}).attach(parent);
		this.bridges.open(new NodeUI(parent));
		this.press(150D, 120D);
		Assert.assertEquals(Collections.singletonList(120D), clicks);
	}

	@Test
	public void reportsAReorderOnlyWhenTheOrderChanges() {
		final List<Node> reorders = new ArrayList<>();
		final RectNode first = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item()).onReorder((node, child) -> reorders.add(child));
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		this.bridges.frames(10);
		Assert.assertTrue(reorders.isEmpty());
		this.bridges.move(150D, 150D).frames(60);
		Assert.assertEquals(Collections.singletonList(first), reorders);
	}

	@Test
	public void keepsAChildAppendedDuringADrag() {
		final RectNode first = ReorderableFlexNodeTest.item();
		final RectNode second = ReorderableFlexNodeTest.item();
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, first, second);
		this.bridges.open(new NodeUI(flex));
		this.press(150D, 120D);
		final RectNode added = ReorderableFlexNodeTest.item();
		flex.append(added);
		this.bridges.frames(60);
		Assert.assertEquals(120D, added.getY(), 0D);
		this.bridges.move(150D, 150D).frames(60);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(60);
		Assert.assertEquals(Arrays.asList(second, first, added), flex.getChildren().ordered());
		Assert.assertEquals(120D, added.getY(), 0D);
	}

	@Test
	public void putsTheChildrenOnOneLineOnceTurnedIntoARow() {
		final RectNode second = RectNode.create(0D, 0D, 50D, 50D);
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(100D, 100D, RectNode.create(0D, 0D, 50D, 50D), second);
		this.bridges.open(new NodeUI(flex));
		Assert.assertEquals(60D, second.getY(), 0D);
		flex.direction(FlexDirection.ROW);
		this.bridges.frame();
		Assert.assertEquals(60D, second.getX(), 0D);
		Assert.assertEquals(0D, second.getY(), 0D);
	}

	private RectNode scrollingColumn() {
		final ReorderableFlexNode flex = ReorderableFlexNodeTest.column(0D, 0D, ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item(), ReorderableFlexNodeTest.item());
		final RectNode parent = RectNode.create(100D, 100D, 200D, 150D).overflow(OverflowProperty.SCROLL).append(flex);
		this.bridges.open(new NodeUI(parent));
		return parent;
	}

	private void press(final double mouseX, final double mouseY) {
		this.bridges.move(mouseX, mouseY).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private static ReorderableFlexNode column(final double x, final double y, final Node... children) {
		final ReorderableFlexNode flex = ReorderableFlexNode.vertical(x, y, 200D).margin(10D);
		flex.append(children);
		return flex;
	}

	private static RectNode item() {
		return RectNode.create(0D, 0D, 200D, 50D);
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