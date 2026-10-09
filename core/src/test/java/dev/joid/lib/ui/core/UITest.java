package dev.joid.lib.ui.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.internal.font.InternalFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontScale;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.debug.UIDataDebug;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlay;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.data.scale.UIDataScale;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.hook.store.context.StoreContext;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.dev.DevNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;

import lombok.AllArgsConstructor;
import lombok.NonNull;

public class UITest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private final List<String> trace = new ArrayList<>();

	private File    configDir;
	private boolean devMode;

	@BeforeClass
	public static void loadTheDevFont() {
		InternalFont.load();
	}

	@Before
	public void useATemporaryConfig() {
		this.configDir = JOID.inst().getConfigDir();
		this.devMode = JOID.inst().isDevMode();
		JOID.inst().setConfigDir(this.folder.getRoot());
	}

	@After
	public void restoreTheSettings() {
		JOID.inst().setConfigDir(this.configDir).setDevMode(this.devMode);
	}

	@Test
	public void readsItsSettingsFromItsAnnotations() {
		final FixedUI ui = new FixedUI(this.trace);
		Assert.assertFalse(ui.getData().zoomable());
		Assert.assertFalse(ui.getDebug().profiler());
		Assert.assertFalse(ui.getDebug().hotreload());
		Assert.assertFalse(ui.getPopup().active());
		Assert.assertNull(ui.getTransition());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToBeAPopupAndAnOverlay() {
		new PopupOverlayUI();
	}

	@Test
	public void popsAPopupInAndOut() {
		final PopupUI ui = new PopupUI(this.trace);
		Assert.assertTrue(ui.getTransition() instanceof PopTransition);
		Assert.assertTrue(ui.getTransition().getIn().isEnabled());
		Assert.assertTrue(ui.getTransition().getOut().isEnabled());
	}

	@Test
	public void popsAPopupInOnly() {
		final InPopupUI ui = new InPopupUI(this.trace);
		Assert.assertTrue(ui.getTransition().getIn().isEnabled());
		Assert.assertFalse(ui.getTransition().getOut().isEnabled());
	}

	@Test
	public void popsAPopupOutOnly() {
		final OutPopupUI ui = new OutPopupUI(this.trace);
		Assert.assertFalse(ui.getTransition().getIn().isEnabled());
		Assert.assertTrue(ui.getTransition().getOut().isEnabled());
	}

	@Test
	public void opensAPopupWithoutTransition() {
		Assert.assertNull(new StillPopupUI(this.trace).getTransition());
	}

	@Test
	public void replacesItsTransition() {
		final TraceUI ui = new TraceUI(this.trace);
		final RecordingTransition transition = new RecordingTransition(this.trace);
		Assert.assertSame(ui, ui.setTransition(transition));
		Assert.assertSame(transition, ui.getTransition());
		ui.setTransition(null);
		Assert.assertNull(ui.getTransition());
	}

	@Test
	public void sitsAtTheFirstIndex() {
		Assert.assertEquals(0, new TraceUI(this.trace).getIndex());
	}

	@Test
	public void findsItsBridge() {
		Assert.assertSame(this.bridges.getUi(), new TraceUI(this.trace).getBridge());
	}

	@Test
	public void initializesOnceOnItsFirstLoad() {
		final TraceUI ui = new TraceUI(this.trace);
		Assert.assertFalse(ui.isInitialized());
		ui.load(1366D, 768D);
		Assert.assertTrue(ui.isInitialized());
		Assert.assertEquals(1, ui.inits);
		Assert.assertEquals(1366D, ui.getWidth(), 0D);
		Assert.assertEquals(768D, ui.getHeight(), 0D);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		ui.load(1920D, 1080D, 0.5D);
		Assert.assertEquals(1, ui.inits);
		Assert.assertEquals(1920D, ui.getWidth(), 0D);
		Assert.assertEquals(1080D, ui.getHeight(), 0D);
		Assert.assertEquals(0.5D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(0.5D, ui.getZoomLevel().get(), 0D);
	}

	@Test
	public void exposesItselfAsTheCurrentUiDuringItsInit() {
		final CurrentUI ui = new CurrentUI();
		ui.load(1920D, 1080D);
		Assert.assertSame(ui, ui.current);
		Assert.assertNull(UI.getCurrent());
	}

	@Test
	public void measuresItsTextAtItsOwnPixelScale() {
		final HoverBridge bridge = new HoverBridge();
		final ScaleUI ui = new ScaleUI();
		bridge.interfaceScale = 0.25D;
		BridgeHandler.UI.register(bridge);
		try {
			this.bridges.resize(1280, 720);
			bridge.add(ui);
			Assert.assertEquals(Arrays.asList("init 0.6667"), ui.scales);
			bridge.draw();
			ui.zoom(2D);
			ui.scales.clear();
			bridge.update();
			bridge.draw();
			bridge.mousePressed(ClickType.LEFT);
			bridge.keyTyped('a', Key.A);
			Assert.assertEquals(Arrays.asList("update 0.3333", "draw 0.3333", "press 0.3333", "key 0.3333"), ui.scales);
			Assert.assertEquals(1D, FontScale.getScale(), 1E-6D);
		} finally {
			BridgeHandler.UI.unregister(bridge);
		}
	}

	@Test
	public void reloadsItsInitOnAFreshTree() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		ui.keybind = new Key[] {Key.R};
		this.bridges.open(ui);
		ui.reload();
		Assert.assertEquals(2, ui.inits);
		Assert.assertEquals(1, ui.getNodeList().size());
		Assert.assertEquals(1, ui.getKeybindMap().size());
		Assert.assertTrue(ui.isInitialized());
	}

	@Test
	public void keepsItsValuesChangedAtRuntimeOnReload() {
		final PopupUI ui = new PopupUI(this.trace);
		this.bridges.open(ui);
		ui.getData().setZlevel(4D).setAnchorX(Align.START);
		ui.getDebug().setProfiler(true);
		ui.getPopup().setActive(false);
		ui.reload();
		this.bridges.frame();
		Assert.assertEquals(4D, ui.getData().zlevel(), 0D);
		Assert.assertEquals(0D, ui.getView().getAnchorX(), 0D);
		Assert.assertTrue(ui.getDebug().profiler());
		Assert.assertFalse(ui.getPopup().active());
		Assert.assertNull(ui.getTransition());
	}

	@Test
	public void appliesOnReloadOnlyTheAnnotationValuesChangedSinceTheirLastRead() {
		final AnnotatedUI ui = new AnnotatedUI(this.trace);
		this.bridges.open(ui);
		ui.getData().setZlevel(4D).setAnchorX(Align.START);
		ui.getPopup().setActive(true);
		this.bridges.frame();
		Assert.assertTrue(ui.getTransition() instanceof PopTransition);
		ui.getAnnotatedData().setZlevel(2D);
		ui.getAnnotatedPopup().setActive(true);
		ui.reload();
		this.bridges.frame();
		Assert.assertEquals(1D, ui.getData().zlevel(), 0D);
		Assert.assertSame(Align.START, ui.getData().anchorX());
		Assert.assertFalse(ui.getPopup().active());
		Assert.assertNull(ui.getTransition());
		Assert.assertEquals(1D, ui.getAnnotatedData().zlevel(), 0D);
		ui.getData().setZlevel(5D);
		ui.reload();
		Assert.assertEquals(5D, ui.getData().zlevel(), 0D);
	}

	@Test
	public void becomesAPopupOnTheFrameAfterItsPopupChanges() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui).frame();
		Assert.assertNull(ui.getTransition());
		ui.getPopup().setActive(true);
		this.bridges.frame();
		Assert.assertTrue(ui.getTransition() instanceof PopTransition);
		ui.getPopup().setTransition(PopupTransition.NONE);
		this.bridges.frame();
		Assert.assertNull(ui.getTransition());
	}

	@Test
	public void watchesItsClassesFromTheFrameAfterItsHotReloadChanges() throws Exception {
		final UI ui = UITest.hotReloaded(this.folder.newFolder("classes"), new AtomicInteger());
		JOID.inst().setDevMode(true);
		ui.getDebug().setHotreload(false);
		UITest.out(() -> ui.load(1920D, 1080D));
		try {
			Assert.assertNull(ui.getFileMonitor());
			ui.getDebug().setHotreload(true);
			UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertNotNull(ui.getFileMonitor());
			ui.getDebug().setHotreload(false);
			UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertNull(ui.getFileMonitor());
		} finally {
			ui.properlyClose();
		}
	}

	@Test
	public void keepsItsTransitionAcrossAReloadThatLeavesItsPopupUnchanged() {
		final PopupUI ui = new PopupUI(this.trace);
		final Transition transition = new PopTransition();
		ui.setTransition(transition);
		this.bridges.open(ui);
		ui.reload();
		Assert.assertSame(transition, ui.getTransition());
	}

	@Test
	public void ignoresTheInputBeforeItsFirstLoad() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.cancel = true;
		Assert.assertFalse(ui.onMousePressed(ClickType.LEFT));
		Assert.assertFalse(ui.onMouseReleased(ClickType.LEFT));
		Assert.assertFalse(ui.onMouseDragged(ClickType.LEFT, 10L));
		Assert.assertFalse(ui.onMouseScroll(1D));
		Assert.assertFalse(ui.onKeyPressed('a', Key.A));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void receivesTheMouseOnItsCanvas() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.resize(1366, 768).open(ui);
		this.bridges.move(683D, 384D).frame();
		this.trace.clear();
		this.bridges.getUi().mousePressed(ClickType.LEFT);
		Assert.assertEquals(960D, ui.getMouseX(), 0D);
		Assert.assertEquals(540D, ui.getMouseY(), 0D);
		Assert.assertEquals(Collections.singletonList("pressed LEFT 960.0 540.0"), this.trace);
	}

	@Test
	public void receivesTheMouseThroughItsZoom() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.zoom(0.5D);
		this.bridges.move(0D, 0D).frame();
		Assert.assertEquals(-960D, ui.getMouseX(), 0D);
		Assert.assertEquals(-540D, ui.getMouseY(), 0D);
		this.bridges.move(960D, 540D).frame();
		Assert.assertEquals(960D, ui.getMouseX(), 0D);
		Assert.assertEquals(540D, ui.getMouseY(), 0D);
	}

	@Test
	public void followsItsAnchorChangedAfterItsOpening() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.zoom(0.5D);
		this.bridges.move(960D, 540D).frame();
		Assert.assertEquals(960D, ui.getMouseX(), 0D);
		Assert.assertEquals(540D, ui.getMouseY(), 0D);
		ui.getData().setAnchorX(Align.START).setAnchorY(Align.END);
		this.bridges.frame();
		Assert.assertEquals(0D, ui.getView().getAnchorX(), 0D);
		Assert.assertEquals(1080D, ui.getView().getAnchorY(), 0D);
		Assert.assertEquals(1920D, ui.getMouseX(), 0D);
		Assert.assertEquals(0D, ui.getMouseY(), 0D);
	}

	@Test
	public void tellsWhetherItsHooksCancelledTheInput() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		Assert.assertFalse(ui.onMousePressed(ClickType.LEFT));
		Assert.assertFalse(ui.onKeyPressed('a', Key.A));
		ui.cancel = true;
		Assert.assertTrue(ui.onMousePressed(ClickType.LEFT));
		Assert.assertTrue(ui.onMouseReleased(ClickType.LEFT));
		Assert.assertTrue(ui.onMouseDragged(ClickType.LEFT, 10L));
		Assert.assertTrue(ui.onMouseScroll(1D));
		Assert.assertTrue(ui.onKeyPressed('a', Key.A));
	}

	@Test
	public void passesTheInputToItsNodesFirst() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		this.bridges.open(ui);
		this.trace.clear();
		ui.onMouseScroll(1D);
		ui.onMouseDragged(ClickType.LEFT, 10L);
		ui.onMouseReleased(ClickType.LEFT);
		ui.onKeyPressed('a', Key.A);
		Assert.assertEquals(Arrays.asList("scrolled node", "scrolled 1.0", "dragged node", "dragged LEFT 10", "released node", "released LEFT", "typed node", "typed a A"), this.trace);
	}

	@Test
	public void pressesItsNodesFromTheHighestIndex() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("below", this.trace, -5), new TraceNode("middle", this.trace, 0), new TraceNode("above", this.trace, 5));
		this.bridges.open(ui);
		this.trace.clear();
		ui.onMousePressed(ClickType.RIGHT);
		Assert.assertEquals(Arrays.asList("pressed above", "pressed middle", "pressed below", "pressed RIGHT 0.0 0.0"), this.trace);
	}

	@Test
	public void updatesItsNodesBeforeItself() {
		this.bridges.open(new TraceUI(this.trace, new TraceNode("node", this.trace, 0)));
		this.trace.clear();
		this.bridges.getUi().update();
		Assert.assertEquals(Arrays.asList("update node", "update"), this.trace);
	}

	@Test
	public void runsAKeybindOnceAllItsKeysAreDown() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.keybind = new Key[] {Key.R, Key.LEFT_CONTROL};
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.R);
		this.bridges.getUi().keyTyped('r', Key.R);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(Arrays.asList("typed r R", "keybind", "typed r R cancelled"), this.trace);
	}

	@Test
	public void runsAKeybindRegisteredOutsideItsInit() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		ui.keybind(() -> this.trace.add("save"), Key.S);
		this.bridges.getWindow().getKeys().add(Key.S);
		this.bridges.getUi().keyTyped('s', Key.S);
		Assert.assertEquals(Arrays.asList("save", "typed s S cancelled"), this.trace);
	}

	@Test
	public void skipsTheKeybindsOnceANodeCancelsTheKey() {
		final TraceNode node = new TraceNode("node", this.trace, 0);
		final TraceUI ui = new TraceUI(this.trace, node);
		node.cancel = true;
		ui.keybind = new Key[] {Key.R};
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.R);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(Arrays.asList("typed node", "typed r R cancelled"), this.trace);
	}

	@Test
	public void keepsOneKeybindAcrossItsReloads() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.keybind = new Key[] {Key.R};
		this.bridges.open(ui);
		ui.reload();
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.R);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(Arrays.asList("keybind", "typed r R cancelled"), this.trace);
	}

	@Test
	public void keepsOneKeybindPerSetOfKeys() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		ui.keybind(() -> this.trace.add("first"), Key.LEFT_CONTROL, Key.S);
		ui.keybind(() -> this.trace.add("second"), Key.S, Key.LEFT_CONTROL);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.S));
		this.bridges.getUi().keyTyped('s', Key.S);
		Assert.assertEquals(1, ui.getKeybindMap().size());
		Assert.assertEquals(Arrays.asList("second", "typed s S cancelled"), this.trace);
	}

	@Test
	public void runsAKeybindOnlyForTheKeysOfItsShortcut() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		ui.keybind(() -> this.trace.add("save"), Key.LEFT_CONTROL, Key.S);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.S, Key.A));
		this.bridges.getUi().keyTyped('a', Key.A);
		this.bridges.getUi().keyTyped('s', Key.S);
		Assert.assertEquals(Arrays.asList("typed a A", "save", "typed s S cancelled"), this.trace);
	}

	@Test
	public void zoomsOutAndInWithTheModifierKeys() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('-', Key.MINUS);
		Assert.assertEquals(0.9D, ui.getView().getZoom(), 0D);
		this.bridges.getUi().keyTyped('a', Key.NUMPAD_ADD);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		this.bridges.getWindow().getKeys().clear();
		this.bridges.getWindow().getKeys().add(Key.RIGHT_ALT);
		this.bridges.getUi().keyTyped('s', Key.NUMPAD_SUBTRACT);
		Assert.assertEquals(0.9D, ui.getView().getZoom(), 0D);
		this.bridges.getUi().keyTyped('+', Key.EQUAL);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(Arrays.asList("typed - MINUS cancelled", "typed a NUMPAD_ADD cancelled", "typed s NUMPAD_SUBTRACT cancelled", "typed + EQUAL cancelled"), this.trace);
	}

	@Test
	public void keepsItsZoomAtItsLimits() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('+', Key.EQUAL);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		ui.zoom(0.1D);
		this.bridges.getUi().keyTyped('-', Key.MINUS);
		Assert.assertEquals(0.1D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(Arrays.asList("typed + EQUAL", "typed - MINUS"), this.trace);
	}

	@Test
	public void keepsItsZoomWithoutModifierKey() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getUi().keyTyped('-', Key.MINUS);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
	}

	@Test
	public void keepsItsZoomWhenNotZoomable() {
		final FixedUI ui = new FixedUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('-', Key.MINUS);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
	}

	@Test
	public void zoomsWithinItsBounds() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.load(1920D, 1080D);
		ui.zoom(0.5D);
		Assert.assertEquals(0.5D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(0.5D, ui.getZoomLevel().get(), 0D);
		Assert.assertEquals(3840D, ui.getScaledWidth().get(), 0D);
		Assert.assertEquals(2160D, ui.getScaledHeight().get(), 0D);
		ui.zoom(0.01D);
		Assert.assertEquals(0.1D, ui.getView().getZoom(), 0D);
		ui.zoom(3D);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(1920D, ui.getScaledWidth().get(), 0D);
		Assert.assertEquals(1080D, ui.getScaledHeight().get(), 0D);
	}

	@Test
	public void readsTheModifierKeysOnBothSides() {
		final Set<Key> keys = this.bridges.getWindow().getKeys();
		Assert.assertFalse(UI.isAltKeyDown());
		Assert.assertFalse(UI.isCtrlKeyDown());
		Assert.assertFalse(UI.isShiftKeyDown());
		keys.addAll(Arrays.asList(Key.LEFT_ALT, Key.LEFT_CONTROL, Key.LEFT_SHIFT));
		Assert.assertTrue(UI.isAltKeyDown());
		Assert.assertTrue(UI.isCtrlKeyDown());
		Assert.assertTrue(UI.isShiftKeyDown());
		keys.clear();
		keys.addAll(Arrays.asList(Key.RIGHT_ALT, Key.RIGHT_CONTROL, Key.RIGHT_SHIFT));
		Assert.assertTrue(UI.isAltKeyDown());
		Assert.assertTrue(UI.isCtrlKeyDown());
		Assert.assertTrue(UI.isShiftKeyDown());
	}

	@Test
	public void reloadsWithControlRInDevMode() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(2, ui.inits);
		Assert.assertEquals(Collections.singletonList("typed r R cancelled"), this.trace);
	}

	@Test
	public void reloadsWithF5InDevMode() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.zoom(0.5D);
		this.bridges.getUi().keyTyped('\0', Key.F5);
		Assert.assertEquals(2, ui.inits);
		Assert.assertEquals(0.5D, ui.getView().getZoom(), 0D);
	}

	@Test
	public void renewsItsInstanceWithControlShiftRInDevMode() {
		JOID.inst().setDevMode(true);
		final RenewUI ui = new RenewUI();
		this.bridges.open(ui);
		ui.clicks++;
		ui.zoom(0.5D);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.LEFT_SHIFT));
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertFalse(this.bridges.getUi().isOpened(ui));
		Assert.assertEquals(1, ui.inits);
		final RenewUI renewed = (RenewUI) this.bridges.getUi().getUiList().get(0);
		Assert.assertNotSame(ui, renewed);
		Assert.assertEquals(1, renewed.inits);
		Assert.assertEquals(0, renewed.clicks);
		Assert.assertEquals(1D, renewed.getView().getZoom(), 0D);
	}

	@Test
	public void keepsItsInstanceAndItsStateWithControlRInDevMode() {
		JOID.inst().setDevMode(true);
		final RenewUI ui = new RenewUI();
		this.bridges.open(ui);
		ui.clicks++;
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertTrue(this.bridges.getUi().isOpened(ui));
		Assert.assertEquals(2, ui.inits);
		Assert.assertEquals(1, ui.clicks);
	}

	@Test
	public void refusesToRenewAUIWithoutConstructorWithoutArgument() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		try {
			ui.renew();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The UI " + TraceUI.class.getName() + " has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead", exception.getMessage());
		}
		Assert.assertTrue(this.bridges.getUi().isOpened(ui));
	}

	@Test
	public void warnsWhenControlShiftRCannotRenewTheUI() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.LEFT_SHIFT));
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			this.bridges.getUi().keyTyped('r', Key.R);
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals("[JOID] The UI " + TraceUI.class.getName() + " has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead", output.toString().trim());
		Assert.assertTrue(this.bridges.getUi().isOpened(ui));
		Assert.assertEquals(1, ui.inits);
	}

	@Test
	public void refusesToRenewAClosedUI() {
		final RenewUI ui = new RenewUI();
		try {
			ui.renew();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The UI " + RenewUI.class.getName() + " is not open, only an open UI can be renewed", exception.getMessage());
		}
	}

	@Test
	public void ignoresTheDevKeysOutsideTheDevMode() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getUi().keyTyped('\0', Key.F5);
		this.bridges.getUi().keyTyped('\0', Key.F3);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyTyped('r', Key.R);
		Assert.assertEquals(1, ui.inits);
		Assert.assertNull(ui.getDevNode());
		Assert.assertEquals(Arrays.asList("typed \0 F5", "typed \0 F3", "typed r R"), this.trace);
	}

	@Test
	public void togglesItsDevPanelWithF3InDevMode() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertTrue(ui.getDevNode() instanceof DevNode);
		Assert.assertFalse(ui.getNodeList().contains(ui.getDevNode()));
		this.bridges.getUi().keyTyped('\0', Key.F3);
		Assert.assertTrue(ui.getNodeList().contains(ui.getDevNode()));
		this.bridges.getUi().keyTyped('\0', Key.F3);
		Assert.assertFalse(ui.getNodeList().contains(ui.getDevNode()));
		Assert.assertEquals(Arrays.asList("typed \0 F3 cancelled", "typed \0 F3 cancelled"), this.trace);
	}

	@Test
	public void keepsItsDevPanelAcrossItsReloads() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getUi().keyTyped('\0', Key.F3);
		final DevNode panel = (DevNode) ui.getDevNode();
		ui.reload();
		Assert.assertSame(panel, ui.getDevNode());
		Assert.assertTrue(ui.getNodeList().contains(panel));
		this.bridges.frames(3);
		Assert.assertTrue(panel.getReloadAnimator().getValue() > 0F);
	}

	@Test
	public void replacesItsDetachedDevPanelOnReload() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		final Node panel = ui.getDevNode();
		ui.reload();
		Assert.assertNotSame(panel, ui.getDevNode());
		Assert.assertFalse(ui.getNodeList().contains(ui.getDevNode()));
	}

	@Test
	public void ignoresF3WithoutDevPanel() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		JOID.inst().setDevMode(true);
		this.trace.clear();
		this.bridges.getUi().keyTyped('\0', Key.F3);
		Assert.assertNull(ui.getDevNode());
		Assert.assertEquals(Collections.singletonList("typed \0 F3"), this.trace);
	}

	@Test
	public void zoomsWithAltAndTheWheelInDevMode() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_ALT);
		this.bridges.scroll(-1D);
		Assert.assertEquals(0.988D, ui.getView().getZoom(), 0.0001D);
		this.bridges.getWindow().getKeys().add(Key.LEFT_SHIFT);
		this.bridges.scroll(-1D);
		Assert.assertEquals(0.868D, ui.getView().getZoom(), 0.0001D);
		Assert.assertTrue(this.trace.isEmpty());
		Assert.assertFalse(ui.onMouseScroll(0D));
		Assert.assertEquals(Collections.singletonList("scrolled 0.0"), this.trace);
	}

	@Test
	public void scrollsWithAltOutsideTheDevMode() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_ALT);
		this.bridges.scroll(-1D);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(Collections.singletonList("scrolled -1.0"), this.trace);
	}

	@Test
	public void printsItsLoadTimeWithItsProfilerInDevMode() {
		JOID.inst().setDevMode(true);
		final String output = UITest.out(() -> new ProfiledUI(this.trace).load(1920D, 1080D));
		Assert.assertTrue(output, output.contains("Starting load..."));
		Assert.assertTrue(output, output.contains("Load completed in "));
	}

	@Test
	public void printsItsLoadTimeOnlyOnItsFirstLoad() {
		JOID.inst().setDevMode(true);
		final ProfiledUI ui = new ProfiledUI(this.trace);
		UITest.out(() -> ui.load(1920D, 1080D));
		Assert.assertEquals("", UITest.out(() -> ui.load(1366D, 768D)));
	}

	@Test
	public void loadsQuietlyWithoutItsProfiler() {
		JOID.inst().setDevMode(true);
		Assert.assertEquals("", UITest.out(() -> new TraceUI(this.trace).load(1920D, 1080D)));
	}

	@Test
	public void warnsAboutASlowFrameWithItsProfilerInDevMode() {
		JOID.inst().setDevMode(true);
		final ProfiledUI ui = new ProfiledUI(this.trace);
		UITest.out(() -> this.bridges.open(ui));
		ui.pause = 20L;
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			this.bridges.frame();
		} finally {
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().contains("[!] Frame took "));
	}

	@Test
	public void closesAtOnceWithoutTransition() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertTrue(ui.onClose());
		Assert.assertEquals(Arrays.asList("close", "detach node"), this.trace);
	}

	@Test
	public void staysOpenWhenItsHookRefusesToClose() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		ui.closeable = false;
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertFalse(ui.onClose());
		Assert.assertEquals(Collections.singletonList("close"), this.trace);
	}

	@Test
	public void playsItsOutTransitionBeforeClosing() {
		final PopupUI ui = new PopupUI(this.trace, new TraceNode("node", this.trace, 0));
		this.bridges.open(ui).frames(20);
		Assert.assertFalse(ui.onClose());
		Assert.assertTrue(ui.getTransition().getOut().isRunning());
		Assert.assertFalse(ui.onClose());
		Assert.assertTrue(this.bridges.getUi().isOpened(ui));
		Assert.assertFalse(this.trace.contains("detach node"));
		this.bridges.frames(20);
		Assert.assertFalse(this.bridges.getUi().isOpened(ui));
		Assert.assertEquals(1, Collections.frequency(this.trace, "close"));
		Assert.assertEquals(1, Collections.frequency(this.trace, "detach node"));
	}

	@Test
	public void closesAtOnceWithItsOutTransitionDisabled() {
		final InPopupUI ui = new InPopupUI(this.trace);
		this.bridges.open(ui).frames(20);
		Assert.assertTrue(ui.onClose());
	}

	@Test
	public void measuresItsFrameTimeOnTheClock() {
		final TraceUI ui = new TraceUI(this.trace);
		Assert.assertEquals(0D, ui.getFrameTime(), 0D);
		this.bridges.open(ui);
		Assert.assertEquals(1000D / 60D, ui.getFrameTime(), 0D);
		this.bridges.frame();
		Assert.assertEquals(16D, ui.getFrameTime(), 0D);
		this.bridges.getClock().advance(34L);
		this.bridges.frame();
		Assert.assertEquals(50D, ui.getFrameTime(), 0D);
	}

	@Test
	public void countsItsFramesEverySecond() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui).frames(62);
		Assert.assertEquals(0D, ui.getFps(), 0D);
		this.bridges.frame();
		Assert.assertEquals(62.5D, ui.getFps(), 0.0001D);
	}

	@Test
	public void drawsItsBackgroundOverTheWholeWindow() {
		this.bridges.resize(1366, 768).open(new TraceUI(this.trace));
		final Draw background = UITest.background(this.bridges);
		Assert.assertSame(background, this.bridges.getRender().getDraws().get(0));
		Assert.assertEquals(192F / 255F, background.getAlpha(), 0.0001F);
		Assert.assertEquals(0D, background.getLeft(), 0.001D);
		Assert.assertEquals(0D, background.getTop(), 0.001D);
		Assert.assertEquals(1366D, background.getRight(), 0.001D);
		Assert.assertEquals(768D, background.getBottom(), 0.001D);
	}

	@Test
	public void drawsNoBackgroundOnceDisabled() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.getData().setBackground(false);
		this.bridges.open(ui);
		Assert.assertTrue(this.bridges.getRender().getDraws(16F / 255F, 16F / 255F, 16F / 255F).isEmpty());
	}

	@Test
	public void fadesItsBackgroundWithItsTransitions() {
		final PopupUI ui = new PopupUI(this.trace);
		this.bridges.open(ui);
		Assert.assertEquals(0F, UITest.background(this.bridges).getAlpha(), 0F);
		this.bridges.frames(20);
		Assert.assertEquals(192F / 255F, UITest.background(this.bridges).getAlpha(), 0.0001F);
		ui.onClose();
		this.bridges.frames(4);
		final float alpha = UITest.background(this.bridges).getAlpha();
		Assert.assertTrue(String.valueOf(alpha), alpha > 0F && alpha < 192F / 255F);
	}

	@Test
	public void drawsItsNodesAroundItsHooksByIndex() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("top", this.trace, 100), new TraceNode("middle", this.trace, 50), new TraceNode("base", this.trace, 0), new TraceNode("back", this.trace, -1));
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update back", "update base", "update middle", "update top", "update", "background", "draw back", "pre", "draw base", "draw middle", "post", "draw top"), this.trace);
	}

	@Test
	public void raisesTheNodesDrawnAfterItsRenderPipelineLevel() {
		final TraceUI ui = new TraceUI(this.trace, new DepthNode(this.trace, 0, 30D), new DepthNode(this.trace, 1, 0D));
		ui.getData().setBackground(false);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update", "background", "pre", "depth 0.0", "depth 30.0", "post"), this.trace);
		Assert.assertEquals(30D, ui.getRenderPipelineLevel(), 0D);
		ui.setRenderPipelineLevel(5D);
		Assert.assertEquals(5D, ui.getRenderPipelineLevel(), 0D);
	}

	@Test
	public void wrapsItsDrawInItsTransition() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		ui.setTransition(new RecordingTransition(this.trace));
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("in init", "in start", "out init", "update node", "update", "background", "in pre", "pre", "draw node", "post", "in post"), this.trace);
		this.trace.clear();
		Assert.assertFalse(ui.onClose());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("close", "out start", "update node", "update", "background", "in pre", "out pre", "pre", "draw node", "post", "in post", "out post"), this.trace);
	}

	@Test
	public void leavesTheDisabledStatesOfItsTransitionAside() {
		final TraceUI ui = new TraceUI(this.trace);
		final RecordingTransition transition = new RecordingTransition(this.trace);
		transition.getIn().disable();
		transition.getOut().disable();
		ui.setTransition(transition);
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("update", "background", "pre", "post"), this.trace);
		Assert.assertTrue(ui.onClose());
	}

	@Test
	public void playsATransitionWithoutOutState() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new PartialTransition(new RecordingIn(this.trace), null));
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("in init", "in start", "update", "background", "in pre", "pre", "post", "in post"), this.trace);
		this.trace.clear();
		Assert.assertTrue(ui.onClose());
		Assert.assertEquals(Collections.singletonList("close"), this.trace);
	}

	@Test
	public void playsATransitionWithoutInState() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new PartialTransition(null, new RecordingOut(this.trace)));
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("out init", "update", "background", "pre", "post"), this.trace);
		this.trace.clear();
		Assert.assertFalse(ui.onClose());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("close", "out start", "update", "background", "out pre", "pre", "post", "out post"), this.trace);
		this.bridges.frames(10);
		Assert.assertFalse(this.bridges.getUi().isOpened(ui));
	}

	@Test
	public void endsItsTransitionOnTheFrameItsTimelineFinishes() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new RecordingTransition(this.trace));
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getClock().advance(200L);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update", "background", "in pre", "pre", "post", "in post"), this.trace);
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update", "background", "pre", "post"), this.trace);
	}

	@Test
	public void endsItsTransitionEvenWhenItsDrawFails() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new RecordingTransition(this.trace));
		this.bridges.open(ui);
		ui.failure = new IllegalStateException("broken");
		this.trace.clear();
		try {
			ui.draw(0D, 0D);
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("broken", e.getMessage());
		}
		Assert.assertEquals(Arrays.asList("background", "in pre", "in post"), this.trace);
	}

	@Test
	public void showsTheTooltipOfItsTopmostHoveredNode() {
		final TraceUI ui = new TraceUI(this.trace, RectNode.create(100D, 100D, 200D, 200D).hover(() -> "below"), RectNode.create(150D, 150D, 200D, 200D).zindex(1).hover(() -> "above"));
		this.bridges.open(ui).move(200D, 200D).frames(2);
		Assert.assertTrue(this.trace.contains("hover [above] 200.0 200.0"));
		Assert.assertFalse(this.trace.contains("hover [below] 200.0 200.0"));
	}

	@Test
	public void showsNoTooltipBelowAnotherUi() {
		final TraceUI ui = new TraceUI(this.trace, RectNode.create(100D, 100D, 200D, 200D).hover(() -> "below"));
		this.bridges.open(ui).open(new TraceUI(new ArrayList<>())).move(200D, 200D).frames(2);
		Assert.assertFalse(ui.isOnTop());
		Assert.assertFalse(this.trace.contains("hover [below] 200.0 200.0"));
	}

	@Test
	public void handsItsTooltipsToItsBridge() {
		final HoverBridge bridge = new HoverBridge();
		final NodeUI ui = new NodeUI(RectNode.create(100D, 100D, 200D, 200D).hover(() -> "Save"));
		BridgeHandler.UI.register(bridge);
		try {
			bridge.add(ui);
			this.bridges.move(150D, 160D).frame();
			bridge.draw();
			Assert.assertTrue(ui.isOnTop());
			Assert.assertEquals(Collections.singletonList("[Save] 150.0 160.0"), bridge.hovers);
		} finally {
			BridgeHandler.UI.unregister(bridge);
		}
	}

	@Test
	public void followsTheInterfaceScaleOfItsBridge() {
		final HoverBridge bridge = new HoverBridge();
		final NodeUI ui = new NodeUI(RectNode.create(860D, 490D, 200D, 100D).color(new Color(0.2F, 0.4F, 0.6F, 1F)));
		bridge.interfaceScale = 0.5D;
		BridgeHandler.UI.register(bridge);
		try {
			bridge.add(ui);
			this.bridges.move(0D, 0D).frame();
			bridge.draw();
			Assert.assertEquals(0.5D, ui.getView().getInterfaceScale(), 0D);
			Assert.assertEquals(3840D, ui.getScaledWidth().get(), 0D);
			Assert.assertEquals(2160D, ui.getScaledHeight().get(), 0D);
			Assert.assertEquals(-960D, ui.getMouseX(), 0D);
			Assert.assertEquals(-540D, ui.getMouseY(), 0D);
			final Draw draw = this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).get(0);
			Assert.assertEquals(910D, draw.getLeft(), 0.001D);
			Assert.assertEquals(515D, draw.getTop(), 0.001D);
			Assert.assertEquals(1010D, draw.getRight(), 0.001D);
			Assert.assertEquals(565D, draw.getBottom(), 0.001D);
			ui.zoom(3D);
			Assert.assertEquals(2D, ui.getView().getZoom(), 0D);
			Assert.assertEquals(1920D, ui.getScaledWidth().get(), 0D);
		} finally {
			BridgeHandler.UI.unregister(bridge);
		}
	}

	@Test
	public void keepsTheFittedSizeWhenItsScaleIsInactive() {
		final HoverBridge bridge = new HoverBridge();
		final NodeUI ui = new NodeUI(RectNode.create(860D, 490D, 200D, 100D));
		bridge.interfaceScale = 0.5D;
		ui.getScale().setActive(false);
		BridgeHandler.UI.register(bridge);
		try {
			bridge.add(ui);
			this.bridges.move(0D, 0D).frame();
			bridge.draw();
			Assert.assertEquals(1D, ui.getView().getInterfaceScale(), 0D);
			Assert.assertEquals(1920D, ui.getScaledWidth().get(), 0D);
			Assert.assertEquals(0, bridge.scaleQueries);
			ui.getScale().setActive(true);
			bridge.draw();
			Assert.assertEquals(0.5D, ui.getView().getInterfaceScale(), 0D);
			Assert.assertEquals(1, bridge.scaleQueries);
		} finally {
			BridgeHandler.UI.unregister(bridge);
		}
	}

	@Test
	public void capsTheInterfaceScaleAtItsLimit() {
		final HoverBridge bridge = new HoverBridge();
		final LimitedScaleUI ui = new LimitedScaleUI(this.trace);
		bridge.interfaceScale = 2D;
		BridgeHandler.UI.register(bridge);
		try {
			bridge.add(ui);
			this.bridges.move(0D, 0D).frame();
			bridge.draw();
			Assert.assertEquals(0.75D, ui.getView().getInterfaceScale(), 0D);
			bridge.interfaceScale = 0.5D;
			bridge.draw();
			Assert.assertEquals(0.5D, ui.getView().getInterfaceScale(), 0D);
			ui.getScale().setLimited(false);
			bridge.interfaceScale = 2D;
			bridge.draw();
			Assert.assertEquals(2D, ui.getView().getInterfaceScale(), 0D);
		} finally {
			BridgeHandler.UI.unregister(bridge);
		}
	}

	@Test
	public void drawsBelowEveryUiWithoutBridge() {
		final TraceUI ui = new TraceUI(this.trace, RectNode.create(100D, 100D, 200D, 200D).color(new Color(0.2F, 0.4F, 0.6F, 1F)).hover(() -> "Save"));
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			Assert.assertNull(ui.getBridge());
			ui.load(1920D, 1080D);
			ui.draw(150D, 150D);
			ui.draw(150D, 150D);
		} finally {
			BridgeHandler.UI.register(this.bridges.getUi());
		}
		Assert.assertFalse(ui.isOnTop());
		Assert.assertEquals(1D, ui.getView().getInterfaceScale(), 0D);
		Assert.assertEquals(2, this.bridges.getRender().getDraws(0.2F, 0.4F, 0.6F).size());
		Assert.assertFalse(this.trace.contains("hover [Save] 150.0 150.0"));
	}

	@Test
	public void masksItsDrawingInsideItsArea() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getRender().getDraws().clear();
		final List<String> stencils = new ArrayList<>();
		ui.mask(10D, 20D, 30D, 40D, () -> stencils.add(UITest.stencil(this.bridges)));
		Assert.assertEquals(Collections.singletonList("EQUAL 1"), stencils);
		Assert.assertFalse(this.bridges.getRender().getState().isStencilTest());
		Assert.assertTrue(ui.getStencilStack().isEmpty());
		final Draw mask = this.bridges.getRender().getDraws(1F, 0F, 0F).get(0);
		Assert.assertEquals(10D, mask.getLeft(), 0.001D);
		Assert.assertEquals(20D, mask.getTop(), 0.001D);
		Assert.assertEquals(40D, mask.getRight(), 0.001D);
		Assert.assertEquals(60D, mask.getBottom(), 0.001D);
	}

	@Test
	public void drawsWithoutMaskOnceDisabled() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getRender().getDraws().clear();
		final List<String> stencils = new ArrayList<>();
		ui.mask(10D, 20D, 30D, 40D, () -> stencils.add(UITest.stencil(this.bridges)), false);
		Assert.assertEquals(Collections.singletonList("off"), stencils);
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void nestsItsMasks() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getRender().getDraws().clear();
		ui.startMask(0D, 0D, 100D, 100D);
		ui.startMask(10D, 10D, 50D, 50D);
		Assert.assertEquals(2, ui.getStencilStack().size());
		Assert.assertEquals("EQUAL 2", UITest.stencil(this.bridges));
		ui.stopMask();
		Assert.assertEquals("EQUAL 1", UITest.stencil(this.bridges));
		ui.stopMask();
		Assert.assertFalse(this.bridges.getRender().getState().isStencilTest());
		Assert.assertEquals(2, this.bridges.getRender().getDraws(1F, 0F, 0F).size());
	}

	@Test
	public void nestsAResourceMaskInsideAnotherMask() {
		final TraceUI ui = new TraceUI(this.trace);
		final Resource resource = Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png"));
		this.bridges.open(ui);
		ui.startMask(0D, 0D, 100D, 100D);
		ui.startMask(resource, 10D, 10D, 50D, 50D);
		Assert.assertEquals("EQUAL 2", UITest.stencil(this.bridges));
		ui.stopMask();
		Assert.assertEquals("EQUAL 1", UITest.stencil(this.bridges));
		ui.stopMask();
		Assert.assertEquals("off", UITest.stencil(this.bridges));
	}

	@Test
	public void stopsItsMaskWhenItsDrawingFails() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		try {
			ui.mask(10D, 20D, 30D, 40D, () -> {
				throw new IllegalStateException("broken");
			});
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("broken", e.getMessage());
		}
		Assert.assertTrue(ui.getStencilStack().isEmpty());
		Assert.assertFalse(this.bridges.getRender().getState().isStencilTest());
	}

	@Test
	public void masksItsDrawingInsideAResource() {
		final TraceUI ui = new TraceUI(this.trace);
		final Resource resource = Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png"));
		this.bridges.open(ui);
		this.bridges.getRender().alphaTest(0.25F);
		final List<String> stencils = new ArrayList<>();
		ui.mask(resource, 10D, 20D, 30D, 40D, () -> stencils.add(UITest.stencil(this.bridges)));
		ui.mask(resource, 10D, 20D, 30D, 40D, () -> stencils.add(UITest.stencil(this.bridges)), false);
		Assert.assertEquals(Arrays.asList("EQUAL 1", "off"), stencils);
		Assert.assertEquals(0.25F, this.bridges.getRender().getState().getAlphaThreshold(), 0F);
		Assert.assertTrue(ui.getStencilStack().isEmpty());
	}

	@Test
	public void releasesItsResourceMaskWhenItsDrawingFails() {
		final TraceUI ui = new TraceUI(this.trace);
		final Resource resource = Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png"));
		this.bridges.open(ui);
		try {
			ui.mask(resource, 10D, 20D, 30D, 40D, () -> {
				throw new IllegalStateException("broken");
			});
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("broken", e.getMessage());
		}
		Assert.assertTrue(ui.getStencilStack().isEmpty());
		Assert.assertFalse(this.bridges.getRender().getState().isStencilTest());
	}

	@Test(expected = EmptyStackException.class)
	public void refusesToStopAMaskNeverStarted() {
		new TraceUI(this.trace).stopMask();
	}

	@Test
	public void runsATaskOnTheNextFrame() {
		final AtomicInteger runs = new AtomicInteger();
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.schedule(runs::incrementAndGet);
		Assert.assertEquals(0, runs.get());
		this.bridges.frames(3);
		Assert.assertEquals(1, runs.get());
		Assert.assertTrue(ui.getScheduledTaskList().isEmpty());
	}

	@Test
	public void runsATaskOnceItsDelayElapsed() {
		final AtomicInteger runs = new AtomicInteger();
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.schedule(runs::incrementAndGet, 100L);
		this.bridges.frames(6);
		Assert.assertEquals(0, runs.get());
		this.bridges.frame();
		Assert.assertEquals(1, runs.get());
		this.bridges.frames(5);
		Assert.assertEquals(1, runs.get());
	}

	@Test
	public void repeatsATaskEveryPeriod() {
		final AtomicInteger runs = new AtomicInteger();
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.schedule(runs::incrementAndGet, 0L, 48L);
		this.bridges.frames(7);
		Assert.assertEquals(3, runs.get());
		Assert.assertEquals(1, ui.getScheduledTaskList().size());
	}

	@Test
	public void movesAThirdOfTheWayAtSixtyFramesPerSecond() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		Assert.assertEquals(10D, ui.lerpByFramerate(0D, 30D, 1D, 0.5D, true), 0D);
		Assert.assertEquals(20D, ui.lerpByFramerate(30D, 0D, 1D, 0.5D, true), 0D);
		Assert.assertEquals(15D, ui.lerpByFramerate(0D, 30D, 1.5D, 0.5D, true), 0D);
	}

	@Test
	public void movesWithTheFrameTimeMeasuredOnTheClock() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui).frame();
		Assert.assertEquals(9.6D, ui.lerpByFramerate(0D, 30D, 1D, 0.5D, true), 0.0001D);
	}

	@Test
	public void staysStillBeforeItsFirstFrame() {
		Assert.assertEquals(0D, new TraceUI(this.trace).lerpByFramerate(0D, 30D, 1D, 0.5D, true), 0D);
	}

	@Test
	public void snapsToItsTargetOnceCloseEnough() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		Assert.assertEquals(30D, ui.lerpByFramerate(29.8D, 30D, 1D, 0.5D, true), 0D);
		Assert.assertEquals(29.8D, ui.lerpByFramerate(29.8D, 30D, 1D, 0.5D, false), 0D);
		Assert.assertEquals(30D, ui.lerpByFramerate(30D, 30D, 1D, 0D, false), 0D);
	}

	@Test
	public void neverMovesPastItsTarget() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		Assert.assertEquals(30D, ui.lerpByFramerate(0D, 30D, 100D, 0.5D, true), 0D);
		Assert.assertEquals(-30D, ui.lerpByFramerate(0D, -30D, 100D, 0.5D, true), 0D);
	}

	@Test
	public void keepsOneLocalStore() {
		final TraceUI ui = new TraceUI(this.trace);
		final LocalStore store = ui.useStore(LocalStore.class);
		Assert.assertSame(store, ui.useStore(LocalStore.class));
		Assert.assertSame(store, ui.getStoreMap().get(LocalStore.class));
		Assert.assertNotSame(store, new TraceUI(this.trace).useStore(LocalStore.class));
	}

	@Test
	public void sharesAGlobalStoreWithTheOtherUis() {
		final TraceUI ui = new TraceUI(this.trace);
		final GlobalStore store = ui.useStore(GlobalStore.class);
		try {
			Assert.assertSame(store, new TraceUI(this.trace).useStore(GlobalStore.class));
			Assert.assertSame(store, UIStoreHook.useStore(GlobalStore.class));
			Assert.assertTrue(ui.getStoreMap().isEmpty());
		} finally {
			UIStoreHook.destroyStore(store);
		}
	}

	@Test
	public void buildsAStoreFromItsArguments() {
		Assert.assertEquals("shop", new TraceUI(this.trace).useStore(NamedStore.class, "shop").name);
	}

	@Test
	public void destroysItsLocalStoresOnClose() {
		final TraceUI ui = new TraceUI(this.trace);
		final LocalStore store = ui.useStore(LocalStore.class);
		ui.properlyClose();
		Assert.assertTrue(store.destroyed);
	}

	@Test
	public void createsItsLocalStoresAgainOnceReopened() {
		final TraceUI ui = new TraceUI(this.trace);
		final LocalStore store = ui.useStore(LocalStore.class);
		ui.properlyClose();
		Assert.assertTrue(ui.getStoreMap().isEmpty());
		final LocalStore reopened = ui.useStore(LocalStore.class);
		Assert.assertNotSame(store, reopened);
		Assert.assertFalse(reopened.destroyed);
	}

	@Test
	public void savesItsPropertiesOnClose() throws IOException {
		final PropertyUI ui = new PropertyUI();
		ui.load(1920D, 1080D);
		ui.title = "Shop";
		ui.properlyClose();
		final File file = new File(new File(this.folder.getRoot(), "property"), PropertyUI.class.getName() + ".property");
		final String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		Assert.assertTrue(json, json.contains("\"title\":\"Shop\""));
	}

	@Test
	public void restoresItsPropertiesOnItsFirstLoad() {
		final PropertyUI saved = new PropertyUI();
		saved.title = "Shop";
		saved.properlyClose();
		final PropertyUI loaded = new PropertyUI();
		Assert.assertEquals("Home", loaded.title);
		loaded.load(1920D, 1080D);
		Assert.assertEquals("Shop", loaded.title);
	}

	@Test
	public void keepsItsPropertiesAcrossAReload() {
		final PropertyUI ui = new PropertyUI();
		ui.properlyClose();
		ui.load(1920D, 1080D);
		ui.title = "Shop";
		ui.reload();
		Assert.assertEquals("Shop", ui.title);
	}

	@Test
	public void leavesItsStaticFieldsOutOfItsProperties() throws IOException {
		final StaticPropertyUI ui = new StaticPropertyUI();
		ui.properlyClose();
		final File file = new File(new File(this.folder.getRoot(), "property"), StaticPropertyUI.class.getName() + ".property");
		final String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		Assert.assertEquals("{\"title\":\"" + ui.title + "\"}", json);
		Assert.assertEquals("Light", StaticPropertyUI.theme);
	}

	@Test
	public void watchesNoFileOutsideTheDevMode() {
		final HotReloadUI ui = new HotReloadUI(new AtomicInteger());
		ui.load(1920D, 1080D);
		Assert.assertNull(ui.getFileMonitor());
	}

	@Test
	public void watchesNoFileWithoutHotReload() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		ui.load(1920D, 1080D);
		Assert.assertNull(ui.getFileMonitor());
	}

	@Test
	public void reloadsOnItsNextFrameOnceItsJarChanges() throws Exception {
		final File location = new File(this.folder.newFolder("classes"), "ui.jar");
		final AtomicInteger inits = new AtomicInteger();
		Files.write(location.toPath(), new byte[] {1});
		final UI ui = UITest.hotReloaded(location, inits);
		JOID.inst().setDevMode(true);
		UITest.out(() -> ui.load(1920D, 1080D));
		try {
			final BlockingQueue<String> changes = UITest.listen(ui);
			Files.write(location.toPath(), new byte[] {1, 2});
			UITest.await(changes, "file change ui.jar");
			UITest.await(changes, "stop");
			Assert.assertTrue(ui.isReloadPending());
			Assert.assertEquals(1, inits.get());
			final String output = UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertFalse(ui.isReloadPending());
			Assert.assertEquals(2, inits.get());
			Assert.assertTrue(ui.isInitialized());
			Assert.assertTrue(output, output.contains("Reload completed in "));
		} finally {
			ui.properlyClose();
		}
	}

	@Test
	public void reloadsOnItsNextFrameOnceAClassOfItsFolderChanges() throws Exception {
		final File location = this.folder.newFolder("class folder \u00E9");
		final File type = new File(new File(location, "shop"), "ShopNode.class");
		final AtomicInteger inits = new AtomicInteger();
		Assert.assertTrue(type.getParentFile().mkdir());
		final UI ui = UITest.hotReloaded(location, inits);
		JOID.inst().setDevMode(true);
		UITest.out(() -> ui.load(1920D, 1080D));
		try {
			final BlockingQueue<String> changes = UITest.listen(ui);
			Files.write(type.toPath(), new byte[] {1});
			UITest.await(changes, "file create ShopNode.class");
			UITest.await(changes, "stop");
			Assert.assertTrue(ui.isReloadPending());
			Assert.assertEquals(1, inits.get());
			UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertEquals(2, inits.get());
			Files.write(type.toPath(), new byte[] {1, 2});
			Assert.assertTrue(type.setLastModified(type.lastModified() + 10000L));
			UITest.await(changes, "file change ShopNode.class");
			UITest.await(changes, "stop");
			UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertEquals(3, inits.get());
		} finally {
			ui.properlyClose();
		}
	}

	@Test
	public void ignoresTheOtherFilesOfItsClassFolder() throws Exception {
		final File location = this.folder.newFolder("classes");
		final File other = new File(location, "logo.png");
		final AtomicInteger inits = new AtomicInteger();
		final UI ui = UITest.hotReloaded(location, inits);
		JOID.inst().setDevMode(true);
		UITest.out(() -> ui.load(1920D, 1080D));
		try {
			final BlockingQueue<String> changes = UITest.listen(ui);
			Files.write(other.toPath(), new byte[] {1});
			UITest.await(changes, "file create logo.png");
			UITest.await(changes, "stop");
			Assert.assertFalse(ui.isReloadPending());
			UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertEquals(1, inits.get());
		} finally {
			ui.properlyClose();
		}
	}

	@Test
	public void ignoresTheOtherChangesOfItsFolder() throws Exception {
		final File folder = this.folder.newFolder("classes");
		final File location = new File(folder, "ui.jar");
		final File other = new File(folder, "other.txt");
		final File directory = new File(folder, "assets");
		final AtomicInteger inits = new AtomicInteger();
		Files.write(location.toPath(), new byte[] {1});
		final UI ui = UITest.hotReloaded(location, inits);
		JOID.inst().setDevMode(true);
		UITest.out(() -> ui.load(1920D, 1080D));
		try {
			final BlockingQueue<String> changes = UITest.listen(ui);
			Files.write(other.toPath(), new byte[] {1});
			Assert.assertTrue(directory.mkdir());
			UITest.await(changes, "file create other.txt", "directory create assets");
			Files.write(other.toPath(), new byte[] {1, 2});
			Assert.assertTrue(directory.setLastModified(directory.lastModified() - 10000L));
			UITest.await(changes, "file change other.txt", "directory change assets");
			Assert.assertTrue(other.delete());
			Assert.assertTrue(directory.delete());
			UITest.await(changes, "file delete other.txt", "directory delete assets");
			UITest.await(changes, "stop");
			Assert.assertFalse(ui.isReloadPending());
			UITest.out(() -> ui.draw(0D, 0D));
			Assert.assertEquals(1, inits.get());
		} finally {
			ui.properlyClose();
		}
	}

	@Test
	public void stopsWatchingItsFileOnceClosed() throws Exception {
		final File location = new File(this.folder.newFolder("classes"), "ui.jar");
		Files.write(location.toPath(), new byte[] {1});
		final UI ui = UITest.hotReloaded(location, new AtomicInteger());
		JOID.inst().setDevMode(true);
		UITest.out(() -> ui.load(1920D, 1080D));
		Assert.assertNotNull(ui.getFileMonitor());
		ui.properlyClose();
		final long deadline = System.currentTimeMillis() + 5000L;
		while (ui.getFileMonitor() != null && System.currentTimeMillis() < deadline) {
			Thread.sleep(10L);
		}
		Assert.assertNull(ui.getFileMonitor());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutButton() {
		new TraceUI(this.trace).onMousePressed(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyWithoutKey() {
		new TraceUI(this.trace).onKeyPressed('a', null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeybindWithoutAction() {
		new TraceUI(this.trace).keybind(null, Key.R);
	}

	@Test(expected = NullPointerException.class)
	public void refusesATaskWithoutAction() {
		new TraceUI(this.trace).schedule(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMaskWithoutDrawing() {
		new TraceUI(this.trace).mask(0D, 0D, 10D, 10D, null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMaskWithoutResource() {
		new TraceUI(this.trace).mask(null, 0D, 0D, 10D, 10D, () -> {});
	}

	@Test(expected = NullPointerException.class)
	public void refusesAStoreWithoutClass() {
		new TraceUI(this.trace).useStore(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesATooltipWithoutLines() {
		new NodeUI(RectNode.create(0D, 0D, 10D, 10D)).drawHover(null, 0D, 0D);
	}

	@Test
	public void detachesItsPreviousNodesOnReload() {
		final ReloadUI ui = new ReloadUI(this.trace);
		this.bridges.open(ui);
		ui.reload();
		Assert.assertEquals(Arrays.asList("init 1", "detach 1", "init 2"), this.trace);
	}

	private static Draw background(final HeadlessBridges bridges) {
		return bridges.getRender().getDraws(16F / 255F, 16F / 255F, 16F / 255F).get(0);
	}

	private static float depth() {
		return ((RenderBridge) BridgeHandler.RENDER.get()).getModelView().getMatrix()[14];
	}

	private static String stencil(final HeadlessBridges bridges) {
		final RenderState state = bridges.getRender().getState();
		return state.isStencilTest() ? state.getStencilFunction() + " " + state.getStencilReference() : "off";
	}

	private static String out(final Runnable runnable) {
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output, true));
		try {
			runnable.run();
		} finally {
			System.setOut(previous);
		}
		return output.toString();
	}

	private static UI hotReloaded(final File location, final AtomicInteger inits) throws Exception {
		final byte[] bytes = IOUtils.toByteArray(HotReloadUI.class.getResource("HotReloadUI.class"));
		final Class<?> clazz = new LocationClassLoader(location).define(HotReloadUI.class.getName(), bytes);
		return (UI) clazz.getConstructor(AtomicInteger.class).newInstance(inits);
	}

	private static BlockingQueue<String> listen(final UI ui) {
		final ChangeListener listener = new ChangeListener();
		for (final FileAlterationObserver observer : ui.getFileMonitor().getObservers()) {
			observer.addListener(listener);
		}
		return listener.changes;
	}

	private static void await(final BlockingQueue<String> changes, final String... expected) throws InterruptedException {
		final Set<String> missing = new HashSet<>(Arrays.asList(expected));
		while (!missing.isEmpty()) {
			final String change = changes.poll(10L, TimeUnit.SECONDS);
			Assert.assertNotNull(missing.toString(), change);
			missing.remove(change);
		}
	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static class TraceUI extends UI {

		private final List<String> trace;
		private final Node[]       nodes;

		private int     inits;
		private Key[]   keybind;
		private boolean cancel;
		private boolean closeable = true;

		private RuntimeException failure;

		public TraceUI(final List<String> trace, final Node... nodes) {
			this.trace = trace;
			this.nodes = nodes;
		}

		@Override
		public void init() {
			this.inits++;
			super.add(this.nodes);
			if (this.keybind != null) {
				super.keybind(() -> this.trace.add("keybind"), this.keybind);
			}
		}

		@Override
		public boolean close() {
			this.trace.add("close");
			return this.closeable;
		}

		@Override
		public void update() {
			this.trace.add("update");
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("pressed " + clickType + " " + mouseX + " " + mouseY);
			this.cancel(context);
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
			this.trace.add("dragged " + clickType + " " + deltaTime);
			this.cancel(context);
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("released " + clickType);
			this.cancel(context);
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final double value, final @NonNull InternalContext context) {
			this.trace.add("scrolled " + value);
			this.cancel(context);
		}

		@Override
		public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
			this.trace.add("typed " + c + " " + key + (context.isCancelled() ? " cancelled" : ""));
			this.cancel(context);
		}

		@Override
		public void drawBackground(final double mouseX, final double mouseY) {
			this.trace.add("background");
		}

		@Override
		public void preDraw(final double mouseX, final double mouseY) {
			if (this.failure != null) {
				throw this.failure;
			}

			this.trace.add("pre");
		}

		@Override
		public void postDraw(final double mouseX, final double mouseY) {
			this.trace.add("post");
		}

		@Override
		public void drawHover(final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
			this.trace.add("hover " + lines + " " + mouseX + " " + mouseY);
		}

		private void cancel(final InternalContext context) {
			if (this.cancel) {
				context.cancel();
			}
		}

	}

	@UIData(zoomable = false)
	public static final class FixedUI extends TraceUI {

		public FixedUI(final List<String> trace) {
			super(trace);
		}

	}

	@UIDataPopup(active = true)
	public static final class PopupUI extends TraceUI {

		public PopupUI(final List<String> trace, final Node... nodes) {
			super(trace, nodes);
		}

	}

	@UIData(zlevel = 1D)
	@UIDataPopup(active = false)
	public static final class AnnotatedUI extends TraceUI {

		public AnnotatedUI(final List<String> trace) {
			super(trace);
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.IN)
	public static final class InPopupUI extends TraceUI {

		public InPopupUI(final List<String> trace) {
			super(trace);
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.OUT)
	public static final class OutPopupUI extends TraceUI {

		public OutPopupUI(final List<String> trace) {
			super(trace);
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class StillPopupUI extends TraceUI {

		public StillPopupUI(final List<String> trace) {
			super(trace);
		}

	}

	@UIDataPopup(active = true)
	@UIDataOverlay(active = true)
	public static final class PopupOverlayUI extends UI {}

	@UIDataScale(limited = true, limit = 0.75D)
	public static final class LimitedScaleUI extends TraceUI {

		public LimitedScaleUI(final List<String> trace) {
			super(trace);
		}

	}

	@UIDataDebug(profiler = true, hotreload = false)
	public static final class ProfiledUI extends TraceUI {

		private long pause;

		public ProfiledUI(final List<String> trace) {
			super(trace);
		}

		@Override
		public void postDraw(final double mouseX, final double mouseY) {
			super.postDraw(mouseX, mouseY);
			try {
				Thread.sleep(this.pause);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	public static final class RenewUI extends UI {

		private int inits;
		private int clicks;

		@Override
		public void init() {
			this.inits++;
		}

	}

	public static final class CurrentUI extends UI {

		private UI current;

		@Override
		public void init() {
			this.current = UI.getCurrent();
		}

	}

	public static final class ScaleUI extends UI {

		private final List<String> scales = new ArrayList<>();

		@Override
		public void init() {
			this.record("init");
		}

		@Override
		public void update() {
			this.record("update");
		}

		@Override
		public void drawBackground(final double mouseX, final double mouseY) {
			this.record("draw");
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.record("press");
		}

		@Override
		public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
			this.record("key");
		}

		private void record(final String hook) {
			this.scales.add(hook + " " + String.format(Locale.ROOT, "%.4f", FontScale.getScale()));
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

	public static final class PropertyUI extends UI {

		@UIProperty
		private String title = "Home";

	}

	public static final class StaticPropertyUI extends UI {

		@UIProperty
		private static String theme = "Light";

		@UIProperty
		private String title = "Home";

	}

	@UIStoreData(id = "uicore-local")
	public static final class LocalStore extends UIStore {

		private boolean destroyed;

		@Override
		public void destroy() {
			this.destroyed = true;
		}

	}

	@UIStoreData(id = "uicore-global", context = StoreContext.GLOBAL)
	public static final class GlobalStore extends UIStore {}

	@AllArgsConstructor
	@UIStoreData(id = "uicore-named")
	public static final class NamedStore extends UIStore {

		private final String name;

	}

	public static final class TraceNode extends Node {

		private final String       name;
		private final List<String> trace;

		private boolean cancel;

		private TraceNode(final String name, final List<String> trace, final int zindex) {
			super(0D, 0D, 100D, 100D);
			super.zindex(zindex);
			this.name = name;
			this.trace = trace;
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			this.trace.add("draw " + this.name);
		}

		@Override
		public void update() {
			this.trace.add("update " + this.name);
		}

		@Override
		public void detach() {
			this.trace.add("detach " + this.name);
		}

		@Override
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("pressed " + this.name);
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
			this.trace.add("dragged " + this.name);
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
			this.trace.add("released " + this.name);
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final double value, final @NonNull InternalContext context) {
			this.trace.add("scrolled " + this.name);
		}

		@Override
		public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
			this.trace.add("typed " + this.name);
			if (this.cancel) {
				context.cancel();
			}
		}

	}

	public static final class DepthNode extends Node {

		private final List<String> trace;
		private final double       level;

		private DepthNode(final List<String> trace, final int zindex, final double level) {
			super(0D, 0D, 100D, 100D);
			super.zindex(zindex);
			this.trace = trace;
			this.level = level;
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			this.trace.add("depth " + (UITest.depth() + 2000F));
			if (this.level > 0D) {
				super.getUi().setRenderPipelineLevel(this.level);
			}
		}

	}

	public static final class PartialTransition extends Transition {

		public PartialTransition(final In in, final Out out) {
			super(in, out);
		}

	}

	public static final class RecordingTransition extends Transition {

		public RecordingTransition(final List<String> trace) {
			super(new RecordingIn(trace), new RecordingOut(trace));
		}

	}

	@AllArgsConstructor
	public static final class RecordingIn extends Transition.In {

		private final List<String> trace;

		@Override
		public void start() {
			this.trace.add("in start");
			super.start(super.getAnimator().sequence(100F, 1F).getTimeline());
		}

		@Override
		public void init(final @NonNull UI ui) {
			this.trace.add("in init");
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.trace.add("in pre");
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.trace.add("in post");
		}

	}

	@AllArgsConstructor
	public static final class RecordingOut extends Transition.Out {

		private final List<String> trace;

		@Override
		public void start() {
			this.trace.add("out start");
			super.start(super.getAnimator().sequence(100F, 0F).getTimeline());
		}

		@Override
		public void init(final @NonNull UI ui) {
			this.trace.add("out init");
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.trace.add("out pre");
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			this.trace.add("out post");
		}

	}

	public static final class HoverBridge extends DemoUIBridge {

		private final List<String> hovers = new ArrayList<>();

		private int    scaleQueries;
		private double interfaceScale = 1D;

		@Override
		public double getInterfaceScale(final @NonNull UI ui) {
			this.scaleQueries++;
			return this.interfaceScale;
		}

		@Override
		public void drawHover(final @NonNull UI ui, final @NonNull List<@NonNull String> lines, final double mouseX, final double mouseY) {
			this.hovers.add(lines + " " + mouseX + " " + mouseY);
		}

	}

	public static final class ChangeListener extends FileAlterationListenerAdaptor {

		private final BlockingQueue<String> changes = new LinkedBlockingQueue<>();

		@Override
		public void onFileCreate(final File file) {
			this.changes.add("file create " + file.getName());
		}

		@Override
		public void onFileChange(final File file) {
			this.changes.add("file change " + file.getName());
		}

		@Override
		public void onFileDelete(final File file) {
			this.changes.add("file delete " + file.getName());
		}

		@Override
		public void onDirectoryCreate(final File directory) {
			this.changes.add("directory create " + directory.getName());
		}

		@Override
		public void onDirectoryChange(final File directory) {
			this.changes.add("directory change " + directory.getName());
		}

		@Override
		public void onDirectoryDelete(final File directory) {
			this.changes.add("directory delete " + directory.getName());
		}

		@Override
		public void onStop(final FileAlterationObserver observer) {
			this.changes.add("stop");
		}

	}

	public static final class LocationClassLoader extends ClassLoader {

		private final File location;

		private LocationClassLoader(final File location) {
			super(UITest.class.getClassLoader());
			this.location = location;
		}

		private Class<?> define(final String name, final byte[] bytes) throws MalformedURLException {
			final CodeSource source = new CodeSource(this.location.toURI().toURL(), (Certificate[]) null);
			return super.defineClass(name, bytes, 0, bytes.length, new ProtectionDomain(source, null, this, null));
		}

	}

	public static final class ReloadUI extends UI {

		private final List<String> trace;

		private int inits;

		public ReloadUI(final List<String> trace) {
			this.trace = trace;
		}

		@Override
		public void init() {
			this.inits++;
			this.trace.add("init " + this.inits);
			new ReloadNode(this.inits, this.trace).attach(this);
		}

	}

	public static final class ReloadNode extends Node {

		private final int          index;
		private final List<String> trace;

		private ReloadNode(final int index, final List<String> trace) {
			super(0D, 0D, 100D, 100D);
			this.index = index;
			this.trace = trace;
		}

		@Override
		public void detach() {
			this.trace.add("detach " + this.index);
		}

	}

}