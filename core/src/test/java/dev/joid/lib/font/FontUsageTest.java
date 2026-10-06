package dev.joid.lib.font;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class FontUsageTest {

	@Test
	public void hasNoOriginOutsideAUsage() {
		Assert.assertFalse(FontUsage.getOrigin().isPresent());
	}

	@Test
	public void returnsTheMeasureOfItsUsage() {
		Assert.assertEquals(12.5D, FontUsage.trace(FontUsageTest.origin("Screen"), () -> 12.5D), 0D);
		Assert.assertEquals(7D, FontUsage.trace(null, () -> 7D), 0D);
	}

	@Test
	public void restoresTheOuterOriginAfterANestedUsage() {
		final StackTraceElement[] outer = FontUsageTest.origin("Screen");
		final StackTraceElement[] inner = FontUsageTest.origin("Popup");
		final List<StackTraceElement[]> seen = new ArrayList<>();
		FontUsage.trace(outer, () -> {
			FontUsage.trace(inner, () -> FontUsageTest.see(seen));
			return FontUsageTest.see(seen);
		});
		Assert.assertSame(inner, seen.get(0));
		Assert.assertSame(outer, seen.get(1));
		Assert.assertFalse(FontUsage.getOrigin().isPresent());
	}

	@Test
	public void keepsTheOuterOriginForAnUntracedUsage() {
		final StackTraceElement[] outer = FontUsageTest.origin("Screen");
		final List<StackTraceElement[]> seen = new ArrayList<>();
		FontUsage.trace(outer, () -> FontUsage.trace(null, () -> FontUsageTest.see(seen)));
		Assert.assertSame(outer, seen.get(0));
	}

	@Test
	public void restoresTheOriginWhenItsUsageFails() {
		try {
			FontUsage.trace(FontUsageTest.origin("Screen"), () -> {
				throw new IllegalStateException("measure failed");
			});
			Assert.fail("The failure of the usage must reach the caller");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("measure failed", expected.getMessage());
			Assert.assertFalse(FontUsage.getOrigin().isPresent());
		}
	}

	private static StackTraceElement[] origin(final String className) {
		return new StackTraceElement[] {new StackTraceElement(className, "init", className + ".java", 42)};
	}

	private static double see(final List<StackTraceElement[]> seen) {
		seen.add(FontUsage.getOrigin().orElse(null));
		return 0D;
	}

}