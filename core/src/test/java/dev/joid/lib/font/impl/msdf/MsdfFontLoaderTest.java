package dev.joid.lib.font.impl.msdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.dto.source.IMsdfSource;
import dev.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;
import dev.joid.lib.font.impl.msdf.dto.source.MsdfOpenTypeSource;
import dev.joid.msdf.atlas.MsdfWriter;
import lombok.NonNull;

public class MsdfFontLoaderTest {

	private static final String[] WEIGHTS = {"Thin", "ExtraLight", "Light", "Regular", "Medium", "SemiBold", "Bold", "ExtraBold", "Black"};

	@ClassRule
	public static final TemporaryFolder FOLDER = new TemporaryFolder();

	private static File atlas;

	@BeforeClass
	public static void generateAnAtlas() throws IOException {
		MsdfFontLoaderTest.atlas = MsdfFontLoaderTest.FOLDER.newFile("font.msdf");
		MsdfFontCache.generate(Asset.of(MsdfFontLoaderTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).read(), MsdfFontLoaderTest.atlas);
	}

	@Test(timeout = 30000L)
	public void restylesASource() {
		final MsdfFont font = MsdfFontLoader.load(MsdfFontLoaderTest.stream("Regular"), MsdfOpenTypeSource.of(MsdfFontLoaderTest.stream("Regular")).italic(true)).join();
		Assert.assertFalse(font.getFace(FontWeight.REGULAR, false).isItalic());
		Assert.assertTrue(font.getFace(FontWeight.REGULAR, true).isItalic());
	}

	@Test(timeout = 30000L)
	public void loadsFromAnAsset() {
		final Asset asset = Asset.of(MsdfFontLoaderTest.stream("Regular"));
		Assert.assertTrue(MsdfFontLoader.load(asset).join().getFace(FontWeight.REGULAR, false).getGlyphs().size() > 200);
	}

	@Test(timeout = 10000L)
	public void refusesAnEmptyFamily() {
		MsdfFontLoaderTest.fails(IllegalArgumentException.class);
	}

	@Test(timeout = 10000L)
	public void refusesAnUnknownHandle() {
		MsdfFontLoaderTest.fails(IllegalArgumentException.class, new Object());
	}

	@Test(timeout = 60000L)
	public void loadsEveryWeightOfAFamily() {
		final Object[] handles = new Object[MsdfFontLoaderTest.WEIGHTS.length];
		for (int i = 0; i < handles.length; i++) {
			handles[handles.length - 1 - i] = MsdfFontLoaderTest.stream(MsdfFontLoaderTest.WEIGHTS[i]);
		}

		final MsdfFont font = MsdfFontLoader.load(handles).join();
		Assert.assertEquals(FontWeight.values().length, font.getFamily().getFaces().size());
		for (final FontWeight weight : FontWeight.values()) {
			Assert.assertSame(weight, font.getFace(weight, false).getWeight());
		}
	}

	@Test(timeout = 10000L)
	public void handsTheFailureBackToTheCaller() {
		MsdfFontLoaderTest.fails(IOException.class, new ByteArrayInputStream("not a font at all".getBytes(StandardCharsets.UTF_8)));
	}

	@Test(timeout = 30000L)
	public void refusesTwoFacesOfTheSameWeight() {
		MsdfFontLoaderTest.fails(IllegalArgumentException.class, MsdfFontLoaderTest.stream("Regular"), MsdfFontLoaderTest.stream("Regular"));
	}

	@Test(timeout = 30000L)
	public void loadsFromAnyHandle() throws IOException {
		try (InputStream stream = MsdfFontLoaderTest.stream("Regular")) {
			final MsdfFont font = MsdfFontLoader.load(stream).join();
			Assert.assertEquals(1, font.getFamily().getFaces().size());
			Assert.assertSame(FontWeight.REGULAR, font.getFace(FontWeight.BOLD, false).getWeight());
		}
	}

	@Test(timeout = 30000L)
	public void acceptsAnyMsdfSource() throws IOException {
		final MsdfFontFace face = MsdfOpenTypeSource.of(MsdfFontLoaderTest.stream("Regular")).read();
		final IMsdfSource source = () -> face;
		Assert.assertSame(face, MsdfFontLoader.load(source).join().getFace(FontWeight.REGULAR, false));
	}

	@Test(timeout = 10000L)
	public void loadsAMsdfFileDirectly() {
		final MsdfFontFace face = MsdfFontLoader.load(MsdfFontLoaderTest.atlas).join().getFace(FontWeight.REGULAR, false);
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertTrue(face.getKerning('A', 'V') < 0F);
	}

	@Test(timeout = 10000L)
	public void loadsAMsdfStream() throws IOException {
		try (InputStream stream = new FileInputStream(MsdfFontLoaderTest.atlas)) {
			Assert.assertEquals("JOID Test Regular", MsdfFontLoader.load(stream).join().getFace(FontWeight.REGULAR, false).getName());
		}
	}

	@Test(timeout = 10000L)
	public void restylesAMsdfFile() {
		final MsdfFontFace face = MsdfFontLoader.load(MsdfBinarySource.of(MsdfFontLoaderTest.atlas).weight(FontWeight.LIGHT).italic(true)).join().getFace(FontWeight.LIGHT, true);
		Assert.assertSame(FontWeight.LIGHT, face.getWeight());
		Assert.assertTrue(face.isItalic());
	}

	@Test(timeout = 10000L)
	public void refusesAnOlderMsdfFile() throws IOException {
		final byte[] bytes = Files.readAllBytes(MsdfFontLoaderTest.atlas.toPath());
		final byte[] body = Asset.of(new InflaterInputStream(new ByteArrayInputStream(bytes, 8, bytes.length - 8))).read();
		body[0] = (byte) (MsdfWriter.VERSION - 1);
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(bytes, 0, 8);
		try (DeflaterOutputStream deflater = new DeflaterOutputStream(output)) {
			deflater.write(body);
		}
		final Throwable cause = MsdfFontLoaderTest.cause(new ByteArrayInputStream(output.toByteArray()));
		Assert.assertTrue(String.valueOf(cause), cause instanceof IOException);
		Assert.assertEquals("Unsupported msdf font version " + (MsdfWriter.VERSION - 1) + ", only the version " + MsdfWriter.VERSION + " loads: generate it again", cause.getMessage());
	}

	@Test(timeout = 10000L)
	public void refusesATruncatedMsdfFile() throws IOException {
		final byte[] bytes = Files.readAllBytes(MsdfFontLoaderTest.atlas.toPath());
		MsdfFontLoaderTest.fails(EOFException.class, new ByteArrayInputStream(Arrays.copyOf(bytes, 64)));
	}

	@Test(timeout = 10000L)
	public void refusesAMissingFile() {
		MsdfFontLoaderTest.fails(FileNotFoundException.class, new File(MsdfFontLoaderTest.FOLDER.getRoot(), "missing.msdf"));
	}

	@Test(timeout = 10000L)
	public void namesTheProblemOfAnEmptyFamily() {
		Assert.assertEquals("A msdf font needs at least one face", MsdfFontLoaderTest.cause().getMessage());
	}

	@Test(timeout = 10000L)
	public void namesTheProblemOfTwoFacesOfTheSameWeight() {
		Assert.assertEquals("Two faces share the weight 400", MsdfFontLoaderTest.cause(MsdfFontLoaderTest.atlas, MsdfFontLoaderTest.atlas).getMessage());
	}

	@Test(timeout = 10000L)
	public void handsBackTheExactFailureOfASource() {
		final IOException unreadable = new IOException("unreadable");
		final IllegalStateException broken = new IllegalStateException("broken");
		Assert.assertSame(unreadable, MsdfFontLoaderTest.cause((IMsdfSource) () -> {
			throw unreadable;
		}));
		Assert.assertSame(broken, MsdfFontLoaderTest.cause((IMsdfSource) () -> {
			throw broken;
		}));
	}

	@Test(timeout = 10000L)
	public void readsTheFacesInParallelOnItsOwnPool() {
		final CountDownLatch started = new CountDownLatch(2);
		final List<String> threads = new CopyOnWriteArrayList<>();
		final MsdfFont font = MsdfFontLoader.load(new FaceSource(started, threads, MsdfBinarySource.of(MsdfFontLoaderTest.atlas)), new FaceSource(started, threads, MsdfBinarySource.of(MsdfFontLoaderTest.atlas).weight(FontWeight.BOLD))).join();
		Assert.assertEquals(2, font.getFamily().getFaces().size());
		Assert.assertEquals(2, threads.size());
		for (final String thread : threads) {
			Assert.assertTrue(thread, thread.startsWith("MsdfFontLoader/"));
		}
	}

	@Test(timeout = 10000L)
	public void logsACustomSourceByItsClassInDevMode() {
		final PrintStream previous = System.out;
		final boolean devMode = JOID.inst().isDevMode();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			JOID.inst().setDevMode(true);
			System.setOut(new PrintStream(output, true));
			MsdfFontLoader.load(new FaceSource(new CountDownLatch(0), new CopyOnWriteArrayList<>(), MsdfBinarySource.of(MsdfFontLoaderTest.atlas))).join();
		} finally {
			System.setOut(previous);
			JOID.inst().setDevMode(devMode);
		}
		final String line = new String(output.toByteArray(), StandardCharsets.UTF_8);
		Assert.assertTrue(line, line.startsWith("[JOID] Font JOID Test Regular read from FaceSource in "));
	}

	private static InputStream stream(final String weight) {
		return JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-" + weight + ".ttf");
	}

	private static void fails(final Class<? extends Throwable> cause, final Object... faces) {
		try {
			MsdfFontLoader.load(faces).join();
			Assert.fail("The font must not load");
		} catch (final CompletionException expected) {
			Assert.assertTrue(String.valueOf(expected.getCause()), cause.isInstance(expected.getCause()));
		}
	}

	private static Throwable cause(final Object... faces) {
		try {
			MsdfFontLoader.load(faces).join();
		} catch (final CompletionException expected) {
			return expected.getCause();
		}
		throw new AssertionError("The font must not load");
	}

	private static final class FaceSource implements IMsdfSource {

		private final CountDownLatch started;
		private final List<String>   threads;
		private final IMsdfSource    source;

		private FaceSource(final CountDownLatch started, final List<String> threads, final IMsdfSource source) {
			this.started = started;
			this.threads = threads;
			this.source = source;
		}

		@Override
		public @NonNull MsdfFontFace read() throws IOException {
			this.threads.add(Thread.currentThread().getName());
			this.started.countDown();
			try {
				if (!this.started.await(5L, TimeUnit.SECONDS)) {
					throw new IOException("The faces were read one after the other");
				}
			} catch (final InterruptedException exception) {
				throw new IOException(exception);
			}
			return this.source.read();
		}

	}

}