package dev.joid.test.snapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.dto.locator.AssetLocator;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.clock.ManualClockBridge;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.cursor.Cursor;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class SnapshotRunner {

	private static final List<String> SCENARIOS = Arrays.asList("dev", "popup", "static", "window", "resource", "transition", "interaction");

	private static final String SYMBOLS         = " '-,./;=[\\]`";
	private static final Key[]  SYMBOL_KEYS     = {Key.SPACE, Key.APOSTROPHE, Key.MINUS, Key.COMMA, Key.PERIOD, Key.SLASH, Key.SEMICOLON, Key.EQUAL, Key.LEFT_BRACKET, Key.BACKSLASH, Key.RIGHT_BRACKET, Key.GRAVE_ACCENT};
	private static final String SHIFTED_DIGITS  = ")!@#$%^&*(";
	private static final String SHIFTED_SYMBOLS = " \"_<>?:+{|}~";

	private final List<int[]>          masks;
	private final ManualClockBridge    clock;
	private final SnapshotUIBridge     bridge;
	private final ISnapshotBackend     backend;
	private final SnapshotWindowBridge window;

	private SnapshotRunner(final ISnapshotBackend backend) {
		this.clock   = ManualClockBridge.create(1735689600000L);
		this.masks   = new ArrayList<>();
		this.window  = new SnapshotWindowBridge();
		this.bridge  = new SnapshotUIBridge();
		this.backend = backend;
	}

	public static @NonNull SnapshotRunner start(final @NonNull ISnapshotBackend backend) {
		final SnapshotRunner runner = new SnapshotRunner(backend);
		backend.create(1920, 1080);
		BridgeHandler.CLOCK.register(runner.clock);
		BridgeHandler.WINDOW.register(runner.window);
		BridgeHandler.AUDIO.register(new SnapshotAudioBridge());
		AssetLocator.register(new SnapshotUrlAssetLocator(SnapshotSettings.getCache()));
		runner.resize(1920, 1080);

		JOID.inst().setDevMode(false).setDemoMode(true).load();
		BridgeHandler.UI.register(runner.bridge);
		return runner;
	}

	public void stop() {
		this.bridge.closeAll();
		this.backend.destroy();
	}

	public @NonNull String getRenderer() {
		return this.backend.getRenderer().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-|-$", "");
	}

	public static @NonNull List<String> getScenarios() {
		return SnapshotRunner.SCENARIOS;
	}

	public static @NonNull List<String> getShots(final @NonNull String scenario) {
		return SnapshotRunner.read(scenario).stream().map(String::trim).filter(line -> line.startsWith("shot ")).map(line -> line.substring(5).trim()).collect(Collectors.toList());
	}

	public @NonNull Map<String, SnapshotImage> run(final @NonNull String scenario) {
		return this.execute(SnapshotRunner.read(scenario).toArray(new String[0]));
	}

	public @NonNull Map<String, SnapshotImage> execute(final @NonNull String... commands) {
		this.bridge.closeAll();
		this.bridge.interfaceScale(1D);
		this.window.getKeys().clear();
		this.masks.clear();
		this.clock.setTime(1735689600000L);
		JOID.inst().setDevMode(false);
		this.resize(1920, 1080);

		final Map<String, SnapshotImage> shots = new LinkedHashMap<>();
		for (final String line : commands) {
			final String command = line.trim();
			if (command.isEmpty() || command.startsWith("#")) {
				continue;
			}

			final String[] arguments = command.split("\\s+");
			switch (arguments[0]) {
			case "ui":
				this.show(arguments[1], false);
				break;
			case "open":
				this.show(arguments[1], true);
				break;
			case "wait":
				this.advance(Long.parseLong(arguments[1]));
				break;
			case "move":
				this.window.setMouseX(Double.parseDouble(arguments[1]));
				this.window.setMouseY(Double.parseDouble(arguments[2]));
				this.render(false);
				break;
			case "moveto":
				this.moveTo(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2]), Long.parseLong(arguments[3]));
				break;
			case "press":
				this.bridge.mousePressed(ClickType.valueOf(arguments[1]));
				break;
			case "release":
				this.bridge.mouseReleased(ClickType.valueOf(arguments[1]));
				break;
			case "scroll":
				this.bridge.mouseScroll(arguments.length > 2 ? Double.parseDouble(arguments[1]) : 0D, Double.parseDouble(arguments[arguments.length > 2 ? 2 : 1]));
				break;
			case "type":
				this.type(command.substring(5));
				break;
			case "key":
				this.key(arguments[1]);
				break;
			case "down":
				this.window.getKeys().add(Key.valueOf(arguments[1]));
				break;
			case "up":
				this.window.getKeys().remove(Key.valueOf(arguments[1]));
				break;
			case "resize":
				this.resize(Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]));
				break;
			case "zoom":
				this.zoom(Double.parseDouble(arguments[1]));
				break;
			case "scale":
				this.bridge.interfaceScale(Double.parseDouble(arguments[1]));
				break;
			case "dev":
				JOID.inst().setDevMode(Boolean.parseBoolean(arguments[1]));
				break;
			case "mask":
				this.masks.add(new int[] {Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4])});
				break;
			case "unmask":
				this.masks.clear();
				break;
			case "cursor":
				this.checkCursor(Cursor.valueOf(arguments[1]));
				break;
			case "shot":
				shots.put(arguments[1], this.settle(arguments[1]));
				break;
			default:
				throw new IllegalArgumentException("Unknown snapshot command: " + command);
			}
		}
		return shots;
	}

	private void type(final String text) {
		for (final char character : text.toCharArray()) {
			final boolean shift = SnapshotRunner.isShifted(character) && this.window.getKeys().add(Key.LEFT_SHIFT);
			this.bridge.keyTyped(character, SnapshotRunner.getKey(character));
			if (shift) {
				this.window.getKeys().remove(Key.LEFT_SHIFT);
			}
		}
	}

	private void checkCursor(final Cursor expected) {
		if (this.window.getCursor() != expected) {
			throw new IllegalStateException("The window shows the " + this.window.getCursor() + " cursor instead of " + expected);
		}
	}

	private void zoom(final double level) {
		for (final UI ui : this.bridge.getUiList()) {
			ui.load(this.window.getWidth(), this.window.getHeight(), level);
		}
	}

	private void advance(final long duration) {
		for (long elapsed = 0L; elapsed < duration; elapsed += 16L) {
			this.clock.advance(Math.min(16L, duration - elapsed));
			this.render(false);
			this.awaitPlayback();
		}
	}

	private void key(final String combination) {
		final Set<Key> added = EnumSet.noneOf(Key.class);
		Key key = Key.UNKNOWN;
		for (final String name : combination.split("\\+")) {
			key = Key.valueOf(name);
			if (this.window.getKeys().add(key)) {
				added.add(key);
			}
		}

		this.bridge.keyTyped((char) 0, key);
		this.window.getKeys().removeAll(added);
	}

	private void resize(final int width, final int height) {
		if (width > 1920 || height > 1080) {
			throw new IllegalArgumentException("The snapshot window " + width + "x" + height + " does not fit the " + 1920 + "x" + 1080 + " snapshot surface");
		}

		this.window.setWidth(width);
		this.window.setHeight(height);

		this.bridge.resize(width, height);
	}

	private void show(final String type, final boolean open) {
		final UI ui;
		try {
			ui = (UI) Class.forName(type).newInstance();
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException("Unable to create the snapshot UI " + type, e);
		}

		if (open) {
			JOID.open(ui);
		} else {
			this.bridge.closeAll();
			this.bridge.add(ui);
			this.window.setMouseX(-5000D);
			this.window.setMouseY(-5000D);
		}
		this.settle(type);
	}

	private void moveTo(final double x, final double y, final long duration) {
		final double startX = this.window.getMouseX();
		final double startY = this.window.getMouseY();
		final long steps = Math.max(1L, (duration + 15L) / 16L);
		for (long step = 1L; step <= steps; step++) {
			final double progress = Math.min(1D, step * 16D / Math.max(1L, duration));
			this.clock.advance(Math.min(16L, duration - (step - 1L) * 16L));
			this.window.setMouseX(startX + (x - startX) * progress);
			this.window.setMouseY(startY + (y - startY) * progress);
			this.render(false);
			this.bridge.mouseMoved();
		}
	}

	private void awaitPlayback() {
		for (int attempt = 0; attempt < 3000 && !SnapshotRunner.isSettled(); attempt++) {
			SnapshotRunner.sleep(10L);
			this.render(false);
		}
	}

	private void awaitResources() {
		for (int attempt = 0; attempt < 3000; attempt++) {
			if (ResourceBuilder.DEFAULT_CACHE.asMap().values().stream().allMatch(data -> data.getTasks().stream().noneMatch(Thread::isAlive) && (!data.isGenerated() || data.isLoaded() || data.isFailed()))) {
				return;
			}
			SnapshotRunner.sleep(10L);
		}
		throw new IllegalStateException("A resource never finished loading");
	}

	private SnapshotImage settle(final String name) {
		this.awaitResources();
		SnapshotImage previous = null;
		SnapshotImage current = this.mask(this.render(true));
		for (int attempt = 0; attempt < 300 && (previous == null || !current.isSame(previous) || !SnapshotRunner.isSettled()); attempt++) {
			this.awaitResources();
			SnapshotRunner.sleep(10L);
			previous = current;
			current = this.mask(this.render(true));
		}

		if (!current.isSame(previous)) {
			throw new IllegalStateException(name + " never became stable while the clock was paused, " + current.compare(previous, 0).getPixels() + " pixels kept changing");
		}

		if (!SnapshotRunner.isSettled()) {
			throw new IllegalStateException(name + " never settled: a resource kept decoding or rendering");
		}
		return current;
	}

	private SnapshotImage render(final boolean capture) {
		this.bridge.frame();

		final SnapshotImage image = capture ? this.backend.capture(this.window.getWidth(), this.window.getHeight()) : null;
		this.backend.present();
		return image;
	}

	private SnapshotImage mask(final SnapshotImage image) {
		for (final int[] mask : this.masks) {
			image.fill(mask[0], mask[1], mask[2], mask[3], 0xFFFF00FF);
		}
		return image;
	}

	private static void sleep(final long duration) {
		try {
			Thread.sleep(duration);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static boolean isSettled() {
		return ResourceBuilder.DEFAULT_CACHE.asMap().values().stream().map(ResourceData::getDecoder).allMatch(decoder -> decoder == null || decoder.isSettled());
	}

	private static boolean isShifted(final char character) {
		return character >= 'A' && character <= 'Z' || SnapshotRunner.SHIFTED_DIGITS.indexOf(character) >= 0 || SnapshotRunner.SHIFTED_SYMBOLS.indexOf(character) > 0;
	}

	private static Key getKey(final char character) {
		if (character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z') {
			return Key.valueOf(String.valueOf(Character.toUpperCase(character)));
		}

		if (character >= '0' && character <= '9') {
			return Key.valueOf("DIGIT_" + character);
		}

		final int digit = SnapshotRunner.SHIFTED_DIGITS.indexOf(character);
		if (digit >= 0) {
			return Key.valueOf("DIGIT_" + digit);
		}

		final int symbol = Math.max(SnapshotRunner.SYMBOLS.indexOf(character), SnapshotRunner.SHIFTED_SYMBOLS.indexOf(character));
		return symbol >= 0 ? SnapshotRunner.SYMBOL_KEYS[symbol] : Key.UNKNOWN;
	}

	private static List<String> read(final String scenario) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(SnapshotRunner.class.getResourceAsStream("/snapshot/" + scenario + ".txt"), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.toList());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}