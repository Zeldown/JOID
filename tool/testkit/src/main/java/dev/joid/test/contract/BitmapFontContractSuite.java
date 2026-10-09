package dev.joid.test.contract;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.impl.bitmap.BitmapCell;
import dev.joid.lib.font.impl.bitmap.BitmapFont;
import dev.joid.lib.font.impl.bitmap.BitmapTextRenderer;
import dev.joid.lib.font.impl.glyph.FontFamily;
import dev.joid.lib.font.impl.glyph.IFontFace;
import dev.joid.lib.font.impl.glyph.TextGlyph;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.AllArgsConstructor;
import lombok.NonNull;

public abstract class BitmapFontContractSuite {

	private static final int WIDTH  = 128;
	private static final int HEIGHT = 64;

	private static ISnapshotBackend backend;

	protected abstract @NonNull ISnapshotBackend createBackend();

	@Before
	public void startBackend() {
		if (BitmapFontContractSuite.backend == null) {
			BitmapFontContractSuite.backend = this.createBackend();
			BitmapFontContractSuite.backend.create(BitmapFontContractSuite.WIDTH, BitmapFontContractSuite.HEIGHT);
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.loadIdentity();
		render.ortho(0D, BitmapFontContractSuite.WIDTH, BitmapFontContractSuite.HEIGHT, 0D, 0D, 10000D);
		render.viewport(0, 0, BitmapFontContractSuite.WIDTH, BitmapFontContractSuite.HEIGHT);
		render.frameBuffer(null);
		render.shader(null);
		render.resetTexture();
		render.blend(BlendState.NORMAL);
		render.depthTest(false);
		render.depthWrite(false);
		render.cull(false);
		render.color(1F, 1F, 1F, 1F);
		render.alphaCutoff(0F);
	}

	@AfterClass
	public static void stopBackend() {
		if (BitmapFontContractSuite.backend == null) {
			return;
		}

		BitmapFontContractSuite.backend.destroy();
		BitmapFontContractSuite.backend = null;
	}

	@Test
	public void drawsWholeTexelsAtAWholeNumberOfPixels() {
		final double[] scales = {1D, 2D, 0.5D};
		for (final double scale : scales) {
			for (final Atlas atlas : Atlas.values()) {
				final SnapshotImage image = BitmapFontContractSuite.render(atlas, scale);
				final String label = atlas + " at " + scale;
				final double texel = 2D * scale;
				int partial = 0;
				for (final int pixel : image.getPixels()) {
					final int green = pixel >> 8 & 255;
					if (green > 2 && green < 253) {
						partial++;
					}
				}
				Assert.assertEquals(label + ": partial pixels", 0, partial);
				Assert.assertEquals(label + ": ink", Atlas.INK * texel * texel, BitmapFontContractSuite.ink(image), 0.5D);
			}
		}
	}

	@Test
	public void givesEveryTexelColumnTheSameInkAtAFractionalScale() {
		final double[] scales = {1.2676D, 1.75D};
		for (final double scale : scales) {
			for (final Atlas atlas : Atlas.values()) {
				final SnapshotImage image = BitmapFontContractSuite.render(atlas, scale);
				final String label = atlas + " at " + scale;
				final double texel = 2D * scale;
				final List<Double> columns = BitmapFontContractSuite.columns(image);
				Assert.assertEquals(label + ": ink", Atlas.INK * texel * texel, BitmapFontContractSuite.ink(image), 0.01D * Atlas.INK * texel * texel);
				Assert.assertEquals(label + ": columns", 3, columns.size());
				for (final double column : columns) {
					Assert.assertEquals(label + ": column", texel, column, 0.03D);
				}
			}
		}
	}

	@Test
	public void drawsTheSameGlyphFromEveryAtlas() {
		final double[] scales = {1D, 1.2676D, 0.75D};
		for (final double scale : scales) {
			final SnapshotImage reference = BitmapFontContractSuite.render(Atlas.ASCII, scale);
			for (final Atlas atlas : Atlas.values()) {
				final SnapshotImage image = BitmapFontContractSuite.render(atlas, scale);
				int difference = 0;
				for (int i = 0; i < image.getPixels().length; i++) {
					difference = Math.max(difference, Math.abs((image.getPixels()[i] >> 8 & 255) - (reference.getPixels()[i] >> 8 & 255)));
				}
				Assert.assertTrue(atlas + " at " + scale + " differs from the ASCII atlas by " + difference, difference <= 2);
			}
		}
	}

	private static SnapshotImage render(final Atlas atlas, final double scale) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final ITexture texture = render.createTexture().allocate(2 * atlas.texels, atlas.texels).upload(atlas.pixels(), 2 * atlas.texels, atlas.texels);
		final Font font = new Font(new Renderer(atlas, texture));
		render.beginFrame();
		render.clearColor(0F, 0F, 0F, 1F);
		render.pushMatrix();
		try {
			render.scale(scale, scale, 1D);
			font.getTextRenderer().drawText(8.3D, 4.6D, "A", TextInfo.create(font, 16F, Color.WHITE));
		} finally {
			render.popMatrix();
		}
		render.endFrame();

		final SnapshotImage image = BitmapFontContractSuite.backend.capture(BitmapFontContractSuite.WIDTH, BitmapFontContractSuite.HEIGHT);
		BitmapFontContractSuite.backend.present();
		texture.delete();
		return image;
	}

	private static double ink(final SnapshotImage image) {
		double ink = 0D;
		for (final int pixel : image.getPixels()) {
			ink += (pixel >> 8 & 255) / 255D;
		}
		return ink;
	}

	private static List<Double> columns(final SnapshotImage image) {
		int top = Integer.MAX_VALUE;
		int bottom = -1;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getPixels()[x + y * image.getWidth()] >> 8 & 255) > 0) {
					top = Math.min(top, y);
					bottom = Math.max(bottom, y);
				}
			}
		}

		final int row = (top + bottom) / 2;
		final List<Double> columns = new ArrayList<>();
		double column = 0D;
		for (int x = 0; x < image.getWidth(); x++) {
			final double ink = (image.getPixels()[x + row * image.getWidth()] >> 8 & 255) / 255D;
			if (ink > 0D) {
				column += ink;
			} else if (column > 0D) {
				columns.add(column);
				column = 0D;
			}
		}
		return columns;
	}

	@AllArgsConstructor
	private enum Atlas {

		ASCII(8, false),
		UNIFONT(16, false),
		HD(32, false),
		GRAYSCALE(8, true);

		private static final int INK = 18;

		private final int     texels;
		private final boolean grayscale;

		private int[] pixels() {
			final int width = 2 * this.texels;
			final int[] pixels = new int[width * this.texels];
			for (int y = 0; y < this.texels; y++) {
				for (int x = 0; x < width; x++) {
					final int column = x * 8 / this.texels;
					final int row = y * 8 / this.texels;
					final boolean ink = column >= 8 || column % 2 == 1 && column < 7 && row >= 1 && row < 7;
					pixels[x + y * width] = this.grayscale ? (ink ? 0xFFFF0000 : 0xFF000000) : (ink ? 0xFFFFFFFF : 0);
				}
			}
			return pixels;
		}

	}

	private static final class Face implements IFontFace {

		@Override
		public boolean isItalic() {
			return false;
		}

		@Override
		public @NonNull String getName() {
			return "Contract";
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
			return 1F;
		}

		@Override
		public float getKerning(final int previous, final int current) {
			return 0F;
		}

		@Override
		public boolean hasGlyph(final int codepoint) {
			return codepoint == 'A';
		}

	}

	private static final class Font extends BitmapFont<Face> {

		private final Renderer renderer;

		private Font(final Renderer renderer) {
			super(FontFamily.of(new Face()), 8);
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
			final BitmapCell cell = BitmapCell.create(this.texture, 0, 0, this.atlas.texels, this.atlas.texels).grayscale(this.atlas.grayscale);
			return this.atlas.texels == 8 ? cell : cell.bounds(0D, 0D, 8D, 8D);
		}

	}

}