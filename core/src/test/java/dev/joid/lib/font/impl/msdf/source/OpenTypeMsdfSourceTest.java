package dev.joid.lib.font.impl.msdf.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.hash.Hashing;

import dev.joid.internal.JOID;
import dev.joid.internal.JOIDEnv;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.cache.FileCacheArea;
import dev.joid.lib.cache.FileCacheWriter;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.tool.msdf.MsdfGenerator;
import dev.joid.tool.msdf.atlas.MsdfWriter;

public class OpenTypeMsdfSourceTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File previous;
	private File cache;
	private File directory;

	@Before
	public void useATemporaryCache() throws IOException {
		this.previous = JOIDEnv.getCacheDir();
		this.cache = Files.createTempDirectory("joid-cache-").toFile();
		this.directory = Files.createDirectory(new File(this.cache, "msdf").toPath()).toFile();
		JOIDEnv.cacheDir(this.cache);
	}

	@After
	public void restoreTheCache() throws IOException {
		JOIDEnv.cacheDir(this.previous);
		for (final File file : this.directory.listFiles()) {
			Files.delete(file.toPath());
		}
		Files.delete(this.directory.toPath());
		Files.delete(this.cache.toPath());
	}

	@Test
	public void recognisesOpenTypeHeaders() {
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {0, 1, 0, 0}));
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {'t', 'r', 'u', 'e'}));
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {'O', 'T', 'T', 'O', 0}));
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {'t', 't', 'c', 'f'}));
	}

	@Test
	public void refusesAnythingElse() {
		Assert.assertFalse(OpenTypeMsdfSource.supports("JOIDMSDF".getBytes(StandardCharsets.US_ASCII)));
		Assert.assertFalse(OpenTypeMsdfSource.supports(new byte[] {0, 1, 0}));
		Assert.assertFalse(OpenTypeMsdfSource.supports(new byte[0]));
	}

	@Test(timeout = 60000L)
	public void generatesAFontOnlyOnce() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final File file = OpenTypeMsdfSourceTest.resolve(font);
		Files.write(file.toPath(), "kept".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals(this.directory, file.getParentFile());
		Assert.assertEquals(file, OpenTypeMsdfSourceTest.resolve(font));
		Assert.assertArrayEquals("kept".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(file.toPath()));
		Assert.assertArrayEquals(new String[] {file.getName()}, this.entries());
	}

	@Test(timeout = 60000L)
	public void keysTheCacheByTheFontContent() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		Assert.assertNotEquals(OpenTypeMsdfSourceTest.resolve(font), OpenTypeMsdfSourceTest.resolve(Arrays.copyOf(font, font.length + 4)));
	}

	@Test(timeout = 60000L)
	public void loadsAnOpenTypeFontThroughTheCache() {
		final MsdfFontFace face = MsdfFontLoader.load(OpenTypeMsdfSourceTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).join().getFace(FontWeight.REGULAR, false);
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(face.getKerning('A', 'V') < 0F);
		Assert.assertEquals(1, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void restylesAnOpenTypeSource() {
		final MsdfFont font = MsdfFontLoader.load(OpenTypeMsdfSource.of(OpenTypeMsdfSourceTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).weight(FontWeight.BOLD)).join();
		Assert.assertSame(FontWeight.BOLD, font.getFace(FontWeight.BOLD, false).getWeight());
	}

	@Test(timeout = 60000L)
	public void logsHowEachFaceIsLoadedInDevMode() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final String[] lines = OpenTypeMsdfSourceTest.capture(() -> {
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			MsdfFontLoader.load(BinaryMsdfSource.of(OpenTypeMsdfSourceTest.find(font))).join();
		}).split(System.lineSeparator());
		Assert.assertTrue(lines[0], lines[0].startsWith("[JOID] Font JOID Test Regular generated into the msdf cache (" + OpenTypeMsdfSourceTest.find(font) + ") in "));
		Assert.assertTrue(lines[1], lines[1].startsWith("[JOID] Font JOID Test Regular read from the msdf cache (" + OpenTypeMsdfSourceTest.find(font) + ") in "));
		Assert.assertTrue(lines[2], lines[2].startsWith("[JOID] Font JOID Test Regular read from a .msdf file in "));
		Assert.assertTrue(lines[2], lines[2].endsWith("ms"));
	}

	@Test(timeout = 60000L)
	public void logsNothingOutsideDevMode() {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final PrintStream previous = System.out;
		final boolean devMode = JOIDEnv.isDevMode();
		try {
			JOIDEnv.devMode(false);
			System.setOut(new PrintStream(output, true));
			MsdfFontLoader.load(OpenTypeMsdfSourceTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).join();
		} finally {
			System.setOut(previous);
			JOIDEnv.devMode(devMode);
		}
		Assert.assertEquals(0, output.size());
	}

	@Test(timeout = 60000L)
	public void followsTheCacheFolderOfJOID() throws IOException {
		JOIDEnv.cacheDir(this.folder.getRoot());
		Assert.assertEquals(new File(this.folder.getRoot(), "msdf"), OpenTypeMsdfSourceTest.resolve(OpenTypeMsdfSourceTest.font()).getParentFile());
	}

	@Test
	public void findsNothingBeforeTheFirstGeneration() throws IOException {
		Assert.assertNull(OpenTypeMsdfSourceTest.find(OpenTypeMsdfSourceTest.font()));
		Assert.assertEquals(0, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void keysTheCacheByTheVersionsOfJoidAndOfItsGenerator() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final String versions = JOID.VERSION + " " + MsdfWriter.VERSION + " " + MsdfGenerator.CHARSET + " " + MsdfGenerator.RANGE + " " + MsdfGenerator.WIDTH + "x" + MsdfGenerator.HEIGHT;
		Assert.assertEquals(Hashing.sha256().newHasher().putString(versions, StandardCharsets.UTF_8).putBytes(font).hash() + ".msdf", OpenTypeMsdfSourceTest.resolve(font).getName());
	}

	@Test(timeout = 60000L)
	public void readsACachedAtlasInsteadOfGeneratingIt() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final OpenTypeMsdfSource miss = OpenTypeMsdfSource.of(new ByteArrayInputStream(font));
		final OpenTypeMsdfSource hit = OpenTypeMsdfSource.of(new ByteArrayInputStream(font));
		miss.read();
		final byte[] atlas = Files.readAllBytes(miss.getFile().toPath());
		Assert.assertEquals("JOID Test Regular", hit.read().getName());
		Assert.assertTrue(miss.isGenerated());
		Assert.assertFalse(hit.isGenerated());
		Assert.assertEquals(miss.getFile(), hit.getFile());
		Assert.assertArrayEquals(atlas, Files.readAllBytes(hit.getFile().toPath()));
	}

	@Test(timeout = 60000L)
	public void generatesIntoAMissingDirectory() throws IOException {
		JOIDEnv.cacheDir(new File(this.folder.getRoot(), "missing"));
		final File file = OpenTypeMsdfSourceTest.resolve(OpenTypeMsdfSourceTest.font());
		Assert.assertEquals("JOID Test Regular", BinaryMsdfSource.of(file).read().getName());
		Assert.assertEquals(new File(this.folder.getRoot(), "missing/msdf"), file.getParentFile());
	}

	@Test(timeout = 60000L)
	public void regeneratesAnExistingAtlas() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		Files.write(OpenTypeMsdfSourceTest.resolve(font).toPath(), "stale".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals("JOID Test Regular", BinaryMsdfSource.of(OpenTypeMsdfSourceTest.regenerate(font)).read().getName());
		Assert.assertEquals(1, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void regeneratesAnAtlasOpenedElsewhere() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final File file = OpenTypeMsdfSourceTest.resolve(font);
		Files.write(file.toPath(), "corrupted".getBytes(StandardCharsets.UTF_8));
		try (final InputStream reader = new FileInputStream(file)) {
			Assert.assertEquals("JOID Test Regular", BinaryMsdfSource.of(OpenTypeMsdfSourceTest.regenerate(font)).read().getName());
			Assert.assertEquals('c', reader.read());
		}
		Assert.assertEquals("JOID Test Regular", BinaryMsdfSource.of(OpenTypeMsdfSourceTest.find(font)).read().getName());
	}

	@Test
	public void refusesADirectoryItCannotCreate() throws IOException {
		final File blocker = this.folder.newFile("blocker");
		JOIDEnv.cacheDir(blocker);
		try {
			OpenTypeMsdfSourceTest.resolve(OpenTypeMsdfSourceTest.font());
			Assert.fail("The cache must not be created");
		} catch (final IOException expected) {
			Assert.assertEquals("The cache area msdf cannot be created in " + new File(blocker, "msdf").getAbsolutePath() + ", check that the cache folder can be written", expected.getMessage());
		}
	}

	@Test(timeout = 120000L)
	public void generatesTheSameFontOnceFromSeveralThreads() throws Exception {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final CountDownLatch start = new CountDownLatch(1);
		final ExecutorService executor = Executors.newFixedThreadPool(8);
		final List<CompletableFuture<File>> futures = new ArrayList<>();
		try {
			for (int i = 0; i < 8; i++) {
				futures.add(CompletableFuture.supplyAsync(() -> {
					try {
						start.await();
						return OpenTypeMsdfSourceTest.resolve(font);
					} catch (final Exception exception) {
						throw new CompletionException(exception);
					}
				}, executor));
			}

			start.countDown();
			CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
		} finally {
			executor.shutdownNow();
		}

		for (final CompletableFuture<File> future : futures) {
			Assert.assertEquals(futures.get(0).join(), future.join());
		}
		Assert.assertEquals(1, this.entries().length);
	}

	@Test
	public void reportsAFontItCannotGenerate() {
		try {
			OpenTypeMsdfSourceTest.resolve("not a font".getBytes(StandardCharsets.UTF_8));
			Assert.fail("The font must not generate");
		} catch (final IOException expected) {
			Assert.assertEquals("The msdf atlas of the font cannot be generated in " + this.directory.getAbsolutePath() + ", check that the font is a valid TrueType or OpenType file and that the cache folder can be written", expected.getMessage());
			Assert.assertNotNull(expected.getCause());
		}
		Assert.assertEquals(0, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void loadsAnOpenTypeCffFont() {
		final MsdfFontFace face = MsdfFontLoader.load(OpenTypeMsdfSourceTest.class.getResourceAsStream("/dev/joid/lib/font/impl/msdf/JoidTest-BoldItalic.otf")).join().getFace(FontWeight.BOLD, true);
		Assert.assertEquals("JOID Test Bold Italic", face.getName());
		Assert.assertSame(FontWeight.BOLD, face.getWeight());
		Assert.assertTrue(face.isItalic());
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertTrue(face.getKerning('A', 'V') < 0F);
		Assert.assertEquals(1, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void loadsTheFirstFontOfACollection() {
		final MsdfFontFace face = MsdfFontLoader.load(OpenTypeMsdfSourceTest.class.getResourceAsStream("/dev/joid/lib/font/impl/msdf/JoidTest.ttc")).join().getFace(FontWeight.REGULAR, false);
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(face.hasGlyph('V'));
		Assert.assertEquals(1, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void loadsAnAppleTrueTypeFont() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		System.arraycopy("true".getBytes(StandardCharsets.US_ASCII), 0, font, 0, 4);
		final MsdfFontFace face = MsdfFontLoader.load(new ByteArrayInputStream(font)).join().getFace(FontWeight.REGULAR, false);
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertNotNull(OpenTypeMsdfSourceTest.find(font));
	}

	@Test
	public void refusesAnOpenTypeFontItCannotRead() {
		final byte[] font = "OTTO and nothing else".getBytes(StandardCharsets.US_ASCII);
		try {
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			Assert.fail("The font must not load");
		} catch (final CompletionException expected) {
			Assert.assertTrue(String.valueOf(expected.getCause()), expected.getCause() instanceof IOException);
			Assert.assertEquals("The msdf atlas of the font cannot be generated in " + this.directory.getAbsolutePath() + ", check that the font is a valid TrueType or OpenType file and that the cache folder can be written", expected.getCause().getMessage());
		}
		Assert.assertEquals(0, this.entries().length);
	}

	@Test(timeout = 60000L)
	public void regeneratesACorruptedAtlas() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		Files.write(OpenTypeMsdfSourceTest.resolve(font).toPath(), "corrupted".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals("JOID Test Regular", MsdfFontLoader.load(new ByteArrayInputStream(font)).join().getFace(FontWeight.REGULAR, false).getName());
	}

	@Test(timeout = 60000L)
	public void regeneratesAnAtlasOfAnOlderVersion() throws IOException {
		final byte[] font = OpenTypeMsdfSourceTest.font();
		final File file = OpenTypeMsdfSourceTest.resolve(font);
		final byte[] bytes = Files.readAllBytes(file.toPath());
		final byte[] body = Asset.of(new InflaterInputStream(new ByteArrayInputStream(bytes, 8, bytes.length - 8))).read();
		body[0] = (byte) (MsdfWriter.VERSION - 1);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(bytes, 0, 8);
		try (final DeflaterOutputStream deflater = new DeflaterOutputStream(output)) {
			deflater.write(body);
		}
		Files.write(file.toPath(), output.toByteArray());
		Assert.assertEquals("JOID Test Regular", MsdfFontLoader.load(new ByteArrayInputStream(font)).join().getFace(FontWeight.REGULAR, false).getName());
	}

	public static File find(final byte[] font) {
		return OpenTypeMsdfSourceTest.area().entry(OpenTypeMsdfSourceTest.key(font));
	}

	public static File resolve(final byte[] font) throws IOException {
		return OpenTypeMsdfSourceTest.area().compute(OpenTypeMsdfSourceTest.key(font), (FileCacheWriter) OpenTypeMsdfSourceTest.invoke("generator", font));
	}

	public static File regenerate(final byte[] font) throws IOException {
		OpenTypeMsdfSourceTest.area().remove(OpenTypeMsdfSourceTest.key(font));
		return OpenTypeMsdfSourceTest.resolve(font);
	}

	private static FileCacheArea area() {
		try {
			final Field field = OpenTypeMsdfSource.class.getDeclaredField("AREA");
			field.setAccessible(true);
			return (FileCacheArea) field.get(null);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static String key(final byte[] font) {
		return (String) OpenTypeMsdfSourceTest.invoke("key", font);
	}

	private static Object invoke(final String name, final byte[] font) {
		try {
			final Method method = OpenTypeMsdfSource.class.getDeclaredMethod(name, byte[].class);
			method.setAccessible(true);
			return method.invoke(null, font);
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static String capture(final Runnable runnable) {
		final PrintStream previous = System.out;
		final boolean devMode = JOIDEnv.isDevMode();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			JOIDEnv.devMode(true);
			System.setOut(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setOut(previous);
			JOIDEnv.devMode(devMode);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private String[] entries() {
		return this.directory.list((parent, name) -> !name.startsWith("."));
	}

	private static byte[] font() throws IOException {
		return Asset.of(OpenTypeMsdfSourceTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).read();
	}

}