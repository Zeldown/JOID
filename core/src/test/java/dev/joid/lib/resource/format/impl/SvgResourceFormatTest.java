package dev.joid.lib.resource.format.impl;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class SvgResourceFormatTest {

	private final SvgResourceFormat format = new SvgResourceFormat();

	@Test
	public void recognizesAnSvgBehindItsProlog() {
		Assert.assertTrue(this.supports("<svg xmlns=\"http://www.w3.org/2000/svg\"/>"));
		Assert.assertTrue(this.supports("\uFEFF  <?xml version=\"1.0\"?>\n<!-- icon -->\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"x\">\n<svg>"));
		Assert.assertTrue(this.supports("<svg>"));
		Assert.assertTrue(this.supports("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"x\" [\n\t<!ENTITY ns_svg \"http://www.w3.org/2000/svg\">\n\t<!ENTITY ns_xlink \"http://www.w3.org/1999/xlink\">\n]>\n<svg>"));
	}

	@Test
	public void rejectsOtherDocuments() {
		Assert.assertFalse(this.supports("<svgx>"));
		Assert.assertFalse(this.supports("<html><svg/></html>"));
		Assert.assertFalse(this.supports("<?xml version=\"1.0\"?><root/>"));
		Assert.assertFalse(this.format.supports(new byte[] {(byte) 0x89, 'P', 'N', 'G'}));
		Assert.assertFalse(this.supports("<!-- never closed"));
	}

	private boolean supports(final String text) {
		return this.format.supports(text.getBytes(StandardCharsets.UTF_8));
	}

}