package dev.joid.showcase;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.control.slider.DoubleSliderNode;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class ShowPaperKit implements IKit {

	public static final Color INK    = Color.decode("#151413").lock();
	public static final Color CARD   = Color.decode("#FFFCF4").lock();
	public static final Color TOMATO = Color.decode("#FF5533").lock();
	public static final Color BUTTER = Color.decode("#FFD24D").lock();

	private static final Color RULE  = Color.decode("#E2D9C6").lock();
	private static final Color MUTE  = Color.decode("#6F685C").lock();
	private static final Color PAPER = Color.decode("#F2ECDF").lock();

	@Override
	public void backdrop(final @NonNull Node layer) {
		RectNode
		.create(0, 0, 1920, 1080)
		.color(ShowPaperKit.PAPER)
		.body(paper -> {
			for (int y = 54; y < 1080; y += 54) {
				RectNode.create(0, y, 1920, 2).color(ShowPaperKit.RULE).attach(paper);
			}
			RectNode.create(96, 0, 3, 1080).color(ShowPaperKit.TOMATO.withAlpha(0.45F)).attach(paper);
		})
		.attach(layer);
		CircleNode.create(1610, -150, 520).color(ShowPaperKit.BUTTER).<CircleNode>y(() -> -150D + IKit.wave(0D) * 18D).attach(layer);
		CircleNode.create(800, 900, 260).color(ShowPaperKit.TOMATO).<CircleNode>x(() -> 800D + IKit.wave(0.3D) * 24D).attach(layer);
	}

	@Override
	public @NonNull TextInfo hint() {
		return ShowUI.font(FontWeight.MEDIUM, 25F, ShowPaperKit.MUTE);
	}

	@Override
	public @NonNull TextInfo title() {
		return TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 54F).color(ShowPaperKit.INK);
	}

	@Override
	public @NonNull TextInfo label() {
		return ShowUI.font(FontWeight.BOLD, 20F, ShowPaperKit.INK).letterSpacing(0.12F);
	}

	@Override
	public @NonNull TextInfo accent() {
		return TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 136F).color(ShowPaperKit.TOMATO);
	}

	@Override
	public @NonNull TextInfo display() {
		return TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, 136F).color(ShowPaperKit.INK);
	}

	@Override
	public @NonNull RectNode divider(final double x, final double y, final double width) {
		return RectNode.create(x, y, width, 2D).color(ShowPaperKit.INK.withAlpha(0.18F));
	}

	@Override
	public @NonNull RectNode panel(final double x, final double y, final double width, final double height) {
		return RectNode
		.create(x, y, width, height)
		.color(Color.TRANSPARENT)
		.body(panel -> {
			RectNode.create(16, 16, width, height).color(ShowPaperKit.INK).attach(panel);
			RectNode.create(0, 0, width, height).color(ShowPaperKit.CARD).effect(BorderNodeEffect.create(ShowPaperKit.INK, 4F).mode(BorderMode.IN)).attach(panel);
		});
	}

	@Override
	public @NonNull RectNode choice(final double x, final double y, final @NonNull String label, final @NonNull BooleanSupplier selected) {
		return RectNode
		.create(x, y, 180, 64)
		.color(Color.TRANSPARENT)
		.body(chip -> {
			RectNode.create(8, 8, 180, 64).color(ShowPaperKit.INK).visible(selected::getAsBoolean).attach(chip);
			RectNode
			.create(0, 0, 180, 64)
			.color(() -> selected.getAsBoolean() ? ShowPaperKit.TOMATO : ShowPaperKit.CARD)
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.attach(chip);
			TextNode.create(90, 32).text(Text.create(label.toUpperCase(Locale.ROOT), ShowUI.font(FontWeight.BOLD, 20F, ShowPaperKit.INK).letterSpacing(0.14F))).anchor(Align.CENTER).attach(chip);
		});
	}

	@Override
	public @NonNull RectNode button(final double x, final double y, final double width, final @NonNull Supplier<String> label, final boolean primary) {
		return RectNode
		.create(x, y, width, 72)
		.color(Color.TRANSPARENT)
		.body(button -> {
			RectNode.create(10, 10, width, 72).color(ShowPaperKit.INK).attach(button);
			RectNode
			.create(0, 0, width, 72)
			.color(primary ? ShowPaperKit.TOMATO : ShowPaperKit.CARD)
			.effect(BorderNodeEffect.create(ShowPaperKit.INK, 3F).mode(BorderMode.IN))
			.<RectNode>x(() -> (double) button.hoverValue(4F))
			.<RectNode>y(() -> (double) button.hoverValue(4F))
			.body(face -> {
				TextNode.create(width / 2D, 36).text(() -> Text.create(label.get().toUpperCase(Locale.ROOT), ShowUI.font(FontWeight.BOLD, 21F, ShowPaperKit.INK).letterSpacing(0.14F))).anchor(Align.CENTER).attach(face);
			})
			.attach(button);
		})
		.hoverDuration(120L);
	}

	@Override
	public @NonNull ToggleNode<Boolean, Boolean> toggle(final double x, final double y) {
		return ShowPaperToggleNode.create(x, y);
	}

	@Override
	public @NonNull TextFieldNode field(final double x, final double y, final double width) {
		return ShowPaperFieldNode.create(x, y, width).info(ShowUI.font(FontWeight.SEMI_BOLD, 25F, ShowPaperKit.INK)).marginHorizontal(24D);
	}

	@Override
	public @NonNull DoubleSliderNode slider(final double x, final double y, final double width) {
		return ShowPaperSliderNode.create(x, y, width);
	}

}