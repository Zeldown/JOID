package dev.joid.lib.ui.node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.google.gson.JsonObject;

import dev.joid.internal.JOID;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.dto.converter.TextConverter;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.node.callback.impl.draggable.NodeDragCallback;
import dev.joid.lib.ui.node.callback.impl.signal.NodeWatchCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeDetachCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeInitCallback;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.hover.HoverElement;
import dev.joid.lib.ui.node.hover.HoverSupplier;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.ui.node.property.position.PositionProperty;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.DoubleSignal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class NodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void scrollsItsChildrenTogetherByWholePixels() {
		this.bridges.resize(1366, 768).open(new ScrollUI()).frames(30);
		this.bridges.move(300D * 1366D / 1920D, 250D * 768D / 1080D).frames(2).scroll(-1D);

		final Set<Double> gaps = new HashSet<>();
		final Set<Double> tops = new HashSet<>();
		for (int frame = 0; frame < 40; frame++) {
			this.bridges.frame();
			final Draw first = this.draw(0.2F, 0.4F, 0.6F);
			gaps.add(Math.rint(this.draw(0.6F, 0.4F, 0.2F).getTop() - first.getTop()));
			tops.add(Math.rint(first.getTop()));
		}

		Assert.assertEquals(1, gaps.size());
		Assert.assertTrue(tops.size() > 5);
	}

	@Test
	public void movesANodeAndItsChildrenByWholePixels() {
		final MovingUI ui = new MovingUI();
		this.bridges.resize(1366, 768).open(ui).frames(30);

		final Set<Double> gaps = new HashSet<>();
		for (int frame = 0; frame < 20; frame++) {
			ui.panel.y(ui.panel.getY() + 0.37D);
			this.bridges.frame();
			gaps.add(Math.rint(this.draw(0.6F, 0.4F, 0.2F).getTop() - this.draw(0.2F, 0.4F, 0.6F).getTop()));
		}

		Assert.assertEquals(1, gaps.size());
	}

	@Test
	public void drawsANodeAtItsExactPositionOnceItStops() {
		final MovingUI ui = new MovingUI();
		this.bridges.resize(1366, 768).open(ui).frames(30);
		for (int frame = 0; frame < 5; frame++) {
			ui.panel.y(ui.panel.getY() + 0.37D);
			this.bridges.frame();
		}

		this.bridges.frames(2);
		Assert.assertEquals(Math.floor(ui.panel.getAbsoluteY() * 768D / 1080D + 0.5D + 1E-6D), this.draw(0.2F, 0.4F, 0.6F).getTop(), 1E-3D);
	}

	@Test
	public void startsWithoutSizeFromItsPosition() {
		final PointNode node = new PointNode(5D, 6D);
		Assert.assertEquals(5D, node.getDefaultX(), 0D);
		Assert.assertEquals(6D, node.getY(), 0D);
		Assert.assertEquals(0D, node.getWidth(), 0D);
		Assert.assertEquals(0D, node.getDefaultHeight(), 0D);
	}

	@Test
	public void startsWithItsDefaultProperties() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		Assert.assertSame(PositionProperty.RELATIVE, node.getPosition());
		Assert.assertSame(OverflowProperty.NONE, node.getOverflow());
		Assert.assertSame(Align.START, node.getAnchorX());
		Assert.assertSame(Align.START, node.getAnchorY());
		Assert.assertEquals(-1D, node.getAspectRatio(), 0D);
		Assert.assertEquals(200L, node.getHoverDuration());
		Assert.assertSame(TweenEquations.LINEAR, node.getHoverEquation());
		Assert.assertEquals(1D, node.getScrollSpeed(), 0D);
		Assert.assertTrue(node.isVisible());
		Assert.assertTrue(node.isEnabled());
		Assert.assertTrue(node.isMounted());
		Assert.assertFalse(node.hasUi());
	}

	@Test
	public void movesWithoutForgettingItsDefaultBounds() {
		final RectNode node = RectNode.create(10D, 20D, 30D, 40D);
		Assert.assertSame(node, node.x(1D).y(2D));
		Assert.assertEquals(1D, node.getX(), 0D);
		Assert.assertEquals(2D, node.getY(), 0D);
		node.x(3D).y(4D).width(5D).height(6D);
		Assert.assertEquals(3D, node.getX(), 0D);
		Assert.assertEquals(4D, node.getY(), 0D);
		Assert.assertEquals(5D, node.getWidth(), 0D);
		Assert.assertEquals(6D, node.getHeight(), 0D);
		node.x(7D).y(8D).width(9D).height(10D).width(11D).height(12D);
		Assert.assertEquals(7D, node.getX(), 0D);
		Assert.assertEquals(8D, node.getY(), 0D);
		Assert.assertEquals(11D, node.getWidth(), 0D);
		Assert.assertEquals(12D, node.getHeight(), 0D);
		Assert.assertEquals(10D, node.getDefaultX(), 0D);
		Assert.assertEquals(20D, node.getDefaultY(), 0D);
		Assert.assertEquals(30D, node.getDefaultWidth(), 0D);
		Assert.assertEquals(40D, node.getDefaultHeight(), 0D);
	}

	@Test
	public void measuresItselfWithItsRelativeHelpers() {
		final RectNode node = RectNode.create(10D, 20D, 200D, 100D);
		Assert.assertEquals(200D, node.w(), 0D);
		Assert.assertEquals(100D, node.h(), 0D);
		Assert.assertEquals(50D, node.dw(4D), 0D);
		Assert.assertEquals(25D, node.dh(4D), 0D);
		Assert.assertEquals(50D, node.mw(0.25D), 0D);
		Assert.assertEquals(25D, node.mh(0.25D), 0D);
		Assert.assertEquals(190D, node.aw(-10D), 0D);
		Assert.assertEquals(90D, node.ah(-10D), 0D);
		Assert.assertEquals(15D, node.ax(5D), 0D);
		Assert.assertEquals(25D, node.ay(5D), 0D);
	}

	@Test
	public void addsThePositionOfItsParents() {
		final ContainerNode root = ContainerNode.create(100D, 50D, 500D, 500D);
		final ContainerNode middle = ContainerNode.create(10D, 20D, 300D, 300D).attach(root);
		final RectNode leaf = RectNode.create(1D, 2D, 10D, 10D).attach(middle);
		leaf.x(5D);
		Assert.assertSame(middle, leaf.getParent());
		Assert.assertEquals(115D, leaf.getAbsoluteX(), 0D);
		Assert.assertEquals(72D, leaf.getAbsoluteY(), 0D);
		Assert.assertEquals(111D, leaf.getAbsoluteDefaultX(), 0D);
		Assert.assertEquals(72D, leaf.getAbsoluteDefaultY(), 0D);
		Assert.assertEquals(100D, root.getAbsoluteDefaultX(), 0D);
		Assert.assertEquals(50D, root.getAbsoluteDefaultY(), 0D);
	}

	@Test
	public void placesAnAbsoluteNodeOnTheUiWhateverItsParents() {
		final RectNode child = RectNode.create(30D, 40D, 20D, 20D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).position(PositionProperty.ABSOLUTE);
		final ContainerNode parent = ContainerNode.create(200D, 300D, 400D, 400D).append(child);
		this.bridges.open(new NodeUI(ContainerNode.create(50D, 60D, 800D, 800D).append(parent))).frame();
		Assert.assertSame(PositionProperty.ABSOLUTE, child.getPosition());
		Assert.assertEquals(250D, parent.getAbsoluteX(), 0D);
		Assert.assertEquals(30D, child.getAbsoluteX(), 0D);
		Assert.assertEquals(40D, child.getAbsoluteY(), 0D);
		Assert.assertEquals(30D, this.draw(0.2F, 0.4F, 0.6F).getLeft(), 1E-3D);
		Assert.assertEquals(40D, this.draw(0.2F, 0.4F, 0.6F).getTop(), 1E-3D);
	}

	@Test
	public void keepsItsCenterWhenResizedAroundACenterAnchor() {
		final RectNode node = RectNode.create(100D, 100D, 200D, 100D).anchor(Align.CENTER);
		this.bridges.open(new NodeUI(node)).frame();
		node.width(100D).height(50D);
		this.bridges.frame();
		Assert.assertSame(Align.CENTER, node.getAnchorX());
		Assert.assertSame(Align.CENTER, node.getAnchorY());
		Assert.assertEquals(150D, node.getX(), 0D);
		Assert.assertEquals(125D, node.getY(), 0D);
	}

	@Test
	public void keepsItsFarEdgesWhenResizedAroundEndAnchors() {
		final RectNode node = RectNode.create(100D, 100D, 200D, 100D).anchorX(Align.END).anchorY(Align.END);
		this.bridges.open(new NodeUI(node)).frame();
		node.width(150D).height(40D);
		this.bridges.frame();
		Assert.assertEquals(150D, node.getX(), 0D);
		Assert.assertEquals(160D, node.getY(), 0D);
		node.width(200D).height(100D);
		this.bridges.frame();
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
	}

	@Test
	public void keepsItsOriginWhenResizedAroundAStartAnchor() {
		final RectNode node = RectNode.create(100D, 100D, 200D, 100D).anchorX(Align.START).anchorY(Align.CENTER);
		this.bridges.open(new NodeUI(node)).frame();
		node.width(100D).height(50D);
		this.bridges.frame();
		Assert.assertSame(Align.START, node.getAnchorX());
		Assert.assertSame(Align.CENTER, node.getAnchorY());
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(125D, node.getY(), 0D);
		node.anchorY(Align.START).height(100D);
		this.bridges.frame();
		Assert.assertEquals(125D, node.getY(), 0D);
	}

	@Test
	public void keepsItsSizeWithoutRatio() {
		final RectNode node = RectNode.create(0D, 0D, 200D, 100D);
		final PointNode empty = new PointNode(0D, 0D).aspectRatio(2D);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 500D, 500D).append(node, empty))).frame();
		Assert.assertEquals(200D, node.getWidth(), 0D);
		Assert.assertEquals(100D, node.getHeight(), 0D);
		Assert.assertEquals(0D, empty.getWidth(), 0D);
		Assert.assertEquals(0D, empty.getHeight(), 0D);
	}

	@Test
	public void hidesItsWholeTreeWhileInvisible() {
		final boolean[] shown = {false};
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).append(child).visible(rect -> shown[0]);
		this.bridges.open(new NodeUI(parent)).frame();
		Assert.assertFalse(parent.isVisible());
		Assert.assertFalse(child.isVisible());
		Assert.assertTrue(child.isVisibleProperty());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).isEmpty());
		shown[0] = true;
		this.bridges.frame();
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).size());
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).size());
	}

	@Test
	public void showsItselfOnceEverySignalHasAValue() {
		final Signal<String> first = new Signal<>();
		final Signal<Integer> second = new Signal<>(3);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).visible(Signal.from(() -> first.get() != null && second.get() != null));
		Assert.assertFalse(node.isVisible());
		first.set("ready");
		Assert.assertTrue(node.isVisible());
		second.set(4);
		Assert.assertTrue(node.isVisible());
	}

	@Test
	public void hidesItselfWhileABooleanSignalIsFalse() {
		final BooleanSignal shown = new BooleanSignal(false);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).visible(shown);
		Assert.assertFalse(node.isVisible());
		shown.set(true);
		Assert.assertTrue(node.isVisible());
		shown.set(false);
		Assert.assertFalse(node.isVisible());
	}

	@Test
	public void ignoresTheMouseWhileDisabled() {
		final int[] clicks = {0};
		final boolean[] enabled = {false};
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).enabled(rect -> enabled[0]).onClick((rect, mouseX, mouseY, clickType) -> clicks[0]++);
		this.bridges.open(new NodeUI(node));
		this.bridges.move(150D, 150D).frames(2);
		Assert.assertFalse(node.isEnabled());
		Assert.assertFalse(node.isHovered(150D, 150D));
		Assert.assertTrue(node.isHovered(150D, 150D, false));
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(0, clicks[0]);
		enabled[0] = true;
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(1, clicks[0]);
	}

	@Test
	public void hidesTheChildrenOutsideItsOverflowArea() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN);
		final RectNode inside = RectNode.create(10D, 10D, 20D, 20D).attach(area);
		final RectNode left = RectNode.create(-40D, 10D, 20D, 20D).attach(area);
		final RectNode right = RectNode.create(120D, 10D, 20D, 20D).attach(area);
		final RectNode above = RectNode.create(10D, -40D, 20D, 20D).attach(area);
		final RectNode below = RectNode.create(10D, 120D, 20D, 20D).attach(area);
		final ContainerNode middle = ContainerNode.create(0D, 0D, 50D, 50D).attach(area);
		final RectNode nested = RectNode.create(0D, 0D, 10D, 10D).attach(middle);
		this.bridges.open(new NodeUI(area)).frame();
		Assert.assertSame(area, inside.getOverflowArea());
		Assert.assertSame(area, nested.getOverflowArea());
		Assert.assertTrue(inside.isVisible());
		Assert.assertTrue(nested.isVisible());
		Assert.assertFalse(left.isVisible());
		Assert.assertFalse(right.isVisible());
		Assert.assertFalse(above.isVisible());
		Assert.assertFalse(below.isVisible());
	}

	@Test
	public void staysVisibleBesideAnAreaWithoutOverflow() {
		final RectNode node = RectNode.create(500D, 500D, 10D, 10D).overflowArea(ContainerNode.create(0D, 0D, 10D, 10D));
		Assert.assertNotNull(node.getOverflowArea());
		Assert.assertTrue(node.isVisible());
	}

	@Test
	public void ignoresTheMouseOverAChildOutsideItsOverflowArea() {
		final RectNode child = RectNode.create(50D, 50D, 100D, 100D);
		this.bridges.open(new NodeUI(ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child))).frame();
		Assert.assertTrue(child.isHovered(160D, 160D));
		Assert.assertFalse(child.isHovered(220D, 220D));
	}

	@Test
	public void forgetsTheOverflowAreaOfItsFormerParentOnceRemoved() {
		final RectNode grandchild = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode child = ContainerNode.create(150D, 10D, 20D, 20D).append(grandchild);
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child);
		this.bridges.open(new NodeUI(area)).frame();
		Assert.assertFalse(child.isVisible());
		area.remove(child);
		Assert.assertNull(child.getOverflowArea());
		Assert.assertNull(grandchild.getOverflowArea());
		Assert.assertTrue(child.isVisible());
	}

	@Test
	public void forgetsTheOverflowAreaOfItsFormerParentOnceCleared() {
		final RectNode child = RectNode.create(150D, 10D, 20D, 20D);
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child);
		this.bridges.open(new NodeUI(area)).frame();
		area.clearChildren();
		Assert.assertNull(child.getOverflowArea());
		Assert.assertTrue(child.isVisible());
	}

	@Test
	public void forgetsTheOverflowAreaOfItsFormerParentOnceMoved() {
		final RectNode child = RectNode.create(150D, 10D, 20D, 20D);
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child);
		final ContainerNode other = ContainerNode.create(0D, 0D, 10D, 10D);
		this.bridges.open(new NodeUI(area)).frame();
		other.append(child);
		Assert.assertNull(child.getOverflowArea());
		Assert.assertTrue(child.isVisible());
	}

	@Test
	public void keepsTheOverflowAreaItGivesToItsChildrenOnceRemoved() {
		final RectNode grandchild = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode child = ContainerNode.create(10D, 10D, 20D, 20D).overflow(OverflowProperty.HIDDEN).append(grandchild);
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child);
		this.bridges.open(new NodeUI(area)).frame();
		area.remove(child);
		Assert.assertNull(child.getOverflowArea());
		Assert.assertSame(child, grandchild.getOverflowArea());
	}

	@Test
	public void isNeverHoveredOutsideAUi() {
		Assert.assertFalse(RectNode.create(0D, 0D, 100D, 100D).isHovered(50D, 50D));
	}

	@Test
	public void fadesItsHoverValueOverItsHoverDuration() {
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hoverDuration(160L);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(160L, node.getHoverDuration());
		Assert.assertEquals(0F, node.hoverValue(2F), 0F);
		this.bridges.move(150D, 150D).frame();
		Assert.assertTrue(node.isHovered());
		this.bridges.frames(5);
		Assert.assertEquals(1F, node.hoverValue(2F), 1E-4F);
		this.bridges.frames(10);
		Assert.assertEquals(2F, node.hoverValue(2F), 1E-4F);
		this.bridges.move(500D, 500D).frame();
		Assert.assertFalse(node.isHovered());
		this.bridges.frames(5);
		Assert.assertEquals(1F, node.hoverValue(2F), 1E-4F);
	}

	@Test
	public void easesItsHoverValueWithItsEquation() {
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hoverDuration(160L).hoverEquation(TweenEquations.QUAD_IN);
		this.bridges.open(new NodeUI(node));
		this.bridges.move(150D, 150D).frame();
		this.bridges.frames(5);
		Assert.assertSame(TweenEquations.QUAD_IN, node.getHoverEquation());
		Assert.assertEquals(0.25F, node.hoverValue(1F), 1E-4F);
	}

	@Test
	public void reportsTheStartEveryFrameAndTheEndOfAHover() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onHoverStart((rect, mouseX, mouseY) -> events.add("start")).onHover((rect, mouseX, mouseY) -> events.add("hover")).onHoverEnd((rect, mouseX, mouseY) -> events.add("end"));
		this.bridges.open(new NodeUI(node));
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.move(500D, 500D).frames(2);
		Assert.assertEquals(Arrays.asList("start", "hover", "hover", "end"), events);
	}

	@Test
	public void endsAForcedHoverOnceTheMouseIsAway() {
		final int[] ends = {0};
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onHoverEnd((rect, mouseX, mouseY) -> ends[0]++);
		this.bridges.open(new NodeUI(node));
		Assert.assertTrue(node.hovered(true).isHovered());
		this.bridges.frame();
		Assert.assertFalse(node.isHovered());
		Assert.assertEquals(1, ends[0]);
	}

	@Test
	public void showsItsTooltipLinesWhileHovered() {
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hover(() -> "Save").hover(() -> Arrays.asList("Shortcut", "Ctrl+S"));
		final HoverUI ui = new HoverUI(node);
		this.bridges.open(ui);
		Assert.assertTrue(ui.tooltips.isEmpty());
		this.bridges.move(150D, 150D).frame();
		Assert.assertEquals(Arrays.asList(Arrays.asList("Save", "Shortcut", "Ctrl+S")), ui.tooltips);
	}

	@Test
	public void showsNoTooltipForAMissingLine() {
		final HoverUI ui = new HoverUI(RectNode.create(100D, 100D, 100D, 100D).hover((HoverSupplier) () -> null));
		this.bridges.open(ui);
		this.bridges.move(150D, 150D).frames(2);
		Assert.assertTrue(ui.tooltips.isEmpty());
	}

	@Test
	public void replacesItsTooltipLines() {
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hover(() -> "Old").hoverLines(() -> "New");
		final HoverUI ui = new HoverUI(node);
		this.bridges.open(ui);
		this.bridges.move(150D, 150D).frame();
		Assert.assertEquals(Arrays.asList("New"), ui.tooltips.get(0));
		node.hoverLines(() -> Arrays.asList("First", "Second"));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("First", "Second"), ui.tooltips.get(1));
		node.clearHoverLines();
		this.bridges.frame();
		Assert.assertEquals(2, ui.tooltips.size());
	}

	@Test
	public void rendersItsHoverElements() {
		final List<String> rendered = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hover((rect, mouseX, mouseY) -> rendered.add("first")).hover(() -> "Line");
		final HoverUI ui = new HoverUI(node);
		this.bridges.open(ui);
		this.bridges.move(150D, 150D).frame();
		Assert.assertEquals(Arrays.asList("first"), rendered);
		Assert.assertEquals(1, ui.tooltips.size());
		node.hoverElements((rect, mouseX, mouseY) -> rendered.add("second"));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("first", "second"), rendered);
		node.clearHoverElements();
		this.bridges.frame();
		Assert.assertEquals(2, rendered.size());
		Assert.assertEquals(3, ui.tooltips.size());
		node.hover((rect, mouseX, mouseY) -> rendered.add("third")).clearHover();
		this.bridges.frame();
		Assert.assertEquals(2, rendered.size());
		Assert.assertEquals(3, ui.tooltips.size());
		Assert.assertTrue(node.getHoverElementList().isEmpty());
		Assert.assertTrue(node.getHoverSupplierList().isEmpty());
	}

	@Test
	public void showsTheTooltipOfAChildBelowItsParent() {
		final RectNode child = RectNode.create(150D, 0D, 100D, 100D).hover(() -> "Child").zindex(-1);
		final HoverUI ui = new HoverUI(RectNode.create(100D, 100D, 100D, 100D).append(child));
		this.bridges.open(ui);
		this.bridges.move(300D, 150D).frame();
		Assert.assertEquals(Arrays.asList(Arrays.asList("Child")), ui.tooltips);
	}

	@Test
	public void appliesItsEffectsInPriorityOrder() {
		final List<String> events = new ArrayList<>();
		final RecordingEffect late = new RecordingEffect("late", events).priority(2);
		final RecordingEffect early = new OtherEffect("early", events).priority(1);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(late).effect(early);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(Arrays.asList("early init", "late init", "early pre", "late pre", "late post", "early post"), events);
	}

	@Test
	public void sortsItsEffectsAgainWhenAPriorityChanges() {
		final List<String> events = new ArrayList<>();
		final RecordingEffect first = new RecordingEffect("first", events);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(first).effect(new OtherEffect("second", events));
		this.bridges.open(new NodeUI(node));
		events.clear();
		first.priority(1);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("second pre", "first pre", "first post", "second post"), events);
	}

	@Test
	public void replacesAnEffectOfTheSameClass() {
		final List<String> events = new ArrayList<>();
		final RecordingEffect second = new RecordingEffect("second", events);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new RecordingEffect("first", events)).effect(second);
		Assert.assertEquals(1, node.getEffectMap().size());
		Assert.assertSame(second, node.getEffect(RecordingEffect.class));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(Arrays.asList("second init", "second pre", "second post"), events);
	}

	@Test
	public void forgetsItsRemovedEffects() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new RecordingEffect("first", events)).effect(new OtherEffect("second", events));
		Assert.assertTrue(node.hasEffect(RecordingEffect.class));
		Assert.assertSame(node, node.removeEffect(RecordingEffect.class));
		Assert.assertFalse(node.hasEffect(RecordingEffect.class));
		Assert.assertNull(node.getEffect(RecordingEffect.class));
		Assert.assertTrue(node.hasEffect(OtherEffect.class));
		Assert.assertSame(node, node.clearEffects());
		Assert.assertFalse(node.hasEffect(OtherEffect.class));
		this.bridges.open(new NodeUI(node));
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void buildsAnEffectFromItsNodeInSelf() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).self(rect -> rect.effect(new RecordingEffect(rect.getClass().getSimpleName(), events)));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals("RectNode init", events.get(0));
	}

	@Test
	public void chainsAConfiguredCustomEffectWithoutTypeWitness() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new RecordingEffect("first", new ArrayList<>()).priority(2)).effect(BorderNodeEffect.create(Color.BLACK, 2F).fill(false));
		final BorderNodeEffect border = node.getEffect(BorderNodeEffect.class);
		Assert.assertEquals(2, node.getEffect(RecordingEffect.class).getPriority());
		Assert.assertFalse(border.isFill());
	}

	@Test
	public void chainsAConfiguredEffectWithoutTypeWitness() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(BlurNodeEffect.create(2F).radius(4F)).effect(RoundedNodeEffect.create(6F).scope(NodeEffectScope.CHILDREN));
		final BlurNodeEffect blur = node.getEffect(BlurNodeEffect.class);
		final RoundedNodeEffect rounded = node.getEffect(RoundedNodeEffect.class);
		Assert.assertEquals(4F, blur.getRadiusSupplier().get(), 0F);
		Assert.assertSame(NodeEffectScope.CHILDREN, rounded.getScope());
	}

	@Test
	public void chainsAnExtendedBuiltInEffectWithATypeWitness() {
		final GlowBorderEffect glow = GlowBorderEffect.create().<GlowBorderEffect>fill(false).glow(4F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(glow);
		Assert.assertSame(glow, node.getEffect(GlowBorderEffect.class));
		Assert.assertNull(node.getEffect(BorderNodeEffect.class));
		Assert.assertFalse(glow.isFill());
		Assert.assertEquals(4F, glow.getGlow(), 0F);
	}

	@Test
	public void findsItsEffectsByTheirBuiltInClass() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(RoundedNodeEffect.create(6F));
		final RoundedNodeEffect rounded = node.getEffect(RoundedNodeEffect.class);
		Assert.assertEquals(6F, rounded.getRadius(), 0F);
		Assert.assertTrue(node.hasEffect(RoundedNodeEffect.class));
		Assert.assertFalse(node.removeEffect(RoundedNodeEffect.class).hasEffect(RoundedNodeEffect.class));
		Assert.assertNull(node.getEffect(RoundedNodeEffect.class));
	}

	@Test
	public void acceptsAnEffectTypedByItsNode() {
		final List<String> events = new ArrayList<>();
		this.bridges.open(new NodeUI(RectNode.create(0D, 0D, 10D, 10D).color(Color.RED).effect(new ColorEffect(events))));
		Assert.assertEquals(Color.RED.toString(), events.get(0));
	}

	@Test
	public void buildsAnEffectFromItsTypedNodeInSelf() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).self((final RectNode rect) -> rect.effect(RoundedNodeEffect.create((float) rect.getWidth())));
		final RoundedNodeEffect rounded = node.getEffect(RoundedNodeEffect.class);
		Assert.assertEquals(10F, rounded.getRadius(), 0F);
	}

	@Test
	public void skipsAnEffectThatDoesNotApply() {
		final List<String> events = new ArrayList<>();
		final SkippedEffect effect = new SkippedEffect(events);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(effect);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertFalse(node.shouldApplyEffect(effect));
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void drawsItsChildrenIntoAChildrenScopedShader() {
		final List<Boolean> buffered = new ArrayList<>();
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).onDraw((rect, mouseX, mouseY) -> buffered.add(this.bridges.getRender().getState().getFrameBuffer() != null));
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).append(child).effect(BlurNodeEffect.create(4F).scope(NodeEffectScope.CHILDREN));
		this.bridges.open(new NodeUI(parent));
		Assert.assertEquals(Arrays.asList(true), buffered);
		parent.effect(BlurNodeEffect.create(4F));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(true, false), buffered);
	}

	@Test
	public void drawsItsLayersOverItsChildren() {
		final List<String> drawn = new ArrayList<>();
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).onDraw((rect, mouseX, mouseY) -> drawn.add("child"));
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).append(child).layer((mouseX, mouseY) -> drawn.add("second")).layer(0, (mouseX, mouseY) -> drawn.add("first"));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(Arrays.asList("child", "first", "second"), drawn);
		Assert.assertSame(node, node.clearLayers());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("child", "first", "second", "child"), drawn);
		Assert.assertTrue(node.getLayerList().isEmpty());
	}

	@Test
	public void drawsItsLayersOnItsPosition() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).layer((mouseX, mouseY) -> DrawUtils.SHAPE.drawRect(10D, 10D, 20D, 20D, new Color(0.2F, 0.4F, 0.6F, 1F)));
		this.bridges.open(new NodeUI(ContainerNode.create(300D, 200D, 500D, 500D).append(node)));
		Assert.assertEquals(310D, this.draw(0.2F, 0.4F, 0.6F).getLeft(), 1E-3D);
		Assert.assertEquals(210D, this.draw(0.2F, 0.4F, 0.6F).getTop(), 1E-3D);
	}

	@Test
	public void drawsItsChildrenInTheirZIndexOrder() {
		final RectNode top = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).zindex(2);
		final RectNode middle = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).zindex(1);
		final RectNode bottom = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.5F, 0.7F, 0.9F, 1F)).zindex(-1);
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).append(top, middle, bottom);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(2, top.getIndex());
		Assert.assertEquals(Arrays.asList(bottom, middle, top), node.getChildren().ordered());
		Assert.assertTrue(this.order(0.5F, 0.7F, 0.9F) < this.order(0.2F, 0.4F, 0.6F));
		Assert.assertTrue(this.order(0.2F, 0.4F, 0.6F) < this.order(0.3F, 0.5F, 0.7F));
		Assert.assertTrue(this.order(0.3F, 0.5F, 0.7F) < this.order(0.1F, 0.3F, 0.5F));
	}

	@Test
	public void liftsANodeAlongTheDepthByItsZLevel() {
		final double[] depths = new double[2];
		final RectNode flat = RectNode.create(0D, 0D, 10D, 10D).onDraw((rect, mouseX, mouseY) -> depths[0] = this.bridges.getRender().getModelView().getMatrix()[14]);
		final RectNode lifted = RectNode.create(0D, 0D, 10D, 10D).onDraw((rect, mouseX, mouseY) -> depths[1] = this.bridges.getRender().getModelView().getMatrix()[14]).zlevel(25D);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 100D, 100D).append(flat, lifted)));
		Assert.assertEquals(25D, lifted.getZlevel(), 0D);
		Assert.assertEquals(25D, depths[1] - depths[0], 1E-3D);
	}

	@Test
	public void sendsTheEventsToItsChildrenAboveThenToItselfThenToTheChildrenBelow() {
		final List<String> events = new ArrayList<>();
		final RecordingNode above = new RecordingNode("above", events, 0D, 0D, 10D, 10D);
		final RecordingNode below = new RecordingNode("below", events, 0D, 0D, 10D, 10D).zindex(-1);
		final RecordingNode parent = new RecordingNode("parent", events, 100D, 100D, 100D, 100D).append(above, below);
		this.bridges.open(new NodeUI(parent));
		this.bridges.move(150D, 150D).frames(2);
		events.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.scroll(1D);
		this.bridges.getUi().keyTyped('a', Key.A);
		Assert.assertEquals(Arrays.asList("above pressed", "parent pressed", "below pressed", "above dragged", "parent dragged", "below dragged", "above released", "parent released", "below released", "above scrolled", "parent scrolled", "below scrolled", "above typed", "parent typed", "below typed"), events);
	}

	@Test
	public void remembersItsLastClickAndKey() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D);
		this.bridges.open(new NodeUI(node));
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertSame(ClickType.RIGHT, node.getLastClickType());
		Assert.assertEquals(this.bridges.getClock().currentTimeMillis(), node.getLastClickTime());
		this.bridges.frame();
		this.bridges.getUi().keyTyped('z', Key.Z);
		Assert.assertEquals('z', node.getLastCharacter());
		Assert.assertSame(Key.Z, node.getLastKey());
		Assert.assertEquals(this.bridges.getClock().currentTimeMillis(), node.getLastKeyTime());
	}

	@Test
	public void runsItsPressCallbacksBesideItsClick() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).onClick((rect, mouseX, mouseY, clickType) -> events.add("click")).onMousePressed((rect, mouseX, mouseY, clickType) -> events.add("pressed " + clickType.name())).onMouseReleased((rect, mouseX, mouseY, clickType) -> events.add("released"));
		this.press(node, 110D, 110D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("click", "pressed LEFT", "released"), events);
	}

	@Test
	public void runsItsRenderCallbackOnEveryFrame() {
		final int[] renders = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onRender((rect, mouseX, mouseY) -> renders[0]++);
		this.bridges.open(new NodeUI(node)).frames(3);
		Assert.assertEquals(4, renders[0]);
	}

	@Test
	public void updatesItsChildrenBeforeItself() {
		final List<String> events = new ArrayList<>();
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D);
		final RecordingNode parent = new RecordingNode("parent", events, 0D, 0D, 100D, 100D).append(child).onUpdate(target -> events.add("callback"));
		parent.onUpdate();
		Assert.assertEquals(Arrays.asList("child update", "parent update", "callback"), events);
	}

	@Test
	public void countsItsLoads() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(1L, node.getUpdateCount());
		node.load(node.getUi());
		Assert.assertEquals(2L, node.getUpdateCount());
	}

	@Test
	public void countsNoLoadOnTheRefreshOfAFollowedProperty() {
		final DoubleSignal x = DoubleSignal.of(0D);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).x(x);
		this.bridges.open(new NodeUI(node)).frames(2);
		Assert.assertEquals(1L, node.getUpdateCount());
		x.set(20D);
		this.bridges.frame();
		Assert.assertEquals(20D, node.getX(), 0D);
		Assert.assertEquals(1L, node.getUpdateCount());
	}

	@Test
	public void runsOneCallbackPhaseOnRequest() {
		final List<String> phases = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onInit(new NodeInitCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode rect) {
				phases.add("apply");
			}

			@Override
			public void pre(final @NonNull RectNode rect, final @NonNull InternalContext context) {
				phases.add("pre");
			}

		});
		node.executePreCallback(NodeCallbackRegistry.getId(NodeInitCallback.class), InternalContext.create());
		Assert.assertEquals(Arrays.asList("pre"), phases);
		node.executePostCallback(NodeCallbackRegistry.getId(NodeInitCallback.class), InternalContext.create());
		Assert.assertEquals(Arrays.asList("pre", "apply"), phases);
		final InternalContext context = InternalContext.create();
		node.executePreCallback(NodeCallbackRegistry.getId(NodeDetachCallback.class), context);
		node.executePostCallback(NodeCallbackRegistry.getId(NodeDetachCallback.class), context);
		Assert.assertFalse(context.isCancelled());
		Assert.assertEquals(2, phases.size());
	}

	@Test
	public void runsItsActionOnceItsCallbacksAreCleared() {
		final List<String> events = new ArrayList<>();
		final int[] detaches = {0};
		final RecordingNode node = new RecordingNode("node", events, 0D, 0D, 10D, 10D).onDetach(target -> detaches[0]++);
		this.bridges.open(new NodeUI(node));
		events.clear();
		node.getCallbackMap().values().forEach(List::clear);
		node.onDetach();
		Assert.assertEquals(0, detaches[0]);
		Assert.assertEquals(Arrays.asList("node detach"), events);
	}

	@Test
	public void skipsItsActionWhenAPreCallbackCancelsIt() {
		final List<String> events = new ArrayList<>();
		final RecordingNode node = new RecordingNode("node", events, 0D, 0D, 10D, 10D).onDetach(new NodeDetachCallback<RecordingNode>() {

			@Override
			public void apply(final @NonNull RecordingNode target) {
				events.add("detached");
			}

			@Override
			public void pre(final @NonNull RecordingNode target, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(node));
		events.clear();
		node.onDetach();
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void skipsAMissingCallback() {
		final int[] detaches = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onDetach(rect -> detaches[0]++);
		node.getCallbackMap().get(NodeCallbackRegistry.getId(NodeDetachCallback.class)).add(null);
		this.bridges.open(new NodeUI(node));
		node.onDetach();
		Assert.assertEquals(1, detaches[0]);
	}

	@Test
	public void drawsItsSkeletonUntilItIsMounted() {
		final boolean[] ready = {false};
		final int[] mounts = {0};
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).onMount(rect -> mounts[0]++).wait(rect -> ready[0]).skeleton(rect -> RectNode.create(5D, 5D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)));
		this.bridges.open(new NodeUI(node));
		Assert.assertFalse(node.isMounted());
		Assert.assertTrue(node.getSkeleton().isMounted());
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
		Assert.assertEquals(105D, this.draw(0.6F, 0.4F, 0.2F).getLeft(), 1E-3D);
		Assert.assertEquals(0, mounts[0]);
		ready[0] = true;
		this.bridges.frames(2);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F).isEmpty());
		Assert.assertEquals(100D, this.draw(0.2F, 0.4F, 0.6F).getLeft(), 1E-3D);
		Assert.assertEquals(1, mounts[0]);
	}

	@Test
	public void drawsALoadingPlaceholderWithoutSkeleton() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 40D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).wait(rect -> false);
		this.bridges.open(new NodeUI(node));
		final Color loading = Color.LOADING();
		final Draw placeholder = this.draw(loading.r, loading.g, loading.b);
		Assert.assertEquals(100D, placeholder.getLeft(), 1E-3D);
		Assert.assertEquals(100D, placeholder.getTop(), 1E-3D);
		Assert.assertEquals(150D, placeholder.getRight(), 1E-3D);
		Assert.assertEquals(140D, placeholder.getBottom(), 1E-3D);
		Assert.assertTrue(this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).isEmpty());
	}

	@Test
	public void forwardsTheEventsToItsSkeletonUntilItIsMounted() {
		final List<String> events = new ArrayList<>();
		final boolean[] ready = {false};
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).wait(rect -> ready[0]).skeleton(rect -> new RecordingNode("skeleton", events, 0D, 0D, 50D, 50D));
		this.bridges.open(new NodeUI(node));
		this.bridges.move(110D, 110D).frames(2);
		events.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.scroll(1D);
		this.bridges.getUi().keyTyped('a', Key.A);
		Assert.assertEquals(Arrays.asList("skeleton pressed", "skeleton dragged", "skeleton released", "skeleton scrolled", "skeleton typed"), events);
		ready[0] = true;
		this.bridges.frame();
		events.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void loadsItsSkeletonInItsUi() {
		final RectNode early = RectNode.create(0D, 0D, 10D, 10D).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D));
		final ContainerNode container = ContainerNode.create(0D, 0D, 500D, 500D).append(early);
		Assert.assertFalse(early.getSkeleton().hasUi());
		this.bridges.open(new NodeUI(container));
		Assert.assertTrue(early.getSkeleton().hasUi());
		final RectNode late = RectNode.create(0D, 0D, 10D, 10D).attach(container).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D));
		Assert.assertTrue(late.getSkeleton().hasUi());
		Assert.assertSame(late, late.getSkeleton().getParent());
	}

	@Test
	public void ignoresAMissingSkeleton() {
		Assert.assertNull(RectNode.create(0D, 0D, 10D, 10D).skeleton(rect -> null).getSkeleton());
	}

	@Test
	public void mountsItsChildrenWithItself() {
		final boolean[] ready = {false};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode parent = RectNode.create(0D, 0D, 100D, 100D).append(child).wait(rect -> ready[0]).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D));
		Assert.assertFalse(child.isMounted());
		Assert.assertTrue(parent.getSkeleton().isMounted());
		ready[0] = true;
		Assert.assertTrue(child.isMounted());
	}

	@Test
	public void waitsForEveryCondition() {
		final boolean[] first = {false};
		final boolean[] second = {false};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).wait(rect -> first[0]).wait(rect -> second[0]);
		Assert.assertFalse(node.isMounted());
		first[0] = true;
		Assert.assertFalse(node.isMounted());
		second[0] = true;
		Assert.assertTrue(node.isMounted());
	}

	@Test
	public void waitsForItsSignalToHoldAValue() {
		final Signal<String> signal = new Signal<>("default");
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).wait(signal);
		Assert.assertFalse(node.isMounted());
		signal.set("loaded");
		Assert.assertTrue(node.isMounted());
	}

	@Test
	public void waitsForItsDelay() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).wait(500L, TimeUnit.MILLISECONDS);
		Assert.assertFalse(node.isMounted());
		this.bridges.getClock().advance(499L);
		Assert.assertFalse(node.isMounted());
		this.bridges.getClock().advance(1L);
		Assert.assertTrue(node.isMounted());
	}

	@Test
	public void scrollsHorizontallyWithTheWheel() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		this.bridges.move(300D, 150D).frames(2);
		Assert.assertTrue(row.hasOverflowX());
		Assert.assertFalse(row.hasOverflowY());
		Assert.assertEquals(300D, row.getMaxScrollX(), 0D);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-30D, row.getTargetScrollX(), 0D);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-90D, row.getTargetScrollX(), 0D);
		this.bridges.getWindow().getKeys().clear();
		this.bridges.scroll(1D);
		Assert.assertEquals(-60D, row.getTargetScrollX(), 0D);
	}

	@Test
	public void scrollsVerticallyWithTheWheelWhenItOverflowsBothWays() {
		final ContainerNode area = NodeTest.column();
		RectNode.create(0D, 0D, 700D, 10D).attach(area);
		this.bridges.open(new NodeUI(area));
		this.bridges.move(300D, 150D).frames(2);
		Assert.assertTrue(area.hasOverflowX());
		Assert.assertTrue(area.hasOverflowY());
		this.bridges.scroll(-1D);
		Assert.assertEquals(-30D, area.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollX(), 0D);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-60D, area.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollX(), 0D);
		this.bridges.getWindow().getKeys().clear();
		area.scrollOffsetX(-30D).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(-30D, area.getChildren().get(0).getX(), 1E-3D);
		Assert.assertEquals(-60D, area.getChildren().get(0).getY(), 1E-3D);
	}

	@Test
	public void scrollsHorizontallyWithTheHorizontalWheelWhenItOverflowsBothWays() {
		final ContainerNode area = NodeTest.column();
		RectNode.create(0D, 0D, 700D, 10D).attach(area);
		this.bridges.open(new NodeUI(area));
		this.bridges.move(300D, 150D).frames(2);
		this.bridges.scroll(-1D, 0D);
		Assert.assertEquals(-30D, area.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollY(), 0D);
		this.bridges.scroll(-1D, -1D);
		Assert.assertEquals(-60D, area.getTargetScrollX(), 0D);
		Assert.assertEquals(-30D, area.getTargetScrollY(), 0D);
		this.bridges.scroll(2D, 0D);
		Assert.assertEquals(-30D, area.getTargetScrollX(), 0D);
	}

	@Test
	public void leavesTheHorizontalWheelWithoutHorizontalOverflow() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column));
		this.bridges.move(300D, 150D).frames(2);
		final InternalContext context = InternalContext.create();
		column.onMouseScroll(300D, 150D, -1D, 0D, context);
		Assert.assertFalse(context.isCancelled());
		Assert.assertEquals(0D, column.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheHorizontalWheelAtItsHorizontalEnd() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		this.bridges.move(300D, 150D).frames(2);
		final InternalContext context = InternalContext.create();
		row.onMouseScroll(300D, 150D, 1D, 0D, context);
		Assert.assertFalse(context.isCancelled());
		Assert.assertEquals(0D, row.getTargetScrollX(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsVerticalEndWhenItOverflowsBothWays() {
		final ContainerNode area = NodeTest.column();
		RectNode.create(0D, 0D, 700D, 10D).attach(area);
		this.bridges.open(new NodeUI(area)).move(300D, 150D).frames(2);
		area.scrollRatioY(1F);
		final InternalContext context = InternalContext.create();
		area.onMouseScroll(300D, 150D, 0D, -1D, context);
		Assert.assertFalse(context.isCancelled());
		Assert.assertEquals(-200D, area.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollX(), 0D);
	}

	@Test
	public void forgetsItsScrollOnceItStopsScrolling() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		column.scrollOffsetY(-100D).updateScroll();
		this.bridges.frame();
		Assert.assertSame(column, column.overflow(OverflowProperty.HIDDEN));
		Assert.assertFalse(column.hasOverflowY());
		Assert.assertEquals(0D, column.getScrollY(), 0D);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
		Assert.assertEquals(100D, column.getChildren().get(1).getY(), 0D);
		final InternalContext context = InternalContext.create();
		column.onMouseScroll(300D, 150D, 0D, -1D, context);
		Assert.assertFalse(context.isCancelled());
	}

	@Test
	public void scrollsByItsScrollSpeed() {
		final ContainerNode row = NodeTest.row().scrollSpeed(0.5D);
		this.bridges.open(new NodeUI(row));
		this.bridges.move(300D, 150D).frames(2).scroll(-1D);
		Assert.assertEquals(0.5D, row.getScrollSpeed(), 0D);
		Assert.assertEquals(-15D, row.getTargetScrollX(), 0D);
	}

	@Test
	public void consumesTheWheelWhileItScrolls() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		final InternalContext down = InternalContext.create();
		column.onMouseScroll(300D, 150D, 0D, -1D, down);
		Assert.assertTrue(down.isCancelled());
		Assert.assertEquals(-30D, column.getTargetScrollY(), 0D);
		final InternalContext up = InternalContext.create();
		column.onMouseScroll(300D, 150D, 0D, 1D, up);
		Assert.assertTrue(up.isCancelled());
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsLimits() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		final InternalContext up = InternalContext.create();
		column.onMouseScroll(300D, 150D, 0D, 1D, up);
		Assert.assertFalse(up.isCancelled());
		column.scrollRatioY(1F);
		final InternalContext down = InternalContext.create();
		column.onMouseScroll(300D, 150D, 0D, -1D, down);
		Assert.assertFalse(down.isCancelled());
		Assert.assertEquals(-200D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentWithNothingToScroll() {
		final ContainerNode box = ContainerNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL);
		RectNode.create(0D, 0D, 400D, 100D).attach(box);
		this.bridges.open(new NodeUI(box)).move(300D, 150D).frames(2);
		final InternalContext down = InternalContext.create();
		final InternalContext up = InternalContext.create();
		box.onMouseScroll(300D, 150D, 0D, -1D, down);
		box.onMouseScroll(300D, 150D, 0D, 1D, up);
		Assert.assertFalse(down.isCancelled());
		Assert.assertFalse(up.isCancelled());
	}

	@Test
	public void leavesAStillWheelToItsParent() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		final InternalContext context = InternalContext.create();
		column.onMouseScroll(300D, 150D, 0D, 0D, context);
		Assert.assertFalse(context.isCancelled());
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void chainsTheWheelBetweenNestedScrollContainers() {
		final ContainerNode outer = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		final ContainerNode inner = ContainerNode.create(0D, 0D, 400D, 100D).overflow(OverflowProperty.SCROLL).attach(outer);
		RectNode.create(0D, 0D, 400D, 300D).attach(inner);
		RectNode.create(0D, 100D, 400D, 500D).attach(outer);
		this.bridges.open(new NodeUI(outer)).move(300D, 150D).frames(2);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-30D, inner.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, outer.getTargetScrollY(), 0D);
		inner.scrollRatioY(1F);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-200D, inner.getTargetScrollY(), 0D);
		Assert.assertEquals(-30D, outer.getTargetScrollY(), 0D);
		this.bridges.scroll(1D);
		Assert.assertEquals(-170D, inner.getTargetScrollY(), 0D);
		Assert.assertEquals(-30D, outer.getTargetScrollY(), 0D);
	}

	@Test
	public void easesTowardsItsScrollTarget() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-300D);
		this.bridges.frame();
		Assert.assertEquals(-19.2D, row.getScrollX(), 1E-9D);
		Assert.assertEquals(381D, row.getChildren().get(1).getX(), 1E-4D);
	}

	@Test
	public void reportsTheEndOfAHorizontalScrollOnceItRests() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEnd((container, scrollX, scrollY) -> ends.add(scrollX));
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-1000D);
		Assert.assertEquals(-300D, row.getTargetScrollX(), 0D);
		Assert.assertTrue(row.isScrollEndX());
		this.bridges.frame();
		Assert.assertTrue(ends.isEmpty());
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList(-300D), ends);
		Assert.assertFalse(row.isScrollEndX());
	}

	@Test
	public void reportsTheEndOfAVerticalScrollOnceItRests() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode column = NodeTest.column().onScrollEnd((container, scrollX, scrollY) -> ends.add(scrollY));
		this.bridges.open(new NodeUI(column));
		column.scrollOffsetY(-1000D);
		Assert.assertEquals(-200D, column.getTargetScrollY(), 0D);
		Assert.assertTrue(column.isScrollEndY());
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList(-200D), ends);
		Assert.assertFalse(column.isScrollEndY());
	}

	@Test
	public void forgetsTheEndOfAScrollLeftBeforeItRests() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEnd((container, scrollX, scrollY) -> ends.add(scrollX));
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-1000D);
		this.bridges.frame();
		row.scrollOffsetX(-100D);
		this.bridges.frames(200);
		Assert.assertFalse(row.isScrollEndX());
		Assert.assertTrue(ends.isEmpty());
	}

	@Test
	public void announcesTheEndOfAScrollAsSoonAsItAimsAtIt() {
		final List<String> events = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEnding((container, scrollX, scrollY) -> events.add("ending " + scrollX)).onScrollEnd((container, scrollX, scrollY) -> events.add("end " + scrollX));
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-1000D);
		Assert.assertEquals(Arrays.asList("ending -300.0"), events);
		row.scrollOffsetX(-2000D);
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList("ending -300.0", "end -300.0"), events);
	}

	@Test
	public void announcesTheEndOfAVerticalScrollWithTheOffsetsItHeadsTo() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode column = NodeTest.column().onScrollEnding((container, scrollX, scrollY) -> endings.add(scrollY));
		this.bridges.open(new NodeUI(column));
		column.scrollOffsetY(-1000D);
		Assert.assertEquals(Arrays.asList(-200D), endings);
		Assert.assertEquals(0D, column.getScrollY(), 0D);
	}

	@Test
	public void announcesTheEndAgainOnceTheScrollComesBackToIt() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEnding((container, scrollX, scrollY) -> endings.add(scrollX));
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-1000D);
		row.scrollOffsetX(-100D);
		row.scrollOffsetX(-1000D);
		Assert.assertEquals(Arrays.asList(-300D, -300D), endings);
	}

	@Test
	public void waitsForTheEndOfAListThatGrowsWhileScrolling() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEnding((container, scrollX, scrollY) -> {
			if (container.getChildren().size() == 2) {
				RectNode.create(700D, 0D, 300D, 100D).attach(container);
			}
		}).onScrollEnd((container, scrollX, scrollY) -> ends.add(scrollX));
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-1000D);
		this.bridges.frames(200);
		Assert.assertEquals(-300D, row.getScrollX(), 0D);
		Assert.assertTrue(ends.isEmpty());
		row.scrollOffsetX(-1000D);
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList(-600D), ends);
	}

	@Test
	public void scrollsToAShareOfItsOverflow() {
		final ContainerNode row = NodeTest.row();
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 1920D, 1080D).append(row, column)));
		row.scrollRatioX(0.5F);
		Assert.assertEquals(-150D, row.getTargetScrollX(), 0D);
		row.scrollRatioX(2F);
		Assert.assertEquals(-300D, row.getTargetScrollX(), 0D);
		column.scrollRatioY(0.25F);
		Assert.assertEquals(-50D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void scrollsByAWeightedStep() {
		final ContainerNode row = NodeTest.row();
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 1920D, 1080D).append(row, column)));
		row.scrollX(-10D, 3D).scrollX(-10D, 3D);
		Assert.assertEquals(-60D, row.getTargetScrollX(), 0D);
		column.scrollY(-20D, 2D);
		Assert.assertEquals(-40D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void reportsEveryScrollItAimsAt() {
		final List<Double> values = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollUpdate((container, value) -> values.add(value));
		this.bridges.open(new NodeUI(row));
		row.scrollOffsetX(-50D);
		this.bridges.move(300D, 150D).frames(2).scroll(-1D);
		Assert.assertEquals(Arrays.asList(-50D, -80D), values);
	}

	@Test
	public void jumpsStraightToItsScrollTarget() {
		final ContainerNode row = NodeTest.row();
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 1920D, 1080D).append(row, column)));
		row.scrollOffsetX(-100D).updateScrollX();
		Assert.assertEquals(-100D, row.getScrollX(), 0D);
		column.scrollOffsetY(-50D).updateScrollY();
		Assert.assertEquals(-50D, column.getScrollY(), 0D);
		row.scrollOffsetX(-200D).updateScroll();
		Assert.assertEquals(-200D, row.getScrollX(), 0D);
	}

	@Test
	public void movesItsScrollbarAlongTheHorizontalScroll() {
		final Bar bar = new Bar(0D, 110D, 40D, 10D, BoundingBox.create(0D, 110D, 400D, 10D));
		final ContainerNode row = NodeTest.row().scrollbar(bar);
		this.bridges.open(new NodeUI(row));
		Assert.assertSame(bar, row.getScrollbar());
		Assert.assertSame(row, bar.getScrollNode());
		Assert.assertSame(row, bar.getParent());
		row.scrollOffsetX(-150D).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(180D, bar.getX(), 1E-9D);
		Assert.assertEquals(280D, this.draw(0.6F, 0.4F, 0.2F).getLeft(), 1E-3D);
	}

	@Test
	public void movesItsScrollbarAlongTheVerticalScroll() {
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final ContainerNode column = NodeTest.column().scrollbar(bar);
		this.bridges.open(new NodeUI(column));
		column.scrollOffsetY(-100D).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(40D, bar.getY(), 1E-9D);
		Assert.assertEquals(140D, this.draw(0.6F, 0.4F, 0.2F).getTop(), 1E-3D);
	}

	@Test
	public void followsItsDraggedScrollbarFaster() {
		final Bar bar = new Bar(0D, 110D, 40D, 10D, BoundingBox.create(0D, 110D, 400D, 10D));
		final ContainerNode row = NodeTest.row().scrollbar(bar);
		this.press(row, 120D, 215D);
		Assert.assertTrue(bar.isDragging());
		this.bridges.move(300D, 215D).frames(2);
		Assert.assertEquals(-150D, row.getTargetScrollX(), 1E-6D);
		Assert.assertEquals(-48D, row.getScrollX(), 1E-6D);
	}

	@Test
	public void forwardsTheMouseAndKeysToItsScrollbar() {
		final List<String> events = new ArrayList<>();
		final Bar bar = new Bar(0D, 110D, 40D, 10D, BoundingBox.create(0D, 110D, 400D, 10D)).onKeyPressed((scrollbar, c, key) -> events.add("typed")).onMouseScroll((scrollbar, mouseX, mouseY, valueX, value) -> events.add("scrolled")).onMouseDragged((scrollbar, mouseX, mouseY, clickType, deltaTime) -> events.add("dragged")).onMouseReleased((scrollbar, mouseX, mouseY, clickType) -> events.add("released"));
		this.bridges.open(new NodeUI(NodeTest.row().scrollbar(bar)));
		this.bridges.getUi().keyTyped('a', Key.A);
		this.bridges.scroll(1D);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("typed", "scrolled", "dragged", "released"), events);
	}

	@Test
	public void loadsAScrollbarSetOnceItsUiIsOpen() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		final Bar bar = new Bar(0D, 110D, 40D, 10D, BoundingBox.create(0D, 110D, 400D, 10D));
		row.scrollbar(bar);
		Assert.assertTrue(bar.hasUi());
	}

	@Test
	public void stopsDraggingOnceTheMouseIsGrabbed() {
		final int[] ends = {0};
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragEnd(rect -> ends[0]++);
		this.press(node, 110D, 110D);
		Assert.assertTrue(node.isDragging());
		final GrabbedWindow window = new GrabbedWindow(this.bridges.getWindow());
		BridgeHandler.WINDOW.register(window);
		try {
			this.bridges.frame();
		} finally {
			BridgeHandler.WINDOW.unregister(window);
		}
		Assert.assertFalse(node.isDragging());
		Assert.assertEquals(1, ends[0]);
	}

	@Test
	public void bringsADroppedNodeBackPastTheStartOfItsArea() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.custom(50D, 60D, 300D, 300D));
		this.press(node, 110D, 110D);
		this.drop(0D, 0D);
		Assert.assertEquals(50D, node.getX(), 0D);
		Assert.assertEquals(60D, node.getY(), 0D);
	}

	@Test
	public void bringsADroppedNodeBackBeforeTheEndOfItsArea() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.custom(50D, 60D, 300D, 300D));
		this.press(node, 110D, 110D);
		this.drop(110D, 600D);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(310D, node.getY(), 0D);
	}

	@Test
	public void keepsADroppedNodeInsideItsParent() {
		final RectNode node = RectNode.create(20D, 20D, 50D, 50D).draggable(DraggableProperty.parent());
		this.press(ContainerNode.create(100D, 100D, 200D, 200D).append(node), 130D, 130D);
		this.drop(600D, 600D);
		Assert.assertEquals(150D, node.getX(), 0D);
		Assert.assertEquals(150D, node.getY(), 0D);
		Assert.assertEquals(250D, node.getAbsoluteX(), 0D);
	}

	@Test
	public void movesIntoTheNodeItIsBoundTo() {
		final RectNode zone = RectNode.create(300D, 300D, 200D, 200D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.node(zone));
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 1920D, 1080D).append(zone, node))).frames(100);
		Assert.assertEquals(300D, node.getX(), 0D);
		Assert.assertEquals(300D, node.getY(), 0D);
		Assert.assertFalse(node.isDragged());
	}

	@Test
	public void keepsADroppedNodeInsideTheUi() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.ui());
		this.press(node, 110D, 110D);
		this.drop(1900D, 110D);
		Assert.assertEquals(1870D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
	}

	@Test
	public void keepsADroppedNodeOnTheScreen() {
		this.bridges.resize(2560, 1080);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.screen());
		this.press(node, 430D, 110D);
		this.drop(0D, 110D);
		Assert.assertEquals(-320D, node.getX(), 1E-9D);
		Assert.assertEquals(100D, node.getY(), 1E-9D);
	}

	@Test
	public void dropsANodeOnTheTargetItOverlaps() {
		final List<Node> snaps = new ArrayList<>();
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().snap(DraggableSnapType.OVERLAP, target)).onSnap((rect, snapNode) -> snaps.add(snapNode));
		this.press(node, 110D, 110D);
		this.bridges.move(430D, 430D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(100);
		Assert.assertEquals(Arrays.asList(target), snaps);
		Assert.assertEquals(400D, node.getX(), 0D);
		Assert.assertEquals(400D, node.getY(), 0D);
	}

	@Test
	public void snapsTheDraggedCopyOfANode() {
		final List<Node> snaps = new ArrayList<>();
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY).snap(DraggableSnapType.OVERLAP, target)).onSnap((rect, snapNode) -> snaps.add(snapNode));
		this.press(node, 110D, 110D);
		this.bridges.move(430D, 430D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(420D, node.getDraggedNode().getX(), 0D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(target), snaps);
		Assert.assertNull(node.getDraggedNode());
		Assert.assertEquals(100D, node.getX(), 0D);
	}

	@Test
	public void sendsADroppedNodeBackWithoutTargetUnderIt() {
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().snap(DraggableSnapType.OVERLAP, target));
		this.press(node, 110D, 110D);
		this.bridges.move(310D, 110D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(300D, node.getX(), 0D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(100);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
	}

	@Test
	public void drawsTheDraggedCopyOfAChildUnderTheMouse() {
		final RectNode node = RectNode.create(10D, 10D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).draggable(DraggableProperty.free().type(DraggableType.COPY));
		this.press(ContainerNode.create(300D, 200D, 500D, 500D).append(node), 320D, 220D);
		this.bridges.move(520D, 420D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(310D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(510D, draws.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(410D, draws.get(1).getTop(), 1E-3D);
	}

	@Test
	public void reportsTheStartEveryMoveAndTheEndOfADrag() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragStart(rect -> events.add("start")).onDrag(rect -> events.add("drag")).onDragEnd(rect -> events.add("end"));
		this.press(node, 110D, 110D);
		this.bridges.move(150D, 110D).frames(5);
		this.bridges.getUi().mouseMoved();
		this.bridges.move(200D, 110D).frames(5);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("start", "drag", "drag", "end"), events);
	}

	@Test
	public void followsTheMouseOnceDraggedProgrammatically() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free());
		this.bridges.open(new NodeUI(node));
		Assert.assertSame(node, node.dragging(true, 110D, 110D));
		Assert.assertTrue(node.isDragging());
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		this.bridges.move(210D, 160D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(200D, node.getX(), 0D);
		Assert.assertEquals(150D, node.getY(), 0D);
		node.dragging(false, 0D, 0D);
		Assert.assertFalse(node.isDragging());
		Assert.assertNull(node.getDraggedNode());
	}

	@Test
	public void startsAndStopsADragProgrammatically() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY));
		this.bridges.open(new NodeUI(node));
		node.startDragging(110D, 110D);
		Assert.assertTrue(node.isDragging());
		Assert.assertEquals(100D, node.getStartDragX(), 0D);
		Assert.assertEquals(100D, node.getDraggedNode().getX(), 0D);
		Assert.assertSame(PositionProperty.ABSOLUTE, node.getDraggedNode().getPosition());
		node.stopDragging();
		Assert.assertFalse(node.isDragging());
		Assert.assertNull(node.getDraggedNode());
	}

	@Test
	public void ignoresAPressWithAnotherButton() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free());
		this.bridges.open(new NodeUI(node));
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertFalse(node.isDragging());
	}

	@Test
	public void letsAChildKeepThePressFromItsDraggableParent() {
		final RectNode child = RectNode.create(0D, 0D, 20D, 20D).onClick((rect, mouseX, mouseY, clickType) -> {});
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).draggable(DraggableProperty.free()).append(child);
		this.press(parent, 110D, 110D);
		Assert.assertFalse(parent.isDragging());
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.move(150D, 150D).frame();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertTrue(parent.isDragging());
	}

	@Test
	public void startsTheDragOfTheFrontNodeOnly() {
		final RectNode back = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free());
		final RectNode front = RectNode.create(120D, 120D, 50D, 50D).draggable(DraggableProperty.free());
		this.press(ContainerNode.create(0D, 0D, 400D, 400D).append(back, front), 130D, 130D);
		Assert.assertTrue(front.isDragging());
		Assert.assertFalse(back.isDragging());
	}

	@Test
	public void startsTheDragOfAChildBeforeItsDraggableParent() {
		final RectNode child = RectNode.create(0D, 0D, 20D, 20D).draggable(DraggableProperty.free());
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).draggable(DraggableProperty.free()).append(child);
		this.press(parent, 110D, 110D);
		Assert.assertTrue(child.isDragging());
		Assert.assertFalse(parent.isDragging());
	}

	@Test
	public void sendsTheNodeBackWhenItsDragEndIsRefused() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragEnd(new NodeDragCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode rect) {
				events.add("end");
			}

			@Override
			public void pre(final @NonNull RectNode rect, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		this.press(node, 110D, 110D);
		this.bridges.move(310D, 210D).frames(100);
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(300D, node.getX(), 0D);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertFalse(node.isDragging());
		this.bridges.frames(100);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void dropsTheCopyOfARefusedDragEnd() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY)).onDragEnd(new NodeDragCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode rect) {}

			@Override
			public void pre(final @NonNull RectNode rect, final @NonNull InternalContext context) {
				context.cancel();
			}

		});
		this.press(node, 110D, 110D);
		this.drop(310D, 210D);
		Assert.assertFalse(node.isDragging());
		Assert.assertFalse(node.isDragged());
		Assert.assertNull(node.getDraggedNode());
		Assert.assertEquals(100D, node.getX(), 0D);
	}

	@Test
	public void keepsTheNodeInsideItsAreaDuringTheDrag() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.custom(0D, 0D, 300D, 300D));
		this.press(node, 110D, 110D);
		this.bridges.move(410D, 510D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertTrue(node.isDragging());
		Assert.assertEquals(250D, node.getX(), 0D);
		Assert.assertEquals(250D, node.getY(), 0D);
	}

	@Test
	public void showsTheDroppedCopyAndItsSnapToTheDragEnd() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final DraggableProperty draggable = DraggableProperty.free().type(DraggableType.COPY).snap(DraggableSnapType.OVERLAP, target);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(draggable).onDragEnd(rect -> received.addAll(Arrays.asList(rect.getDraggedNode().getX(), rect.getDraggable().getSnapping(rect.getDraggedNode()))));
		this.press(node, 110D, 110D);
		this.bridges.move(430D, 430D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList(420D, target), received);
		Assert.assertNull(node.getDraggedNode());
		Assert.assertFalse(node.isDragged());
	}

	@Test
	public void refusesToStartADragWithoutDraggableProperty() {
		try {
			RectNode.create(100D, 100D, 50D, 50D).startDragging(110D, 110D);
			Assert.fail("A node without DraggableProperty cannot start a drag");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The node RectNode has no DraggableProperty, call draggable(...) first", expected.getMessage());
		}
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToDragWithoutDraggableProperty() {
		RectNode.create(100D, 100D, 50D, 50D).dragging(true, 110D, 110D);
	}

	@Test
	public void refusesToDragATopLevelNodeInsideItsParent() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.parent());
		try {
			this.press(node, 110D, 110D);
			Assert.fail("A top level node has no parent to be dragged inside");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The node RectNode is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()", expected.getMessage());
		}
		Assert.assertFalse(node.isDragging());
		this.bridges.frames(2);
		Assert.assertEquals(100D, node.getX(), 0D);
	}

	@Test
	public void bringsAChildBackInsideItsParentFromItsAttachedPosition() {
		final RectNode child = RectNode.create(480D, 10D, 50D, 50D).draggable(DraggableProperty.parent());
		this.bridges.open(new NodeUI(ContainerNode.create(300D, 200D, 500D, 500D).append(child))).frames(100);
		Assert.assertEquals(450D, child.getX(), 0D);
		Assert.assertEquals(10D, child.getY(), 0D);
	}

	@Test
	public void keepsADroppedChildInItsScrolledParent() {
		final RectNode child = RectNode.create(10D, 10D, 50D, 50D).draggable(DraggableProperty.parent());
		final ContainerNode list = ContainerNode.create(100D, 100D, 400D, 200D).overflow(OverflowProperty.SCROLL).append(RectNode.create(0D, 0D, 400D, 1000D), child);
		this.press(list, 120D, 120D);
		this.drop(120D, 220D);
		Assert.assertEquals(110D, child.getY(), 0D);
		Assert.assertEquals(110D, child.getDefaultY(), 0D);
		list.scrollOffsetY(-50D).updateScroll();
		this.bridges.frames(2);
		Assert.assertEquals(60D, child.getY(), 1E-5D);
	}

	@Test
	public void appendsItsChildrenInOneCall() {
		final List<Node> appended = new ArrayList<>();
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).onAppend((container, child) -> appended.add(child));
		final RectNode first = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode second = RectNode.create(10D, 0D, 10D, 10D);
		Assert.assertSame(parent, parent.append(first, second));
		Assert.assertEquals(Arrays.asList(first, second), appended);
		Assert.assertEquals(Arrays.asList(first, second), parent.getChildren().ordered());
		Assert.assertSame(parent, second.getParent());
		Assert.assertFalse(first.hasUi());
	}

	@Test
	public void movesAChildAppendedToAnotherParent() {
		final List<String> events = new ArrayList<>();
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D);
		final ContainerNode first = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		final ContainerNode second = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		Assert.assertFalse(first.getChildren().contains(child));
		Assert.assertTrue(second.getChildren().contains(child));
		Assert.assertSame(second, child.getParent());
		Assert.assertEquals(Arrays.asList("child detach"), events);
	}

	@Test
	public void movesATopLevelNodeIntoAParent() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D);
		final NodeUI ui = new NodeUI(parent);
		this.bridges.open(ui);
		node.attach(ui);
		parent.append(node);
		Assert.assertFalse(ui.getNodeList().contains(node));
		Assert.assertSame(parent, node.getParent());
	}

	@Test
	public void removesItsChildrenAndDetachesThem() {
		final List<String> events = new ArrayList<>();
		final RecordingNode first = new RecordingNode("first", events, 0D, 0D, 10D, 10D).onDetach(target -> events.add("callback"));
		final RecordingNode second = new RecordingNode("second", events, 0D, 0D, 10D, 10D);
		final RectNode kept = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode stranger = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(first, kept, second);
		Assert.assertSame(parent, parent.remove(first, second, stranger));
		Assert.assertEquals(Arrays.asList(kept), parent.getChildren().ordered());
		Assert.assertEquals(Arrays.asList("first detach", "callback", "second detach"), events);
		Assert.assertNull(first.getParent());
	}

	@Test
	public void disablesItsWholeTree() {
		final int[] clicks = {0};
		final boolean[] enabled = {false};
		final RectNode child = RectNode.create(0D, 0D, 50D, 50D).onClick((rect, mouseX, mouseY, clickType) -> clicks[0]++);
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).enabled(rect -> enabled[0]).append(child);
		this.bridges.open(new NodeUI(parent));
		this.bridges.move(110D, 110D).frames(2);
		Assert.assertFalse(child.isEnabled());
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(0, clicks[0]);
		enabled[0] = true;
		Assert.assertTrue(child.isEnabled());
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(1, clicks[0]);
	}

	@Test
	public void loadsAChildAppendedOnceItsUiIsOpen() {
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D);
		final NodeUI ui = new NodeUI(parent);
		this.bridges.open(ui);
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).attach(parent);
		Assert.assertTrue(child.hasUi());
		Assert.assertSame(ui, child.getUi());
	}

	@Test
	public void attachesItselfToAUi() {
		final NodeUI ui = new NodeUI(ContainerNode.create(0D, 0D, 10D, 10D));
		this.bridges.open(ui);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).attach(ui);
		Assert.assertTrue(ui.getNodeList().contains(node));
		Assert.assertSame(ui, node.getUi());
	}

	@Test
	public void findsItsChildrenByClass() {
		final RectNode first = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode box = ContainerNode.create(0D, 0D, 10D, 10D);
		final RectNode second = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(first, box, second);
		Assert.assertSame(first, parent.getChild(0, RectNode.class));
		Assert.assertSame(second, parent.getChild(1, RectNode.class));
		Assert.assertSame(box, parent.getChild(0, ContainerNode.class));
		Assert.assertNull(parent.getChild(2, RectNode.class));
		Assert.assertEquals(Arrays.asList(first, second), parent.getChildren(RectNode.class).ordered());
		Assert.assertEquals(3, parent.getChildren(Node.class).size());
	}

	@Test
	public void detachesItsChildrenWhenCleared() {
		final List<String> events = new ArrayList<>();
		final RecordingNode grandchild = new RecordingNode("grandchild", events, 0D, 0D, 10D, 10D);
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D).append(grandchild).onDetach(target -> events.add("callback"));
		final RectNode parent = RectNode.create(0D, 0D, 100D, 100D).append(child);
		Assert.assertSame(parent, parent.clearChildren());
		Assert.assertEquals(Arrays.asList("grandchild detach", "child detach", "callback"), events);
		Assert.assertTrue(parent.getChildren().isEmpty());
	}

	@Test
	public void clearsTheParentOfItsChildrenWhenCleared() {
		final RectNode grandchild = RectNode.create(0D, 0D, 5D, 5D);
		final RectNode child = RectNode.create(10D, 20D, 10D, 10D).append(grandchild);
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).append(child).visible(node -> false).enabled(node -> false);
		final List<Node> parents = new ArrayList<>();
		child.onDetach(node -> parents.add(node.getParent()));
		Assert.assertFalse(child.isVisible());
		Assert.assertFalse(child.isEnabled());
		Assert.assertEquals(110D, child.getAbsoluteX(), 0D);
		parent.clearChildren();
		Assert.assertEquals(Collections.singletonList(parent), parents);
		Assert.assertNull(child.getParent());
		Assert.assertSame(child, grandchild.getParent());
		Assert.assertTrue(child.isVisible());
		Assert.assertTrue(child.isEnabled());
		Assert.assertEquals(10D, child.getAbsoluteX(), 0D);
	}

	@Test
	public void describesItsHierarchy() {
		final PointNode leaf = new PointNode(0D, 0D);
		final ContainerNode root = ContainerNode.create(0D, 0D, 100D, 100D).append(RectNode.create(0D, 0D, 10D, 10D).append(leaf));
		Assert.assertEquals("ContainerNode", root.getHierarchy());
		Assert.assertEquals("ContainerNode - RectNode - PointNode", leaf.getHierarchy());
	}

	@Test
	public void mapsItsIndexInTheTree() {
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D);
		final NodeUI ui = new NodeUI(ContainerNode.create(0D, 0D, 100D, 100D).append(RectNode.create(0D, 0D, 10D, 10D), child));
		Assert.assertEquals("N/A", child.getMappedIndex());
		this.bridges.open(ui);
		final ContainerNode second = ContainerNode.create(0D, 0D, 100D, 100D).attach(ui);
		Assert.assertEquals("0", child.getParent().getMappedIndex());
		Assert.assertEquals("1", second.getMappedIndex());
		Assert.assertEquals("0.1", child.getMappedIndex());
	}

	@Test
	public void runsItsBodyRightAway() {
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).body(rect -> RectNode.create(0D, 0D, 10D, 10D).attach(rect));
		Assert.assertEquals(1, node.getChildren().size());
		node.getBodyConsumer().accept(node);
		Assert.assertEquals(2, node.getChildren().size());
		final int[] runs = {0};
		RectNode.create(0D, 0D, 10D, 10D).body(() -> runs[0]++);
		Assert.assertEquals(1, runs[0]);
	}

	@Test
	public void runsItsSelfConsumerRightAwayWithoutStoringIt() {
		final List<Node> received = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).self(received::add);
		Assert.assertEquals(Arrays.asList(node), received);
		Assert.assertNull(node.getBodyConsumer());
	}

	@Test
	public void keepsItsBodyWhenItsSelfConsumerRuns() {
		final Signal<Integer> count = new Signal<>(1);
		final int[] runs = {0, 0};
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).watch(count, WatchProperty.BODY).body(() -> runs[0]++).self(rect -> runs[1]++);
		final Consumer<Node> body = node.getBodyConsumer();
		this.bridges.open(new NodeUI(node));
		count.set(2);
		Assert.assertSame(body, node.getBodyConsumer());
		Assert.assertArrayEquals(new int[] {2, 1}, runs);
	}

	@Test
	public void findsTheUiBeingInitialized() {
		final CurrentUI ui = new CurrentUI();
		this.bridges.open(ui);
		Assert.assertSame(ui, ui.found);
		Assert.assertFalse(ui.bound);
		Assert.assertNull(RectNode.create(0D, 0D, 10D, 10D).getUi());
	}

	@Test
	public void sharesTheStoresOfItsUi() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		final NodeUI ui = new NodeUI(node);
		this.bridges.open(ui);
		Assert.assertSame(ui.useStore(CountStore.class), node.useStore(CountStore.class));
	}

	@Test
	public void takesTheUiItIsGiven() {
		final NodeUI ui = new NodeUI(ContainerNode.create(0D, 0D, 10D, 10D));
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).ui(ui);
		Assert.assertTrue(node.hasUi());
		Assert.assertSame(ui, node.getUi());
	}

	@Test
	public void copiesItsBoundsPropertiesAndChildren() {
		final Color color = new Color(0.2F, 0.4F, 0.6F, 1F);
		final DraggableProperty draggable = DraggableProperty.free();
		final RectNode child = RectNode.create(1D, 2D, 3D, 4D).color(color);
		final RectNode node = RectNode.create(10D, 20D, 30D, 40D).color(color).append(child).position(PositionProperty.ABSOLUTE).overflow(OverflowProperty.HIDDEN).anchorX(Align.CENTER).anchorY(Align.END).draggable(draggable).zindex(3).zlevel(4D).aspectRatio(0.5D);
		node.x(15D);
		final RectNode copy = node.copy();
		Assert.assertNotSame(node, copy);
		Assert.assertEquals(15D, copy.getX(), 0D);
		Assert.assertEquals(20D, copy.getY(), 0D);
		Assert.assertEquals(30D, copy.getWidth(), 0D);
		Assert.assertEquals(40D, copy.getHeight(), 0D);
		Assert.assertSame(PositionProperty.ABSOLUTE, copy.getPosition());
		Assert.assertSame(OverflowProperty.HIDDEN, copy.getOverflow());
		Assert.assertSame(Align.CENTER, copy.getAnchorX());
		Assert.assertSame(Align.END, copy.getAnchorY());
		Assert.assertSame(draggable, copy.getDraggable());
		Assert.assertEquals(3, copy.getZindex());
		Assert.assertEquals(4D, copy.getZlevel(), 0D);
		Assert.assertEquals(0.5D, copy.getAspectRatio(), 0D);
		Assert.assertSame(color, copy.getColor());
		final RectNode childCopy = copy.getChild(0, RectNode.class);
		Assert.assertNotSame(child, childCopy);
		Assert.assertSame(copy, childCopy.getParent());
		Assert.assertEquals(3D, childCopy.getWidth(), 0D);
		Assert.assertSame(color, childCopy.getColor());
		Assert.assertEquals(1, node.getChildren().size());
	}

	@Test
	public void copiesItsHoverScrollWaitsAnimatorsAndScrollbar() {
		final TweenAnimator animator = TweenAnimator.create();
		final Bar bar = new Bar(410D, 0D, 10D, 20D, BoundingBox.create(410D, 0D, 10D, 100D));
		final ContainerNode column = NodeTest.column().hoverDuration(50L).hoverEquation(TweenEquations.QUAD_IN).scrollSpeed(2D).wait(node -> false).animate(animator).scrollbar(bar);
		final ContainerNode copy = column.copy();
		Assert.assertEquals(50L, copy.getHoverDuration());
		Assert.assertSame(TweenEquations.QUAD_IN, copy.getHoverEquation());
		Assert.assertEquals(2D, copy.getScrollSpeed(), 0D);
		Assert.assertFalse(copy.isMounted());
		Assert.assertTrue(copy.getAnimatorMap().containsKey(animator));
		Assert.assertNotSame(bar, copy.getScrollbar());
		Assert.assertSame(Bar.class, copy.getScrollbar().getClass());
		Assert.assertSame(copy, copy.getScrollbar().getScrollNode());
		Assert.assertSame(bar.getScroll(), copy.getScrollbar().getScroll());
		Assert.assertSame(column, bar.getScrollNode());
	}

	@Test
	public void copiesANodeBuiltFromItsPositionOnly() {
		final PointNode node = new PointNode(5D, 6D).width(7D).height(8D);
		final PointNode copy = node.copy();
		Assert.assertEquals(5D, copy.getX(), 0D);
		Assert.assertEquals(6D, copy.getY(), 0D);
		Assert.assertEquals(7D, copy.getWidth(), 0D);
		Assert.assertEquals(8D, copy.getHeight(), 0D);
	}

	@Test
	public void copiesANodeBuiltWithoutArguments() {
		final EmptyNode node = new EmptyNode().x(1D).y(2D).width(3D).height(4D);
		final EmptyNode copy = node.copy();
		Assert.assertEquals(1D, copy.getX(), 0D);
		Assert.assertEquals(2D, copy.getY(), 0D);
		Assert.assertEquals(3D, copy.getWidth(), 0D);
		Assert.assertEquals(4D, copy.getHeight(), 0D);
	}

	@Test
	public void refusesToCopyANodeItCannotBuild() {
		try {
			new NamedNode("label").copy();
			Assert.fail("A node without known constructor cannot be copied");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Failed to copy node: NamedNode", expected.getMessage());
			Assert.assertTrue(expected.getCause() instanceof NoSuchMethodException);
		}
	}

	@Test
	public void keepsTheCallbacksOfTheOriginalInItsCopy() {
		final int[] clicks = {0};
		final RectNode copy = RectNode.create(100D, 100D, 50D, 50D).onClick((rect, mouseX, mouseY, clickType) -> clicks[0]++).copy();
		this.press(copy, 110D, 110D);
		Assert.assertEquals(1, clicks[0]);
	}

	@Test
	public void cannotBeReloadedAlone() {
		for (final Method method : Node.class.getMethods()) {
			Assert.assertNotEquals("reload", method.getName());
			Assert.assertNotEquals("onReload", method.getName());
		}
	}

	@Test
	public void onlyCallsItsWatchCallbacksForAWatchWithoutProperty() {
		final Signal<Integer> signal = new Signal<>(0);
		final List<WatchProperty> watched = new ArrayList<>();
		final int[] inits = {0};
		final int[] watches = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).append(RectNode.create(0D, 0D, 5D, 5D)).onInit(rect -> inits[0]++).watch(signal).onWatch((rect, source, properties) -> {
			watches[0]++;
			watched.addAll(Arrays.asList(properties));
		});
		this.bridges.open(new NodeUI(node));
		signal.set(1);
		signal.set(2);
		Assert.assertEquals(2, watches[0]);
		Assert.assertEquals(1, inits[0]);
		Assert.assertTrue(watched.isEmpty());
		Assert.assertEquals(1, node.getChildren().size());
	}

	@Test
	public void appliesItsWatchPropertiesInTheirOrder() {
		final Signal<Integer> signal = new Signal<>(0);
		final List<String> applied = new ArrayList<>();
		final RectNode node = RectNode
		.create(0D, 0D, 10D, 10D)
		.body(rect -> {
			applied.add("body " + rect.getChildren().size());
			RectNode.create(0D, 0D, 5D, 5D).attach(rect);
		})
		.watch(signal, WatchProperty.custom((rect, source) -> applied.add("first " + source.get())), WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY, WatchProperty.custom((rect, source) -> applied.add("last " + rect.getChildren().size())));
		this.bridges.open(new NodeUI(node));
		applied.clear();
		signal.set(3);
		Assert.assertEquals(Arrays.asList("first 3", "body 0", "last 1"), applied);
	}

	@Test
	public void letsAPreWatchCallbackRefuseTheProperties() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] bodies = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).body(rect -> bodies[0]++).watch(signal, WatchProperty.BODY).onWatch(new NodeWatchCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode rect, final @NonNull Signal<?> source, final @NonNull WatchProperty @NonNull... properties) {
				bodies[0] += 10;
			}

			@Override
			public void pre(final @NonNull RectNode rect, final @NonNull InternalContext context, final @NonNull Signal<?> source, final @NonNull WatchProperty @NonNull... properties) {
				context.cancel();
			}

		});
		this.bridges.open(new NodeUI(node));
		signal.set(1);
		Assert.assertEquals(1, bodies[0]);
	}

	@Test
	public void stopsWatchingOnceItsUiIsClosed() {
		final Signal<Integer> signal = new Signal<>(0);
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D).watch(signal));
		this.bridges.open(ui);
		signal.set(1);
		Assert.assertEquals(1, signal.getEventSet().size());
		JOID.close(ui);
		signal.set(2);
		Assert.assertTrue(signal.getEventSet().isEmpty());
	}

	@Test
	public void startsAWatchMadeOutsideAnyUiOnceAttached() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] watches = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> watches[0]++);
		Assert.assertTrue(signal.getEventSet().isEmpty());
		signal.set(1);
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D));
		this.bridges.open(ui);
		node.attach(ui);
		Assert.assertEquals(0, watches[0]);
		Assert.assertEquals(1, signal.getEventSet().size());
		signal.set(2);
		Assert.assertEquals(1, watches[0]);
	}

	@Test
	public void keepsAWatchMadeWhileItsUiIsBuilt() {
		final WatchingUI ui = new WatchingUI();
		this.bridges.open(ui);
		Assert.assertEquals(1, ui.watches);
		Assert.assertEquals(1, ui.signal.getEventSet().size());
		ui.signal.set(2);
		Assert.assertEquals(2, ui.watches);
	}

	@Test
	public void checksItsConditionBeforeApplyingAChange() {
		final Signal<Integer> signal = new Signal<>(0);
		final boolean[] kept = {true};
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).append(RectNode.create(0D, 0D, 10D, 10D)).watch(signal, () -> kept[0], WatchProperty.CLEAR_CHILDREN);
		this.bridges.open(new NodeUI(node));
		kept[0] = false;
		signal.set(1);
		Assert.assertEquals(1, node.getChildren().size());
		Assert.assertTrue(signal.getEventSet().isEmpty());
	}

	@Test
	public void forgetsTheWatchesOfTheNodesDroppedByAReload() {
		final Signal<Integer> signal = new Signal<>(0);
		final ReloadedUI ui = new ReloadedUI(signal);
		this.bridges.open(ui);
		ui.reload();
		ui.reload();
		Assert.assertEquals(1, signal.getEventSet().size());
		signal.set(1);
		Assert.assertEquals(1, ui.watches);
	}

	@Test
	public void stopsWatchingOnceRemoved() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> reloads[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.remove(child);
		Assert.assertTrue(signal.getEventSet().isEmpty());
		signal.set(1);
		Assert.assertEquals(0, reloads[0]);
	}

	@Test
	public void stopsWatchingInTheWholeClearedTree() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode grandchild = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> reloads[0]++);
		final ContainerNode child = ContainerNode.create(0D, 0D, 50D, 50D).watch(signal).onWatch((container, source, properties) -> reloads[0]++).append(grandchild);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.clearChildren();
		Assert.assertTrue(signal.getEventSet().isEmpty());
		signal.set(1);
		Assert.assertEquals(0, reloads[0]);
	}

	@Test
	public void ignoresAPublishThatReachesItAfterItsDetach() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> reloads[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		final List<SignalSubscriber<Integer>> pending = new ArrayList<>(signal.getEventSet());
		parent.remove(child);
		for (final SignalSubscriber<Integer> subscriber : pending) {
			Assert.assertTrue(subscriber.update(1));
		}
		Assert.assertEquals(0, reloads[0]);
	}

	@Test
	public void watchesAgainOnceAttachedAgain() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> reloads[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.remove(child);
		parent.append(child);
		parent.remove(child);
		parent.append(child);
		Assert.assertEquals(1, signal.getEventSet().size());
		signal.set(1);
		Assert.assertEquals(1, reloads[0]);
	}

	@Test
	public void catchesUpWithAChangeMissedWhileDetached() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] watches = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> watches[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.remove(child);
		signal.set(1);
		Assert.assertEquals(0, watches[0]);
		parent.append(child);
		Assert.assertEquals(1, watches[0]);
		parent.remove(child);
		parent.append(child);
		Assert.assertEquals(1, watches[0]);
	}

	@Test
	public void watchesAgainOnceItsUiIsOpenedAgain() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch((rect, source, properties) -> reloads[0]++));
		this.bridges.open(ui);
		JOID.close(ui);
		Assert.assertTrue(signal.getEventSet().isEmpty());
		this.bridges.open(ui);
		Assert.assertEquals(1, signal.getEventSet().size());
		signal.set(1);
		Assert.assertEquals(1, reloads[0]);
		Assert.assertEquals(1, signal.getEventSet().size());
	}

	@Test
	public void keepsSeveralBindingsActiveAtOnce() {
		final Signal<String> title = new Signal<>("first");
		final Signal<Integer> count = new Signal<>(1);
		final List<Object> values = new ArrayList<>();
		final BindingNode node = new BindingNode();
		final SignalSubscriber<String> titleSubscription = node.follow(title, values::add);
		final SignalSubscriber<Integer> countSubscription = node.follow(count, values::add);
		this.bridges.open(new NodeUI(node));
		title.set("second");
		count.set(2);
		Assert.assertEquals(Arrays.asList("first", 1, "second", 2), values);
		Assert.assertNotSame(titleSubscription, countSubscription);
		Assert.assertEquals(2, node.getSubscriptionList().size());
		node.forget(titleSubscription);
		title.set("third");
		count.set(3);
		Assert.assertEquals(Arrays.asList("first", 1, "second", 2, 3), values);
		Assert.assertTrue(title.getEventSet().isEmpty());
		Assert.assertEquals(1, count.getEventSet().size());
	}

	@Test
	public void ignoresAMissingBindingInUnbind() {
		final Signal<Integer> signal = new Signal<>(0);
		final List<Integer> values = new ArrayList<>();
		final BindingNode node = new BindingNode();
		node.follow(signal, values::add);
		this.bridges.open(new NodeUI(node));
		node.forget(null);
		node.forget(new BindingNode().follow(signal, value -> {}));
		signal.set(1);
		Assert.assertEquals(Arrays.asList(0, 1), values);
		Assert.assertEquals(1, node.getSubscriptionList().size());
	}

	@Test
	public void rebindsOnceAcrossADetach() {
		final Signal<Integer> first = new Signal<>(0);
		final Signal<Integer> second = new Signal<>(10);
		final List<Integer> values = new ArrayList<>();
		final BindingNode node = new BindingNode();
		final SignalSubscriber<Integer> previous = node.follow(first, values::add);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent));
		parent.remove(node);
		node.refollow(previous, second, values::add);
		Assert.assertTrue(first.getEventSet().isEmpty());
		Assert.assertTrue(second.getEventSet().isEmpty());
		parent.append(node);
		parent.remove(node);
		parent.append(node);
		Assert.assertTrue(first.getEventSet().isEmpty());
		Assert.assertEquals(1, second.getEventSet().size());
		Assert.assertEquals(1, node.getSubscriptionList().size());
		first.set(1);
		second.set(11);
		Assert.assertEquals(Arrays.asList(0, 10, 11), values);
	}

	@Test
	public void detachesItsScrollbarAndItsSkeleton() {
		final Signal<Integer> signal = new Signal<>(0);
		final Bar bar = new Bar(90D, 0D, 10D, 20D, BoundingBox.create(0D, 0D, 10D, 100D)).watch(signal);
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(bar).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D).watch(signal));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 200D, 200D).append(node);
		this.bridges.open(new NodeUI(parent));
		Assert.assertEquals(2, signal.getEventSet().size());
		parent.remove(node);
		Assert.assertTrue(signal.getEventSet().isEmpty());
		Assert.assertFalse(bar.isSubscribed());
		Assert.assertFalse(node.getSkeleton().isSubscribed());
		parent.append(node);
		parent.remove(node);
		parent.append(node);
		Assert.assertEquals(2, signal.getEventSet().size());
	}

	@Test
	public void detachesItsEffects() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new RecordingEffect("effect", events));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent));
		events.clear();
		parent.remove(node);
		Assert.assertEquals(Arrays.asList("effect detach"), events);
		parent.append(node);
		Assert.assertEquals(Arrays.asList("effect detach", "effect init"), events);
	}

	@Test
	public void endsItsDragWhenDetached() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragStart(rect -> events.add("start")).onDragEnd(rect -> events.add("end"));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 400D, 400D).append(node);
		this.bridges.open(new NodeUI(parent));
		node.startDragging(110D, 110D);
		node.onMouseDragged(160D, 110D, ClickType.LEFT, 0L, InternalContext.create());
		parent.remove(node);
		Assert.assertEquals(Arrays.asList("start", "end"), events);
		Assert.assertFalse(node.isDragging());
		Assert.assertFalse(node.isDragged());
		Assert.assertEquals(150D, node.getX(), 0D);
		parent.append(node);
		this.bridges.frame();
		Assert.assertEquals(150D, node.getX(), 0D);
		Assert.assertFalse(node.isDragging());
	}

	@Test
	public void dropsTheCopyOfItsDragWhenDetached() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 400D, 400D).append(node);
		this.bridges.open(new NodeUI(parent));
		node.startDragging(110D, 110D);
		Assert.assertNotNull(node.getDraggedNode());
		parent.remove(node);
		Assert.assertNull(node.getDraggedNode());
		Assert.assertEquals(100D, node.getX(), 0D);
	}

	@Test
	public void endsItsHoverWhenDetached() {
		final int[] ends = {0};
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onHoverEnd((rect, mouseX, mouseY) -> ends[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 400D, 400D).append(node);
		this.bridges.open(new NodeUI(parent));
		node.hovered(true).getHoverAnimator().sequence(0F, 1F).start().update(1F);
		parent.remove(node);
		Assert.assertFalse(node.isHovered());
		Assert.assertEquals(1, ends[0]);
		Assert.assertEquals(0F, node.hoverValue(1F), 0F);
		node.onDetach();
		Assert.assertEquals(1, ends[0]);
	}

	@Test
	public void mountsAgainOnceAttachedAgain() {
		final int[] mounts = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onMount(rect -> mounts[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent)).frame();
		Assert.assertEquals(1, mounts[0]);
		parent.remove(node);
		parent.append(node);
		this.bridges.frame();
		Assert.assertEquals(2, mounts[0]);
	}

	@Test
	public void waitsForItsConditionsAcrossADetach() {
		final Signal<String> title = new Signal<>();
		final int[] mounts = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).wait(title).onMount(rect -> mounts[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent)).frame();
		parent.remove(node);
		title.set("ready");
		this.bridges.frame();
		Assert.assertEquals(0, mounts[0]);
		parent.append(node);
		this.bridges.frame();
		Assert.assertEquals(1, mounts[0]);
	}

	@Test
	public void stopsUpdatingItsAnimatorsWhileDetached() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate((rect, source, value) -> values.add(value));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent));
		parent.remove(node);
		animator.sequence(0F, 1F).start();
		this.bridges.frame();
		Assert.assertTrue(values.isEmpty());
		Assert.assertEquals(0F, animator.getValue(), 0F);
		parent.append(node);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(1F), values);
	}

	@Test
	public void watchesWhileItsConditionHolds() {
		final Signal<Integer> signal = new Signal<>(0);
		final boolean[] kept = {true};
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).append(RectNode.create(0D, 0D, 10D, 10D)).watch(signal, () -> kept[0], WatchProperty.CLEAR_CHILDREN);
		this.bridges.open(new NodeUI(node));
		signal.set(1);
		Assert.assertTrue(node.getChildren().isEmpty());
		Assert.assertEquals(1, signal.getEventSet().size());
		kept[0] = false;
		signal.set(2);
		Assert.assertTrue(signal.getEventSet().isEmpty());
	}

	@Test
	public void appliesTheOtherPropertiesWhenOneFails() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] bodies = {0};
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).body(rect -> {
			if (bodies[0]++ > 0) {
				throw new IllegalStateException("Body failed");
			}
		}).append(RectNode.create(0D, 0D, 10D, 10D)).watch(signal, WatchProperty.BODY, WatchProperty.CLEAR_CHILDREN);
		this.bridges.open(new NodeUI(node));
		final String error = NodeTest.capture(() -> signal.set(1));
		Assert.assertTrue(error, error.contains("Body failed"));
		Assert.assertTrue(node.getChildren().isEmpty());
	}

	@Test
	public void reportsEveryValueOfItsAnimators() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate((rect, tween, value) -> values.add(value));
		this.bridges.open(new NodeUI(node)).frames(2);
		Assert.assertTrue(values.isEmpty());
		animator.start();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void stopsReportingARemovedAnimator() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate((rect, tween, value) -> values.add(value));
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertSame(node, node.removeAnimator(animator));
		Assert.assertTrue(node.getAnimatorMap().isEmpty());
		animator.start();
		this.bridges.frames(3);
		Assert.assertTrue(values.isEmpty());
	}

	@Test
	public void removesAnAnimatorFromItsOwnCallback() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).animate(TweenAnimator.create()).onAnimate((rect, tween, value) -> {
			values.add(value);
			rect.removeAnimator(tween);
		});
		this.bridges.open(new NodeUI(node)).frame();
		animator.start();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F), values);
		Assert.assertEquals(1, node.getAnimatorMap().size());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void exposesItsAnimatorsReadOnly() {
		RectNode.create(0D, 0D, 10D, 10D).getAnimatorMap().put(TweenAnimator.create(), 0F);
	}

	@Test
	public void updatesTheAnimatorsOfAHiddenNode() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).visible(false).animate(animator).onAnimate((rect, tween, value) -> values.add(value));
		this.bridges.open(new NodeUI(node)).frame();
		animator.start();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void updatesTheAnimatorsUnderAHiddenParent() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate((rect, tween, value) -> values.add(value));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).visible(false).append(ContainerNode.create(0D, 0D, 100D, 100D).append(node));
		this.bridges.open(new NodeUI(parent)).frame();
		animator.start();
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void describesItselfInJson() {
		final RectNode node = RectNode.create(10D, 20D, 30D, 40D).zindex(2);
		node.x(15D);
		final JsonObject json = node.toJson();
		Assert.assertEquals(2, json.get("index").getAsInt());
		Assert.assertEquals("N/A", json.get("mappedIndex").getAsString());
		Assert.assertEquals("RectNode", json.get("name").getAsString());
		Assert.assertEquals(RectNode.class.getName(), json.get("class").getAsString());
		Assert.assertEquals(10D, json.get("defaultX").getAsDouble(), 0D);
		Assert.assertEquals(20D, json.get("defaultY").getAsDouble(), 0D);
		Assert.assertEquals("15.0 / 15.0", json.getAsJsonObject("bounding").get("x").getAsString());
		Assert.assertEquals("20.0 / 20.0", json.getAsJsonObject("bounding").get("y").getAsString());
		Assert.assertEquals(30D, json.getAsJsonObject("bounding").get("width").getAsDouble(), 0D);
		Assert.assertEquals(40D, json.getAsJsonObject("bounding").get("height").getAsDouble(), 0D);
		Assert.assertFalse(json.has("hierarchy"));
		Assert.assertFalse(node.toString().contains("\n"));
		Assert.assertTrue(node.toString().contains("\"name\":\"RectNode\""));
	}

	@Test
	public void describesItsStateInDevMode() {
		final boolean previous = JOID.inst().isDevMode();
		JOID.inst().setDevMode(true);
		try {
			final RectNode child = RectNode.create(0D, 0D, 10D, 10D).overflow(OverflowProperty.SCROLL);
			final RectNode parent = RectNode.create(0D, 0D, 100D, 100D).append(child);
			final JsonObject json = child.toJson();
			Assert.assertEquals("0.0 / 0.0 [SCROLL]", json.get("scrollX").getAsString());
			Assert.assertEquals("0.0 / 0.0 [SCROLL]", json.get("scrollY").getAsString());
			Assert.assertTrue(json.get("visible").getAsBoolean());
			Assert.assertTrue(json.get("enabled").getAsBoolean());
			Assert.assertFalse(json.get("hovered").getAsBoolean());
			Assert.assertTrue(json.get("isChild").getAsBoolean());
			Assert.assertEquals(0, json.get("children").getAsInt());
			Assert.assertEquals("RectNode - RectNode (this)", json.get("hierarchy").getAsString());
			Assert.assertFalse(parent.toJson().get("isChild").getAsBoolean());
			Assert.assertEquals(1, parent.toJson().get("children").getAsInt());
			Assert.assertTrue(child.toString().contains("\n"));
		} finally {
			JOID.inst().setDevMode(previous);
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingParent() {
		RectNode.create(0D, 0D, 10D, 10D).attach((Node) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingEffect() {
		RectNode.create(0D, 0D, 10D, 10D).effect(null);
	}

	@Test
	public void keepsItsWidthAtItsHeightTimesItsRatio() {
		final RectNode node = RectNode.create(0D, 0D, 200D, 100D).aspectRatio(2D);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertEquals(200D, node.getWidth(), 0D);
		Assert.assertEquals(100D, node.getHeight(), 0D);
	}

	@Test
	public void keepsTheSizeItDerivesFromItsRatio() {
		final RectNode node = RectNode.create(0D, 0D, 0D, 100D).aspectRatio(1.5D);
		this.bridges.open(new NodeUI(node)).frames(2);
		Assert.assertEquals(150D, node.getWidth(), 0D);
		Assert.assertEquals(100D, node.getHeight(), 0D);
	}

	@Test
	public void drawsAChildRaisedAfterItsAttachmentOverItsSiblings() {
		final RectNode raised = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.2F, 0.4F, 0.6F, 1F));
		final RectNode sibling = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 100D, 100D).append(raised, sibling)));
		raised.zindex(1);
		this.bridges.frame();
		Assert.assertTrue(this.order(0.6F, 0.4F, 0.2F) < this.order(0.2F, 0.4F, 0.6F));
	}

	@Test
	public void showsOnlyTheTooltipOfTheHoveredChild() {
		final HoverUI ui = new HoverUI(RectNode.create(100D, 100D, 200D, 200D).hover(() -> "Parent").append(RectNode.create(50D, 50D, 50D, 50D).hover(() -> "Child")));
		this.bridges.open(ui);
		this.bridges.move(170D, 170D).frame();
		Assert.assertEquals(Arrays.asList(Arrays.asList("Child")), ui.tooltips);
	}

	@Test
	public void removesAHoverElementToShowTheTooltipOfItsParent() {
		final HoverElement silent = (rect, mouseX, mouseY) -> {};
		final RectNode child = RectNode.create(50D, 50D, 50D, 50D).hover(silent);
		final HoverUI ui = new HoverUI(RectNode.create(100D, 100D, 200D, 200D).hover(() -> "Parent").append(child));
		this.bridges.open(ui);
		this.bridges.move(170D, 170D).frame();
		Assert.assertTrue(ui.tooltips.isEmpty());
		Assert.assertSame(child, child.removeHover(silent));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(Arrays.asList("Parent")), ui.tooltips);
		Assert.assertTrue(child.getHoverElementList().isEmpty());
	}

	@Test
	public void convertsACanvasCoordinateIntoItsDrawingSpace() {
		final RectNode child = RectNode.create(30D, 40D, 50D, 50D);
		final RectNode parent = RectNode.create(100D, 200D, 300D, 300D).append(child);
		this.bridges.open(new NodeUI(parent));
		Assert.assertEquals(45D, child.toDrawX(145D), 1E-9D);
		Assert.assertEquals(45D, child.toDrawY(245D), 1E-9D);
		Assert.assertEquals(145D, parent.toDrawX(145D), 1E-9D);
		Assert.assertEquals(245D, parent.toDrawY(245D), 1E-9D);
	}

	@Test
	public void showsTheTooltipOfTheParentOverAChildWithoutOne() {
		final HoverUI ui = new HoverUI(RectNode.create(100D, 100D, 200D, 200D).hover(() -> "Parent").append(RectNode.create(50D, 50D, 50D, 50D)));
		this.bridges.open(ui);
		this.bridges.move(170D, 170D).frame();
		Assert.assertEquals(Arrays.asList(Arrays.asList("Parent")), ui.tooltips);
	}

	@Test
	public void keepsTheCallbacksAddedToACopyAwayFromTheOriginal() {
		final List<String> clicks = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).onClick((rect, mouseX, mouseY, clickType) -> clicks.add("original"));
		final RectNode copy = node.copy();
		copy.onClick((rect, mouseX, mouseY, clickType) -> clicks.add("copy"));
		this.bridges.open(new NodeUI(node));
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("original"), clicks);
	}

	@Test
	public void copiesTheEffectsOfTheNode() {
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D).effect(RoundedNodeEffect.create(6F));
		Assert.assertEquals(1, node.copy().getEffectMap().size());
	}

	@Test
	public void placesTheDefaultPositionOfAnAbsoluteNodeOnTheUi() {
		final RectNode child = RectNode.create(30D, 40D, 20D, 20D).position(PositionProperty.ABSOLUTE);
		ContainerNode.create(200D, 300D, 400D, 400D).append(child);
		Assert.assertEquals(child.getAbsoluteX(), child.getAbsoluteDefaultX(), 0D);
		Assert.assertEquals(child.getAbsoluteY(), child.getAbsoluteDefaultY(), 0D);
	}

	@Test
	public void findsAChildByClassLikeItsChildren() {
		final ShadedRect child = new ShadedRect();
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		Assert.assertEquals(Arrays.asList(child), parent.getChildren(RectNode.class).ordered());
		Assert.assertSame(child, parent.getChild(0, RectNode.class));
	}

	@Test
	public void stopsWatchingOnceItsOwnUiIsClosed() {
		final Signal<Integer> signal = new Signal<>(0);
		final NodeUI first = new NodeUI(RectNode.create(0D, 0D, 10D, 10D).watch(signal));
		this.bridges.open(first);
		JOID.close(first);
		this.bridges.open(new NodeUI(RectNode.create(0D, 0D, 10D, 10D)));
		signal.set(1);
		Assert.assertTrue(signal.getEventSet().isEmpty());
	}

	@Test
	public void listsTheMissingCallbacksAsAnEmptyList() {
		Assert.assertNotNull(RectNode.create(0D, 0D, 10D, 10D).getCallbackList(NodeCallbackRegistry.getId(NodeInitCallback.class)));
	}

	@Test
	public void drawsANodeRaisedAfterItsAttachmentOverTheOtherNodesOfItsUi() {
		final RectNode raised = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.2F, 0.4F, 0.6F, 1F));
		final RectNode other = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
		final NodeUI ui = new NodeUI(raised);
		this.bridges.open(ui);
		other.attach(ui);
		raised.zindex(1);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(other, raised), ui.getNodeList().ordered());
		Assert.assertTrue(this.order(0.6F, 0.4F, 0.2F) < this.order(0.2F, 0.4F, 0.6F));
	}

	private Draw draw(final float red, final float green, final float blue) {
		return this.bridges.getRender().getDraws(red, green, blue).get(0);
	}

	private int order(final float red, final float green, final float blue) {
		return this.bridges.getRender().getDraws().indexOf(this.draw(red, green, blue));
	}

	private void press(final Node node, final double x, final double y) {
		this.bridges.open(new NodeUI(node)).frame();
		this.bridges.move(x, y).frames(2);
		this.bridges.getUi().mousePressed(ClickType.LEFT);
	}

	private void drop(final double x, final double y) {
		this.bridges.move(x, y).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		this.bridges.frames(100);
	}

	private static ContainerNode row() {
		final ContainerNode row = ContainerNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL);
		RectNode.create(0D, 0D, 400D, 100D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).attach(row);
		RectNode.create(400D, 0D, 300D, 100D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).attach(row);
		return row;
	}

	private static ContainerNode column() {
		final ContainerNode column = ContainerNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL);
		RectNode.create(0D, 0D, 400D, 100D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).attach(column);
		RectNode.create(0D, 100D, 400D, 100D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).attach(column);
		RectNode.create(0D, 200D, 400D, 100D).color(new Color(0.5F, 0.7F, 0.9F, 1F)).attach(column);
		return column;
	}

	private static String capture(final Runnable runnable) {
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	public static final class ScrollUI extends UI {

		@Override
		public void init() {
			final ContainerNode container = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
			RectNode.create(0D, 100.3D, 200D, 20D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(container);
			RectNode.create(0D, 140.8D, 200D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).attach(container);
			RectNode.create(0D, 1000D, 10D, 10D).color(new Color(0F, 0F, 1F, 1F)).attach(container);
			container.attach(this);
		}

	}

	public static final class MovingUI extends UI {

		private RectNode panel;

		@Override
		public void init() {
			this.panel = RectNode.create(100D, 100.3D, 300D, 200D).color(new Color(0.2F, 0.4F, 0.6F, 1F));
			RectNode.create(10D, 20.45D, 100D, 30D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).attach(this.panel);
			this.panel.attach(this);
		}

	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	@RequiredArgsConstructor
	public static final class HoverUI extends UI {

		private final Node               node;
		private final List<List<String>> tooltips = new ArrayList<>();

		@Override
		public void init() {
			super.add(this.node);
		}

		@Override
		public void drawHover(final @NonNull Object content, final double mouseX, final double mouseY) {
			this.tooltips.add(TextConverter.convertLines(content));
		}

	}

	public static final class CurrentUI extends UI {

		private UI      found;
		private boolean bound;

		@Override
		public void init() {
			final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
			this.found = node.getUi();
			this.bound = node.hasUi();
		}

	}

	public static final class WatchingUI extends UI {

		private final Signal<Integer> signal = new Signal<>(0);

		private int watches;

		@Override
		public void init() {
			RectNode.create(0D, 0D, 10D, 10D).watch(this.signal).onWatch((rect, source, properties) -> this.watches++).attach(this);
			this.signal.set(1);
		}

	}

	@RequiredArgsConstructor
	public static final class ReloadedUI extends UI {

		private final Signal<Integer> signal;

		private int watches;

		@Override
		public void init() {
			RectNode.create(0D, 0D, 10D, 10D).watch(this.signal).onWatch((rect, source, properties) -> this.watches++).attach(this);
		}

	}

	public static final class PointNode extends Node {

		public PointNode(final double x, final double y) {
			super(x, y);
		}

	}

	public static final class BindingNode extends Node {

		public BindingNode() {
			super(0D, 0D, 10D, 10D);
		}

		public <V> SignalSubscriber<V> follow(final Signal<V> signal, final Consumer<V> consumer) {
			return super.bind(signal, consumer);
		}

		public void forget(final SignalSubscriber<?> subscriber) {
			super.unbind(subscriber);
		}

		public <V> SignalSubscriber<V> refollow(final SignalSubscriber<?> previous, final Signal<V> signal, final Consumer<V> consumer) {
			return super.rebind(previous, signal, consumer);
		}

	}

	public static final class EmptyNode extends Node {

		public EmptyNode() {
			super(0D, 0D, 0D, 0D);
		}

	}

	public static final class NamedNode extends Node {

		public NamedNode(final String name) {
			super(0D, 0D, name.length(), 0D);
		}

	}

	public static final class RecordingNode extends Node {

		private final String       name;
		private final List<String> events;

		public RecordingNode(final String name, final List<String> events, final double x, final double y, final double width, final double height) {
			super(x, y, width, height);
			this.name = name;
			this.events = events;
		}

		@Override
		public void init(final @NonNull UI ui) {
			this.events.add(this.name + " init");
		}

		@Override
		public void update() {
			this.events.add(this.name + " update");
		}

		@Override
		public void detach() {
			this.events.add(this.name + " detach");
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.events.add(this.name + " pressed");
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
			this.events.add(this.name + " dragged");
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.events.add(this.name + " released");
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final double valueX, final double value, final @NonNull InternalContext context) {
			this.events.add(this.name + " scrolled");
		}

		@Override
		public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
			this.events.add(this.name + " typed");
		}

	}

	@AllArgsConstructor
	public static class RecordingEffect extends NodeEffect<Node> {

		private final String       name;
		private final List<String> events;

		@Override
		public void init(final @NonNull Node node, final @NonNull UI ui) {
			this.events.add(this.name + " init");
		}

		@Override
		public void detach(final @NonNull Node node) {
			this.events.add(this.name + " detach");
		}

		@Override
		public void pre(final @NonNull Node node, final double mouseX, final double mouseY) {
			this.events.add(this.name + " pre");
		}

		@Override
		public void post(final @NonNull Node node, final double mouseX, final double mouseY) {
			this.events.add(this.name + " post");
		}

	}

	public static final class OtherEffect extends RecordingEffect {

		public OtherEffect(final String name, final List<String> events) {
			super(name, events);
		}

	}

	public static final class SkippedEffect extends RecordingEffect {

		public SkippedEffect(final List<String> events) {
			super("skipped", events);
		}

		@Override
		public boolean shouldApply(final @NonNull Node node) {
			return false;
		}

	}

	@AllArgsConstructor
	public static final class ColorEffect extends NodeEffect<RectNode> {

		private final List<String> events;

		@Override
		public void pre(final @NonNull RectNode node, final double mouseX, final double mouseY) {
			this.events.add(node.getColor().toString());
		}

	}

	@Getter
	@SuppressWarnings("unchecked")
	public static class GlowBorderEffect extends BorderNodeEffect {

		private float glow;

		private GlowBorderEffect() {
			super(Color.BLACK, 2F, BorderMode.OUT);
		}

		public static GlowBorderEffect create() {
			return new GlowBorderEffect();
		}

		public final <E extends GlowBorderEffect> @NonNull E glow(final float glow) {
			this.glow = glow;
			return (E) this;
		}

	}

	public static final class Bar extends ScrollbarNode {

		public Bar(final double x, final double y, final double width, final double height, final BoundingBox scroll) {
			super(x, y, width, height, scroll);
		}

		@Override
		public void drawScrollbar(final double mouseX, final double mouseY) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(0.6F, 0.4F, 0.2F, 1F));
		}

	}

	@AllArgsConstructor
	public static final class GrabbedWindow implements IWindowBridge {

		private final IWindowBridge window;

		@Override
		public int getWidth() {
			return this.window.getWidth();
		}

		@Override
		public int getHeight() {
			return this.window.getHeight();
		}

		@Override
		public double getMouseX() {
			return this.window.getMouseX();
		}

		@Override
		public double getMouseY() {
			return this.window.getMouseY();
		}

		@Override
		public boolean isMouseGrabbed() {
			return true;
		}

		@Override
		public boolean isKeyDown(final @NonNull Key key) {
			return this.window.isKeyDown(key);
		}

		@Override
		public @NonNull String getClipboard() {
			return this.window.getClipboard();
		}

		@Override
		public void setClipboard(final @NonNull String text) {
			this.window.setClipboard(text);
		}

	}

	@UIStoreData(id = "node")
	public static class CountStore extends UIStore {}

	public static final class ShadedRect extends RectNode {

		public ShadedRect() {
			super(0D, 0D, 10D, 10D);
		}

	}

}