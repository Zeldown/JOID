package dev.joid.lib.font.impl.bitmap;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.glyph.FontFamily;
import dev.joid.lib.font.impl.glyph.GlyphFont;
import dev.joid.lib.font.impl.glyph.IFontFace;
import dev.joid.lib.font.impl.glyph.TextGlyph;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public class BitmapTextRendererTest {

	private static final Face FACE = new Face();

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@Test
	public void drawsEveryAtlasAtTheSameSize() {
		final double[] scales = {1D, 1.2676D, 0.75D, 2D};
		for (final double scale : scales) {
			final List<double[]> quads = new ArrayList<>();
			for (final Atlas atlas : Atlas.values()) {
				final Capture capture = this.draw(atlas, scale, "A", 16F);
				quads.add(new double[] {capture.getLeft(), capture.getTop(), capture.getRight(), capture.getBottom()});
			}
			for (int i = 0; i < quads.size(); i++) {
				final String label = Atlas.values()[i] + " at " + scale;
				Assert.assertArrayEquals(label, quads.get(0), quads.get(i), 1E-3D);
				Assert.assertEquals(label, 16D * scale + 2D, (quads.get(i)[2] - quads.get(i)[0]) * scale, 1E-3D);
				Assert.assertEquals(label, 16D * scale + 2D, (quads.get(i)[3] - quads.get(i)[1]) * scale, 1E-3D);
			}
		}
	}

	@Test
	public void computesThePixelFromTheCellAndItsQuad() {
		final double[] scales = {1D, 1.2676D, 0.75D, 0.25D};
		for (final double scale : scales) {
			for (final Atlas atlas : Atlas.values()) {
				final float pixel = (float) (atlas.texels / (16D * scale));
				Assert.assertArrayEquals(atlas + " at " + scale, new float[] {pixel, pixel}, (float[]) this.draw(atlas, scale, "A", 16F).getUniforms().get("pixel"), 1E-6F);
			}
		}
	}

	@Test
	public void boundsTheSamplingToTheCellOfTheGlyph() {
		for (final Atlas atlas : Atlas.values()) {
			final Capture capture = this.draw(atlas, 1D, "B", 16F);
			Assert.assertArrayEquals(atlas.name(), new float[] {atlas.texels, 0F, 2F * atlas.texels, atlas.texels}, (float[]) capture.getUniforms().get("bounds"), 0F);
			Assert.assertArrayEquals(atlas.name(), new float[] {1F / (3F * atlas.texels), 1F / atlas.texels}, (float[]) capture.getUniforms().get("texel"), 0F);
			Assert.assertEquals(atlas.name(), atlas.grayscale, capture.getUniforms().get("grayscale"));
		}
	}

	@Test
	public void extendsTheCellByOneScreenPixel() {
		for (final Atlas atlas : Atlas.values()) {
			final Capture capture = this.draw(atlas, 1.2676D, "B", 16F);
			final float pixel = ((float[]) capture.getUniforms().get("pixel"))[0];
			float left = Float.MAX_VALUE;
			float right = -Float.MAX_VALUE;
			for (int i = 0; i < capture.getCount(); i++) {
				left = Math.min(left, capture.getU(i));
				right = Math.max(right, capture.getU(i));
			}
			Assert.assertEquals(atlas.name(), (atlas.texels - pixel) / (3F * atlas.texels), left, 1E-6F);
			Assert.assertEquals(atlas.name(), (2F * atlas.texels + pixel) / (3F * atlas.texels), right, 1E-6F);
		}
	}

	@Test
	public void drawsEveryTexelOnWholePixelsAtAWholeScale() {
		final double[] scales = {1D, 2D, 0.5D};
		for (final double scale : scales) {
			for (final Atlas atlas : Atlas.values()) {
				final Capture capture = this.draw(atlas, scale, "A", 16F);
				final String label = atlas + " at " + scale;
				final double texels = atlas.texels / ((float[]) capture.getUniforms().get("pixel"))[0];
				Assert.assertEquals(label, Math.rint(capture.getLeft() * scale), capture.getLeft() * scale, 1E-3D);
				Assert.assertEquals(label, Math.rint(capture.getTop() * scale), capture.getTop() * scale, 1E-3D);
				Assert.assertEquals(label, 2D * scale * 8D, texels, 1E-3D);
				Assert.assertEquals(label, Math.rint(texels / 8D), texels / 8D, 1E-3D);
			}
		}
	}

	@Test
	public void drawsABoldCellTwiceOneFontPixelApart() {
		final Capture regular = this.draw(Atlas.ASCII, 1.2676D, "A", 16F);
		final Capture bold = this.draw(Atlas.ASCII, 1.2676D, "a", 16F);
		Assert.assertEquals(2 * regular.getCount(), bold.getCount());
		Assert.assertEquals(regular.getLeft(), bold.getLeft(), 1E-3D);
		Assert.assertEquals(regular.getRight() + 2D, bold.getRight(), 1E-3D);
		Assert.assertEquals(regular.getTop(), bold.getTop(), 1E-3D);
	}

	@Test
	public void slantsAnUprightGlyphAskedInItalic() {
		final Capture upright = this.draw(Atlas.ASCII, 1D, "A", 16F);
		final Capture slanted = this.draw(Atlas.ASCII, 1D, "A", 16F, true);
		Assert.assertEquals(upright.getTop(), slanted.getTop(), 1E-3D);
		Assert.assertEquals(upright.getBottom(), slanted.getBottom(), 1E-3D);
		Assert.assertNotEquals(upright.getLeft(), slanted.getLeft(), 1E-3D);
		Assert.assertEquals(upright.getRight() - upright.getLeft() + (slanted.getBottom() - slanted.getTop()) * 0.2D, slanted.getRight() - slanted.getLeft(), 1E-3D);
	}

	@Test
	public void startsEachLineOnThePixelGrid() {
		for (final Atlas atlas : Atlas.values()) {
			final Font font = new Font(new Renderer(atlas, this.texture(atlas)));
			this.render.getCaptures().clear();
			this.render.pushMatrix();
			try {
				this.render.scale(1.2676D, 1.2676D, 1D);
				font.getTextRenderer().drawText(100.3D, 100.6D, "AB", TextInfo.create(font, 16F, Color.WHITE));
			} finally {
				this.render.popMatrix();
			}
			final Capture first = this.render.getCaptures().get(0);
			Assert.assertEquals(atlas.name(), Math.rint(first.getLeft() * 1.2676D), first.getLeft() * 1.2676D, 1E-3D);
			Assert.assertEquals(atlas.name(), 12D, this.render.getCaptures().get(1).getLeft() - first.getLeft(), 1E-3D);
		}
	}

	@Test
	public void drawsNothingForAGlyphWithoutCell() {
		this.draw(Atlas.ASCII, 1D, "A", 16F);
		final int count = this.render.getCaptures().size();
		this.draw(Atlas.ASCII, 1D, "A A", 16F);
		Assert.assertEquals(count + 2, this.render.getCaptures().size());
	}

	@Test
	public void bindsTheAtlasOfTheCell() {
		final ITexture texture = this.texture(Atlas.HD);
		final Font font = new Font(new Renderer(Atlas.HD, texture));
		font.getTextRenderer().drawText(0D, 0D, "A", TextInfo.create(font, 16F, Color.WHITE));
		Assert.assertSame(texture, this.render.getLast().getState().getTexture());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAFontThatIsNotABitmapFont() {
		final Renderer renderer = new Renderer(Atlas.ASCII, this.texture(Atlas.ASCII));
		final GlyphFont<Face> font = new GlyphFont<Face>(FontFamily.of(BitmapTextRendererTest.FACE)) {

			@Override
			public @NonNull ITextRenderer getTextRenderer() {
				return renderer;
			}

		};
		renderer.drawText(0D, 0D, "A", TextInfo.create(font, 16F, Color.WHITE));
	}

	@Test(expected = IllegalStateException.class)
	public void refusesAnUnusableShader() {
		final Font font = new Font(new Renderer(Atlas.ASCII, this.texture(Atlas.ASCII)));
		font.getTextRenderer().drawText(0D, 0D, "A", TextInfo.create(font, 16F, Color.WHITE));
		((RecordingShader) this.render.getLast().getShader()).setActive(false);
		font.getTextRenderer().drawText(0D, 0D, "A", TextInfo.create(font, 16F, Color.WHITE));
	}

	private Capture draw(final Atlas atlas, final double scale, final String text, final float size) {
		return this.draw(atlas, scale, text, size, false);
	}

	private Capture draw(final Atlas atlas, final double scale, final String text, final float size, final boolean italic) {
		final Font font = new Font(new Renderer(atlas, this.texture(atlas)));
		this.render.pushMatrix();
		try {
			this.render.scale(scale, scale, 1D);
			font.getTextRenderer().drawText(100D, 100D, text, TextInfo.create(font, size, Color.WHITE).italic(italic));
		} finally {
			this.render.popMatrix();
		}
		return this.render.getLast();
	}

	private ITexture texture(final Atlas atlas) {
		return this.render.createTexture().allocate(3 * atlas.texels, atlas.texels);
	}

	@AllArgsConstructor
	private enum Atlas {

		ASCII(8, false),
		UNIFONT(16, false),
		HD(32, false),
		GRAYSCALE(8, true);

		private final int     texels;
		private final boolean grayscale;

	}

	private static final class Face implements IFontFace {

		@Override
		public boolean isItalic() {
			return false;
		}

		@Override
		public @NonNull String getName() {
			return "Bitmap";
		}

		@Override
		public @NonNull FontWeight getWeight() {
			return FontWeight.REGULAR;
		}

		@Override
		public float getAscender() {
			return 7F / 8F;
		}

		@Override
		public float getDescender() {
			return -2F / 8F;
		}

		@Override
		public float getLineHeight() {
			return 9F / 8F;
		}

		@Override
		public float getUnderlineY() {
			return -1F / 8F;
		}

		@Override
		public float getUnderlineThickness() {
			return 1F / 8F;
		}

		@Override
		public float getAdvance(final int codepoint) {
			return 6F / 8F;
		}

		@Override
		public float getKerning(final int previous, final int current) {
			return 0F;
		}

		@Override
		public boolean hasGlyph(final int codepoint) {
			return codepoint == 'A' || codepoint == 'B' || codepoint == 'a';
		}

	}

	private static final class Font extends BitmapFont<Face> {

		private final Renderer renderer;

		private Font(final Renderer renderer) {
			super(FontFamily.of(BitmapTextRendererTest.FACE), 8);
			this.renderer = renderer;
		}

		@Override
		public @NonNull ITextRenderer getTextRenderer() {
			return this.renderer;
		}

	}

	@AllArgsConstructor
	private static final class Renderer extends BitmapTextRenderer<Face> {

		private final Atlas    atlas;
		private final ITexture texture;

		@Override
		protected BitmapCell getCell(final @NonNull TextGlyph<Face> glyph) {
			if (!glyph.hasGlyph(glyph.getCodepoint())) {
				return null;
			}

			final int index = glyph.getCodepoint() == 'B' ? 1 : 0;
			final BitmapCell cell = BitmapCell.create(this.texture, index * this.atlas.texels, 0, (index + 1) * this.atlas.texels, this.atlas.texels).grayscale(this.atlas.grayscale).bold(glyph.getCodepoint() == 'a');
			return this.atlas.texels == 8 ? cell : cell.bounds(0D, 0D, 8D, 8D);
		}

	}

}