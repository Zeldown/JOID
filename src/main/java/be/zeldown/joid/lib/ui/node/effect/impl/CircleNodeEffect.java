package be.zeldown.joid.lib.ui.node.effect.impl;

import be.zeldown.joid.lib.shader.impl.CircleShader;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.CircleShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CircleNodeEffect<T extends Node> extends NodeEffect<T> {

	public static <T extends Node> CircleNodeEffect<T> create() {
		return new CircleNodeEffect<>();
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public ShaderPass toShaderPass(final @NonNull T node) {
		return new CircleShaderPass(node);
	}

	/* [ Internal Section ] */
	@Override
	public void pre(final @NonNull T node, final double mouseX, final double mouseY) {
		if (!CircleShader.inst().isAvailable()) {
			return;
		}

		CircleShader.inst().bind((float) Math.min(node.dw(2), node.dh(2)), (float) node.ax(node.dw(2)), (float) node.ay(node.dh(2)));
	}

	@Override
	public void post(final @NonNull T node, final double mouseX, final double mouseY) {
		if (!CircleShader.inst().isAvailable()) {
			return;
		}

		CircleShader.inst().unbind();
	}

}
