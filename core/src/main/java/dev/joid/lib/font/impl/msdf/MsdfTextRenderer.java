package dev.joid.lib.font.impl.msdf;

import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.font.impl.glyph.GlyphTextRenderer;
import dev.joid.lib.font.impl.glyph.TextGlyph;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.NonNull;

public final class MsdfTextRenderer extends GlyphTextRenderer<MsdfFontFace> {

	private static final MsdfTextRenderer INSTANCE = new MsdfTextRenderer();

	private MsdfFontFace face;

	private MsdfTextRenderer() {
		super(CoreShader.FONT);
	}

	public static @NonNull MsdfTextRenderer inst() {
		return MsdfTextRenderer.INSTANCE;
	}

	@Override
	protected void begin() {
		super.begin();
		this.face = null;
	}

	@Override
	protected void drawGlyph(final @NonNull TextGlyph<MsdfFontFace> glyph) {
		final MsdfFontFace face = glyph.getFace();
		final MsdfGlyph msdf = face.getGlyph(glyph.getCodepoint());
		if (msdf == null || msdf.getPlaneBounds() == null || msdf.getAtlasBounds() == null) {
			return;
		}

		if (face != this.face) {
			this.bindFace(face);
		}

		super.bindColor(glyph.getColor());

		final PixelGrid grid = super.getGrid();
		final MsdfAtlas atlas = face.getAtlas();
		final MsdfBounds plane = msdf.getPlaneBounds();
		final MsdfBounds bounds = msdf.getAtlasBounds();
		final double size = glyph.getSize();
		final double height = this.getVerticalSize(face, size);
		final float pixelX = (float) (atlas.getSize() / (atlas.getWidth() * size * grid.getScaleX()));
		final float pixelY = (float) (atlas.getSize() / (atlas.getHeight() * height * grid.getScaleY()));
		super.bindPixel(pixelX, pixelY);

		final double inset = 0.5D / atlas.getSize();
		final double baseline = grid.snapY(glyph.getBaseline()) + glyph.getOffsetY();
		final double origin = glyph.getX() + glyph.getOffsetX();

		final double left = origin + (plane.getLeft() + inset) * size;
		final double right = origin + (plane.getRight() - inset) * size;
		final double top = baseline - (plane.getTop() - inset) * height;
		final double bottom = baseline - (plane.getBottom() + inset) * height;
		final double topSlant = glyph.isSlanted() ? (baseline - top) * 0.2D : 0D;
		final double bottomSlant = glyph.isSlanted() ? (baseline - bottom) * 0.2D : 0D;

		final double textureLeft = (bounds.getLeft() + 0.5D) / atlas.getWidth();
		final double textureRight = (bounds.getRight() - 0.5D) / atlas.getWidth();
		final double textureTop = 1D - (bounds.getTop() - 0.5D) / atlas.getHeight();
		final double textureBottom = 1D - (bounds.getBottom() + 0.5D) / atlas.getHeight();

		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.QUADS);
		tessellator.addVertexWithUV(left + bottomSlant, bottom, 0D, textureLeft, textureBottom);
		tessellator.addVertexWithUV(right + bottomSlant, bottom, 0D, textureRight, textureBottom);
		tessellator.addVertexWithUV(right + topSlant, top, 0D, textureRight, textureTop);
		tessellator.addVertexWithUV(left + topSlant, top, 0D, textureLeft, textureTop);
		tessellator.draw();
	}

	private void bindFace(final @NonNull MsdfFontFace face) {
		this.face = face;
		face.getTexture().bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		super.getShader()
		.uniform("texel", 1F / face.getAtlas().getWidth(), 1F / face.getAtlas().getHeight())
		.uniform("pxRange", face.getAtlas().getDistanceRange());
	}

	private double getVerticalSize(final @NonNull MsdfFontFace face, final double size) {
		final PixelGrid grid = super.getGrid();
		final double xHeight = face.getXHeight() * size * grid.getScaleY();
		return grid.isAligned() && xHeight > 0D ? size * Math.max(1D, Math.round(xHeight)) / xHeight : size;
	}

}