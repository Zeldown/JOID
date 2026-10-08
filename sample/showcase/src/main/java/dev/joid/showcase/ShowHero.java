package dev.joid.showcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4f;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.font.markup.DemoTextMarkup;
import dev.joid.internal.JOID;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.obj.OBJModel;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.model.ModelNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class ShowHero extends ShowUI {

	private final IntegerSignal plays    = IntegerSignal.of(12480);
	private final DoubleSignal  progress = DoubleSignal.of(0D);

	@Override
	protected void scene() {
		this.backdrop();
		this.blob(-260, -320, 1000, ShowUI.VIOLET.copyAlpha(0.55F), 6D, 0D, 60D);
		this.blob(1480, -260, 760, ShowUI.CYAN.copyAlpha(0.32F), 6D, 0.3D, 50D);
		this.blob(1080, 640, 720, ShowUI.PINK.copyAlpha(0.30F), 6D, 0.6D, 70D);
		this.blob(560, 820, 420, ShowUI.AMBER.copyAlpha(0.16F), 6D, 0.85D, 40D);

		this.brand();
		this.headline();
		this.queue();
		this.video();
		this.stats();
		this.model();
		this.toast();

		RectNode.create(0, 0, 0, 0).onUpdate(node -> this.plays.set(12480 + (int) (this.t() * 9D))).attach(this);
	}

	private void brand() {
		RectNode
		.create(120, 86, 60, 60)
		.color(ShowUI.diagonal(ShowUI.FUCHSIA, ShowUI.VIOLET))
		.effect(RoundedNodeEffect.create(18F))
		.effect(ShadowNodeEffect.create(ShowUI.VIOLET.copyAlpha(0.8F), 28F))
		.body(logo -> {
			TextNode.create(30, 30).text(Text.create("J", ShowUI.font(FontWeight.BLACK, 34F, Color.WHITE))).anchor(Align.CENTER).attach(logo);
		})
		.attach(this);
		TextNode.create(200, 116).text(Text.create("JOID", ShowUI.font(FontWeight.EXTRA_BOLD, 32F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(this);
		RectNode
		.create(306, 96, 92, 40)
		.color(Color.WHITE.copyAlpha(0.08F))
		.effect(RoundedNodeEffect.create(20F))
		.body(pill -> {
			TextNode.create(46, 20).text(Text.create("8.0.0", ShowUI.font(FontWeight.SEMI_BOLD, 18F, ShowUI.MUTED))).anchor(Align.CENTER).attach(pill);
		})
		.attach(this);
	}

	private void headline() {
		TextNode.create(112, 236).text(Text.create("Your design.", ShowUI.font(FontWeight.BLACK, 118F, ShowUI.TEXT))).attach(this);
		TextNode.create(112, 372).text(() -> Text.create("Any engine.", ShowUI.font(FontWeight.BLACK, 118F, this.shimmer()))).attach(this);
		final TextInfo sub = ShowUI.font(FontWeight.MEDIUM, 30F, ShowUI.MUTED).lineHeight(1.45F);
		TextNode.create(120, 540).text(Text.create("A design-neutral UI engine for Java: retained nodes,", sub)).attach(this);
		TextNode.create(120, 584).text(Text.create("reactive signals and GPU effects on any renderer.", sub)).attach(this);
		final String[] names = {"LWJGL 2", "LWJGL 3", "Vulkan", "Your engine"};
		final Color[] dots = {ShowUI.AMBER, ShowUI.EMERALD, ShowUI.PINK, ShowUI.CYAN};
		double x = 120D;
		for (int i = 0; i < names.length; i++) {
			final TextInfo chip = ShowUI.font(FontWeight.SEMI_BOLD, 22F, ShowUI.TEXT);
			final double width = chip.getWidth(names[i]) + 64D;
			final Color dot = dots[i];
			RectNode
			.create(x, 672, width, 52)
			.color(Color.WHITE.copyAlpha(0.07F))
			.effect(RoundedNodeEffect.create(26F))
			.body(pill -> {
				CircleNode.create(20, 20, 12).color(dot).effect(ShadowNodeEffect.create(dot, 10F)).attach(pill);
			})
			.attach(this);
			TextNode.create(x + 42D, 698).text(Text.create(names[i], chip)).anchorY(Align.CENTER).attach(this);
			x += width + 14D;
		}
	}

	private Color shimmer() {
		final float shift = (float) (this.wave(6D, 0D) * 0.35D);
		return ShowUI.FUCHSIA.toGradient(ShowUI.CYAN, new Vector4f(-0.1F + shift, 0F, 0.9F + shift, 0F));
	}

	private void queue() {
		this.glass(120, 790, 800, 196, 24F).attach(this);
		TextNode.create(150, 812).text(Text.create("UP NEXT", ShowUI.font(FontWeight.BOLD, 16F, ShowUI.FAINT).letterSpacing(0.25F))).attach(this);
		TextNode.create(890, 812).text(Text.create("3 tracks", ShowUI.font(FontWeight.MEDIUM, 16F, ShowUI.FAINT))).anchorX(Align.END).attach(this);
		this.track(0, "Neon Tide", "The Shallows", ShowUI.CYAN, ShowUI.VIOLET);
		this.track(1, "Velvet", "Rosa Mendes", ShowUI.PINK, ShowUI.FUCHSIA);
		this.track(2, "Aurora", "North Lights", ShowUI.EMERALD, ShowUI.SKY);
	}

	private void track(final int index, final String name, final String by, final Color from, final Color to) {
		final double x = 150D + index * 250D;
		RectNode
		.create(x, 858, 84, 84)
		.color(ShowUI.diagonal(from, to))
		.effect(RoundedNodeEffect.create(18F))
		.effect(ShadowNodeEffect.create(from.copyAlpha(0.45F), 18F, 0D, 6D))
		.body(art -> {
			TextNode.create(42, 42).text(Text.create(name.substring(0, 1), TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.REGULAR, 40F, Color.WHITE))).anchor(Align.CENTER).attach(art);
		})
		.attach(this);
		TextNode.create(x + 102D, 872).text(Text.create(name, ShowUI.font(FontWeight.SEMI_BOLD, 21F, ShowUI.TEXT))).attach(this);
		TextNode.create(x + 102D, 904).text(Text.create(by, ShowUI.font(FontWeight.MEDIUM, 17F, ShowUI.MUTED))).attach(this);
	}

	private void video() {
		final Resource clip = Resource.of(ShowHero.class.getResourceAsStream("/assets/showcase/honey.mp4"));
		RectNode
		.create(1010, 96, 620, 400)
		.color(ShowUI.NIGHT)
		.<RectNode>y(() -> 96D + this.wave(6D, 0.1D) * 8D)
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.55F), 60F, 0D, 30D))
		.effect(RoundedNodeEffect.create(30F).scope(NodeEffectScope.CHILDREN))
		.body(card -> {
			ResourcePlayerNode
			.create(0, 0, 620, 400)
			.resource(clip)
			.stretch(StretchType.COVER)
			.loop(true)
			.volume(0F)
			.onProgress((player, value, time) -> this.progress.set(value))
			.attach(card);
			RectNode.create(0, 170, 620, 230).color(ShowUI.vertical(Color.BLACK.copyAlpha(0F), Color.BLACK.copyAlpha(0.82F))).attach(card);
			RectNode
			.create(24, 24, 150, 38)
			.color(Color.BLACK.copyAlpha(0.35F))
			.effect(RoundedNodeEffect.create(19F))
			.body(pill -> {
				CircleNode.create(16, 14, 10).color(Color.decode("#F43F5E")).effect(ShadowNodeEffect.create(Color.decode("#F43F5E"), 8F)).attach(pill);
				TextNode.create(36, 19).text(Text.create("Now playing", ShowUI.font(FontWeight.SEMI_BOLD, 16F, ShowUI.TEXT))).anchorY(Align.CENTER).attach(pill);
			})
			.attach(card);
			TextNode.create(32, 282).text(Text.create("Golden Hour", TextInfo.create(DemoFont.PLAYFAIR_DISPLAY, FontWeight.BOLD, 46F, ShowUI.TEXT))).attach(card);
			TextNode.create(34, 340).text(Text.create("Slow motion  ·  honey and light", ShowUI.font(FontWeight.MEDIUM, 19F, ShowUI.MUTED))).attach(card);
			RectNode.create(32, 372, 476, 6).color(Color.WHITE.copyAlpha(0.18F)).effect(RoundedNodeEffect.create(3F)).attach(card);
			RectNode.create(32, 372, 0, 6).color(ShowUI.AMBER.toGradient(ShowUI.PINK)).width(476D * this.progress.get()).effect(RoundedNodeEffect.create(3F)).attach(card);
			CircleNode
			.create(536, 300, 64)
			.color(Color.WHITE)
			.effect(ShadowNodeEffect.create(ShowUI.AMBER.copyAlpha(0.9F), 26F))
			.layer((mouseX, mouseY) -> {
				final double cx = 571D;
				final double cy = 332D;
				DrawUtils.SHAPE.drawPolygon(ShowUI.NIGHT, new Vector2d(cx - 7D, cy - 12D), new Vector2d(cx - 7D, cy + 12D), new Vector2d(cx + 13D, cy));
			})
			.attach(card);
		})
		.attach(this);
	}

	private void stats() {
		RectNode
		.create(1010, 560, 500, 340)
		.color(ShowUI.vertical(Color.WHITE.copyAlpha(0.10F), Color.WHITE.copyAlpha(0.04F)))
		.<RectNode>y(() -> 560D + this.wave(6D, 0.45D) * 7D)
		.effect(RoundedNodeEffect.create(28F))
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.45F), 48F, 0D, 24D))
		.body(card -> {
			TextNode.create(32, 34).text(Text.create("Plays this week", ShowUI.font(FontWeight.MEDIUM, 20F, ShowUI.MUTED))).attach(card);
			TextNode.create(30, 70).text(Text.create(String.format(Locale.US, "%,d", this.plays.get()), ShowUI.font(FontWeight.BLACK, 64F, ShowUI.TEXT))).attach(card);
			RectNode
			.create(360, 36, 108, 36)
			.color(ShowUI.EMERALD.copyAlpha(0.16F))
			.effect(RoundedNodeEffect.create(18F))
			.body(pill -> {
				TextNode.create(54, 18).text(Text.create("+18.4 %", ShowUI.font(FontWeight.BOLD, 17F, ShowUI.EMERALD))).anchor(Align.CENTER).attach(pill);
			})
			.attach(card);
			RectNode
			.create(30, 170, 440, 140)
			.layer((mouseX, mouseY) -> this.chart(30D, 170D, 440D, 140D))
			.attach(card);
		})
		.attach(this);
	}

	private void chart(final double x, final double y, final double width, final double height) {
		final int count = 64;
		final List<Vector2d> line = new ArrayList<>();
		final List<Vector2d> area = new ArrayList<>();
		for (int i = 0; i <= count; i++) {
			final double u = i / (double) count;
			final double value = 0.45D + 0.22D * Math.sin(u * 7D + this.t() * Math.PI / 3D) + 0.14D * Math.sin(u * 13D - this.t() * Math.PI * 2D / 3D) + 0.18D * u;
			line.add(new Vector2d(x + u * width, y + height - value * height));
		}
		for (int i = 0; i < count; i++) {
			area.add(line.get(i));
			area.add(line.get(i + 1));
			area.add(new Vector2d(line.get(i + 1).x, y + height));
			area.add(new Vector2d(line.get(i).x, y + height));
		}
		DrawUtils.SHAPE.drawShape(DrawMode.QUADS, ShowUI.vertical(ShowUI.FUCHSIA.copyAlpha(0.45F), ShowUI.FUCHSIA.copyAlpha(0F)), area.toArray(new Vector2d[0]));
		DrawUtils.SHAPE.drawLine(ShowUI.FUCHSIA.toGradient(ShowUI.CYAN), 4F, line.toArray(new Vector2d[0]));
		final Vector2d last = line.get(count);
		DrawUtils.SHAPE.drawCircle(last.x, last.y, ShowUI.CYAN, 8D);
	}

	private void model() {
		final OBJModel model = OBJModel.load("showcase", JOID.class.getResourceAsStream("/assets/demo/models/model.obj"), Resource.of(ShowHero.class.getResourceAsStream("/assets/showcase/teapot.png")));
		RectNode
		.create(1550, 560, 300, 340)
		.color(ShowUI.vertical(Color.WHITE.copyAlpha(0.12F), Color.WHITE.copyAlpha(0.04F)))
		.<RectNode>y(() -> 560D + this.wave(6D, 0.7D) * 9D)
		.effect(RoundedNodeEffect.create(28F))
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.45F), 48F, 0D, 24D))
		.body(card -> {
			CircleNode.create(40, 20, 220).color(ShowUI.VIOLET.copyAlpha(0.7F)).effect(BlurNodeEffect.create(40F)).attach(card);
			ModelNode.create(30, 10, 240, 240).model(model).size(0.92D).rotationPitch(-18D).rotationYaw(() -> this.t() * 60D).attach(card);
			TextNode.create(30, 252).text(Text.create("teapot.obj", ShowUI.font(FontWeight.BOLD, 26F, ShowUI.TEXT))).attach(card);
			TextNode.create(30, 292).text(Text.create("Lit, textured, smooth", ShowUI.font(FontWeight.MEDIUM, 18F, ShowUI.MUTED))).attach(card);
		})
		.attach(this);
	}

	private void toast() {
		RectNode
		.create(1290, 940, 520, 100)
		.color(Color.decode("#1E1B3A").copyAlpha(0.92F))
		.<RectNode>x(() -> 1290D + 700D * (1D - this.ease(TweenEquations.BACK_OUT, 1D, 1.8D)) + 700D * this.ease(TweenEquations.QUAD_IN, 5.2D, 5.8D))
		.effect(RoundedNodeEffect.create(24F))
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.5F), 40F, 0D, 18D))
		.body(toast -> {
			RectNode
			.create(22, 22, 56, 56)
			.color(ShowUI.diagonal(ShowUI.PINK, ShowUI.AMBER))
			.effect(CircleNodeEffect.create())
			.body(avatar -> {
				TextNode.create(28, 28).text(Text.create("M", ShowUI.font(FontWeight.BLACK, 24F, Color.WHITE))).anchor(Align.CENTER).attach(avatar);
			})
			.attach(toast);
			TextNode.create(96, 26).text(Text.create("<b>Mia</b> added <c=f472b6>Golden Hour</c> to a playlist", ShowUI.font(FontWeight.MEDIUM, 19F, ShowUI.TEXT).markups(DemoTextMarkup.inst()))).attach(toast);
			TextNode.create(96, 58).text(Text.create("just now", ShowUI.font(FontWeight.MEDIUM, 16F, ShowUI.FAINT))).attach(toast);
		})
		.attach(this);
	}

}