package dev.joid.showcase;

import java.util.Locale;

import javax.vecmath.Vector2d;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.DoubleSignal;

public class ShowPlayer extends ShowUI {

	private final DoubleSignal  progress = DoubleSignal.of(0D);
	private final DoubleSignal  scrub    = DoubleSignal.of(-1D);
	private final BooleanSignal playing  = BooleanSignal.of(true);

	private ResourcePlayerNode player;

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-250, 500, 900, ShowUI.AMBER.copyAlpha(0.22F), 9D, 0D, 50D);
		this.blob(1350, -300, 900, ShowUI.PINK.copyAlpha(0.22F), 9D, 0.5D, 50D);
		CircleNode.create(560, 140, 800).color(ShowUI.ORANGE.copyAlpha(0.25F)).effect(BlurNodeEffect.create(120F)).attach(this);

		final Resource clip = Resource.of(ShowPlayer.class.getResourceAsStream("/assets/showcase/honey.mp4"));
		RectNode
		.create(240, 96, 1440, 810)
		.color(ShowUI.NIGHT)
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.6F), 70F, 0D, 34D))
		.effect(RoundedNodeEffect.create(34F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			this.player = ResourcePlayerNode
			.create(0, 0, 1440, 810)
			.resource(clip)
			.stretch(StretchType.COVER)
			.loop(true)
			.volume(0F)
			.onPlay(player -> this.playing.set(true))
			.onPause(player -> this.playing.set(false))
			.onProgress((player, value, seconds) -> this.progress.set(value))
			.attach(card);
			RectNode.create(0, 0, 1440, 220).color(() -> ShowUI.vertical(Color.BLACK.copyAlpha(0.55F * card.hoverValue(1F)), Color.BLACK.copyAlpha(0F))).attach(card);
			RectNode.create(0, 500, 1440, 310).color(() -> ShowUI.vertical(Color.BLACK.copyAlpha(0F), Color.BLACK.copyAlpha(0.35F + 0.5F * card.hoverValue(1F)))).attach(card);
			TextNode.create(56, 48).text(() -> Text.create("Golden Hour", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.BOLD, 54F, ShowUI.TEXT.copyAlpha(card.hoverValue(1F))))).attach(card);
			TextNode.create(60, 120).text(() -> Text.create("Slow motion  ·  honey and light", ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED.copyAlpha(card.hoverValue(1F))))).attach(card);
			this.controls(card);
		})
		.hoverDuration(350L)
		.attach(this);

		TextNode.create(960, 960).text(Text.create("Video and audio streams, decoded off the render thread, drawn like any node.", ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED))).anchorX(Align.CENTER).attach(this);
	}

	private void controls(final Node card) {
		CircleNode
		.create(56, 690, 84)
		.color(() -> Color.WHITE.copyAlpha(0.92F * card.hoverValue(1F) + 0.08F))
		.effect(ShadowNodeEffect.create(ShowUI.AMBER, 26F).color(() -> ShowUI.AMBER.copyAlpha(0.8F * card.hoverValue(1F))))
		.onClick((node, mouseX, mouseY, clickType) -> {
			if (this.player.isPlaying()) {
				this.player.pause();
			} else {
				this.player.resume();
			}
		})
		.layer((mouseX, mouseY) -> this.icon(98D, 732D, card.hoverValue(1F)))
		.attach(card);
		RectNode
		.create(176, 712, 1060, 40)
		.color(Color.TRANSPARENT)
		.onClick((node, mouseX, mouseY, clickType) -> this.scrub.set(this.ratio(mouseX)))
		.onMouseDragged((node, mouseX, mouseY, clickType, delta) -> {
			if (this.scrub.get() >= 0D) {
				this.scrub.set(this.ratio(mouseX));
			}
		})
		.onMouseReleased((node, mouseX, mouseY, clickType) -> {
			if (this.scrub.get() >= 0D) {
				this.player.seekTo(this.scrub.get() * this.player.getDuration());
				this.scrub.set(-1D);
			}
		})
		.layer((mouseX, mouseY) -> this.bar(176D, 732D, 1060D, card.hoverValue(1F)))
		.attach(card);
		TextNode.create(1384, 732).text(() -> Text.create(ShowPlayer.clock(this.shown() * Math.max(1D, this.player.getDuration())) + " / " + ShowPlayer.clock(this.player.getDuration()), ShowUI.font(FontWeight.SEMI_BOLD, 22F, ShowUI.TEXT.copyAlpha(0.4F + 0.6F * card.hoverValue(1F))))).anchorX(Align.END).anchorY(Align.CENTER).attach(card);
	}

	private void icon(final double x, final double y, final float hover) {
		final Color ink = ShowUI.NIGHT.copyAlpha(0.3F + 0.7F * hover);
		if (this.playing.get()) {
			DrawUtils.SHAPE.drawRoundedRect(x - 13D, y - 16D, 9D, 32D, ink, 3F);
			DrawUtils.SHAPE.drawRoundedRect(x + 4D, y - 16D, 9D, 32D, ink, 3F);
		} else {
			DrawUtils.SHAPE.drawPolygon(ink, new Vector2d(x - 9D, y - 17D), new Vector2d(x - 9D, y + 17D), new Vector2d(x + 17D, y));
		}
	}

	private void bar(final double x, final double y, final double width, final float hover) {
		final double shown = this.shown();
		final double thickness = 6D + 4D * hover;
		DrawUtils.SHAPE.drawRoundedRect(x, y - thickness / 2D, width, thickness, Color.WHITE.copyAlpha(0.22F), (float) thickness / 2F);
		DrawUtils.SHAPE.drawShadow(x, y - thickness / 2D, width * shown, thickness, ShowUI.ORANGE.copyAlpha(0.6F * hover), (float) thickness / 2F, 16F);
		DrawUtils.SHAPE.drawRoundedRect(x, y - thickness / 2D, width * shown, thickness, ShowUI.AMBER.toGradient(ShowUI.PINK), (float) thickness / 2F);
		final double knob = (this.scrub.get() >= 0D ? 14D : 9D) * hover;
		if (knob > 0.5D) {
			DrawUtils.SHAPE.drawCircle(x + width * shown, y, Color.WHITE, knob);
		}
	}

	private double shown() {
		return this.scrub.get() >= 0D ? this.scrub.get() : this.progress.get();
	}

	private double ratio(final double mouseX) {
		return Math.max(0D, Math.min(1D, (mouseX - 416D) / 1060D));
	}

	private static String clock(final double seconds) {
		final int total = (int) Math.floor(seconds);
		return String.format(Locale.US, "%d:%02d", total / 60, total % 60);
	}

}