package be.zeldown.joid.lib.ui.node.effect;

import java.util.Collections;
import java.util.List;

import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class NodeEffect<T extends Node> {

	private int priority;
	private EffectScope scope = EffectScope.SELF;

	public void init(final @NonNull T node, final @NonNull UI ui) {}
	public void pre(final @NonNull T node, final double mouseX, final double mouseY) {}
	public void post(final @NonNull T node, final double mouseX, final double mouseY) {}

	public boolean shouldApply(final @NonNull T node) {
		return true;
	}

	public boolean isShaderEffect() {
		return false;
	}

	public ShaderPass toShaderPass(final @NonNull T node) {
		return null;
	}

	public List<ShaderPass> toShaderPasses(final @NonNull T node) {
		final ShaderPass pass = this.toShaderPass(node);
		return pass != null ? Collections.singletonList(pass) : Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	public @NonNull <E extends NodeEffect<T>> E priority(final int priority) {
		this.priority = priority;
		return (E) this;
	}

	public @NonNull NodeEffect<T> scope(final @NonNull EffectScope scope) {
		this.scope = scope;
		return this;
	}

}