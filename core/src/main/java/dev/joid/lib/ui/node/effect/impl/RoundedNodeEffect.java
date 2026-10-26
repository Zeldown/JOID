package dev.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect;
import lombok.NonNull;

public class RoundedNodeEffect extends NodeEffect<Node> {

	private Supplier<Boolean> topSupplier;
	private Supplier<Float>   radiusSupplier;
	private Supplier<Boolean> leftSupplier;
	private Supplier<Boolean> rightSupplier;
	private Supplier<Boolean> bottomSupplier;

	protected RoundedNodeEffect(final @NonNull Supplier<Float> radiusSupplier) {
		super(100);
		this.radiusSupplier = radiusSupplier;
		this.topSupplier    = () -> true;
		this.leftSupplier   = () -> true;
		this.rightSupplier  = () -> true;
		this.bottomSupplier = () -> true;
	}

	public static @NonNull RoundedNodeEffect create(final float radius) {
		return new RoundedNodeEffect(Signal.from(radius));
	}

	public static @NonNull RoundedNodeEffect create(final @NonNull Supplier<Float> radiusSupplier) {
		return new RoundedNodeEffect(radiusSupplier);
	}

	public float getRadius() {
		return this.radiusSupplier.get();
	}

	public boolean isLeft() {
		return this.leftSupplier.get();
	}

	public boolean isRight() {
		return this.rightSupplier.get();
	}

	public boolean isTop() {
		return this.topSupplier.get();
	}

	public boolean isBottom() {
		return this.bottomSupplier.get();
	}

	@Override
	public IShaderPass toShaderPass(final @NonNull Node node) {
		final PixelGrid grid = BridgeHandler.RENDER.get().getPixelGrid();
		final double left = grid.snapX(node.getX());
		final double top = grid.snapY(node.getY());
		final double right = grid.snapRight(node.getX(), node.getX() + node.getWidth());
		final double bottom = grid.snapBottom(node.getY(), node.getY() + node.getHeight());
		final float radius = (float) Math.min(this.getRadius(), Math.min(Math.abs(right - left), Math.abs(bottom - top)) / 2D);
		return RoundedShaderPass.create(radius, (float) (left + (this.isLeft() ? radius : 0F)), (float) (top + (this.isTop() ? radius : 0F)), (float) (right - (this.isRight() ? radius : 0F)), (float) (bottom - (this.isBottom() ? radius : 0F)));
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