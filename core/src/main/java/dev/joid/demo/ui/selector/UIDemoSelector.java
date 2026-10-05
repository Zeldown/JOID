package dev.joid.demo.ui.selector;

import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.selector.node.DemoSelectorNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.selector.SelectorNode.SelectorDirection;

public class UIDemoSelector extends UIDemo {

	@Override
	public void init() {
		DemoSelectorNode
		.create(1920 / 2 - 450, 1080 / 2 - 25, 400, 50)
		.onChange((node, selected) -> System.out.println(selected))
		.body(selector -> {
			RectNode.create(0, 0, 400, 50).color(Color.WHITE).attach(selector);
			RectNode.create(0, 0, 400, 50).color(Color.RED).attach(selector);
			RectNode.create(0, 0, 400, 50).color(Color.GREEN).attach(selector);
			RectNode.create(0, 0, 400, 50).color(Color.BLUE).attach(selector);
		})
		.attach(this);

		DemoSelectorNode
		.create(1920 / 2 + 50, 1080 / 2 - 25, 400, 50)
		.direction(SelectorDirection.UP)
		.onChange((node, selected) -> System.out.println(selected))
		.body(selector -> {
			RectNode.create(0, 0, 400, 50).color(Color.WHITE).attach(selector);
			RectNode.create(0, 0, 400, 50).color(Color.RED).attach(selector);
			RectNode.create(0, 0, 400, 50).color(Color.GREEN).attach(selector);
			RectNode.create(0, 0, 400, 50).color(Color.BLUE).attach(selector);
		})
		.attach(this);
	}

}