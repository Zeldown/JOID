package dev.joid.lib.font;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class IFontProviderTest {

	@Test
	public void drawsARunAsAPlainText() {
		final List<String> drawn = new ArrayList<>();
		final IFontProvider provider = new IFontProvider() {

			@Override
			public FontBounds drawText(final double x, final double y, final String text, final TextInfo info) {
				drawn.add(text + "@" + x + "," + y);
				return new FontBounds(12D, 34D);
			}

			@Override
			public double getWidth(final String text, final TextInfo info) {
				return 0D;
			}

			@Override
			public double getHeight(final String text, final TextInfo info) {
				return 0D;
			}

			@Override
			public double getLineHeight(final TextInfo info) {
				return 0D;
			}

		};

		final FontBounds bounds = provider.drawText(1D, 2D, "run", TextInfo.create(() -> provider, 10F), 0D, 0D, 100D, 100D);
		Assert.assertEquals(12D, bounds.getWidth(), 0D);
		Assert.assertEquals(34D, bounds.getHeight(), 0D);
		Assert.assertEquals(1, drawn.size());
		Assert.assertEquals("run@1.0,2.0", drawn.get(0));
	}

}