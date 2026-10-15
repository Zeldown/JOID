package dev.joid.lib.bridge.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.ui.UIBridgeCursorTest.NodeUI;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.render.tessellator.TessellatorAccess;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlay;
import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRender;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.shortcut.Shortcut;

import lombok.NonNull;

public class UIBridgeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace = new ArrayList<>();

	@Test(expected = UnsupportedOperationException.class)
	public void exposesAReadOnlyUiList() {
		this.bridges.getUi().getUiList().clear();
	}

	@Test
	public void listsTheUisItOpened() {
		final TraceUI ui = new TraceUI("menu", this.trace);
		Assert.assertFalse(this.bridges.getUi().isOpen(ui));
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
		Assert.assertEquals(Collections.singletonList(ui), this.bridges.getUi().getUiList());
	}

	@Test
	public void keepsEveryUiAtFullSizeByDefault() {
		final TraceUI ui = new TraceUI("menu", this.trace);
		Assert.assertEquals(1D, this.bridges.getUi().getInterfaceScale(ui), 0D);
	}

	@Test
	public void resizesEveryUiToTheWindow() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud).resize(1366, 768);
		Assert.assertEquals(1366D, menu.getWindowWidth(), 0D);
		Assert.assertEquals(768D, menu.getWindowHeight(), 0D);
		Assert.assertEquals(1366D, hud.getWindowWidth(), 0D);
		Assert.assertEquals(768D, hud.getWindowHeight(), 0D);
		Assert.assertEquals(1, menu.inits);
	}

	@Test
	public void keepsTheZoomOfEveryUiOnAResize() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		menu.zoom(0.5D);
		this.bridges.resize(1366, 768);
		Assert.assertEquals(0.5D, menu.getView().getZoom(), 0D);
		Assert.assertEquals(0.5D, menu.getZoom(), 0D);
		Assert.assertEquals(1366D, menu.getWindowWidth(), 0D);
		Assert.assertEquals(1, menu.inits);
	}

	@Test
	public void updatesEveryUi() {
		this.bridges.open(new TraceUI("menu", this.trace)).open(new TraceUI("hud", this.trace));
		this.trace.clear();
		this.bridges.getUi().update();
		Assert.assertEquals(Arrays.asList("update menu", "update hud"), this.trace);
	}

	@Test
	public void pressesTheTopUiFirst() {
		this.bridges.open(new TraceUI("menu", this.trace)).open(new TraceUI("hud", this.trace));
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("pressed hud LEFT", "pressed menu LEFT"), this.trace);
	}

	@Test
	public void stopsAPressOnceAUiCancelsIt() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		Assert.assertEquals(Collections.singletonList("pressed hud RIGHT"), this.trace);
	}

	@Test
	public void skipsTheInactiveAndHiddenUisOnAPress() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().active(false);
		hidden.getData().visible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Collections.singletonList("pressed menu LEFT"), this.trace);
	}

	@Test
	public void keepsThePressInsideAPopup() {
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace));
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Collections.singletonList("pressed popup LEFT"), this.trace);
	}

	@Test
	public void dragsTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.trace.clear();
		this.bridges.getClock().advance(40L);
		this.bridges.getUi().mouseMoved();
		hud.cancel = true;
		this.bridges.getClock().advance(40L);
		this.bridges.getUi().mouseMoved();
		Assert.assertEquals(Arrays.asList("dragged hud LEFT 40", "dragged menu LEFT 40", "dragged hud LEFT 80"), this.trace);
	}

	@Test
	public void keepsTheDragInsideAPopup() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().active(false);
		hidden.getData().visible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.trace.clear();
		this.bridges.getClock().advance(40L);
		this.bridges.getUi().mouseMoved();
		Assert.assertEquals(Collections.singletonList("dragged popup LEFT 40"), this.trace);
	}

	@Test
	public void releasesTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().mouseReleased(MouseButton.MIDDLE);
		hud.cancel = true;
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("released hud MIDDLE", "released menu MIDDLE", "released hud LEFT"), this.trace);
	}

	@Test
	public void keepsTheReleaseInsideAPopup() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().active(false);
		hidden.getData().visible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Collections.singletonList("released popup LEFT"), this.trace);
	}

	@Test
	public void scrollsTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.scroll(1D);
		hud.cancel = true;
		this.bridges.scroll(-1D);
		Assert.assertEquals(Arrays.asList("scrolled hud 1.0", "scrolled menu 1.0", "scrolled hud -1.0"), this.trace);
	}

	@Test
	public void ignoresAnEmptyScroll() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.scroll(0D);
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void keepsTheScrollInsideAPopup() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().active(false);
		hidden.getData().visible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.scroll(1D);
		Assert.assertEquals(Collections.singletonList("scrolled popup 1.0"), this.trace);
	}

	@Test
	public void ignoresAStillScrollOnBothAxes() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		menu.cancel = true;
		this.bridges.open(menu);
		this.trace.clear();
		Assert.assertFalse(this.bridges.getUi().mouseScroll(0D, 0D));
		Assert.assertTrue(this.trace.isEmpty());
		Assert.assertTrue(this.bridges.getUi().mouseScroll(1D, 0D));
	}

	@Test
	public void typesInTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().charTyped('a');
		hud.cancel = true;
		this.bridges.getUi().keyPressed(Key.B);
		this.bridges.getUi().charTyped('b');
		Assert.assertEquals(Arrays.asList("key hud A", "key menu A", "char hud a", "char menu a", "key hud B", "char hud b"), this.trace);
	}

	@Test
	public void typesNoControlCharacter() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		menu.cancel = true;
		this.bridges.open(menu);
		this.trace.clear();
		Assert.assertFalse(this.bridges.getUi().charTyped('\u0003'));
		Assert.assertFalse(this.bridges.getUi().charTyped('\r'));
		Assert.assertFalse(this.bridges.getUi().charTyped('\b'));
		Assert.assertFalse(this.bridges.getUi().charTyped('\t'));
		Assert.assertFalse(this.bridges.getUi().charTyped('\u007F'));
		Assert.assertFalse(this.bridges.getUi().charTyped(0));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void typesEachCharacterInOneCall() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.getUi().charTyped('é');
		this.bridges.getUi().charTyped(' ');
		this.bridges.getUi().charTyped(0x1F600);
		Assert.assertEquals(Arrays.asList("char menu é", "char menu  ", "char menu \uD83D\uDE00"), this.trace);
	}

	@Test
	public void consumesTheKeyAndTheCharacterApart() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.trace.clear();
		Shortcut.create(Key.A).onTrigger(event -> this.trace.add("shortcut")).attach(menu);
		this.bridges.getWindow().getKeys().add(Key.A);
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertFalse(this.bridges.getUi().charTyped('a'));
		Assert.assertEquals(Arrays.asList("shortcut", "key menu A", "char menu a"), this.trace);
	}

	@Test
	public void skipsTheInactiveAndHiddenUisOnAKey() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().active(false);
		hidden.getData().visible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().charTyped('a');
		Assert.assertEquals(Arrays.asList("key popup A", "char popup a"), this.trace);
	}

	@Test
	public void closesTheTopUiOnEscape() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyPressed(Key.ESCAPE);
		Assert.assertEquals(Arrays.asList("key hud ESCAPE", "close hud"), this.trace);
		Assert.assertEquals(Collections.singletonList(menu), this.bridges.getUi().getUiList());
	}

	@Test
	public void keepsTheTopUiOpenWhenItConsumesEscape() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.cancel = true;
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyPressed(Key.ESCAPE);
		Assert.assertEquals(Collections.singletonList("key hud ESCAPE"), this.trace);
		Assert.assertTrue(this.bridges.getUi().isOpen(hud));
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
	}

	@Test
	public void typesEscapeInAUiThatCannotBeClosed() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.getData().closeable(false);
		this.bridges.open(hud);
		this.trace.clear();
		this.bridges.getUi().keyPressed(Key.ESCAPE);
		Assert.assertEquals(Collections.singletonList("key hud ESCAPE"), this.trace);
		Assert.assertTrue(this.bridges.getUi().isOpen(hud));
	}

	@Test
	public void consumesEscapeInAUiThatRefusesToClose() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.closeable = false;
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyPressed(Key.ESCAPE);
		Assert.assertEquals(Arrays.asList("key hud ESCAPE", "close hud"), this.trace);
		Assert.assertTrue(this.bridges.getUi().isOpen(hud));
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
	}

	@Test
	public void drawsTheVisibleUisInTheirOpeningOrder() {
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		hidden.getData().visible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hidden).open(new TraceUI("hud", this.trace));
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList("draw menu -2000.0", "draw hud -1990.0"), this.trace);
	}

	@Test
	public void stacksEachUiAboveTheDepthOfThePreviousOne() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		menu.level = 40D;
		hud.getData().zlevel(5D);
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList("draw menu -2000.0", "draw hud -1945.0"), this.trace);
		Assert.assertEquals(0F, UIBridgeTest.depth(), 0F);
	}

	@Test
	public void drawsNothingWithoutUi() {
		this.bridges.draw();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void survivesAUiThatFailsToDraw() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		menu.failure = new IllegalStateException("broken");
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			this.bridges.draw();
		} finally {
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains("broken"));
		Assert.assertEquals(0F, UIBridgeTest.depth(), 0F);
	}

	@Test
	public void givesBackTheStacksAndTheTessellatorOfAFailedDraw() {
		final RenderBridge render = this.bridges.getRender();
		final PrintStream previous = System.err;
		System.setErr(new PrintStream(new ByteArrayOutputStream(), true));
		try {
			this.bridges.open(new NodeUI(new DrawingNode(() -> {
				render.pushState();
				render.getModelView().push();
				render.getProjection().push();
				Tessellator.inst().start(DrawMode.QUADS);
				throw new IllegalStateException("half drawn");
			})));
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals(0, render.getStateDepth());
		Assert.assertEquals(0, render.getModelView().getDepth());
		Assert.assertEquals(0, render.getProjection().getDepth());
		Assert.assertFalse(TessellatorAccess.isDrawing(Tessellator.inst()));
	}

	@Test
	public void timesItsDrawsWithTheClockBridge() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D);
		final NodeUI ui = new NodeUI(node);
		this.bridges.open(ui).frame();
		Assert.assertEquals(0L, ui.getRenderTime());
		Assert.assertEquals(0L, node.getRenderTime());
	}

	@Test
	public void refusesToDrawOutsideAFrame() {
		try {
			this.bridges.getUi().draw();
			Assert.fail("A draw outside a frame must be refused");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("DemoUIBridge.draw() needs an open frame and the render bridge has none: call render.beginFrame() before and render.endFrame() after, or use frame()", expected.getMessage());
		}
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutButton() {
		this.bridges.getUi().mousePressed(null);
	}

	@Test
	public void dragsNothingWithoutAPress() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.getUi().mouseMoved();
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void stopsDraggingWhenThePressedButtonIsReleased() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.trace.clear();
		this.bridges.getUi().mouseMoved();
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void keepsDraggingWhenAnotherButtonIsReleased() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		this.trace.clear();
		this.bridges.getUi().mouseMoved();
		Assert.assertEquals(Collections.singletonList("dragged menu LEFT 0"), this.trace);
	}

	@Test
	public void timesTheDragFromTheLastPressOnTheClock() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getClock().advance(25L);
		this.bridges.getUi().mousePressed(MouseButton.RIGHT);
		this.trace.clear();
		this.bridges.getClock().advance(5L);
		this.bridges.getUi().mouseMoved();
		Assert.assertEquals(Collections.singletonList("dragged menu RIGHT 5"), this.trace);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAReleaseWithoutButton() {
		this.bridges.getUi().mouseReleased(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyPressWithoutKey() {
		this.bridges.getUi().keyPressed(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToLookForAMissingUi() {
		this.bridges.getUi().isOpen(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToScaleAMissingUi() {
		this.bridges.getUi().getInterfaceScale(null);
	}

	@Test
	public void keepsUpdatingAndDrawingAnInactiveUi() {
		final DepthUI menu = new DepthUI("menu", this.trace);
		this.bridges.open(menu);
		menu.getData().active(false);
		this.trace.clear();
		this.bridges.frame();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("update menu", "draw menu"), this.trace);
	}

	@Test
	public void letsTheMouseThroughAnInactivePopup() {
		final PopupUI popup = new PopupUI("popup", this.trace);
		this.bridges.open(new DepthUI("menu", this.trace)).open(popup);
		popup.getData().active(false);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Collections.singletonList("pressed menu"), this.trace);
	}

	@Test
	public void keepsDrawingTheUisAboveAUiClosingBehindThem() {
		final DepthUI menu = new DepthUI("menu", this.trace);
		menu.transition(PopTransition.create());
		this.bridges.open(menu).open(new PopupUI("popup", this.trace)).frames(20);
		JOID.close(menu);
		final PrintStream previous = System.err;
		System.setErr(new PrintStream(new ByteArrayOutputStream(), true));
		try {
			for (int frame = 0; frame < 20; frame++) {
				this.trace.clear();
				this.bridges.frame();
				Assert.assertTrue(this.trace.toString(), this.trace.stream().anyMatch(line -> line.startsWith("draw popup")));
			}
		} finally {
			System.setErr(previous);
		}
		Assert.assertFalse(this.bridges.getUi().isOpen(menu));
	}

	@Test
	public void drawsTheUiOfHigherZindexOnTop() {
		final DepthUI menu = new DepthUI("menu", this.trace);
		final DepthUI hud = new DepthUI("hud", this.trace);
		menu.getData().zindex(10);
		this.bridges.open(menu).open(hud);
		Assert.assertTrue(menu.depth + " behind " + hud.depth, menu.depth > hud.depth);
	}

	@Test
	public void sortsAUiAgainOnceItsZindexChanges() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		menu.getData().zindex(10);
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList(hud, menu), this.bridges.getUi().getUiList());
		Assert.assertEquals(Arrays.asList("draw hud -2000.0", "draw menu -1990.0"), this.trace);
	}

	@Test
	public void keepsTheOpeningOrderOfTheOtherUisOnceOneMoves() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		final TraceUI chat = new TraceUI("chat", this.trace);
		this.bridges.open(menu).open(hud).open(chat);
		chat.getData().zindex(-1);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(chat, menu, hud), this.bridges.getUi().getUiList());
	}

	@Test
	public void putsTheLastOpenedUiOnTop() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		Assert.assertTrue(this.bridges.getUi().isOnTop(hud));
		Assert.assertFalse(this.bridges.getUi().isOnTop(menu));
		Assert.assertTrue(hud.isOnTop());
		Assert.assertFalse(menu.isOnTop());
	}

	@Test
	public void putsTheUiOfHighestZindexOnTop() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		menu.getData().zindex(10);
		this.bridges.open(menu).open(new TraceUI("hud", this.trace));
		Assert.assertTrue(this.bridges.getUi().isOnTop(menu));
		Assert.assertTrue(menu.isOnTop());
	}

	@Test
	public void putsAUiOnTopOnceItsZindexRises() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		menu.getData().zindex(5);
		this.bridges.frame();
		Assert.assertTrue(menu.isOnTop());
		Assert.assertFalse(hud.isOnTop());
	}

	@Test
	public void putsTheTopActiveAndVisibleUiOnTop() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().active(false);
		hidden.getData().visible(false);
		this.bridges.open(menu).open(inactive).open(hidden);
		Assert.assertTrue(this.bridges.getUi().isOnTop(menu));
		Assert.assertFalse(this.bridges.getUi().isOnTop(inactive));
		Assert.assertFalse(this.bridges.getUi().isOnTop(hidden));
	}

	@Test
	public void putsNoUiOnTopWithoutUi() {
		Assert.assertFalse(this.bridges.getUi().isOnTop(new TraceUI("menu", this.trace)));
	}

	@Test
	public void reportsWhetherAScreenConsumedAnEvent() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		Assert.assertFalse(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		menu.cancel = true;
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertTrue(this.bridges.getUi().mouseMoved());
		Assert.assertTrue(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
		Assert.assertTrue(this.bridges.getUi().mouseScroll(0D, 1D));
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertTrue(this.bridges.getUi().charTyped('a'));
		Assert.assertFalse(this.bridges.getUi().mouseScroll(0D, 0D));
	}

	@Test
	public void consumesNothingWithoutUi() {
		Assert.assertFalse(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertFalse(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertFalse(this.bridges.getUi().charTyped('a'));
	}

	@Test
	public void letsTheInputThroughAnOverlayWithoutInteraction() {
		final PassiveOverlayUI overlay = new PassiveOverlayUI("overlay", this.trace);
		overlay.getOverlay().render().aboveScreens(true);
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().charTyped('a');
		Assert.assertEquals(Arrays.asList("pressed menu LEFT", "key menu A", "char menu a"), this.trace);
	}

	@Test
	public void pressesAnInteractiveOverlayBeforeTheScreens() {
		this.bridges.open(new OverlayUI("overlay", this.trace)).open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("pressed overlay LEFT", "pressed menu LEFT"), this.trace);
	}

	@Test
	public void consumesTheEventsAnOverlayCancels() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		this.trace.clear();
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertTrue(this.bridges.getUi().mouseScroll(0D, 1D));
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertTrue(this.bridges.getUi().charTyped('a'));
		Assert.assertEquals(Arrays.asList("pressed overlay LEFT", "scrolled overlay 1.0", "key overlay A", "char overlay a"), this.trace);
	}

	@Test
	public void consumesTheReleaseOfAPressItConsumed() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		overlay.cancel = false;
		Assert.assertFalse(this.bridges.getUi().mouseReleased(MouseButton.RIGHT));
		Assert.assertTrue(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
		Assert.assertFalse(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
		Assert.assertFalse(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertFalse(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
	}

	@Test
	public void consumesTheReleaseOfEachButtonWhosePressItConsumed() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.MIDDLE));
		overlay.cancel = false;
		Assert.assertFalse(this.bridges.getUi().mousePressed(MouseButton.RIGHT));
		Assert.assertFalse(this.bridges.getUi().mouseReleased(MouseButton.RIGHT));
		Assert.assertTrue(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
		Assert.assertTrue(this.bridges.getUi().mouseReleased(MouseButton.MIDDLE));
		Assert.assertFalse(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
	}

	@Test
	public void consumesTheReleaseOfAKeyItConsumed() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		overlay.cancel = false;
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.B));
		Assert.assertTrue(this.bridges.getUi().keyReleased(Key.A));
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.A));
		Assert.assertFalse(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.A));
	}

	@Test
	public void consumesTheReleaseOfEachKeyWhosePressItConsumed() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.B));
		overlay.cancel = false;
		Assert.assertFalse(this.bridges.getUi().keyPressed(Key.C));
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.C));
		Assert.assertTrue(this.bridges.getUi().keyReleased(Key.A));
		Assert.assertTrue(this.bridges.getUi().keyReleased(Key.B));
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.A));
	}

	@Test
	public void consumesTheReleaseOfTheEscapeThatClosedAScreen() {
		this.bridges.open(new TraceUI("menu", this.trace));
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.ESCAPE));
		Assert.assertTrue(this.bridges.getUi().getUiList().isEmpty());
		Assert.assertTrue(this.bridges.getUi().keyReleased(Key.ESCAPE));
	}

	@Test
	public void leavesUnconsumedTheEventsAnOverlayDoesNotCancel() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		overlay.getOverlay().interaction().cancelClick(false).cancelScroll(false).cancelKeyboard(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(overlay);
		this.trace.clear();
		Assert.assertFalse(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertFalse(this.bridges.getUi().mouseMoved());
		Assert.assertFalse(this.bridges.getUi().mouseReleased(MouseButton.LEFT));
		Assert.assertFalse(this.bridges.getUi().mouseScroll(0D, 1D));
		Assert.assertFalse(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.A));
		Assert.assertFalse(this.bridges.getUi().charTyped('a'));
		Assert.assertFalse(this.trace.stream().anyMatch(line -> line.contains("menu")));
	}

	@Test
	public void cancelsEachKindOfEventWithItsOwnFlag() {
		final TraceUI overlay = new OverlayUI("overlay", this.trace);
		overlay.cancel = true;
		overlay.getOverlay().interaction().cancelClick(false).cancelKeyboard(false);
		this.bridges.open(overlay);
		Assert.assertFalse(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertTrue(this.bridges.getUi().mouseScroll(0D, -1D));
		Assert.assertFalse(this.bridges.getUi().keyPressed(Key.A));
		Assert.assertFalse(this.bridges.getUi().keyReleased(Key.A));
		Assert.assertFalse(this.bridges.getUi().charTyped('a'));
	}

	@Test
	public void keepsEscapeForTheScreenBelowAnOverlay() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
		this.bridges.open(menu).open(overlay);
		this.trace.clear();
		Assert.assertTrue(this.bridges.getUi().keyPressed(Key.ESCAPE));
		Assert.assertEquals(Arrays.asList("key overlay ESCAPE", "key menu ESCAPE", "close menu"), this.trace);
		Assert.assertEquals(Collections.singletonList(overlay), this.bridges.getUi().getUiList());
	}

	@Test
	public void drawsTheOverlaysAboveTheScreensByZindex() {
		final OverlayUI high = new OverlayUI("high", this.trace);
		final OverlayUI low = new OverlayUI("low", this.trace);
		high.getData().zindex(2);
		low.getData().zindex(1);
		this.bridges.open(high).open(low).open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList("draw menu -2000.0", "draw low -1990.0", "draw high -1980.0"), this.trace);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("pressed high LEFT", "pressed low LEFT", "pressed menu LEFT"), this.trace);
	}

	@Test
	public void drawsAnOverlayBelowTheScreensUnlessAbove() {
		final PassiveOverlayUI overlay = new PassiveOverlayUI("overlay", this.trace);
		this.bridges.open(overlay).open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList("draw overlay -2000.0", "draw menu -1990.0"), this.trace);
		overlay.getOverlay().render().aboveScreens(true);
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList("draw menu -2000.0", "draw overlay -1990.0"), this.trace);
	}

	@Test
	public void sortsTheOverlaysBelowTheScreensByZindex() {
		final PassiveOverlayUI high = new PassiveOverlayUI("high", this.trace);
		final PassiveOverlayUI low = new PassiveOverlayUI("low", this.trace);
		high.getData().zindex(2);
		low.getData().zindex(1);
		this.bridges.open(high).open(low).open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.draw();
		Assert.assertEquals(Arrays.asList("draw low -2000.0", "draw high -1990.0", "draw menu -1980.0"), this.trace);
	}

	@Test
	public void hidesTheHiddenOverlaysUnlessAlways() {
		final HidingBridge bridge = new HidingBridge();
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
		bridge.add(overlay);
		bridge.hidden = true;
		this.trace.clear();
		this.bridges.draw(bridge);
		bridge.mousePressed(MouseButton.LEFT);
		Assert.assertTrue(this.trace.isEmpty());
		overlay.getOverlay().render().always(true);
		this.bridges.draw(bridge);
		bridge.mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("draw overlay -2000.0", "pressed overlay LEFT"), this.trace);
		bridge.remove(overlay);
		overlay.dispose();
	}

	@Test
	public void keepsTheScreensAndTheOverlaysOnTopSeparately() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
		final PassiveOverlayUI passive = new PassiveOverlayUI("passive", this.trace);
		passive.getOverlay().render().aboveScreens(true);
		this.bridges.open(menu).open(overlay).open(passive);
		Assert.assertTrue(this.bridges.getUi().isOnTop(menu));
		Assert.assertTrue(this.bridges.getUi().isOnTop(overlay));
		Assert.assertFalse(this.bridges.getUi().isOnTop(passive));
	}

	@Test
	public void opensAnOverlayWithoutClosingTheScreens() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
		final TraceUI settings = new TraceUI("settings", this.trace);
		this.bridges.getUi().open(menu);
		this.bridges.getUi().open(overlay);
		Assert.assertEquals(Arrays.asList(menu, overlay), this.bridges.getUi().getUiList());
		this.bridges.getUi().open(settings);
		Assert.assertEquals(Arrays.asList(overlay, settings), this.bridges.getUi().getUiList());
		Assert.assertTrue(this.bridges.getUi().hasScreen());
	}

	@Test
	public void opensNoScreenWithOnlyOverlays() {
		this.bridges.open(new OverlayUI("overlay", this.trace));
		Assert.assertFalse(this.bridges.getUi().hasScreen());
		Assert.assertFalse(this.bridges.getUi().isOverlayHidden());
	}

	@Test
	public void updatesThenDrawsItsBackgroundInsideTheFrameBeforeItsUis() {
		final UIBridge bridge = new StackUIBridge() {

			@Override
			protected void drawBackground() {
				UIBridgeTest.this.trace.add("background " + ((RenderBridge) BridgeHandler.RENDER.get()).isFrameActive());
			}

		};
		bridge.add(new TraceUI("menu", this.trace));
		this.trace.clear();
		bridge.frame();
		Assert.assertEquals(Arrays.asList("update menu", "background true", "draw menu -2000.0"), this.trace);
		Assert.assertFalse(((RenderBridge) BridgeHandler.RENDER.get()).isFrameActive());
	}

	@Test
	public void resizesTheScreenAndLoadsItsUis() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getUi().resize(800, 600);
		Assert.assertEquals(800, this.bridges.getRender().getState().getViewportWidth());
		Assert.assertEquals(600, this.bridges.getRender().getState().getViewportHeight());
		Assert.assertEquals(1, menu.inits);
	}

	@Test
	public void drawsOnlyTheUisItsFilterAccepts() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final OverlayUI hud = new OverlayUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.draw(ui -> ui == hud);
		Assert.assertEquals(1, this.trace.size());
		Assert.assertTrue(this.trace.get(0), this.trace.get(0).startsWith("draw hud"));
	}

	@Test
	public void drawsTheTooltipLinesOfTheDemoBridge() {
		final TraceFont font = new TraceFont();
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		new TooltipBridge(TextInfo.create(font, 20F)).drawHover(menu, Arrays.asList("Play", 42), 100D, 100D);
		Assert.assertEquals(Arrays.asList("Play", "42"), font.drawn);
	}

	@Test
	public void drawsTheTooltipBackgroundOfTheDemoBridge() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getRender().getDraws().clear();
		new TooltipBridge(TextInfo.create(new TraceFont(), 20F)).drawHover(menu, "Play", 100D, 100D);
		Assert.assertTrue(this.bridges.getRender().getDraws().stream().anyMatch(draw -> draw.getRed() == 0x27 / 255F && draw.getBlue() == 0x2A / 255F));
		Assert.assertTrue(this.bridges.getRender().getDraws().stream().anyMatch(draw -> draw.getRed() == 0x18 / 255F && draw.getBlue() == 0x1B / 255F));
	}

	@Test
	public void drawsNoTooltipWithoutTheDevFont() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getRender().getDraws().clear();
		new TooltipBridge(null).drawHover(menu, "Play", 100D, 100D);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void warnsOnceInDevModeWhenABridgeDrawsNoTooltip() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		final UIBridge bridge = new StackUIBridge() {};
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(true);
		try {
			bridge.drawHover(menu, "Play", 100D, 100D);
			bridge.drawHover(menu, "Play", 100D, 100D);
		} finally {
			JOIDEnv.devMode(devMode);
			System.setErr(previous);
		}
		Assert.assertEquals("[JOID] " + bridge.getClass().getSimpleName() + " draws no tooltip, override drawHover(UI, Object, double, double) to draw them" + System.lineSeparator(), output.toString());
	}

	@Test
	public void staysSilentOutOfDevModeWhenABridgeDrawsNoTooltip() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getRender().getDraws().clear();
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(false);
		try {
			new StackUIBridge() {}.drawHover(menu, "Play", 100D, 100D);
		} finally {
			JOIDEnv.devMode(devMode);
			System.setErr(previous);
		}
		Assert.assertEquals("", output.toString());
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void drawsNothingForAnEmptyTooltip() {
		final TraceFont font = new TraceFont();
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getRender().getDraws().clear();
		new TooltipBridge(TextInfo.create(font, 20F)).drawHover(menu, Collections.emptyList(), 100D, 100D);
		Assert.assertTrue(font.drawn.isEmpty());
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void clearsTheScreenToTheBackgroundOfAWindowBridge() {
		final WindowUIBridge bridge = WindowUIBridge.create();
		bridge.frame();
		Assert.assertEquals(Color.BLACK, this.bridges.getRender().getClearedColor());
		bridge.background(Color.RED).frame();
		Assert.assertEquals(Color.RED, this.bridges.getRender().getClearedColor());
		Assert.assertEquals(2, this.bridges.getRender().getColorClears());
		Assert.assertEquals(2, this.bridges.getRender().getDepthClears());
	}

	@Test
	public void drawsTheTooltipOfAWindowBridgeWithItsHoverInfo() {
		final TraceFont font = new TraceFont();
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getRender().getDraws().clear();
		WindowUIBridge.create().hoverColor(Color.BLUE).hoverInfo(TextInfo.create(font, 20F)).drawHover(menu, "Play", 100D, 100D);
		Assert.assertEquals(Collections.singletonList("Play"), font.drawn);
		Assert.assertTrue(this.bridges.getRender().getDraws().stream().anyMatch(draw -> draw.getBlue() == 1F && draw.getRed() == 0F));
	}

	@Test
	public void warnsOnceInDevModeWhenAWindowBridgeHasNoHoverInfo() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		this.bridges.getRender().getDraws().clear();
		final WindowUIBridge bridge = WindowUIBridge.create();
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(true);
		try {
			bridge.drawHover(menu, "Play", 100D, 100D);
			bridge.drawHover(menu, "Play", 100D, 100D);
		} finally {
			JOIDEnv.devMode(devMode);
			System.setErr(previous);
		}
		Assert.assertEquals("[JOID] WindowUIBridge draws no tooltip, set its hoverInfo(TextInfo) to draw them" + System.lineSeparator(), output.toString());
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesATooltipWithoutContent() {
		this.bridges.getUi().drawHover(new TraceUI("menu", this.trace), null, 0D, 0D);
	}

	private static float depth() {
		return ((RenderBridge) BridgeHandler.RENDER.get()).getModelView().getMatrix()[14];
	}

	public static final class TooltipBridge extends DemoUIBridge {

		private final TextInfo info;

		public TooltipBridge(final TextInfo info) {
			this.info = info;
		}

		@Override
		public TextInfo getHoverInfo() {
			return this.info;
		}

	}

	public static final class TraceFont implements IFont, ITextRenderer {

		private final List<String> drawn = new ArrayList<>();

		@Override
		public @NonNull ITextRenderer getTextRenderer() {
			return this;
		}

		@Override
		public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info) {
			this.drawn.add(text);
			return FontBounds.create(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public double getLineHeight(final @NonNull TextInfo info) {
			return info.getFontSize();
		}

		@Override
		public double getWidth(final @NonNull String text, final @NonNull TextInfo info) {
			return text.length() * 10D;
		}

		@Override
		public double getHeight(final @NonNull String text, final @NonNull TextInfo info) {
			return info.getFontSize();
		}

	}

	public static class TraceUI extends UI {

		private final String       name;
		private final List<String> trace;

		private int              inits;
		private double           level;
		private boolean          cancel;
		private boolean          closeable = true;
		private RuntimeException failure;

		public TraceUI(final String name, final List<String> trace) {
			this.name = name;
			this.trace = trace;
			super.beforeClose(event -> {
				this.trace.add("close " + this.name);
				if (!this.closeable) {
					event.cancel();
				}
			});
			super.handleMousePressed(event -> {
				this.trace.add("pressed " + this.name + " " + event.getButton());
				this.cancel(event);
			});
			super.handleMouseDragged(event -> {
				this.trace.add("dragged " + this.name + " " + event.getButton() + " " + event.getDeltaTime());
				this.cancel(event);
			});
			super.handleMouseReleased(event -> {
				this.trace.add("released " + this.name + " " + event.getButton());
				this.cancel(event);
			});
			super.handleMouseScroll(event -> {
				this.trace.add("scrolled " + this.name + " " + event.getNotchesY());
				this.cancel(event);
			});
			super.handleKeyPressed(event -> this.cancel(event));
			super.handleKeyPressed(EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("key " + this.name + " " + event.getKey())));
			super.handleCharTyped(event -> this.cancel(event));
			super.handleCharTyped(EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("char " + this.name + " " + new String(Character.toChars(event.getCodepoint())))));
		}

		@Override
		public void init() {
			this.inits++;
			super.add(new DrawingNode(() -> {
				if (this.failure != null) {
					throw this.failure;
				}

				this.trace.add("draw " + this.name + " " + UIBridgeTest.depth());
				super.depthLevel(this.level);
			}));
		}

		@Override
		public void update() {
			this.trace.add("update " + this.name);
		}

		private void cancel(final ConsumableEvent event) {
			if (this.cancel) {
				event.consume();
			}
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class PopupUI extends TraceUI {

		public PopupUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

	@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true), render = @UIDataOverlayRender(aboveScreens = true))
	public static final class OverlayUI extends TraceUI {

		public OverlayUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

	@UIDataOverlay(active = true)
	public static final class PassiveOverlayUI extends TraceUI {

		public PassiveOverlayUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

	public static final class HidingBridge extends DemoUIBridge {

		private boolean hidden;

		@Override
		public boolean isOverlayHidden() {
			return this.hidden;
		}

	}

	public static class DepthUI extends UI {

		private final String       name;
		private final List<String> trace;

		private float depth;

		public DepthUI(final String name, final List<String> trace) {
			this.name = name;
			this.trace = trace;
			super.handleMousePressed(event -> this.trace.add("pressed " + this.name));
		}

		@Override
		public void update() {
			this.trace.add("update " + this.name);
		}

		@Override
		public void init() {
			super.add(new DrawingNode(() -> {
				this.trace.add("draw " + this.name);
				this.depth = ((RenderBridge) BridgeHandler.RENDER.get()).getModelView().getMatrix()[14];
			}));
		}

	}

	public static final class DrawingNode extends Node {

		private final Runnable drawing;

		private DrawingNode(final Runnable drawing) {
			super(0D, 0D, 0D, 0D);
			this.drawing = drawing;
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			this.drawing.run();
		}

	}

}