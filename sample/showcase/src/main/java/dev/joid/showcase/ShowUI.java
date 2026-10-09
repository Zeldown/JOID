package dev.joid.showcase;

import javax.vecmath.Vector4f;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.DemoPushTransition;
import dev.joid.internal.JOID;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.shader.impl.BorderShader.BorderMode;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BlurNodeEffect;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public abstract class ShowUI extends UI {

	public static final Color NIGHT   = Color.decode("#07071A");
	public static final Color DUSK    = Color.decode("#170A33");
	public static final Color VIOLET  = Color.decode("#8B5CF6");
	public static final Color FUCHSIA = Color.decode("#E879F9");
	public static final Color PINK    = Color.decode("#F472B6");
	public static final Color CYAN    = Color.decode("#22D3EE");
	public static final Color SKY     = Color.decode("#38BDF8");
	public static final Color AMBER   = Color.decode("#FBBF24");
	public static final Color ORANGE  = Color.decode("#FB923C");
	public static final Color EMERALD = Color.decode("#34D399");
	public static final Color TEXT    = Color.decode("#F8FAFC");
	public static final Color MUTED   = Color.decode("#A1A1AA");
	public static final Color FAINT   = Color.decode("#71717A");

	private long start;

	@Override
	public void init() {
		this.start = BridgeHandler.CLOCK.get().currentTimeMillis();
		RectNode.create(0, 0, 1920, 1080).color(Color.decode("#18181B")).attach(this);
		this.scene();

		this.keybind(() -> this.navigate(1), Key.PAGE_DOWN);
		this.keybind(() -> this.navigate(-1), Key.PAGE_UP);
		this.keybind(this::menu, Key.ESCAPE);
	}

	protected abstract void scene();

	protected double t() {
		return (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 1000D;
	}

	protected double wave(final double period, final double phase) {
		return Math.sin((this.t() / period + phase) * Math.PI * 2D);
	}

	protected double ease(final TweenEquation equation, final double from, final double to) {
		final double t = this.t();
		if (t <= from) {
			return 0D;
		}
		if (t >= to) {
			return 1D;
		}
		return equation.compute((float) ((t - from) / (to - from)));
	}

	protected void backdrop() {
		RectNode.create(0, 0, 1920, 1080).color(ShowUI.diagonal(ShowUI.NIGHT, ShowUI.DUSK)).attach(this);
	}

	protected CircleNode blob(final double x, final double y, final double size, final Color color, final double period, final double phase, final double range) {
		return CircleNode
		.create(x, y, size)
		.color(color)
		.<CircleNode>x(() -> x + this.wave(period, phase) * range)
		.<CircleNode>y(() -> y + this.wave(period, phase + 0.25D) * range * 0.6D)
		.effect(BlurNodeEffect.create((float) (size / 6D)))
		.attach(this);
	}

	protected RectNode glass(final double x, final double y, final double width, final double height, final float radius) {
		return RectNode
		.create(x, y, width, height)
		.color(ShowUI.vertical(Color.WHITE.copyAlpha(0.10F), Color.WHITE.copyAlpha(0.04F)))
		.effect(RoundedNodeEffect.create(radius))
		.effect(BorderNodeEffect.create(Color.WHITE.copyAlpha(0.14F), 1.5F, BorderMode.IN))
		.effect(ShadowNodeEffect.create(Color.BLACK.copyAlpha(0.45F), 48F, 0D, 24D));
	}

	private void navigate(final int offset) {
		final ShowScene scene = ShowScene.of(this);
		final int index = scene != null ? scene.ordinal() : offset > 0 ? -1 : 0;
		ShowUI.open(ShowScene.values()[Math.floorMod(index + offset, ShowScene.values().length)].create());
	}

	private void menu() {
		if (ShowScene.of(this) != null) {
			ShowUI.open(new ShowMenu());
		}
	}

	protected static void open(final UI ui) {
		ui.setTransition(new DemoPushTransition());
		JOID.open(ui);
	}

	protected static TextInfo font(final FontWeight weight, final float size, final Color color) {
		return TextInfo.create(DemoFont.MONTSERRAT, weight, size, color);
	}

	protected static Color vertical(final Color top, final Color bottom) {
		return top.toGradient(bottom, new Vector4f(0F, 0F, 0F, 1F));
	}

	protected static Color diagonal(final Color from, final Color to) {
		return from.toGradient(to, new Vector4f(0F, 0F, 1F, 1F));
	}

}