package dev.joid.internal.font;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;

public class InternalFontTest {

	@BeforeClass
	public static void load() {
		InternalFont.load();
	}

	@Test
	public void loadsTheNineWeightsOfMontserrat() {
		for (final FontWeight weight : FontWeight.values()) {
			final MsdfFontFace face = InternalFont.MONTSERRAT.getFace(weight, false);
			Assert.assertSame(weight, face.getWeight());
			Assert.assertFalse(face.isItalic());
			Assert.assertTrue(face.getName(), face.getName().startsWith("Montserrat "));
		}
	}

	@Test
	public void namesEachFaceAfterItsWeight() {
		Assert.assertEquals("Montserrat Regular", InternalFont.MONTSERRAT.getFace(FontWeight.REGULAR, false).getName());
		Assert.assertEquals("Montserrat Black", InternalFont.MONTSERRAT.getFace(FontWeight.BLACK, false).getName());
	}

}