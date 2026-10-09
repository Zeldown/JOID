package dev.joid.showcase;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.render.transform.operation.RotateTransformOperation;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.effect.impl.TransformNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ShowEffects extends ShowUI {

	private static final String[] NAMES = {"Rounded corners", "Gradient", "Inner border", "Drop shadow", "Glow", "Blur", "Tilt"};

	private final BooleanSignal[] switches = new BooleanSignal[ShowEffects.NAMES.length];
	private final double[]        levels   = new double[ShowEffects.NAMES.length];

	private long last;

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-300, 300, 900, ShowUI.VIOLET.copyAlpha(0.40F), 9D, 0D, 50D);
		this.blob(1250, -350, 900, ShowUI.PINK.copyAlpha(0.22F), 9D, 0.4D, 50D);
		this.blob(1200, 650, 800, ShowUI.CYAN.copyAlpha(0.18F), 9D, 0.7D, 50D);

		this.panel();
		this.card();

		RectNode.create(0, 0, 0, 0).onUpdate(node -> this.step()).attach(this);
	}

	private void panel() {
		this.glass(110, 120, 650, 840, 32F).attach(this);
		TextNode.create(156, 164).text(Text.create("Effects", ShowUI.font(FontWeight.EXTRA_BOLD, 44F, ShowUI.TEXT))).attach(this);
		TextNode.create(158, 226).text(Text.create("Stack them on any node, animate any value.", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);
		final Color[] dots = {ShowUI.VIOLET, ShowUI.PINK, ShowUI.SKY, ShowUI.AMBER, ShowUI.FUCHSIA, ShowUI.CYAN, ShowUI.EMERALD};
		for (int i = 0; i < ShowEffects.NAMES.length; i++) {
			this.switches[i] = BooleanSignal.of(false);
			final double y = 300D + i * 88D;
			final BooleanSignal on = this.switches[i];
			final Color dot = dots[i];
			CircleNode.create(158, y + 15D, 12).color(() -> on.get() ? dot : Color.WHITE.copyAlpha(0.18F)).effect(ShadowNodeEffect.create(dot, 10F).color(() -> dot.copyAlpha(on.get() ? 0.9F : 0F))).attach(this);
			TextNode.create(188, y + 21D).text(Text.create(ShowEffects.NAMES[i], ShowUI.font(FontWeight.SEMI_BOLD, 26F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(this);
			ShowSwitchNode.create(650, y, dot).signal(on).attach(this);
		}
	}

	private void card() {
		this.glass(860, 120, 950, 840, 32F).attach(this);
		RectNode
		.create(1035, 355, 600, 370)
		.color(() -> ShowUI.diagonal(Color.decode("#2A2A3C").to(ShowUI.VIOLET, this.level(1)), Color.decode("#2A2A3C").to(ShowUI.PINK, this.level(1))))
		.effect(RoundedNodeEffect.create(() -> 2F + 34F * this.level(0)).scope(NodeEffectScope.CHILDREN))
		.effect(BorderNodeEffect.create(Color.WHITE, 2F, BorderMode.IN).color(() -> Color.WHITE.copyAlpha(0.45F * this.level(2))))
		.effect(ShadowNodeEffect.create(Color.BLACK, 40F).color(this::shadow).blur(() -> 6F + 54F * Math.max(this.level(3), this.level(4)) + 20F * this.level(4)).offsetY(() -> 34D * this.level(3) * (1D - this.level(4))))
		.effect(TransformNodeEffect.create(new RotateTransformOperation(() -> this.level(6) * (-7D + 2D * this.wave(4D, 0D)), Rotation.ROLL, Vector.create(1335D, 540D))))
		.body(card -> {
			CircleNode.create(330, -160, 420).color(() -> ShowUI.AMBER.copyAlpha(0.85F * this.level(1))).effect(BlurNodeEffect.create(1F).radius(() -> 1F + 60F * this.level(5))).attach(card);
			CircleNode.create(-120, 160, 380).color(() -> ShowUI.CYAN.copyAlpha(0.7F * this.level(1))).effect(BlurNodeEffect.create(1F).radius(() -> 1F + 60F * this.level(5))).attach(card);
			CircleNode.create(260, 220, 300).color(() -> ShowUI.FUCHSIA.copyAlpha(0.8F * this.level(1))).effect(BlurNodeEffect.create(1F).radius(() -> 1F + 50F * this.level(5))).attach(card);
			TextNode.create(44, 40).text(Text.create("JOID", ShowUI.font(FontWeight.BLACK, 34F, Color.WHITE))).attach(card);
			TextNode.create(556, 46).text(Text.create("PLATINUM", ShowUI.font(FontWeight.BOLD, 16F, Color.WHITE.copyAlpha(0.8F)).letterSpacing(0.3F))).anchorX(Align.END).attach(card);
			RectNode.create(44, 128, 76, 58).color(ShowUI.diagonal(Color.decode("#FDE68A"), Color.decode("#D97706"))).effect(RoundedNodeEffect.create(12F)).attach(card);
			TextNode.create(44, 222).text(Text.create("4000  1234  5678  8000", ShowUI.font(FontWeight.SEMI_BOLD, 36F, Color.WHITE).letterSpacing(0.06F))).attach(card);
			TextNode.create(44, 300).text(Text.create("ADA LOVELACE", ShowUI.font(FontWeight.BOLD, 20F, Color.WHITE.copyAlpha(0.85F)).letterSpacing(0.2F))).attach(card);
			TextNode.create(556, 300).text(Text.create("10 / 30", ShowUI.font(FontWeight.BOLD, 20F, Color.WHITE.copyAlpha(0.85F)))).anchorX(Align.END).attach(card);
		})
		.attach(this);
	}

	private Color shadow() {
		final float level = Math.max(this.level(3), this.level(4));
		return Color.transition(Color.BLACK, ShowUI.FUCHSIA, this.level(4)).copyAlpha((0.6F + 0.3F * this.level(4)) * level);
	}

	private float level(final int index) {
		return (float) this.levels[index];
	}

	private void step() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final double delta = this.last == 0L ? 0D : (now - this.last) / 1000D;
		this.last = now;
		for (int i = 0; i < this.levels.length; i++) {
			final double target = this.switches[i].get() ? 1D : 0D;
			this.levels[i] += (target - this.levels[i]) * (1D - Math.exp(-delta * 7D));
		}
	}

}