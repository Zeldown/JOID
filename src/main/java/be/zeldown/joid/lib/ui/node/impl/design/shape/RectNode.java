package be.zeldown.joid.lib.ui.node.impl.design.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.vecmath.Vector4f;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.shader.pipeline.ShaderPass;
import be.zeldown.joid.lib.shader.pipeline.ShaderPipeline;
import be.zeldown.joid.lib.shader.pipeline.pass.BorderShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.CircleShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.GradientShaderPass;
import be.zeldown.joid.lib.shader.pipeline.pass.RoundedShaderPass;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.effect.NodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import be.zeldown.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class RectNode extends Node {

	private Supplier<Color> color;
	private Supplier<Color> hoveredColor;

	private Supplier<Color> borderColor;
	private Supplier<Color> hoveredBorderColor;
	private double          borderStroke;
	private boolean         borderFill;

	protected RectNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.color = () -> Color.TRANSPARENT;
		this.hoveredColor = null;

		this.borderColor = () -> Color.TRANSPARENT;
		this.hoveredBorderColor = null;
	}

	public static @NonNull RectNode create(final double x, final double y, final double width, final double height) {
		return new RectNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final Color hoveredColor = this.hoveredColor != null ? this.hoveredColor.get() : null;
		final Color color = hoveredColor != null ? this.color.get().to(hoveredColor, super.hoverValue(1F)) : this.color.get();

		final Color hoveredBorderColor = this.hoveredBorderColor != null ? this.hoveredBorderColor.get() : null;
		final Color borderColor = hoveredBorderColor != null ? this.borderColor.get().to(hoveredBorderColor, super.hoverValue(1F)) : this.borderColor.get();

		final RoundedNodeEffect<?> roundedEffect = super.getEffect(RoundedNodeEffect.class);
		final CircleNodeEffect<?> circleEffect = super.getEffect(CircleNodeEffect.class);

		final List<ShaderPass> passes = new ArrayList<>();

		if (color.isGradient()) {
			final Vector4f canvas = new Vector4f((float) super.getX(), (float) super.getY(), (float) (super.getX() + super.getWidth()), (float) (super.getY() + super.getHeight()));
			passes.add(new GradientShaderPass(color.gradient, canvas));
		}

		if (roundedEffect != null && roundedEffect.getRadius() > 0F) {
			passes.add(new RoundedShaderPass(roundedEffect, this));
		}

		if (circleEffect != null) {
			passes.add(new CircleShaderPass(this));
		}

		if (this.borderStroke > 0D) {
			passes.add(new BorderShaderPass((float) this.borderStroke, borderColor, this.borderFill));
		}

		ShaderPipeline.render(this, passes, () -> {
			if (color.isGradient()) {
				Color.WHITE.bind();
				DrawUtils.SHAPE.drawRawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight());
				Color.reset();
			} else {
				DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), color);
			}
		});
	}

	/* [ Override Section ] */
	@Override
	public boolean shouldApplyEffect(final @NonNull NodeEffect<Node> effect) {
		return super.shouldApplyEffect(effect) && !(effect instanceof RoundedNodeEffect) && !(effect instanceof CircleNodeEffect);
	}

	/* [ Getter Section ] */
	public final @NonNull Color getColor() {
		return this.color.get();
	}

	public final @NonNull Color getHoveredColor() {
		return this.hoveredColor.get();
	}

	public final @NonNull Color getBorderColor() {
		return this.borderColor.get();
	}

	public final @NonNull Color getHoveredBorderColor() {
		return this.hoveredBorderColor.get();
	}

	/* [ Setter Section ] */
	public final <T extends RectNode> @NonNull T color(final @NonNull Color color) {
		this.color(() -> color);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Color color, final Color hoveredColor) {
		this.color(() -> color, () -> hoveredColor);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final Color color) {
		this.hoveredColor(() -> color);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color border, final double stroke) {
		return this.border(() -> border, stroke);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color color, final double stroke, final boolean fill) {
		return this.border(() -> color, stroke, fill);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Color color, final Color hoveredColor, final double stroke, final boolean fill) {
		return this.border(() -> color, () -> hoveredColor, stroke, fill);
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Color hoveredColor) {
		this.hoveredBorderColor(() -> hoveredColor);
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color) {
		this.color = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T color(final @NonNull Supplier<@NonNull Color> color, final Supplier<Color> hoveredColor) {
		this.color        = color;
		this.hoveredColor = hoveredColor;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredColor(final Supplier<Color> color) {
		this.hoveredColor = color;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> border, final double stroke) {
		return this.border(border, stroke, true);
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> color, final double stroke, final boolean fill) {
		this.borderColor  = color;
		this.borderStroke = stroke;
		this.borderFill   = fill;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T border(final @NonNull Supplier<@NonNull Color> color, final Supplier<Color> hoveredColor, final double stroke, final boolean fill) {
		this.borderColor        = color;
		this.hoveredBorderColor = hoveredColor;
		this.borderStroke       = stroke;
		this.borderFill         = fill;
		return (T) this;
	}

	public final <T extends RectNode> @NonNull T hoveredBorderColor(final Supplier<Color> hoveredColor) {
		this.hoveredBorderColor = hoveredColor;
		return (T) this;
	}

}