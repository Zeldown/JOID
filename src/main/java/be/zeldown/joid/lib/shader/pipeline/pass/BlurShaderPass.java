package be.zeldown.joid.lib.shader.pipeline.pass;

import be.zeldown.joid.lib.shader.impl.BlurShader;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.ShaderPipeline;
import be.zeldown.joid.lib.ui.node.Node;

public class BlurShaderPass implements ShaderPass {

	private final float radius;
	private final boolean horizontal;
	private final int priorityValue;

	public BlurShaderPass(final float radius, final boolean horizontal, final int iteration) {
		this.radius = radius;
		this.horizontal = horizontal;
		this.priorityValue = 150 + iteration * 2 + (horizontal ? 0 : 1);
	}

	@Override
	public void bindDirect(final Node node) {
		this.bindInternal(node);
	}

	@Override
	public void bindForTexture(final Node node) {
		this.bindInternal(node);
	}

	@Override
	public void unbind() {
		BlurShader.inst().unbind();
	}

	@Override
	public int priority() {
		return this.priorityValue;
	}

	@Override
	public float expansion() {
		return this.radius;
	}

	/* [ Internal Section ] */
	private void bindInternal(final Node node) {
		if (!BlurShader.inst().isAvailable()) {
			return;
		}

		final int scaleFactor = ShaderPipeline.scaleFactor(node != null ? node.getUi() : null);
		final double nodeW = node != null ? node.getWidth() : 200D;
		final double nodeH = node != null ? node.getHeight() : 120D;
		final float pixelW = (float) Math.ceil((nodeW + this.radius * 2F) * scaleFactor);
		final float pixelH = (float) Math.ceil((nodeH + this.radius * 2F) * scaleFactor);
		final float texelW = 1F / Math.max(1F, pixelW);
		final float texelH = 1F / Math.max(1F, pixelH);

		BlurShader.inst().bind(this.radius * scaleFactor, this.horizontal ? 1F : 0F, this.horizontal ? 0F : 1F, texelW, texelH);
	}

}