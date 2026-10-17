package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.color.Color;
import lombok.NonNull;

public final class BorderShader extends ShaderProgram {

	private static final BorderShader INSTANCE = new BorderShader();

	private BorderShader() {
		super.load(CoreShader.BORDER.open(ShaderStage.VERTEX), CoreShader.BORDER.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull BorderShader inst() {
		return BorderShader.INSTANCE;
	}

	public void bind(final float borderWidth, final @NonNull Color borderColor, final float texelW, final float texelH, final boolean fill, final @NonNull BorderMode mode, final float rectX1, final float rectY1, final float rectX2, final float rectY2) {
		super.bind();

		final Color color = borderColor.update();
		super.getShader()
		.uniform("u_BorderWidth", borderWidth)
		.uniform("u_BorderColor", color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF())
		.uniform("u_TexelSize", texelW, texelH)
		.uniform("u_Fill", fill ? 1 : 0)
		.uniform("u_Mode", mode.ordinal())
		.uniform("u_Rect", rectX1, rectY1, rectX2, rectY2);

		if (color.isGradient()) {
			ShaderProgram.uniformGradient(super.getShader(), color.getGradient(), rectX1, rectY1, rectX2, rectY2);
		} else {
			super.getShader().uniform("u_HasGradient", 0);
		}
	}

	public enum BorderMode {

		OUT,
		IN;

	}

}