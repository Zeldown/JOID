package dev.joid.lib.bridge.ui;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.textfield.node.DemoIntegerFieldNode;
import dev.joid.demo.ui.textfield.node.DemoMultilineTextFieldNode;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlay;
import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRender;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class UIBridgeCursorTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void leavesTheCursorAloneWhileNoNodeIsHovered() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER))).move(1000D, 1000D).frames(3);
		Assert.assertEquals(Collections.emptyList(), this.bridges.getWindow().getCursors());
	}

	@Test
	public void showsTheCursorOfTheHoveredNode() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER))).move(150D, 150D).frame();
		Assert.assertEquals(Collections.singletonList(Cursor.POINTER), this.bridges.getWindow().getCursors());
	}

	@Test
	public void setsTheCursorOnlyWhenItChanges() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER), RectNode.create(300, 100, 200, 200).cursor(Cursor.POINTER)));
		this.bridges.move(150D, 150D).frames(3).move(350D, 150D).frames(3).move(1000D, 1000D).frames(3);
		Assert.assertEquals(Arrays.asList(Cursor.POINTER, Cursor.DEFAULT), this.bridges.getWindow().getCursors());
	}

	@Test
	public void inheritsTheCursorOfItsParent() {
		final RectNode parent = RectNode.create(100, 100, 400, 400).cursor(Cursor.CROSSHAIR);
		RectNode.create(50, 50, 100, 100).attach(parent);
		RectNode.create(250, 50, 100, 100).cursor(Cursor.MOVE).attach(parent);
		this.bridges.open(new NodeUI(parent)).move(200D, 200D).frame();
		Assert.assertEquals(Cursor.CROSSHAIR, this.bridges.getWindow().getCursor());
		this.bridges.move(400D, 200D).frame();
		Assert.assertEquals(Cursor.MOVE, this.bridges.getWindow().getCursor());
		this.bridges.move(300D, 450D).frame();
		Assert.assertEquals(Cursor.CROSSHAIR, this.bridges.getWindow().getCursor());
	}

	@Test
	public void showsTheDefaultCursorWhenNoAncestorSetsOne() {
		final RectNode parent = RectNode.create(100, 100, 400, 400);
		RectNode.create(50, 50, 100, 100).cursor(Cursor.TEXT).attach(parent);
		this.bridges.open(new NodeUI(parent)).move(200D, 200D).frame().move(400D, 400D).frame();
		Assert.assertEquals(Arrays.asList(Cursor.TEXT, Cursor.DEFAULT), this.bridges.getWindow().getCursors());
	}

	@Test
	public void takesTheCursorOfTheTopmostNode() {
		final RectNode below = RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER);
		final RectNode above = RectNode.create(200, 100, 200, 200).cursor(Cursor.TEXT);
		this.bridges.open(new NodeUI(below, above)).move(250D, 150D).frame();
		Assert.assertEquals(Cursor.TEXT, this.bridges.getWindow().getCursor());
		above.zindex(-1);
		this.bridges.frame();
		Assert.assertEquals(Cursor.POINTER, this.bridges.getWindow().getCursor());
	}

	@Test
	public void keepsTheCursorOfThePressedNodeDuringADrag() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.MOVE), RectNode.create(400, 100, 200, 200).cursor(Cursor.TEXT)));
		this.bridges.move(150D, 150D).frame();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.move(450D, 150D).frames(2).move(1000D, 1000D).frames(2);
		Assert.assertEquals(Collections.singletonList(Cursor.MOVE), this.bridges.getWindow().getCursors());
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frame().move(450D, 150D).frame();
		Assert.assertEquals(Arrays.asList(Cursor.MOVE, Cursor.DEFAULT, Cursor.TEXT), this.bridges.getWindow().getCursors());
	}

	@Test
	public void followsAReactiveCursor() {
		final Signal<Boolean> busy = Signal.of(false);
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(() -> busy.get() ? Cursor.NOT_ALLOWED : Cursor.POINTER))).move(150D, 150D).frame();
		busy.set(true);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(Cursor.POINTER, Cursor.NOT_ALLOWED), this.bridges.getWindow().getCursors());
	}

	@Test
	public void inheritsWhenAReactiveCursorGivesNull() {
		final Signal<Boolean> own = Signal.of(true);
		final RectNode parent = RectNode.create(100, 100, 400, 400).cursor(Cursor.CROSSHAIR);
		RectNode.create(50, 50, 100, 100).cursor(() -> own.get() ? Cursor.RESIZE_EW : null).attach(parent);
		this.bridges.open(new NodeUI(parent)).move(200D, 200D).frame();
		own.set(false);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(Cursor.RESIZE_EW, Cursor.CROSSHAIR), this.bridges.getWindow().getCursors());
	}

	@Test
	public void keepsTheCursorOfADisabledNode() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.NOT_ALLOWED).enabled(false))).move(150D, 150D).frame();
		Assert.assertEquals(Cursor.NOT_ALLOWED, this.bridges.getWindow().getCursor());
	}

	@Test
	public void ignoresAHiddenNode() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER).visible(false))).move(150D, 150D).frame();
		Assert.assertEquals(Collections.emptyList(), this.bridges.getWindow().getCursors());
	}

	@Test
	public void givesTheTextCursorToTextFieldsOnly() {
		Assert.assertEquals(Cursor.TEXT, DemoTextFieldNode.create(0, 0, 100).getCursor());
		Assert.assertEquals(Cursor.TEXT, DemoIntegerFieldNode.create(0, 0, 100).getCursor());
		Assert.assertEquals(Cursor.TEXT, DemoMultilineTextFieldNode.create(0, 0, 100, 100).getCursor());
		Assert.assertNull(RectNode.create(0, 0, 100, 100).getCursor());
	}

	@Test
	public void showsTheCursorOfAPopupAboveTheScreen() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER))).move(150D, 150D).frame();
		this.bridges.open(new PopupUI(RectNode.create(500, 500, 200, 200).cursor(Cursor.TEXT))).frame();
		Assert.assertEquals(Cursor.DEFAULT, this.bridges.getWindow().getCursor());
		this.bridges.move(550D, 550D).frame();
		Assert.assertEquals(Cursor.TEXT, this.bridges.getWindow().getCursor());
	}

	@Test
	public void showsTheCursorOfAnInteractiveOverlayAboveTheScreen() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER)));
		this.bridges.open(new PassiveOverlayUI(RectNode.create(100, 100, 100, 100).cursor(Cursor.TEXT))).move(150D, 150D).frame();
		Assert.assertEquals(Cursor.POINTER, this.bridges.getWindow().getCursor());
		this.bridges.open(new OverlayUI(RectNode.create(100, 100, 100, 100).cursor(Cursor.CROSSHAIR))).frame();
		Assert.assertEquals(Cursor.CROSSHAIR, this.bridges.getWindow().getCursor());
	}

	@Test
	public void leavesTheCursorAloneWhileTheMouseIsGrabbed() {
		this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER)));
		this.bridges.getWindow().setMouseGrabbed(true);
		this.bridges.move(150D, 150D).frames(2);
		Assert.assertEquals(Collections.emptyList(), this.bridges.getWindow().getCursors());
		this.bridges.getWindow().setMouseGrabbed(false);
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList(Cursor.POINTER), this.bridges.getWindow().getCursors());
	}

	@Test
	public void showsTheDefaultCursorOnceTheUiCloses() {
		final NodeUI ui = new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER));
		this.bridges.open(ui).move(150D, 150D).frame();
		this.bridges.getUi().close(ui);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(Cursor.POINTER, Cursor.DEFAULT), this.bridges.getWindow().getCursors());
	}

	@Test
	public void ignoresTheCursorOfABridgeWithoutUi() {
		final DemoUIBridge other = new DemoUIBridge();
		final NodeUI ui = new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.POINTER));
		BridgeHandler.UI.register(other);
		try {
			this.bridges.open(new NodeUI(RectNode.create(100, 100, 200, 200).cursor(Cursor.TEXT))).move(150D, 150D);
			other.add(ui);
			other.draw();
			other.close(ui);
			this.bridges.frame();
			Assert.assertEquals(Arrays.asList(Cursor.POINTER, Cursor.TEXT), this.bridges.getWindow().getCursors());
		} finally {
			BridgeHandler.UI.unregister(other);
		}
	}

	public static class NodeUI extends UI {

		private final Node[] nodes;

		public NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class PopupUI extends NodeUI {

		public PopupUI(final Node... nodes) {
			super(nodes);
		}

	}

	@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true), render = @UIDataOverlayRender(screens = true))
	public static final class OverlayUI extends NodeUI {

		public OverlayUI(final Node... nodes) {
			super(nodes);
		}

	}

	@UIDataOverlay(active = true, render = @UIDataOverlayRender(screens = true))
	public static final class PassiveOverlayUI extends NodeUI {

		public PassiveOverlayUI(final Node... nodes) {
			super(nodes);
		}

	}

}