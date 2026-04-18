package be.zeldown.joid.lib.shader.pipeline.pass;

import be.zeldown.joid.lib.shader.impl.RoundedShader;
import be.zeldown.joid.lib.shader.impl.RoundedShader.RoundedShaderType;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import lombok.NonNull;

public class RoundedShaderPass implements ShaderPass {

	private final float radius;
	private final float x1;
	private final float y1;
	private final float x2;
	private final float y2;

	public RoundedShaderPass(final @NonNull RoundedNodeEffect<?> effect, final @NonNull Node node) {
		this.radius = effect.getRadius();
		this.x1 = (float) (node.getX() + (effect.isLeft() ? this.radius : 0));
		this.y1 = (float) (node.getY() + (effect.isTop() ? this.radius : 0));
		this.x2 = (float) (node.getX() + node.getWidth() - (effect.isRight() ? this.radius : 0));
		this.y2 = (float) (node.getY() + node.getHeight() - (effect.isBottom() ? this.radius : 0));
	}

	public RoundedShaderPass(final float radius, final float x1, final float y1, final float x2, final float y2) {
		this.radius = radius;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public void bindDirect(final Node node) {
		if (!RoundedShader.inst().isAvailable()) {
			return;
		}

		RoundedShader.inst().bind(this.radius, this.x1, this.y1, this.x2, this.y2, RoundedShaderType.AUTO);
	}

	@Override
	public void bindForTexture(final Node node) {
		if (!RoundedShader.inst().isAvailable()) {
			return;
		}

		RoundedShader.inst().bind(this.radius, this.x1, this.y1, this.x2, this.y2, RoundedShaderType.TEXTURE);
	}

	@Override
	public void unbind() {
		RoundedShader.inst().unbind();
	}

	@Override
	public int priority() {
		return 100;
	}

}