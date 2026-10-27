package com.example.capture;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import dev.joid.test.snapshot.SnapshotRunner;

public final class DocCapture {

	private static final int[] POINTER_X = {0, 0, 6, 10, 14, 11, 18};
	private static final int[] POINTER_Y = {0, 25, 19, 29, 27, 18, 18};

	private static final int MASK = 0xFFFF00FF;

	private DocCapture() {}

	public static void main(final String[] arguments) throws IOException {
		Locale.setDefault(Locale.ROOT);
		final File scenarios = new File(arguments[0]);
		final File output = new File(arguments[1]);
		final File[] files = scenarios.listFiles((parent, name) -> name.endsWith(".txt") && (arguments.length < 3 || name.equals(arguments[2] + ".txt")));
		if (files == null || files.length == 0) {
			throw new IllegalArgumentException("No capture scenario in " + scenarios + (arguments.length < 3 ? "" : " named " + arguments[2]));
		}

		Arrays.sort(files);
		output.mkdirs();
		final double[] mouse = {-5000D, -5000D};
		final List<String> failures = new ArrayList<>();
		final SnapshotRunner runner = SnapshotRunner.start(new Lwjgl3SnapshotBackend());
		try {
			for (final File file : files) {
				try {
					DocCapture.capture(runner, file, output, mouse);
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

	private static void capture(final SnapshotRunner runner, final File file, final File output, final double[] mouse) throws IOException {
		final List<String> commands = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		final Map<String, double[]> pointers = DocCapture.trackPointer(commands, mouse);
		final Map<String, List<int[]>> masks = DocCapture.trackRegions(commands, "mask");
		final Map<String, List<int[]>> mirrors = DocCapture.trackRegions(commands, "mirror");
		final Map<String, SnapshotImage> shots = runner.execute(commands.stream().filter(line -> !line.trim().startsWith("mirror ")).toArray(String[]::new));
		for (final Map.Entry<String, SnapshotImage> shot : shots.entrySet()) {
			final File target = new File(output, shot.getKey() + ".png");
			shot.getValue().write(target);
			DocCapture.paintMasks(target, masks.get(shot.getKey()));
			DocCapture.paintMirrors(target, mirrors.get(shot.getKey()));
			DocCapture.drawPointer(target, pointers.get(shot.getKey()));
			System.out.println(file.getName() + " -> " + shot.getKey() + ".png");
		}
	}

	private static Map<String, double[]> trackPointer(final List<String> commands, final double[] mouse) {
		final Map<String, double[]> pointers = new HashMap<>();
		for (final String line : commands) {
			final String[] arguments = line.trim().split("\\s+");
			switch (arguments[0]) {
			case "ui":
				mouse[0] = -5000D;
				mouse[1] = -5000D;
				break;
			case "move":
			case "moveto":
				mouse[0] = Double.parseDouble(arguments[1]);
				mouse[1] = Double.parseDouble(arguments[2]);
				break;
			case "shot":
				pointers.put(arguments[1], new double[] {mouse[0], mouse[1]});
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

	private static void paintMasks(final File file, final List<int[]> masks) throws IOException {
		if (masks.isEmpty()) {
			return;
		}

		final BufferedImage image = ImageIO.read(file);
		for (final int[] mask : masks) {
			final int color = DocCapture.getSurrounding(image, mask);
			for (int y = Math.max(0, mask[1]); y < Math.min(image.getHeight(), mask[1] + mask[3]); y++) {
				for (int x = Math.max(0, mask[0]); x < Math.min(image.getWidth(), mask[0] + mask[2]); x++) {
					image.setRGB(x, y, color);
				}
			}
		}
		ImageIO.write(image, "png", file);
	}

	private static void paintMirrors(final File file, final List<int[]> mirrors) throws IOException {
		if (mirrors.isEmpty()) {
			return;
		}

		final BufferedImage image = ImageIO.read(file);
		for (final int[] mirror : mirrors) {
			final int[] pixels = image.getRGB(mirror[0], mirror[1], mirror[2], mirror[3], null, 0, mirror[2]);
			for (int y = 0; y < mirror[3]; y++) {
				for (int x = 0; x < mirror[2]; x++) {
					image.setRGB(mirror[4] + mirror[2] - 1 - x, mirror[1] + y, pixels[y * mirror[2] + x]);
				}
			}
		}
		ImageIO.write(image, "png", file);
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
		return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0xFFDDDDDD);
	}

	private static void drawPointer(final File file, final double[] pointer) throws IOException {
		final BufferedImage image = ImageIO.read(file);
		final int x = (int) Math.round(pointer[0]);
		final int y = (int) Math.round(pointer[1]);
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
			return;
		}

		final Polygon arrow = new Polygon();
		for (int index = 0; index < DocCapture.POINTER_X.length; index++) {
			arrow.addPoint(x + DocCapture.POINTER_X[index], y + DocCapture.POINTER_Y[index]);
		}

		final Graphics2D graphics = image.createGraphics();
		try {
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(Color.BLACK);
			graphics.fillPolygon(arrow);
			graphics.setColor(Color.WHITE);
			graphics.setStroke(new BasicStroke(2F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			graphics.drawPolygon(arrow);
		} finally {
			graphics.dispose();
		}
		ImageIO.write(image, "png", file);
	}

}