package dev.joid.demo.ui.checkbox;

import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.checkbox.node.DemoCheckboxNode;

public class UIDemoCheckbox extends UIDemo {

	@Override
	public void init() {
		DemoCheckboxNode
		.create(1920 / 2 - 50, 1080 / 2 - 50, 100, 100)
		.onChange((node, checked) -> System.out.println("checked: " + checked))
		.attach(this);
	}

}