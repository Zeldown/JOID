package dev.joid.demo.ui.option.popup;

import dev.joid.demo.DemoFont;
import dev.joid.lib.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
@UIDataPopup(active = true)
public class UIDemoOptionPopup extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 30).color(UIDemoOptionPopup.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);

		RectNode
		.create(640, 390, 640, 300)
		.color(UIDemoOptionPopup.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(320, 100).text(Text.create(super.getClass().getSimpleName(), info).align(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			RectNode
			.create(260, 190, 120, 50)
			.color(UIDemoOptionPopup.INK)
			.onClick(event -> JOID.close(this))
			.label(Text.create("Close", label))
			.attach(rect);
		})
		.attach(this);
	}

}