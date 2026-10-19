package dev.joid.lib.ui.node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.joid.internal.JOID;
import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.ui.UIBridgeTest.PopupUI;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.converter.TextConverter;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalListener;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.hover.HoverSupplier;
import dev.joid.lib.ui.node.hover.IHoverElement;
import dev.joid.lib.ui.node.hover.impl.NodeHoverElement;
import dev.joid.lib.ui.node.impl.control.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.ui.node.property.PositionProperty;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.align.Axis;
import dev.joid.lib.utils.geom.Point;
import dev.joid.lib.utils.list.IndexedList;

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
		Assert.assertFalse(node.isAttached());
	}

	@Test
	public void movesWithoutForgettingItsDefaultSize() {
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
		Assert.assertEquals(7D, node.getDefaultX(), 0D);
		Assert.assertEquals(8D, node.getDefaultY(), 0D);
		Assert.assertEquals(11D, node.getDefaultWidth(), 0D);
		Assert.assertEquals(12D, node.getDefaultHeight(), 0D);
	}

	@Test
	public void fitsOnlyTheSidesItWasNotGiven() {
		final RectNode node = RectNode.create(0D, 0D, 30D, 0D);
		Assert.assertSame(node, node.fit(50D, 60D));
		Assert.assertEquals(30D, node.getWidth(), 0D);
		Assert.assertEquals(60D, node.getHeight(), 0D);
		Assert.assertEquals(0D, node.getDefaultHeight(), 0D);
		Assert.assertSame(node, node.layoutSize(70D, 80D));
		Assert.assertEquals(70D, node.getWidth(), 0D);
		Assert.assertEquals(30D, node.getDefaultWidth(), 0D);
	}

	@Test
	public void placesItselfFromItsLayoutPosition() {
		final RectNode node = RectNode.create(10D, 20D, 30D, 40D);
		Assert.assertSame(node, node.layoutPosition(5D, 6D));
		Assert.assertEquals(15D, node.getX(), 0D);
		Assert.assertEquals(26D, node.getY(), 0D);
		Assert.assertEquals(10D, node.getDefaultX(), 0D);
	}

	@Test
	public void takesALabel() {
		final Text label = Text.create();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		Assert.assertSame(node, node.label(label));
		Assert.assertSame(label, node.getLabel());
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
		Assert.assertEquals(115D, leaf.getAbsoluteDefaultX(), 0D);
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
	public void placesItsCenterOnItsPositionWithACenterAnchor() {
		final RectNode node = RectNode.create(100D, 100D, 200D, 100D).anchor(Align.CENTER);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertSame(Align.CENTER, node.getAnchorX());
		Assert.assertSame(Align.CENTER, node.getAnchorY());
		Assert.assertEquals(0D, node.getX(), 0D);
		Assert.assertEquals(50D, node.getY(), 0D);
		node.width(100D).height(50D);
		this.bridges.frame();
		Assert.assertEquals(50D, node.getX(), 0D);
		Assert.assertEquals(75D, node.getY(), 0D);
	}

	@Test
	public void placesItsFarEdgesOnItsPositionWithEndAnchors() {
		final RectNode node = RectNode.create(100D, 100D, 200D, 100D).anchorX(Align.END).anchorY(Align.END);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertEquals(-100D, node.getX(), 0D);
		Assert.assertEquals(0D, node.getY(), 0D);
		node.width(150D).height(40D);
		this.bridges.frame();
		Assert.assertEquals(-50D, node.getX(), 0D);
		Assert.assertEquals(60D, node.getY(), 0D);
		node.width(200D).height(100D);
		this.bridges.frame();
		Assert.assertEquals(-100D, node.getX(), 0D);
		Assert.assertEquals(0D, node.getY(), 0D);
	}

	@Test
	public void movesItsPivotWithItsAnchor() {
		final RectNode node = RectNode.create(100D, 100D, 200D, 100D).anchorX(Align.START).anchorY(Align.CENTER);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(50D, node.getY(), 0D);
		node.height(50D);
		this.bridges.frame();
		Assert.assertEquals(75D, node.getY(), 0D);
		node.anchorY(Align.START).height(100D);
		this.bridges.frame();
		Assert.assertEquals(100D, node.getY(), 0D);
		node.x(300D);
		Assert.assertEquals(300D, node.getX(), 0D);
		Assert.assertEquals(300D, node.getDefaultX(), 0D);
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
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).append(child).visible(() -> shown[0]);
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
		this.bridges.open(new NodeUI(node));
		first.set("ready");
		this.bridges.frame();
		Assert.assertTrue(node.isVisible());
		second.set(4);
		this.bridges.frame();
		Assert.assertTrue(node.isVisible());
	}

	@Test
	public void hidesItselfWhileABooleanSignalIsFalse() {
		final BooleanSignal shown = new BooleanSignal(false);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).visible(shown);
		Assert.assertFalse(node.isVisible());
		this.bridges.open(new NodeUI(node));
		shown.set(true);
		this.bridges.frame();
		Assert.assertTrue(node.isVisible());
		shown.set(false);
		this.bridges.frame();
		Assert.assertFalse(node.isVisible());
	}

	@Test
	public void ignoresTheMouseWhileDisabled() {
		final int[] clicks = {0};
		final boolean[] enabled = {false};
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).enabled(() -> enabled[0]).onClick(event -> clicks[0]++);
		this.bridges.open(new NodeUI(node));
		this.bridges.move(150D, 150D).frames(2);
		Assert.assertFalse(node.isEnabled());
		Assert.assertTrue(node.isHovered());
		Assert.assertEquals(0F, node.hoverValue(1F), 0F);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(0, clicks[0]);
		enabled[0] = true;
		this.bridges.frame();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(1, clicks[0]);
	}

	@Test
	public void letsTheMouseThroughWhileNotInteractive() {
		final List<String> clicks = new ArrayList<>();
		final boolean[] interactive = {false};
		final RectNode below = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("below"));
		final RectNode child = RectNode.create(0D, 0D, 50D, 50D);
		final RectNode above = RectNode.create(100D, 100D, 100D, 100D).zindex(1).interactive(() -> interactive[0]).onClick(event -> clicks.add("above")).append(child);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(below, above)));
		this.bridges.move(150D, 150D).frames(2);
		Assert.assertFalse(above.isInteractive());
		Assert.assertFalse(child.isInteractive());
		Assert.assertFalse(above.isHovered());
		Assert.assertTrue(below.isHovered());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("below"), clicks);
		interactive[0] = true;
		this.bridges.frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("below", "above"), clicks);
	}

	@Test
	public void keepsTheClickFromTheNodesBehindItsTarget() {
		final List<String> clicks = new ArrayList<>();
		final RectNode below = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("below"));
		final RectNode above = RectNode.create(150D, 150D, 100D, 100D);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(below, above)));
		this.bridges.move(175D, 175D).frames(2);
		Assert.assertTrue(above.isHovered());
		Assert.assertFalse(below.isHovered());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(clicks.isEmpty());
		this.bridges.move(125D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("below"), clicks);
	}

	@Test
	public void bubblesTheClickUpToTheParentsOfItsTarget() {
		final List<String> clicks = new ArrayList<>();
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D);
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("parent")).append(child);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).onClick(event -> clicks.add("root")).append(parent)));
		this.bridges.move(140D, 140D).frames(2);
		Assert.assertTrue(child.isHovered());
		Assert.assertTrue(parent.isHovered());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("parent"), clicks);
	}

	@Test
	public void clicksTheTargetAtTheMouseOfTheLastDrawnFrame() {
		final List<String> clicks = new ArrayList<>();
		final RectNode left = RectNode.create(0D, 0D, 100D, 100D).onClick(event -> clicks.add("left " + (int) event.getMouseX()));
		final RectNode right = RectNode.create(200D, 0D, 100D, 100D).onClick(event -> clicks.add("right " + (int) event.getMouseX()));
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(left, right)));
		this.bridges.move(50D, 50D).frames(2);
		this.bridges.move(250D, 50D);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("left 50"), clicks);
		this.bridges.frame();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("left 50", "right 250"), clicks);
	}

	@Test
	public void bubblesTheClickToTheParentsOfATargetDetachedOnTheWay() {
		final List<String> clicks = new ArrayList<>();
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D).handleMousePressed(event -> event.getNode().getParent().remove(event.getNode()));
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("parent")).append(child);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(parent)));
		this.bridges.move(140D, 140D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("parent"), clicks);
		Assert.assertFalse(parent.getChildren().contains(child));
		this.bridges.frame();
		Assert.assertTrue(parent.isHovered());
	}

	@Test
	public void bubblesTheClickAlongTheHoverOfTheLastFrame() {
		final List<String> clicks = new ArrayList<>();
		final RectNode other = RectNode.create(300D, 100D, 100D, 100D).onClick(event -> clicks.add("other"));
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D).handleMousePressed(event -> {
			if (event.getNode().getParent() != other) {
				other.append(event.getNode());
			}
		});
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("parent")).append(child);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 500D, 400D).append(other, parent)));
		this.bridges.move(140D, 140D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("parent"), clicks);
		Assert.assertSame(other, child.getParent());
	}

	@Test
	public void readsTheHoverOfTheLastFrameInItsHooks() {
		final List<String> hovers = new ArrayList<>();
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D);
		final RectNode parent = new RectNode(100D, 100D, 100D, 100D) {

			@Override
			public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
				hovers.add(child.isHovered() + " " + super.isHovered());
			}

		}.append(child);
		this.bridges.open(new NodeUI(parent));
		this.bridges.move(140D, 140D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(190D, 190D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(300D, 300D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("true true", "false true", "false false"), hovers);
	}

	@Test
	public void ignoresANodeAddedSinceTheLastFrame() {
		final List<String> clicks = new ArrayList<>();
		final RectNode below = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("below"));
		final RectNode added = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("added"));
		final ContainerNode root = ContainerNode.create(0D, 0D, 400D, 400D).append(below);
		this.bridges.open(new NodeUI(root)).move(150D, 150D).frames(2);
		root.append(added);
		Assert.assertFalse(added.isHovered());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("below"), clicks);
		this.bridges.frame();
		Assert.assertTrue(added.isHovered());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("below", "added"), clicks);
	}

	@Test
	public void bubblesTheClickToTheParentsOfATargetCoveredByAPopupOnTheWay() {
		final List<String> clicks = new ArrayList<>();
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D).handleMousePressed(event -> this.bridges.getUi().add(new PopupUI("popup", new ArrayList<>())));
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> clicks.add("parent")).append(child);
		final NodeUI ui = new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(parent));
		this.bridges.open(ui).move(140D, 140D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("parent"), clicks);
		this.bridges.frame();
		Assert.assertNull(ui.getHoveredNode());
		Assert.assertFalse(parent.isHovered());
		Assert.assertFalse(child.isHovered());
	}

	@Test
	public void hoversTheNodeLeftByAnUpdateOnTheNextFrame() {
		final int[] step = {0};
		final RectNode below = RectNode.create(100D, 100D, 100D, 100D);
		final RectNode hidden = RectNode.create(100D, 100D, 100D, 100D);
		final RectNode passive = RectNode.create(100D, 100D, 100D, 100D);
		final RectNode lowered = RectNode.create(100D, 100D, 100D, 100D);
		final ContainerNode root = ContainerNode.create(0D, 0D, 400D, 400D).append(below, hidden, passive, lowered).onUpdate(event -> {
			if (step[0] == 1) {
				lowered.zindex(-1);
			} else if (step[0] == 2) {
				passive.interactive(false);
			} else if (step[0] == 3) {
				hidden.visible(false);
			}
		});
		final NodeUI ui = new NodeUI(root);
		this.bridges.open(ui).move(150D, 150D).frames(2);
		Assert.assertSame(lowered, ui.getHoveredNode());
		step[0] = 1;
		this.bridges.frame();
		Assert.assertSame(passive, ui.getHoveredNode());
		Assert.assertFalse(lowered.isHovered());
		step[0] = 2;
		this.bridges.frame();
		Assert.assertSame(hidden, ui.getHoveredNode());
		step[0] = 3;
		this.bridges.frame();
		Assert.assertSame(below, ui.getHoveredNode());
		Assert.assertTrue(below.isHovered());
	}

	@Test
	public void hoversTheParentOfAChildThatSticksOutOfIt() {
		final RectNode child = RectNode.create(80D, 0D, 100D, 50D);
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		this.bridges.move(250D, 120D).frames(2);
		Assert.assertTrue(child.isHovered());
		Assert.assertTrue(parent.isHovered());
		this.bridges.move(350D, 120D).frame();
		Assert.assertFalse(parent.isHovered());
	}

	@Test
	public void hoversADisabledTargetAndItsParents() {
		final List<String> events = new ArrayList<>();
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D).enabled(false).onHoverStart(event -> events.add("child")).onClick(event -> events.add("child click"));
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).onHoverStart(event -> events.add("parent")).onClick(event -> events.add("click")).append(child);
		final NodeUI ui = new NodeUI(parent);
		this.bridges.open(ui).move(140D, 140D).frames(2);
		Assert.assertSame(child, ui.getHoveredNode());
		Assert.assertTrue(child.isHovered());
		Assert.assertTrue(parent.isHovered());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("parent", "child", "click"), events);
	}

	@Test
	public void followsABooleanSignalForItsInteractivity() {
		final BooleanSignal interactive = new BooleanSignal(false);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).interactive(interactive);
		Assert.assertFalse(node.isInteractive());
		this.bridges.open(new NodeUI(node));
		interactive.set(true);
		this.bridges.frame();
		Assert.assertTrue(node.isInteractive());
		Assert.assertFalse(RectNode.create(0D, 0D, 10D, 10D).interactive(false).isInteractive());
		Assert.assertTrue(RectNode.create(0D, 0D, 10D, 10D).isInteractive());
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
		final RectNode node = NodeTest.assign(RectNode.create(500D, 500D, 10D, 10D), "overflowArea", ContainerNode.create(0D, 0D, 10D, 10D));
		Assert.assertNotNull(node.getOverflowArea());
		Assert.assertTrue(node.isVisible());
	}

	@Test
	public void ignoresTheMouseOverAChildOutsideItsOverflowArea() {
		final RectNode child = RectNode.create(50D, 50D, 100D, 100D);
		final NodeUI ui = new NodeUI(ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child));
		this.bridges.open(ui).frame();
		Assert.assertSame(child, ui.getNodeAt(160D, 160D));
		Assert.assertNull(ui.getNodeAt(220D, 220D));
	}

	@Test
	public void givesTheSharedEdgeOfTwoNodesToTheNodeThatStartsThere() {
		final RectNode left = RectNode.create(100D, 100D, 100D, 100D);
		final RectNode right = RectNode.create(200D, 100D, 100D, 100D);
		final NodeUI ui = new NodeUI(ContainerNode.create(0D, 0D, 500D, 500D).append(left, right));
		this.bridges.open(ui).frame();
		Assert.assertSame(left, ui.getNodeAt(100D, 100D));
		Assert.assertSame(right, ui.getNodeAt(200D, 150D));
		Assert.assertSame(right, ui.getNodeAt(250D, 100D));
		Assert.assertNotSame(right, ui.getNodeAt(300D, 150D));
		Assert.assertNotSame(left, ui.getNodeAt(150D, 200D));
	}

	@Test
	public void hoversTheChildrenOutsideAnAreaThatStopsClipping() {
		final RectNode child = RectNode.create(50D, 50D, 100D, 100D);
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN).append(child);
		final NodeUI ui = new NodeUI(area);
		this.bridges.open(ui).frame();
		area.overflow(OverflowProperty.NONE);
		this.bridges.frame();
		Assert.assertNull(child.getOverflowArea());
		Assert.assertSame(child, ui.getNodeAt(220D, 220D));
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
		Assert.assertFalse(RectNode.create(0D, 0D, 100D, 100D).isHovered());
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
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onHoverStart(event -> events.add("start")).onHover(event -> events.add("hover")).onHoverEnd(event -> events.add("end"));
		this.bridges.open(new NodeUI(node));
		this.bridges.move(150D, 150D).frames(2);
		this.bridges.move(500D, 500D).frames(2);
		Assert.assertEquals(Arrays.asList("start", "hover", "hover", "end"), events);
	}

	@Test
	public void endsAForcedHoverOnceTheMouseIsAway() {
		final int[] ends = {0};
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onHoverEnd(event -> ends[0]++);
		this.bridges.open(new NodeUI(node));
		Assert.assertTrue(NodeTest.hover(node).isHovered());
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
		Assert.assertEquals(Arrays.asList("150.0 150.0"), ui.positions);
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
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hover(() -> "Old").clearHover().hover(() -> "New");
		final HoverUI ui = new HoverUI(node);
		this.bridges.open(ui);
		this.bridges.move(150D, 150D).frame();
		Assert.assertEquals(Arrays.asList("New"), ui.tooltips.get(0));
		node.clearHover().hover(() -> Arrays.asList("First", "Second"));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("First", "Second"), ui.tooltips.get(1));
		node.clearHover();
		this.bridges.frame();
		Assert.assertEquals(2, ui.tooltips.size());
	}

	@Test
	public void rendersItsHoverElements() {
		final List<String> rendered = new ArrayList<>();
		final IHoverElement first = (rect, mouseX, mouseY) -> rendered.add("first");
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hover(first).hover(() -> "Line");
		final HoverUI ui = new HoverUI(node);
		this.bridges.open(ui);
		this.bridges.move(150D, 150D).frame();
		Assert.assertEquals(Arrays.asList("first"), rendered);
		Assert.assertEquals(1, ui.tooltips.size());
		node.removeHover(first).hover((rect, mouseX, mouseY) -> rendered.add("second"));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("first", "second"), rendered);
		Assert.assertEquals(2, ui.tooltips.size());
		node.hover((rect, mouseX, mouseY) -> rendered.add("third")).clearHover();
		this.bridges.frame();
		Assert.assertEquals(2, rendered.size());
		Assert.assertEquals(2, ui.tooltips.size());
		Assert.assertTrue(NodeTest.<List<?>>field(node, "hoverElementList").isEmpty());
		Assert.assertTrue(NodeTest.<List<?>>field(node, "hoverSupplierList").isEmpty());
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
		Assert.assertEquals(Arrays.asList("early init", "late init", "early open", "late open", "late close", "early close"), events);
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
		Assert.assertEquals(Arrays.asList("second open", "first open", "first close", "second close"), events);
	}

	@Test
	public void replacesAnEffectOfTheSameClass() {
		final List<String> events = new ArrayList<>();
		final RecordingEffect second = new RecordingEffect("second", events);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new RecordingEffect("first", events)).effect(second);
		Assert.assertEquals(1, NodeTest.<Map<?, ?>>field(node, "effectMap").size());
		Assert.assertSame(second, node.getEffect(RecordingEffect.class));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(Arrays.asList("second init", "second open", "second close"), events);
	}

	@Test
	public void forgetsItsRemovedEffects() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new RecordingEffect("first", events)).effect(new OtherEffect("second", events));
		Assert.assertTrue(node.hasEffect(OtherEffect.class));
		Assert.assertSame(node, node.removeEffect(OtherEffect.class));
		Assert.assertFalse(node.hasEffect(OtherEffect.class));
		Assert.assertNull(node.getEffect(OtherEffect.class));
		Assert.assertTrue(node.hasEffect(RecordingEffect.class));
		Assert.assertSame(node, node.clearEffects());
		Assert.assertFalse(node.hasEffect(RecordingEffect.class));
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
		Assert.assertEquals(4F, blur.getRadius(), 0F);
		Assert.assertSame(NodeEffectScope.CHILDREN, rounded.getScope());
	}

	@Test
	public void chainsAnExtendedBuiltInEffectWithATypeWitness() {
		final GlowBorderEffect glow = GlowBorderEffect.create().<GlowBorderEffect>fill(false).glow(4F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(glow);
		Assert.assertSame(glow, node.getEffect(GlowBorderEffect.class));
		Assert.assertSame(glow, node.getEffect(BorderNodeEffect.class));
		Assert.assertFalse(glow.isFill());
		Assert.assertEquals(4F, glow.getGlow(), 0F);
		Assert.assertFalse(node.removeEffect(BorderNodeEffect.class).hasEffect(GlowBorderEffect.class));
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
		Assert.assertFalse(effect.shouldApply(node));
		Assert.assertEquals(Arrays.asList("skipped init"), events);
	}

	@Test
	public void drawsItsChildrenIntoAChildrenScopedShader() {
		final List<Boolean> buffered = new ArrayList<>();
		final RectNode child = RectNode.create(10D, 10D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)).onDraw(event -> buffered.add(this.bridges.getRender().getState().getFrameBuffer() != null));
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
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).onDraw(event -> drawn.add("child"));
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).append(child).layer((rect, mouseX, mouseY) -> drawn.add("second")).layer(0, (rect, mouseX, mouseY) -> drawn.add("first"));
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(Arrays.asList("child", "first", "second"), drawn);
		Assert.assertSame(node, node.clearLayers());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("child", "first", "second", "child"), drawn);
		Assert.assertTrue(NodeTest.<List<?>>field(node, "layerList").isEmpty());
	}

	@Test
	public void drawsItsLayersOnItsPosition() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).layer((rect, mouseX, mouseY) -> DrawUtils.SHAPE.drawRect(10D, 10D, 20D, 20D, new Color(0.2F, 0.4F, 0.6F, 1F)));
		this.bridges.open(new NodeUI(ContainerNode.create(300D, 200D, 500D, 500D).append(node)));
		Assert.assertEquals(410D, this.draw(0.2F, 0.4F, 0.6F).getLeft(), 1E-3D);
		Assert.assertEquals(310D, this.draw(0.2F, 0.4F, 0.6F).getTop(), 1E-3D);
	}

	@Test
	public void drawsItsChildrenInTheirZIndexOrder() {
		final RectNode top = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.1F, 0.3F, 0.5F, 1F)).zindex(2);
		final RectNode middle = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.3F, 0.5F, 0.7F, 1F)).zindex(1);
		final RectNode bottom = RectNode.create(0D, 0D, 10D, 10D).color(new Color(0.5F, 0.7F, 0.9F, 1F)).zindex(-1);
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).append(top, middle, bottom);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(2, top.getIndex());
		Assert.assertEquals(Arrays.asList(bottom, middle, top), NodeTest.<IndexedList<Node>>field(node, "drawList").ordered());
		Assert.assertEquals(Arrays.asList(top, middle, bottom), node.getChildren());
		Assert.assertTrue(this.order(0.5F, 0.7F, 0.9F) < this.order(0.2F, 0.4F, 0.6F));
		Assert.assertTrue(this.order(0.2F, 0.4F, 0.6F) < this.order(0.3F, 0.5F, 0.7F));
		Assert.assertTrue(this.order(0.3F, 0.5F, 0.7F) < this.order(0.1F, 0.3F, 0.5F));
	}

	@Test
	public void liftsANodeAlongTheDepthByItsZLevel() {
		final double[] depths = new double[2];
		final RectNode flat = RectNode.create(0D, 0D, 10D, 10D).onDraw(event -> depths[0] = this.bridges.getRender().getModelView().getMatrix()[14]);
		final RectNode lifted = RectNode.create(0D, 0D, 10D, 10D).onDraw(event -> depths[1] = this.bridges.getRender().getModelView().getMatrix()[14]).zlevel(25D);
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
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.scroll(1D);
		this.bridges.getUi().keyPressed(Key.A, Key.A);
		this.bridges.getUi().charTyped('a');
		Assert.assertEquals(Arrays.asList("above pressed", "parent pressed", "below pressed", "above dragged", "parent dragged", "below dragged", "above released", "parent released", "below released", "above scrolled", "parent scrolled", "below scrolled", "above key", "parent key", "below key", "above char", "parent char", "below char"), events);
	}

	@Test
	public void remembersItsLastClickAndKey() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D);
		this.bridges.open(new NodeUI(node));
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertSame(MouseButton.RIGHT, node.getLastClickButton());
		Assert.assertEquals(this.bridges.getClock().currentTimeMillis(), node.getLastClickTime());
		this.bridges.frame();
		this.bridges.getUi().keyPressed(Key.Z, Key.Z);
		this.bridges.getUi().charTyped('z');
		Assert.assertEquals('z', node.getLastCodepoint());
		Assert.assertSame(Key.Z, node.getLastKey());
		Assert.assertEquals(this.bridges.getClock().currentTimeMillis(), node.getLastKeyTime());
	}

	@Test
	public void runsItsPressCallbacksBesideItsClick() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).onClick(event -> events.add("click")).handleMousePressed(event -> events.add("pressed " + event.getButton().name())).handleMouseReleased(event -> events.add("released"));
		this.press(node, 110D, 110D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("pressed LEFT", "click", "released"), events);
	}

	@Test
	public void runsItsRenderCallbackOnEveryFrame() {
		final int[] renders = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onRender(event -> renders[0]++);
		this.bridges.open(new NodeUI(node)).frames(3);
		Assert.assertEquals(4, renders[0]);
	}

	@Test
	public void updatesItsChildrenBeforeItself() {
		final List<String> events = new ArrayList<>();
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D);
		final RecordingNode parent = new RecordingNode("parent", events, 0D, 0D, 100D, 100D).append(child).onUpdate(event -> events.add("callback"));
		parent.fireUpdate();
		Assert.assertEquals(Arrays.asList("child update", "parent update", "callback"), events);
	}

	@Test
	public void countsItsLoads() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(1L, node.getLoadCount());
		node.load(node.getUi());
		Assert.assertEquals(2L, node.getLoadCount());
	}

	@Test
	public void countsNoLoadOnTheRefreshOfAFollowedProperty() {
		final DoubleSignal x = new DoubleSignal(0D);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).x(x);
		this.bridges.open(new NodeUI(node)).frames(2);
		Assert.assertEquals(1L, node.getLoadCount());
		x.set(20D);
		this.bridges.frame();
		Assert.assertEquals(20D, node.getX(), 0D);
		Assert.assertEquals(1L, node.getLoadCount());
	}

	@Test
	public void runsItsActionOnceItsCallbacksAreCleared() {
		final List<String> events = new ArrayList<>();
		final int[] detaches = {0};
		final RecordingNode node = new RecordingNode("node", events, 0D, 0D, 10D, 10D).onDetach(event -> detaches[0]++);
		this.bridges.open(new NodeUI(node));
		events.clear();
		node.getEventBus().clear();
		node.fireDetach();
		Assert.assertEquals(0, detaches[0]);
		Assert.assertEquals(Arrays.asList("node detach"), events);
	}

	@Test
	public void detachesItselfBeforeItsDetachListeners() {
		final List<String> events = new ArrayList<>();
		final RecordingNode node = new RecordingNode("node", events, 0D, 0D, 10D, 10D).onDetach(event -> events.add("detached"));
		this.bridges.open(new NodeUI(node));
		events.clear();
		node.fireDetach();
		Assert.assertEquals(Arrays.asList("node detach", "detached"), events);
	}

	@Test
	public void drawsItsSkeletonUntilItIsMounted() {
		final boolean[] ready = {false};
		final int[] mounts = {0};
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).onMount(event -> mounts[0]++).wait(rect -> ready[0]).skeleton(rect -> RectNode.create(5D, 5D, 20D, 20D).color(new Color(0.6F, 0.4F, 0.2F, 1F)));
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
		final Draw placeholder = this.draw(loading.getRedF(), loading.getGreenF(), loading.getBlueF());
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
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.scroll(1D);
		this.bridges.getUi().keyPressed(Key.A, Key.A);
		this.bridges.getUi().charTyped('a');
		Assert.assertEquals(Arrays.asList("skeleton pressed", "skeleton dragged", "skeleton released", "skeleton scrolled", "skeleton key", "skeleton char"), events);
		ready[0] = true;
		this.bridges.frame();
		events.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void sendsTheReleaseToItsChildrenWhileItsSkeletonIsShown() {
		final List<String> events = new ArrayList<>();
		final boolean[] ready = {true};
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 50D, 50D).handleKeyReleased(event -> events.add("child key released"));
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).append(child).wait(rect -> ready[0]).skeleton(rect -> new RecordingNode("skeleton", events, 0D, 0D, 50D, 50D));
		this.bridges.open(new NodeUI(node));
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().keyPressed(Key.A, Key.A);
		ready[0] = false;
		this.bridges.frame();
		events.clear();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.getUi().keyReleased(Key.A, Key.A);
		Assert.assertEquals(Arrays.asList("skeleton released", "child released", "child key released"), events);
	}

	@Test
	public void loadsItsSkeletonInItsUi() {
		final RectNode early = RectNode.create(0D, 0D, 10D, 10D).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D));
		final ContainerNode container = ContainerNode.create(0D, 0D, 500D, 500D).append(early);
		Assert.assertFalse(early.getSkeleton().isAttached());
		this.bridges.open(new NodeUI(container));
		Assert.assertTrue(early.getSkeleton().isAttached());
		final RectNode late = RectNode.create(0D, 0D, 10D, 10D).attach(container).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D));
		Assert.assertTrue(late.getSkeleton().isAttached());
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
	public void scrollsToTheEndOfWhatItsChildrenHold() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		final RectNode card = RectNode.create(0D, 0D, 200D, 250D);
		RectNode.create(0D, 260D, 500D, 90D).attach(card);
		card.attach(area);
		this.bridges.open(new NodeUI(area));
		this.bridges.frames(2);
		Assert.assertEquals(50D, area.getMaxScrollY(), 0D);
		Assert.assertEquals(100D, area.getMaxScrollX(), 0D);
	}

	@Test
	public void drawsAGrandchildScrolledIntoViewPastItsParent() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		final RectNode card = RectNode.create(0D, 0D, 200D, 250D);
		RectNode.create(0D, 600D, 500D, 90D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).attach(card);
		card.attach(area);
		this.bridges.open(new NodeUI(area));
		this.bridges.frames(2);
		area.scrollY(-area.getMaxScrollY()).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(310D, this.draw(0.2F, 0.4F, 0.6F).getTop(), 1E-3D);
	}

	@Test
	public void stopsItsScrollExtentAtAChildThatClipsItsChildren() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		final RectNode card = RectNode.create(0D, 0D, 200D, 250D).overflow(OverflowProperty.HIDDEN);
		RectNode.create(0D, 260D, 500D, 90D).attach(card);
		card.attach(area);
		this.bridges.open(new NodeUI(area));
		this.bridges.frames(2);
		Assert.assertEquals(0D, area.getMaxScrollY(), 0D);
		Assert.assertEquals(0D, area.getMaxScrollX(), 0D);
	}

	@Test
	public void scrollsToTheEndOfAChildPlacedAtANegativePosition() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 400D, 300D).overflow(OverflowProperty.SCROLL);
		RectNode.create(-50D, -50D, 460D, 360D).attach(area);
		this.bridges.open(new NodeUI(area));
		this.bridges.frames(2);
		Assert.assertEquals(10D, area.getMaxScrollX(), 0D);
		Assert.assertEquals(10D, area.getMaxScrollY(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollY(), 0D);
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
		this.bridges.getWindow().getKeys().add(Key.RIGHT_CONTROL);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-120D, row.getTargetScrollX(), 0D);
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
		area.scrollX(-30D).updateScroll();
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
		this.bridges.scroll(1D, 0D);
		Assert.assertEquals(-30D, area.getTargetScrollX(), 0D);
	}

	@Test
	public void scrollsByTheAmplitudeOfTheWheel() {
		final ContainerNode area = NodeTest.column();
		this.bridges.open(new NodeUI(area));
		this.bridges.move(300D, 150D).frames(2);
		this.bridges.scroll(-0.1D);
		Assert.assertEquals(-3D, area.getTargetScrollY(), 1E-9D);
		this.bridges.scroll(-2D);
		Assert.assertEquals(-63D, area.getTargetScrollY(), 1E-9D);
	}

	@Test
	public void scrollsADiagonalWheelOnceAlongItsOnlyOverflow() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		this.bridges.move(300D, 150D).frames(2);
		this.bridges.scroll(-1D, -1D);
		Assert.assertEquals(-30D, row.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, row.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheHorizontalWheelWithoutHorizontalOverflow() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column));
		this.bridges.move(300D, 150D).frames(2);
		final boolean context = column.fireMouseScroll(300D, 150D, -1D, 0D);
		Assert.assertFalse(context);
		Assert.assertEquals(0D, column.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheHorizontalWheelAtItsHorizontalEnd() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		this.bridges.move(300D, 150D).frames(2);
		final boolean context = row.fireMouseScroll(300D, 150D, 1D, 0D);
		Assert.assertFalse(context);
		Assert.assertEquals(0D, row.getTargetScrollX(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsVerticalEndWhenItOverflowsBothWays() {
		final ContainerNode area = NodeTest.column();
		RectNode.create(0D, 0D, 700D, 10D).attach(area);
		this.bridges.open(new NodeUI(area)).move(300D, 150D).frames(2);
		area.scrollY(-area.getMaxScrollY());
		final boolean context = area.fireMouseScroll(300D, 150D, 0D, -1D);
		Assert.assertFalse(context);
		Assert.assertEquals(-200D, area.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, area.getTargetScrollX(), 0D);
	}

	@Test
	public void forgetsItsScrollOnceItStopsScrolling() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		column.scrollY(-100D).updateScroll();
		this.bridges.frame();
		Assert.assertSame(column, column.overflow(OverflowProperty.HIDDEN));
		Assert.assertFalse(column.hasOverflowY());
		Assert.assertEquals(0D, column.getScrollY(), 0D);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
		Assert.assertEquals(100D, column.getChildren().get(1).getY(), 0D);
		final boolean context = column.fireMouseScroll(300D, 150D, 0D, -1D);
		Assert.assertFalse(context);
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
		final boolean down = column.fireMouseScroll(300D, 150D, 0D, -1D);
		Assert.assertTrue(down);
		Assert.assertEquals(-30D, column.getTargetScrollY(), 0D);
		final boolean up = column.fireMouseScroll(300D, 150D, 0D, 1D);
		Assert.assertTrue(up);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentAtItsLimits() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		final boolean up = column.fireMouseScroll(300D, 150D, 0D, 1D);
		Assert.assertFalse(up);
		column.scrollY(-column.getMaxScrollY());
		final boolean down = column.fireMouseScroll(300D, 150D, 0D, -1D);
		Assert.assertFalse(down);
		Assert.assertEquals(-200D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void leavesTheWheelToItsParentWithNothingToScroll() {
		final ContainerNode box = ContainerNode.create(100D, 100D, 400D, 100D).overflow(OverflowProperty.SCROLL);
		RectNode.create(0D, 0D, 400D, 100D).attach(box);
		this.bridges.open(new NodeUI(box)).move(300D, 150D).frames(2);
		final boolean down = box.fireMouseScroll(300D, 150D, 0D, -1D);
		final boolean up = box.fireMouseScroll(300D, 150D, 0D, 1D);
		Assert.assertFalse(down);
		Assert.assertFalse(up);
	}

	@Test
	public void leavesAStillWheelToItsParent() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		final boolean context = column.fireMouseScroll(300D, 150D, 0D, 0D);
		Assert.assertFalse(context);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void tellsWhetherItsHandlersConsumedAnInputFiredWithoutSource() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).handleMouseReleased(event -> event.consume()).handleCharTyped(event -> event.consume()).handleKeyReleased(event -> event.consume());
		final RectNode idle = RectNode.create(200D, 100D, 50D, 50D);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(node, idle))).frame();
		Assert.assertTrue(node.fireMouseReleased(120D, 120D, MouseButton.LEFT));
		Assert.assertTrue(node.fireCharTyped('a'));
		Assert.assertTrue(node.fireKeyReleased(Key.A, Key.A));
		Assert.assertFalse(idle.fireMouseReleased(220D, 120D, MouseButton.LEFT));
		Assert.assertFalse(idle.fireCharTyped('a'));
		Assert.assertFalse(idle.fireKeyReleased(Key.A, Key.A));
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
		inner.scrollY(-inner.getMaxScrollY());
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
		row.scrollX(-300D);
		this.bridges.frame();
		Assert.assertEquals(-300D * (1D - Math.pow(1D - 0.2D / 3D, 0.96D)), row.getScrollX(), 1E-9D);
		Assert.assertEquals(381D, row.getChildren().get(1).getX(), 1E-4D);
	}

	@Test
	public void reportsTheEndOfAHorizontalScrollOnceItRests() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEndReached(event -> ends.add(event.getNode().getScrollX()));
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
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
		final ContainerNode column = NodeTest.column().onScrollEndReached(event -> ends.add(event.getNode().getScrollY()));
		this.bridges.open(new NodeUI(column));
		column.scrollY(-1000D);
		Assert.assertEquals(-200D, column.getTargetScrollY(), 0D);
		Assert.assertTrue(column.isScrollEndY());
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList(-200D), ends);
		Assert.assertFalse(column.isScrollEndY());
	}

	@Test
	public void forgetsTheEndOfAScrollLeftBeforeItRests() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollEndReached(event -> ends.add(event.getNode().getScrollX()));
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
		this.bridges.frame();
		row.scrollX(-100D);
		this.bridges.frames(200);
		Assert.assertFalse(row.isScrollEndX());
		Assert.assertTrue(ends.isEmpty());
	}

	@Test
	public void announcesTheEndOfAScrollAsSoonAsItAimsAtIt() {
		final List<String> events = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollToEnd(event -> events.add("ending " + event.getNode().getTargetScrollX())).onScrollEndReached(event -> events.add("end " + event.getNode().getScrollX()));
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
		Assert.assertEquals(Arrays.asList("ending -300.0"), events);
		row.scrollX(-2000D);
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList("ending -300.0", "end -300.0"), events);
	}

	@Test
	public void announcesTheEndOfAScrollGivenExactlyOnItBeforeItsFirstMeasure() {
		final List<String> events = new ArrayList<>();
		final ContainerNode row = NodeTest.row().scrollX(-300D).onScrollToEnd(event -> events.add("ending " + event.getNode().getTargetScrollX())).onScrollEndReached(event -> events.add("end " + event.getNode().getScrollX()));
		this.bridges.open(new NodeUI(row));
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList("ending -300.0", "end -300.0"), events);
	}

	@Test
	public void announcesTheEndOfAVerticalScrollWithTheOffsetsItHeadsTo() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode column = NodeTest.column().onScrollToEnd(event -> endings.add(event.getNode().getTargetScrollY()));
		this.bridges.open(new NodeUI(column));
		column.scrollY(-1000D);
		Assert.assertEquals(Arrays.asList(-200D), endings);
		Assert.assertEquals(0D, column.getScrollY(), 0D);
	}

	@Test
	public void announcesTheEndAgainOnceTheScrollComesBackToIt() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollToEnd(event -> endings.add(event.getNode().getTargetScrollX()));
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
		row.scrollX(-100D);
		row.scrollX(-1000D);
		Assert.assertEquals(Arrays.asList(-300D, -300D), endings);
	}

	@Test
	public void waitsForTheEndOfAListThatGrowsWhileScrolling() {
		final List<Double> ends = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollToEnd(event -> {
			if (event.getNode().getChildren().size() == 2) {
				RectNode.create(700D, 0D, 300D, 100D).attach(event.getNode());
			}
		}).onScrollEndReached(event -> ends.add(event.getNode().getScrollX()));
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
		this.bridges.frames(200);
		Assert.assertEquals(-300D, row.getScrollX(), 0D);
		Assert.assertTrue(ends.isEmpty());
		row.scrollX(-1000D);
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList(-600D), ends);
	}

	@Test
	public void scrollsToAPositionInsideItsOverflow() {
		final ContainerNode row = NodeTest.row();
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 1920D, 1080D).append(row, column)));
		Assert.assertSame(row, row.scrollTo(-150D, -40D));
		Assert.assertEquals(-150D, row.getTargetScrollX(), 0D);
		Assert.assertEquals(0D, row.getTargetScrollY(), 0D);
		row.scrollTo(-600D, 0D);
		Assert.assertEquals(-300D, row.getTargetScrollX(), 0D);
		column.scrollTo(30D, -50D);
		Assert.assertEquals(0D, column.getTargetScrollX(), 0D);
		Assert.assertEquals(-50D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void keepsAScrollGivenBeforeItsFirstMeasure() {
		final ContainerNode column = NodeTest.column().onInit(event -> event.getNode().scrollTo(0D, -50D));
		this.bridges.open(new NodeUI(column));
		Assert.assertEquals(-50D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void scrollsToTheEndOfAChildAppendedJustBefore() {
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(column));
		RectNode.create(0D, 300D, 400D, 100D).attach(column);
		column.scrollTo(0D, -1000D);
		this.bridges.frame();
		Assert.assertEquals(-300D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void writesTheWheelIntoItsScrollSignal() {
		final DoubleSignal scroll = new DoubleSignal(0D);
		final ContainerNode column = NodeTest.column().scrollY(scroll);
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-30D, scroll.peek(), 0D);
		scroll.set(-150D);
		Assert.assertEquals(-150D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void writesItsScrollLimitBackIntoItsSignal() {
		final DoubleSignal scroll = new DoubleSignal(-1000D);
		final ContainerNode column = NodeTest.column().scrollY(scroll);
		this.bridges.open(new NodeUI(column)).frames(2);
		Assert.assertEquals(-200D, column.getTargetScrollY(), 0D);
		Assert.assertEquals(-200D, scroll.peek(), 0D);
	}

	@Test
	public void forgetsItsScrollSignalOnceGivenAValue() {
		final DoubleSignal scroll = new DoubleSignal(-100D);
		final ContainerNode column = NodeTest.column().scrollY(scroll).scrollY(-50D);
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		scroll.set(-150D);
		this.bridges.scroll(-1D);
		Assert.assertEquals(-80D, column.getTargetScrollY(), 0D);
		Assert.assertEquals(-150D, scroll.peek(), 0D);
	}

	@Test
	public void scrollsLikeTheWheelThroughItsGesture() {
		final List<String> events = new ArrayList<>();
		final ContainerNode column = NodeTest.column().beforeScroll(event -> events.add("before " + event.getAxis() + " " + event.getOffset())).onScroll(event -> events.add("after " + event.getPrevious() + " " + event.getOffset()));
		this.bridges.open(new NodeUI(column)).frames(2);
		Assert.assertSame(column, column.scrollTo(0D, -120D));
		column.scrollY(-60D);
		Assert.assertEquals(Arrays.asList("before VERTICAL -120.0", "after 0.0 -120.0", "after -120.0 -60.0"), events);
	}

	@Test
	public void keepsItsScrollWhenItsGestureIsRefused() {
		final DoubleSignal scroll = new DoubleSignal(0D);
		final ContainerNode column = NodeTest.column().scrollY(scroll).beforeScroll(event -> event.cancel());
		this.bridges.open(new NodeUI(column)).move(300D, 150D).frames(2);
		column.scrollTo(0D, -120D);
		this.bridges.scroll(-1D);
		Assert.assertEquals(0D, column.getTargetScrollY(), 0D);
		Assert.assertEquals(0D, scroll.peek(), 0D);
		scroll.set(-60D);
		Assert.assertEquals(-60D, column.getTargetScrollY(), 0D);
	}

	@Test
	public void reportsEveryScrollItAimsAt() {
		final List<Double> values = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScroll(event -> values.add(event.getOffset()));
		this.bridges.open(new NodeUI(row));
		row.scrollX(-50D);
		this.bridges.move(300D, 150D).frames(2).scroll(-1D);
		Assert.assertEquals(Arrays.asList(-50D, -80D), values);
	}

	@Test
	public void jumpsStraightToItsScrollTarget() {
		final ContainerNode row = NodeTest.row();
		final ContainerNode column = NodeTest.column();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 1920D, 1080D).append(row, column)));
		row.scrollX(-100D).updateScrollX();
		Assert.assertEquals(-100D, row.getScrollX(), 0D);
		column.scrollY(-50D).updateScrollY();
		Assert.assertEquals(-50D, column.getScrollY(), 0D);
		row.scrollX(-200D).updateScroll();
		Assert.assertEquals(-200D, row.getScrollX(), 0D);
	}

	@Test
	public void movesItsScrollbarAlongTheHorizontalScroll() {
		final Bar bar = new Bar(0D, 110D, 400D, 10D, 40D);
		final ContainerNode row = NodeTest.row().scrollbar(bar);
		this.bridges.open(new NodeUI(row));
		Assert.assertSame(bar, row.getScrollbar());
		Assert.assertSame(row, bar.getScrollNode());
		Assert.assertSame(row, bar.getParent());
		row.scrollX(-150D).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(600D / 7D, bar.getThumb().getX(), 1E-9D);
		Assert.assertEquals(186D, this.draw(0.6F, 0.4F, 0.2F).getLeft(), 1E-3D);
	}

	@Test
	public void movesItsScrollbarAlongTheVerticalScroll() {
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final ContainerNode column = NodeTest.column().scrollbar(bar);
		this.bridges.open(new NodeUI(column));
		column.scrollY(-100D).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(100D / 3D, bar.getThumb().getY(), 1E-9D);
		Assert.assertEquals(133D, this.draw(0.6F, 0.4F, 0.2F).getTop(), 1E-3D);
	}

	@Test
	public void followsItsDraggedScrollbarFaster() {
		final Bar bar = new Bar(0D, 110D, 400D, 10D, 40D);
		final ContainerNode row = NodeTest.row().scrollbar(bar);
		this.press(row, 120D, 215D);
		Assert.assertTrue(bar.isDragging());
		this.bridges.drag(300D, 215D).frame();
		Assert.assertEquals(-300D, row.getTargetScrollX(), 1E-6D);
		Assert.assertEquals(-300D * (1D - Math.pow(2D / 3D, 0.96D)), row.getScrollX(), 1E-6D);
	}

	@Test
	public void forwardsTheMouseAndKeysToItsScrollbar() {
		final List<String> events = new ArrayList<>();
		final Bar bar = new Bar(0D, 110D, 400D, 10D, 40D).handleKeyPressed(event -> events.add("key")).handleCharTyped(event -> events.add("char")).handleMouseScroll(event -> events.add("scrolled")).handleMouseDragged(event -> events.add("dragged")).handleMouseReleased(event -> events.add("released"));
		this.bridges.open(new NodeUI(NodeTest.row().scrollbar(bar)));
		this.bridges.getUi().keyPressed(Key.A, Key.A);
		this.bridges.getUi().charTyped('a');
		this.bridges.scroll(1D);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("key", "char", "scrolled", "dragged", "released"), events);
	}

	@Test
	public void loadsAScrollbarSetOnceItsUiIsOpen() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		final Bar bar = new Bar(0D, 110D, 400D, 10D, 40D);
		row.scrollbar(bar);
		Assert.assertTrue(bar.isAttached());
	}

	@Test
	public void stopsDraggingOnceTheMouseIsGrabbed() {
		final int[] ends = {0};
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragDrop(event -> ends[0]++);
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
	public void settlesAMovedNodeOnAFractionalTarget() {
		final RectNode node = RectNode.create(10D, 10D, 50D, 50D).draggable(DraggableProperty.free());
		this.bridges.resize(1366, 768).open(new NodeUI(ContainerNode.create(111.1D, 47.6D, 1000D, 1000D).append(FlexNode.vertical(333.3D, 333.3D, 200D).append(RectNode.create(0D, 0D, 999.9D, 2333.1D), node)))).frames(3);
		this.bridges.move((node.getAbsoluteX() + 5D) * 1366D / 1920D, (node.getAbsoluteY() + 5D) * 768D / 1080D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.drop(300.15D, 180.09D);
		Assert.assertFalse(node.isDragged());
	}

	@Test
	public void restsAtTheStartOfAParentNarrowerThanItself() {
		final RectNode node = RectNode.create(0D, 0D, 300D, 50D).draggable(DraggableProperty.parent());
		this.bridges.open(new NodeUI(ContainerNode.create(100D, 100D, 200D, 200D).append(node))).frames(100);
		Assert.assertEquals(0D, node.getX(), 0D);
		Assert.assertFalse(node.isDragged());
		this.bridges.frame();
		Assert.assertEquals(0D, node.getX(), 0D);
		Assert.assertFalse(node.isDragged());
	}

	@Test
	public void keepsDraggingWhenAnotherButtonIsReleased() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free());
		this.press(node, 110D, 110D);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		Assert.assertTrue(node.isDragging());
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertFalse(node.isDragging());
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
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().snapType(DraggableSnapType.OVERLAP).snap(target)).onDragSnap(event -> snaps.add(event.getTarget()));
		this.press(node, 110D, 110D);
		this.bridges.move(430D, 430D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(100);
		Assert.assertEquals(Arrays.asList(target), snaps);
		Assert.assertEquals(400D, node.getX(), 0D);
		Assert.assertEquals(400D, node.getY(), 0D);
	}

	@Test
	public void snapsTheDraggedCopyOfANode() {
		final List<Node> snaps = new ArrayList<>();
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY).snapType(DraggableSnapType.OVERLAP).snap(target)).onDragSnap(event -> snaps.add(event.getTarget()));
		this.press(node, 110D, 110D);
		this.bridges.move(430D, 430D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(420D, node.getDraggedNode().getX(), 0D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(target), snaps);
		Assert.assertNull(node.getDraggedNode());
		Assert.assertEquals(100D, node.getX(), 0D);
	}

	@Test
	public void sendsADroppedNodeBackWithoutTargetUnderIt() {
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().snapType(DraggableSnapType.OVERLAP).snap(target));
		this.press(node, 110D, 110D);
		this.bridges.move(310D, 110D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(300D, node.getX(), 0D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
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
	public void drawsTheDraggedCopyOfAnAbsoluteChildUnderTheMouse() {
		final RectNode node = RectNode.create(10D, 10D, 50D, 50D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).position(PositionProperty.ABSOLUTE).draggable(DraggableProperty.free().type(DraggableType.COPY));
		this.press(ContainerNode.create(300D, 200D, 500D, 500D).append(node), 20D, 20D);
		this.bridges.move(220D, 220D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(10D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(210D, draws.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(210D, draws.get(1).getTop(), 1E-3D);
	}

	@Test
	public void drawsTheDraggedCopyOutsideTheEffectsOfTheOriginal() {
		final RectNode node = RectNode
		.create(10D, 10D, 50D, 50D)
		.color(new Color(0.2F, 0.4F, 0.6F, 1F))
		.effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(30D, 0D))))
		.draggable(DraggableProperty.free().type(DraggableType.COPY));
		this.press(ContainerNode.create(300D, 200D, 500D, 500D).append(node), 320D, 220D);
		this.bridges.move(520D, 420D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		final List<Draw> draws = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(340D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(540D, draws.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(410D, draws.get(1).getTop(), 1E-3D);
	}

	@Test
	public void reportsTheStartEveryMoveAndTheEndOfADrag() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragStart(event -> events.add("start")).onDrag(event -> events.add("drag")).onDragDrop(event -> events.add("end"));
		this.press(node, 110D, 110D);
		this.bridges.move(150D, 110D).frames(5);
		this.bridges.getUi().mouseMoved();
		this.bridges.move(200D, 110D).frames(5);
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("start", "drag", "drag", "end"), events);
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
	public void keepsDraggingWhileItsDraggableGivesTheSameBehaviour() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty::free);
		this.press(node, 110D, 110D);
		this.bridges.frame();
		Assert.assertTrue(node.isDragging());
	}

	@Test
	public void stopsDraggingOnceItsDraggableTurnsDisabled() {
		final BooleanSignal locked = new BooleanSignal(false);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(() -> locked.get() ? DraggableProperty.disabled() : DraggableProperty.free());
		this.press(node, 110D, 110D);
		locked.set(true);
		this.bridges.frame();
		Assert.assertFalse(node.isDragging());
	}

	@Test
	public void ignoresAPressWithAnotherButton() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free());
		this.bridges.open(new NodeUI(node));
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertFalse(node.isDragging());
	}

	@Test
	public void letsAChildKeepThePressFromItsDraggableParent() {
		final RectNode child = RectNode.create(0D, 0D, 20D, 20D).onClick(event -> {});
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).draggable(DraggableProperty.free()).append(child);
		this.press(parent, 110D, 110D);
		Assert.assertFalse(parent.isDragging());
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.move(150D, 150D).frame();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).beforeDragDrop(event -> event.cancel()).onDragDrop(event -> events.add("end"));
		this.press(node, 110D, 110D);
		this.bridges.move(310D, 210D).frames(100);
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertEquals(300D, node.getX(), 0D);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertFalse(node.isDragging());
		this.bridges.frames(100);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void dropsTheCopyOfARefusedDragEnd() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY)).beforeDragDrop(event -> event.cancel());
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
	public void followsTheMouseBackIntoItsOverflowAreaDuringTheDrag() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN);
		final RectNode node = RectNode.create(10D, 10D, 20D, 20D).draggable(DraggableProperty.ui()).attach(area);
		this.press(area, 115D, 115D);
		this.bridges.move(615D, 115D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertFalse(node.isVisible());
		this.bridges.move(115D, 115D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		Assert.assertTrue(node.isDragging());
		Assert.assertTrue(node.isVisible());
		Assert.assertEquals(10D, node.getX(), 0D);
		Assert.assertEquals(10D, node.getY(), 0D);
	}

	@Test
	public void keepsItsPositionWhileHiddenOutsideItsDragArea() {
		final ContainerNode area = ContainerNode.create(100D, 100D, 100D, 100D).overflow(OverflowProperty.HIDDEN);
		final RectNode node = RectNode.create(10D, -300D, 20D, 20D).draggable(DraggableProperty.ui()).attach(area);
		this.bridges.open(new NodeUI(area)).frames(100);
		Assert.assertFalse(node.isVisible());
		Assert.assertFalse(node.isDragged());
		Assert.assertEquals(-300D, node.getY(), 0D);
	}

	@Test
	public void showsTheDroppedCopyAndItsSnapToTheDragEnd() {
		final List<Object> received = new ArrayList<>();
		final RectNode target = RectNode.create(400D, 400D, 50D, 50D);
		final DraggableProperty draggable = DraggableProperty.free().type(DraggableType.COPY).snapType(DraggableSnapType.OVERLAP).snap(target);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(draggable).onDragDrop(event -> received.addAll(Arrays.asList(event.getNode().getDraggedNode().getX(), event.getNode().getDraggable().getSnapping(event.getNode().getDraggedNode()))));
		this.press(node, 110D, 110D);
		this.bridges.move(430D, 430D).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.frames(100);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(420D, target), received);
		Assert.assertNull(node.getDraggedNode());
		Assert.assertFalse(node.isDragged());
	}

	@Test
	public void writesItsDragIntoItsPositionSignals() {
		final DoubleSignal x = new DoubleSignal(100D);
		final DoubleSignal y = new DoubleSignal(100D);
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D).x(x).y(y).draggable(DraggableProperty.free());
		this.press(node, 110D, 110D);
		this.drop(310D, 210D);
		Assert.assertEquals(300D, x.peek(), 0D);
		Assert.assertEquals(200D, y.peek(), 0D);
		x.set(50D);
		Assert.assertEquals(50D, node.getX(), 0D);
		Assert.assertEquals(200D, node.getY(), 0D);
	}

	@Test
	public void refusesToMoveAComputedPosition() {
		final double[] x = {100D};
		try {
			RectNode.create(0D, 100D, 50D, 50D).x(() -> x[0]).draggable(DraggableProperty.free());
			Assert.fail("A MOVE drag cannot move a computed position");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The RectNode is dragged with DraggableType.MOVE but its x is computed (a lambda or a signal that cannot be written), so a drag cannot move it: give x(...) a value or a writable Signal, or drag a copy with DraggableType.COPY", expected.getMessage());
		}
		try {
			RectNode.create(0D, 100D, 50D, 50D).draggable(DraggableProperty.free()).y(new DoubleSignal(5D).map(value -> value * 2D));
			Assert.fail("A computed position cannot be given to a node dragged with MOVE");
		} catch (final IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("its y is computed"));
		}
		Assert.assertSame(PositionProperty.ABSOLUTE, RectNode.create(0D, 0D, 50D, 50D).x(() -> x[0]).draggable(DraggableProperty.free().type(DraggableType.COPY)).position(PositionProperty.ABSOLUTE).getPosition());
	}

	@Test
	public void forgetsItsPositionSignalOnceGivenAValue() {
		final DoubleSignal x = new DoubleSignal(100D);
		final RectNode node = RectNode.create(0D, 100D, 50D, 50D).x(x).x(60D).draggable(DraggableProperty.free());
		this.press(node, 70D, 110D);
		this.drop(270D, 110D);
		Assert.assertEquals(260D, node.getX(), 0D);
		Assert.assertEquals(100D, x.peek(), 0D);
		x.set(10D);
		Assert.assertEquals(260D, node.getX(), 0D);
	}

	@Test
	public void movesLikeADragThroughItsGesture() {
		final List<Object> events = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).beforeMove(event -> events.add(event.getValue())).onMove(event -> events.add(event.getPrevious()));
		this.bridges.open(new NodeUI(node));
		Assert.assertSame(node, node.move(200D, 150D));
		node.x(250D);
		Assert.assertEquals(Arrays.asList(Point.of(200D, 150D), Point.of(100D, 100D), Point.of(200D, 150D)), events);
		Assert.assertEquals(250D, node.getX(), 0D);
		Assert.assertEquals(150D, node.getY(), 0D);
	}

	@Test
	public void reportsTheMovesOfItsDrag() {
		final List<Object> moves = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).beforeMove(event -> moves.add("before")).onMove(event -> moves.add(event.getValue()));
		this.press(node, 110D, 110D);
		this.drop(310D, 110D);
		Assert.assertEquals("before", moves.get(0));
		Assert.assertEquals(Point.of(300D, 100D), moves.get(moves.size() - 1));
	}

	@Test
	public void keepsItsPlaceWhenItsMoveIsRefused() {
		final DoubleSignal x = new DoubleSignal(100D);
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).x(x).draggable(DraggableProperty.free()).beforeMove(event -> event.cancel());
		this.press(node, 110D, 110D);
		this.drop(310D, 210D);
		node.move(20D, 20D);
		Assert.assertEquals(100D, node.getX(), 0D);
		Assert.assertEquals(100D, node.getY(), 0D);
		Assert.assertEquals(100D, x.peek(), 0D);
		x.set(40D);
		Assert.assertEquals(40D, node.getX(), 0D);
	}

	@Test
	public void movesOnlyTheGhostOfACopyDrag() {
		final List<Object> moves = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free().type(DraggableType.COPY)).onMove(event -> moves.add(event.getValue()));
		this.press(node, 110D, 110D);
		this.drop(310D, 210D);
		Assert.assertTrue(moves.isEmpty());
		Assert.assertEquals(100D, node.getX(), 0D);
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

	@Test
	public void refusesToDragATopLevelNodeInsideItsParent() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.parent());
		final String output = NodeTest.capture(() -> this.press(node, 110D, 110D));
		Assert.assertTrue(output, output.contains("The node RectNode is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()"));
		Assert.assertFalse(node.isDragging());
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
		list.scrollY(-50D).updateScroll();
		this.bridges.frames(2);
		Assert.assertEquals(60D, child.getY(), 1E-5D);
	}

	@Test
	public void leavesTheScrollOfItsFormerParent() {
		final RectNode child = RectNode.create(0D, 10D, 50D, 50D);
		final ContainerNode list = ContainerNode.create(0D, 0D, 400D, 200D).overflow(OverflowProperty.SCROLL).append(RectNode.create(0D, 0D, 400D, 1000D), child);
		final ContainerNode panel = ContainerNode.create(500D, 0D, 200D, 200D);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 800D, 600D).append(list, panel)));
		list.scrollY(-50D).updateScroll();
		this.bridges.frames(2);
		Assert.assertEquals(-40D, child.getY(), 1E-5D);
		panel.append(child);
		this.bridges.frame();
		Assert.assertEquals(10D, child.getY(), 1E-5D);
	}

	@Test
	public void appendsItsChildrenInOneCall() {
		final List<Node> appended = new ArrayList<>();
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).onChildAppend(event -> appended.add(event.getChild()));
		final RectNode first = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode second = RectNode.create(10D, 0D, 10D, 10D);
		Assert.assertSame(parent, parent.append(first, second));
		Assert.assertEquals(Arrays.asList(first, second), appended);
		Assert.assertEquals(Arrays.asList(first, second), parent.getChildren());
		Assert.assertSame(parent, second.getParent());
		Assert.assertFalse(first.isAttached());
	}

	@Test
	public void movesAChildAppendedToAnotherParent() {
		final List<String> events = new ArrayList<>();
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D);
		final ContainerNode first = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(first));
		events.clear();
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
		final RecordingNode first = new RecordingNode("first", events, 0D, 0D, 10D, 10D).onDetach(event -> events.add("callback"));
		final RecordingNode second = new RecordingNode("second", events, 0D, 0D, 10D, 10D);
		final RectNode kept = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode stranger = RectNode.create(0D, 0D, 10D, 10D);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(first, kept, second);
		this.bridges.open(new NodeUI(parent));
		events.clear();
		Assert.assertSame(parent, parent.remove(first, second, stranger));
		Assert.assertEquals(Arrays.asList(kept), parent.getChildren());
		Assert.assertEquals(Arrays.asList("first detach", "callback", "second detach"), events);
		Assert.assertNull(first.getParent());
	}

	@Test
	public void disablesItsWholeTree() {
		final int[] clicks = {0};
		final boolean[] enabled = {false};
		final RectNode child = RectNode.create(0D, 0D, 50D, 50D).onClick(event -> clicks[0]++);
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).enabled(() -> enabled[0]).append(child);
		this.bridges.open(new NodeUI(parent));
		this.bridges.move(110D, 110D).frames(2);
		Assert.assertFalse(child.isEnabled());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(0, clicks[0]);
		enabled[0] = true;
		this.bridges.frame();
		Assert.assertTrue(child.isEnabled());
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(1, clicks[0]);
	}

	@Test
	public void loadsAChildAppendedOnceItsUiIsOpen() {
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D);
		final NodeUI ui = new NodeUI(parent);
		this.bridges.open(ui);
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).attach(parent);
		Assert.assertTrue(child.isAttached());
		Assert.assertSame(ui, child.getUi());
	}

	@Test
	public void loadsAChildAppendedToARemovedParentOnceTheParentIsBack() {
		final List<String> events = new ArrayList<>();
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D);
		final NodeUI ui = new NodeUI(parent);
		this.bridges.open(ui);
		ui.remove(parent);
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D).attach(parent);
		Assert.assertFalse(child.isAttached());
		Assert.assertTrue(events.isEmpty());
		ui.add(parent);
		Assert.assertTrue(child.isAttached());
		Assert.assertEquals(Arrays.asList("child init"), events);
	}

	@Test
	public void detachesAChildWhoseInitFails() {
		final Signal<Integer> signal = new Signal<>(0);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D);
		this.bridges.open(new NodeUI(parent));
		final BrokenNode child = new BrokenNode().watch(signal);
		try {
			parent.append(child);
			Assert.fail("The init of the child fails");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The init of this node fails on purpose", expected.getMessage());
		}
		Assert.assertFalse(child.isAttached());
		Assert.assertNull(child.getParent());
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
	}

	@Test
	public void finishesItsDetachWhenItsDetachHookFails() {
		final DoubleSignal x = new DoubleSignal(0D);
		final SloppyNode sloppy = new SloppyNode().x(x);
		final List<String> events = new ArrayList<>();
		final RecordingNode sibling = new RecordingNode("sibling", events, 0D, 0D, 10D, 10D);
		final int[] detaches = {0};
		sloppy.onDetach(event -> detaches[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(sloppy, sibling);
		this.bridges.open(new NodeUI(parent));
		events.clear();
		NodeTest.forgetWarnings();
		final String output = NodeTest.capture(() -> parent.remove(sloppy, sibling));
		Assert.assertTrue(output, output.contains("The detach of SloppyNode in NodeUI failed (java.lang.IllegalStateException: The detach of this node fails on purpose), the node is detached anyway"));
		Assert.assertTrue(parent.getChildren().isEmpty());
		Assert.assertTrue(NodeTest.<IndexedList<Node>>field(parent, "drawList").ordered().isEmpty());
		Assert.assertEquals(Arrays.asList("sibling detach"), events);
		Assert.assertEquals(1, detaches[0]);
		Assert.assertFalse(NodeTest.subscribed(sloppy));
		x.set(42D);
		Assert.assertEquals(0D, sloppy.getX(), 0D);
	}

	@Test
	public void keepsTheInitFailureWhenTheDetachAfterItFails() {
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D);
		this.bridges.open(new NodeUI(parent));
		final Node child = new Node(0D, 0D, 10D, 10D) {

			private Object resource;

			@Override
			public void init(final @NonNull UI ui) {
				throw new IllegalStateException("The init of this node fails on purpose");
			}

			@Override
			public void detach() {
				this.resource.hashCode();
			}

		};
		NodeTest.capture(() -> {
			try {
				parent.append(child);
				Assert.fail("The init of the child fails");
			} catch (final IllegalStateException expected) {
				Assert.assertEquals("The init of this node fails on purpose", expected.getMessage());
			}
		});
		Assert.assertFalse(child.isAttached());
	}

	@Test
	public void placesAtItsOwnPositionAChildAddedToTheUIAfterItsAppendFailed() {
		final boolean[] fails = {true};
		final RectNode parent = RectNode.create(100D, 100D, 50D, 50D).visible(false);
		final NodeUI ui = new NodeUI(parent);
		this.bridges.open(ui);
		final Node child = new Node(10D, 10D, 10D, 10D) {

			@Override
			public void init(final @NonNull UI ui) {
				if (fails[0]) {
					throw new IllegalStateException("The first init of this node fails on purpose");
				}
			}

		};
		try {
			parent.append(child);
			Assert.fail("The first init of the child fails");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The first init of this node fails on purpose", expected.getMessage());
		}
		fails[0] = false;
		ui.add(child);
		Assert.assertNull(child.getParent());
		Assert.assertEquals(10D, child.getAbsoluteX(), 0D);
		Assert.assertEquals(10D, child.getAbsoluteY(), 0D);
		Assert.assertTrue(child.isVisible());
	}

	@Test
	public void movesAChildAppendedAgainToTheEndWithoutReloadingIt() {
		final List<String> events = new ArrayList<>();
		final RecordingNode first = new RecordingNode("first", events, 0D, 0D, 10D, 10D);
		final RecordingNode second = new RecordingNode("second", events, 0D, 0D, 10D, 10D);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(first, second).onChildAppend(event -> events.add("append"));
		this.bridges.open(new NodeUI(parent));
		events.clear();
		parent.append(first);
		Assert.assertTrue(events.isEmpty());
		Assert.assertEquals(Arrays.asList(second, first), parent.getChildren());
		Assert.assertEquals("0.1", first.getMappedIndex());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("second update", "first update"), events.subList(0, 2));
	}

	@Test
	public void movesATopLevelNodeAttachedAgainToTheEndWithoutReloadingIt() {
		final List<String> events = new ArrayList<>();
		final RecordingNode first = new RecordingNode("first", events, 0D, 0D, 10D, 10D);
		final RecordingNode second = new RecordingNode("second", events, 0D, 0D, 10D, 10D);
		final NodeUI ui = new NodeUI(first);
		this.bridges.open(ui);
		second.attach(ui);
		events.clear();
		first.attach(ui);
		Assert.assertTrue(events.isEmpty());
		Assert.assertEquals(Arrays.asList(second, first), ui.getNodeList());
	}

	@Test
	public void updatesItsScrollbarAndItsSkeleton() {
		final List<String> events = new ArrayList<>();
		final Bar bar = new Bar(90D, 0D, 10D, 100D, 20D).onUpdate(event -> events.add("scrollbar"));
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(bar).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D).onUpdate(event -> events.add("skeleton"))).wait(rect -> false);
		this.bridges.open(new NodeUI(node));
		events.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("scrollbar", "skeleton"), events);
	}

	@Test
	public void sendsTheReleaseToTheNodesHiddenOrDisabledSinceThePress() {
		final List<String> events = new ArrayList<>();
		final boolean[] shown = {true};
		final RecordingNode hidden = new RecordingNode("hidden", events, 0D, 0D, 50D, 50D).handleMouseReleased(event -> events.add("hidden handler"));
		final RecordingNode disabled = new RecordingNode("disabled", events, 0D, 0D, 50D, 50D).enabled(() -> shown[0]).handleMouseReleased(event -> events.add("disabled handler"));
		final RecordingNode inert = new RecordingNode("inert", events, 0D, 0D, 50D, 50D).interactive(() -> shown[0]);
		final ContainerNode panel = ContainerNode.create(0D, 0D, 100D, 100D).visible(() -> shown[0]).append(hidden);
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 400D, 400D).append(panel, disabled, inert)));
		this.bridges.move(25D, 25D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		shown[0] = false;
		events.clear();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(new HashSet<>(Arrays.asList("hidden released", "hidden handler", "disabled released", "disabled handler", "inert released")), new HashSet<>(events));
	}

	@Test
	public void detachesTheNodeOfItsHoverWithItself() {
		final RectNode tooltip = RectNode.create(0D, 0D, 10D, 10D);
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).hover(NodeHoverElement.follow(tooltip));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 400D, 400D).append(node);
		this.bridges.open(new NodeUI(parent)).move(150D, 150D).frames(2);
		Assert.assertTrue(tooltip.isAttached());
		parent.remove(node);
		Assert.assertFalse(tooltip.isAttached());
		parent.append(node);
		this.bridges.frames(2);
		Assert.assertTrue(tooltip.isAttached());
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
		Assert.assertEquals(Arrays.asList(first, second), parent.getChildren(RectNode.class));
		Assert.assertEquals(3, parent.getChildren(Node.class).size());
	}

	@Test
	public void detachesItsChildrenWhenCleared() {
		final List<String> events = new ArrayList<>();
		final RecordingNode grandchild = new RecordingNode("grandchild", events, 0D, 0D, 10D, 10D);
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D).append(grandchild).onDetach(event -> events.add("callback"));
		final RectNode parent = RectNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		events.clear();
		Assert.assertSame(parent, parent.clearChildren());
		Assert.assertEquals(Arrays.asList("grandchild detach", "child detach", "callback"), events);
		Assert.assertTrue(parent.getChildren().isEmpty());
	}

	@Test
	public void clearsTheParentOfItsChildrenWhenCleared() {
		final RectNode grandchild = RectNode.create(0D, 0D, 5D, 5D);
		final RectNode child = RectNode.create(10D, 20D, 10D, 10D).append(grandchild);
		final RectNode parent = RectNode.create(100D, 100D, 100D, 100D).append(child).visible(() -> false).enabled(() -> false);
		final List<Node> parents = new ArrayList<>();
		child.onDetach(event -> parents.add(event.getNode().getParent()));
		this.bridges.open(new NodeUI(parent));
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
		Assert.assertSame(ui.useStore(CountStore.class, CountStore::new), node.useStore(CountStore.class, CountStore::new));
	}

	@Test
	public void takesTheUiItIsGiven() {
		final NodeUI ui = new NodeUI(ContainerNode.create(0D, 0D, 10D, 10D));
		final RectNode node = NodeTest.assign(RectNode.create(0D, 0D, 10D, 10D), "ui", ui);
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
		Assert.assertEquals(15D, copy.getDefaultX(), 0D);
		Assert.assertEquals(node.getX(), copy.getX(), 0D);
		Assert.assertEquals(node.getY(), copy.getY(), 0D);
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
		final Bar bar = new Bar(410D, 0D, 10D, 100D, 20D);
		final ContainerNode column = NodeTest.column().hoverDuration(50L).hoverEquation(TweenEquations.QUAD_IN).scrollSpeed(2D).wait(node -> false).animate(animator).scrollbar(bar);
		final ContainerNode copy = column.copy();
		Assert.assertEquals(50L, copy.getHoverDuration());
		Assert.assertSame(TweenEquations.QUAD_IN, copy.getHoverEquation());
		Assert.assertEquals(2D, copy.getScrollSpeed(), 0D);
		Assert.assertFalse(copy.isMounted());
		Assert.assertTrue(NodeTest.<Map<?, ?>>field(copy, "animatorMap").containsKey(animator));
		Assert.assertNotSame(bar, copy.getScrollbar());
		Assert.assertSame(Bar.class, copy.getScrollbar().getClass());
		Assert.assertSame(copy, copy.getScrollbar().getScrollNode());
		Assert.assertNotSame(bar.getThumb(), ((Bar) copy.getScrollbar()).getThumb());
		Assert.assertSame(copy.getScrollbar(), ((Bar) copy.getScrollbar()).getThumb().getParent());
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
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The node NamedNode cannot be copied: declare a constructor (x, y, width, height) or override instantiate()", expected.getMessage());
		}
	}

	@Test
	public void reportsTheFailureOfTheConstructorOfACopy() {
		try {
			new FragileNode().width(-1D).copy();
			Assert.fail("A copy whose constructor fails cannot be built");
		} catch (final IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith("The node FragileNode cannot be copied: its constructor "));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().endsWith(" failed (java.lang.IllegalArgumentException: A fragile node needs a positive width)"));
			Assert.assertEquals("A fragile node needs a positive width", expected.getCause().getMessage());
		}
	}

	@Test
	public void keepsTheCallbacksOfTheOriginalInItsCopy() {
		final int[] clicks = {0};
		final RectNode copy = RectNode.create(100D, 100D, 50D, 50D).onClick(event -> clicks[0]++).copy();
		this.press(copy, 110D, 110D);
		Assert.assertEquals(1, clicks[0]);
	}

	@Test
	public void copiesANodeBuiltFromOtherSimpleArguments() {
		final CircleNode circle = CircleNode.create(10D, 20D, 30D);
		final CircleNode circleCopy = circle.copy();
		Assert.assertEquals(30D, circleCopy.getWidth(), 0D);
		Assert.assertEquals(10D, circleCopy.getX(), 0D);
		final FlexNode flex = FlexNode.horizontal(1D, 2D, 3D);
		final FlexNode flexCopy = flex.copy();
		Assert.assertSame(Axis.HORIZONTAL, flexCopy.getDirection());
		Assert.assertEquals(3D, flexCopy.getHeight(), 0D);
	}

	@Test
	public void copiesWithoutParent() {
		final RectNode child = RectNode.create(1D, 2D, 3D, 4D);
		ContainerNode.create(100D, 100D, 10D, 10D).append(child);
		Assert.assertNull(child.copy().getParent());
	}

	@Test
	public void copiesAFlexChildAtItsDeclaredPosition() {
		final RectNode child = RectNode.create(0D, 0D, 80D, 50D);
		this.bridges.open(new NodeUI(FlexNode.horizontal(0D, 0D, 50D).margin(0D).append(RectNode.create(0D, 0D, 100D, 50D), child)));
		final RectNode copy = child.copy();
		this.bridges.open(new NodeUI(copy));
		Assert.assertEquals(100D, child.getX(), 0D);
		Assert.assertEquals(0D, copy.getX(), 0D);
	}

	@Test
	public void copiesTheCurrentValuesWithoutTheirSources() {
		final DoubleSignal width = new DoubleSignal(10D);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).width(width);
		final RectNode copy = node.copy();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 100D, 100D).append(node, copy))).frame();
		width.set(25D);
		this.bridges.frame();
		Assert.assertEquals(25D, node.getWidth(), 0D);
		Assert.assertEquals(10D, copy.getWidth(), 0D);
	}

	@Test
	public void copiesTheValuesAndLayersItsConstructorSetsOnce() {
		final DefaultedNode node = new DefaultedNode().width(30D);
		node.base = 25D;
		final DefaultedNode copy = node.copy();
		this.bridges.open(new NodeUI(copy)).frame();
		Assert.assertEquals(30D, copy.getWidth(), 0D);
		Assert.assertEquals(1, this.bridges.getRender().getDraws(0.3F, 0.5F, 0.7F).size());
	}

	@Test
	public void copiesItsVisibilityAndInteractivityWithoutTheirSources() {
		final boolean[] on = {true};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).visible(() -> on[0]).enabled(() -> on[0]).interactive(() -> on[0]);
		final RectNode copy = node.copy();
		this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 100D, 100D).append(node, copy)));
		on[0] = false;
		this.bridges.frame();
		Assert.assertFalse(node.isVisibleProperty());
		Assert.assertTrue(copy.isVisibleProperty());
		Assert.assertTrue(copy.isEnabled());
		Assert.assertTrue(copy.isInteractive());
	}

	@Test
	public void leavesTheSignalsOfTheOriginalOutOfItsCopy() {
		final Signal<Integer> signal = new Signal<>(1);
		final List<Object> values = new ArrayList<>();
		final BindingNode copy = new BindingNode().link("value", signal, values).copy();
		this.bridges.open(new NodeUI(copy));
		signal.set(2);
		Assert.assertEquals(Arrays.asList(1), values);
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
	}

	@Test
	public void leavesTheWatchesOfTheOriginalOutOfItsCopy() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] watches = {0};
		final RectNode copy = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> watches[0]++).copy();
		this.bridges.open(new NodeUI(copy));
		signal.set(1);
		Assert.assertEquals(0, watches[0]);
	}

	@Test
	public void keepsTheGhostOfACopyDragOutOfTheSignals() {
		final Signal<Integer> signal = new Signal<>(1);
		final List<Object> values = new ArrayList<>();
		final BindingNode node = new BindingNode().link("value", signal, values).draggable(DraggableProperty.free().type(DraggableType.COPY));
		this.press(node, 5D, 5D);
		Assert.assertNotNull(node.getDraggedNode());
		Assert.assertTrue(node.getDraggedNode().getEventBus().isEmpty());
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		signal.set(2);
		this.drop(300D, 300D);
		Assert.assertNull(node.getDraggedNode());
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		signal.set(3);
		Assert.assertEquals(Arrays.asList(1, 2, 3), values);
	}

	@Test
	public void givesTheGhostOfACopyDragTheParentOfTheOriginal() {
		final DoubleSignal width = new DoubleSignal(50D);
		final RectNode node = RectNode.create(10D, 10D, 50D, 50D).width(width).draggable(DraggableProperty.free().type(DraggableType.COPY));
		final ContainerNode parent = ContainerNode.create(100D, 100D, 300D, 300D).append(node);
		this.press(parent, 120D, 120D);
		final Node ghost = node.getDraggedNode();
		Assert.assertSame(parent, ghost.getParent());
		Assert.assertEquals(110D, ghost.getX(), 0D);
		width.set(70D);
		this.bridges.frame();
		Assert.assertEquals(50D, ghost.getWidth(), 0D);
	}

	@Test
	public void pairsTheInitAndDetachOfItsEffectsWhileAttached() {
		final List<String> events = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).effect(new SkippedEffect(events));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent));
		node.effect(new RecordingEffect("first", events));
		node.effect(new RecordingEffect("second", events));
		node.effect(new OtherEffect("other", events));
		node.removeEffect(OtherEffect.class);
		node.clearEffects();
		parent.remove(node);
		node.effect(new RecordingEffect("detached", events));
		node.removeEffect(RecordingEffect.class);
		Assert.assertEquals(Arrays.asList("skipped init", "first init", "first detach", "second init", "other init", "other detach", "skipped detach", "second detach"), events);
	}

	@Test
	public void detachesTheScrollbarAndTheSkeletonItReplaces() {
		final List<String> events = new ArrayList<>();
		final Bar first = new Bar(90D, 0D, 10D, 100D, 20D);
		final Bar second = new Bar(90D, 0D, 10D, 100D, 20D);
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(first).skeleton(rect -> new RecordingNode("old", events, 0D, 0D, 10D, 10D));
		this.bridges.open(new NodeUI(node));
		events.clear();
		node.scrollbar(second).skeleton(rect -> new RecordingNode("new", events, 0D, 0D, 10D, 10D));
		Assert.assertFalse(first.isAttached());
		Assert.assertTrue(second.isAttached());
		Assert.assertEquals(Arrays.asList("old detach", "new init"), events);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void showsItsChildrenReadOnly() {
		ContainerNode.create(0D, 0D, 10D, 10D).getChildren().add(RectNode.create(0D, 0D, 1D, 1D));
	}

	@Test
	public void listsItsChildrenInTheirOrderOfAddition() {
		final RectNode first = RectNode.create(0D, 0D, 1D, 1D).zindex(5);
		final RectNode second = RectNode.create(0D, 0D, 1D, 1D);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 10D, 10D).append(first, second);
		Assert.assertEquals(Arrays.asList(first, second), parent.getChildren());
		Assert.assertEquals(Arrays.asList(first, second), parent.getChildren(RectNode.class));
		Assert.assertSame(first, parent.getChild(0, RectNode.class));
		Assert.assertEquals(Arrays.asList(second, first), NodeTest.<IndexedList<Node>>field(parent, "drawList").ordered());
	}

	@Test
	public void endsTheHoverOfANodeHiddenUnderTheMouse() {
		final List<String> events = new ArrayList<>();
		final BooleanSignal shown = new BooleanSignal(true);
		final RectNode child = RectNode.create(10D, 10D, 50D, 50D).onHoverEnd(event -> events.add("child end"));
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).visible(shown).append(child).onHoverEnd(event -> events.add("node end"));
		this.bridges.open(new NodeUI(node)).move(120D, 120D).frames(20);
		Assert.assertTrue(child.isHovered());
		Assert.assertTrue(child.hoverValue(1F) > 0F);
		shown.set(false);
		this.bridges.frame();
		Assert.assertFalse(node.isHovered());
		Assert.assertFalse(child.isHovered());
		Assert.assertEquals(Arrays.asList("node end", "child end"), events);
		Assert.assertEquals(0F, child.hoverValue(1F), 0F);
	}

	@Test
	public void keepsItsHoverValueWithinZeroAndOne() {
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).hoverDuration(200L).hoverEquation(TweenEquations.BACK_OUT);
		this.bridges.open(new NodeUI(node)).move(120D, 120D).frame();
		float raw = 0F;
		float shown = 0F;
		for (int frame = 0; frame < 20; frame++) {
			this.bridges.frame();
			raw = Math.max(raw, node.getHoverAnimator().get());
			shown = Math.max(shown, node.hoverValue(1F));
		}

		Assert.assertTrue(raw > 1F);
		Assert.assertEquals(1F, shown, 0F);
	}

	@Test
	public void leavesItsAbsoluteChildrenOutOfItsScroll() {
		final RectNode absolute = RectNode.create(10D, 900D, 20D, 20D).position(PositionProperty.ABSOLUTE);
		final ContainerNode column = NodeTest.column().append(absolute);
		this.bridges.open(new NodeUI(column)).frames(2);
		Assert.assertEquals(200D, column.getMaxScrollY(), 0D);
		column.scrollY(-200D).updateScroll();
		this.bridges.frame();
		Assert.assertEquals(900D, absolute.getY(), 0D);
		Assert.assertEquals(-200D, column.getChild(2, RectNode.class).getShiftY(), 1E-3D);
	}

	@Test
	public void keepsUpdatingTheOtherNodesWhenAnUpdateFails() {
		final List<String> events = new ArrayList<>();
		final RecordingNode after = new RecordingNode("after", events, 0D, 0D, 10D, 10D);
		NodeTest.forgetWarnings();
		final String output = NodeTest.capture(() -> {
			this.bridges.open(new NodeUI(ContainerNode.create(0D, 0D, 100D, 100D).append(new FailingNode(), after)));
			events.clear();
			this.bridges.frames(2);
		});
		Assert.assertEquals(Arrays.asList("after update", "after update"), events);
		Assert.assertEquals(1, output.split("The update of FailingNode in NodeUI failed", -1).length - 1);
	}

	@Test
	public void warnsOnceAboutAnUpdateThatFailsWithAnotherMessageEveryFrame() {
		final int[] frames = {0};
		final Node node = new Node(0D, 0D, 10D, 10D) {

			@Override
			public void update() {
				throw new IllegalStateException("The update fails on frame " + frames[0]++);
			}

		};
		final String output = NodeTest.capture(() -> this.bridges.open(new NodeUI(node)).frames(3));
		Assert.assertTrue(frames[0] > 2);
		Assert.assertEquals(1, output.split("failed \\(java.lang.IllegalStateException", -1).length - 1);
	}

	@Test
	public void keepsTheFrameGoingWhenAnInputFails() {
		final String output = NodeTest.capture(() -> {
			this.bridges.open(new NodeUI(new FailingNode())).move(5D, 5D).frames(2);
			this.bridges.getUi().mousePressed(MouseButton.LEFT);
			this.bridges.getUi().mousePressed(MouseButton.LEFT);
			this.bridges.frame();
		});
		Assert.assertEquals(1, output.split("The mousePressed of NodeUI failed", -1).length - 1);
	}

	@Test
	public void clicksOnlyWithTheLeftButton() {
		final List<MouseButton> clicks = new ArrayList<>();
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).onClick(event -> clicks.add(event.getButton()));
		this.bridges.open(new NodeUI(node)).move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(MouseButton.LEFT), clicks);
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
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).append(RectNode.create(0D, 0D, 5D, 5D)).onInit(event -> inits[0]++).watch(signal).onWatch(event -> {
			watches[0]++;
			watched.addAll(event.getProperties());
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
		.watch(signal, (rect, source) -> applied.add("first " + source.get()), WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY, (rect, source) -> applied.add("last " + rect.getChildren().size()));
		this.bridges.open(new NodeUI(node));
		applied.clear();
		signal.set(3);
		Assert.assertEquals(Arrays.asList("first 3", "body 0", "last 1"), applied);
	}

	@Test
	public void appliesItsWatchPropertiesBeforeItsWatchListeners() {
		final Signal<Integer> signal = new Signal<>(0);
		final List<String> applied = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).body(rect -> applied.add("body")).watch(signal, WatchProperty.BODY).onWatch(event -> applied.add("watch"));
		this.bridges.open(new NodeUI(node));
		applied.clear();
		signal.set(1);
		Assert.assertEquals(Arrays.asList("body", "watch"), applied);
	}

	@Test
	public void stopsWatchingOnceItsUiIsClosed() {
		final Signal<Integer> signal = new Signal<>(0);
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D).watch(signal));
		this.bridges.open(ui);
		signal.set(1);
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		JOID.close(ui);
		signal.set(2);
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
	}

	@Test
	public void startsAWatchMadeOutsideAnyUiOnceAttached() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] watches = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> watches[0]++);
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
		signal.set(1);
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D));
		this.bridges.open(ui);
		node.attach(ui);
		Assert.assertEquals(0, watches[0]);
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		signal.set(2);
		Assert.assertEquals(1, watches[0]);
	}

	@Test
	public void keepsAWatchMadeWhileItsUiIsBuilt() {
		final WatchingUI ui = new WatchingUI();
		this.bridges.open(ui);
		Assert.assertEquals(1, ui.watches);
		Assert.assertEquals(1, NodeTest.subscribers(ui.signal).size());
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
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
	}

	@Test
	public void forgetsTheWatchesOfTheNodesDroppedByAReload() {
		final Signal<Integer> signal = new Signal<>(0);
		final ReloadedUI ui = new ReloadedUI(signal);
		this.bridges.open(ui);
		ui.reload();
		ui.reload();
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		signal.set(1);
		Assert.assertEquals(1, ui.watches);
	}

	@Test
	public void stopsWatchingOnceRemoved() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> reloads[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.remove(child);
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
		signal.set(1);
		Assert.assertEquals(0, reloads[0]);
	}

	@Test
	public void stopsWatchingInTheWholeClearedTree() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode grandchild = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> reloads[0]++);
		final ContainerNode child = ContainerNode.create(0D, 0D, 50D, 50D).watch(signal).onWatch(event -> reloads[0]++).append(grandchild);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.clearChildren();
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
		signal.set(1);
		Assert.assertEquals(0, reloads[0]);
	}

	@Test
	public void ignoresAPublishThatReachesItAfterItsDetach() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> reloads[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		final List<SignalListener<Integer>> pending = new ArrayList<>(NodeTest.subscribers(signal));
		parent.remove(child);
		for (final SignalListener<Integer> subscriber : pending) {
			subscriber.onChange(1);
		}
		Assert.assertEquals(0, reloads[0]);
	}

	@Test
	public void watchesAgainOnceAttachedAgain() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] reloads = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> reloads[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		this.bridges.open(new NodeUI(parent));
		parent.remove(child);
		parent.append(child);
		parent.remove(child);
		parent.append(child);
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		signal.set(1);
		Assert.assertEquals(1, reloads[0]);
	}

	@Test
	public void catchesUpWithAChangeMissedWhileDetached() {
		final Signal<Integer> signal = new Signal<>(0);
		final int[] watches = {0};
		final RectNode child = RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> watches[0]++);
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
		final NodeUI ui = new NodeUI(RectNode.create(0D, 0D, 10D, 10D).watch(signal).onWatch(event -> reloads[0]++));
		this.bridges.open(ui);
		JOID.close(ui);
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
		this.bridges.open(ui);
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		signal.set(1);
		Assert.assertEquals(1, reloads[0]);
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
	}

	@Test
	public void keepsOneBindingPerProperty() {
		final Signal<String> title = new Signal<>("first");
		final Signal<Integer> count = new Signal<>(1);
		final Signal<String> other = new Signal<>("other");
		final List<Object> values = new ArrayList<>();
		final BindingNode node = new BindingNode().link("title", title, values).link("count", count, values);
		this.bridges.open(new NodeUI(node));
		title.set("second");
		count.set(2);
		Assert.assertEquals(Arrays.asList("first", 1, "second", 2), values);
		Assert.assertEquals(2, NodeTest.links(node));
		node.link("title", other, values);
		title.set("third");
		count.set(3);
		Assert.assertEquals(Arrays.asList("first", 1, "second", 2, "other", 3), values);
		Assert.assertEquals(2, NodeTest.links(node));
		Assert.assertTrue(NodeTest.subscribers(title).isEmpty());
		Assert.assertEquals(1, NodeTest.subscribers(other).size());
		Assert.assertEquals(1, NodeTest.subscribers(count).size());
	}

	@Test
	public void rebindsOnceAcrossADetach() {
		final Signal<Integer> first = new Signal<>(0);
		final Signal<Integer> second = new Signal<>(10);
		final List<Object> values = new ArrayList<>();
		final BindingNode node = new BindingNode().link("value", first, values);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent));
		parent.remove(node);
		node.link("value", second, values);
		Assert.assertTrue(NodeTest.subscribers(first).isEmpty());
		Assert.assertTrue(NodeTest.subscribers(second).isEmpty());
		parent.append(node);
		parent.remove(node);
		parent.append(node);
		Assert.assertTrue(NodeTest.subscribers(first).isEmpty());
		Assert.assertEquals(1, NodeTest.subscribers(second).size());
		Assert.assertEquals(1, NodeTest.links(node));
		first.set(1);
		second.set(11);
		Assert.assertEquals(Arrays.asList(0, 10, 11), values);
	}

	@Test
	public void detachesItsScrollbarAndItsSkeleton() {
		final Signal<Integer> signal = new Signal<>(0);
		final Bar bar = new Bar(90D, 0D, 10D, 100D, 20D).watch(signal);
		final RectNode node = RectNode.create(0D, 0D, 100D, 100D).overflow(OverflowProperty.SCROLL).scrollbar(bar).skeleton(rect -> RectNode.create(0D, 0D, 10D, 10D).watch(signal));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 200D, 200D).append(node);
		this.bridges.open(new NodeUI(parent));
		Assert.assertEquals(2, NodeTest.subscribers(signal).size());
		parent.remove(node);
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
		Assert.assertFalse(NodeTest.subscribed(bar));
		Assert.assertFalse(NodeTest.subscribed(node.getSkeleton()));
		parent.append(node);
		parent.remove(node);
		parent.append(node);
		Assert.assertEquals(2, NodeTest.subscribers(signal).size());
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
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).draggable(DraggableProperty.free()).onDragStart(event -> events.add("start")).onDragDrop(event -> events.add("end"));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 400D, 400D).append(node);
		this.bridges.open(new NodeUI(parent));
		node.startDragging(110D, 110D);
		node.fireMouseDragged(160D, 110D, MouseButton.LEFT, 0L);
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
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onHoverEnd(event -> ends[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 400D, 400D).append(node);
		this.bridges.open(new NodeUI(parent));
		NodeTest.hover(node).getHoverAnimator().sequence(0F, 1F).start().update(1F);
		parent.remove(node);
		Assert.assertFalse(node.isHovered());
		Assert.assertEquals(1, ends[0]);
		Assert.assertEquals(0F, node.hoverValue(1F), 0F);
		node.fireDetach();
		Assert.assertEquals(1, ends[0]);
	}

	@Test
	public void mountsAgainOnceAttachedAgain() {
		final int[] mounts = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onMount(event -> mounts[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent)).frame();
		Assert.assertEquals(1, mounts[0]);
		parent.remove(node);
		parent.append(node);
		this.bridges.frame();
		Assert.assertEquals(2, mounts[0]);
	}

	@Test
	public void mountsTheCopyOfAMountedNode() {
		final int[] mounts = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).onMount(event -> mounts[0]++);
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent)).frame();
		parent.append(node.copy());
		this.bridges.frame();
		Assert.assertEquals(2, mounts[0]);
	}

	@Test
	public void waitsForItsConditionsAcrossADetach() {
		final Signal<String> title = new Signal<>();
		final int[] mounts = {0};
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).wait(rect -> title.get() != null).onMount(event -> mounts[0]++);
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
	public void stopsReportingItsAnimatorsWhileDetached() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create();
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate(event -> values.add(event.getValue()));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).append(node);
		this.bridges.open(new NodeUI(parent));
		parent.remove(node);
		new AnimatorUI(animator.sequence(0F, 1F)).load(1920D, 1080D);
		this.bridges.frame();
		Assert.assertTrue(values.isEmpty());
		Assert.assertEquals(1F, animator.get(), 0F);
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
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		kept[0] = false;
		node.append(RectNode.create(0D, 0D, 10D, 10D));
		signal.set(2);
		Assert.assertEquals(1, node.getChildren().size());
		Assert.assertEquals(1, NodeTest.subscribers(signal).size());
		kept[0] = true;
		signal.set(3);
		Assert.assertTrue(node.getChildren().isEmpty());
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
		Assert.assertTrue(error, error.contains("the remaining properties of this watch still apply"));
		Assert.assertTrue(node.getChildren().isEmpty());
	}

	@Test
	public void reportsEveryValueOfItsAnimators() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate(event -> values.add(event.getValue()));
		this.bridges.open(new NodeUI(node)).frames(2);
		Assert.assertTrue(values.isEmpty());
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void stopsReportingARemovedAnimator() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate(event -> values.add(event.getValue()));
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertSame(node, node.removeAnimator(animator));
		Assert.assertTrue(NodeTest.<Map<?, ?>>field(node, "animatorMap").isEmpty());
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(3);
		Assert.assertTrue(values.isEmpty());
		Assert.assertEquals(1F, animator.get(), 0F);
	}

	@Test
	public void removesAnAnimatorFromItsOwnCallback() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).animate(TweenAnimator.create()).onAnimate(event -> {
			values.add(event.getValue());
			event.getNode().removeAnimator(event.getAnimator());
		});
		this.bridges.open(new NodeUI(node)).frame();
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F), values);
		Assert.assertEquals(1, NodeTest.<Map<?, ?>>field(node, "animatorMap").size());
	}

	@Test
	public void reportsTheAnimatorsOfAHiddenNode() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).visible(false).animate(animator).onAnimate(event -> values.add(event.getValue()));
		this.bridges.open(new NodeUI(node)).frame();
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void reportsTheAnimatorsUnderAHiddenParent() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate(event -> values.add(event.getValue()));
		final ContainerNode parent = ContainerNode.create(0D, 0D, 100D, 100D).visible(false).append(ContainerNode.create(0D, 0D, 100D, 100D).append(node));
		this.bridges.open(new NodeUI(parent)).frame();
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(3);
		Assert.assertEquals(Arrays.asList(0.5F, 1F), values);
	}

	@Test
	public void keepsAnimatingWithoutJumpWhileHidden() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(160F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate(event -> values.add(event.getValue()));
		this.bridges.open(new NodeUI(node));
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(2);
		node.visible(false);
		this.bridges.frames(3);
		node.visible(true);
		this.bridges.frames(2);
		Assert.assertEquals(7, values.size());
		for (int i = 0; i < values.size(); i++) {
			Assert.assertEquals((i + 1) * 0.1F, values.get(i), 1E-5F);
		}
	}

	@Test
	public void animatesWithoutBeingAnimatedByANode() {
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).<RectNode>width(() -> 10D + 90D * animator.get());
		this.bridges.open(new NodeUI(node));
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frame();
		Assert.assertEquals(55D, node.getWidth(), 1E-9D);
		this.bridges.frame();
		Assert.assertEquals(100D, node.getWidth(), 1E-9D);
	}

	@Test
	public void recomputesAnAnimatedPropertyOnlyWhenTheAnimatorChanges() {
		final int[] computed = {0};
		final TweenAnimator animator = TweenAnimator.create().sequence(32F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).width(animator.map(value -> {
			computed[0]++;
			return 10D + 90D * value;
		}));
		this.bridges.open(new NodeUI(node)).frames(2);
		final int idle = computed[0];
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frames(2);
		Assert.assertEquals(100D, node.getWidth(), 1E-9D);
		Assert.assertEquals(idle + 2, computed[0]);
		this.bridges.frames(3);
		Assert.assertEquals(idle + 2, computed[0]);
	}

	@Test
	public void reportsTheValueSetOnItsAnimator() {
		final List<Float> values = new ArrayList<>();
		final TweenAnimator animator = TweenAnimator.create().sequence(160F, 1F);
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).animate(animator).onAnimate(event -> values.add(event.getValue()));
		this.bridges.open(new NodeUI(node));
		new AnimatorUI(animator).load(1920D, 1080D);
		this.bridges.frame();
		animator.value(0F);
		this.bridges.frames(2);
		Assert.assertEquals(2, values.size());
		Assert.assertEquals(0.1F, values.get(0), 1E-5F);
		Assert.assertEquals(0F, values.get(1), 0F);
	}

	@Test
	public void describesItselfInJson() {
		final RectNode node = RectNode.create(10D, 20D, 30D, 40D).zindex(2);
		node.x(15D);
		final boolean previous = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		try {
			final JsonObject json = JsonParser.parseString(node.toJson()).getAsJsonObject();
			Assert.assertEquals(2, json.get("index").getAsInt());
			Assert.assertEquals("N/A", json.get("mappedIndex").getAsString());
			Assert.assertEquals("RectNode", json.get("name").getAsString());
			Assert.assertEquals(RectNode.class.getName(), json.get("class").getAsString());
			Assert.assertEquals(15D, json.get("defaultX").getAsDouble(), 0D);
			Assert.assertEquals(20D, json.get("defaultY").getAsDouble(), 0D);
			Assert.assertEquals("15.0 / 15.0", json.getAsJsonObject("bounding").get("x").getAsString());
			Assert.assertEquals("20.0 / 20.0", json.getAsJsonObject("bounding").get("y").getAsString());
			Assert.assertEquals(30D, json.getAsJsonObject("bounding").get("width").getAsDouble(), 0D);
			Assert.assertEquals(40D, json.getAsJsonObject("bounding").get("height").getAsDouble(), 0D);
			Assert.assertFalse(json.has("hierarchy"));
			Assert.assertFalse(node.toString().contains("\n"));
			Assert.assertTrue(node.toString().contains("\"name\":\"RectNode\""));
		} finally {
			JOIDEnv.devMode(previous);
		}
	}

	@Test
	public void describesItsStateInDevMode() {
		final boolean previous = JOIDEnv.isDevMode();
		JOIDEnv.devMode(true);
		try {
			final RectNode child = RectNode.create(0D, 0D, 10D, 10D).overflow(OverflowProperty.SCROLL);
			final RectNode parent = RectNode.create(0D, 0D, 100D, 100D).append(child);
			final JsonObject json = JsonParser.parseString(child.toJson()).getAsJsonObject();
			Assert.assertEquals("0.0 / 0.0 [SCROLL]", json.get("scrollX").getAsString());
			Assert.assertEquals("0.0 / 0.0 [SCROLL]", json.get("scrollY").getAsString());
			Assert.assertTrue(json.get("visible").getAsBoolean());
			Assert.assertTrue(json.get("enabled").getAsBoolean());
			Assert.assertFalse(json.get("hovered").getAsBoolean());
			Assert.assertTrue(json.get("isChild").getAsBoolean());
			Assert.assertEquals(0, json.get("children").getAsInt());
			Assert.assertEquals("RectNode - RectNode (this)", json.get("hierarchy").getAsString());
			Assert.assertFalse(JsonParser.parseString(parent.toJson()).getAsJsonObject().get("isChild").getAsBoolean());
			Assert.assertEquals(1, JsonParser.parseString(parent.toJson()).getAsJsonObject().get("children").getAsInt());
			Assert.assertTrue(child.toString().contains("\n"));
		} finally {
			JOIDEnv.devMode(previous);
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
		final IHoverElement silent = (rect, mouseX, mouseY) -> {};
		final RectNode child = RectNode.create(50D, 50D, 50D, 50D).hover(silent);
		final HoverUI ui = new HoverUI(RectNode.create(100D, 100D, 200D, 200D).hover(() -> "Parent").append(child));
		this.bridges.open(ui);
		this.bridges.move(170D, 170D).frame();
		Assert.assertTrue(ui.tooltips.isEmpty());
		Assert.assertSame(child, child.removeHover(silent));
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(Arrays.asList("Parent")), ui.tooltips);
		Assert.assertTrue(NodeTest.<List<?>>field(child, "hoverElementList").isEmpty());
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
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).onClick(event -> clicks.add("original"));
		final RectNode copy = node.copy();
		copy.onClick(event -> clicks.add("copy"));
		this.bridges.open(new NodeUI(node));
		this.bridges.move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("original"), clicks);
	}

	@Test
	public void copiesTheEffectsOfTheNode() {
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D).effect(RoundedNodeEffect.create(6F));
		Assert.assertEquals(1, NodeTest.<Map<?, ?>>field(node.copy(), "effectMap").size());
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
		Assert.assertEquals(Arrays.asList(child), parent.getChildren(RectNode.class));
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
		Assert.assertTrue(NodeTest.subscribers(signal).isEmpty());
	}

	@Test
	public void startsWithoutListener() {
		Assert.assertTrue(RectNode.create(0D, 0D, 10D, 10D).getEventBus().isEmpty());
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
		Assert.assertEquals(Arrays.asList(other, raised), ui.getNodeList());
		Assert.assertTrue(this.order(0.6F, 0.4F, 0.2F) < this.order(0.2F, 0.4F, 0.6F));
	}

	@Test
	public void hidesItsSourcesAndLinks() {
		for (final Method method : Node.class.getMethods()) {
			Assert.assertFalse(method.getName(), Arrays.asList("getBindings", "getSourceMap", "getLinkList", "getRequirementList", "isFollowed").contains(method.getName()));
		}
	}

	@Test
	public void keepsDrawingWhenItsVisibilityFails() {
		NodeTest.forgetWarnings();
		final boolean[] failing = {false};
		final RectNode node = RectNode.create(100D, 100D, 50D, 50D).visible(() -> {
			if (failing[0]) {
				throw new IllegalStateException("The visibility of this node fails on purpose");
			}
			return true;
		});
		this.bridges.open(new NodeUI(node)).frame();
		failing[0] = true;
		final String errors = NodeTest.capture(() -> this.bridges.frames(3));
		Assert.assertEquals(1, errors.split("The visible of RectNode cannot take its new value", -1).length - 1);
		Assert.assertTrue(node.isVisible());
	}

	@Test
	public void announcesTheEndOnceForNotchesThatOutrunTheGrowthOfItsList() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollToEnd(event -> {
			endings.add(event.getNode().getTargetScrollX());
			RectNode.create(700D, 0D, 700D, 100D).attach(event.getNode());
		});
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
		row.scrollX(-330D);
		this.bridges.frames(2);
		Assert.assertEquals(Arrays.asList(-300D), endings);
	}

	@Test
	public void announcesTheEndOnceWhenItSticksToTheBottomOfAGrowingList() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode column = NodeTest.column().onScrollToEnd(event -> endings.add(event.getNode().getTargetScrollY()));
		this.bridges.open(new NodeUI(column));
		RectNode.create(0D, 300D, 400D, 100D).attach(column);
		column.scrollY(-1E9D);
		this.bridges.frames(2);
		Assert.assertEquals(Arrays.asList(-300D), endings);
	}

	@Test
	public void announcesTheEndOfAListThatGrowsUpToTheOffsetItAimsAt() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode column = NodeTest.column().onScrollToEnd(event -> endings.add(event.getNode().getTargetScrollY()));
		this.bridges.open(new NodeUI(column));
		RectNode.create(0D, 300D, 400D, 100D).attach(column);
		column.scrollY(-300D);
		this.bridges.frames(2);
		Assert.assertEquals(Arrays.asList(-300D), endings);
	}

	@Test
	public void announcesTheEndOnceWhenItsListenerAddsAChildThatKeepsItsContentSize() {
		final List<Double> endings = new ArrayList<>();
		final ContainerNode row = NodeTest.row().onScrollToEnd(event -> {
			endings.add(event.getNode().getTargetScrollX());
			RectNode.create(0D, 0D, 10D, 10D).attach(event.getNode());
		});
		this.bridges.open(new NodeUI(row));
		row.scrollX(-1000D);
		this.bridges.frames(200);
		Assert.assertEquals(Arrays.asList(-300D), endings);
		Assert.assertEquals(3, row.getChildren().size());
	}

	@Test
	public void leavesItsParentWhenAttachedToTheRootOfItsUi() {
		final List<String> events = new ArrayList<>();
		final RecordingNode card = new RecordingNode("card", events, 10D, 10D, 50D, 50D);
		final ContainerNode panel = ContainerNode.create(100D, 100D, 200D, 200D).append(card);
		final NodeUI ui = new NodeUI(panel);
		this.bridges.open(ui);
		events.clear();
		card.attach(ui);
		card.attach(ui);
		Assert.assertNull(card.getParent());
		Assert.assertTrue(panel.getChildren().isEmpty());
		Assert.assertEquals(Arrays.asList(panel, card), ui.getNodeList());
		Assert.assertEquals(Arrays.asList("card detach", "card init"), events);
	}

	@Test
	public void leavesANodeThatWasNeverAttachedAlone() {
		final List<String> events = new ArrayList<>();
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D).onDetach(event -> events.add("onDetach"));
		ContainerNode.create(0D, 0D, 100D, 100D).append(child).remove(child);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void detachesTheChildrenOfARemovedNodeOnce() {
		final List<String> events = new ArrayList<>();
		final RecordingNode child = new RecordingNode("child", events, 0D, 0D, 10D, 10D).onDetach(event -> events.add("onDetach"));
		final ContainerNode panel = ContainerNode.create(0D, 0D, 100D, 100D).append(child);
		final NodeUI ui = new NodeUI(panel);
		this.bridges.open(ui);
		events.clear();
		ui.remove(panel);
		panel.clearChildren();
		Assert.assertEquals(Arrays.asList("child detach", "onDetach"), events);
	}

	@Test
	public void copiesTheScrollOfItsContent() {
		final ContainerNode row = NodeTest.row();
		this.bridges.open(new NodeUI(row));
		row.scrollX(-150D).updateScroll();
		final ContainerNode copy = row.copy();
		Assert.assertEquals(-150D, copy.getTargetScrollX(), 0D);
		Assert.assertEquals(-150D, copy.getScrollX(), 0D);
	}

	@Test
	public void pointsTheFieldsOfACopyAtTheNodesOfItsOwnTree() {
		final BarredNode copy = new BarredNode().copy();
		Assert.assertSame(copy.getScrollbar(), copy.bar);
		Assert.assertSame(copy.getSkeleton(), copy.placeholder);
		Assert.assertSame(copy.getChildren().get(0), copy.title);
		Assert.assertSame(copy.getChildren().get(1).getChildren().get(0), copy.caption);
		Assert.assertSame(copy, ((OwnedNode) copy.getChildren().get(2)).owner);
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
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
	}

	private void drop(final double x, final double y) {
		this.bridges.move(x, y).frame();
		this.bridges.getUi().mouseMoved();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
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

	private static void forgetWarnings() {
		try {
			final Field field = JOIDLog.class.getDeclaredField("KEY_SET");
			field.setAccessible(true);
			((Set<?>) field.get(null)).clear();
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static int links(final Node node) {
		final Object bindings = NodeTest.field(node, "bindings");
		return NodeTest.<Map<?, ?>>field(bindings, "bindingMap").size() + NodeTest.<List<?>>field(bindings, "watchList").size();
	}

	private static boolean subscribed(final Node node) {
		return NodeTest.<Boolean>field(NodeTest.field(node, "bindings"), "subscribed");
	}

	private static <N extends Node> N assign(final N node, final String name, final Object value) {
		try {
			final Field field = Node.class.getDeclaredField(name);
			field.setAccessible(true);
			field.set(node, value);
			return node;
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static <T> T field(final Object target, final String name) {
		try {
			for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
				for (final Field field : type.getDeclaredFields()) {
					if (field.getName().equals(name)) {
						field.setAccessible(true);
						return (T) field.get(target);
					}
				}
			}
			throw new AssertionError("No field " + name + " in " + target.getClass().getSimpleName());
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static <T> Set<SignalListener<T>> subscribers(final Signal<T> signal) {
		try {
			final Field field = Signal.class.getDeclaredField("listenerSet");
			field.setAccessible(true);
			return (Set<SignalListener<T>>) field.get(signal);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static <N extends Node> N hover(final N node) {
		try {
			final Field field = Node.class.getDeclaredField("hovered");
			field.setAccessible(true);
			field.set(node, true);
			return node;
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
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
		private final List<String>       positions = new ArrayList<>();
		private final List<List<String>> tooltips  = new ArrayList<>();

		@Override
		public void init() {
			super.add(this.node);
		}

		@Override
		public void drawHover(final @NonNull Object content, final double mouseX, final double mouseY) {
			this.tooltips.add(TextConverter.convertLines(content));
			this.positions.add(mouseX + " " + mouseY);
		}

	}

	@AllArgsConstructor
	@UIDataDebug(profiler = false, hotreload = false)
	public static final class AnimatorUI extends UI {

		private final TweenAnimator animator;

		@Override
		public void init() {
			this.animator.start();
		}

	}

	public static final class CurrentUI extends UI {

		private UI      found;
		private boolean bound;

		@Override
		public void init() {
			final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
			this.found = node.getUi();
			this.bound = node.isAttached();
		}

	}

	public static final class WatchingUI extends UI {

		private final Signal<Integer> signal = new Signal<>(0);

		private int watches;

		@Override
		public void init() {
			RectNode.create(0D, 0D, 10D, 10D).watch(this.signal).onWatch(event -> this.watches++).attach(this);
			this.signal.set(1);
		}

	}

	@RequiredArgsConstructor
	public static final class ReloadedUI extends UI {

		private final Signal<Integer> signal;

		private int watches;

		@Override
		public void init() {
			RectNode.create(0D, 0D, 10D, 10D).watch(this.signal).onWatch(event -> this.watches++).attach(this);
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

		public <V> BindingNode link(final String property, final Signal<V> signal, final List<Object> values) {
			return super.bind(property, signal, value -> values.add(value));
		}

	}

	public static final class DefaultedNode extends Node {

		private double base;

		public DefaultedNode() {
			super(0D, 0D, 10D, 10D);
			super.width(() -> this.base * 2D).layer((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRect(0D, 0D, 1D, 1D, new Color(0.3F, 0.5F, 0.7F, 1F)));
		}

	}

	public static final class FailingNode extends Node {

		public FailingNode() {
			super(0D, 0D, 10D, 10D);
		}

		@Override
		public void update() {
			throw new IllegalStateException("The update of this node fails on purpose");
		}

		@Override
		public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
			throw new IllegalStateException("The press on this node fails on purpose");
		}

	}

	public static final class BrokenNode extends Node {

		public BrokenNode() {
			super(0D, 0D, 10D, 10D);
		}

		@Override
		public void init(final @NonNull UI ui) {
			throw new IllegalStateException("The init of this node fails on purpose");
		}

	}

	public static final class SloppyNode extends Node {

		public SloppyNode() {
			super(0D, 0D, 10D, 10D);
		}

		@Override
		public void detach() {
			throw new IllegalStateException("The detach of this node fails on purpose");
		}

	}

	public static final class EmptyNode extends Node {

		public EmptyNode() {
			super(0D, 0D, 0D, 0D);
		}

	}

	public static final class FragileNode extends Node {

		public FragileNode() {
			this(0D, 0D, 10D, 10D);
		}

		public FragileNode(final double x, final double y, final double width, final double height) {
			super(x, y, width, height);
			if (width < 0D) {
				throw new IllegalArgumentException("A fragile node needs a positive width");
			}
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
		public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
			this.events.add(this.name + " pressed");
		}

		@Override
		public void mouseDragged(final @NonNull NodeMouseEvent<?> event) {
			this.events.add(this.name + " dragged");
		}

		@Override
		public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
			this.events.add(this.name + " released");
		}

		@Override
		public void mouseScroll(final @NonNull NodeMouseScrollEvent<?> event) {
			this.events.add(this.name + " scrolled");
		}

		@Override
		public void keyPressed(final @NonNull NodeKeyEvent<?> event) {
			this.events.add(this.name + " key");
		}

		@Override
		public void charTyped(final @NonNull NodeCharEvent<?> event) {
			this.events.add(this.name + " char");
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
		public void draw(final @NonNull Node node, final @NonNull Runnable body) {
			this.events.add(this.name + " open");
			body.run();
			this.events.add(this.name + " close");
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
		public void draw(final @NonNull RectNode node, final @NonNull Runnable body) {
			this.events.add(node.getColor().toString());
			body.run();
		}

	}

	@Getter
	public static class GlowBorderEffect extends BorderNodeEffect {

		private float glow;

		private GlowBorderEffect() {
			super(() -> Color.BLACK, () -> 2F);
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

		private final double length;

		public Bar(final double x, final double y, final double width, final double height, final double length) {
			super(x, y, width, height);
			this.length = length;
			super.thumb(RectNode.create(0D, 0D, width > height ? length : 0D, width > height ? 0D : length).color(new Color(0.6F, 0.4F, 0.2F, 1F)));
		}

		@Override
		protected @NonNull Node instantiate() {
			return new Bar(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.length);
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

	public static final class BarredNode extends Node {

		private final RectNode title;

		private Bar      bar;
		private Node     placeholder;
		private RectNode caption;

		public BarredNode() {
			super(100D, 100D, 400D, 100D);
			this.bar = new Bar(400D, 0D, 10D, 100D, 20D);
			this.title = RectNode.create(0D, 0D, 400D, 20D).attach(this);
			this.caption = RectNode.create(0D, 0D, 400D, 20D).attach(RectNode.create(0D, 20D, 400D, 80D).attach(this));
			new OwnedNode().owner(this).attach(this);
			super.scrollbar(this.bar).skeleton(node -> this.placeholder = RectNode.create(0D, 0D, 400D, 100D));
		}

	}

	public static final class OwnedNode extends Node {

		private Node owner;

		public OwnedNode() {
			super(0D, 0D, 10D, 10D);
		}

		public @NonNull OwnedNode owner(final @NonNull Node owner) {
			this.owner = owner;
			return this;
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