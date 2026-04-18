package be.zeldown.joid.lib.shader.pipeline.pass;

import javax.vecmath.Vector4f;

import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.shader.impl.GradientShader;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public class GradientShaderPass implements ShaderPass {

	private final ColorGradient gradient;
	private final Vector4f canvas;

	public GradientShaderPass(final @NonNull ColorGradient gradient, final @NonNull Vector4f canvas) {
		this.gradient = gradient;
		this.canvas = canvas;
	}

	@Override
	public void bindDirect(final Node node) {
		this.bindInternal(false);
	}

	@Override
	public void bindForTexture(final Node node) {
		this.bindInternal(true);
	}

	@Override
	public void unbind() {
		GradientShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 0;
	}

	@Override
	public boolean supportsDirectBind() {
		return false;
	}

	/* [ Internal Section ] */
	private void bindInternal(final boolean hasTexture) {
		if (!GradientShader.inst().isAvailable()) {
			return;
		}

		GradientShader.inst().bind();

		final Float2Uniform startPosUniform = GradientShader.inst().getShader().getFloat2Uniform("startPos");
		startPosUniform.setValue(this.gradient.getDirection().x, this.gradient.getDirection().y);

		final Float2Uniform endPosUniform = GradientShader.inst().getShader().getFloat2Uniform("endPos");
		endPosUniform.setValue(this.gradient.getDirection().z, this.gradient.getDirection().w);

		final Float4Uniform startColorUniform = GradientShader.inst().getShader().getFloat4Uniform("startColor");
		startColorUniform.setValue(this.gradient.getStartColor().r, this.gradient.getStartColor().g, this.gradient.getStartColor().b, this.gradient.getStartColor().a);

		final Float4Uniform endColorUniform = GradientShader.inst().getShader().getFloat4Uniform("endColor");
		endColorUniform.setValue(this.gradient.getEndColor().r, this.gradient.getEndColor().g, this.gradient.getEndColor().b, this.gradient.getEndColor().a);

		final IntUniform hasTextureUniform = GradientShader.inst().getShader().getIntUniform("hasTexture");
		hasTextureUniform.setValue(hasTexture ? 1 : 0);

		final Float4Uniform canvasUniform = GradientShader.inst().getShader().getFloat4Uniform("canvas");
		canvasUniform.setValue(this.canvas.x, this.canvas.y, this.canvas.z, this.canvas.w);
	}

}