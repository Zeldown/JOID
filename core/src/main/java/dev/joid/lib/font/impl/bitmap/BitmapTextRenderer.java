package dev.joid.lib.font.impl.bitmap;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.font.impl.glyph.GlyphFont;
import dev.joid.lib.font.impl.glyph.GlyphTextRenderer;
import dev.joid.lib.font.impl.glyph.IFontFace;
import dev.joid.lib.font.impl.glyph.TextGlyph;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.NonNull;

public abstract class BitmapTextRenderer<F extends IFontFace> extends GlyphTextRenderer<F> {

	private ITexture texture;
	private boolean  grayscale;

	protected BitmapTextRenderer() {
		super(CoreShader.BITMAP);
	}

	@Override
	protected final void begin() {
		super.begin();
		this.texture = null;
		this.grayscale = false;
		super.getShader().uniform("grayscale", false);
	}

	@Override
	protected final void drawGlyph(final @NonNull TextGlyph<F> glyph) {
		final BitmapCell cell = this.getCell(glyph);
		if (cell == null) {
			return;
		}

		if (cell.getTexture() != this.texture) {
			this.bindTexture(cell.getTexture());
		}

		if (cell.isGrayscale() != this.grayscale) {
			this.bindGrayscale(cell.isGrayscale());
		}

		super.bindColor(glyph.getColor());

		final PixelGrid grid = super.getGrid();
		final double unit = glyph.getSize() / BitmapTextRenderer.getBitmapSize(glyph);
		final double width = cell.getWidth() * unit;
		final double height = cell.getHeight() * unit;
		final float pixelX = (float) (cell.getTexelWidth() / (width * grid.getScaleX()));
		final float pixelY = (float) (cell.getTexelHeight() / (height * grid.getScaleY()));
		super.bindPixel(pixelX, pixelY);

		super.getShader().uniform("bounds", (float) cell.getTexelLeft(), (float) cell.getTexelTop(), (float) cell.getTexelRight(), (float) cell.getTexelBottom());

		final ITexture texture = cell.getTexture();
		final double marginX = 1D / grid.getScaleX();
		final double marginY = 1D / grid.getScaleY();
		final double baseline = glyph.getBaseline() + glyph.getOffsetY();
		final double left = glyph.getX() + glyph.getOffsetX() + cell.getLeft() * unit - marginX;
		final double top = baseline - glyph.getAscender() + cell.getTop() * unit - marginY;
		final double right = left + width + 2D * marginX;
		final double bottom = top + height + 2D * marginY;
		final double topSlant = glyph.isSlanted() ? (baseline - top) * 0.2D : 0D;
		final double bottomSlant = glyph.isSlanted() ? (baseline - bottom) * 0.2D : 0D;
		final double textureLeft = (cell.getTexelLeft() - pixelX) / texture.getWidth();
		final double textureTop = (cell.getTexelTop() - pixelY) / texture.getHeight();
		final double textureRight = (cell.getTexelRight() + pixelX) / texture.getWidth();
		final double textureBottom = (cell.getTexelBottom() + pixelY) / texture.getHeight();
		final int strokes = cell.isBold() ? 2 : 1;

		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.QUADS);
		for (int stroke = 0; stroke < strokes; stroke++) {
			final double shift = stroke * unit;
			tessellator.addVertexWithUV(left + shift + bottomSlant, bottom, 0D, textureLeft, textureBottom);
			tessellator.addVertexWithUV(right + shift + bottomSlant, bottom, 0D, textureRight, textureBottom);
			tessellator.addVertexWithUV(right + shift + topSlant, top, 0D, textureRight, textureTop);
			tessellator.addVertexWithUV(left + shift + topSlant, top, 0D, textureLeft, textureTop);
		}
		tessellator.draw();
	}

	@Override
	protected final boolean isGridAligned() {
		return true;
	}

	protected abstract BitmapCell getCell(final @NonNull TextGlyph<F> glyph);

	private void bindGrayscale(final boolean grayscale) {
		this.grayscale = grayscale;
		super.getShader().uniform("grayscale", grayscale);
	}

	private void bindTexture(final @NonNull ITexture texture) {
		this.texture = texture;
		BridgeHandler.RENDER.get().getState().texture(texture).textureFilter(TextureFilter.NEAREST).textureWrap(TextureWrap.CLAMP_TO_EDGE);
		super.getShader().uniform("texel", 1F / texture.getWidth(), 1F / texture.getHeight());
	}

	private static int getBitmapSize(final @NonNull TextGlyph<?> glyph) {
		final GlyphFont<?> font = glyph.getFont();
		if (!(font instanceof BitmapFont)) {
			throw new IllegalStateException("The font " + font.getClass().getName() + " is drawn by a BitmapTextRenderer but is not a BitmapFont, draw it with its own font.getTextRenderer()");
		}

		return ((BitmapFont<?>) font).getBitmapSize();
	}

}