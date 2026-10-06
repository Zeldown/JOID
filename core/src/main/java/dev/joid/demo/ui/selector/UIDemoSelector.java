package dev.joid.demo.ui.selector;

import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.selector.node.DemoSelectorNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;

public class UIDemoSelector extends UIDemo {

	@Override
	public void init() {
		DemoSelectorNode
		.create(1920 / 2 - 450, 1080 / 2 - 25, 400, 50)
		.onChange((node, color) -> System.out.println(color))
		.values(Color.WHITE, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)
		.attach(this);

		DemoSelectorNode
		.create(1920 / 2 + 50, 1080 / 2 - 25, 400, 50)
		.direction(SelectorDirection.UP)
		.onChange((node, color) -> System.out.println(color))
		.values(Color.WHITE, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)
		.attach(this);
	}

}