package be.zeldown.joid.test.snapshot;

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

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.asset.dto.locator.AssetLocator;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.clock.ManualClockBridge;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class SnapshotRunner {

	private static final int WIDTH  = 1920;
	private static final int HEIGHT = 1080;

	private static final long FRAME_TIME      = 16L;
	private static final int  SETTLE_ATTEMPTS = 300;
	private static final long SETTLE_DELAY    = 10L;
	private static final int  LOAD_ATTEMPTS   = 3000;
	private static final long START_TIME      = 1735689600000L;

	private static final int   MASK       = 0xFFFF00FF;
	private static final Color BACKGROUND = new Color(50, 50, 50);

	private static final List<String> SCENARIOS = Arrays.asList("dev", "popup", "video", "static", "window", "transition", "interaction");

	private static final String SHIFTED_DIGITS  = ")!@#$%^&*(";
	private static final String SYMBOLS         = " '-,./;=[\\]`";
	private static final String SHIFTED_SYMBOLS = " \"_<>?:+{|}~";
	private static final Key[]  SYMBOL_KEYS     = {Key.SPACE, Key.APOSTROPHE, Key.MINUS, Key.COMMA, Key.PERIOD, Key.SLASH, Key.SEMICOLON, Key.EQUAL, Key.LEFT_BRACKET, Key.BACKSLASH, Key.RIGHT_BRACKET, Key.GRAVE_ACCENT};

	private final ManualClockBridge    clock;
	private final List<int[]>          masks;
	private final SnapshotWindowBridge window;
	private final SnapshotUIBridge     bridge;
	private final ISnapshotBackend     backend;

	private ClickType pressed;
	private long      pressTime;

	private SnapshotRunner(final ISnapshotBackend backend) {
		this.clock   = ManualClockBridge.create(SnapshotRunner.START_TIME);
		this.masks   = new ArrayList<>();
		this.window  = new SnapshotWindowBridge();
		this.bridge  = new SnapshotUIBridge();
		this.backend = backend;
	}

	public static @NonNull SnapshotRunner start(final @NonNull ISnapshotBackend backend) {
		final SnapshotRunner runner = new SnapshotRunner(backend);
		backend.create(SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT);
		BridgeHandler.CLOCK.register(runner.clock);
		BridgeHandler.WINDOW.register(runner.window);
		BridgeHandler.AUDIO.register(new SnapshotAudioBridge());
		AssetLocator.register(new SnapshotUrlLocator(SnapshotSettings.getCache()));
		runner.resize(SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT);

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
		this.bridge.closeAll();
		this.window.getKeys().clear();
		this.masks.clear();
		this.clock.setTime(SnapshotRunner.START_TIME);
		JOID.inst().setDevMode(false);
		this.resize(SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT);

		final Map<String, SnapshotImage> shots = new LinkedHashMap<>();
		for (final String line : SnapshotRunner.read(scenario)) {
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
				break;
			case "moveto":
				this.moveTo(Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2]), Long.parseLong(arguments[3]));
				break;
			case "press":
				this.pressed   = ClickType.valueOf(arguments[1]);
				this.pressTime = this.clock.currentTimeMillis();
				this.bridge.mousePressed(this.pressed);
				break;
			case "release":
				this.bridge.mouseReleased(this.pressed);
				this.pressed = null;
				break;
			case "scroll":
				this.bridge.mouseScroll(Integer.parseInt(arguments[1]));
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
			case "dev":
				JOID.inst().setDevMode(Boolean.parseBoolean(arguments[1]));
				break;
			case "mask":
				this.masks.add(new int[] {Integer.parseInt(arguments[1]), Integer.parseInt(arguments[2]), Integer.parseInt(arguments[3]), Integer.parseInt(arguments[4])});
				break;
			case "unmask":
				this.masks.clear();
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

	private void zoom(final double level) {
		for (final UI ui : this.bridge.getUiList()) {
			ui.load(this.window.getWidth(), this.window.getHeight(), level);
		}
	}

	private void advance(final long duration) {
		for (long elapsed = 0L; elapsed < duration; elapsed += SnapshotRunner.FRAME_TIME) {
			this.clock.advance(SnapshotRunner.FRAME_TIME);
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
		this.window.setWidth(width);
		this.window.setHeight(height);

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.ortho(0D, width, height, 0D, 0D, 10000D);
		render.viewport(0, 0, width, height);
		this.bridge.load();
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
		final long steps = Math.max(1L, duration / SnapshotRunner.FRAME_TIME);
		for (long step = 1L; step <= steps; step++) {
			final double progress = (double) step / steps;
			this.clock.advance(SnapshotRunner.FRAME_TIME);
			this.window.setMouseX(startX + (x - startX) * progress);
			this.window.setMouseY(startY + (y - startY) * progress);
			if (this.pressed != null) {
				this.bridge.mouseDragged(this.pressed, this.clock.currentTimeMillis() - this.pressTime);
			}
			this.render(false);
		}
	}

	private void awaitPlayback() {
		for (int attempt = 0; attempt < SnapshotRunner.LOAD_ATTEMPTS && !SnapshotRunner.isPlaybackSynchronized(); attempt++) {
			SnapshotRunner.sleep(SnapshotRunner.SETTLE_DELAY);
			this.render(false);
		}
	}

	private void awaitResources() {
		for (int attempt = 0; attempt < SnapshotRunner.LOAD_ATTEMPTS; attempt++) {
			if (ResourceBuilder.DEFAULT_CACHE.asMap().values().stream().allMatch(data -> !data.isGenerated() || data.isLoaded())) {
				return;
			}
			SnapshotRunner.sleep(SnapshotRunner.SETTLE_DELAY);
		}
		throw new IllegalStateException("A resource never finished loading");
	}

	private SnapshotImage settle(final String name) {
		SnapshotImage previous = null;
		SnapshotImage current = this.mask(this.render(true));
		for (int attempt = 0; attempt < SnapshotRunner.SETTLE_ATTEMPTS && (previous == null || !current.isSame(previous) || !SnapshotRunner.isPlaybackSynchronized()); attempt++) {
			this.awaitResources();
			SnapshotRunner.sleep(SnapshotRunner.SETTLE_DELAY);
			previous = current;
			current = this.mask(this.render(true));
		}

		if (!current.isSame(previous)) {
			throw new IllegalStateException(name + " never became stable while the clock was paused, " + current.compare(previous, 0).getPixels() + " pixels kept changing");
		}

		if (!SnapshotRunner.isPlaybackSynchronized()) {
			throw new IllegalStateException(name + " never caught up with the clock while playing a video");
		}
		return current;
	}

	private SnapshotImage render(final boolean capture) {
		this.bridge.update();
		this.backend.frame(() -> {
			BridgeHandler.RENDER.get().clear(0F, 0F, 0F, 0F);
			DrawUtils.SHAPE.drawRect(0, 0, this.window.getWidth(), this.window.getHeight(), SnapshotRunner.BACKGROUND);
			this.bridge.draw();
		});

		final SnapshotImage image = capture ? this.backend.capture(this.window.getWidth(), this.window.getHeight()) : null;
		this.backend.present();
		return image;
	}

	private SnapshotImage mask(final SnapshotImage image) {
		for (final int[] mask : this.masks) {
			image.fill(mask[0], mask[1], mask[2], mask[3], SnapshotRunner.MASK);
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

	private static boolean isPlaybackSynchronized() {
		return ResourceBuilder.DEFAULT_CACHE.asMap().values().stream().map(data -> data.getDecoder(VideoResourceDecoder.class)).allMatch(decoder -> decoder == null || decoder.isSynchronized());
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

	private static boolean isShifted(final char character) {
		return character >= 'A' && character <= 'Z' || SnapshotRunner.SHIFTED_DIGITS.indexOf(character) >= 0 || SnapshotRunner.SHIFTED_SYMBOLS.indexOf(character) > 0;
	}

	private static List<String> read(final String scenario) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(SnapshotRunner.class.getResourceAsStream("/snapshot/" + scenario + ".txt"), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.toList());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}