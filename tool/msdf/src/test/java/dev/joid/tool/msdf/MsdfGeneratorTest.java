package dev.joid.tool.msdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MsdfGeneratorTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void generatesTheAtlasOfATrueTypeFont() throws Exception {
		final File target = this.target();
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.REGULAR), target, MsdfGenerator.codepoints("[32, 126]"), 256, 256, 8D, 0D);
		Assert.assertTrue(summary, summary.startsWith("JOID Test Regular, weight 400, 4 glyphs, 1 kerning pairs, size "));
		Assert.assertArrayEquals("JOIDMSDF".getBytes(StandardCharsets.US_ASCII), Arrays.copyOf(Files.readAllBytes(target.toPath()), 8));
	}

	@Test
	public void generatesTheAtlasOfAnOpenTypeFont() throws Exception {
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.BOLD_ITALIC), this.target(), MsdfGenerator.codepoints("[32, 126]"), 256, 256, 8D, 0D);
		Assert.assertTrue(summary, summary.startsWith("JOID Test Bold Italic, weight 700 italic, 4 glyphs, 1 kerning pairs, size "));
	}

	@Test
	public void generatesTheFirstFontOfACollection() throws Exception {
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.COLLECTION), this.target(), MsdfGenerator.codepoints("[32, 126]"), 256, 256, 8D, 0D);
		Assert.assertTrue(summary, summary.startsWith("JOID Test Regular, weight 400, 4 glyphs, 1 kerning pairs, size "));
	}

	@Test
	public void forcesTheEmSizeWhenAsked() throws Exception {
		final String summary = MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.REGULAR), this.target(), MsdfGenerator.codepoints("[65, 65]"), 256, 256, 8D, 32D);
		Assert.assertTrue(summary, summary.endsWith("size 32.0px"));
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAnEmSizeThatDoesNotFit() throws Exception {
		MsdfGenerator.generate(MsdfFonts.read(MsdfFonts.REGULAR), this.target(), MsdfGenerator.codepoints("[65, 65]"), 64, 64, 8D, 4096D);
	}

	@Test
	public void writesTheFontFileOfTheCommandLine() throws Exception {
		final File output = this.folder.newFolder();
		final String printed = MsdfGeneratorTest.capture(() -> MsdfGenerator.main(new String[] {"--font", MsdfFonts.copy(MsdfFonts.REGULAR, this.folder.getRoot()).getAbsolutePath(), "--output", output.getAbsolutePath(), "--width", "128", "--height", "128", "--range", "4"}));
		final File font = new File(output, "font.msdf");
		Assert.assertTrue(font.isFile());
		Assert.assertTrue(printed, printed.contains(" -> JOID Test Regular, weight 400, 4 glyphs, 1 kerning pairs, size "));
		Assert.assertTrue(printed, printed.trim().endsWith("ms"));
	}

	@Test
	public void refusesToRunWithoutAFont() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> MsdfGenerator.main(new String[0]));
		Assert.assertTrue(exception.getMessage().startsWith("Usage: msdf --font"));
	}

	@Test
	public void readsACharsetInlineOrFromAFile() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66, 67, 70}, MsdfGenerator.codepoints("[65, 67], 70"));
		final File charset = this.folder.newFile("charset.txt");
		Files.write(charset.toPath(), "[48, 49]\n".getBytes(StandardCharsets.UTF_8));
		Assert.assertArrayEquals(new int[] {48, 49}, MsdfGenerator.codepoints(charset.getAbsolutePath()));
	}

	@Test
	public void skipsTheEmptyEntriesOfACharset() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66}, MsdfGenerator.codepoints("65,, 66,"));
	}

	@Test
	public void readsARangeWithoutSpaceAfterTheComma() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66, 67, 70}, MsdfGenerator.codepoints("[65,67],70"));
	}

	@Test
	public void readsARangeWithSpacesInsideTheBrackets() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66, 67, 70}, MsdfGenerator.codepoints("[ 65, 67 ], 70"));
	}

	@Test
	public void keepsEachCodepointOfACharsetOnce() throws Exception {
		Assert.assertArrayEquals(new int[] {65, 66, 67, 70}, MsdfGenerator.codepoints("[65, 67], 66, 70, [65, 66]"));
	}

	@Test
	public void explainsACharsetThatIsNeitherAFileNorCodepoints() throws Exception {
		final IllegalArgumentException missing = Assert.assertThrows(IllegalArgumentException.class, () -> MsdfGenerator.codepoints("charst.txt"));
		Assert.assertEquals("The charset charst.txt is neither a file nor codepoints like [32, 126], 8364: check the path or separate the values with commas", missing.getMessage());
		final File charset = this.folder.newFile("charset.txt");
		Files.write(charset.toPath(), "65\n66\n".getBytes(StandardCharsets.UTF_8));
		final IllegalArgumentException lines = Assert.assertThrows(IllegalArgumentException.class, () -> MsdfGenerator.codepoints(charset.getAbsolutePath()));
		Assert.assertTrue(lines.getMessage(), lines.getMessage().endsWith(" is neither a file nor codepoints like [32, 126], 8364: check the path or separate the values with commas"));
	}

	@Test
	public void forcesTheEmSizeOfTheCommandLine() throws Exception {
		final File output = this.folder.newFolder();
		final String printed = MsdfGeneratorTest.capture(() -> MsdfGenerator.main(new String[] {"--font", MsdfFonts.copy(MsdfFonts.REGULAR, this.folder.getRoot()).getAbsolutePath(), "--output", output.getAbsolutePath(), "--width", "128", "--height", "128", "--range", "4", "--size", "24", "--charset", "[65, 65]"}));
		Assert.assertTrue(printed, printed.contains(" -> JOID Test Regular, weight 400, 1 glyphs, 0 kerning pairs, size 24.0px, "));
	}

	@Test
	public void keepsTheDashesInsideAnOptionName() {
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> MsdfGenerator.main(new String[] {"--fo-nt", "missing.ttf"}));
		Assert.assertTrue(exception.getMessage().startsWith("Usage: msdf --font"));
	}

	@Test
	public void refusesAnUnknownOption() throws Exception {
		final File output = this.folder.newFolder();
		final String font = MsdfFonts.copy(MsdfFonts.REGULAR, this.folder.getRoot()).getAbsolutePath();
		final IllegalArgumentException exception = Assert.assertThrows(IllegalArgumentException.class, () -> MsdfGenerator.main(new String[] {"--font", font, "--output", output.getAbsolutePath(), "--chrset", "[65, 65]"}));
		Assert.assertTrue(exception.getMessage(), exception.getMessage().startsWith("Usage: msdf --font"));
		Assert.assertFalse(new File(output, "font.msdf").exists());
	}

	@Test
	public void refusesAnOptionWithoutValue() throws Exception {
		try {
			MsdfGenerator.main(new String[] {"--output", "out", "--font"});
			Assert.fail("An option without value must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The option --font has no value, give it one after a space: --font <value>", expected.getMessage());
		}
	}

	private File target() throws Exception {
		return this.folder.newFile("font.msdf");
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