package dev.joid.lib.ui.node.effect;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.signal.Signal;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class NodeEffect<T extends Node> {

	private Supplier<Integer>         priority = () -> 0;
	private Supplier<NodeEffectScope> scope    = () -> NodeEffectScope.SELF;

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

	public final int getPriority() {
		return this.priority.get();
	}

	public final @NonNull NodeEffectScope getScope() {
		return this.scope.get();
	}

	public final <E extends NodeEffect<T>> @NonNull E priority(final int priority) {
		return this.priority(Signal.from(priority));
	}

	public final <E extends NodeEffect<T>> @NonNull E priority(final @NonNull Supplier<Integer> priority) {
		this.priority = priority;
		return (E) this;
	}

	public final <E extends NodeEffect<T>> @NonNull E scope(final @NonNull NodeEffectScope scope) {
		return this.scope(Signal.from(scope));
	}

	public final <E extends NodeEffect<T>> @NonNull E scope(final @NonNull Supplier<@NonNull NodeEffectScope> scope) {
		this.scope = scope;
		return (E) this;
	}

	public static enum NodeEffectScope {

		SELF,
		CHILDREN;

	}

}