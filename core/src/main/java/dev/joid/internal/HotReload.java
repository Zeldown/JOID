package dev.joid.internal;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HotReload {

	private static final ExecutorService                  MONITOR_EXECUTOR = Executors.newSingleThreadExecutor(ThreadUtils.daemonFactory("UI/monitor"));
	private static final Set<FileAlterationMonitor>       RUNNING_SET      = new HashSet<>();
	private static final Map<File, Map<UI, Runnable>>     WATCHER_MAP      = new HashMap<>();
	private static final Map<File, FileAlterationMonitor> MONITOR_MAP      = new HashMap<>();

	public static void watch(final UI ui, final Runnable reload) {
		final File location = HotReload.locate(ui.getClass());
		if (location == null) {
			JOIDLog.warnOnce("UI.hotReload", "Hot reload is off for " + JOIDLog.nameOf(ui.getClass()) + ": its classes are not loaded from a folder or a jar file, run it from one of them to reload it");
			return;
		}

		synchronized (HotReload.WATCHER_MAP) {
			HotReload.WATCHER_MAP.computeIfAbsent(location, key -> new HashMap<>()).put(ui, reload);
			if (!HotReload.MONITOR_MAP.containsKey(location)) {
				final FileAlterationMonitor monitor = HotReload.createMonitor(location);
				HotReload.MONITOR_MAP.put(location, monitor);
				HotReload.MONITOR_EXECUTOR.execute(() -> HotReload.openMonitor(location, monitor));
			}
		}
		JOIDLog.info("Hot reload is on for " + JOIDLog.nameOf(ui.getClass()) + ", it reloads when its classes change");
	}

	public static void unwatch(final UI ui) {
		final File location = HotReload.locate(ui.getClass());
		synchronized (HotReload.WATCHER_MAP) {
			final Map<UI, Runnable> watchers = HotReload.WATCHER_MAP.get(location);
			if (watchers == null || watchers.remove(ui) == null || !watchers.isEmpty()) {
				return;
			}

			HotReload.WATCHER_MAP.remove(location);
			final FileAlterationMonitor monitor = HotReload.MONITOR_MAP.remove(location);
			HotReload.MONITOR_EXECUTOR.execute(() -> HotReload.closeMonitor(monitor));
		}
	}

	private static void openMonitor(final File location, final FileAlterationMonitor monitor) {
		synchronized (HotReload.WATCHER_MAP) {
			if (HotReload.MONITOR_MAP.get(location) != monitor) {
				return;
			}
		}

		try {
			monitor.start();
			HotReload.RUNNING_SET.add(monitor);
		} catch (final Exception exception) {
			JOIDLog.warn("The hot reload of " + location.getAbsolutePath() + " cannot start (" + exception + "), the UI no longer reloads: check that the folder can be watched", exception);
		}
	}

	private static void closeMonitor(final FileAlterationMonitor monitor) {
		if (!HotReload.RUNNING_SET.remove(monitor)) {
			return;
		}

		try {
			monitor.stop();
		} catch (final Exception exception) {
			JOIDLog.warn("A hot reload watcher cannot stop (" + exception + "), it stops with the JVM", exception);
		}
	}

	private static FileAlterationMonitor createMonitor(final File location) {
		final FileAlterationObserver observer;
		try {
			observer = FileAlterationObserver.builder().setFile(location.isDirectory() ? location : location.getParentFile()).get();
		} catch (final IOException exception) {
			throw new UncheckedIOException(exception);
		}

		observer.addListener(new FileAlterationListenerAdaptor() {

			private File change;

			@Override
			public void onFileChange(final File file) {
				this.track(file);
			}

			@Override
			public void onFileCreate(final File file) {
				this.track(file);
			}

			@Override
			public void onStop(final FileAlterationObserver observer) {
				if (this.change == null) {
					return;
				}

				try {
					Thread.sleep(1000L);
				} catch (final InterruptedException exception) {
					Thread.currentThread().interrupt();
					return;
				}

				JOIDLog.info(this.change.getName() + " changed, the UIs that use it reload");
				this.change = null;
				synchronized (HotReload.WATCHER_MAP) {
					final Map<UI, Runnable> watchers = HotReload.WATCHER_MAP.get(location);
					if (watchers != null) {
						watchers.values().forEach(Runnable::run);
					}
				}
			}

			private void track(final File file) {
				if (location.isDirectory() ? file.getName().endsWith(".class") : file.equals(location)) {
					this.change = file;
				}
			}

		});

		final FileAlterationMonitor monitor = new FileAlterationMonitor(500);
		monitor.setThreadFactory(ThreadUtils.daemonFactory("UI/" + location.getName() + "/monitor"));
		monitor.addObserver(observer);
		return monitor;
	}

	private static File locate(final Class<?> type) {
		final ProtectionDomain domain = type.getProtectionDomain();
		final CodeSource source = domain != null ? domain.getCodeSource() : null;
		final URL location = source != null ? source.getLocation() : null;
		final String entry = type.getName().replace('.', '/') + ".class";
		if (location == null) {
			return HotReload.locateInLoaders(type, entry);
		}

		if ("jar".equals(location.getProtocol()) || "file".equals(location.getProtocol())) {
			return HotReload.toLocation(type, location);
		}

		final File file = HotReload.toNamedFile(location);
		return file != null || !location.getPath().endsWith("/" + entry) ? file : HotReload.locateInLoaders(type, entry);
	}

	private static File locateInLoaders(final Class<?> type, final String entry) {
		for (ClassLoader loader = type.getClassLoader(); loader != null; loader = loader.getParent()) {
			final URL resource = loader.getResource(entry);
			if (resource != null && ("jar".equals(resource.getProtocol()) || "file".equals(resource.getProtocol()))) {
				return HotReload.toLocation(type, resource);
			}
		}
		return null;
	}

	private static File toLocation(final Class<?> type, final URL location) {
		if ("jar".equals(location.getProtocol())) {
			final String path = location.getPath();
			final int separator = path.indexOf("!/");
			try {
				return HotReload.toFile(new URL(separator < 0 ? path : path.substring(0, separator)));
			} catch (final MalformedURLException exception) {
				return null;
			}
		}

		File file = HotReload.toFile(location);
		if (file != null && file.getName().endsWith(".class")) {
			for (int segment = type.getName().split("\\.").length; segment > 0 && file != null; segment--) {
				file = file.getParentFile();
			}
		}
		return file;
	}

	private static File toNamedFile(final URL location) {
		try {
			final String path = URLDecoder.decode(location.getPath().replace("+", "%2B"), "UTF-8");
			final int separator = path.indexOf("!/");
			final String entry = separator < 0 ? path : path.substring(0, separator);
			final int suffix = entry.lastIndexOf('#');
			final File file = HotReload.toFile(new URL("file", "", suffix < 0 ? entry : entry.substring(0, suffix)));
			return file != null && file.exists() ? file : null;
		} catch (final UnsupportedEncodingException | MalformedURLException | IllegalArgumentException exception) {
			return null;
		}
	}

	private static File toFile(final URL location) {
		if (!"file".equals(location.getProtocol())) {
			return null;
		}

		try {
			return new File(location.toURI());
		} catch (final URISyntaxException | IllegalArgumentException exception) {
			return null;
		}
	}

}