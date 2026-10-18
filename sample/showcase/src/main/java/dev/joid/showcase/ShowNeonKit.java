package dev.joid.showcase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class ShowNeonKit implements IKit {

	@Override
	public void backdrop(final @NonNull Node layer) {
		RectNode.create(0, 0, 1920, 1080).color(ShowUI.diagonal(ShowUI.NIGHT, ShowUI.DUSK)).attach(layer);
		ShowUI.blob(-260, 420, 900, ShowUI.VIOLET.withAlpha(0.42F), 0D, 50D).attach(layer);
		ShowUI.blob(1250, -320, 900, ShowUI.CYAN.withAlpha(0.22F), 0.4D, 50D).attach(layer);
		ShowUI.blob(1300, 640, 760, ShowUI.PINK.withAlpha(0.24F), 0.7D, 50D).attach(layer);
	}

	@Override
	public @NonNull TextInfo display() {
		return ShowUI.font(FontWeight.BLACK, 124F, ShowUI.TEXT);
	}

	@Override
	public @NonNull TextInfo accent() {
		return ShowUI.font(FontWeight.BLACK, 124F, ShowUI.FUCHSIA.toGradient(ShowUI.CYAN));
	}

	@Override
	public @NonNull TextInfo title() {
		return ShowUI.font(FontWeight.EXTRA_BOLD, 46F, ShowUI.TEXT);
	}

	@Override
	public @NonNull TextInfo label() {
		return ShowUI.font(FontWeight.SEMI_BOLD, 25F, ShowUI.TEXT);
	}

	@Override
	public @NonNull TextInfo hint() {
		return ShowUI.font(FontWeight.MEDIUM, 25F, ShowUI.MUTED);
	}

	@Override
	public @NonNull RectNode panel(final double x, final double y, final double width, final double height) {
		return ShowUI.glass(x, y, width, height, 36F);
	}

	@Override
	public @NonNull RectNode divider(final double x, final double y, final double width) {
		return RectNode.create(x, y, width, 1.5D).color(Color.WHITE.withAlpha(0.1F));
	}

	@Override
	public @NonNull RectNode choice(final double x, final double y, final @NonNull String label, final @NonNull BooleanSupplier selected) {
		return RectNode
		.create(x, y, 180, 64)
		.color(() -> selected.getAsBoolean() ? ShowUI.VIOLET.toGradient(ShowUI.PINK) : Color.WHITE.withAlpha(0.08F))
		.effect(RoundedNodeEffect.create(32F))
		.effect(ShadowNodeEffect.create(ShowUI.FUCHSIA, 26F).color(() -> ShowUI.FUCHSIA.withAlpha(selected.getAsBoolean() ? 0.6F : 0F)))
		.body(chip -> {
			TextNode.create(90, 32).text(() -> Text.create(label, ShowUI.font(FontWeight.BOLD, 23F, selected.getAsBoolean() ? Color.WHITE : ShowUI.MUTED))).anchor(Align.CENTER).attach(chip);
		});
	}

	@Override
	public @NonNull RectNode button(final double x, final double y, final double width, final @NonNull Supplier<String> label, final boolean primary) {
		return RectNode
		.create(x, y, width, 72)
		.<RectNode>self(button -> button.color(() -> primary ? ShowUI.VIOLET.toGradient(ShowUI.PINK) : Color.WHITE.withAlpha(0.07F + button.hoverValue(0.06F))))
		.effect(RoundedNodeEffect.create(22F))
		.effect(ShadowNodeEffect.create(ShowUI.FUCHSIA.withAlpha(primary ? 0.6F : 0F), 30F))
		.body(button -> {
			TextNode.create(width / 2D, 36).text(() -> Text.create(label.get(), ShowUI.font(FontWeight.BOLD, 24F, primary ? Color.WHITE : ShowUI.TEXT))).anchor(Align.CENTER).attach(button);
		})
		.hoverDuration(150L);
	}

	@Override
	public @NonNull ToggleNode<Boolean, Boolean> toggle(final double x, final double y) {
		return ShowToggleNode.create(x, y, ShowUI.VIOLET);
	}

	@Override
	public @NonNull DoubleSliderNode slider(final double x, final double y, final double width) {
		return ShowSliderNode.create(x, y, width, ShowUI.FUCHSIA, ShowUI.PINK);
	}

	@Override
	public @NonNull TextFieldNode field(final double x, final double y, final double width) {
		return ShowFieldNode.create(x, y, width, ShowUI.CYAN).info(ShowUI.font(FontWeight.MEDIUM, 25F, ShowUI.TEXT)).marginHorizontal(24D);
	}

}