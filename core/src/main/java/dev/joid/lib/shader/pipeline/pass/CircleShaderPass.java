package dev.joid.lib.shader.pipeline.pass;

import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.RoundedShaderType;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class CircleShaderPass implements IShaderPass {

	private final float radius;
	private final float centerX;
	private final float centerY;

	public CircleShaderPass(final @NonNull Node node) {
		this.radius = (float) Math.min(node.dw(2D), node.dh(2D));
		this.centerX = (float) node.ax(node.dw(2D));
		this.centerY = (float) node.ay(node.dh(2D));
	}

	public CircleShaderPass(final float radius, final float centerX, final float centerY) {
		this.radius = radius;
		this.centerX = centerX;
		this.centerY = centerY;
	}

	@Override
	public void unbind() {
		CircleShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 100;
	}

	@Override
	public void bind(final @NonNull ShaderPassContext context) {
		if (!CircleShader.inst().canDraw()) {
			return;
		}

		CircleShader.inst().bind(this.radius, this.centerX, this.centerY, RoundedShaderType.TEXTURE);
	}

}