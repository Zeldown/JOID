package dev.joid.lib.font.impl.msdf;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.color.Color;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.font.impl.glyph.GlyphFontProvider;
import dev.joid.lib.font.impl.glyph.dto.TextGlyph;
import dev.joid.lib.font.impl.msdf.dto.MsdfAtlas;
import dev.joid.lib.font.impl.msdf.dto.MsdfBounds;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.dto.MsdfGlyph;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFontProvider extends GlyphFontProvider<MsdfFontFace> {

	private static final MsdfFontProvider INSTANCE = new MsdfFontProvider();

	private Color        color;
	private double       runX;
	private double       runY;
	private float        pixelX;
	private float        pixelY;
	private PixelGrid    grid;
	private double       runWidth;
	private double       runHeight;
	private MsdfFontFace face;

	public static @NonNull MsdfFontProvider inst() {
		return MsdfFontProvider.INSTANCE;
	}

	@Override
	protected void end() {
		MsdfShader.SHADER.unbind();
	}

	@Override
	protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {
		if (!MsdfShader.SHADER.isActive()) {
			throw new IllegalStateException("The msdf font shader is not usable");
		}

		this.face = null;
		this.color = null;
		this.pixelX = 0F;
		this.runX = runX;
		this.runY = runY;
		this.runWidth = runWidth;
		this.runHeight = runHeight;
		this.grid = BridgeHandler.RENDER.get().getPixelGrid();

		Color.reset();
		MsdfShader.SHADER.bind();
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

		if (glyph.getColor() != this.color) {
			this.bindColor(glyph.getColor());
		}

		final MsdfAtlas atlas = face.getAtlas();
		final MsdfBounds plane = msdf.getPlaneBounds();
		final MsdfBounds bounds = msdf.getAtlasBounds();
		final double size = glyph.getSize();
		final double height = this.getVerticalSize(face, size);
		final float pixelX = (float) (atlas.getSize() / (atlas.getWidth() * size * this.grid.getScaleX()));
		final float pixelY = (float) (atlas.getSize() / (atlas.getHeight() * height * this.grid.getScaleY()));
		if (pixelX != this.pixelX || pixelY != this.pixelY) {
			this.bindPixel(pixelX, pixelY);
		}

		final double inset = 0.5D / atlas.getSize();
		final double baseline = this.grid.snapY(glyph.getBaseline()) + glyph.getOffsetY();
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

	private void bindColor(final @NonNull Color color) {
		this.color = color;
		final Color current = color.update();
		MsdfShader.SHADER.uniform("color", current.r, current.g, current.b, current.a);
		if (!current.isGradient()) {
			MsdfShader.SHADER.uniform("u_HasGradient", 0);
			return;
		}

		final ColorGradient gradient = current.gradient;
		MsdfShader.SHADER
		.uniform("u_HasGradient", 1)
		.uniform("u_GradientStart", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
		.uniform("u_GradientEnd", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
		.uniform("u_GradientStartPos", gradient.getDirection().x, gradient.getDirection().y)
		.uniform("u_GradientEndPos", gradient.getDirection().z, gradient.getDirection().w)
		.uniform("u_GradientCanvas", (float) this.runX, (float) this.runY, (float) (this.runX + this.runWidth), (float) (this.runY + this.runHeight));
	}

	private void bindFace(final @NonNull MsdfFontFace face) {
		this.face = face;
		face.getTexture().bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		MsdfShader.SHADER
		.uniform("texel", 1F / face.getAtlas().getWidth(), 1F / face.getAtlas().getHeight())
		.uniform("pxRange", face.getAtlas().getDistanceRange());
	}

	private void bindPixel(final float pixelX, final float pixelY) {
		this.pixelX = pixelX;
		this.pixelY = pixelY;
		MsdfShader.SHADER.uniform("pixel", pixelX, pixelY);
	}

	private double getVerticalSize(final @NonNull MsdfFontFace face, final double size) {
		final double xHeight = face.getXHeight() * size * this.grid.getScaleY();
		return this.grid.isAligned() && xHeight > 0D ? size * Math.max(1D, Math.round(xHeight)) / xHeight : size;
	}

	private static final class MsdfShader {

		private static final IShader SHADER = BridgeHandler.RENDER.get().createShader(ShaderSource.read(ShaderStage.VERTEX, JOID.class.getResourceAsStream("/assets/shaders/font/font.vsh")), ShaderSource.read(ShaderStage.FRAGMENT, JOID.class.getResourceAsStream("/assets/shaders/font/font.fsh")), BlendState.NORMAL);

	}

}