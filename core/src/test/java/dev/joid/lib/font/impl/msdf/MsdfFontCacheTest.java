package dev.joid.lib.font.impl.msdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.source.MsdfBinarySource;
import dev.joid.lib.font.impl.msdf.source.MsdfOpenTypeSource;
import dev.joid.tool.msdf.MsdfGenerator;
import dev.joid.tool.msdf.atlas.MsdfWriter;

public class MsdfFontCacheTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	private File previous;
	private File directory;

	@Before
	public void useATemporaryCache() throws IOException {
		this.previous = MsdfFontCache.getDirectory();
		this.directory = Files.createTempDirectory("joid-msdf-").toFile();
		MsdfFontCache.directory(this.directory);
	}

	@After
	public void restoreTheCache() throws IOException {
		MsdfFontCache.directory(this.previous);
		for (final File file : this.directory.listFiles()) {
			Files.delete(file.toPath());
		}
		Files.delete(this.directory.toPath());
	}

	@Test(timeout = 60000L)
	public void generatesAFontOnlyOnce() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final File file = MsdfFontCache.resolve(font);
		final long modified = file.lastModified();
		Assert.assertEquals(this.directory, file.getParentFile());
		Assert.assertEquals(file, MsdfFontCache.resolve(font));
		Assert.assertEquals(modified, file.lastModified());
		Assert.assertArrayEquals(new String[] {file.getName()}, this.directory.list());
	}

	@Test(timeout = 60000L)
	public void keysTheCacheByTheFontContent() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		Assert.assertNotEquals(MsdfFontCache.resolve(font), MsdfFontCache.resolve(Arrays.copyOf(font, font.length + 4)));
	}

	@Test(timeout = 60000L)
	public void loadsAnOpenTypeFontThroughTheCache() {
		final MsdfFontFace face = MsdfFontLoader.load(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).join().getFace(FontWeight.REGULAR, false);
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(face.getKerning('A', 'V') < 0F);
		Assert.assertEquals(1, this.directory.list().length);
	}

	@Test(timeout = 60000L)
	public void restylesAnOpenTypeSource() {
		final MsdfFont font = MsdfFontLoader.load(MsdfOpenTypeSource.of(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).weight(FontWeight.BOLD)).join();
		Assert.assertSame(FontWeight.BOLD, font.getFace(FontWeight.BOLD, false).getWeight());
	}

	@Test(timeout = 60000L)
	public void logsHowEachFaceIsLoadedInDevMode() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final String[] lines = MsdfFontCacheTest.capture(() -> {
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			MsdfFontLoader.load(MsdfBinarySource.of(MsdfFontCache.locate(font))).join();
		}).split(System.lineSeparator());
		Assert.assertTrue(lines[0], lines[0].startsWith("[JOID] Font JOID Test Regular generated into the msdf cache (" + MsdfFontCache.locate(font) + ") in "));
		Assert.assertTrue(lines[1], lines[1].startsWith("[JOID] Font JOID Test Regular read from the msdf cache (" + MsdfFontCache.locate(font) + ") in "));
		Assert.assertTrue(lines[2], lines[2].startsWith("[JOID] Font JOID Test Regular read from a .msdf file in "));
		Assert.assertTrue(lines[2], lines[2].endsWith("ms"));
	}

	@Test(timeout = 60000L)
	public void logsNothingOutsideDevMode() {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final PrintStream previous = System.out;
		try {
			System.setOut(new PrintStream(output, true));
			MsdfFontLoader.load(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).join();
		} finally {
			System.setOut(previous);
		}
		Assert.assertEquals(0, output.size());
	}

	@Test
	public void movesItsDirectory() {
		Assert.assertEquals(this.directory, MsdfFontCache.getDirectory());
		MsdfFontCache.directory(this.folder.getRoot());
		Assert.assertEquals(this.folder.getRoot(), MsdfFontCache.getDirectory());
		Assert.assertEquals(this.folder.getRoot(), MsdfFontCache.locate(new byte[] {1, 2, 3}).getParentFile());
	}

	@Test
	public void defaultsToADirectoryOfTheSystemCache() {
		Assert.assertTrue(this.previous.getPath(), this.previous.getPath().endsWith(new File("joid", "msdf").getPath()));
	}

	@Test
	public void locatesAFontWithoutWritingAnything() throws IOException {
		final File file = MsdfFontCache.locate(MsdfFontCacheTest.font());
		Assert.assertEquals(this.directory, file.getParentFile());
		Assert.assertTrue(file.getName(), file.getName().matches("[0-9a-f]{64}\\.msdf"));
		Assert.assertFalse(file.exists());
		Assert.assertEquals(0, this.directory.list().length);
	}

	@Test
	public void keysTheCacheByTheVersionsOfJoidAndOfItsGenerator() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final String versions = JOID.VERSION + " " + MsdfWriter.VERSION + " " + MsdfGenerator.CHARSET + " " + MsdfGenerator.RANGE + " " + MsdfGenerator.WIDTH + "x" + MsdfGenerator.HEIGHT;
		Assert.assertEquals(Hashing.sha256().newHasher().putString(versions, StandardCharsets.UTF_8).putBytes(font).hash() + ".msdf", MsdfFontCache.locate(font).getName());
	}

	@Test(timeout = 60000L)
	public void readsACachedAtlasInsteadOfGeneratingIt() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final MsdfOpenTypeSource miss = MsdfOpenTypeSource.of(new ByteArrayInputStream(font));
		final MsdfOpenTypeSource hit = MsdfOpenTypeSource.of(new ByteArrayInputStream(font));
		miss.read();
		final long modified = MsdfFontCache.locate(font).lastModified();
		Assert.assertEquals("JOID Test Regular", hit.read().getName());
		Assert.assertTrue(miss.isGenerated());
		Assert.assertFalse(hit.isGenerated());
		Assert.assertEquals(MsdfFontCache.locate(font), hit.getFile());
		Assert.assertEquals(modified, hit.getFile().lastModified());
	}

	@Test(timeout = 60000L)
	public void generatesIntoAMissingDirectory() throws IOException {
		final File file = new File(new File(this.folder.getRoot(), "fonts/regular"), "font.msdf");
		MsdfFontCache.generate(MsdfFontCacheTest.font(), file);
		Assert.assertEquals("JOID Test Regular", MsdfBinarySource.of(file).read().getName());
		Assert.assertArrayEquals(new String[] {"font.msdf"}, file.getParentFile().list());
	}

	@Test(timeout = 60000L)
	public void replacesAnExistingAtlas() throws IOException {
		final File file = this.folder.newFile("font.msdf");
		Files.write(file.toPath(), "stale".getBytes(StandardCharsets.UTF_8));
		MsdfFontCache.generate(MsdfFontCacheTest.font(), file);
		Assert.assertEquals("JOID Test Regular", MsdfBinarySource.of(file).read().getName());
	}

	@Test(timeout = 60000L)
	public void keepsTheAtlasAnotherProcessIsReading() throws IOException {
		final File file = this.folder.newFile("font.msdf");
		MsdfFontCache.generate(MsdfFontCacheTest.font(), file);
		try (final FileInputStream reader = new FileInputStream(file)) {
			MsdfFontCache.generate(MsdfFontCacheTest.font(), file);
			Assert.assertTrue(reader.available() > 0);
		}

		Assert.assertEquals("JOID Test Regular", MsdfBinarySource.of(file).read().getName());
	}

	@Test
	public void refusesADirectoryItCannotCreate() throws IOException {
		final File blocker = this.folder.newFile("blocker");
		final File cache = new File(blocker, "msdf");
		try {
			MsdfFontCache.generate(MsdfFontCacheTest.font(), new File(cache, "font.msdf"));
			Assert.fail("The cache must not be created");
		} catch (final IOException expected) {
			Assert.assertEquals("Unable to create the msdf cache " + cache.getAbsolutePath(), expected.getMessage());
		}
	}

	@Test(timeout = 120000L)
	public void generatesSeveralFontsAtOnceIntoAMissingDirectory() throws Exception {
		final byte[] font = MsdfFontCacheTest.font();
		final File cache = new File(this.folder.getRoot(), "shared/msdf");
		final CountDownLatch start = new CountDownLatch(1);
		final ExecutorService executor = Executors.newFixedThreadPool(8);
		try {
			final List<CompletableFuture<Void>> futures = new ArrayList<>();
			for (int i = 0; i < 8; i++) {
				final File file = new File(cache, "font-" + i + ".msdf");
				futures.add(CompletableFuture.runAsync(() -> {
					try {
						start.await();
						MsdfFontCache.generate(font, file);
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
		Assert.assertEquals(8, cache.list().length);
	}

	@Test
	public void reportsAFontItCannotGenerate() {
		try {
			MsdfFontCache.generate("not a font".getBytes(StandardCharsets.UTF_8), new File(this.directory, "font.msdf"));
			Assert.fail("The font must not generate");
		} catch (final IOException expected) {
			Assert.assertEquals("Unable to generate the msdf atlas of the font in " + this.directory.getAbsolutePath(), expected.getMessage());
			Assert.assertNotNull(expected.getCause());
		}
		Assert.assertEquals(0, this.directory.list().length);
	}

	@Test(timeout = 60000L)
	public void loadsAnOpenTypeCffFont() {
		final MsdfFontFace face = MsdfFontLoader.load(MsdfFontCacheTest.class.getResourceAsStream("/dev/joid/lib/font/impl/msdf/JoidTest-BoldItalic.otf")).join().getFace(FontWeight.BOLD, true);
		Assert.assertEquals("JOID Test Bold Italic", face.getName());
		Assert.assertSame(FontWeight.BOLD, face.getWeight());
		Assert.assertTrue(face.isItalic());
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertTrue(face.getKerning('A', 'V') < 0F);
		Assert.assertEquals(1, this.directory.list().length);
	}

	@Test(timeout = 60000L)
	public void loadsTheFirstFontOfACollection() {
		final MsdfFontFace face = MsdfFontLoader.load(MsdfFontCacheTest.class.getResourceAsStream("/dev/joid/lib/font/impl/msdf/JoidTest.ttc")).join().getFace(FontWeight.REGULAR, false);
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(face.hasGlyph('V'));
		Assert.assertEquals(1, this.directory.list().length);
	}

	@Test(timeout = 60000L)
	public void loadsAnAppleTrueTypeFont() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		System.arraycopy("true".getBytes(StandardCharsets.US_ASCII), 0, font, 0, 4);
		final MsdfFontFace face = MsdfFontLoader.load(new ByteArrayInputStream(font)).join().getFace(FontWeight.REGULAR, false);
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(MsdfFontCache.locate(font).isFile());
	}

	@Test
	public void refusesAnOpenTypeFontItCannotRead() {
		final byte[] font = "OTTO and nothing else".getBytes(StandardCharsets.US_ASCII);
		try {
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			Assert.fail("The font must not load");
		} catch (final CompletionException expected) {
			Assert.assertTrue(String.valueOf(expected.getCause()), expected.getCause() instanceof IOException);
			Assert.assertEquals("Unable to generate the msdf atlas of the font in " + this.directory.getAbsolutePath(), expected.getCause().getMessage());
		}
		Assert.assertEquals(0, this.directory.list().length);
	}

	@Test(timeout = 60000L)
	public void regeneratesACorruptedAtlas() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		Files.write(MsdfFontCache.locate(font).toPath(), "corrupted".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals("JOID Test Regular", MsdfFontLoader.load(new ByteArrayInputStream(font)).join().getFace(FontWeight.REGULAR, false).getName());
	}

	@Test(timeout = 60000L)
	public void regeneratesAnAtlasOfAnOlderVersion() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final File file = MsdfFontCache.resolve(font);
		final byte[] bytes = Files.readAllBytes(file.toPath());
		final byte[] body = Asset.of(new InflaterInputStream(new ByteArrayInputStream(bytes, 8, bytes.length - 8))).read();
		body[0] = (byte) (MsdfWriter.VERSION - 1);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(bytes, 0, 8);
		try (DeflaterOutputStream deflater = new DeflaterOutputStream(output)) {
			deflater.write(body);
		}
		Files.write(file.toPath(), output.toByteArray());
		Assert.assertEquals("JOID Test Regular", MsdfFontLoader.load(new ByteArrayInputStream(font)).join().getFace(FontWeight.REGULAR, false).getName());
	}

	private static String capture(final Runnable runnable) {
		final PrintStream previous = System.out;
		final boolean devMode = JOID.inst().isDevMode();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			JOID.inst().setDevMode(true);
			System.setOut(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setOut(previous);
			JOID.inst().setDevMode(devMode);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private static byte[] font() throws IOException {
		return Asset.of(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).read();
	}

}