package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlay;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import lombok.NonNull;

public class StackUIBridgeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace  = new ArrayList<>();
	private final ScreenBridge bridge = new ScreenBridge(this.trace);

	@Test
	public void closesTheOpenScreenBeforeOpeningAnother() {
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		final ScreenUI settings = new ScreenUI("settings", this.trace);
		this.bridge.open(menu);
		this.bridge.open(settings);
		Assert.assertEquals(Collections.singletonList(settings), this.bridge.getUiList().ordered());
		Assert.assertTrue(this.trace.contains("close menu"));
		Assert.assertTrue(menu.isClosed());
	}

	@Test
	public void keepsTheOpenScreenWhenItRefusesToClose() {
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		menu.closeable = false;
		this.bridge.open(menu);
		this.bridge.open(new ScreenUI("settings", this.trace));
		Assert.assertEquals(Collections.singletonList(menu), this.bridge.getUiList().ordered());
	}

	@Test
	public void opensAPopupAndAnOverlayOverTheOpenScreen() {
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		final PopupUI popup = new PopupUI("popup", this.trace);
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
		this.bridge.open(menu);
		this.bridge.open(popup);
		this.bridge.open(overlay);
		Assert.assertEquals(Arrays.asList(menu, popup, overlay), this.bridge.getUiList().ordered());
		Assert.assertFalse(this.trace.contains("close menu"));
	}

	@Test
	public void loadsTheUiItAddsAtTheWindowSize() {
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		this.bridges.resize(1366, 768);
		this.bridge.add(menu);
		Assert.assertEquals(1366D, menu.getWidth(), 0D);
		Assert.assertEquals(768D, menu.getHeight(), 0D);
		Assert.assertTrue(this.trace.contains("init menu"));
	}

	@Test
	public void closesEveryUiWithoutAskingThem() {
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
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
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		final PopupUI popup = new PopupUI("popup", this.trace);
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
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
		this.bridge.open(new ScreenUI("menu", this.trace));
		this.bridge.open(new PopupUI("popup", this.trace));
		this.bridge.open(new ScreenUI("settings", this.trace));
		this.bridge.close(this.bridge.getUiList().getLast());
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void tellsTheFirstScreenOpenWhenNoScreenWasReplaced() {
		final OverlayUI overlay = new OverlayUI("overlay", this.trace);
		this.bridge.open(overlay);
		this.bridge.open(new ScreenUI("menu", this.trace));
		Assert.assertEquals(Collections.singletonList("first screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void skipsTheScreenCallbacksWhenTheOpenScreenRefusesToClose() {
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		menu.closeable = false;
		this.bridge.open(menu);
		this.bridge.open(new ScreenUI("settings", this.trace));
		menu.closeable = true;
		this.bridge.close(menu);
		Assert.assertEquals(Arrays.asList("first screen", "last screen"), this.trace.stream().filter(line -> line.endsWith("screen")).collect(Collectors.toList()));
	}

	@Test
	public void keepsTheScreensItCannotReplace() {
		final BaseBridge bridge = new BaseBridge();
		final BaseUI base = new BaseUI("base", this.trace);
		final ScreenUI menu = new ScreenUI("menu", this.trace);
		final ScreenUI settings = new ScreenUI("settings", this.trace);
		bridge.open(base);
		bridge.open(menu);
		bridge.open(settings);
		Assert.assertEquals(Arrays.asList(base, settings), bridge.getUiList().ordered());
		Assert.assertFalse(this.trace.contains("close base"));
		Assert.assertTrue(this.trace.contains("close menu"));
	}

	@Test
	public void handlesEveryUi() {
		Assert.assertTrue(this.bridge.canHandle(new ScreenUI("menu", this.trace)));
		Assert.assertTrue(this.bridge.canHandle(ScreenUI.class));
	}

	@Test
	public void replacesEveryUiByDefault() {
		Assert.assertTrue(this.bridge.canReplace(new ScreenUI("menu", this.trace)));
	}

	public static final class ScreenBridge extends StackUIBridge {

		private final List<String> trace;

		public ScreenBridge(final List<String> trace) {
			this.trace = trace;
		}

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

	public static class ScreenUI extends UI {

		private final String       name;
		private final List<String> trace;

		private boolean closeable = true;

		public ScreenUI(final String name, final List<String> trace) {
			this.name  = name;
			this.trace = trace;
		}

		@Override
		public void init() {
			this.trace.add("init " + this.name);
		}

		@Override
		public boolean close() {
			this.trace.add("close " + this.name);
			return this.closeable;
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class PopupUI extends ScreenUI {

		public PopupUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

	public static final class BaseUI extends ScreenUI {

		public BaseUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

	@UIDataOverlay(active = true)
	public static final class OverlayUI extends ScreenUI {

		public OverlayUI(final String name, final List<String> trace) {
			super(name, trace);
		}

	}

}