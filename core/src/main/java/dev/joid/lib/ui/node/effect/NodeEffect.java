package dev.joid.lib.ui.node.effect;

import java.util.Collections;
import java.util.List;

import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class NodeEffect<T extends Node> {

	private int priority;
	private NodeEffectScope scope = NodeEffectScope.SELF;

	public void init(final @NonNull T node, final @NonNull UI ui) {}

	public void detach(final @NonNull T node) {}

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

	public final <E extends NodeEffect<T>> @NonNull E priority(final int priority) {
		this.priority = priority;
		return (E) this;
	}

	public final <E extends NodeEffect<T>> @NonNull E scope(final @NonNull NodeEffectScope scope) {
		this.scope = scope;
		return (E) this;
	}

	public static enum NodeEffectScope {

		SELF,
		CHILDREN;

	}

}