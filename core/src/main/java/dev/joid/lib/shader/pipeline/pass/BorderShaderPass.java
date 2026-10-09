package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.color.Color;
import dev.joid.lib.shader.impl.BorderShader;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import lombok.NonNull;

public class BorderShaderPass implements IShaderPass {

	private final boolean fill;
	private final BorderMode mode;
	private final float borderWidth;
	private final Color borderColor;

	public BorderShaderPass(final float borderWidth, final @NonNull Color borderColor) {
		this(borderWidth, borderColor, true, BorderMode.OUT);
	}

	public BorderShaderPass(final float borderWidth, final @NonNull Color borderColor, final boolean fill) {
		this(borderWidth, borderColor, fill, BorderMode.OUT);
	}

	public BorderShaderPass(final float borderWidth, final @NonNull Color borderColor, final boolean fill, final @NonNull BorderMode mode) {
		this.borderWidth = borderWidth;
		this.borderColor = borderColor;
		this.fill = fill;
		this.mode = mode;
	}

	@Override
	public void unbind() {
		BorderShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 200;
	}

	@Override
	public float expansion() {
		return this.borderWidth + 2F;
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!BorderShader.inst().canDraw()) {
			return;
		}

		final float x = (float) context.getX();
		final float y = (float) context.getY();
		BorderShader.inst().bind((float) (this.borderWidth * context.getGrid().getScaleX()), this.borderColor, context.getTexelWidth(), context.getTexelHeight(), this.fill, this.mode.ordinal(), x, y, x + (float) context.getWidth(), y + (float) context.getHeight());
	}

}