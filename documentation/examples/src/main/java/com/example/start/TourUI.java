package com.example.start;

import com.example.ExampleFont;

import dev.joid.lib.JOID;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.BaseTween;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class TourUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();
	private static final Color SHADOW  = new Color(0, 0, 0, 153).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	// #region signals
	private final IntegerSignal           clicks = new IntegerSignal(0);
	private final ComputedSignal<Boolean> many   = this.clicks.map(count -> count >= 3);
	// #endregion

	// #region property
	@UIProperty
	private int visits;
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(TourUI.TEXT);
		// #region animator
		final TweenAnimator slide = TweenAnimator
		.create(0F)
		.sequence(800F, 1F, TweenEquations.SINE_IN_OUT)
		.repeatYoyo(BaseTween.INFINITY, 0F)
		.start();
		// #endregion
		// #region visits
		this.visits++;
		// #endregion

		RectNode
		.create(40, 40, 420, 300)
		.color(TourUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TourUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region flex
			FlexNode
			.horizontal(60, 110, 80)
			.margin(20D)
			.body(flex -> {
				RectNode.create(0, 0, 80, 80).color(TourUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 80, 80).color(TourUI.ACCENT).attach(flex);
				RectNode.create(0, 0, 80, 80).color(TourUI.ACCENT).attach(flex);
			})
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(500, 40, 420, 300)
		.color(TourUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TourUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region image
			ResourceNode
			.create(110, 50, 200, 200)
			.resource(Resource.of("images/placeholder.png"))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(960, 40, 420, 300)
		.color(TourUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TourUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region effects
			RectNode
			.create(110, 70, 200, 160)
			.color(TourUI.ACCENT)
			.effect(RoundedNodeEffect.create(24F))
			.effect(ShadowNodeEffect.create(TourUI.SHADOW, 24F).offsetY(8D))
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(1420, 40, 420, 300)
		.color(TourUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TourUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region animation
			RectNode
			.create(60, 110, 80, 80)
			.color(TourUI.ACCENT)
			.x(() -> 60D + 220D * slide.get())
			.attach(card);
			// #endregion
		})
		.onDetach(event -> slide.clear())
		.attach(this);

		RectNode
		.create(40, 380, 420, 300)
		.color(TourUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TourUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region click
			RectNode
			.create(110, 100, 200, 100)
			.color(this.many.get() ? TourUI.ACCENT : TourUI.ELEMENT)
			.onClick(event -> this.clicks.increment())
			.attach(card);
			// #endregion
		})
		.attach(this);

		RectNode
		.create(500, 380, 420, 300)
		.color(TourUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(TourUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region popup
			RectNode
			.create(110, 100, 200, 100)
			.color(TourUI.ELEMENT)
			.onClick(event -> JOID.open(new NoticeUI()))
			.attach(card);
			// #endregion
			TextNode
			.create(210, 240)
			.text(Text.create("Visit " + this.visits, info))
			.anchorX(Align.CENTER)
			.attach(card);
		})
		.attach(this);
	}

}