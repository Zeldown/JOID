package com.example.capture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.node.Node;
import dev.joid.test.snapshot.SnapshotRunner;
import dev.joid.test.snapshot.SnapshotUIBridge;

public final class RunExamples {

	private static final String PACKAGE = "com.example.";
	private static final String CAPTURE = "com.example.capture.";

	private static final String[] LOADERS = {"ResourceAsync/", "ResourceTask/", "ResourceVector/", "MsdfFontLoader/"};

	private RunExamples() {}

	public static void main(final String[] arguments) throws IOException, ReflectiveOperationException {
		Locale.setDefault(Locale.ROOT);
		final List<File> roots = new ArrayList<>();
		for (final String root : arguments[0].split(File.pathSeparator)) {
			roots.add(new File(root));
		}
		final Map<String, List<String>> skipped = RunExamples.readList(new File(arguments[1]), "skip");
		final Map<String, List<String>> expected = RunExamples.readList(new File(arguments[1]), "expect");
		final Map<String, List<String>> calls = RunExamples.readList(new File(arguments[1]), "call");
		final File report = new File(arguments[2]);
		final String only = arguments.length > 3 ? arguments[3] : null;

		final Set<String> classes = new TreeSet<>();
		for (final File root : roots) {
			RunExamples.collect(root, root, classes);
		}

		final Set<String> uis = RunExamples.findUis(classes);
		final ByteArrayOutputStream errors = new ByteArrayOutputStream();
		final PrintStream console = System.err;
		System.setErr(new PrintStream(new TeeOutputStream(console, errors), true, "UTF-8"));
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> exception.printStackTrace());

		final Map<String, List<String>> failures = new LinkedHashMap<>();
		int ran = 0;
		final SnapshotRunner runner = SnapshotRunner.start(new Lwjgl3SnapshotBackend());
		try {
			for (final String ui : uis) {
				if (only != null && !ui.equals(only) && !ui.startsWith(only + ".") && !ui.startsWith(only + "$")) {
					continue;
				}
				ran++;
				errors.reset();
				int clicks = 0;
				try {
					runner.execute("dev true", "ui " + ui, "wait 200");
					final List<double[]> targets = RunExamples.targets();
					clicks = targets.size();
					runner.execute(RunExamples.interactions(ui, targets));
					RunExamples.settle();
				} catch (final RuntimeException | LinkageError exception) {
					exception.printStackTrace();
				}

				final List<String> output = RunExamples.filter(new String(errors.toByteArray(), StandardCharsets.UTF_8), expected.getOrDefault(ui, Collections.emptyList()));
				if (!output.isEmpty()) {
					failures.put(ui, output);
				}
				console.println((output.isEmpty() ? "ok   " : "FAIL ") + ui + " (" + clicks + " nodes clicked)");
			}

			for (final Map.Entry<String, List<String>> call : calls.entrySet()) {
				if (only != null && !call.getKey().equals(only) && !call.getKey().startsWith(only + ".")) {
					continue;
				}
				for (final String member : call.getValue()) {
					ran++;
					errors.reset();
					runner.execute("dev true");
					try {
						RunExamples.call(call.getKey(), member);
					} catch (final ReflectiveOperationException | RuntimeException | LinkageError exception) {
						(exception instanceof InvocationTargetException ? exception.getCause() : exception).printStackTrace();
					}

					final List<String> output = RunExamples.filter(new String(errors.toByteArray(), StandardCharsets.UTF_8), expected.getOrDefault(call.getKey(), Collections.emptyList()));
					if (!output.isEmpty()) {
						failures.put(call.getKey() + "#" + member, output);
					}
					console.println((output.isEmpty() ? "ok   " : "FAIL ") + call.getKey() + "#" + member);
				}
			}
		} finally {
			runner.stop();
		}

		final List<String> lines = new ArrayList<>();
		lines.add("# run-examples: " + ran + " UIs and calls run, " + failures.size() + " failed");
		for (final Map.Entry<String, List<String>> failure : failures.entrySet()) {
			final List<String> output = failure.getValue();
			lines.add("FAIL " + failure.getKey() + " " + output.get(0));
			for (int index = 1; index < Math.min(output.size(), 12); index++) {
				lines.add("     " + output.get(index));
			}
		}

		if (only == null) {
			final Method loaded = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
			loaded.setAccessible(true);
			final ClassLoader loader = RunExamples.class.getClassLoader();
			for (final String name : classes) {
				if (name.contains("$") || uis.contains(name) || name.startsWith(RunExamples.CAPTURE)) {
					continue;
				}
				final boolean used = loaded.invoke(loader, name) != null;
				if (!used && !skipped.containsKey(name)) {
					lines.add("UNUSED " + name + " no UI runs it: add a UI that uses it, or skip it in run-examples.txt with the reason");
				} else if (used && skipped.containsKey(name)) {
					lines.add("STALE " + name + " runs with the UIs: remove its skip line from run-examples.txt");
				}
			}
			for (final String name : skipped.keySet()) {
				if (!classes.contains(name)) {
					lines.add("STALE " + name + " does not exist: remove its skip line from run-examples.txt");
				}
			}
			for (final String name : expected.keySet()) {
				if (!uis.contains(name)) {
					lines.add("STALE " + name + " is not a UI that runs: remove its expect line from run-examples.txt");
				}
			}
		}

		report.getParentFile().mkdirs();
		Files.write(report.toPath(), lines, StandardCharsets.UTF_8);
		for (final String line : lines) {
			console.println(line);
		}
		System.exit(0);
	}

	private static List<double[]> targets() {
		final List<double[]> targets = new ArrayList<>();
		final Set<String> seen = new HashSet<>();
		for (final UI ui : BridgeHandler.UI.getBridge(SnapshotUIBridge.class).getUiList()) {
			for (final Node node : ui.getNodeList()) {
				RunExamples.collectTargets(ui, node, targets, seen);
			}
		}
		return targets.size() > 40 ? targets.subList(0, 40) : targets;
	}

	private static void collectTargets(final UI ui, final Node node, final List<double[]> targets, final Set<String> seen) {
		if (RunExamples.hasListener(node) && node.isVisible() && node.isInteractive() && node.getWidth() > 0D && node.getHeight() > 0D) {
			final double x = Math.round(ui.getView().toScreenX(node.getAbsoluteX() + node.getWidth() / 2D));
			final double y = Math.round(ui.getView().toScreenY(node.getAbsoluteY() + node.getHeight() / 2D));
			if (x >= 0D && y >= 0D && x < ui.getWindowWidth() && y < ui.getWindowHeight() && seen.add(x + " " + y)) {
				targets.add(new double[] {x, y});
			}
		}
		for (final Node child : node.getChildren(Node.class)) {
			RunExamples.collectTargets(ui, child, targets, seen);
		}
	}

	private static boolean hasListener(final Node node) {
		for (Class<?> type = node.getClass(); type != null; type = type.getSuperclass()) {
			for (final Field field : type.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) && field.getType() == EventType.class) {
					try {
						field.setAccessible(true);
						if (node.hasListener((EventType<?>) field.get(null))) {
							return true;
						}
					} catch (final IllegalAccessException exception) {
						throw new IllegalStateException(exception);
					}
				}
			}
		}
		return false;
	}

	private static String[] interactions(final String ui, final List<double[]> targets) {
		final List<String> commands = new ArrayList<>();
		commands.add("dev true");
		commands.add("ui " + ui);
		commands.add("wait 200");
		for (final double[] target : targets) {
			commands.add("moveto " + target[0] + " " + target[1] + " 48");
			commands.add("wait 100");
			commands.add("press LEFT");
			commands.add("release LEFT");
			commands.add("wait 200");
		}
		commands.add("moveto -5000 -5000 16");
		commands.add("wait 200");
		return commands.toArray(new String[0]);
	}

	private static Set<String> findUis(final Set<String> classes) throws IOException {
		final Set<String> uis = new TreeSet<>();
		final List<URL> urls = new ArrayList<>();
		for (final String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
			urls.add(new File(entry).toURI().toURL());
		}

		try (URLClassLoader discovery = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent())) {
			final Class<?> base = Class.forName(UI.class.getName(), false, discovery);
			for (final String name : classes) {
				if (name.startsWith(RunExamples.CAPTURE) || name.matches(".*\\$\\d.*")) {
					continue;
				}
				final Class<?> type = Class.forName(name, false, discovery);
				if (base.isAssignableFrom(type) && Modifier.isPublic(type.getModifiers()) && !Modifier.isAbstract(type.getModifiers()) && RunExamples.hasPublicConstructor(type)) {
					uis.add(name);
				}
			}
		} catch (final ClassNotFoundException exception) {
			throw new IllegalStateException("Unable to inspect the examples", exception);
		}
		return uis;
	}

	private static boolean hasPublicConstructor(final Class<?> type) {
		try {
			return Modifier.isPublic(type.getConstructor().getModifiers());
		} catch (final NoSuchMethodException exception) {
			return false;
		}
	}

	private static void collect(final File root, final File directory, final Set<String> classes) {
		final File[] files = directory.listFiles();
		if (files == null) {
			return;
		}

		for (final File file : files) {
			if (file.isDirectory()) {
				RunExamples.collect(root, file, classes);
			} else if (file.getName().endsWith(".class")) {
				final String path = root.toURI().relativize(file.toURI()).getPath();
				final String name = path.substring(0, path.length() - 6).replace('/', '.');
				if (name.startsWith(RunExamples.PACKAGE)) {
					classes.add(name);
				}
			}
		}
	}

	private static void call(final String type, final String member) throws ReflectiveOperationException {
		final Class<?> owner = Class.forName(type);
		final Object result = member.equals("<clinit>") ? null : RunExamples.isField(owner, member) ? owner.getField(member).get(null) : owner.getMethod(member).invoke(null);
		if (result instanceof CompletableFuture) {
			((CompletableFuture<?>) result).join();
		}
		RunExamples.settle();
	}

	private static void settle() {
		final long deadline = System.currentTimeMillis() + 15000L;
		do {
			BridgeHandler.UI.frame();
			RunExamples.sleep();
		} while (RunExamples.isLoading() && System.currentTimeMillis() < deadline);

		if (RunExamples.isLoading()) {
			System.err.println("[run-examples] A resource or a font still loads 15 s after the example ran");
		}
		for (int frame = 0; frame < 10; frame++) {
			BridgeHandler.UI.frame();
		}
	}

	private static boolean isLoading() {
		for (final Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			final String name = entry.getKey().getName();
			if (Arrays.stream(RunExamples.LOADERS).anyMatch(name::startsWith) && Arrays.stream(entry.getValue()).noneMatch(element -> element.getMethodName().equals("getTask"))) {
				return true;
			}
		}
		return false;
	}

	private static void sleep() {
		try {
			Thread.sleep(10L);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}

	private static boolean isField(final Class<?> owner, final String member) {
		try {
			return Modifier.isStatic(owner.getField(member).getModifiers());
		} catch (final NoSuchFieldException exception) {
			return false;
		}
	}

	private static List<String> filter(final String output, final List<String> expected) {
		final List<String> problems = new ArrayList<>();
		final Set<String> printed = new HashSet<>();
		boolean trace = false;
		for (final String raw : output.split("\\r?\\n")) {
			final String line = raw.trim();
			if (line.isEmpty() || line.matches("#+") || line.startsWith("[!] Frame took ")) {
				continue;
			}
			if (trace && (line.startsWith("at ") || line.startsWith("Caused by") || line.startsWith("..."))) {
				continue;
			}

			trace = false;
			for (final String message : expected) {
				if (line.contains(message)) {
					printed.add(message);
					trace = true;
				}
			}
			if (!trace) {
				problems.add(line);
			}
		}

		for (final String message : expected) {
			if (!printed.contains(message)) {
				problems.add("the expected message never printed (remove its expect line from run-examples.txt): " + message);
			}
		}
		return problems;
	}

	private static Map<String, List<String>> readList(final File file, final String keyword) throws IOException {
		final Map<String, List<String>> entries = new LinkedHashMap<>();
		for (final String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
			final String[] parts = line.trim().split("\\s+", 3);
			if (parts[0].isEmpty() || parts[0].startsWith("#")) {
				continue;
			}
			if (parts.length < 3 || !parts[0].matches("skip|expect|call")) {
				throw new IllegalArgumentException("run-examples.txt: write \"skip <class> <reason>\", \"call <class> <static method or field>\" or \"expect <class> <message>\", not " + line.trim());
			}
			if (parts[0].equals(keyword)) {
				entries.computeIfAbsent(parts[1], name -> new ArrayList<>()).add(parts[2].trim());
			}
		}
		return entries;
	}

	private static final class TeeOutputStream extends OutputStream {

		private final OutputStream first;
		private final OutputStream second;

		private TeeOutputStream(final OutputStream first, final OutputStream second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public void write(final int value) throws IOException {
			this.first.write(value);
			this.second.write(value);
		}

		@Override
		public void write(final byte[] buffer, final int offset, final int length) throws IOException {
			this.first.write(buffer, offset, length);
			this.second.write(buffer, offset, length);
		}

		@Override
		public void flush() throws IOException {
			this.first.flush();
			this.second.flush();
		}

	}

}