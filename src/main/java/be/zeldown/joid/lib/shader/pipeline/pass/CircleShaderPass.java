package be.zeldown.joid.lib.shader.pipeline.pass;

import be.zeldown.joid.lib.shader.impl.CircleShader;
import be.zeldown.joid.lib.shader.impl.CircleShader.RoundedShaderType;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public class CircleShaderPass implements ShaderPass {

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
	public void bindDirect(final Node node) {
		if (!CircleShader.inst().isAvailable()) {
			return;
		}

		CircleShader.inst().bind(this.radius, this.centerX, this.centerY, RoundedShaderType.AUTO);
	}

	@Override
	public void bindForTexture(final Node node) {
		if (!CircleShader.inst().isAvailable()) {
			return;
		}

		CircleShader.inst().bind(this.radius, this.centerX, this.centerY, RoundedShaderType.TEXTURE);
	}

	@Override
	public void unbind() {
		CircleShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 100;
	}

}