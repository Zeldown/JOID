package dev.joid.showcase;

import java.util.Locale;

import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ShowMenu extends ShowUI {

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-260, -320, 1000, ShowUI.VIOLET.copyAlpha(0.45F), 9D, 0D, 60D);
		this.blob(1480, -260, 760, ShowUI.CYAN.copyAlpha(0.24F), 9D, 0.3D, 50D);
		this.blob(1000, 700, 760, ShowUI.PINK.copyAlpha(0.20F), 9D, 0.6D, 60D);

		this.header();

		final ShowScene[] scenes = ShowScene.values();
		this.feature(scenes[0]);
		for (int i = 1; i < scenes.length; i++) {
			final int slot = i < 5 ? i - 1 : i + 3;
			final int column = slot < 4 ? 2 + slot % 2 : slot % 4;
			final int row = slot < 4 ? slot / 2 : slot / 4;
			this.card(scenes[i], 132D + column * 420D, 300D + row * 174D);
		}

		double x = 132D;
		x = this.key(x, "Page Down", "Next scene");
		x = this.key(x, "Page Up", "Previous scene");
		x = this.key(x, "Esc", "Back to this menu");
		this.key(x, "F3", "Dev panel");
	}

	private void header() {
		RectNode
		.create(132, 64, 56, 56)
		.color(ShowUI.diagonal(ShowUI.FUCHSIA, ShowUI.VIOLET))
		.effect(RoundedNodeEffect.create(16F))
		.effect(ShadowNodeEffect.create(ShowUI.VIOLET.copyAlpha(0.8F), 26F))
		.body(logo -> {
			TextNode.create(28, 28).text(Text.create("J", ShowUI.font(FontWeight.BLACK, 32F, Color.WHITE))).anchor(Align.CENTER).attach(logo);
		})
		.attach(this);
		TextNode.create(208, 92).text(Text.create("JOID", ShowUI.font(FontWeight.EXTRA_BOLD, 30F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(this);
		RectNode
		.create(308, 72, 92, 40)
		.color(Color.WHITE.copyAlpha(0.08F))
		.effect(RoundedNodeEffect.create(20F))
		.body(pill -> {
			TextNode.create(46, 20).text(Text.create(JOID.VERSION, ShowUI.font(FontWeight.SEMI_BOLD, 18F, ShowUI.MUTED))).anchor(Align.CENTER).attach(pill);
		})
		.attach(this);
		TextNode.create(128, 144).text(Text.create("The showcase, live.", ShowUI.font(FontWeight.BLACK, 64F, ShowUI.TEXT))).attach(this);
		TextNode.create(132, 236).text(Text.create("Every designed scene of the README, running on the Vulkan backend. Pick one and play with it.", ShowUI.font(FontWeight.MEDIUM, 24F, ShowUI.MUTED))).attach(this);
	}

	private void feature(final ShowScene scene) {
		final Color accent = scene.getAccent();
		RectNode
		.create(132, 300, 816, 324)
		.<RectNode>self(card -> card.color(() -> ShowUI.diagonal(accent.copyAlpha(0.32F + card.hoverValue(0.14F)), ShowUI.PINK.copyAlpha(0.12F + card.hoverValue(0.10F)))))
		.<RectNode>self(card -> card.effect(ShadowNodeEffect.create(accent, 48F).color(() -> accent.copyAlpha(card.hoverValue(0.55F)))))
		.effect(RoundedNodeEffect.create(30F))
		.effect(BorderNodeEffect.create(Color.WHITE.copyAlpha(0.16F), 1.5F, BorderMode.IN))
		.body(card -> {
			TextNode.create(40, 40).text(Text.create(ShowMenu.number(scene) + "  ·  START HERE", ShowUI.font(FontWeight.BOLD, 17F, ShowUI.TEXT.copyAlpha(0.7F)).letterSpacing(0.25F))).attach(card);
			TextNode.create(36, 78).text(Text.create(scene.getTitle(), ShowUI.font(FontWeight.BLACK, 84F, ShowUI.TEXT))).attach(card);
			TextNode.create(40, 190).text(Text.create(scene.getCaption(), ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED))).attach(card);
			RectNode
			.create(40, 244, 150, 48)
			.color(() -> Color.WHITE.copyAlpha(0.85F + card.hoverValue(0.15F)))
			.effect(RoundedNodeEffect.create(24F))
			.body(button -> {
				TextNode.create(75, 24).text(Text.create("Open", ShowUI.font(FontWeight.BOLD, 20F, ShowUI.NIGHT))).anchor(Align.CENTER).attach(button);
			})
			.attach(card);
		})
		.onClick((node, mouseX, mouseY, clickType) -> ShowUI.open(scene.create()))
		.hoverDuration(220L)
		.attach(this);
	}

	private void card(final ShowScene scene, final double x, final double y) {
		final Color accent = scene.getAccent();
		RectNode
		.create(x, y, 396, 150)
		.<RectNode>self(card -> card.color(() -> Color.WHITE.copyAlpha(0.06F + card.hoverValue(0.06F))))
		.<RectNode>self(card -> card.effect(ShadowNodeEffect.create(accent, 36F).color(() -> accent.copyAlpha(card.hoverValue(0.30F)))))
		.effect(RoundedNodeEffect.create(24F))
		.effect(BorderNodeEffect.create(Color.WHITE.copyAlpha(0.12F), 1.5F, BorderMode.IN))
		.body(card -> {
			RectNode
			.create(24, 24, 52, 52)
			.color(ShowUI.diagonal(accent, accent.to(ShowUI.NIGHT, 0.45F)))
			.effect(RoundedNodeEffect.create(14F))
			.body(badge -> {
				TextNode.create(26, 26).text(Text.create(ShowMenu.number(scene), ShowUI.font(FontWeight.BOLD, 20F, Color.WHITE))).anchor(Align.CENTER).attach(badge);
			})
			.attach(card);
			TextNode.create(96, 50).text(Text.create(scene.getTitle(), ShowUI.font(FontWeight.BOLD, 26F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(card);
			TextNode.create(24, 110).text(Text.create(scene.getCaption(), ShowUI.font(FontWeight.MEDIUM, 18F, ShowUI.MUTED))).anchorY(Align.CENTER).attach(card);
		})
		.onClick((node, mouseX, mouseY, clickType) -> ShowUI.open(scene.create()))
		.hoverDuration(180L)
		.attach(this);
	}

	private double key(final double x, final String key, final String label) {
		final TextInfo cap = ShowUI.font(FontWeight.BOLD, 16F, ShowUI.TEXT);
		final TextInfo info = ShowUI.font(FontWeight.MEDIUM, 19F, ShowUI.MUTED);
		final double width = cap.getWidth(key) + 28D;
		RectNode
		.create(x, 1002, width, 36)
		.color(Color.WHITE.copyAlpha(0.10F))
		.effect(RoundedNodeEffect.create(9F))
		.effect(BorderNodeEffect.create(Color.WHITE.copyAlpha(0.16F), 1F, BorderMode.IN))
		.body(keycap -> {
			TextNode.create(width / 2D, 18).text(Text.create(key, cap)).anchor(Align.CENTER).attach(keycap);
		})
		.attach(this);
		TextNode.create(x + width + 14D, 1020).text(Text.create(label, info)).anchorY(Align.CENTER).attach(this);
		return x + width + 14D + info.getWidth(label) + 44D;
	}

	private static String number(final ShowScene scene) {
		return String.format(Locale.US, "%02d", scene.ordinal() + 1);
	}

}