package dev.joid.lib.ui.node.effect.impl;

import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.pass.CircleShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CircleNodeEffect<T extends Node> extends NodeEffect<T, CircleNodeEffect<T>> {

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

}