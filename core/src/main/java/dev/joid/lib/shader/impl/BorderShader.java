package dev.joid.lib.shader.impl;

import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.color.ColorGradient;
import lombok.NonNull;

public class BorderShader extends ShaderImpl {

	private static final BorderShader INSTANCE = new BorderShader();

	private BorderShader() {
		this.load(JOID.class.getResourceAsStream("/assets/shaders/border/border.vsh"), JOID.class.getResourceAsStream("/assets/shaders/border/border.fsh"));
	}

	public static @NonNull BorderShader inst() {
		return BorderShader.INSTANCE;
	}

	public void bind(final float borderWidth, final @NonNull Color borderColor, final float texelW, final float texelH, final boolean fill, final int mode, final float rectX1, final float rectY1, final float rectX2, final float rectY2) {
		BorderShader.INSTANCE.bind();

		BorderShader.INSTANCE.shader
		.uniform("u_BorderWidth", borderWidth)
		.uniform("u_BorderColor", borderColor.r, borderColor.g, borderColor.b, borderColor.a)
		.uniform("u_TexelSize", texelW, texelH)
		.uniform("u_Fill", fill ? 1 : 0)
		.uniform("u_Mode", mode)
		.uniform("u_Rect", rectX1, rectY1, rectX2, rectY2);

		if (borderColor.isGradient()) {
			final ColorGradient gradient = borderColor.gradient;

			BorderShader.INSTANCE.shader
			.uniform("u_HasGradient", 1)
			.uniform("u_GradientStart", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
			.uniform("u_GradientEnd", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
			.uniform("u_GradientStartPos", gradient.getDirection().x, gradient.getDirection().y)
			.uniform("u_GradientEndPos", gradient.getDirection().z, gradient.getDirection().w)
			.uniform("u_GradientCanvas", rectX1, rectY1, rectX2, rectY2);
		} else {
			BorderShader.INSTANCE.shader.uniform("u_HasGradient", 0);
		}
	}

	public enum BorderMode {

		OUT,
		IN;

	}

}