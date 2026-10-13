package dev.joid.lib.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.bytedeco.javacpp.Loader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class VideoNativesTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private String repository;

	@Before
	public void rememberTheRepository() {
		this.repository = VideoNatives.getRepository();
	}

	@After
	public void restoreTheRepository() {
		VideoNatives.repository(this.repository);
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
		final File cache = this.folder.newFolder("cache");
		final File directory = new File(new File(cache, "ffmpeg-" + VideoNatives.VERSION), platform);
		Files.createDirectories(directory.toPath());
		Files.write(new File(directory, "stale").toPath(), new byte[] {1});
		final File jar = this.publish(platform, Files.readAllBytes(VideoNativesTest.nativesJar(platform).toPath()));

		Assert.assertEquals(directory, VideoNativesTest.download(platform, cache));
		Assert.assertTrue(new File(directory, ".complete").isFile());
		Assert.assertTrue(new File(directory, System.mapLibraryName("jniavutil")).isFile());
		Assert.assertFalse(new File(directory, "stale").exists());
		for (final String name : directory.list()) {
			Assert.assertFalse(name, name.startsWith("ffmpeg") || name.startsWith("ffprobe"));
		}
		Assert.assertArrayEquals(new String[] {platform}, directory.getParentFile().list());

		Files.delete(jar.toPath());
		Assert.assertEquals(directory, VideoNativesTest.download(platform, cache));
	}

	@Test
	public void keepsACompleteFolderWithoutDownloading() throws IOException {
		final File cache = this.folder.newFolder("cache");
		final File directory = new File(new File(cache, "ffmpeg-" + VideoNatives.VERSION), "linux-x86_64");
		Files.createDirectories(directory.toPath());
		Files.createFile(new File(directory, ".complete").toPath());
		VideoNatives.repository("https://joid.invalid");
		Assert.assertEquals(directory, VideoNativesTest.download("linux-x86_64", cache));
	}

	@Test
	public void refusesAJarThatDoesNotMatchItsChecksum() throws IOException {
		final File cache = this.folder.newFolder("cache");
		this.publish("linux-x86_64", "not the natives".getBytes(StandardCharsets.UTF_8));
		try {
			VideoNativesTest.download("linux-x86_64", cache);
			Assert.fail("The natives must not install");
		} catch (final VideoNativesException expected) {
			Assert.assertEquals("The FFmpeg natives downloaded from " + VideoNatives.getRepository() + "/org/bytedeco/ffmpeg/6.0-1.5.9/ffmpeg-6.0-1.5.9-linux-x86_64.jar do not match their SHA-256, check the repository", expected.getMessage());
		}
		Assert.assertArrayEquals(new String[0], new File(cache, "ffmpeg-" + VideoNatives.VERSION).list());
	}

	@Test
	public void reportsARepositoryWithoutTheNatives() throws IOException {
		final File cache = this.folder.newFolder("cache");
		VideoNatives.repository(this.folder.newFolder("empty").toURI().toString().replaceAll("/$", ""));
		try {
			VideoNativesTest.download("linux-x86_64", cache);
			Assert.fail("The natives must not install");
		} catch (final VideoNativesException expected) {
			Assert.assertEquals("The FFmpeg natives of linux-x86_64 cannot be downloaded from " + VideoNatives.getRepository() + "/org/bytedeco/ffmpeg/6.0-1.5.9/ffmpeg-6.0-1.5.9-linux-x86_64.jar, check the connection and the repository, or put org.bytedeco:ffmpeg:6.0-1.5.9:linux-x86_64 on the classpath", expected.getMessage());
			Assert.assertTrue(String.valueOf(expected.getCause()), expected.getCause() instanceof FileNotFoundException);
		}
		Assert.assertArrayEquals(new String[0], new File(cache, "ffmpeg-" + VideoNatives.VERSION).list());
	}

	@Test
	public void refusesAPlatformWithoutNatives() {
		try {
			VideoNativesTest.download("linux-arm64", this.folder.getRoot());
			Assert.fail("The natives must not install");
		} catch (final VideoNativesException expected) {
			Assert.assertEquals("FFmpeg has no natives for the platform linux-arm64, videos play on windows-x86_64, linux-x86_64, macosx-x86_64 and macosx-arm64", expected.getMessage());
		}
		Assert.assertArrayEquals(new String[0], this.folder.getRoot().list());
	}

	private static File download(final String platform, final File cacheDir) {
		try {
			final Method download = VideoNatives.class.getDeclaredMethod("download", String.class, File.class);
			download.setAccessible(true);
			return (File) download.invoke(null, platform, cacheDir);
		} catch (final InvocationTargetException exception) {
			throw (RuntimeException) exception.getCause();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private File publish(final String platform, final byte[] bytes) throws IOException {
		final File root = this.folder.newFolder("repository");
		final File jar = new File(root, "org/bytedeco/ffmpeg/" + VideoNatives.VERSION + "/ffmpeg-" + VideoNatives.VERSION + "-" + platform + ".jar");
		Files.createDirectories(jar.getParentFile().toPath());
		Files.write(jar.toPath(), bytes);
		VideoNatives.repository(root.toURI().toString().replaceAll("/$", ""));
		return jar;
	}

	private static File nativesJar(final String platform) throws IOException, URISyntaxException {
		final JarURLConnection connection = (JarURLConnection) VideoNativesTest.class.getResource("/org/bytedeco/ffmpeg/" + platform + "/" + System.mapLibraryName("jniavutil")).openConnection();
		return new File(connection.getJarFileURL().toURI());
	}

}