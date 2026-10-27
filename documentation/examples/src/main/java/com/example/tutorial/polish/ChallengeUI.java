package com.example.tutorial.polish;

import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ChallengeUI extends UI {

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(Theme.FONT, 36F).color(Color.WHITE);
		final TweenAnimator intro = TweenAnimator.create(0F).sequence(700F, 1F, TweenEquations.CUBIC_OUT).start();

		// #region solution
		FlexNode
		.vertical(160, 420, 360)
		.margin(20D)
		.x(() -> 160D - 520D * (1D - intro.get()))
		// #endregion
		.body(flex -> {
			RectNode.create(0, 0, 360, 80).color(Theme.INK).label(Text.create("Play", label)).attach(flex);
			RectNode.create(0, 0, 360, 80).color(Theme.INK).label(Text.create("Settings", label)).attach(flex);
			RectNode.create(0, 0, 360, 80).color(Theme.INK).label(Text.create("Quit", label)).attach(flex);
		})
		.attach(this);
	}

}