package dev.joid.showcase;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class ShowReactive extends ShowUI {

	private final DoubleSignal  energy = DoubleSignal.of(0.35D);
	private final DoubleSignal  warmth = DoubleSignal.of(0.2D);
	private final DoubleSignal  space  = DoubleSignal.of(0.3D);
	private final BooleanSignal halo   = BooleanSignal.of(false);

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-300, 200, 900, ShowUI.VIOLET.copyAlpha(0.45F), 9D, 0D, 50D);
		this.blob(1300, -300, 900, ShowUI.CYAN.copyAlpha(0.22F), 9D, 0.4D, 50D);
		this.blob(1100, 700, 700, ShowUI.PINK.copyAlpha(0.22F), 9D, 0.7D, 50D);

		this.mixer();
		this.stage();
	}

	private void mixer() {
		this.glass(110, 120, 700, 840, 32F).attach(this);
		TextNode.create(156, 164).text(Text.create("Mixer", ShowUI.font(FontWeight.EXTRA_BOLD, 44F, ShowUI.TEXT))).attach(this);
		TextNode.create(158, 226).text(Text.create("Three signals. Every node that reads one follows it.", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);

		TextNode.create(158, 304).text(Text.create("Energy", ShowUI.font(FontWeight.SEMI_BOLD, 26F, ShowUI.TEXT))).attach(this);
		TextNode.create(764, 304).text(Text.create(Math.round(this.energy.get() * 100D) + " %", ShowUI.font(FontWeight.BOLD, 26F, ShowUI.PINK))).anchorX(Align.END).attach(this);
		ShowSliderNode.create(156, 350, 610, ShowUI.FUCHSIA, ShowUI.PINK).values(0D, 1D, 0.01D, 0.35D).signal(this.energy).attach(this);

		TextNode.create(158, 444).text(Text.create("Warmth", ShowUI.font(FontWeight.SEMI_BOLD, 26F, ShowUI.TEXT))).attach(this);
		TextNode.create(764, 444).text(Text.create(Math.round(this.warmth.get() * 100D) + " %", ShowUI.font(FontWeight.BOLD, 26F, ShowUI.AMBER))).anchorX(Align.END).attach(this);
		ShowSliderNode.create(156, 490, 610, ShowUI.AMBER, ShowUI.ORANGE).values(0D, 1D, 0.01D, 0.2D).signal(this.warmth).attach(this);

		TextNode.create(158, 584).text(Text.create("Space", ShowUI.font(FontWeight.SEMI_BOLD, 26F, ShowUI.TEXT))).attach(this);
		TextNode.create(764, 584).text(Text.create(Math.round(this.space.get() * 100D) + " %", ShowUI.font(FontWeight.BOLD, 26F, ShowUI.CYAN))).anchorX(Align.END).attach(this);
		ShowSliderNode.create(156, 630, 610, ShowUI.SKY, ShowUI.CYAN).values(0D, 1D, 0.01D, 0.3D).signal(this.space).attach(this);

		TextNode.create(158, 734).text(Text.create("Halo", ShowUI.font(FontWeight.SEMI_BOLD, 26F, ShowUI.TEXT))).attach(this);
		TextNode.create(158, 770).text(Text.create(this.halo.get() ? "On, glowing behind the orb" : "Off", ShowUI.font(FontWeight.MEDIUM, 18F, ShowUI.MUTED))).attach(this);
		ShowSwitchNode.create(690, 736, ShowUI.VIOLET).signal(this.halo).attach(this);

		final Color[] cool = {ShowUI.VIOLET, ShowUI.SKY, ShowUI.CYAN, ShowUI.EMERALD, ShowUI.FUCHSIA};
		final Color[] warm = {ShowUI.PINK, ShowUI.ORANGE, ShowUI.AMBER, Color.decode("#F43F5E"), Color.decode("#FDE68A")};
		for (int i = 0; i < cool.length; i++) {
			final Color from = cool[i];
			final Color to = warm[i];
			RectNode
			.create(156 + i * 125, 842, 110, 72)
			.color(() -> Color.transition(from, to, this.warmth.get().floatValue()))
			.effect(RoundedNodeEffect.create(18F))
			.effect(ShadowNodeEffect.create(from, 10F).color(() -> Color.transition(from, to, this.warmth.get().floatValue()).copyAlpha(0.7F)).blur(() -> 4F + 30F * this.energy.get().floatValue()))
			.attach(this);
		}
	}

	private void stage() {
		this.glass(860, 120, 950, 840, 32F).attach(this);

		CircleNode
		.create(1335 - 330, 500 - 330, 660)
		.color(Color.transition(ShowUI.SKY, ShowUI.ORANGE, this.warmth.get().floatValue()).copyAlpha(0.55F))
		.visible(this.halo)
		.effect(BlurNodeEffect.create(70F))
		.attach(this);

		RectNode
		.create(0, 0, 0, 0)
		.layer((mouseX, mouseY) -> this.rings())
		.attach(this);

		RectNode
		.create(0, 0, 0, 0)
		.color(ShowUI.diagonal(Color.transition(ShowUI.CYAN, ShowUI.AMBER, this.warmth.get().floatValue()), Color.transition(ShowUI.VIOLET, ShowUI.PINK, this.warmth.get().floatValue())))
		.x(1335D - 100D - 110D * this.space.get())
		.y(500D - 100D - 110D * this.space.get())
		.width(200D + 220D * this.space.get())
		.height(200D + 220D * this.space.get())
		.effect(CircleNodeEffect.create())
		.effect(ShadowNodeEffect.create(ShowUI.CYAN, 20F).color(() -> Color.transition(ShowUI.CYAN, ShowUI.PINK, this.warmth.get().floatValue())).blur(() -> 20F + 90F * this.energy.get().floatValue()))
		.attach(this);

		TextNode.create(910, 160).text(Text.create(String.valueOf(Math.round(this.energy.get() * 100D)), ShowUI.font(FontWeight.BLACK, 96F, ShowUI.TEXT))).attach(this);
		TextNode.create(910, 270).text(Text.create("energy, in %", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(this);

		for (int i = 0; i < 36; i++) {
			final int index = i;
			RectNode
			.create(898 + i * 24, 0, 14, 0)
			.color(() -> ShowUI.vertical(Color.transition(ShowUI.FUCHSIA, ShowUI.AMBER, this.warmth.get().floatValue()), Color.transition(ShowUI.VIOLET, ShowUI.ORANGE, this.warmth.get().floatValue()).copyAlpha(0.35F)))
			.<RectNode>height(() -> this.bar(index))
			.<RectNode>y(() -> 915D - this.bar(index))
			.effect(RoundedNodeEffect.create(7F))
			.attach(this);
		}
	}

	private double bar(final int index) {
		final double u = index / 35D;
		final double shape = 0.55D + 0.45D * Math.sin(this.t() * 5.2D + index * 0.55D) * Math.cos(this.t() * 2.1D - index * 0.21D);
		return 14D + (40D + 110D * Math.sin(u * Math.PI)) * this.energy.get() * Math.max(0.1D, shape) * 1.4D;
	}

	private void rings() {
		final double radius = 100D + 110D * this.space.get();
		for (int ring = 1; ring <= 3; ring++) {
			final double r = radius + ring * (26D + 40D * this.space.get());
			final List<Vector2d> points = new ArrayList<>();
			for (int i = 0; i <= 120; i++) {
				final double angle = i / 120D * Math.PI * 2D + this.t() * 0.3D * ring;
				points.add(new Vector2d(1335D + Math.cos(angle) * r, 500D + Math.sin(angle) * r));
			}
			DrawUtils.SHAPE.drawLine(Color.WHITE.copyAlpha(0.16F / ring + 0.04F), 2F, points.toArray(new Vector2d[0]));
		}
	}

}