package be.zeldown.joid.test.snapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.clock.ManualClockBridge;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.key.Key;
import lombok.NonNull;

public final class SnapshotRunner {

	private static final int   WIDTH           = 1920;
	private static final int   HEIGHT          = 1080;
	private static final long  START_TIME      = 1735689600000L;
	private static final long  FRAME_TIME      = 16L;
	private static final int   SETTLE_ATTEMPTS = 300;
	private static final long  SETTLE_DELAY    = 10L;
	private static final int   LOAD_ATTEMPTS   = 3000;
	private static final Color BACKGROUND      = new Color(50, 50, 50);

	private final ISnapshotBackend     backend;
	private final ManualClockBridge    clock;
	private final SnapshotWindowBridge window;
	private final SnapshotUIBridge     bridge;

	private ClickType pressed;
	private long      pressTime;

	private SnapshotRunner(final ISnapshotBackend backend) {
		this.backend = backend;
		this.clock   = ManualClockBridge.create(SnapshotRunner.START_TIME);
		this.window  = new SnapshotWindowBridge(SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT);
		this.bridge  = new SnapshotUIBridge();
	}

	public static @NonNull SnapshotRunner start(final @NonNull ISnapshotBackend backend) {
		final SnapshotRunner runner = new SnapshotRunner(backend);
		backend.create(SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT);
		BridgeHandler.CLOCK.register(runner.clock);
		BridgeHandler.WINDOW.register(runner.window);

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.ortho(0D, SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT, 0D, 0D, 10000D);
		render.viewport(0, 0, SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT);

		JOID.inst().setDevMode(false).setDemoMode(true).load();
		BridgeHandler.UI.register(runner.bridge);
		return runner;
	}

	public void stop() {
		this.bridge.closeAll();
		this.backend.destroy();
	}

	public @NonNull Map<String, SnapshotImage> run(final @NonNull String scenario) {
		final Map<String, SnapshotImage> shots = new LinkedHashMap<>();
		for (final String line : SnapshotRunner.read(scenario)) {
			final String command = line.trim();
			if (command.isEmpty() || command.startsWith("#")) {
				continue;
			}

			final String[] arguments = command.split("\\s+");
			switch (arguments[0]) {
			case "ui":
				this.open(arguments[1]);
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
				for (final char character : command.substring(5).toCharArray()) {
					this.bridge.keyTyped(character, SnapshotRunner.getKey(character));
				}
				break;
			case "key":
				this.bridge.keyTyped((char) 0, Key.valueOf(arguments[1]));
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

	private void open(final String type) {
		this.bridge.closeAll();
		try {
			this.bridge.add((UI) Class.forName(type).newInstance());
		} catch (final ReflectiveOperationException e) {
			throw new IllegalArgumentException("Unable to open the snapshot UI " + type, e);
		}

		this.window.setMouseX(-5000D);
		this.window.setMouseY(-5000D);
		this.settle(type);
	}

	private void advance(final long duration) {
		for (long elapsed = 0L; elapsed < duration; elapsed += SnapshotRunner.FRAME_TIME) {
			this.clock.advance(SnapshotRunner.FRAME_TIME);
			this.render(false);
		}
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

	private SnapshotImage settle(final String name) {
		SnapshotImage previous = null;
		SnapshotImage current = this.render(true);
		for (int attempt = 0; attempt < SnapshotRunner.SETTLE_ATTEMPTS && (previous == null || !current.isSame(previous)); attempt++) {
			this.awaitResources();
			SnapshotRunner.sleep(SnapshotRunner.SETTLE_DELAY);
			previous = current;
			current = this.render(true);
		}

		if (!current.isSame(previous)) {
			throw new IllegalStateException(name + " never became stable while the clock was paused, " + current.compare(previous, 0).getPixels() + " pixels kept changing");
		}
		return current;
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

	private SnapshotImage render(final boolean capture) {
		this.bridge.update();
		this.backend.frame(() -> {
			BridgeHandler.RENDER.get().clear(0F, 0F, 0F, 0F);
			DrawUtils.SHAPE.drawRect(0, 0, SnapshotRunner.WIDTH, SnapshotRunner.HEIGHT, SnapshotRunner.BACKGROUND);
			this.bridge.draw();
		});

		final SnapshotImage image = capture ? this.backend.capture() : null;
		this.backend.present();
		return image;
	}

	private static List<String> read(final String scenario) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(SnapshotRunner.class.getResourceAsStream("/snapshot/" + scenario + ".txt"), StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.toList());
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Key getKey(final char character) {
		if (Character.isLetter(character)) {
			return Key.valueOf(String.valueOf(Character.toUpperCase(character)));
		}

		if (Character.isDigit(character)) {
			return Key.valueOf("DIGIT_" + character);
		}
		return character == ' ' ? Key.SPACE : Key.UNKNOWN;
	}

	private static void sleep(final long duration) {
		try {
			Thread.sleep(duration);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}