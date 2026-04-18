package be.zeldown.joid.lib.shader.impl;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import lombok.NonNull;

public class BorderShader extends GLShaderImpl {

	private static final BorderShader INSTANCE = new BorderShader();

	private BorderShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/border/border.vsh"), JOID.class.getResourceAsStream("/assets/shaders/border/border.fsh"));
	}

	public void bind(final float borderWidth, final @NonNull Color borderColor, final float texelW, final float texelH, final boolean fill, final int mode, final float rectX1, final float rectY1, final float rectX2, final float rectY2) {
		BorderShader.INSTANCE.bind();

		final FloatUniform widthUniform = BorderShader.INSTANCE.shader.getFloatUniform("u_BorderWidth");
		widthUniform.setValue(borderWidth);

		final Float4Uniform colorUniform = BorderShader.INSTANCE.shader.getFloat4Uniform("u_BorderColor");
		colorUniform.setValue(borderColor.r, borderColor.g, borderColor.b, borderColor.a);

		final Float2Uniform texelUniform = BorderShader.INSTANCE.shader.getFloat2Uniform("u_TexelSize");
		texelUniform.setValue(texelW, texelH);

		final IntUniform fillUniform = BorderShader.INSTANCE.shader.getIntUniform("u_Fill");
		fillUniform.setValue(fill ? 1 : 0);

		final IntUniform modeUniform = BorderShader.INSTANCE.shader.getIntUniform("u_Mode");
		modeUniform.setValue(mode);

		final Float4Uniform rectUniform = BorderShader.INSTANCE.shader.getFloat4Uniform("u_Rect");
		rectUniform.setValue(rectX1, rectY1, rectX2, rectY2);

		if (borderColor.isGradient()) {
			final ColorGradient gradient = borderColor.gradient;

			final IntUniform hasGradientUniform = BorderShader.INSTANCE.shader.getIntUniform("u_HasGradient");
			hasGradientUniform.setValue(1);

			final Float4Uniform startUniform = BorderShader.INSTANCE.shader.getFloat4Uniform("u_GradientStart");
			startUniform.setValue(gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a);

			final Float4Uniform endUniform = BorderShader.INSTANCE.shader.getFloat4Uniform("u_GradientEnd");
			endUniform.setValue(gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a);

			final Float2Uniform startPosUniform = BorderShader.INSTANCE.shader.getFloat2Uniform("u_GradientStartPos");
			startPosUniform.setValue(gradient.getDirection().x, gradient.getDirection().y);

			final Float2Uniform endPosUniform = BorderShader.INSTANCE.shader.getFloat2Uniform("u_GradientEndPos");
			endPosUniform.setValue(gradient.getDirection().z, gradient.getDirection().w);

			final Float4Uniform canvasUniform = BorderShader.INSTANCE.shader.getFloat4Uniform("u_GradientCanvas");
			canvasUniform.setValue(rectX1, rectY1, rectX2, rectY2);
		} else {
			final IntUniform hasGradientUniform = BorderShader.INSTANCE.shader.getIntUniform("u_HasGradient");
			hasGradientUniform.setValue(0);
		}
	}

	public static @NonNull BorderShader inst() {
		return BorderShader.INSTANCE;
	}

	public enum BorderMode {

		OUT,
		IN;

	}

}