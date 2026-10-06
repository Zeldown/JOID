package dev.joid.lib.draw.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.draw.text.builder.TextElement;
import dev.joid.lib.draw.text.builder.modifier.TextModifier;
import dev.joid.lib.draw.text.builder.utils.TextOverflow;
import dev.joid.lib.draw.text.utils.TextMode;
import dev.joid.lib.font.FontUsage;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.font.impl.msdf.MsdfFontLoader;
import dev.joid.lib.utils.align.Align;

public class DrawTextTest {

	private static MsdfFont montserrat;

	private final FixedFont font = new FixedFont();
	private final TextInfo  info = TextInfo.create(this.font, 10F);

	@BeforeClass
	public static void load() {
		DrawTextTest.montserrat = MsdfFontLoader.load(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Regular.ttf")).join();
	}

	@Test(expected = RuntimeException.class)
	public void refusesASecondInstance() {
		Assert.assertNotNull(DrawTextTest.draw());
		new DrawText();
	}

	@Test
	public void drawsNothingWithoutElements() {
		Assert.assertEquals(0D, DrawTextTest.draw().drawText(10D, 20D, Text.create()).getWidth(), 0D);
		for (final TextMode mode : TextMode.values()) {
			final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 100D, 100D, Text.create(), mode);
			Assert.assertEquals(0D, bounds.getWidth(), 0D);
			Assert.assertEquals(0D, bounds.getHeight(), 0D);
		}
		Assert.assertTrue(this.font.drawn.isEmpty());
	}

	@Test
	public void drawsEachElementAfterThePreviousOne() {
		final FontBounds bounds = DrawTextTest.draw().drawText(100D, 100D, this.text());
		Assert.assertEquals(Arrays.asList("ab@100.0,100.0", "c@120.0,100.0"), this.font.drawn);
		Assert.assertEquals(40D, bounds.getWidth(), 0D);
		Assert.assertEquals(40D, bounds.getHeight(), 0D);
	}

	@Test
	public void centersTheTextOnItsPosition() {
		DrawTextTest.draw().drawText(100D, 100D, this.text().align(Align.CENTER, Align.CENTER));
		Assert.assertEquals(Arrays.asList("ab@80.0,90.0", "c@100.0,80.0"), this.font.drawn);
	}

	@Test
	public void endsTheTextOnItsPosition() {
		DrawTextTest.draw().drawText(100D, 100D, this.text().align(Align.END, Align.END));
		Assert.assertEquals(Arrays.asList("ab@60.0,80.0", "c@80.0,60.0"), this.font.drawn);
	}

	@Test
	public void sharesTheBoundsOfTheWholeTextWithEveryElement() {
		DrawTextTest.draw().drawText(100D, 100D, this.text().align(Align.CENTER, Align.CENTER));
		Assert.assertEquals(Arrays.asList("80.0,80.0 40.0x40.0", "80.0,80.0 40.0x40.0"), this.font.runs);
	}

	@Test
	public void drawsTheModifiedText() {
		DrawTextTest.draw().drawText(0D, 0D, Text.create("ab", this.info).modifier(TextModifier.UPPER_CASE));
		Assert.assertEquals(Collections.singletonList("AB@0.0,0.0"), this.font.drawn);
	}

	@Test
	public void drawsEachElementUnderItsOrigin() {
		final boolean previous = JOID.inst().isDevMode();
		JOID.inst().setDevMode(true);
		try {
			final Text text = Text.create("ab", this.info);
			DrawTextTest.draw().drawText(0D, 0D, text);
			Assert.assertSame(text.get(0).getOrigin(), this.font.origin);
		} finally {
			JOID.inst().setDevMode(previous);
		}
	}

	@Test
	public void drawsAPlainStringWithItsAlignment() {
		final FontBounds bounds = DrawTextTest.draw().drawText(100D, 100D, "ab", this.info, Align.END, Align.CENTER);
		Assert.assertEquals(Collections.singletonList("ab@80.0,90.0"), this.font.drawn);
		Assert.assertEquals(20D, bounds.getWidth(), 0D);
		Assert.assertEquals(20D, bounds.getHeight(), 0D);
	}

	@Test
	public void alignsTheTextInsideItsBox() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 100D, 50D, Text.create("ab", this.info), TextMode.NORMAL);
		DrawTextTest.draw().drawText(0D, 0D, 100D, 50D, Text.create("ab", this.info, Align.CENTER, Align.CENTER), TextMode.NORMAL);
		DrawTextTest.draw().drawText(0D, 0D, 100D, 50D, Text.create("ab", this.info, Align.END, Align.END), TextMode.NORMAL);
		Assert.assertEquals(Arrays.asList("ab@0.0,0.0", "ab@40.0,15.0", "ab@80.0,30.0"), this.font.drawn);
		Assert.assertEquals(20D, bounds.getWidth(), 0D);
		Assert.assertEquals(20D, bounds.getHeight(), 0D);
	}

	@Test
	public void cutsALongTextWithItsOverflowMark() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 45D, 30D, "abcdef", this.info, Align.START, Align.START, TextOverflow.ELLIPSIS, TextMode.OVERFLOW);
		Assert.assertEquals(Collections.singletonList("a...@0.0,0.0"), this.font.drawn);
		Assert.assertEquals(45D, bounds.getWidth(), 0D);
		Assert.assertEquals(20D, bounds.getHeight(), 0D);
	}

	@Test
	public void cutsTheModifiedText() {
		DrawTextTest.draw().drawText(0D, 0D, 45D, 30D, Text.create("abcdef", this.info, TextOverflow.ELLIPSIS).modifier(TextModifier.UPPER_CASE), TextMode.OVERFLOW);
		Assert.assertEquals(Collections.singletonList("A...@0.0,0.0"), this.font.drawn);
	}

	@Test
	public void cutsALongTextWithoutMarkWithoutOverflow() {
		DrawTextTest.draw().drawText(0D, 0D, 45D, 30D, Text.create("abcdef", this.info), TextMode.OVERFLOW);
		Assert.assertEquals(Collections.singletonList("abcd@0.0,0.0"), this.font.drawn);
	}

	@Test
	public void keepsAShortTextWhole() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 100D, 30D, Text.create("abc", this.info, TextOverflow.ELLIPSIS), TextMode.OVERFLOW);
		Assert.assertEquals(Collections.singletonList("abc@0.0,0.0"), this.font.drawn);
		Assert.assertEquals(100D, bounds.getWidth(), 0D);
		Assert.assertEquals(20D, bounds.getHeight(), 0D);
	}

	@Test
	public void cutsOnlyTheElementThatOverflows() {
		final Text text = Text.create(TextElement.create("ab", this.info), TextElement.create("cdef", this.info)).overflow(TextOverflow.DOT);
		DrawTextTest.draw().drawText(0D, 0D, 55D, 30D, text, TextMode.OVERFLOW);
		Assert.assertEquals(Arrays.asList("ab@0.0,0.0", "cd.@20.0,0.0"), this.font.drawn);
		Assert.assertEquals("abcdef", text.getText());
	}

	@Test
	public void drawsNothingWhenNoLetterFits() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 5D, 30D, Text.create("abc", this.info), TextMode.OVERFLOW);
		Assert.assertTrue(this.font.drawn.isEmpty());
		Assert.assertEquals(0D, bounds.getWidth(), 0D);
		Assert.assertEquals(0D, bounds.getHeight(), 0D);
	}

	@Test
	public void alignsTheCutTextInsideItsBox() {
		DrawTextTest.draw().drawText(0D, 0D, 45D, 40D, Text.create("abcdef", this.info, Align.CENTER, Align.CENTER, TextOverflow.ELLIPSIS), TextMode.OVERFLOW);
		DrawTextTest.draw().drawText(0D, 0D, 45D, 40D, Text.create("abcdef", this.info, Align.END, Align.END, TextOverflow.ELLIPSIS), TextMode.OVERFLOW);
		Assert.assertEquals(Arrays.asList("a...@2.5,10.0", "a...@5.0,20.0"), this.font.drawn);
	}

	@Test
	public void wrapsAtTheLastSpaceThatFits() {
		Assert.assertEquals(Arrays.asList("ab", "cd", "ef"), DrawTextTest.draw().getLines(35D, "ab cd ef", this.info));
	}

	@Test
	public void cutsAWordWiderThanTheLine() {
		Assert.assertEquals(Arrays.asList("abc", "def"), DrawTextTest.draw().getLines(35D, "abcdef", this.info));
	}

	@Test
	public void keepsAShortTextOnOneLine() {
		Assert.assertEquals(Collections.singletonList("ab cd"), DrawTextTest.draw().getLines(100D, "ab cd", this.info));
	}

	@Test
	public void breaksOnEveryLineMarker() {
		for (final String text : Arrays.asList("ab\ncd", "ab\rcd", "ab<br>cd")) {
			final List<String> lines = DrawTextTest.draw().getLines(100D, text, this.info);
			Assert.assertEquals(text, 2, lines.size());
			Assert.assertEquals(text, "ab", lines.get(0));
			Assert.assertEquals(text, "cd", lines.get(1));
		}
	}

	@Test
	public void keepsTheSettingsOfTheTextOnEachLine() {
		final List<Text> lines = DrawTextTest.draw().getLines(35D, Text.create("ab cd", this.info, Align.END, TextOverflow.DOT).modifier(TextModifier.UPPER_CASE));
		Assert.assertEquals(2, lines.size());
		Assert.assertEquals("AB", lines.get(0).getText());
		Assert.assertEquals("CD", lines.get(1).getText());
		Assert.assertSame(Align.END, lines.get(1).getHorizontalAlignment());
		Assert.assertSame(TextOverflow.DOT, lines.get(1).getOverflow());
	}

	@Test
	public void keepsTheInfoOfEachElementOnItsLine() {
		final TextInfo large = this.info.copy().fontSize(20F);
		final List<Text> lines = DrawTextTest.draw().getLines(55D, Text.create(TextElement.create("ab", this.info), TextElement.create(" cd", large)));
		Assert.assertEquals(2, lines.size());
		Assert.assertEquals("ab", lines.get(0).getText());
		Assert.assertSame(this.info, lines.get(0).get(0).getInfo());
		Assert.assertEquals("cd", lines.get(1).getText());
		Assert.assertSame(large, lines.get(1).get(0).getInfo());
	}

	@Test
	public void keepsTheOriginOfTheSplitElements() {
		final boolean previous = JOID.inst().isDevMode();
		JOID.inst().setDevMode(true);
		try {
			final Text text = Text.create("ab cd", this.info);
			final List<Text> lines = DrawTextTest.draw().getLines(35D, text);
			Assert.assertNotNull(text.get(0).getOrigin());
			Assert.assertSame(text.get(0).getOrigin(), lines.get(0).get(0).getOrigin());
			Assert.assertSame(text.get(0).getOrigin(), lines.get(1).get(0).getOrigin());
		} finally {
			JOID.inst().setDevMode(previous);
		}
	}

	@Test
	public void wrapsASentenceMeasuredWithTheFont() {
		final TextInfo info = TextInfo.create(DrawTextTest.montserrat, 20F);
		final double width = info.getWidth("Hello world");
		Assert.assertEquals(Arrays.asList("Hello", "world"), DrawTextTest.draw().getLines(width - 1D, "Hello world", info));
		Assert.assertEquals(Collections.singletonList("Hello world"), DrawTextTest.draw().getLines(width + 1D, "Hello world", info));
	}

	@Test
	public void drawsEachLineBelowThePreviousOne() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 35D, 30D, Text.create("ab cd ef", this.info), TextMode.SPLIT);
		Assert.assertEquals(Arrays.asList("ab@0.0,0.0", "cd@0.0,20.0", "ef@0.0,40.0"), this.font.drawn);
		Assert.assertEquals(35D, bounds.getWidth(), 0D);
		Assert.assertEquals(60D, bounds.getHeight(), 0D);
	}

	@Test
	public void centersItsLinesInsideItsBox() {
		DrawTextTest.draw().drawText(0D, 0D, 35D, 100D, Text.create("ab cd ef", this.info, Align.CENTER, Align.CENTER), TextMode.SPLIT);
		Assert.assertEquals(Arrays.asList("ab@7.5,20.0", "cd@7.5,40.0", "ef@7.5,60.0"), this.font.drawn);
	}

	@Test
	public void endsItsLinesInsideItsBox() {
		DrawTextTest.draw().drawText(0D, 0D, 35D, 100D, Text.create("ab cd ef", this.info, Align.END, Align.END), TextMode.SPLIT);
		Assert.assertEquals(Arrays.asList("ab@15.0,40.0", "cd@15.0,60.0", "ef@15.0,80.0"), this.font.drawn);
	}

	@Test
	public void drawsNothingForABlankSplitText() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 35D, 100D, Text.create("", this.info), TextMode.SPLIT);
		Assert.assertTrue(this.font.drawn.isEmpty());
		Assert.assertEquals(0D, bounds.getWidth(), 0D);
		Assert.assertEquals(0D, bounds.getHeight(), 0D);
	}

	@Test
	public void skipsTheLinesBelowItsBox() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 35D, 50D, Text.create("ab cd ef", this.info), TextMode.BOX);
		Assert.assertEquals(Arrays.asList("ab@0.0,0.0", "cd@0.0,20.0"), this.font.drawn);
		Assert.assertEquals(35D, bounds.getWidth(), 0D);
		Assert.assertEquals(40D, bounds.getHeight(), 0D);
	}

	@Test
	public void skipsTheLinesAboveItsBox() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 35D, 50D, Text.create("ab cd ef", this.info, Align.START, Align.END), TextMode.BOX);
		Assert.assertEquals(Arrays.asList("cd@0.0,10.0", "ef@0.0,30.0"), this.font.drawn);
		Assert.assertEquals(40D, bounds.getHeight(), 0D);
	}

	@Test
	public void skipsTheLinesOnBothSidesOfItsBox() {
		final FontBounds bounds = DrawTextTest.draw().drawText(0D, 0D, 35D, 50D, Text.create("ab cd ef", this.info, Align.START, Align.CENTER), TextMode.BOX);
		Assert.assertEquals(Collections.singletonList("cd@0.0,15.0"), this.font.drawn);
		Assert.assertEquals(20D, bounds.getHeight(), 0D);
	}

	@Test
	public void keepsATextThatFitsWithoutItsOverflowMark() {
		DrawTextTest.draw().drawText(0D, 0D, 50D, 30D, Text.create("abc", this.info, TextOverflow.ELLIPSIS), TextMode.OVERFLOW);
		Assert.assertEquals(Collections.singletonList("abc@0.0,0.0"), this.font.drawn);
	}

	@Test
	public void keepsALineAsWideAsItsBoxOnOneLine() {
		Assert.assertEquals(Collections.singletonList("abc"), DrawTextTest.draw().getLines(30D, "abc", this.info));
	}

	@Test
	public void breaksAWindowsLineEndingOnce() {
		Assert.assertEquals(2, DrawTextTest.draw().getLines(100D, "ab\r\ncd", this.info).size());
	}

	@Test
	public void dropsTheSpaceWhereALineBreaks() {
		Assert.assertEquals(Arrays.asList("abc", "def"), DrawTextTest.draw().getLines(35D, "abc def", this.info));
	}

	@Test
	public void keepsAnEmptyLineAfterAFinalLineBreak() {
		Assert.assertEquals(Arrays.asList("ab", ""), DrawTextTest.draw().getLines(100D, "ab\n", this.info));
	}

	@Test
	public void leavesTheLineBreaksOutOfItsLines() {
		Assert.assertEquals(Arrays.asList("ab", "", "cd"), DrawTextTest.draw().getLines(100D, "ab\n\ncd", this.info));
	}

	@Test
	public void wrapsADoubleSpaceWithoutAnEmptyLine() {
		Assert.assertEquals(Arrays.asList("abc", " def"), DrawTextTest.draw().getLines(35D, "abc  def", this.info));
	}

	@Test
	public void putsEachLetterOnItsOwnLineInANarrowBox() {
		Assert.assertEquals(Arrays.asList("a", "b"), DrawTextTest.draw().getLines(5D, "ab", this.info));
	}

	@Test
	public void modifiesItsLinesOnlyOnce() {
		final List<Text> lines = DrawTextTest.draw().getLines(1000D, Text.create("hello big world", this.info).modifier(TextModifier.CAMEL_CASE));
		Assert.assertEquals(1, lines.size());
		Assert.assertEquals("helloBigWorld", lines.get(0).getText());
	}

	@Test
	public void drawsTheModifiedTextOfEachLineOnce() {
		DrawTextTest.draw().drawText(0D, 0D, 1000D, 30D, Text.create("hello big world", this.info).modifier(TextModifier.CAMEL_CASE), TextMode.SPLIT);
		Assert.assertEquals(Collections.singletonList("helloBigWorld@0.0,0.0"), this.font.drawn);
	}

	@Test
	public void cutsTheModifiedTextOnlyOnce() {
		DrawTextTest.draw().drawText(0D, 0D, 85D, 30D, Text.create("hello big world", this.info, TextOverflow.DOT).modifier(TextModifier.CAMEL_CASE), TextMode.OVERFLOW);
		Assert.assertEquals(Collections.singletonList("helloBi.@0.0,0.0"), this.font.drawn);
	}

	private Text text() {
		return Text.create(TextElement.create("ab", this.info), TextElement.create("c", this.info.copy().fontSize(20F)));
	}

	private static DrawText draw() {
		return DrawUtils.TEXT;
	}

	private static final class FixedFont implements IFont, IFontProvider {

		private final List<String> runs  = new ArrayList<>();
		private final List<String> drawn = new ArrayList<>();

		private StackTraceElement[] origin;

		@Override
		public IFontProvider getFontProvider() {
			return this;
		}

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
			this.drawn.add(text + "@" + x + "," + y);
			this.origin = FontUsage.getOrigin();
			return new FontBounds(this.getWidth(text, info), this.getHeight(text, info));
		}

		@Override
		public FontBounds drawText(final double x, final double y, final String text, final TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
			this.runs.add(runX + "," + runY + " " + runWidth + "x" + runHeight);
			return IFontProvider.super.drawText(x, y, text, info, runX, runY, runWidth, runHeight);
		}

		@Override
		public double getWidth(final String text, final TextInfo info) {
			return text.length() * info.getFontSize();
		}

		@Override
		public double getHeight(final String text, final TextInfo info) {
			return info.getFontSize() * 2D;
		}

		@Override
		public double getLineHeight(final TextInfo info) {
			return info.getFontSize() * 2D;
		}

	}

}