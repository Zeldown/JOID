package be.zeldown.joid.lib.ui.node.effect.impl;

import java.util.function.Supplier;

import be.zeldown.joid.lib.shader.impl.RoundedShader;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RoundedNodeEffect<T extends Node> extends NodeEffect<T> {

	private Supplier<Float> radiusSupplier;
	private Supplier<Boolean> leftSupplier;
	private Supplier<Boolean> rightSupplier;
	private Supplier<Boolean> topSupplier;
	private Supplier<Boolean> bottomSupplier;

	private RoundedNodeEffect(final float radius, final boolean left, final boolean right, final boolean top, final boolean bottom) {
		this.radiusSupplier = () -> radius;
		this.leftSupplier = () -> left;
		this.rightSupplier = () -> right;
		this.topSupplier = () -> top;
		this.bottomSupplier = () -> bottom;
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

	public static <T extends Node> RoundedNodeEffect<T> create(final float radius) {
		return new RoundedNodeEffect<>(radius, true, true, true, true);
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final float radius, final boolean left, final boolean right, final boolean top, final boolean bottom) {
		return new RoundedNodeEffect<>(radius, left, right, top, bottom);
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final @NonNull Supplier<Float> radiusSupplier) {
		return RoundedNodeEffect.create(radiusSupplier, () -> true, () -> true, () -> true, () -> true);
	}

	public static <T extends Node> RoundedNodeEffect<T> create(final @NonNull Supplier<Float> radiusSupplier, final @NonNull Supplier<Boolean> leftSupplier, final @NonNull Supplier<Boolean> rightSupplier, final @NonNull Supplier<Boolean> topSupplier, final @NonNull Supplier<Boolean> bottomSupplier) {
		final RoundedNodeEffect<T> effect = new RoundedNodeEffect<>(0, true, true, true, true);
		effect.radiusSupplier = radiusSupplier;
		effect.leftSupplier = leftSupplier;
		effect.rightSupplier = rightSupplier;
		effect.topSupplier = topSupplier;
		effect.bottomSupplier = bottomSupplier;
		return effect;
	}

	@Override
	public boolean isShaderEffect() {
		return true;
	}

	@Override
	public ShaderPass toShaderPass(final @NonNull T node) {
		return new RoundedShaderPass(this, node);
	}

	/* [ Internal Section ] */
	@Override
	public void pre(final @NonNull T node, final double mouseX, final double mouseY) {
		if (!RoundedShader.inst().isAvailable()) {
			return;
		}

		final double x = node.getX();
		final double y = node.getY();
		final double width = node.getWidth();
		final double height = node.getHeight();
		final float radius = this.getRadius();
		final boolean left = this.isLeft();
		final boolean right = this.isRight();
		final boolean top = this.isTop();
		final boolean bottom = this.isBottom();
		RoundedShader.inst().bind(radius, (float) (x + (left ? radius : 0)), (float) (y + (top ? radius : 0)), (float) (x + width - (right ? radius : 0)), (float) (y + height - (bottom ? radius : 0)));
	}

	@Override
	public void post(final @NonNull T node, final double mouseX, final double mouseY) {
		if (!RoundedShader.inst().isAvailable()) {
			return;
		}

		RoundedShader.inst().unbind();
	}

	/* [ Setter Section ] */
	public <E extends RoundedNodeEffect<T>> @NonNull E radius(final float radius) {
		this.radiusSupplier = () -> radius;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E radius(final @NonNull Supplier<Float> radiusSupplier) {
		this.radiusSupplier = radiusSupplier;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E left(final boolean left) {
		this.leftSupplier = () -> left;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E left(final @NonNull Supplier<Boolean> leftSupplier) {
		this.leftSupplier = leftSupplier;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E right(final boolean right) {
		this.rightSupplier = () -> right;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E right(final @NonNull Supplier<Boolean> rightSupplier) {
		this.rightSupplier = rightSupplier;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E top(final boolean top) {
		this.topSupplier = () -> top;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E top(final @NonNull Supplier<Boolean> topSupplier) {
		this.topSupplier = topSupplier;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E bottom(final boolean bottom) {
		this.bottomSupplier = () -> bottom;
		return (E) this;
	}

	public <E extends RoundedNodeEffect<T>> @NonNull E bottom(final @NonNull Supplier<Boolean> bottomSupplier) {
		this.bottomSupplier = bottomSupplier;
		return (E) this;
	}

}
