package dev.joid.tool.msdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class MsdfGeneratorTest {

	@Test
	public void generatesTheAtlasOfATrueTypeFont() throws Exception {
		final File target = MsdfGeneratorTest.target();
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.REGULAR), target, MsdfGenerator.codepoints("[32, 126]"), 256, 256, 8D, 0D);
		Assert.assertTrue(summary, summary.startsWith("JOID Test Regular, weight 400, 4 glyphs, 1 kerning pairs, size "));
		Assert.assertArrayEquals("JOIDMSDF".getBytes(StandardCharsets.US_ASCII), Arrays.copyOf(Files.readAllBytes(target.toPath()), 8));
	}

	@Test
	public void generatesTheAtlasOfAnOpenTypeFont() throws Exception {
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.BOLD_ITALIC), MsdfGeneratorTest.target(), MsdfGenerator.codepoints("[32, 126]"), 256, 256, 8D, 0D);
		Assert.assertTrue(summary, summary.startsWith("JOID Test Bold Italic, weight 700 italic, 4 glyphs, 1 kerning pairs, size "));
	}

	@Test
	public void generatesTheFirstFontOfACollection() throws Exception {
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.COLLECTION), MsdfGeneratorTest.target(), MsdfGenerator.codepoints("[32, 126]"), 256, 256, 8D, 0D);
		Assert.assertTrue(summary, summary.startsWith("JOID Test Regular, weight 400, 4 glyphs, 1 kerning pairs, size "));
	}

	@Test
	public void forcesTheEmSizeWhenAsked() throws Exception {
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.REGULAR), MsdfGeneratorTest.target(), MsdfGenerator.codepoints("[65, 65]"), 256, 256, 8D, 32D);
		Assert.assertTrue(summary, summary.endsWith("size 32.0px"));
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAnEmSizeThatDoesNotFit() throws Exception {
		MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.REGULAR), MsdfGeneratorTest.target(), MsdfGenerator.codepoints("[65, 65]"), 64, 64, 8D, 4096D);
	}

	@Test
	public void writesTheFontFileOfTheCommandLine() throws Exception {
		final File output = Files.createTempDirectory("joid-msdf-").toFile();
		final String printed = MsdfGeneratorTest.capture(() -> MsdfGenerator.main(new String[] {"--font", MsdfFonts.copy(MsdfFonts.REGULAR).getAbsolutePath(), "--output", output.getAbsolutePath(), "--width", "128", "--height", "128", "--range", "4"}));
		final File font = new File(output, "font.msdf");
		Assert.assertTrue(font.isFile());
		Assert.assertTrue(printed, printed.contains(" -> JOID Test Regular, weight 400, 4 glyphs, 1 kerning pairs, size "));
		Assert.assertTrue(printed, printed.trim().endsWith("ms"));
		Files.delete(font.toPath());
		Files.delete(output.toPath());
	}

	@Test
	public void printsTheUsageWithoutAFont() throws Exception {
		Assert.assertTrue(MsdfGeneratorTest.capture(() -> MsdfGenerator.main(new String[0])).startsWith("Usage: msdf --font"));
	}

	@Test
	public void readsACharsetInlineOrFromAFile() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66, 67, 70}, MsdfGenerator.codepoints("[65, 67], 70"));
		final File charset = File.createTempFile("joid-charset-", ".txt");
		charset.deleteOnExit();
		Files.write(charset.toPath(), "[48, 49]\n".getBytes(StandardCharsets.UTF_8));
		Assert.assertArrayEquals(new int[] {48, 49}, MsdfGenerator.codepoints(charset.getAbsolutePath()));
	}

	@Test
	public void skipsTheEmptyEntriesOfACharset() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66}, MsdfGenerator.codepoints("65,, 66,"));
	}

	@Test
	public void forcesTheEmSizeOfTheCommandLine() throws Exception {
		final File output = Files.createTempDirectory("joid-msdf-").toFile();
		final String printed = MsdfGeneratorTest.capture(() -> MsdfGenerator.main(new String[] {"--font", MsdfFonts.copy(MsdfFonts.REGULAR).getAbsolutePath(), "--output", output.getAbsolutePath(), "--width", "128", "--height", "128", "--range", "4", "--size", "24", "--charset", "[65, 65]"}));
		Assert.assertTrue(printed, printed.contains(" -> JOID Test Regular, weight 400, 1 glyphs, 0 kerning pairs, size 24.0px, "));
		Files.delete(new File(output, "font.msdf").toPath());
		Files.delete(output.toPath());
	}

	private static File target() throws Exception {
		final File target = File.createTempFile("joid-msdf-", ".msdf");
		target.deleteOnExit();
		return target;
	}

	private static String capture(final Action action) throws Exception {
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output, true));
			action.run();
		} finally {
			System.setOut(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	@FunctionalInterface
	private interface Action {

		public void run() throws Exception;

	}

}