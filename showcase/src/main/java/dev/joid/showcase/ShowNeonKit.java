package dev.joid.showcase;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.design.textfield.TextFieldNode;
import dev.joid.lib.ui.node.impl.structure.checkbox.CheckboxNode;
import dev.joid.lib.ui.node.impl.structure.slider.impl.DoubleSliderNode;
import dev.joid.lib.utils.align.Align;

public class ShowNeonKit implements ShowKit {

	@Override
	public void backdrop(final Node layer) {
		RectNode.create(0, 0, 1920, 1080).color(ShowUI.diagonal(ShowUI.NIGHT, ShowUI.DUSK)).attach(layer);
		this.blob(layer, -260, 420, 900, ShowUI.VIOLET.copyAlpha(0.42F), 0D);
		this.blob(layer, 1250, -320, 900, ShowUI.CYAN.copyAlpha(0.22F), 0.4D);
		this.blob(layer, 1300, 640, 760, ShowUI.PINK.copyAlpha(0.24F), 0.7D);
	}

	@Override
	public TextInfo display() {
		return ShowUI.font(FontWeight.BLACK, 124F, ShowUI.TEXT);
	}

	@Override
	public TextInfo accent() {
		return ShowUI.font(FontWeight.BLACK, 124F, ShowUI.FUCHSIA.toGradient(ShowUI.CYAN));
	}

	@Override
	public TextInfo title() {
		return ShowUI.font(FontWeight.EXTRA_BOLD, 46F, ShowUI.TEXT);
	}

	@Override
	public TextInfo label() {
		return ShowUI.font(FontWeight.SEMI_BOLD, 25F, ShowUI.TEXT);
	}

	@Override
	public TextInfo hint() {
		return ShowUI.font(FontWeight.MEDIUM, 25F, ShowUI.MUTED);
	}

	@Override
	public RectNode panel(final double x, final double y, final double width, final double height) {
		return RectNode
		.create(x, y, width, height)
		.color(ShowUI.vertical(Color.WHITE.copyAlpha(0.10F), Color.WHITE.copyAlpha(0.04F)))
		.effect(RoundedNodeEffect.create(36F))
		.effect(BorderNodeEffect.create(Color.WHITE.copyAlpha(0.14F), 1.5F, BorderMode.IN))
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.45F), 48F, 0D, 24D));
	}

	@Override
	public RectNode divider(final double x, final double y, final double width) {
		return RectNode.create(x, y, width, 1.5D).color(Color.WHITE.copyAlpha(0.1F));
	}

	@Override
	public RectNode choice(final double x, final double y, final String label, final BooleanSupplier selected) {
		return RectNode
		.create(x, y, 180, 64)
		.color(() -> selected.getAsBoolean() ? ShowUI.VIOLET.toGradient(ShowUI.PINK) : Color.WHITE.copyAlpha(0.08F))
		.effect(RoundedNodeEffect.create(32F))
		.effect(ShadowNodeEffect.create(ShowUI.FUCHSIA, 26F).color(() -> ShowUI.FUCHSIA.copyAlpha(selected.getAsBoolean() ? 0.6F : 0F)))
		.body(chip -> {
			TextNode.create(90, 32).text(() -> Text.create(label, ShowUI.font(FontWeight.BOLD, 23F, selected.getAsBoolean() ? Color.WHITE : ShowUI.MUTED))).anchor(Align.CENTER).attach(chip);
		});
	}

	@Override
	public RectNode button(final double x, final double y, final double width, final Supplier<String> label, final boolean primary) {
		return RectNode
		.create(x, y, width, 72)
		.<RectNode>self(button -> button.color(() -> primary ? ShowUI.VIOLET.toGradient(ShowUI.PINK) : Color.WHITE.copyAlpha(0.07F + button.hoverValue(0.06F))))
		.effect(RoundedNodeEffect.create(22F))
		.effect(ShadowNodeEffect.create(ShowUI.FUCHSIA.copyAlpha(primary ? 0.6F : 0F), 30F))
		.body(button -> {
			TextNode.create(width / 2D, 36).text(() -> Text.create(label.get(), ShowUI.font(FontWeight.BOLD, 24F, primary ? Color.WHITE : ShowUI.TEXT))).anchor(Align.CENTER).attach(button);
		})
		.hoverDuration(150L);
	}

	@Override
	public CheckboxNode toggle(final double x, final double y) {
		return ShowSwitchNode.create(x, y, ShowUI.VIOLET);
	}

	@Override
	public DoubleSliderNode slider(final double x, final double y, final double width) {
		return ShowSliderNode.create(x, y, width, ShowUI.FUCHSIA, ShowUI.PINK);
	}

	@Override
	public TextFieldNode field(final double x, final double y, final double width) {
		return ShowFieldNode.create(x, y, width, ShowUI.CYAN).info(ShowUI.font(FontWeight.MEDIUM, 25F, ShowUI.TEXT)).marginHorizontal(24D);
	}

	private void blob(final Node layer, final double x, final double y, final double size, final Color color, final double phase) {
		CircleNode
		.create(x, y, size)
		.color(color)
		.<CircleNode>x(() -> x + ShowNeonKit.wave(phase) * 50D)
		.<CircleNode>y(() -> y + ShowNeonKit.wave(phase + 0.25D) * 30D)
		.effect(BlurNodeEffect.create((float) (size / 6D)))
		.attach(layer);
	}

	private static double wave(final double phase) {
		return Math.sin((BridgeHandler.CLOCK.get().currentTimeMillis() / 9000D + phase) * Math.PI * 2D);
	}

}