package be.zeldown.joid.demo.ui.textfield;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.textfield.node.DemoMultilineTextFieldNode;
import be.zeldown.joid.demo.ui.textfield.node.DemoTextFieldNode;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.text.TextInfo;

public class UIDemoTextField extends UIDemo {

	@Override
	public void init() {
		DemoTextFieldNode
		.create(1920 / 2 - 200, 1080 / 2 - 25, 400)
		.info(TextInfo.create(DemoFont.MONTSERRAT, 30, Color.WHITE))
		.placeholder("Placeholder")
		.onChange((node, oldText, newText) -> System.out.println("Text: " + oldText + " -> " + newText))
		.attach(this);

		DemoMultilineTextFieldNode
		.create(1920 / 2 - 200, 1080 / 2 - 25 + 100, 700, 400)
		.info(TextInfo.create(DemoFont.MONTSERRAT, 30, Color.WHITE))
		.placeholder("Placeholder")
		.onChange((node, oldText, newText) -> System.out.println("Text: " + oldText + " -> " + newText))
		.attach(this);
	}

}