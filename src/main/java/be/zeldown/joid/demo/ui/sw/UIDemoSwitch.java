package be.zeldown.joid.demo.ui.sw;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.sw.node.DemoSwitchNode;

public class UIDemoSwitch extends UIDemo {

	@Override
	public void init() {
		DemoSwitchNode
		.create(1920 / 2 - 400 / 2, 1080 / 2 - 50, 400, 50)
		.state("state 1", "state 2", "state 3")
		.onChange((node, value) -> System.out.println("value: " + value))
		.attach(this);
	}

}