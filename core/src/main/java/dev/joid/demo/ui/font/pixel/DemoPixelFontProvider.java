package dev.joid.demo.ui.font.pixel;

import javax.vecmath.Vector4f;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.TextureWrap;
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

	private Vector4f          canvas;
	private DemoPixelFontFace face;

	public static @NonNull DemoPixelFontProvider inst() {
		return DemoPixelFontProvider.INSTANCE;
	}

	@Override
	protected void end() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		try {
			render.resetTexture();
		} finally {
			render.popState();
		}
	}

	@Override
	protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		this.face = null;
		this.canvas = new Vector4f((float) runX, (float) runY, (float) (runX + runWidth), (float) (runY + runHeight));

		render.pushState();
		render.blend(BlendState.NORMAL);
	}

	@Override
	protected void drawGlyph(final @NonNull TextGlyph<DemoPixelFontFace> glyph) {
		final int codepoint = glyph.getCodepoint();
		if (codepoint == ' ' || !glyph.hasGlyph(codepoint)) {
			return;
		}

		final DemoPixelFontFace face = glyph.getFace();
		if (face != this.face) {
			this.face = face;
			face.getTexture().bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		}

		final Resource texture = face.getTexture();
		final double left = glyph.getX() + glyph.getOffsetX();
		final double top = glyph.getBaseline() + glyph.getOffsetY() - face.getAscender() * glyph.getSize();
		final double right = left + glyph.getSize();
		final double bottom = top + glyph.getSize();
		final double textureLeft = (double) DemoPixelFontFace.cellX(codepoint) / texture.getWidth();
		final double textureTop = (double) DemoPixelFontFace.cellY(codepoint) / texture.getHeight();
		final double textureRight = (double) (DemoPixelFontFace.cellX(codepoint) + DemoPixelFont.SIZE) / texture.getWidth();
		final double textureBottom = (double) (DemoPixelFontFace.cellY(codepoint) + DemoPixelFont.SIZE) / texture.getHeight();
		glyph.getColor().bind(() -> {
			final Tessellator tessellator = Tessellator.inst();
			tessellator.start(DrawMode.QUADS);
			tessellator.addVertexWithUV(left, bottom, 0D, textureLeft, textureBottom);
			tessellator.addVertexWithUV(right, bottom, 0D, textureRight, textureBottom);
			tessellator.addVertexWithUV(right, top, 0D, textureRight, textureTop);
			tessellator.addVertexWithUV(left, top, 0D, textureLeft, textureTop);
			tessellator.draw();
		}, this.canvas, true);
	}

}