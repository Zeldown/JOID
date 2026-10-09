package dev.joid.demo.ui.popup;

import dev.joid.demo.DemoFont;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlay;
import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteraction;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRender;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = false)
@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true), render = @UIDataOverlayRender(screens = true))
public class UIDemoOverlay extends UI {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoOverlay.INK);

		RectNode
		.create(1500, 500, 320, 260)
		.color(UIDemoOverlay.PLACEHOLDER)
		.draggable(DraggableProperty.screen())
		.body(rect -> {
			RectNode.create(100, 50, 120, 120).color(UIDemoOverlay.INK).hoveredColor(Color.WHITE).attach(rect);
			TextNode.create(160, 210).text(Text.create("Overlay", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}