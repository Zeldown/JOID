package be.zeldown.joid.lib.shader.pipeline.pass;

import be.zeldown.joid.lib.shader.impl.RoundedShader;
import be.zeldown.joid.lib.shader.impl.RoundedShader.RoundedShaderType;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import lombok.NonNull;

public class RoundedShaderPass implements ShaderPass {

	private final RoundedNodeEffect<?> effect;
	private final Node node;

	private final float fixedRadius;
	private final float fixedX1;
	private final float fixedY1;
	private final float fixedX2;
	private final float fixedY2;

	public RoundedShaderPass(final @NonNull RoundedNodeEffect<?> effect, final @NonNull Node node) {
		this.effect = effect;
		this.node = node;
		this.fixedRadius = -1F;
		this.fixedX1 = 0F;
		this.fixedY1 = 0F;
		this.fixedX2 = 0F;
		this.fixedY2 = 0F;
	}

	public RoundedShaderPass(final float radius, final float x1, final float y1, final float x2, final float y2) {
		this.effect = null;
		this.node = null;
		this.fixedRadius = radius;
		this.fixedX1 = x1;
		this.fixedY1 = y1;
		this.fixedX2 = x2;
		this.fixedY2 = y2;
	}

	@Override
	public void bindDirect(final Node node) {
		this.bindShader(RoundedShaderType.AUTO);
	}

	@Override
	public void bindForTexture(final Node node) {
		this.bindShader(RoundedShaderType.TEXTURE);
	}

	@Override
	public void unbind() {
		RoundedShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 100;
	}

	private void bindShader(final @NonNull RoundedShaderType type) {
		if (!RoundedShader.inst().isAvailable()) {
			return;
		}

		if (this.effect == null) {
			RoundedShader.inst().bind(this.fixedRadius, this.fixedX1, this.fixedY1, this.fixedX2, this.fixedY2, type);
			return;
		}

		final float radius = this.effect.getRadius();
		final float x1 = (float) (this.node.getX() + (this.effect.isLeft() ? radius : 0));
		final float y1 = (float) (this.node.getY() + (this.effect.isTop() ? radius : 0));
		final float x2 = (float) (this.node.getX() + this.node.getWidth() - (this.effect.isRight() ? radius : 0));
		final float y2 = (float) (this.node.getY() + this.node.getHeight() - (this.effect.isBottom() ? radius : 0));
		RoundedShader.inst().bind(radius, x1, y1, x2, y2, type);
	}

}