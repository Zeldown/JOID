package dev.joid.lib.video;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.bytedeco.javacpp.Loader;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VideoNatives {

	public static final String VERSION = "6.0-1.5.9";

	private static final boolean             CLASSPATH = VideoNatives.class.getResource("/org/bytedeco/ffmpeg/" + Loader.getPlatform() + "/" + System.mapLibraryName("jniavutil")) != null;
	private static final Map<String, String> CHECKSUMS = new HashMap<>();

	static {
		VideoNatives.CHECKSUMS.put("linux-x86_64", "3b26dc266a24da542c012a08e445b8a212703d152d0f0e4663dd7d253bebe819");
		VideoNatives.CHECKSUMS.put("macosx-arm64", "d627ceb140fa627f131f7ae341c2e0143ba4501f57245578b8bdd3f5f6a6e5d0");
		VideoNatives.CHECKSUMS.put("macosx-x86_64", "910f35440b75f341787be21be5e5f72a70a8d43b27022f10a31d833652117534");
		VideoNatives.CHECKSUMS.put("windows-x86_64", "c2dba4e82e5ad865412f72b8f02ff5462088666100f9734b46a4023bac3e2a5c");
	}

	@Getter private static String           repository = "https://repo1.maven.org/maven2";
	@Getter private static volatile boolean installed  = VideoNatives.CLASSPATH;

	private static CompletableFuture<Void> installation;

	public static void repository(final @NonNull String repository) {
		VideoNatives.repository = repository;
	}

	public static synchronized @NonNull CompletableFuture<Void> install() {
		if (VideoNatives.installation != null && !VideoNatives.installation.isCompletedExceptionally()) {
			return VideoNatives.installation;
		}

		final CompletableFuture<Void> installation = new CompletableFuture<>();
		VideoNatives.installation = installation;
		if (VideoNatives.installed) {
			installation.complete(null);
			return installation;
		}

		ThreadUtils.daemonThread(() -> {
			try {
				VideoNatives.register(VideoNatives.download(Loader.getPlatform(), JOIDEnv.getCacheDir()));
				installation.complete(null);
			} catch (final RuntimeException exception) {
				JOIDLog.warn(exception.getMessage() + (exception.getCause() != null ? " (" + exception.getCause() + ")" : ""));
				installation.completeExceptionally(exception);
			}
		}, "VideoNatives").start();
		return installation;
	}

	private static File download(final String platform, final File cacheDir) {
		final String checksum = VideoNatives.CHECKSUMS.get(platform);
		if (checksum == null) {
			throw new VideoNativesException("FFmpeg has no natives for the platform " + platform + ", videos play on windows-x86_64, linux-x86_64, macosx-x86_64 and macosx-arm64");
		}

		final File directory = new File(new File(cacheDir, "ffmpeg-" + VideoNatives.VERSION), platform);
		if (new File(directory, ".complete").isFile()) {
			return directory;
		}

		final File parent = directory.getParentFile();
		final String url = VideoNatives.repository + "/org/bytedeco/ffmpeg/" + VideoNatives.VERSION + "/ffmpeg-" + VideoNatives.VERSION + "-" + platform + ".jar";
		File jar = null;
		File extraction = null;
		try {
			FileUtils.deleteDirectory(directory);
			Files.createDirectories(parent.toPath());
			jar = File.createTempFile(platform + "-", ".jar.part", parent);
			if (JOIDEnv.isDevMode()) {
				JOIDLog.info("Downloading the FFmpeg natives of " + platform + " from " + url);
			}

			VideoNatives.fetch(url, jar, platform, checksum);
			extraction = Files.createTempDirectory(parent.toPath(), platform + ".tmp").toFile();
			VideoNatives.extract(jar, extraction, platform);
			try {
				Files.move(extraction.toPath(), directory.toPath(), StandardCopyOption.ATOMIC_MOVE);
			} catch (final IOException exception) {
				if (!new File(directory, ".complete").isFile()) {
					throw exception;
				}
			}
			return directory;
		} catch (final IOException exception) {
			throw new VideoNativesException("The FFmpeg natives of " + platform + " cannot be written into " + parent.getAbsolutePath() + ", check that the cache folder can be written", exception);
		} finally {
			FileUtils.deleteQuietly(jar);
			FileUtils.deleteQuietly(extraction);
		}
	}

	private static void fetch(final String url, final File jar, final String platform, final String checksum) {
		final Hasher hasher = Hashing.sha256().newHasher();
		try {
			final URLConnection connection = new URL(url).openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(30000);
			try (final InputStream input = connection.getInputStream(); final OutputStream output = new FileOutputStream(jar)) {
				final byte[] buffer = new byte[65536];
				int read;
				while ((read = input.read(buffer)) != -1) {
					hasher.putBytes(buffer, 0, read);
					output.write(buffer, 0, read);
				}
			}
		} catch (final IOException exception) {
			throw new VideoNativesException("The FFmpeg natives of " + platform + " cannot be downloaded from " + url + ", check the connection and the repository, or put org.bytedeco:ffmpeg:" + VideoNatives.VERSION + ":" + platform + " on the classpath", exception);
		}

		if (!hasher.hash().toString().equals(checksum)) {
			throw new VideoNativesException("The FFmpeg natives downloaded from " + url + " do not match their SHA-256, check the repository");
		}
	}

	private static void extract(final File jar, final File extraction, final String platform) throws IOException {
		final String prefix = "org/bytedeco/ffmpeg/" + platform + "/";
		try (final ZipFile zip = new ZipFile(jar)) {
			final Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				if (entry.isDirectory() || !entry.getName().startsWith(prefix)) {
					continue;
				}

				final String name = entry.getName().substring(prefix.length());
				if (name.contains("/") || name.matches("(ffmpeg|ffprobe)(\\.exe)?")) {
					continue;
				}

				try (final InputStream input = zip.getInputStream(entry)) {
					Files.copy(input, new File(extraction, name).toPath());
				}
			}
		}

		Files.createFile(new File(extraction, ".complete").toPath());
	}

	private static void register(final File directory) {
		final Properties properties = Loader.loadProperties();
		final String separator = properties.getProperty("platform.path.separator", File.pathSeparator);
		for (final String key : new String[] {"platform.preloadpath", "platform.linkpath"}) {
			final String path = properties.getProperty(key);
			properties.setProperty(key, path == null || path.isEmpty() ? directory.getAbsolutePath() : path + separator + directory.getAbsolutePath());
		}

		VideoNatives.installed = true;
	}

}