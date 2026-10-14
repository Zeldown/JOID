package dev.joid.internal;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
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

	private static final Set<FileAlterationMonitor>       RUNNING_SET      = new HashSet<>();
	private static final Map<File, Map<UI, Runnable>>     WATCHER_MAP      = new HashMap<>();
	private static final Map<File, FileAlterationMonitor> MONITOR_MAP      = new HashMap<>();
	private static final ExecutorService                  MONITOR_EXECUTOR = Executors.newSingleThreadExecutor(ThreadUtils.daemonFactory("UI/monitor"));

	public static void watch(final UI ui, final Runnable reload) {
		final File location = HotReload.locate(ui.getClass());
		if (location == null) {
			JOIDLog.warnOnce("UI.hotReload", "Hot reload is off for " + ui.getClass().getSimpleName() + ": its classes are not loaded from a folder or a jar file, run it from one of them to reload it");
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
		JOIDLog.info("Hot reload is on for " + ui.getClass().getSimpleName() + ", it reloads when its classes change");
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
		} catch (final Exception e) {
			JOIDLog.warn("The hot reload of " + location.getAbsolutePath() + " cannot start (" + e + "), the UI no longer reloads: check that the folder can be watched", e);
		}
	}

	private static void closeMonitor(final FileAlterationMonitor monitor) {
		if (!HotReload.RUNNING_SET.remove(monitor)) {
			return;
		}

		try {
			monitor.stop();
		} catch (final Exception e) {
			JOIDLog.warn("A hot reload watcher cannot stop (" + e + "), it stops with the JVM", e);
		}
	}

	private static FileAlterationMonitor createMonitor(final File location) {
		final FileAlterationObserver observer;
		try {
			observer = FileAlterationObserver.builder().setFile(location.isDirectory() ? location : location.getParentFile()).get();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
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
				} catch (final Exception e) {}

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
		if (location == null || !"file".equals(location.getProtocol())) {
			return null;
		}

		try {
			return new File(location.toURI());
		} catch (final URISyntaxException | IllegalArgumentException exception) {
			return null;
		}
	}

}