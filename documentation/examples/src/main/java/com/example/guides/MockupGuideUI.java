package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.effect.impl.ShadowNodeEffect;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.utils.align.Align;

public class MockupGuideUI extends UI {

	@Override
	public void init() {
		// #region flag
		final BooleanSignal mockup = new BooleanSignal(false);
		// #endregion
		// #region text
		final TextInfo name = TextInfo
		.create(ExampleFont.REGULAR, 32F)
		.weight(FontWeight.BOLD)
		.letterSpacing(-0.02F)
		.color(Color.decode("#E7E9EE"));
		final TextInfo role = TextInfo
		.create(ExampleFont.REGULAR, 22F)
		.lineHeight(1.4F)
		.color(Color.decode("#8B93A3"));
		// #endregion

		// #region card
		RectNode
		.create(660, 290, 600, 500)
		.color(Color.decode("#171A21"))
		.effect(RoundedNodeEffect.create(24F))
		.effect(ShadowNodeEffect.create(Color.decode("#00000099"), 30F).offsetY(8D))
		.body(card -> {
			CircleNode.create(240, 60, 120).color(Color.decode("#262B35")).attach(card);
			TextNode.create(300, 230).text(Text.create("Ada Lovelace", name)).anchorX(Align.CENTER).attach(card);
			TextNode.create(300, 290).text(Text.create("Analyst", role)).anchorX(Align.CENTER).attach(card);
			RectNode
			.create(200, 380, 200, 64)
			.color(Color.decode("#5B8CFF"))
			.effect(RoundedNodeEffect.create(32F))
			.label(Text.create("Follow", role.withColor(Color.decode("#0E1014"))))
			.attach(card);
		})
		.attach(this);
		// #endregion

		// #region overlay
		ResourceNode
		.create(0, 0, 1920, 1080)
		.resource(Resource.of("/guides/mockup.png"))
		.color(Color.WHITE.withAlpha(0.5F))
		.visible(mockup)
		.interactive(false)
		.attach(this);

		Shortcut.create(Key.F2).onTrigger(event -> mockup.toggle()).attach(this);
		// #endregion
	}

}