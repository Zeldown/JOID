package dev.joid.demo.ui.font.pixel;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.color.Color;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.font.impl.glyph.GlyphFontProvider;
import dev.joid.lib.font.impl.glyph.dto.TextGlyph;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.Resource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoPixelFontProvider extends GlyphFontProvider<DemoPixelFontFace> {

	private static final DemoPixelFontProvider INSTANCE = new DemoPixelFontProvider();

	private Color             color;
	private double            runX;
	private double            runY;
	private float             pixelX;
	private float             pixelY;
	private PixelGrid         grid;
	private double            runWidth;
	private double            runHeight;
	private DemoPixelFontFace face;

	public static @NonNull DemoPixelFontProvider inst() {
		return DemoPixelFontProvider.INSTANCE;
	}

	@Override
	protected void end() {
		BitmapShader.SHADER.unbind();
	}

	@Override
	protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {
		if (!BitmapShader.SHADER.isActive()) {
			throw new IllegalStateException("The bitmap font shader is not usable");
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
		BitmapShader.SHADER.bind();
	}

	@Override
	protected void drawGlyph(final @NonNull TextGlyph<DemoPixelFontFace> glyph) {
		final int codepoint = glyph.getCodepoint();
		if (codepoint == ' ' || !glyph.hasGlyph(codepoint)) {
			return;
		}

		if (glyph.getFace() != this.face) {
			this.bindFace(glyph.getFace());
		}

		if (glyph.getColor() != this.color) {
			this.bindColor(glyph.getColor());
		}

		final double size = glyph.getSize();
		final float pixelX = (float) (DemoPixelFont.SIZE / (size * this.grid.getScaleX()));
		final float pixelY = (float) (DemoPixelFont.SIZE / (size * this.grid.getScaleY()));
		if (pixelX != this.pixelX || pixelY != this.pixelY) {
			this.bindPixel(pixelX, pixelY);
		}

		final int cellX = DemoPixelFontFace.cellX(codepoint);
		final int cellY = DemoPixelFontFace.cellY(codepoint);
		BitmapShader.SHADER.uniform("bounds", cellX, cellY, cellX + DemoPixelFont.SIZE, cellY + DemoPixelFont.SIZE);

		final Resource texture = this.face.getTexture();
		final double marginX = 1D / this.grid.getScaleX();
		final double marginY = 1D / this.grid.getScaleY();
		final double left = glyph.getX() + glyph.getOffsetX() - marginX;
		final double top = glyph.getBaseline() + glyph.getOffsetY() - this.face.getAscender() * size - marginY;
		final double right = left + size + 2D * marginX;
		final double bottom = top + size + 2D * marginY;
		final double textureLeft = (cellX - pixelX) / texture.getWidth();
		final double textureTop = (cellY - pixelY) / texture.getHeight();
		final double textureRight = (cellX + DemoPixelFont.SIZE + pixelX) / texture.getWidth();
		final double textureBottom = (cellY + DemoPixelFont.SIZE + pixelY) / texture.getHeight();

		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.QUADS);
		tessellator.addVertexWithUV(left, bottom, 0D, textureLeft, textureBottom);
		tessellator.addVertexWithUV(right, bottom, 0D, textureRight, textureBottom);
		tessellator.addVertexWithUV(right, top, 0D, textureRight, textureTop);
		tessellator.addVertexWithUV(left, top, 0D, textureLeft, textureTop);
		tessellator.draw();
	}

	private void bindColor(final @NonNull Color color) {
		this.color = color;
		final Color current = color.update();
		BitmapShader.SHADER.uniform("color", current.r, current.g, current.b, current.a);
		if (!current.isGradient()) {
			BitmapShader.SHADER.uniform("u_HasGradient", 0);
			return;
		}

		final ColorGradient gradient = current.gradient;
		BitmapShader.SHADER
		.uniform("u_HasGradient", 1)
		.uniform("u_GradientStart", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
		.uniform("u_GradientEnd", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
		.uniform("u_GradientStartPos", gradient.getDirection().x, gradient.getDirection().y)
		.uniform("u_GradientEndPos", gradient.getDirection().z, gradient.getDirection().w)
		.uniform("u_GradientCanvas", (float) this.runX, (float) this.runY, (float) (this.runX + this.runWidth), (float) (this.runY + this.runHeight));
	}

	private void bindFace(final @NonNull DemoPixelFontFace face) {
		this.face = face;
		face.getTexture().bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		BitmapShader.SHADER.uniform("texel", 1F / face.getTexture().getWidth(), 1F / face.getTexture().getHeight());
	}

	private void bindPixel(final float pixelX, final float pixelY) {
		this.pixelX = pixelX;
		this.pixelY = pixelY;
		BitmapShader.SHADER.uniform("pixel", pixelX, pixelY);
	}

	private static final class BitmapShader {

		private static final IShader SHADER = CoreShader.BITMAP.create(BlendState.NORMAL);

	}

}