package com.example.describing.position;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class HideUI extends UI {

	private static final Color TEXT   = new Color(231, 233, 238).lock();
	private static final Color MUTED  = new Color(139, 147, 163).lock();
	private static final Color ACCENT = new Color(91, 140, 255).lock();
	private static final Color ACTIVE = new Color(125, 164, 255).lock();

	private final BooleanSignal shown = new BooleanSignal(true);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(HideUI.TEXT);

		// #region hide
		RectNode
		.create(660, 440, 180, 180)
		.color(HideUI.ACCENT)
		.hoveredColor(HideUI.ACTIVE)
		.visible(this.shown)
		.attach(this);

		RectNode
		.create(870, 440, 180, 180)
		.color(HideUI.ACCENT)
		.hoveredColor(HideUI.ACTIVE)
		.enabled(false)
		.attach(this);

		RectNode.create(1080, 440, 180, 180).color(HideUI.MUTED).onClick(event -> this.shown.toggle()).attach(this);
		// #endregion

		TextNode.create(750, 650).text(Text.create("Shown", info)).anchorX(Align.CENTER).attach(this);

		TextNode.create(960, 650).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(this);

		TextNode.create(1170, 650).text(Text.create("Toggle", info)).anchorX(Align.CENTER).attach(this);
	}

}