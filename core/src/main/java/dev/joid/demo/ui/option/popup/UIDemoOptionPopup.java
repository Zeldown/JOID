package dev.joid.demo.ui.option.popup;

import dev.joid.demo.DemoFont;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
@UIDataPopup(active = true)
public class UIDemoOptionPopup extends UI {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 30, UIDemoOptionPopup.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);

		RectNode
		.create(640, 390, 640, 300)
		.color(UIDemoOptionPopup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(320, 100).text(Text.create(super.getClass().getSimpleName(), info, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(rect);
			RectNode
			.create(260, 190, 120, 50)
			.color(UIDemoOptionPopup.INK)
			.onClick(event -> JOID.close(this))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Close", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
		})
		.attach(this);
	}

}