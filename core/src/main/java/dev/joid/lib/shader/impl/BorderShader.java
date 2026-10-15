package dev.joid.lib.shader.impl;

import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.color.Color;
import lombok.NonNull;

public final class BorderShader extends ShaderProgram {

	private static final BorderShader INSTANCE = new BorderShader();

	private BorderShader() {
		this.load(CoreShader.BORDER.open(ShaderStage.VERTEX), CoreShader.BORDER.open(ShaderStage.FRAGMENT));
	}

	public static @NonNull BorderShader inst() {
		return BorderShader.INSTANCE;
	}

	public void bind(final float borderWidth, final @NonNull Color borderColor, final float texelW, final float texelH, final boolean fill, final int mode, final float rectX1, final float rectY1, final float rectX2, final float rectY2) {
		this.bind();

		this.getShader()
		.uniform("u_BorderWidth", borderWidth)
		.uniform("u_BorderColor", borderColor.getRedF(), borderColor.getGreenF(), borderColor.getBlueF(), borderColor.getAlphaF())
		.uniform("u_TexelSize", texelW, texelH)
		.uniform("u_Fill", fill ? 1 : 0)
		.uniform("u_Mode", mode)
		.uniform("u_Rect", rectX1, rectY1, rectX2, rectY2);

		if (borderColor.isGradient()) {
			ShaderProgram.uniformGradient(this.getShader(), borderColor.getGradient(), rectX1, rectY1, rectX2, rectY2);
		} else {
			this.getShader().uniform("u_HasGradient", 0);
		}
	}

	public enum BorderMode {

		OUT,
		IN;

	}

}