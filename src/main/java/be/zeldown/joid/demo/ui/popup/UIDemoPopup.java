package be.zeldown.joid.demo.ui.popup;

import java.util.Random;

import org.lwjgl.input.Keyboard;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.textfield.node.DemoTextFieldNode;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.core.data.UIData;
import be.zeldown.joid.lib.ui.core.data.popup.UIDataPopup;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

@UIData(background = true, popup = @UIDataPopup(active = true))
public class UIDemoPopup extends UI {

	@Override
	public void init() {
		final Random random = new Random();

		DemoTextFieldNode
		.create(random.nextInt(1920 - 500) + 50, random.nextInt(1080 - 100) + 50, 400)
		.info(TextInfo.create(DemoFont.MONTSERRAT, 30, Color.WHITE))
		.placeholder("Placeholder")
		.onChange((node, oldText, newText) -> System.out.println("Text: " + oldText + " -> " + newText))
		.attach(this);
	}

	@Override
	public void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {
		if (!context.isCancelled() && keyCode == Keyboard.KEY_K && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			context.cancel(() -> JOID.open(new UIDemoPopup()));
		}
	}

}