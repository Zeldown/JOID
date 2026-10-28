package com.example.capture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import com.example.ExampleFont;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import dev.joid.test.snapshot.SnapshotRunner;
import dev.joid.test.snapshot.SnapshotWindowBridge;

public final class DocCapture {

	private static final Color POINTER_FILL    = new Color(231, 233, 238);
	private static final Color POINTER_RING    = new Color(91, 140, 255);
	private static final Color POINTER_OUTLINE = new Color(14, 16, 20);

	private static final int MASK = 0xFFFF00FF;

	private static final Set<String> SETTINGS = new HashSet<>(Arrays.asList("dev", "resize", "scale", "mask", "unmask"));

	private DocCapture() {}

	public static void main(final String[] arguments) throws IOException {
		Locale.setDefault(Locale.ROOT);
		final File scenarios = new File(arguments[0]);
		final File output = new File(arguments[1]);
		final List<String> names = arguments.length < 3 ? null : Arrays.asList(arguments[2].split(","));
		final File[] files = scenarios.listFiles((parent, name) -> name.endsWith(".txt") && (names == null || names.contains(name.substring(0, name.length() - 4))));
		if (files == null || files.length == 0) {
			throw new IllegalArgumentException("No capture scenario in " + scenarios + (arguments.length < 3 ? "" : " named " + arguments[2]));
		}

		Arrays.sort(files);
		output.mkdirs();
		final double[] mouse = {-5000D, -5000D};
		final List<String> failures = new ArrayList<>();
		final CursorBackend backend = new CursorBackend(new Lwjgl3SnapshotBackend());
		final SnapshotRunner runner = SnapshotRunner.start(backend);
		try {
			for (final File file : files) {
				try {
					DocCapture.capture(runner, backend, file, output, mouse);
				} catch (final RuntimeException | Error exception) {
					exception.printStackTrace();
					failures.add(file.getName() + " " + exception);
				}
			}
		} finally {
			runner.stop();
		}

		for (final String failure : failures) {
			System.err.println("[DocCapture] FAIL " + failure);
		}
		System.err.println("[DocCapture] " + (files.length - failures.size()) + " of " + files.length + " scenarios rendered");
		System.exit(failures.isEmpty() ? 0 : 1);
	}

	private static void capture(final SnapshotRunner runner, final CursorBackend backend, final File file, final File output, final double[] mouse) throws IOException {
		final List<String> commands = DocCapture.stage(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
		final Map<String, Pointer> pointers = DocCapture.trackPointer(commands, mouse);
		final Map<String, List<int[]>> masks = DocCapture.trackRegions(commands, "mask");
		final Map<String, List<int[]>> mirrors = DocCapture.trackRegions(commands, "mirror");
		BridgeHandler.UI.getBridge(WindowUIBridge.class).hoverInfo(TextInfo.create(ExampleFont.REGULAR, DocCapture.getTooltipSize(commands)).color(CaptureStyle.TEXT));
		output.mkdirs();
		int count = 0;
		for (final List<String> part : DocCapture.split(commands)) {
			final Map<String, SnapshotImage> shots = runner.execute(part.toArray(new String[0]));
			DocCapture.write(shots, backend, output, pointers, masks, mirrors);
			count += shots.size();
		}
		System.out.println(file.getName() + " -> " + count + " shots");
	}

	private static List<String> stage(final List<String> commands) {
		if (DocCapture.isBare(commands)) {
			return commands;
		}

		final List<String> staged = new ArrayList<>();
		for (final String line : commands) {
			staged.add(line);
			if (line.trim().startsWith("ui ")) {
				staged.add("open " + StageUI.class.getName());
			}
		}
		return staged;
	}

	private static boolean isBare(final List<String> commands) {
		return commands.stream().anyMatch(line -> line.trim().equals("# bare"));
	}

	private static float getTooltipSize(final List<String> commands) {
		return commands.stream().map(String::trim).filter(line -> line.startsWith("# tooltip ")).map(line -> Float.parseFloat(line.substring(10).trim())).findFirst().orElse(20F);
	}

	private static List<List<String>> split(final List<String> commands) {
		final List<List<String>> parts = new ArrayList<>();
		final List<String> settings = new ArrayList<>();
		List<String> part = new ArrayList<>();
		boolean shot = false;
		for (final String line : commands) {
			final String command = line.trim().split("\\s+")[0];
			if (command.equals("mirror")) {
				continue;
			}
			if (command.equals("ui") && shot) {
				parts.add(part);
				part = new ArrayList<>(settings);
				shot = false;
			}
			if (DocCapture.SETTINGS.contains(command)) {
				settings.add(line);
			}
			shot |= command.equals("shot");
			part.add(line);
		}
		parts.add(part);
		return parts;
	}

	private static void write(final Map<String, SnapshotImage> shots, final CursorBackend backend, final File output, final Map<String, Pointer> pointers, final Map<String, List<int[]>> masks, final Map<String, List<int[]>> mirrors) {
		final Map<String, Cursor> cursors = new HashMap<>();
		shots.forEach((name, snapshot) -> cursors.put(name, backend.getCursor(snapshot)));
		shots.entrySet().parallelStream().forEach(shot -> {
			final SnapshotImage snapshot = shot.getValue();
			final BufferedImage image = new BufferedImage(snapshot.getWidth(), snapshot.getHeight(), BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, snapshot.getWidth(), snapshot.getHeight(), snapshot.getPixels(), 0, snapshot.getWidth());
			DocCapture.paintMasks(image, masks.get(shot.getKey()));
			DocCapture.paintMirrors(image, mirrors.get(shot.getKey()));
			DocCapture.drawPointer(image, pointers.get(shot.getKey()), cursors.get(shot.getKey()));
			try {
				ImageIO.write(image, "png", new File(output, shot.getKey() + ".png"));
			} catch (final IOException exception) {
				throw new UncheckedIOException(exception);
			}
		});
	}

	private static Map<String, Pointer> trackPointer(final List<String> commands, final double[] mouse) {
		final Map<String, Pointer> pointers = new HashMap<>();
		final boolean bare = DocCapture.isBare(commands);
		double time = 0D;
		final double[] press = {-5000D, -5000D};
		double pressed = Double.NEGATIVE_INFINITY;
		boolean held = false;
		for (final String line : commands) {
			final String[] arguments = line.trim().split("\\s+");
			switch (arguments[0]) {
			case "ui":
				mouse[0] = -5000D;
				mouse[1] = -5000D;
				pressed = Double.NEGATIVE_INFINITY;
				held = false;
				break;
			case "wait":
				time += Double.parseDouble(arguments[1]);
				break;
			case "move":
				mouse[0] = Double.parseDouble(arguments[1]);
				mouse[1] = Double.parseDouble(arguments[2]);
				break;
			case "moveto":
				mouse[0] = Double.parseDouble(arguments[1]);
				mouse[1] = Double.parseDouble(arguments[2]);
				time += Double.parseDouble(arguments[3]);
				break;
			case "press":
				press[0] = mouse[0];
				press[1] = mouse[1];
				pressed = time;
				held = true;
				break;
			case "release":
				held = false;
				break;
			case "shot":
				pointers.put(arguments[1], bare ? null : new Pointer(mouse[0], mouse[1], press[0], press[1], time - pressed, held));
				break;
			default:
				break;
			}
		}
		return pointers;
	}

	private static Map<String, List<int[]>> trackRegions(final List<String> commands, final String command) {
		final Map<String, List<int[]>> regions = new HashMap<>();
		final List<int[]> current = new ArrayList<>();
		for (final String line : commands) {
			final String[] arguments = line.trim().split("\\s+");
			if (arguments[0].equals(command)) {
				current.add(Arrays.stream(arguments, 1, arguments.length).mapToInt(Integer::parseInt).toArray());
			} else if (arguments[0].equals("unmask")) {
				current.clear();
			} else if (arguments[0].equals("shot")) {
				regions.put(arguments[1], new ArrayList<>(current));
			}
		}
		return regions;
	}

	private static void paintMasks(final BufferedImage image, final List<int[]> masks) {
		for (final int[] mask : masks) {
			final int color = DocCapture.getSurrounding(image, mask);
			for (int y = Math.max(0, mask[1]); y < Math.min(image.getHeight(), mask[1] + mask[3]); y++) {
				for (int x = Math.max(0, mask[0]); x < Math.min(image.getWidth(), mask[0] + mask[2]); x++) {
					image.setRGB(x, y, color);
				}
			}
		}
	}

	private static void paintMirrors(final BufferedImage image, final List<int[]> mirrors) {
		for (final int[] mirror : mirrors) {
			final int[] pixels = image.getRGB(mirror[0], mirror[1], mirror[2], mirror[3], null, 0, mirror[2]);
			for (int y = 0; y < mirror[3]; y++) {
				for (int x = 0; x < mirror[2]; x++) {
					image.setRGB(mirror[4] + mirror[2] - 1 - x, mirror[1] + y, pixels[y * mirror[2] + x]);
				}
			}
		}
	}

	private static int getSurrounding(final BufferedImage image, final int[] mask) {
		final Map<Integer, Integer> counts = new HashMap<>();
		for (int y = mask[1] - 1; y <= mask[1] + mask[3]; y++) {
			for (int x = mask[0] - 1; x <= mask[0] + mask[2]; x++) {
				final boolean ring = y == mask[1] - 1 || y == mask[1] + mask[3] || x == mask[0] - 1 || x == mask[0] + mask[2];
				if (ring && x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight() && image.getRGB(x, y) != DocCapture.MASK) {
					counts.merge(image.getRGB(x, y), 1, Integer::sum);
				}
			}
		}
		return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0xFF0E1014);
	}

	private static void drawPointer(final BufferedImage image, final Pointer pointer, final Cursor cursor) {
		if (pointer == null) {
			return;
		}

		final int x = (int) Math.round(pointer.x);
		final int y = (int) Math.round(pointer.y);
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
			return;
		}

		final Shape shape = AffineTransform.getTranslateInstance(x, y).createTransformedShape(DocCapture.getCursorShape(cursor == null ? Cursor.DEFAULT : cursor));
		final Graphics2D graphics = image.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			DocCapture.drawPress(graphics, pointer);
			graphics.setColor(DocCapture.POINTER_FILL);
			graphics.fill(shape);
			graphics.setColor(DocCapture.POINTER_OUTLINE);
			graphics.setStroke(new BasicStroke(2F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			graphics.draw(shape);
		} finally {
			graphics.dispose();
		}
	}

	private static Shape getCursorShape(final Cursor cursor) {
		switch (cursor) {
		case POINTER:
			return DocCapture.createHand();
		case TEXT:
			return DocCapture.createPolygon(-5D, -12D, 5D, -12D, 5D, -9.5D, 1.25D, -9.5D, 1.25D, 9.5D, 5D, 9.5D, 5D, 12D, -5D, 12D, -5D, 9.5D, -1.25D, 9.5D, -1.25D, -9.5D, -5D, -9.5D);
		case CROSSHAIR:
			return DocCapture.createPolygon(-1.25D, -11D, 1.25D, -11D, 1.25D, -1.25D, 11D, -1.25D, 11D, 1.25D, 1.25D, 1.25D, 1.25D, 11D, -1.25D, 11D, -1.25D, 1.25D, -11D, 1.25D, -11D, -1.25D, -1.25D, -1.25D);
		case MOVE:
			return DocCapture.createPolygon(0D, -13D, 5.5D, -7.5D, 2.25D, -7.5D, 2.25D, -2.25D, 7.5D, -2.25D, 7.5D, -5.5D, 13D, 0D, 7.5D, 5.5D, 7.5D, 2.25D, 2.25D, 2.25D, 2.25D, 7.5D, 5.5D, 7.5D, 0D, 13D, -5.5D, 7.5D, -2.25D, 7.5D, -2.25D, 2.25D, -7.5D, 2.25D, -7.5D, 5.5D, -13D, 0D, -7.5D, -5.5D, -7.5D, -2.25D, -2.25D, -2.25D, -2.25D, -7.5D, -5.5D, -7.5D);
		case NOT_ALLOWED:
			return DocCapture.createForbidden();
		case RESIZE_EW:
			return DocCapture.createDoubleArrow(0D);
		case RESIZE_NS:
			return DocCapture.createDoubleArrow(90D);
		case RESIZE_NWSE:
			return DocCapture.createDoubleArrow(45D);
		case RESIZE_NESW:
			return DocCapture.createDoubleArrow(-45D);
		default:
			return DocCapture.createPolygon(0D, 0D, 0D, 25D, 6D, 19D, 10D, 29D, 14D, 27D, 11D, 18D, 18D, 18D);
		}
	}

	private static Shape createHand() {
		final Area hand = new Area(new RoundRectangle2D.Double(-2.5D, 0D, 5D, 16D, 5D, 5D));
		hand.add(new Area(new RoundRectangle2D.Double(2.5D, 7D, 4.5D, 11D, 4.5D, 4.5D)));
		hand.add(new Area(new RoundRectangle2D.Double(7D, 8.5D, 4.5D, 10D, 4.5D, 4.5D)));
		hand.add(new Area(new RoundRectangle2D.Double(11.5D, 10D, 4.5D, 9D, 4.5D, 4.5D)));
		hand.add(new Area(new RoundRectangle2D.Double(-2.5D, 13D, 18.5D, 13D, 8D, 8D)));
		hand.add(new Area(AffineTransform.getRotateInstance(Math.toRadians(-40D), -2D, 21D).createTransformedShape(new RoundRectangle2D.Double(-4.5D, 11D, 4.5D, 11D, 4.5D, 4.5D))));
		return hand;
	}

	private static Shape createForbidden() {
		final Area forbidden = new Area(new Ellipse2D.Double(-10D, -10D, 20D, 20D));
		forbidden.subtract(new Area(new Ellipse2D.Double(-7D, -7D, 14D, 14D)));
		forbidden.add(new Area(AffineTransform.getRotateInstance(Math.toRadians(45D)).createTransformedShape(new RoundRectangle2D.Double(-8.5D, -1.5D, 17D, 3D, 1D, 1D))));
		return forbidden;
	}

	private static Shape createDoubleArrow(final double angle) {
		return AffineTransform.getRotateInstance(Math.toRadians(angle)).createTransformedShape(DocCapture.createPolygon(-12D, 0D, -6.5D, -5.5D, -6.5D, -2.25D, 6.5D, -2.25D, 6.5D, -5.5D, 12D, 0D, 6.5D, 5.5D, 6.5D, 2.25D, -6.5D, 2.25D, -6.5D, 5.5D));
	}

	private static Shape createPolygon(final double... points) {
		final Path2D polygon = new Path2D.Double();
		polygon.moveTo(points[0], points[1]);
		for (int index = 2; index < points.length; index += 2) {
			polygon.lineTo(points[index], points[index + 1]);
		}
		polygon.closePath();
		return polygon;
	}

	private static void drawPress(final Graphics2D graphics, final Pointer pointer) {
		if (pointer.held) {
			graphics.setColor(DocCapture.withAlpha(DocCapture.POINTER_RING, 0.3D));
			graphics.fill(new Ellipse2D.Double(pointer.x - 14D, pointer.y - 14D, 28D, 28D));
			DocCapture.drawRing(graphics, pointer.x, pointer.y, 14D, 1D);
			return;
		}
		if (pointer.pressAge < 0D || pointer.pressAge >= 450D) {
			return;
		}

		final double progress = pointer.pressAge / 450D;
		final double radius = 14D + 12D * (1D - Math.pow(1D - progress, 3D));
		graphics.setColor(DocCapture.withAlpha(DocCapture.POINTER_RING, 0.3D * (1D - progress)));
		graphics.fill(new Ellipse2D.Double(pointer.pressX - 14D, pointer.pressY - 14D, 28D, 28D));
		DocCapture.drawRing(graphics, pointer.pressX, pointer.pressY, radius, 1D - progress);
	}

	private static void drawRing(final Graphics2D graphics, final double x, final double y, final double radius, final double alpha) {
		final Ellipse2D ring = new Ellipse2D.Double(x - radius, y - radius, radius * 2D, radius * 2D);
		graphics.setColor(DocCapture.withAlpha(DocCapture.POINTER_OUTLINE, alpha * 0.9D));
		graphics.setStroke(new BasicStroke(6F));
		graphics.draw(ring);
		graphics.setColor(DocCapture.withAlpha(DocCapture.POINTER_RING, alpha));
		graphics.setStroke(new BasicStroke(3F));
		graphics.draw(ring);
	}

	private static Color withAlpha(final Color color, final double alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) Math.round(Math.max(0D, Math.min(1D, alpha)) * 255D));
	}

	private static final class Pointer {

		private final double x;
		private final double y;
		private final double pressX;
		private final double pressY;
		private final double pressAge;
		private final boolean held;

		private Pointer(final double x, final double y, final double pressX, final double pressY, final double pressAge, final boolean held) {
			this.x = x;
			this.y = y;
			this.pressX = pressX;
			this.pressY = pressY;
			this.pressAge = pressAge;
			this.held = held;
		}

	}

	private static final class CursorBackend implements ISnapshotBackend {

		private final ISnapshotBackend           backend;
		private final Map<SnapshotImage, Cursor> cursors = new WeakHashMap<>();

		private CursorBackend(final ISnapshotBackend backend) {
			this.backend = backend;
		}

		@Override
		public void create(final int width, final int height) {
			this.backend.create(width, height);
		}

		@Override
		public void destroy() {
			this.backend.destroy();
		}

		@Override
		public void present() {
			this.backend.present();
		}

		@Override
		public SnapshotImage capture(final int width, final int height) {
			final SnapshotImage image = this.backend.capture(width, height);
			this.cursors.put(image, ((SnapshotWindowBridge) BridgeHandler.WINDOW.get()).getCursor());
			return image;
		}

		@Override
		public String getRenderer() {
			return this.backend.getRenderer();
		}

		private Cursor getCursor(final SnapshotImage image) {
			return this.cursors.get(image);
		}

	}

}