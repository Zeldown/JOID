package dev.joid.demo.ui.popup;

import java.util.Random;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.textfield.node.DemoTextFieldNode;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

@UIDataPopup(active = true)
public class UIDemoPopup extends UI {

	@Override
	public void init() {
		final Random random = new Random(BridgeHandler.CLOCK.get().currentTimeMillis());

		DemoTextFieldNode
		.create(random.nextInt(1920 - 500) + 50, random.nextInt(1080 - 100) + 50, 400)
		.info(TextInfo.create(DemoFont.MONTSERRAT, 30, Color.WHITE))
		.placeholder("Placeholder")
		.onChange((node, oldText, newText) -> System.out.println("Text: " + oldText + " -> " + newText))
		.attach(this);
	}

	@Override
	public void keyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
		if (!context.isCancelled() && key == Key.K && Key.LEFT_CONTROL.isDown()) {
			context.cancel(() -> JOID.open(new UIDemoPopup()));
		}
	}

}