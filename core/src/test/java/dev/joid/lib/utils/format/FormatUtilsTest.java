package dev.joid.lib.utils.format;

import org.junit.Assert;
import org.junit.Test;

public class FormatUtilsTest {

	@Test
	public void keepsSmallNumbersAsIs() {
		Assert.assertEquals("0", FormatUtils.formatNumber(0L));
		Assert.assertEquals("42", FormatUtils.formatNumber(42L));
		Assert.assertEquals("999", FormatUtils.formatNumber(999L));
	}

	@Test
	public void shortensThousands() {
		Assert.assertEquals("1k", FormatUtils.formatNumber(1_000L));
		Assert.assertEquals("1.5k", FormatUtils.formatNumber(1_500L));
		Assert.assertEquals("12k", FormatUtils.formatNumber(12_345L));
		Assert.assertEquals("999k", FormatUtils.formatNumber(999_999L));
	}

	@Test
	public void truncatesTheDecimal() {
		Assert.assertEquals("1.9k", FormatUtils.formatNumber(1_999L));
		Assert.assertEquals("2k", FormatUtils.formatNumber(2_049L));
	}

	@Test
	public void usesTheSuffixOfEachMagnitude() {
		Assert.assertEquals("1M", FormatUtils.formatNumber(1_000_000L));
		Assert.assertEquals("2.5B", FormatUtils.formatNumber(2_500_000_000L));
		Assert.assertEquals("1T", FormatUtils.formatNumber(1_000_000_000_000L));
		Assert.assertEquals("3.2P", FormatUtils.formatNumber(3_200_000_000_000_000L));
		Assert.assertEquals("1E", FormatUtils.formatNumber(1_000_000_000_000_000_000L));
	}

	@Test
	public void keepsTheSignOfNegativeNumbers() {
		Assert.assertEquals("-999", FormatUtils.formatNumber(-999L));
		Assert.assertEquals("-1.5k", FormatUtils.formatNumber(-1_500L));
	}

	@Test
	public void formatsTheExtremeLongs() {
		Assert.assertEquals("9.2E", FormatUtils.formatNumber(Long.MAX_VALUE));
		Assert.assertEquals("-9.2E", FormatUtils.formatNumber(Long.MIN_VALUE));
	}

}