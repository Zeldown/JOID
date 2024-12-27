package be.zeldown.joid.demo.ui.toggle;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.toggle.node.DemoToggleNode;

public class UIDemoToggle extends UIDemo {

	@Override
	public void init() {
		DemoToggleNode
		.create(1920 / 2 - 100, 1080 / 2 - 50, 200, 100)
		.state("on", "off")
		.onChange((node, toggle) -> System.out.println("value: " + node.getValue() + " (" + toggle + ")"))
		.attach(this);
	}

}