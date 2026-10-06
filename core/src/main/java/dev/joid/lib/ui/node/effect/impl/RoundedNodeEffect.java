package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoundedNodeEffect<T extends Node> extends NodeEffect<T, RoundedNodeEffect<T>> {

	private Supplier<Float> radiusSupplier;
	private Supplier<Boolean> leftSupplier;
	private Supplier<Boolean> topSupplier;
	private Supplier<Boolean> rightSupplier;
	private Supplier<Boolean> bottomSupplier;

	private RoundedNodeEffect(final float radius, final boolean left, final boolean top, final boolean right, final boolean bottom) {
		this.radiusSupplier = () -> radius;
		this.leftSupplier = () -> left;
		this.topSupplier = () -> top;
		this.rightSupplier = () -> right;
		this.bottomSupplier = () -> bottom;
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final float radius) {
		return new RoundedNodeEffect<>(radius, true, true, true, true);
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final @NonNull Supplier<Float> radiusSupplier) {
		return RoundedNodeEffect.create(radiusSupplier, () -> true, () -> true, () -> true, () -> true);
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final float radius, final boolean left, final boolean top, final boolean right, final boolean bottom) {
		return new RoundedNodeEffect<>(radius, left, top, right, bottom);
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final @NonNull Supplier<Float> radiusSupplier, final @NonNull Supplier<Boolean> leftSupplier, final @NonNull Supplier<Boolean> topSupplier, final @NonNull Supplier<Boolean> rightSupplier, final @NonNull Supplier<Boolean> bottomSupplier) {
		return new RoundedNodeEffect<>(radiusSupplier, leftSupplier, topSupplier, rightSupplier, bottomSupplier);
	}

	public float getRadius() {
		return this.radiusSupplier.get();
	}

	public boolean isTop() {
		return this.topSupplier.get();
	}

	public boolean isLeft() {
		return this.leftSupplier.get();
	}

	public boolean isRight() {
		return this.rightSupplier.get();
	}

	public boolean isBottom() {
		return this.bottomSupplier.get();
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public ShaderPass toShaderPass(final @NonNull T node) {
		return new RoundedShaderPass(this, node);
	}

	public @NonNull RoundedNodeEffect<T> radius(final float radius) {
		this.radiusSupplier = () -> radius;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> left(final boolean left) {
		this.leftSupplier = () -> left;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> left(final @NonNull Supplier<Boolean> leftSupplier) {
		this.leftSupplier = leftSupplier;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> right(final boolean right) {
		this.rightSupplier = () -> right;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> right(final @NonNull Supplier<Boolean> rightSupplier) {
		this.rightSupplier = rightSupplier;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> top(final boolean top) {
		this.topSupplier = () -> top;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> top(final @NonNull Supplier<Boolean> topSupplier) {
		this.topSupplier = topSupplier;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> bottom(final boolean bottom) {
		this.bottomSupplier = () -> bottom;
		return this;
	}

	public @NonNull RoundedNodeEffect<T> bottom(final @NonNull Supplier<Boolean> bottomSupplier) {
		this.bottomSupplier = bottomSupplier;
		return this;
	}

}