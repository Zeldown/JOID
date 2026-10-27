package dev.joid.demo.ui.popup;

import java.util.Random;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.node.impl.control.field.DemoTextFieldNode;
import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.shortcut.Shortcut;

@UIData(background = true)
@UIDataPopup(active = true)
public class UIDemoPopup extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final Random random = new Random(BridgeHandler.CLOCK.get().currentTimeMillis());

		DemoTextFieldNode
		.create(random.nextInt(1920 - 500) + 50, random.nextInt(1080 - 100) + 50, 400, 0)
		.info(TextInfo.create(DemoFont.MONTSERRAT, 30).color(UIDemoPopup.INK))
		.marginHorizontal(12D)
		.placeholder("Placeholder")
		.attach(this);

		Shortcut.create(Key.K).ctrl(true).overrideFocus(true).onTrigger(event -> JOID.open(new UIDemoPopup())).attach(this);
	}

}