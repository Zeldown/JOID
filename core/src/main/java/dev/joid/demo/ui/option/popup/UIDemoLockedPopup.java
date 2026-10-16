package dev.joid.demo.ui.option.popup;

import dev.joid.demo.DemoFont;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, closeable = false)
public class UIDemoLockedPopup extends UIDemoOptionPopup {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		super.init();

		RectNode
		.create(1060, 580, 160, 50)
		.color(UIDemoLockedPopup.INK)
		.onClick(event -> JOID.forceClose(this))
		.label(Text.create("Force close", TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
		.attach(this);
	}

}