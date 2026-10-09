package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RoundedNodeEffect extends NodeEffect<Node> {

	private Supplier<Float> radiusSupplier;
	private Supplier<Boolean> leftSupplier;
	private Supplier<Boolean> topSupplier;
	private Supplier<Boolean> rightSupplier;
	private Supplier<Boolean> bottomSupplier;

	protected RoundedNodeEffect(final float radius, final boolean left, final boolean top, final boolean right, final boolean bottom) {
		this.radiusSupplier = () -> radius;
		this.leftSupplier = () -> left;
		this.topSupplier = () -> top;
		this.rightSupplier = () -> right;
		this.bottomSupplier = () -> bottom;
	}

	public static RoundedNodeEffect create(final float radius) {
		return new RoundedNodeEffect(radius, true, true, true, true);
	}

	public static RoundedNodeEffect create(final @NonNull Supplier<Float> radiusSupplier) {
		return RoundedNodeEffect.create(radiusSupplier, () -> true, () -> true, () -> true, () -> true);
	}

	public static RoundedNodeEffect create(final float radius, final boolean left, final boolean top, final boolean right, final boolean bottom) {
		return new RoundedNodeEffect(radius, left, top, right, bottom);
	}

	public static RoundedNodeEffect create(final @NonNull Supplier<Float> radiusSupplier, final @NonNull Supplier<Boolean> leftSupplier, final @NonNull Supplier<Boolean> topSupplier, final @NonNull Supplier<Boolean> rightSupplier, final @NonNull Supplier<Boolean> bottomSupplier) {
		return new RoundedNodeEffect(radiusSupplier, leftSupplier, topSupplier, rightSupplier, bottomSupplier);
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
	public IShaderPass toShaderPass(final @NonNull Node node) {
		return new RoundedShaderPass(this, node);
	}

	public final <E extends RoundedNodeEffect> @NonNull E radius(final float radius) {
		return this.radius(Signal.from(radius));
	}

	public final <E extends RoundedNodeEffect> @NonNull E radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return (E) this;
	}

	public final <E extends RoundedNodeEffect> @NonNull E left(final boolean left) {
		return this.left(Signal.from(left));
	}

	public final <E extends RoundedNodeEffect> @NonNull E left(final @NonNull Supplier<Boolean> leftSupplier) {
		this.leftSupplier = leftSupplier;
		return (E) this;
	}

	public final <E extends RoundedNodeEffect> @NonNull E right(final boolean right) {
		return this.right(Signal.from(right));
	}

	public final <E extends RoundedNodeEffect> @NonNull E right(final @NonNull Supplier<Boolean> rightSupplier) {
		this.rightSupplier = rightSupplier;
		return (E) this;
	}

	public final <E extends RoundedNodeEffect> @NonNull E top(final boolean top) {
		return this.top(Signal.from(top));
	}

	public final <E extends RoundedNodeEffect> @NonNull E top(final @NonNull Supplier<Boolean> topSupplier) {
		this.topSupplier = topSupplier;
		return (E) this;
	}

	public final <E extends RoundedNodeEffect> @NonNull E bottom(final boolean bottom) {
		return this.bottom(Signal.from(bottom));
	}

	public final <E extends RoundedNodeEffect> @NonNull E bottom(final @NonNull Supplier<Boolean> bottomSupplier) {
		this.bottomSupplier = bottomSupplier;
		return (E) this;
	}

}