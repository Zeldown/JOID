package dev.joid.showcase;

import java.util.Locale;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class ShowDesign extends ShowUI {

	private final StringSignal  name     = StringSignal.of("Ada Lovelace");
	private final BooleanSignal paper    = BooleanSignal.of(false);
	private final DoubleSignal  volume   = DoubleSignal.of(0.3D);
	private final BooleanSignal notify   = BooleanSignal.of(false);
	private final BooleanSignal autoplay = BooleanSignal.of(true);

	private long   last;
	private long   saved;
	private double level;

	@Override
	protected void scene() {
		RectNode.create(0, 0, 1, 1).color(Color.TRANSPARENT).onUpdate(node -> this.step()).attach(this);
		final RectNode neon = RectNode.create(0, 0, 1920, 1080).color(Color.TRANSPARENT).visible(() -> this.right() - this.left() < 1920D);
		neon.body(layer -> this.screen(new ShowNeonKit(), layer)).attach(this);

		RectNode
		.create(0, 0, 1920, 1080)
		.color(Color.TRANSPARENT)
		.<RectNode>x(() -> this.left())
		.<RectNode>width(() -> this.right() - this.left())
		.visible(() -> this.right() - this.left() > 0.5D)
		.overflow(OverflowProperty.HIDDEN)
		.body(clip -> {
			RectNode.create(0, 0, 1920, 1080).color(Color.TRANSPARENT).<RectNode>x(() -> -this.left()).body(layer -> this.screen(new ShowPaperKit(), layer)).attach(clip);
		})
		.attach(this);

		RectNode
		.create(0, 0, 6, 1080)
		.color(Color.WHITE)
		.<RectNode>x(() -> this.front() - 3D)
		.visible(() -> this.level > 0.001D && this.level < 0.999D)
		.interactive(false)
		.effect(ShadowNodeEffect.create(ShowUI.FUCHSIA, 40F).color(() -> ShowUI.FUCHSIA.to(ShowPaperKit.TOMATO, (float) this.level)))
		.attach(this);
	}

	private void screen(final IShowKit kit, final Node layer) {
		kit.backdrop(layer);

		TextNode.create(150, 236).text(Text.create("Same code.", kit.display())).attach(layer);
		TextNode.create(150, 386).text(Text.create("Two kits.", kit.accent())).attach(layer);
		TextNode.create(156, 590).text(Text.create("One screen class, one set of signals.", kit.hint())).attach(layer);
		TextNode.create(156, 632).text(Text.create("The kit decides every pixel.", kit.hint())).attach(layer);
		kit.choice(156, 720, "Neon", () -> !this.paper.get()).onClick((node, mouseX, mouseY, clickType) -> this.paper.set(false)).attach(layer);
		kit.choice(366, 720, "Paper", () -> this.paper.get()).onClick((node, mouseX, mouseY, clickType) -> this.paper.set(true)).attach(layer);

		kit.panel(980, 140, 800, 800).attach(layer);
		TextNode.create(1040, 186).text(Text.create("Settings", kit.title())).attach(layer);
		TextNode.create(1042, 258).text(Text.create("Playback and profile", kit.hint())).attach(layer);
		kit.divider(1040, 316, 680).attach(layer);

		TextNode.create(1040, 366).text(Text.create("Notifications", kit.label())).anchorY(Align.CENTER).attach(layer);
		kit.toggle(1644, 345).signal(this.notify).attach(layer);
		TextNode.create(1040, 452).text(Text.create("Autoplay videos", kit.label())).anchorY(Align.CENTER).attach(layer);
		kit.toggle(1644, 431).signal(this.autoplay).attach(layer);
		kit.divider(1040, 506, 680).attach(layer);

		TextNode.create(1040, 554).text(Text.create("Volume", kit.label())).anchorY(Align.CENTER).attach(layer);
		TextNode.create(1720, 554).text(() -> Text.create(String.format(Locale.US, "%d %%", Math.round(this.volume.get() * 100D)), kit.label())).anchorX(Align.END).anchorY(Align.CENTER).attach(layer);
		kit.slider(1040, 584, 680).range(0D, 1D, 0.01D).signal(this.volume).attach(layer);

		TextNode.create(1040, 674).text(Text.create("Display name", kit.label())).anchorY(Align.CENTER).attach(layer);
		kit.field(1040, 704, 680).signal(this.name).attach(layer);

		kit.button(1040, 826, 320, () -> "Reset", false).onClick((node, mouseX, mouseY, clickType) -> this.reset()).attach(layer);
		kit.button(1400, 826, 320, () -> this.isSaved() ? "Saved" : "Save changes", true).onClick((node, mouseX, mouseY, clickType) -> this.saved = BridgeHandler.CLOCK.get().currentTimeMillis()).attach(layer);
	}

	private void reset() {
		this.notify.set(false);
		this.autoplay.set(true);
		this.volume.set(0.3D);
	}

	private boolean isSaved() {
		return this.saved != 0L && BridgeHandler.CLOCK.get().currentTimeMillis() - this.saved < 1600L;
	}

	private double eased() {
		return this.level * this.level * (3D - 2D * this.level);
	}

	private double front() {
		return 1920D * (this.paper.get() ? this.eased() : 1D - this.eased());
	}

	private double left() {
		return this.paper.get() ? 0D : this.front();
	}

	private double right() {
		return this.paper.get() ? this.front() : 1920D;
	}

	private void step() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final double delta = this.last == 0L ? 0D : (now - this.last) / 1000D;
		this.last = now;
		final double target = this.paper.get() ? 1D : 0D;
		this.level = target > this.level ? Math.min(target, this.level + delta / 1.1D) : Math.max(target, this.level - delta / 1.1D);
	}

}