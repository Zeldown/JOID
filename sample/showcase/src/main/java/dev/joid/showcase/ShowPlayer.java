package dev.joid.showcase;

import java.util.Locale;

import dev.joid.demo.DemoFont;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.transform.Scale;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

public class ShowPlayer extends ShowUI {

	private final DoubleSignal  hover    = new DoubleSignal(0D);
	private final DoubleSignal  scrub    = new DoubleSignal(-1D);
	private final DoubleSignal  progress = new DoubleSignal(0D);
	private final BooleanSignal playing  = new BooleanSignal(true);
	private final TweenAnimator reveal   = TweenAnimator.create(0F);

	private long   peekTime;
	private double peeked = -1D;

	@Override
	protected void scene() {
		super.backdrop();
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
			final ResourcePlayerNode player = ResourcePlayerNode
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
			final ResourcePlayerNode preview = ResourcePlayerNode
			.create(8, 8, 320, 180)
			.resource(clip)
			.stretch(StretchType.COVER)
			.volume(0F)
			.pause();
			this.controls(card, player, preview);
			this.preview(card, player, preview);
		})
		.hoverDuration(350L)
		.attach(this);

		TextNode.create(960, 960).text(Text.create("Video and audio streams, decoded off the render thread, drawn like any node.", ShowUI.font(FontWeight.MEDIUM, 22F, ShowUI.MUTED))).anchorX(Align.CENTER).attach(this);
	}

	private void controls(final Node card, final ResourcePlayerNode player, final ResourcePlayerNode preview) {
		CircleNode
		.create(56, 690, 84)
		.color(() -> Color.WHITE.withAlpha(0.92F * card.hoverValue(1F) + 0.08F))
		.effect(ShadowNodeEffect.create(ShowUI.AMBER, 26F).color(() -> ShowUI.AMBER.withAlpha(0.8F * card.hoverValue(1F))))
		.onClick(event -> player.toggle())
		.layer((node, mouseX, mouseY) -> this.icon(42D, 42D, card.hoverValue(1F)))
		.attach(card);
		RectNode
		.create(176, 712, 1060, 40)
		.color(Color.TRANSPARENT)
		.onClick(event -> this.scrub.set(ShowPlayer.ratio(event.getNode(), event.getMouseX())))
		.onHoverStart(event -> this.reveal.sequence(220F, 1F, TweenEquations.QUAD_OUT).start())
		.onHoverEnd(event -> this.reveal.sequence(220F, 0F, TweenEquations.QUAD_OUT).start())
		.onHover(event -> this.peek(ShowPlayer.ratio(event.getNode(), event.getMouseX()), player, preview))
		.handleMouseDragged(event -> {
			if (this.scrub.peek() >= 0D) {
				this.scrub.set(ShowPlayer.ratio(event.getNode(), event.getMouseX()));
			}
		})
		.handleMouseReleased(event -> {
			if (this.scrub.peek() >= 0D) {
				player.seek(this.scrub.peek() * player.getDuration());
				this.scrub.set(-1D);
			}
		})
		.layer((node, mouseX, mouseY) -> this.bar(0D, 20D, 1060D, card.hoverValue(1F)))
		.attach(card);
		TextNode.create(1384, 732).text(() -> Text.create(ShowPlayer.clock(this.shown() * Math.max(1D, player.getDuration())) + " / " + ShowPlayer.clock(player.getDuration()), ShowUI.font(FontWeight.SEMI_BOLD, 22F, ShowUI.TEXT.withAlpha(0.4F + 0.6F * card.hoverValue(1F))))).anchorX(Align.END).anchorY(Align.CENTER).attach(card);
	}

	private void preview(final Node card, final ResourcePlayerNode player, final ResourcePlayerNode preview) {
		RectNode
		.create(0, 456, 336, 236)
		.color(ShowUI.NIGHT.withAlpha(0.94F))
		.<RectNode>x(() -> Math.max(16D, Math.min(1088D, 8D + this.hover.get() * 1060D)))
		.visible(() -> this.reveal.get() > 0.01F)
		.effect(ShadowNodeEffect.create(Color.BLACK.withAlpha(0.55F), 32F).offsetY(14D))
		.effect(BorderNodeEffect.create(Color.WHITE.withAlpha(0.14F), 2F).mode(BorderMode.IN))
		.effect(RoundedNodeEffect.create(18F).scope(NodeEffectScope.CHILDREN))
		.effect(TransformNodeEffect.create(Transformation.create().translate(Vector.create(() -> 0D, () -> 16D * (1D - this.reveal.get()))).scale(Scale.create(() -> 0.92D + 0.08D * this.reveal.get(), () -> 0.92D + 0.08D * this.reveal.get(), () -> 1D))))
		.body(thumb -> {
			preview.effect(RoundedNodeEffect.create(12F)).attach(thumb);
			TextNode.create(168, 212).text(() -> Text.create(ShowPlayer.clock(this.hover.get() * player.getDuration()), ShowUI.font(FontWeight.SEMI_BOLD, 22F, ShowUI.TEXT))).anchorX(Align.CENTER).anchorY(Align.CENTER).attach(thumb);
		})
		.attach(card);
	}

	private void peek(final double ratio, final ResourcePlayerNode player, final ResourcePlayerNode preview) {
		this.hover.set(ratio);
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		if (ratio != this.peeked && now - this.peekTime >= 200L) {
			this.peeked = ratio;
			this.peekTime = now;
			preview.seek(ratio * player.getDuration());
		}
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