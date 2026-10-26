package dev.joid.showcase;

import java.util.Locale;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

public class ShowDesign extends ShowUI {

	private final StringSignal  name     = new StringSignal("Ada Lovelace");
	private final DoubleSignal  volume   = new DoubleSignal(0.3D);
	private final BooleanSignal paper    = new BooleanSignal(false);
	private final TweenAnimator level    = TweenAnimator.create(0F);
	private final BooleanSignal notify   = new BooleanSignal(false);
	private final BooleanSignal autoplay = new BooleanSignal(true);

	private long saved;

	@Override
	protected void scene() {
		RectNode
		.create(0, 0, 1920, 1080)
		.color(Color.TRANSPARENT)
		.visible(() -> this.front() < 1920D)
		.body(layer -> this.screen(new ShowNeonKit(), layer))
		.attach(this);

		RectNode
		.create(0, 0, 1920, 1080)
		.color(Color.TRANSPARENT)
		.<RectNode>width(() -> this.front())
		.visible(() -> this.front() > 0.5D)
		.overflow(OverflowProperty.HIDDEN)
		.body(clip -> {
			RectNode.create(0, 0, 1920, 1080).color(Color.TRANSPARENT).body(layer -> this.screen(new ShowPaperKit(), layer)).attach(clip);
		})
		.attach(this);

		RectNode
		.create(0, 0, 6, 1080)
		.color(Color.WHITE)
		.<RectNode>x(() -> this.front() - 3D)
		.visible(() -> this.level.get() > 0.001F && this.level.get() < 0.999F)
		.interactive(false)
		.effect(ShadowNodeEffect.create(ShowUI.FUCHSIA, 40F).color(() -> ShowUI.FUCHSIA.to(ShowPaperKit.TOMATO, this.level.get())))
		.attach(this);
	}

	private void screen(final IKit kit, final Node layer) {
		kit.backdrop(layer);

		TextNode.create(150, 236).text(Text.create("Same code.", kit.display())).attach(layer);
		TextNode.create(150, 386).text(Text.create("Two kits.", kit.accent())).attach(layer);
		TextNode.create(156, 590).text(Text.create("One screen class, one set of signals.", kit.hint())).attach(layer);
		TextNode.create(156, 632).text(Text.create("The kit decides every pixel.", kit.hint())).attach(layer);
		kit.choice(156, 720, "Neon", () -> !this.paper.get()).onClick(event -> this.paper(false)).attach(layer);
		kit.choice(366, 720, "Paper", () -> this.paper.get()).onClick(event -> this.paper(true)).attach(layer);

		kit.panel(980, 140, 800, 800).attach(layer);
		TextNode.create(1040, 186).text(Text.create("Settings", kit.title())).attach(layer);
		TextNode.create(1042, 258).text(Text.create("Playback and profile", kit.hint())).attach(layer);
		kit.divider(1040, 316, 680).attach(layer);

		TextNode.create(1040, 366).text(Text.create("Notifications", kit.label())).anchorY(Align.CENTER).attach(layer);
		kit.toggle(1644, 345).toggle(this.notify).attach(layer);
		TextNode.create(1040, 452).text(Text.create("Autoplay videos", kit.label())).anchorY(Align.CENTER).attach(layer);
		kit.toggle(1644, 431).toggle(this.autoplay).attach(layer);
		kit.divider(1040, 506, 680).attach(layer);

		TextNode.create(1040, 554).text(Text.create("Volume", kit.label())).anchorY(Align.CENTER).attach(layer);
		TextNode.create(1720, 554).text(() -> Text.create(String.format(Locale.US, "%d %%", Math.round(this.volume.get() * 100D)), kit.label())).anchorX(Align.END).anchorY(Align.CENTER).attach(layer);
		kit.slider(1040, 584, 680).range(0D, 1D, 0.01D).value(this.volume).attach(layer);

		TextNode.create(1040, 674).text(Text.create("Display name", kit.label())).anchorY(Align.CENTER).attach(layer);
		kit.field(1040, 704, 680).text(this.name).attach(layer);

		kit.button(1040, 826, 320, () -> "Reset", false).onClick(event -> this.reset()).attach(layer);
		kit.button(1400, 826, 320, () -> this.isSaved() ? "Saved" : "Save changes", true).onClick(event -> this.saved = BridgeHandler.CLOCK.get().currentTimeMillis()).attach(layer);
	}

	private void reset() {
		this.notify.set(false);
		this.autoplay.set(true);
		this.volume.set(0.3D);
	}

	private void paper(final boolean paper) {
		this.paper.set(paper);
		this.level.sequence(1100F, paper ? 1F : 0F).start();
	}

	private boolean isSaved() {
		return this.saved != 0L && BridgeHandler.CLOCK.get().currentTimeMillis() - this.saved < 1600L;
	}

	private double eased() {
		final double progress = this.level.get();
		return progress * progress * (3D - 2D * progress);
	}

	private double front() {
		return 1920D * this.eased();
	}

}