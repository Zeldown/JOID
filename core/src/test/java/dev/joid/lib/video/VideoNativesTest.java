package dev.joid.lib.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bytedeco.javacpp.Loader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.ConsoleCapture;
import dev.joid.lib.JOIDEnv;

public class VideoNativesTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public final ConsoleCapture console = new ConsoleCapture();

	private File   cache;
	private File   previous;
	private String repository;

	@Before
	public void rememberTheRepository() throws IOException {
		this.repository = VideoNatives.getRepository();
		this.previous = JOIDEnv.getCacheDir();
		this.cache = this.folder.newFolder("cache");
		JOIDEnv.cacheDir(this.cache);
	}

	@After
	public void restoreTheRepository() throws ReflectiveOperationException {
		VideoNatives.repository(this.repository);
		JOIDEnv.cacheDir(this.previous);
		final Field installation = VideoNatives.class.getDeclaredField("installation");
		installation.setAccessible(true);
		installation.set(null, null);
	}

	@Test
	public void downloadsFromMavenCentralByDefault() {
		Assert.assertEquals("https://repo1.maven.org/maven2", this.repository);
	}

	@Test
	public void isInstalledByTheNativesOfTheClasspath() {
		Assert.assertTrue(VideoNatives.isInstalled());
		final CompletableFuture<Void> installation = VideoNatives.install();
		Assert.assertTrue(installation.isDone());
		Assert.assertFalse(installation.isCompletedExceptionally());
		Assert.assertSame(installation, VideoNatives.install());
	}

	@Test(timeout = 120000L)
	public void installsTheNativesOfThePlatformIntoTheCache() throws IOException, URISyntaxException {
		final String platform = Loader.getPlatform();
		final File directory = new File(new File(this.cache, "ffmpeg"), "6.0-1.5.9-" + platform);
		final File jar = this.publish(platform, Files.readAllBytes(VideoNativesTest.nativesJar(platform).toPath()));

		Assert.assertEquals(directory, VideoNativesTest.download(platform));
		Assert.assertTrue(new File(directory, System.mapLibraryName("jniavutil")).isFile());
		Assert.assertFalse(new File(directory, platform + ".jar").exists());
		for (final String name : directory.list()) {
			Assert.assertFalse(name, name.startsWith("ffmpeg") || name.startsWith("ffprobe"));
		}
		Assert.assertArrayEquals(new String[] {directory.getName()}, this.entries());

		Files.delete(jar.toPath());
		Assert.assertEquals(directory, VideoNativesTest.download(platform));
	}

	@Test(timeout = 30000L)
	public void endsItsInstallationOnAnErrorOfItsThread() throws ReflectiveOperationException, InterruptedException {
		final Field installed = VideoNatives.class.getDeclaredField("installed");
		final Field installation = VideoNatives.class.getDeclaredField("installation");
		installed.setAccessible(true);
		installation.setAccessible(true);
		final Object wasInstalled = installed.get(null);
		final String handlers = System.getProperty("java.protocol.handler.pkgs");
		System.setProperty("java.protocol.handler.pkgs", "dev.joid.lib.video");
		installed.set(null, false);
		installation.set(null, null);
		VideoNatives.repository("failing://natives");
		try {
			VideoNatives.install().get();
			Assert.fail("The natives must not install");
		} catch (final ExecutionException expected) {
			Assert.assertTrue(String.valueOf(expected.getCause()), expected.getCause() instanceof LinkageError);
		} finally {
			installed.set(null, wasInstalled);
			installation.set(null, null);
			if (handlers == null) {
				System.clearProperty("java.protocol.handler.pkgs");
			} else {
				System.setProperty("java.protocol.handler.pkgs", handlers);
			}
		}
	}

	@Test
	public void keepsAnInstalledFolderWithoutDownloading() throws IOException {
		final File directory = new File(new File(this.cache, "ffmpeg"), "6.0-1.5.9-linux-x86_64");
		Files.createDirectories(directory.toPath());
		VideoNatives.repository("https://joid.invalid");
		Assert.assertEquals(directory, VideoNativesTest.download("linux-x86_64"));
	}

	@Test
	public void refusesAJarThatDoesNotMatchItsChecksum() throws IOException {
		this.publish("linux-x86_64", "not the natives".getBytes(StandardCharsets.UTF_8));
		try {
			VideoNativesTest.download("linux-x86_64");
			Assert.fail("The natives must not install");
		} catch (final VideoNativesException expected) {
			Assert.assertEquals("The FFmpeg natives downloaded from " + VideoNatives.getRepository() + "/org/bytedeco/ffmpeg/6.0-1.5.9/ffmpeg-6.0-1.5.9-linux-x86_64.jar do not match their SHA-256, check the repository", expected.getMessage());
		}
		Assert.assertArrayEquals(new String[0], this.entries());
	}

	@Test
	public void reportsARepositoryWithoutTheNatives() throws IOException {
		VideoNatives.repository(this.folder.newFolder("empty").toURI().toString().replaceAll("/$", ""));
		try {
			VideoNativesTest.download("linux-x86_64");
			Assert.fail("The natives must not install");
		} catch (final VideoNativesException expected) {
			Assert.assertEquals("The FFmpeg natives of linux-x86_64 cannot be downloaded from " + VideoNatives.getRepository() + "/org/bytedeco/ffmpeg/6.0-1.5.9/ffmpeg-6.0-1.5.9-linux-x86_64.jar, check the connection and the repository, or put org.bytedeco:ffmpeg:6.0-1.5.9:linux-x86_64 on the classpath", expected.getMessage());
			Assert.assertTrue(String.valueOf(expected.getCause()), expected.getCause() instanceof FileNotFoundException);
		}
		Assert.assertArrayEquals(new String[0], this.entries());
	}

	@Test
	public void refusesAPlatformWithoutNatives() {
		try {
			VideoNativesTest.download("linux-arm64");
			Assert.fail("The natives must not install");
		} catch (final VideoNativesException expected) {
			Assert.assertEquals("FFmpeg has no natives for the platform linux-arm64, play the videos on windows-x86_64, linux-x86_64, macosx-x86_64 or macosx-arm64", expected.getMessage());
		}
		Assert.assertArrayEquals(new String[0], this.cache.list());
	}

	private String[] entries() {
		return new File(this.cache, "ffmpeg").list((parent, name) -> !name.startsWith("."));
	}

	private File publish(final String platform, final byte[] bytes) throws IOException {
		final File root = this.folder.newFolder("repository");
		final File jar = new File(root, "org/bytedeco/ffmpeg/6.0-1.5.9/ffmpeg-6.0-1.5.9-" + platform + ".jar");
		Files.createDirectories(jar.getParentFile().toPath());
		Files.write(jar.toPath(), bytes);
		VideoNatives.repository(root.toURI().toString().replaceAll("/$", ""));
		return jar;
	}

	private static File download(final String platform) {
		try {
			final Method download = VideoNatives.class.getDeclaredMethod("download", String.class);
			download.setAccessible(true);
			return (File) download.invoke(null, platform);
		} catch (final InvocationTargetException exception) {
			throw (RuntimeException) exception.getCause();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static File nativesJar(final String platform) throws IOException, URISyntaxException {
		final JarURLConnection connection = (JarURLConnection) VideoNativesTest.class.getResource("/org/bytedeco/ffmpeg/" + platform + "/" + System.mapLibraryName("jniavutil")).openConnection();
		return new File(connection.getJarFileURL().toURI());
	}

}