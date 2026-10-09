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
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.color.Color;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.resolver.IKeyResolver;
import dev.joid.lib.input.key.resolver.KeyResolver;
import dev.joid.lib.input.mouse.MouseButton;
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
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.hook.store.scope.StoreScope;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.dev.DevNode;
import dev.joid.lib.utils.align.Align;

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
		DevFont.load();
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
	public void reloadsItsInitOnAFreshTree() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		ui.keybind = new Object[] {Key.R};
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
			ui.dispose();
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
		Assert.assertFalse(ui.fireMousePressed(MouseButton.LEFT));
		Assert.assertFalse(ui.fireMouseReleased(MouseButton.LEFT));
		Assert.assertFalse(ui.fireMouseDragged(MouseButton.LEFT, 10L));
		Assert.assertFalse(ui.fireMouseScroll(0D, 1D));
		Assert.assertFalse(ui.fireKeyPressed(Key.A));
		Assert.assertFalse(ui.fireCharTyped('a'));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void receivesTheMouseOnItsCanvas() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.resize(1366, 768).open(ui);
		this.bridges.move(683D, 384D).frame();
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
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
		Assert.assertFalse(ui.fireMousePressed(MouseButton.LEFT));
		Assert.assertFalse(ui.fireKeyPressed(Key.A));
		Assert.assertFalse(ui.fireCharTyped('a'));
		ui.cancel = true;
		Assert.assertTrue(ui.fireMousePressed(MouseButton.LEFT));
		Assert.assertTrue(ui.fireMouseReleased(MouseButton.LEFT));
		Assert.assertTrue(ui.fireMouseDragged(MouseButton.LEFT, 10L));
		Assert.assertTrue(ui.fireMouseScroll(0D, 1D));
		Assert.assertTrue(ui.fireKeyPressed(Key.A));
		Assert.assertTrue(ui.fireCharTyped('a'));
	}

	@Test
	public void passesTheInputToItsNodesFirst() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		this.bridges.open(ui);
		this.trace.clear();
		ui.fireMouseScroll(0D, 1D);
		ui.fireMouseDragged(MouseButton.LEFT, 10L);
		ui.fireMouseReleased(MouseButton.LEFT);
		ui.fireKeyPressed(Key.A);
		ui.fireCharTyped('a');
		Assert.assertEquals(Arrays.asList("scrolled node", "scrolled 1.0", "dragged node", "dragged LEFT 10", "released node", "released LEFT", "key node", "key A", "char node", "char a"), this.trace);
	}

	@Test
	public void pressesItsNodesFromTheHighestIndex() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("below", this.trace, -5), new TraceNode("middle", this.trace, 0), new TraceNode("above", this.trace, 5));
		this.bridges.open(ui);
		this.trace.clear();
		ui.fireMousePressed(MouseButton.RIGHT);
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
		ui.keybind = new Object[] {Key.R, Key.LEFT_CONTROL};
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.R);
		this.bridges.getUi().keyPressed(Key.R);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertEquals(Arrays.asList("key R", "keybind", "key R cancelled"), this.trace);
	}

	@Test
	public void runsAKeybindRegisteredOutsideItsInit() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		ui.keybind(() -> this.trace.add("save"), Key.S);
		this.bridges.getWindow().getKeys().add(Key.S);
		this.bridges.getUi().keyPressed(Key.S);
		Assert.assertEquals(Arrays.asList("save", "key S cancelled"), this.trace);
	}

	@Test
	public void skipsTheKeybindsOnceANodeCancelsTheKey() {
		final TraceNode node = new TraceNode("node", this.trace, 0);
		final TraceUI ui = new TraceUI(this.trace, node);
		node.cancel = true;
		ui.keybind = new Object[] {Key.R};
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.R);
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertEquals(Arrays.asList("key node", "key R cancelled"), this.trace);
	}

	@Test
	public void keepsOneKeybindAcrossItsReloads() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.keybind = new Object[] {Key.R};
		this.bridges.open(ui);
		ui.reload();
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.R);
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertEquals(Arrays.asList("keybind", "key R cancelled"), this.trace);
	}

	@Test
	public void keepsOneKeybindPerSetOfKeys() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		ui.keybind(() -> this.trace.add("first"), Key.LEFT_CONTROL, Key.S);
		ui.keybind(() -> this.trace.add("second"), Key.S, Key.LEFT_CONTROL);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.S));
		this.bridges.getUi().keyPressed(Key.S);
		Assert.assertEquals(1, ui.getKeybindMap().size());
		Assert.assertEquals(Arrays.asList("second", "key S cancelled"), this.trace);
	}

	@Test
	public void runsAKeybindOnlyForTheKeysOfItsShortcut() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		ui.keybind(() -> this.trace.add("save"), Key.LEFT_CONTROL, Key.S);
		this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.S, Key.A));
		this.bridges.getUi().keyPressed(Key.A);
		this.bridges.getUi().keyPressed(Key.S);
		Assert.assertEquals(Arrays.asList("key A", "save", "key S cancelled"), this.trace);
	}

	@Test
	public void zoomsOutAndInWithTheModifierKeys() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.MINUS);
		Assert.assertEquals(0.9D, ui.getView().getZoom(), 0D);
		this.bridges.getUi().keyPressed(Key.NUMPAD_ADD);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		this.bridges.getWindow().getKeys().clear();
		this.bridges.getWindow().getKeys().add(Key.RIGHT_ALT);
		this.bridges.getUi().keyPressed(Key.NUMPAD_SUBTRACT);
		Assert.assertEquals(0.9D, ui.getView().getZoom(), 0D);
		this.bridges.getUi().keyPressed(Key.EQUAL);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(Arrays.asList("key MINUS cancelled", "key NUMPAD_ADD cancelled", "key NUMPAD_SUBTRACT cancelled", "key EQUAL cancelled"), this.trace);
	}

	@Test
	public void keepsItsZoomAtItsLimits() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.EQUAL);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
		ui.zoom(0.1D);
		this.bridges.getUi().keyPressed(Key.MINUS);
		Assert.assertEquals(0.1D, ui.getView().getZoom(), 0D);
		Assert.assertEquals(Arrays.asList("key EQUAL", "key MINUS"), this.trace);
	}

	@Test
	public void keepsItsZoomWithoutModifierKey() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getUi().keyPressed(Key.MINUS);
		Assert.assertEquals(1D, ui.getView().getZoom(), 0D);
	}

	@Test
	public void keepsItsZoomWhenNotZoomable() {
		final FixedUI ui = new FixedUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.MINUS);
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
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertEquals(2, ui.inits);
		Assert.assertEquals(Collections.singletonList("key R cancelled"), this.trace);
	}

	@Test
	public void reloadsWithF5InDevMode() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		ui.zoom(0.5D);
		this.bridges.getUi().keyPressed(Key.F5);
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
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertFalse(this.bridges.getUi().isOpen(ui));
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
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
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
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
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
			this.bridges.getUi().keyPressed(Key.R);
		} finally {
			System.setErr(previous);
		}
		Assert.assertEquals("[JOID] The UI " + TraceUI.class.getName() + " has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead", output.toString().trim());
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
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
		this.bridges.getUi().keyPressed(Key.F5);
		this.bridges.getUi().keyPressed(Key.F3);
		this.bridges.getWindow().getKeys().add(Key.LEFT_CONTROL);
		this.bridges.getUi().keyPressed(Key.R);
		Assert.assertEquals(1, ui.inits);
		Assert.assertNull(ui.getDevNode());
		Assert.assertEquals(Arrays.asList("key F5", "key F3", "key R"), this.trace);
	}

	@Test
	public void togglesItsDevPanelWithF3InDevMode() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertTrue(ui.getDevNode() instanceof DevNode);
		Assert.assertFalse(ui.getNodeList().contains(ui.getDevNode()));
		this.bridges.getUi().keyPressed(Key.F3);
		Assert.assertTrue(ui.getNodeList().contains(ui.getDevNode()));
		this.bridges.getUi().keyPressed(Key.F3);
		Assert.assertFalse(ui.getNodeList().contains(ui.getDevNode()));
		Assert.assertEquals(Arrays.asList("key F3 cancelled", "key F3 cancelled"), this.trace);
	}

	@Test
	public void keepsItsDevPanelAcrossItsReloads() {
		JOID.inst().setDevMode(true);
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.open(ui);
		this.bridges.getUi().keyPressed(Key.F3);
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
		this.bridges.getUi().keyPressed(Key.F3);
		Assert.assertNull(ui.getDevNode());
		Assert.assertEquals(Collections.singletonList("key F3"), this.trace);
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
		Assert.assertFalse(ui.fireMouseScroll(0D, 0D));
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
		Assert.assertTrue(ui.fireClose());
		Assert.assertEquals(Arrays.asList("close", "detach node"), this.trace);
	}

	@Test
	public void staysOpenWhenItsHookRefusesToClose() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		ui.closeable = false;
		this.bridges.open(ui);
		this.trace.clear();
		Assert.assertFalse(ui.fireClose());
		Assert.assertEquals(Collections.singletonList("close"), this.trace);
	}

	@Test
	public void playsItsOutTransitionBeforeClosing() {
		final PopupUI ui = new PopupUI(this.trace, new TraceNode("node", this.trace, 0));
		this.bridges.open(ui).frames(20);
		Assert.assertFalse(ui.fireClose());
		Assert.assertTrue(ui.getTransition().getOut().isRunning());
		Assert.assertFalse(ui.fireClose());
		Assert.assertTrue(this.bridges.getUi().isOpen(ui));
		Assert.assertFalse(this.trace.contains("detach node"));
		this.bridges.frames(20);
		Assert.assertFalse(this.bridges.getUi().isOpen(ui));
		Assert.assertEquals(1, Collections.frequency(this.trace, "close"));
		Assert.assertEquals(1, Collections.frequency(this.trace, "detach node"));
	}

	@Test
	public void closesAtOnceWithItsOutTransitionDisabled() {
		final InPopupUI ui = new InPopupUI(this.trace);
		this.bridges.open(ui).frames(20);
		Assert.assertTrue(ui.fireClose());
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
		ui.fireClose();
		this.bridges.frames(4);
		final float alpha = UITest.background(this.bridges).getAlpha();
		Assert.assertTrue(String.valueOf(alpha), alpha > 0F && alpha < 192F / 255F);
	}

	@Test
	public void drawsItsNodesByIndex() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("top", this.trace, Integer.MAX_VALUE), new TraceNode("middle", this.trace, 150), new TraceNode("base", this.trace, 0), new TraceNode("back", this.trace, -1), new TraceNode("bottom", this.trace, Integer.MIN_VALUE));
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update bottom", "update back", "update base", "update middle", "update top", "update", "draw bottom", "draw back", "draw base", "draw middle", "draw top"), this.trace);
	}

	@Test
	public void givesTheEdgesOfTheWindowOnItsCanvas() {
		final TraceUI ui = new TraceUI(this.trace);
		this.bridges.resize(2560, 1080);
		this.bridges.open(ui).frame();
		Assert.assertEquals(-320D, ui.getViewX(), 1E-9D);
		Assert.assertEquals(0D, ui.getViewY(), 1E-9D);
		Assert.assertEquals(2560D, ui.getViewWidth(), 1E-9D);
		Assert.assertEquals(1080D, ui.getViewHeight(), 1E-9D);
		ui.zoom(0.5D);
		Assert.assertEquals(-1600D, ui.getViewX(), 1E-9D);
		Assert.assertEquals(-540D, ui.getViewY(), 1E-9D);
		Assert.assertEquals(5120D, ui.getViewWidth(), 1E-9D);
		Assert.assertEquals(2160D, ui.getViewHeight(), 1E-9D);
	}

	@Test
	public void raisesTheNodesDrawnAfterItsDepthLevel() {
		final TraceUI ui = new TraceUI(this.trace, new DepthNode(this.trace, 0, 30D), new DepthNode(this.trace, 1, 0D));
		ui.getData().setBackground(false);
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update", "depth 0.0", "depth 30.0"), this.trace);
		Assert.assertEquals(30D, ui.getDepthLevel(), 0D);
		ui.setDepthLevel(5D);
		Assert.assertEquals(5D, ui.getDepthLevel(), 0D);
	}

	@Test
	public void wrapsItsDrawInItsTransition() {
		final TraceUI ui = new TraceUI(this.trace, new TraceNode("node", this.trace, 0));
		ui.setTransition(new RecordingTransition(this.trace));
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("in init", "in start", "out init", "update node", "update", "in pre", "draw node", "in post"), this.trace);
		this.trace.clear();
		Assert.assertFalse(ui.fireClose());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("close", "out start", "update node", "update", "in pre", "out pre", "draw node", "in post", "out post"), this.trace);
	}

	@Test
	public void leavesTheDisabledStatesOfItsTransitionAside() {
		final TraceUI ui = new TraceUI(this.trace);
		final RecordingTransition transition = new RecordingTransition(this.trace);
		transition.getIn().disable();
		transition.getOut().disable();
		ui.setTransition(transition);
		this.bridges.open(ui);
		Assert.assertEquals(Collections.singletonList("update"), this.trace);
		Assert.assertTrue(ui.fireClose());
	}

	@Test
	public void playsATransitionWithoutOutState() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new PartialTransition(new RecordingIn(this.trace), null));
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("in init", "in start", "update", "in pre", "in post"), this.trace);
		this.trace.clear();
		Assert.assertTrue(ui.fireClose());
		Assert.assertEquals(Collections.singletonList("close"), this.trace);
	}

	@Test
	public void playsATransitionWithoutInState() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new PartialTransition(null, new RecordingOut(this.trace)));
		this.bridges.open(ui);
		Assert.assertEquals(Arrays.asList("out init", "update"), this.trace);
		this.trace.clear();
		Assert.assertFalse(ui.fireClose());
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("close", "out start", "update", "out pre", "out post"), this.trace);
		this.bridges.frames(10);
		Assert.assertFalse(this.bridges.getUi().isOpen(ui));
	}

	@Test
	public void endsItsTransitionOnTheFrameItsTimelineFinishes() {
		final TraceUI ui = new TraceUI(this.trace);
		ui.setTransition(new RecordingTransition(this.trace));
		this.bridges.open(ui);
		this.trace.clear();
		this.bridges.getClock().advance(200L);
		this.bridges.frame();
		Assert.assertEquals(Arrays.asList("update", "in pre", "in post"), this.trace);
		this.trace.clear();
		this.bridges.frame();
		Assert.assertEquals(Collections.singletonList("update"), this.trace);
	}

	@Test
	public void endsItsTransitionEvenWhenItsDrawFails() {
		final boolean[] broken = {false};
		final TraceUI ui = new TraceUI(this.trace, new DrawingNode(() -> {
			if (broken[0]) {
				throw new IllegalStateException("broken");
			}
		}));
		ui.setTransition(new RecordingTransition(this.trace));
		this.bridges.open(ui);
		broken[0] = true;
		this.trace.clear();
		try {
			ui.draw(0D, 0D);
			Assert.fail();
		} catch (final IllegalStateException e) {
			Assert.assertEquals("broken", e.getMessage());
		}
		Assert.assertEquals(Arrays.asList("in pre", "in post"), this.trace);
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
	public void showsTheTooltipThroughANodeThatIsNotInteractive() {
		final TraceUI ui = new TraceUI(this.trace, RectNode.create(100D, 100D, 200D, 200D).hover(() -> "below"), RectNode.create(150D, 150D, 200D, 200D).zindex(1).hover(() -> "above").interactive(false));
		this.bridges.open(ui).move(200D, 200D).frames(2);
		Assert.assertTrue(this.trace.contains("hover [below] 200.0 200.0"));
		Assert.assertFalse(this.trace.contains("hover [above] 200.0 200.0"));
	}

	@Test
	public void listsTheNodesAtAPointFromTheFrontToTheBack() {
		final RectNode child = RectNode.create(20D, 20D, 50D, 50D);
		final RectNode behind = RectNode.create(10D, 10D, 50D, 50D).zindex(-1);
		final RectNode parent = RectNode.create(100D, 100D, 200D, 200D).append(child, behind);
		final RectNode above = RectNode.create(150D, 150D, 200D, 200D).zindex(1).interactive(false);
		final RectNode below = RectNode.create(0D, 0D, 400D, 400D).zindex(-5);
		final RectNode hidden = RectNode.create(0D, 0D, 400D, 400D).zindex(2).visible(false);
		final TraceUI ui = new TraceUI(this.trace, parent, above, below, hidden);
		this.bridges.open(ui).move(155D, 155D).frames(2);
		Assert.assertEquals(Arrays.asList(above, child, parent, behind, below), ui.getNodeListAt(155D, 155D));
		Assert.assertEquals(Arrays.asList(parent, below), ui.getNodeListAt(105D, 105D));
		Assert.assertEquals(Arrays.asList(child, parent, behind), parent.getNodeListAt(155D, 155D));
		Assert.assertSame(child, ui.getHoveredNode());
	}

	@Test
	public void hoversNoNodeBelowAnotherUi() {
		final TraceUI ui = new TraceUI(this.trace, RectNode.create(100D, 100D, 200D, 200D));
		this.bridges.open(ui).open(new TraceUI(new ArrayList<>())).move(200D, 200D).frames(2);
		Assert.assertEquals(1, ui.getNodeListAt(200D, 200D).size());
		Assert.assertNull(ui.getHoveredNode());
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
		Assert.assertFalse(this.bridges.getRender().getState().getStencil().isEnabled());
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
		Assert.assertFalse(this.bridges.getRender().getState().getStencil().isEnabled());
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
		Assert.assertFalse(this.bridges.getRender().getState().getStencil().isEnabled());
	}

	@Test
	public void masksItsDrawingInsideAResource() {
		final TraceUI ui = new TraceUI(this.trace);
		final Resource resource = Resource.of(JOID.class.getResourceAsStream("/assets/dev/textures/icons/eye.png"));
		this.bridges.open(ui);
		this.bridges.getRender().alphaCutoff(0.25F);
		final List<String> stencils = new ArrayList<>();
		ui.mask(resource, 10D, 20D, 30D, 40D, () -> stencils.add(UITest.stencil(this.bridges)));
		ui.mask(resource, 10D, 20D, 30D, 40D, () -> stencils.add(UITest.stencil(this.bridges)), false);
		Assert.assertEquals(Arrays.asList("EQUAL 1", "off"), stencils);
		Assert.assertEquals(0.25F, this.bridges.getRender().getState().getAlphaCutoff(), 0F);
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
		Assert.assertFalse(this.bridges.getRender().getState().getStencil().isEnabled());
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
		ui.dispose();
		Assert.assertTrue(store.destroyed);
	}

	@Test
	public void createsItsLocalStoresAgainOnceReopened() {
		final TraceUI ui = new TraceUI(this.trace);
		final LocalStore store = ui.useStore(LocalStore.class);
		ui.dispose();
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
		ui.dispose();
		final File file = new File(new File(this.folder.getRoot(), "property"), PropertyUI.class.getName() + ".property");
		final String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		Assert.assertTrue(json, json.contains("\"title\":\"Shop\""));
	}

	@Test
	public void restoresItsPropertiesOnItsFirstLoad() {
		final PropertyUI saved = new PropertyUI();
		saved.title = "Shop";
		saved.dispose();
		final PropertyUI loaded = new PropertyUI();
		Assert.assertEquals("Home", loaded.title);
		loaded.load(1920D, 1080D);
		Assert.assertEquals("Shop", loaded.title);
	}

	@Test
	public void keepsItsPropertiesAcrossAReload() {
		final PropertyUI ui = new PropertyUI();
		ui.dispose();
		ui.load(1920D, 1080D);
		ui.title = "Shop";
		ui.reload();
		Assert.assertEquals("Shop", ui.title);
	}

	@Test
	public void leavesItsStaticFieldsOutOfItsProperties() throws IOException {
		final StaticPropertyUI ui = new StaticPropertyUI();
		ui.dispose();
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
			ui.dispose();
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
			ui.dispose();
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
			ui.dispose();
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
			ui.dispose();
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
		ui.dispose();
		final long deadline = System.currentTimeMillis() + 5000L;
		while (ui.getFileMonitor() != null && System.currentTimeMillis() < deadline) {
			Thread.sleep(10L);
		}
		Assert.assertNull(ui.getFileMonitor());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAPressWithoutButton() {
		new TraceUI(this.trace).fireMousePressed(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeyWithoutKey() {
		new TraceUI(this.trace).fireKeyPressed(null);
	}

	@Test
	public void resolvesTheBindingsOfAKeybindAtEachKey() {
		final Binding binding = new Binding(Key.S);
		final IKeyResolver resolver = new BindingKeyResolver();
		KeyResolver.register(resolver);
		try {
			final TraceUI ui = new TraceUI(this.trace);
			this.bridges.open(ui);
			this.trace.clear();
			ui.keybind(() -> this.trace.add("save"), Key.LEFT_CONTROL, binding);
			this.bridges.getWindow().getKeys().addAll(Arrays.asList(Key.LEFT_CONTROL, Key.S, Key.D));
			this.bridges.getUi().keyPressed(Key.S);
			binding.key = Key.D;
			this.bridges.getUi().keyPressed(Key.S);
			this.bridges.getUi().keyPressed(Key.D);
			binding.key = null;
			this.bridges.getUi().keyPressed(Key.D);
			Assert.assertEquals(Arrays.asList("save", "key S cancelled", "key S", "save", "key D cancelled", "key D"), this.trace);
		} finally {
			KeyResolver.unregister(resolver);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAKeybindWithoutResolverForItsBinding() {
		new TraceUI(this.trace).keybind(() -> this.trace.add("save"), Key.LEFT_CONTROL, "S");
	}

	@Test(expected = NullPointerException.class)
	public void refusesAKeybindWithANullBinding() {
		new TraceUI(this.trace).keybind(() -> this.trace.add("save"), Key.LEFT_CONTROL, null);
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
		return state.getStencil().isEnabled() ? state.getStencil().getFunction() + " " + state.getStencil().getReference() : "off";
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

		private final Node[]       nodes;
		private final List<String> trace;

		private int      inits;
		private boolean  cancel;
		private Object[] keybind;
		private boolean  closeable = true;

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
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
			this.trace.add("pressed " + button + " " + mouseX + " " + mouseY);
			this.cancel(context);
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull DispatchContext context) {
			this.trace.add("dragged " + button + " " + deltaTime);
			this.cancel(context);
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
			this.trace.add("released " + button);
			this.cancel(context);
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final double valueX, final double value, final @NonNull DispatchContext context) {
			this.trace.add("scrolled " + value);
			this.cancel(context);
		}

		@Override
		public void keyPressed(final @NonNull Key key, final @NonNull DispatchContext context) {
			this.trace.add("key " + key + (context.isCancelled() ? " cancelled" : ""));
			this.cancel(context);
		}

		@Override
		public void charTyped(final int codepoint, final @NonNull DispatchContext context) {
			this.trace.add("char " + new String(Character.toChars(codepoint)) + (context.isCancelled() ? " cancelled" : ""));
			this.cancel(context);
		}

		@Override
		public void drawHover(final @NonNull Object content, final double mouseX, final double mouseY) {
			this.trace.add("hover " + content + " " + mouseX + " " + mouseY);
		}

		private void cancel(final DispatchContext context) {
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
		public void init() {
			super.init();
			super.add(new DrawingNode(() -> {
				try {
					Thread.sleep(this.pause);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}));
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

	@UIStoreData(id = "uicore-global", scope = StoreScope.GLOBAL)
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
		public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
			this.trace.add("pressed " + this.name);
		}

		@Override
		public void mouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull DispatchContext context) {
			this.trace.add("dragged " + this.name);
		}

		@Override
		public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
			this.trace.add("released " + this.name);
		}

		@Override
		public void mouseScroll(final double mouseX, final double mouseY, final double valueX, final double value, final @NonNull DispatchContext context) {
			this.trace.add("scrolled " + this.name);
		}

		@Override
		public void keyPressed(final @NonNull Key key, final @NonNull DispatchContext context) {
			this.trace.add("key " + this.name);
			if (this.cancel) {
				context.cancel();
			}
		}

		@Override
		public void charTyped(final int codepoint, final @NonNull DispatchContext context) {
			this.trace.add("char " + this.name);
			if (this.cancel) {
				context.cancel();
			}
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

	public static final class DepthNode extends Node {

		private final double       level;
		private final List<String> trace;

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
				super.getUi().setDepthLevel(this.level);
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
		public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {
			this.hovers.add(content + " " + mouseX + " " + mouseY);
		}

	}

	public static final class Binding {

		private Key key;

		public Binding(final Key key) {
			this.key = key;
		}

	}

	public static final class BindingKeyResolver implements IKeyResolver {

		@Override
		public boolean supports(final @NonNull Object binding) {
			return binding instanceof Binding;
		}

		@Override
		public Key resolve(final @NonNull Object binding) {
			return ((Binding) binding).key;
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