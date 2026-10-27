package com.example.style;

import com.example.ExampleFont;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class AnimationUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	// #region follow-field
	private double followX = 160D;
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(AnimationUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		// #region appear
		final TweenAnimator appear = TweenAnimator.create(0F).sequence(600F, 1F, TweenEquations.CUBIC_OUT).start();

		RectNode
		.create(160, 0, 300, 180)
		.color(() -> Color.WHITE.withAlpha(appear.get()))
		.y(() -> 140D - 60D * appear.get())
		.attach(this);
		// #endregion

		// #region loop
		final TweenAnimator loop = TweenAnimator
		.create(0F)
		.sequence(600F, 1F, TweenEquations.QUAD_OUT)
		.push(600F, 0F, TweenEquations.QUAD_IN)
		.repeat(BaseTween.INFINITY, 200F)
		.start();

		RectNode
		.create(620, 120, 320, 60)
		.color(Color.WHITE)
		.body(track -> {
			RectNode.create(0, 0, 60, 60).color(AnimationUI.INK).x(() -> 260D * loop.get()).attach(track);
		})
		.onDetach(event -> loop.clear())
		.attach(this);
		// #endregion

		// #region click
		final IntegerSignal plays = new IntegerSignal(0);
		final TweenAnimator toggle = TweenAnimator.create(0F).onComplete(tween -> plays.increment());

		RectNode
		.create(1100, 120, 320, 60)
		.color(Color.WHITE)
		.body(track -> {
			RectNode.create(0, 0, 60, 60).color(AnimationUI.INK).x(() -> 260D * toggle.get()).attach(track);
		})
		.attach(this);

		RectNode
		.create(1100, 220, 200, 60)
		.color(AnimationUI.INK)
		.onClick(event -> {
			final float target = toggle.peek() < 0.5F ? 1F : 0F;
			toggle.sequence(400F, target, TweenEquations.QUART_OUT).start();
		})
		.label(Text.create("Toggle", label))
		.attach(this);
		// #endregion

		TextNode.create(1330, 232).text(Text.create(plays.map(count -> "Plays: " + count), info)).attach(this);

		RectNode.create(160, 500, 840, 120).color(Color.WHITE).attach(this);

		// #region follow-node
		CircleNode.create(0, 520, 80).color(AnimationUI.INK).x(() -> this.followX).attach(this);
		// #endregion
	}

	// #region follow-update
	@Override
	public void update() {
		final double target = Math.max(160D, Math.min(920D, super.getMouseX() - 40D));
		this.followX = super.lerpByFramerate(this.followX, target, 0.1D, 0.5D);
	}
	// #endregion

}