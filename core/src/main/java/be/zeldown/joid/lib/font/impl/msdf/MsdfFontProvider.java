package be.zeldown.joid.lib.font.impl.msdf;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.IntUniform;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.font.impl.glyph.GlyphFontProvider;
import be.zeldown.joid.lib.font.impl.glyph.dto.TextGlyph;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfAtlas;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfBounds;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfGlyph;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFontProvider extends GlyphFontProvider<MsdfFontFace> {

	private static final MsdfFontProvider INSTANCE = new MsdfFontProvider();

	private static final double SLANT         = 0.2D;
	private static final double HALF_TEXEL    = 0.5D;
	private static final double BASELINE_LIFT = 0.025D;

	private Color    color;
	private double   runX;
	private double   runY;
	private double   runWidth;
	private double   runHeight;
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
		this.runX = runX;
		this.runY = runY;
		this.runWidth = runWidth;
		this.runHeight = runHeight;

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
		final double inset = MsdfFontProvider.HALF_TEXEL / atlas.getSize();
		final double baseline = glyph.getBaseline() + glyph.getOffsetY();
		final double origin = glyph.getX() + glyph.getOffsetX();

		final double left = origin + (plane.getLeft() + inset) * size;
		final double right = origin + (plane.getRight() - inset) * size;
		final double top = baseline - (plane.getTop() - inset + MsdfFontProvider.BASELINE_LIFT) * size;
		final double bottom = baseline - (plane.getBottom() + inset + MsdfFontProvider.BASELINE_LIFT) * size;
		final double topSlant = glyph.isSlanted() ? (baseline - top) * MsdfFontProvider.SLANT : 0D;
		final double bottomSlant = glyph.isSlanted() ? (baseline - bottom) * MsdfFontProvider.SLANT : 0D;

		final double textureLeft = (bounds.getLeft() + MsdfFontProvider.HALF_TEXEL) / atlas.getWidth();
		final double textureRight = (bounds.getRight() - MsdfFontProvider.HALF_TEXEL) / atlas.getWidth();
		final double textureTop = 1D - (bounds.getTop() - MsdfFontProvider.HALF_TEXEL) / atlas.getHeight();
		final double textureBottom = 1D - (bounds.getBottom() + MsdfFontProvider.HALF_TEXEL) / atlas.getHeight();

		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.QUADS);
		tessellator.addVertexWithUV(left + bottomSlant, bottom, 0D, textureLeft, textureBottom);
		tessellator.addVertexWithUV(right + bottomSlant, bottom, 0D, textureRight, textureBottom);
		tessellator.addVertexWithUV(right + topSlant, top, 0D, textureRight, textureTop);
		tessellator.addVertexWithUV(left + topSlant, top, 0D, textureLeft, textureTop);
		tessellator.draw();
	}

	private void bindColor(final @NonNull Color color) {
		this.color = color.update();
		MsdfShader.COLOR.setValue(color.r, color.g, color.b, color.a);
		if (!color.isGradient()) {
			MsdfShader.HAS_GRADIENT.setValue(0);
			return;
		}

		final ColorGradient gradient = color.gradient;
		MsdfShader.HAS_GRADIENT.setValue(1);
		MsdfShader.GRADIENT_START.setValue(gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a);
		MsdfShader.GRADIENT_END.setValue(gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a);
		MsdfShader.GRADIENT_START_POS.setValue(gradient.getDirection().x, gradient.getDirection().y);
		MsdfShader.GRADIENT_END_POS.setValue(gradient.getDirection().z, gradient.getDirection().w);
		MsdfShader.GRADIENT_CANVAS.setValue((float) this.runX, (float) this.runY, (float) (this.runX + this.runWidth), (float) (this.runY + this.runHeight));
	}

	private void bindFace(final @NonNull MsdfFontFace face) {
		this.face = face;
		face.getTexture().bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);
		MsdfShader.TEXEL.setValue(1F / face.getAtlas().getWidth(), 1F / face.getAtlas().getHeight());
		MsdfShader.PX_RANGE.setValue(face.getAtlas().getDistanceRange());
	}

	private static final class MsdfShader {

		private static final IShader SHADER = BridgeHandler.RENDER.get().createShader(ShaderSource.read(ShaderStage.VERTEX, JOID.class.getResourceAsStream("/assets/shaders/font/font.vsh")), ShaderSource.read(ShaderStage.FRAGMENT, JOID.class.getResourceAsStream("/assets/shaders/font/font.fsh")), BlendState.NORMAL);

		private static final Float2Uniform TEXEL              = MsdfShader.SHADER.getFloat2Uniform("texel");
		private static final Float4Uniform COLOR              = MsdfShader.SHADER.getFloat4Uniform("color");
		private static final FloatUniform  PX_RANGE           = MsdfShader.SHADER.getFloatUniform("pxRange");
		private static final IntUniform    HAS_GRADIENT       = MsdfShader.SHADER.getIntUniform("u_HasGradient");
		private static final Float4Uniform GRADIENT_END       = MsdfShader.SHADER.getFloat4Uniform("u_GradientEnd");
		private static final Float4Uniform GRADIENT_START     = MsdfShader.SHADER.getFloat4Uniform("u_GradientStart");
		private static final Float4Uniform GRADIENT_CANVAS    = MsdfShader.SHADER.getFloat4Uniform("u_GradientCanvas");
		private static final Float2Uniform GRADIENT_END_POS   = MsdfShader.SHADER.getFloat2Uniform("u_GradientEndPos");
		private static final Float2Uniform GRADIENT_START_POS = MsdfShader.SHADER.getFloat2Uniform("u_GradientStartPos");

	}

}