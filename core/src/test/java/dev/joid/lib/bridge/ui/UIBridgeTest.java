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

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

import lombok.NonNull;

public class UIBridgeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace = new ArrayList<>();

	@Test
	public void listsTheUisItOpened() {
		final TraceUI ui = new TraceUI("menu", this.trace);
		Assert.assertFalse(this.bridges.getUi().isOpened(ui));
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertTrue(this.bridges.getUi().isOpened(ui));
		Assert.assertEquals(Collections.singletonList(ui), this.bridges.getUi().getUiList().ordered());
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
		Assert.assertEquals(1366D, menu.getWidth(), 0D);
		Assert.assertEquals(768D, menu.getHeight(), 0D);
		Assert.assertEquals(1366D, hud.getWidth(), 0D);
		Assert.assertEquals(768D, hud.getHeight(), 0D);
		Assert.assertEquals(1, menu.inits);
	}

	@Test
	public void keepsTheZoomOfEveryUiOnAResize() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		this.bridges.open(menu);
		menu.zoom(0.5D);
		this.bridges.resize(1366, 768);
		Assert.assertEquals(0.5D, menu.getView().getZoom(), 0D);
		Assert.assertEquals(0.5D, menu.getZoomLevel().get(), 0D);
		Assert.assertEquals(1366D, menu.getWidth(), 0D);
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
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("pressed hud LEFT", "pressed menu LEFT"), this.trace);
	}

	@Test
	public void stopsAPressOnceAUiCancelsIt() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.cancel = true;
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().mousePressed(ClickType.RIGHT);
		Assert.assertEquals(Collections.singletonList("pressed hud RIGHT"), this.trace);
	}

	@Test
	public void skipsTheInactiveAndHiddenUisOnAPress() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().setActive(false);
		hidden.getData().setVisible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Collections.singletonList("pressed menu LEFT"), this.trace);
	}

	@Test
	public void keepsThePressInsideAPopup() {
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace));
		this.trace.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Collections.singletonList("pressed popup LEFT"), this.trace);
	}

	@Test
	public void dragsTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 40L);
		hud.cancel = true;
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 80L);
		Assert.assertEquals(Arrays.asList("dragged hud LEFT 40", "dragged menu LEFT 40", "dragged hud LEFT 80"), this.trace);
	}

	@Test
	public void keepsTheDragInsideAPopup() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().setActive(false);
		hidden.getData().setVisible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().mouseDragged(ClickType.LEFT, 40L);
		Assert.assertEquals(Collections.singletonList("dragged popup LEFT 40"), this.trace);
	}

	@Test
	public void releasesTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().mouseReleased(ClickType.MIDDLE);
		hud.cancel = true;
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("released hud MIDDLE", "released menu MIDDLE", "released hud LEFT"), this.trace);
	}

	@Test
	public void keepsTheReleaseInsideAPopup() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().setActive(false);
		hidden.getData().setVisible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().mouseReleased(ClickType.LEFT);
		Assert.assertEquals(Collections.singletonList("released popup LEFT"), this.trace);
	}

	@Test
	public void scrollsTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.scroll(120);
		hud.cancel = true;
		this.bridges.scroll(-120);
		Assert.assertEquals(Arrays.asList("scrolled hud 120", "scrolled menu 120", "scrolled hud -120"), this.trace);
	}

	@Test
	public void ignoresAnEmptyScroll() {
		this.bridges.open(new TraceUI("menu", this.trace));
		this.trace.clear();
		this.bridges.scroll(0);
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void keepsTheScrollInsideAPopup() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().setActive(false);
		hidden.getData().setVisible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.scroll(120);
		Assert.assertEquals(Collections.singletonList("scrolled popup 120"), this.trace);
	}

	@Test
	public void typesInTheTopUiFirst() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyTyped('a', Key.A);
		hud.cancel = true;
		this.bridges.getUi().keyTyped('b', Key.B);
		Assert.assertEquals(Arrays.asList("typed hud a A", "typed menu a A", "typed hud b B"), this.trace);
	}

	@Test
	public void skipsTheInactiveAndHiddenUisOnAKey() {
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().setActive(false);
		hidden.getData().setVisible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(new PopupUI("popup", this.trace)).open(inactive).open(hidden);
		this.trace.clear();
		this.bridges.getUi().keyTyped('a', Key.A);
		Assert.assertEquals(Collections.singletonList("typed popup a A"), this.trace);
	}

	@Test
	public void closesTheTopUiOnEscape() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyTyped('\0', Key.ESCAPE);
		Assert.assertEquals(Collections.singletonList("close hud"), this.trace);
		Assert.assertEquals(Collections.singletonList(menu), this.bridges.getUi().getUiList().ordered());
	}

	@Test
	public void typesEscapeInAUiThatCannotBeClosed() {
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.getData().setCloseable(false);
		this.bridges.open(hud);
		this.trace.clear();
		this.bridges.getUi().keyTyped('\0', Key.ESCAPE);
		Assert.assertEquals(Collections.singletonList("typed hud \0 ESCAPE"), this.trace);
		Assert.assertTrue(this.bridges.getUi().isOpened(hud));
	}

	@Test
	public void consumesEscapeInAUiThatRefusesToClose() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		hud.closeable = false;
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.getUi().keyTyped('\0', Key.ESCAPE);
		Assert.assertEquals(Collections.singletonList("close hud"), this.trace);
		Assert.assertTrue(this.bridges.getUi().isOpened(hud));
		Assert.assertTrue(this.bridges.getUi().isOpened(menu));
	}

	@Test
	public void drawsTheVisibleUisInTheirOpeningOrder() {
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		hidden.getData().setVisible(false);
		this.bridges.open(new TraceUI("menu", this.trace)).open(hidden).open(new TraceUI("hud", this.trace));
		this.trace.clear();
		this.bridges.getUi().draw();
		Assert.assertEquals(Arrays.asList("draw menu -2000.0", "draw hud -1990.0"), this.trace);
	}

	@Test
	public void stacksEachUiAboveTheDepthOfThePreviousOne() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		menu.level = 40D;
		hud.getData().setZlevel(5D);
		this.bridges.open(menu).open(hud);
		this.trace.clear();
		this.bridges.getUi().draw();
		Assert.assertEquals(Arrays.asList("draw menu -2000.0", "draw hud -1945.0"), this.trace);
		Assert.assertEquals(0F, UIBridgeTest.depth(), 0F);
	}

	@Test
	public void drawsNothingWithoutUi() {
		this.bridges.getUi().draw();
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
			this.bridges.getUi().draw();
		} finally {
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains("broken"));
		Assert.assertEquals(0F, UIBridgeTest.depth(), 0F);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutButton() {
		this.bridges.getUi().mousePressed(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesADragWithoutButton() {
		this.bridges.getUi().mouseDragged(null, 40L);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAReleaseWithoutButton() {
		this.bridges.getUi().mouseReleased(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesATypeWithoutKey() {
		this.bridges.getUi().keyTyped('a', null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToLookForAMissingUi() {
		this.bridges.getUi().isOpened(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToScaleAMissingUi() {
		this.bridges.getUi().getInterfaceScale(null);
	}

	@Test
	public void keepsUpdatingAndDrawingAnInactiveUi() {
		final DepthUI menu = new DepthUI("menu", this.trace);
		this.bridges.open(menu);
		menu.getData().setActive(false);
		this.trace.clear();
		this.bridges.frame();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Arrays.asList("update menu", "draw menu"), this.trace);
	}

	@Test
	public void letsTheMouseThroughAnInactivePopup() {
		final PopupUI popup = new PopupUI("popup", this.trace);
		this.bridges.open(new DepthUI("menu", this.trace)).open(popup);
		popup.getData().setActive(false);
		this.trace.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(Collections.singletonList("pressed menu"), this.trace);
	}

	@Test
	public void keepsDrawingTheUisAboveAUiClosingBehindThem() {
		final DepthUI menu = new DepthUI("menu", this.trace);
		menu.setTransition(new PopTransition());
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
		Assert.assertFalse(this.bridges.getUi().isOpened(menu));
	}

	@Test
	public void drawsTheUiOfHigherZlevelOnTop() {
		final DepthUI menu = new DepthUI("menu", this.trace);
		final DepthUI hud = new DepthUI("hud", this.trace);
		menu.getData().setZlevel(10D);
		this.bridges.open(menu).open(hud);
		Assert.assertTrue(menu.depth + " behind " + hud.depth, menu.depth > hud.depth);
	}

	@Test
	public void sortsAUiAgainOnceItsZlevelChanges() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		menu.getData().setZlevel(10D);
		this.trace.clear();
		this.bridges.getUi().draw();
		Assert.assertEquals(Arrays.asList(hud, menu), this.bridges.getUi().getUiList().ordered());
		Assert.assertEquals(Arrays.asList("draw hud -2000.0", "draw menu -1980.0"), this.trace);
	}

	@Test
	public void keepsTheOpeningOrderOfTheOtherUisOnceOneMoves() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		final TraceUI chat = new TraceUI("chat", this.trace);
		this.bridges.open(menu).open(hud).open(chat);
		chat.getData().setZlevel(-1D);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList(chat, menu, hud), this.bridges.getUi().getUiList().ordered());
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
	public void putsTheUiOfHighestZlevelOnTop() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		menu.getData().setZlevel(10D);
		this.bridges.open(menu).open(new TraceUI("hud", this.trace));
		Assert.assertTrue(this.bridges.getUi().isOnTop(menu));
		Assert.assertTrue(menu.isOnTop());
	}

	@Test
	public void putsAUiOnTopOnceItsZlevelRises() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI hud = new TraceUI("hud", this.trace);
		this.bridges.open(menu).open(hud);
		menu.getData().setZlevel(5D);
		this.bridges.frame();
		Assert.assertTrue(menu.isOnTop());
		Assert.assertFalse(hud.isOnTop());
	}

	@Test
	public void putsTheTopActiveAndVisibleUiOnTop() {
		final TraceUI menu = new TraceUI("menu", this.trace);
		final TraceUI inactive = new TraceUI("inactive", this.trace);
		final TraceUI hidden = new TraceUI("hidden", this.trace);
		inactive.getData().setActive(false);
		hidden.getData().setVisible(false);
		this.bridges.open(menu).open(inactive).open(hidden);
		Assert.assertTrue(this.bridges.getUi().isOnTop(menu));
		Assert.assertFalse(this.bridges.getUi().isOnTop(inactive));
		Assert.assertFalse(this.bridges.getUi().isOnTop(hidden));
	}

	@Test
	public void putsNoUiOnTopWithoutUi() {
		Assert.assertFalse(this.bridges.getUi().isOnTop(new TraceUI("menu", this.trace)));
	}

	private static float depth() {
		return ((RenderBridge) BridgeHandler.RENDER.get()).getModelView().getMatrix()[14];
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
		}

		@Override
		public void init() {
			this.inits++;
		}

		@Override
		public boolean close() {
			this.trace.add("close " + this.name);
			return this.closeable;
		}

		@Override
		public void update() {
			this.trace.add("update " + this.name);
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("pressed " + this.name + " " + clickType);
			this.cancel(context);
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
			this.trace.add("dragged " + this.name + " " + clickType + " " + deltaTime);
			this.cancel(context);
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("released " + this.name + " " + clickType);
			this.cancel(context);
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
			this.trace.add("scrolled " + this.name + " " + value);
			this.cancel(context);
		}

		@Override
		public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
			this.trace.add("typed " + this.name + " " + c + " " + key);
			this.cancel(context);
		}

		@Override
		public void preDraw(final double mouseX, final double mouseY) {
			if (this.failure != null) {
				throw this.failure;
			}

			this.trace.add("draw " + this.name + " " + UIBridgeTest.depth());
			super.setRenderPipelineLevel(this.level);
		}

		private void cancel(final InternalContext context) {
			if (this.cancel) {
				context.cancel();
			}
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class PopupUI extends TraceUI {

		public PopupUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

	public static class DepthUI extends UI {

		private final String       name;
		private final List<String> trace;

		private float depth;

		public DepthUI(final String name, final List<String> trace) {
			this.name = name;
			this.trace = trace;
		}

		@Override
		public void update() {
			this.trace.add("update " + this.name);
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("pressed " + this.name);
		}

		@Override
		public void preDraw(final double mouseX, final double mouseY) {
			this.trace.add("draw " + this.name);
			this.depth = ((RenderBridge) BridgeHandler.RENDER.get()).getModelView().getMatrix()[14];
		}

	}

}