package dev.joid.msdf.font;

import java.awt.Font;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.msdf.MsdfFonts;

public class KerningTest {

	@Test
	public void readsThePairsOfTheGposTable() throws Exception {
		final byte[] data = MsdfFonts.read(MsdfFonts.REGULAR);
		final Font font = Glyphs.load(data);
		final Kerning kerning = Kerning.read(FontFile.read(data), font, new int[] {'A', 'V', 'x'});
		Assert.assertEquals(1000, kerning.getUnitsPerEm());
		Assert.assertEquals(1, kerning.getKerning().size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.getKerning().get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void keepsOnlyThePairsOfTheCharset() throws Exception {
		final byte[] data = MsdfFonts.read(MsdfFonts.BOLD_ITALIC);
		Assert.assertTrue(Kerning.read(FontFile.read(data), Glyphs.load(data), new int[] {'A', 'x'}).getKerning().isEmpty());
	}

	@Test
	public void readsTheAdvancePastThePlacements() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {1, 24, 7, 4, 1, 12, 1, 3, 11, 22, -80, 33, 1, 1, 2}))));
		Assert.assertEquals(1, kerning.size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void ignoresAPairWithoutAdvance() throws Exception {
		Assert.assertTrue(KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {1, 18, 1, 0, 1, 12, 1, 3, -80, 1, 1, 2})))).isEmpty());
	}

	@Test
	public void readsTheRangesOfACoverage() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {1, 34, 4, 0, 3, 16, 22, 28, 1, 3, -10, 1, 2, -20, 1, 4, -30, 2, 2, 2, 3, 0, 4, 4, 2}))));
		Assert.assertEquals(3, kerning.size());
		Assert.assertEquals(Integer.valueOf(-10), kerning.get((long) 'A' << 32 | 'V'));
		Assert.assertEquals(Integer.valueOf(-20), kerning.get((long) 'V' << 32 | 'A'));
		Assert.assertEquals(Integer.valueOf(-30), kerning.get((long) 'x' << 32 | 'x'));
	}

	@Test
	public void readsNoMorePairSetsThanCoveredGlyphs() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {1, 26, 4, 0, 2, 14, 20, 1, 3, -80, 1, 4, -40, 1, 1, 2}))));
		Assert.assertEquals(1, kerning.size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void readsTheAdjustmentsOfGlyphClasses() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {2, 24, 4, 0, 34, 42, 2, 2, 0, -10, -20, -80, 2, 1, 2, 4, 0, 1, 2, 1, 1, 2, 1, 3, 3, 1}))));
		Assert.assertEquals(5, kerning.size());
		Assert.assertEquals(Integer.valueOf(-20), kerning.get((long) 'A' << 32 | 'A'));
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
		Assert.assertEquals(Integer.valueOf(-20), kerning.get((long) 'A' << 32 | 'x'));
		Assert.assertEquals(Integer.valueOf(-10), kerning.get((long) 'V' << 32 | 'V'));
		Assert.assertEquals(Integer.valueOf(-10), kerning.get((long) 'x' << 32 | 'V'));
	}

	@Test
	public void skipsTheClassesBeyondTheDeclaredCounts() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {2, 18, 4, 0, 28, 36, 1, 1, -30, 1, 3, 2, 3, 4, 1, 2, 1, 1, 2, 1, 3, 3, 1}))));
		Assert.assertEquals(4, kerning.size());
		Assert.assertNull(kerning.get((long) 'A' << 32 | 'x'));
		Assert.assertNull(kerning.get((long) 'x' << 32 | 'V'));
		Assert.assertEquals(Integer.valueOf(-30), kerning.get((long) 'x' << 32 | 'A'));
	}

	@Test
	public void followsTheExtensionLookups() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(9, KerningTest.extension(2, KerningTest.pair(2, 3, -80))))));
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void ignoresTheOtherLookupTypes() throws Exception {
		Assert.assertTrue(KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(1, KerningTest.pair(2, 3, -80)), KerningTest.lookup(9, KerningTest.extension(1, KerningTest.pair(2, 3, -80)))))).isEmpty());
	}

	@Test
	public void ignoresAnUnknownPairFormat() throws Exception {
		Assert.assertTrue(KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, new int[] {3, 18, 4, 0, 1, 12, 1, 3, -80, 1, 1, 2})))).isEmpty());
	}

	@Test
	public void keepsTheFirstSubtableThatMatchesAPair() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, KerningTest.pair(2, 3, -80), KerningTest.pair(2, 3, -40)))));
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void letsAZeroAdjustmentHideTheNextSubtables() throws Exception {
		Assert.assertTrue(KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, KerningTest.pair(2, 3, 0), KerningTest.pair(2, 3, -80))))).isEmpty());
	}

	@Test
	public void addsTheLookupsTogether() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, KerningTest.pair(2, 3, -80)), KerningTest.lookup(2, KerningTest.pair(2, 3, -40)))));
		Assert.assertEquals(Integer.valueOf(-120), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void dropsThePairsThatCancelOut() throws Exception {
		Assert.assertTrue(KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, KerningTest.pair(2, 3, -80)), KerningTest.lookup(2, KerningTest.pair(2, 3, 80))))).isEmpty());
	}

	@Test
	public void ignoresTheGlyphsOutsideTheCharset() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos(KerningTest.lookup(2, KerningTest.pair(1, 3, -50), KerningTest.pair(2, 1, -60), KerningTest.pair(2, 3, -80)))));
		Assert.assertEquals(1, kerning.size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void readsOnlyTheKernFeature() throws Exception {
		Assert.assertTrue(KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos("liga", new int[] {0}, KerningTest.lookup(2, KerningTest.pair(2, 3, -80))))).isEmpty());
	}

	@Test
	public void appliesALookupListedTwiceOnce() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("GPOS", KerningTest.gpos("kern", new int[] {0, 0}, KerningTest.lookup(2, KerningTest.pair(2, 3, -80)))));
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void fallsBackToTheHorizontalPairsOfTheKernTable() throws Exception {
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build("kern", KerningTest.kern(KerningTest.subtable(0x0000, 2, 3, -10), KerningTest.subtable(0x0201, 2, 3, -20), KerningTest.subtable(0x0001, 2, 3, -80, 3, 2, -40))));
		Assert.assertEquals(2, kerning.size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
		Assert.assertEquals(Integer.valueOf(-40), kerning.get((long) 'V' << 32 | 'A'));
	}

	@Test
	public void prefersTheGposPairsToTheKernTable() throws Exception {
		final Map<String, int[]> tables = new LinkedHashMap<>();
		tables.put("GPOS", KerningTest.gpos(KerningTest.lookup(2, KerningTest.pair(2, 3, -80))));
		tables.put("kern", KerningTest.kern(KerningTest.subtable(0x0001, 2, 3, -10, 3, 2, -40)));
		final Map<Long, Integer> kerning = KerningTest.kerning(MsdfFonts.build(tables));
		Assert.assertEquals(1, kerning.size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void readsTheKernTableWhenTheGposHasNoPair() throws Exception {
		final Map<String, int[]> tables = new LinkedHashMap<>();
		tables.put("GPOS", KerningTest.gpos("liga", new int[] {0}, KerningTest.lookup(2, KerningTest.pair(2, 3, -80))));
		tables.put("kern", KerningTest.kern(KerningTest.subtable(0x0001, 2, 3, -10)));
		Assert.assertEquals(Integer.valueOf(-10), KerningTest.kerning(MsdfFonts.build(tables)).get((long) 'A' << 32 | 'V'));
	}

	private static Map<Long, Integer> kerning(final byte[] data) throws Exception {
		return Kerning.read(FontFile.read(data), Glyphs.load(MsdfFonts.read(MsdfFonts.REGULAR)), new int[] {'A', 'V', 'x'}).getKerning();
	}

	private static int[] gpos(final int[]... lookups) {
		return KerningTest.gpos("kern", IntStream.range(0, lookups.length).toArray(), lookups);
	}

	private static int[] gpos(final String feature, final int[] indices, final int[]... lookups) {
		final IntStream.Builder words = IntStream.builder().add(1).add(0).add(0).add(10).add(22 + indices.length * 2);
		words.add(1).add(feature.charAt(0) << 8 | feature.charAt(1)).add(feature.charAt(2) << 8 | feature.charAt(3)).add(8).add(0).add(indices.length);
		Arrays.stream(indices).forEach(words);
		return KerningTest.offsets(words.add(lookups.length), 2 + lookups.length * 2, lookups);
	}

	private static int[] lookup(final int type, final int[]... subtables) {
		return KerningTest.offsets(IntStream.builder().add(type).add(0).add(subtables.length), 6 + subtables.length * 2, subtables);
	}

	private static int[] offsets(final IntStream.Builder words, final int start, final int[]... tables) {
		int offset = start;
		for (final int[] table : tables) {
			words.add(offset);
			offset += table.length * 2;
		}

		for (final int[] table : tables) {
			Arrays.stream(table).forEach(words);
		}
		return words.build().toArray();
	}

	private static int[] extension(final int type, final int[] subtable) {
		return IntStream.concat(IntStream.of(1, type, 0, 8), Arrays.stream(subtable)).toArray();
	}

	private static int[] pair(final int first, final int second, final int value) {
		return new int[] {1, 18, 4, 0, 1, 12, 1, second, value, 1, 1, first};
	}

	private static int[] kern(final int[]... subtables) {
		return IntStream.concat(IntStream.of(0, subtables.length), Arrays.stream(subtables).flatMapToInt(Arrays::stream)).toArray();
	}

	private static int[] subtable(final int coverage, final int... pairs) {
		return IntStream.concat(IntStream.of(0, 14 + pairs.length * 2, coverage, pairs.length / 3, 0, 0, 0), Arrays.stream(pairs)).toArray();
	}

}