package com.example.running;

import com.example.ExampleFont;
import com.example.capture.CaptureStyle;

import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.core.data.UIDataOverlayInteraction;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true))
public class HostOverlayUI extends UI {

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(CaptureStyle.TEXT);

		RectNode
		.create(40, 40, 200, 80)
		.color(CaptureStyle.STROKE)
		.onClick(event -> HostApp.togglePause())
		.body(rect -> TextNode.create(0, 0, 200, 80).text(Text.create("Pause", info).align(Align.CENTER)).attach(rect))
		.attach(this);
	}

}