package be.zeldown.joid.lib.shader.pipeline.pass;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.shader.impl.BorderShader;
import be.zeldown.joid.lib.shader.impl.BorderShader.BorderMode;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.ShaderPipeline;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.NonNull;

public class BorderShaderPass implements ShaderPass {

	private final float borderWidth;
	private final Color borderColor;
	private final boolean fill;
	private final BorderMode mode;

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
	public void bindDirect(final Node node) {
		this.bindInternal(node);
	}

	@Override
	public void bindForTexture(final Node node) {
		this.bindInternal(node);
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
		return this.borderWidth;
	}

	/* [ Internal Section ] */
	private void bindInternal(final Node node) {
		if (!BorderShader.inst().isAvailable()) {
			return;
		}

		final int scaleFactor = ShaderPipeline.scaleFactor(node != null ? node.getUi() : null);
		final double nodeW = node != null ? node.getWidth() : 200D;
		final double nodeH = node != null ? node.getHeight() : 120D;
		final double expandedW = nodeW + this.borderWidth * 2D;
		final double expandedH = nodeH + this.borderWidth * 2D;
		final float pixelW = (float) Math.ceil(expandedW * scaleFactor);
		final float pixelH = (float) Math.ceil(expandedH * scaleFactor);
		final float texelW = 1F / Math.max(1F, pixelW);
		final float texelH = 1F / Math.max(1F, pixelH);

		final float rectX1 = node != null ? (float) node.getX() : 0F;
		final float rectY1 = node != null ? (float) node.getY() : 0F;
		final float rectX2 = rectX1 + (float) nodeW;
		final float rectY2 = rectY1 + (float) nodeH;

		BorderShader.inst().bind(this.borderWidth * scaleFactor, this.borderColor, texelW, texelH, this.fill, this.mode.ordinal(), rectX1, rectY1, rectX2, rectY2);
	}

}