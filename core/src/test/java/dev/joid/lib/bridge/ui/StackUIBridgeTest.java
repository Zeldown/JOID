package dev.joid.lib.bridge.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.core.data.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.transition.PopTransition;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class StackUIBridgeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<UI>     uiList = new ArrayList<>();
	private final List<String> trace  = new ArrayList<>();
	private final ScreenBridge bridge = new ScreenBridge(this.trace);

	@After
	public void disposeTheUis() {
		this.uiList.forEach(UI::dispose);
	}

	@Test
	public void closesTheOpenScreenBeforeOpeningAnother() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		this.bridge.open(menu);
		this.bridge.open(settings);
		Assert.assertEquals(Collections.singletonList(settings), this.bridge.getUiList());
		Assert.assertTrue(this.trace.contains("close menu"));
		Assert.assertTrue(menu.isClosed());
	}

	@Test
	public void keepsTheOpenScreenWhenItRefusesToClose() {
		final ScreenUI menu = new ScreenUI("menu", this);
		menu.closeable = false;
		this.bridge.open(menu);
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(true);
		try {
			this.bridge.open(new ScreenUI("settings", this));
		} finally {
			JOIDEnv.devMode(devMode);
			System.setErr(previous);
		}
		Assert.assertEquals(Collections.singletonList(menu), this.bridge.getUiList());
		Assert.assertEquals("[JOID] ScreenUI refused to close: a beforeClose listener cancelled the event, so ScreenUI is not opened: do not cancel it to let the UI close" + System.lineSeparator(), output.toString());
	}

	@Test
	public void keepsTheScreenUnderAPopupThatRefusesToClose() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI popup = new PopupUI("popup", this);
		popup.closeable = false;
		this.bridge.open(menu);
		this.bridge.open(popup);
		this.bridge.open(new ScreenUI("settings", this));
		Assert.assertEquals(Arrays.asList(menu, popup), this.bridge.getUiList());
		Assert.assertFalse(this.trace.contains("close menu"));
		Assert.assertFalse(menu.isClosed());
	}

	@Test
	public void opensAPopupAndAnOverlayOverTheOpenScreen() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final PopupUI popup = new PopupUI("popup", this);
		final OverlayUI overlay = new OverlayUI("overlay", this);
		this.bridge.open(menu);
		this.bridge.open(popup);
		this.bridge.open(overlay);
		Assert.assertEquals(Arrays.asList(menu, popup, overlay), this.bridge.getUiList());
		Assert.assertFalse(this.trace.contains("close menu"));
	}

	@Test
	public void bringsAnOpenUiBackOnTopWithoutClosingIt() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final PopupUI first = new PopupUI("first", this);
		final PopupUI second = new PopupUI("second", this);
		this.bridge.open(menu);
		this.bridge.open(first);
		this.bridge.open(second);
		this.trace.clear();
		this.bridge.open(first);
		this.bridge.open(menu);
		Assert.assertEquals(Arrays.asList(second, first, menu), this.bridge.getUiList());
		Assert.assertTrue(this.trace.isEmpty());
		Assert.assertFalse(menu.isClosed());
	}

	@Test
	public void loadsTheUiItAddsAtTheWindowSize() {
		final ScreenUI menu = new ScreenUI("menu", this);
		this.bridges.resize(1366, 768);
		this.bridge.add(menu);
		Assert.assertEquals(1366D, menu.getWindowWidth(), 0D);
		Assert.assertEquals(768D, menu.getWindowHeight(), 0D);
		Assert.assertTrue(this.trace.contains("init menu"));
	}

	@Test
	public void closesEveryUiWithoutAskingThem() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final OverlayUI overlay = new OverlayUI("overlay", this);
		this.bridge.add(menu);
		this.bridge.add(overlay);
		this.bridge.closeAll();
		Assert.assertTrue(this.bridge.getUiList().isEmpty());
		Assert.assertTrue(menu.isClosed());
		Assert.assertTrue(overlay.isClosed());
		Assert.assertFalse(this.trace.contains("close menu"));
	}

	@Test
	public void tellsWhenTheFirstScreenOpensAndTheLastOneCloses() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final PopupUI popup = new PopupUI("popup", this);
		final OverlayUI overlay = new OverlayUI("overlay", this);
		this.bridge.add(overlay);
		this.bridge.add(menu);
		this.bridge.add(popup);
		this.bridge.remove(menu);
		this.bridge.remove(popup);
		this.bridge.remove(popup);
		this.bridge.remove(overlay);
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void skipsTheScreenCallbacksWhileAScreenReplacesAnother() {
		this.bridge.open(new ScreenUI("menu", this));
		this.bridge.open(new PopupUI("popup", this));
		this.bridge.open(new ScreenUI("settings", this));
		this.bridge.close(this.bridge.getUiList().get(this.bridge.getUiList().size() - 1));
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void skipsTheScreenCallbacksWhileItForcesAScreenOpen() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		menu.closeable = false;
		this.bridge.open(menu);
		this.bridge.open(new PopupUI("popup", this));
		this.bridge.forceOpen(settings);
		Assert.assertEquals(Collections.singletonList(settings), this.bridge.getUiList());
		Assert.assertTrue(menu.isClosed());
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void tellsTheLastScreenClosedWhenItForcesAnOverlayOpen() {
		final OverlayUI overlay = new OverlayUI("overlay", this);
		this.bridge.open(new ScreenUI("menu", this));
		this.bridge.forceOpen(overlay);
		Assert.assertEquals(Collections.singletonList(overlay), this.bridge.getUiList());
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void skipsTheScreenCallbacksWhileAUiReplacesAnother() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		this.bridge.open(menu);
		this.bridge.replace(menu, settings);
		Assert.assertEquals(Collections.singletonList(settings), this.bridge.getUiList());
		Assert.assertTrue(menu.isClosed());
		Assert.assertFalse(this.trace.contains("close menu"));
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void keepsTheScreenCallbacksQuietWhileTheReplacedScreenPlaysItsExit() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		menu.transition(PopTransition.create());
		BridgeHandler.UI.register(this.bridge);
		try {
			this.bridge.open(menu);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}

			this.bridge.open(settings);
			Assert.assertTrue(menu.getTransition().getOut().isRunning());
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}
		} finally {
			BridgeHandler.UI.unregister(this.bridge);
		}
		Assert.assertEquals(Collections.singletonList(settings), this.bridge.getUiList());
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void attachesTheScreenOfAUiWhoseInitFails() {
		try {
			this.bridge.open(new FailingUI("menu", this));
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("Init failed", exception.getMessage());
		}
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void opensTheWaitingScreenWhenTheLeavingOneIsForcedToClose() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		menu.transition(PopTransition.create());
		BridgeHandler.UI.register(this.bridge);
		try {
			this.bridge.open(menu);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}

			this.bridge.open(settings);
			menu.dispose();
			this.bridge.close(menu);
			Assert.assertEquals(Collections.singletonList(settings), this.bridge.getUiList());
			this.bridge.close(settings);
		} finally {
			BridgeHandler.UI.unregister(this.bridge);
		}
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void keepsTheScreenCallbacksQuietWhenTheWaitingScreenChangesDuringTheExit() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		final ScreenUI help = new ScreenUI("help", this);
		menu.transition(PopTransition.create());
		BridgeHandler.UI.register(this.bridge);
		try {
			this.bridge.open(menu);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}

			this.bridge.open(settings);
			this.bridge.close(settings);
			this.bridge.open(help);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}
		} finally {
			BridgeHandler.UI.unregister(this.bridge);
		}
		Assert.assertEquals(Collections.singletonList(help), this.bridge.getUiList());
		Assert.assertFalse(this.trace.contains("init settings"));
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void dropsTheWaitingScreenWhenItClosesEveryUi() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		menu.transition(PopTransition.create());
		BridgeHandler.UI.register(this.bridge);
		try {
			this.bridge.open(menu);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}

			this.bridge.open(settings);
			this.bridge.closeAll();
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}
		} finally {
			BridgeHandler.UI.unregister(this.bridge);
		}
		Assert.assertTrue(this.bridge.getUiList().isEmpty());
		Assert.assertFalse(this.trace.contains("init settings"));
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void keepsALeavingScreenOpenedAgain() {
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		menu.transition(PopTransition.create());
		BridgeHandler.UI.register(this.bridge);
		try {
			this.bridge.open(menu);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}

			this.bridge.open(settings);
			this.bridge.open(menu);
			for (int frame = 0; frame < 30; frame++) {
				this.bridges.frame().draw(this.bridge);
			}
		} finally {
			BridgeHandler.UI.unregister(this.bridge);
		}
		Assert.assertEquals(Collections.singletonList(menu), this.bridge.getUiList());
		Assert.assertFalse(menu.isClosed());
		Assert.assertFalse(this.trace.contains("init settings"));
	}

	@Test
	public void tellsTheFirstScreenOpenWhenNoScreenWasReplaced() {
		final OverlayUI overlay = new OverlayUI("overlay", this);
		this.bridge.open(overlay);
		this.bridge.open(new ScreenUI("menu", this));
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void skipsTheScreenCallbacksWhenTheOpenScreenRefusesToClose() {
		final ScreenUI menu = new ScreenUI("menu", this);
		menu.closeable = false;
		this.bridge.open(menu);
		this.bridge.open(new ScreenUI("settings", this));
		menu.closeable = true;
		this.bridge.close(menu);
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void keepsTheScreensItCannotReplace() {
		final BaseBridge bridge = new BaseBridge();
		final BaseUI base = new BaseUI("base", this);
		final ScreenUI menu = new ScreenUI("menu", this);
		final ScreenUI settings = new ScreenUI("settings", this);
		bridge.open(base);
		bridge.open(menu);
		bridge.open(settings);
		Assert.assertEquals(Arrays.asList(base, settings), bridge.getUiList());
		Assert.assertFalse(this.trace.contains("close base"));
		Assert.assertTrue(this.trace.contains("close menu"));
	}

	@Test
	public void handlesEveryUi() {
		Assert.assertTrue(this.bridge.canHandle(new ScreenUI("menu", this)));
		Assert.assertTrue(this.bridge.canHandle(ScreenUI.class));
	}

	@Test
	public void replacesEveryUiByDefault() {
		Assert.assertTrue(this.bridge.canReplace(new ScreenUI("menu", this)));
	}

	@RequiredArgsConstructor
	public static final class ScreenBridge extends StackUIBridge {

		private final List<String> trace;

		@Override
		protected void attachScreen() {
			this.trace.add("first screen");
		}

		@Override
		protected void detachScreen() {
			this.trace.add("last screen");
		}

	}

	public static final class BaseBridge extends StackUIBridge {

		@Override
		public boolean canReplace(final @NonNull UI ui) {
			return !(ui instanceof BaseUI);
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static class ScreenUI extends UI {

		private final String       name;
		private final List<String> trace;

		private boolean closeable = true;

		public ScreenUI(final String name, final StackUIBridgeTest test) {
			this.name  = name;
			this.trace = test.trace;
			test.uiList.add(this);
			super.beforeClose(event -> {
				this.trace.add("close " + this.name);
				if (!this.closeable) {
					event.cancel();
				}
			});
		}

		@Override
		public void init() {
			this.trace.add("init " + this.name);
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class PopupUI extends ScreenUI {

		public PopupUI(final String name, final StackUIBridgeTest test) {
			super(name, test);
		}

	}

	public static final class BaseUI extends ScreenUI {

		public BaseUI(final String name, final StackUIBridgeTest test) {
			super(name, test);
		}

	}

	public static final class FailingUI extends ScreenUI {

		public FailingUI(final String name, final StackUIBridgeTest test) {
			super(name, test);
		}

		@Override
		public void init() {
			super.init();
			throw new IllegalStateException("Init failed");
		}

	}

	@UIDataOverlay(active = true)
	public static final class OverlayUI extends ScreenUI {

		public OverlayUI(final String name, final StackUIBridgeTest test) {
			super(name, test);
		}

	}

}