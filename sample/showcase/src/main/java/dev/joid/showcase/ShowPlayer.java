package dev.joid.showcase;

import java.util.Locale;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class ShowPlayer extends ShowUI {

	private final DoubleSignal  scrub    = new DoubleSignal(-1D);
	private final DoubleSignal  progress = new DoubleSignal(0D);
	private final BooleanSignal playing  = new BooleanSignal(true);

	private ResourcePlayerNode player;

	@Override
	protected void scene() {
		this.backdrop();
		ShowUI.blob(-250, 500, 900, ShowUI.AMBER.withAlpha(0.22F), 0D, 50D).attach(this);
		ShowUI.blob(1350, -300, 900, ShowUI.PINK.withAlpha(0.22F), 0.5D, 50D).attach(this);
		CircleNode.create(560, 140, 800).color(ShowUI.ORANGE.withAlpha(0.25F)).effect(BlurNodeEffect.create(120F)).attach(this);

		final Resource clip = Resource.of("assets/showcase/honey.mp4");
		RectNode
		.create(240, 96, 1440, 810)
		.color(ShowUI.NIGHT)
		.effect(ShadowNodeEffect.create(Color.BLACK.withAlpha(0.6F), 70F).offsetY(34D))
		.effect(RoundedNodeEffect.create(34F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			this.player = ResourcePlayerNode
			.create(0, 0, 1440, 810)
			.resource(clip)
			.stretch(StretchType.COVER)
			.loop(true)
			.volume(0F)
			.onPlay(event -> this.playing.set(true))
			.onPause(event -> this.playing.set(false))
			.onProgress(event -> this.progress.set(event.getProgress()))
			.attach(card);
			RectNode.create(0, 0, 1440, 220).color(() -> ShowUI.vertical(Color.BLACK.withAlpha(0.55F * card.hoverValue(1F)), Color.BLACK.withAlpha(0F))).attach(card);
			RectNode.create(0, 500, 1440, 310).color(() -> ShowUI.vertical(Color.BLACK.withAlpha(0F), Color.BLACK.withAlpha(0.35F + 0.5F * card.hoverValue(1F)))).attach(card);
			TextNode.create(56, 48).text(() -> Text.create("Golden Hour", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.BOLD, 54F, ShowUI.TEXT.withAlpha(card.hoverValue(1F))))).attach(card);
			TextNode.create(60, 120).text(() -> Text.create("Slow motion  ·  honey and light", ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED.withAlpha(card.hoverValue(1F))))).attach(card);
			this.controls(card);
		})
		.hoverDuration(350L)
		.attach(this);

		TextNode.create(960, 960).text(Text.create("Video and audio streams, decoded off the render thread, drawn like any node.", ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED))).anchorX(Align.CENTER).attach(this);
	}

	private void controls(final Node card) {
		CircleNode
		.create(56, 690, 84)
		.color(() -> Color.WHITE.withAlpha(0.92F * card.hoverValue(1F) + 0.08F))
		.effect(ShadowNodeEffect.create(ShowUI.AMBER, 26F).color(() -> ShowUI.AMBER.withAlpha(0.8F * card.hoverValue(1F))))
		.onClick(event -> {
			if (this.player.isPlaying()) {
				this.player.pause();
			} else {
				this.player.play();
			}
		})
		.layer((node, mouseX, mouseY) -> this.icon(42D, 42D, card.hoverValue(1F)))
		.attach(card);
		RectNode
		.create(176, 712, 1060, 40)
		.color(Color.TRANSPARENT)
		.onClick(event -> this.scrub.set(ShowPlayer.ratio(event.getNode(), event.getMouseX())))
		.handleMouseDragged(event -> {
			if (this.scrub.peek() >= 0D) {
				this.scrub.set(ShowPlayer.ratio(event.getNode(), event.getMouseX()));
			}
		})
		.handleMouseReleased(event -> {
			if (this.scrub.peek() >= 0D) {
				this.player.seek(this.scrub.peek() * this.player.getDuration());
				this.scrub.set(-1D);
			}
		})
		.layer((node, mouseX, mouseY) -> this.bar(0D, 20D, 1060D, card.hoverValue(1F)))
		.attach(card);
		TextNode.create(1384, 732).text(() -> Text.create(ShowPlayer.clock(this.shown() * Math.max(1D, this.player.getDuration())) + " / " + ShowPlayer.clock(this.player.getDuration()), ShowUI.font(FontWeight.SEMI_BOLD, 22F, ShowUI.TEXT.withAlpha(0.4F + 0.6F * card.hoverValue(1F))))).anchorX(Align.END).anchorY(Align.CENTER).attach(card);
	}

	private void icon(final double x, final double y, final float hover) {
		final Color ink = ShowUI.NIGHT.withAlpha(0.3F + 0.7F * hover);
		if (this.playing.get()) {
			DrawUtils.SHAPE.drawRoundedRect(x - 13D, y - 16D, 9D, 32D, ink, 3F);
			DrawUtils.SHAPE.drawRoundedRect(x + 4D, y - 16D, 9D, 32D, ink, 3F);
		} else {
			DrawUtils.SHAPE.drawPolygon(ink, Point.create(x - 9D, y - 17D), Point.create(x - 9D, y + 17D), Point.create(x + 17D, y));
		}
	}

	private void bar(final double x, final double y, final double width, final float hover) {
		final double shown = this.shown();
		final double thickness = 6D + 4D * hover;
		DrawUtils.SHAPE.drawRoundedRect(x, y - thickness / 2D, width, thickness, Color.WHITE.withAlpha(0.22F), (float) thickness / 2F);
		DrawUtils.SHAPE.drawShadow(x, y - thickness / 2D, width * shown, thickness, ShowUI.ORANGE.withAlpha(0.6F * hover), (float) thickness / 2F, 16F);
		DrawUtils.SHAPE.drawRoundedRect(x, y - thickness / 2D, width * shown, thickness, ShowUI.AMBER.toGradient(ShowUI.PINK), (float) thickness / 2F);
		final double knob = (this.scrub.get() >= 0D ? 14D : 9D) * hover;
		if (knob > 0.5D) {
			DrawUtils.SHAPE.drawCircle(x + width * shown, y, Color.WHITE, knob);
		}
	}

	private double shown() {
		return this.scrub.get() >= 0D ? this.scrub.get() : this.progress.get();
	}

	private static double ratio(final Node node, final double mouseX) {
		return Math.max(0D, Math.min(1D, (mouseX - node.getAbsoluteX()) / node.getWidth()));
	}

	private static String clock(final double seconds) {
		final int total = (int) Math.floor(seconds);
		return String.format(Locale.US, "%d:%02d", total / 60, total % 60);
	}

}