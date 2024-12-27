package be.zeldown.joid.demo.ui.flex;


import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.utils.align.Align;

public class UIDemoFlex extends UIDemo {

	@Override
	public void init() {
		FlexNode
		.horizontal(1920 / 2, 1080 / 2 - 50, 100)
		.margin(10)
		.align(Align.CENTER)
		.body(flex -> {
			for (int i = 0; i < 3; i++) {
				RectNode.create(0, 0, 100, 100).color(Color.RED).attach(flex);
			}
		})
		.onClick((node, mouseX, mouseY, clickType) -> RectNode.create(0, 0, 100, 100).color(Color.RED).attach(node))
		.anchorX(Align.CENTER)
		.attach(this);

		FlexNode
		.vertical(1920 / 2 - 50, 1080 / 2, 100)
		.margin(10)
		.align(Align.CENTER)
		.body(flex -> {
			for (int i = 0; i < 3; i++) {
				RectNode.create(0, 0, 100, 100).color(Color.BLUE).attach(flex);
			}
		})
		.onClick((node, mouseX, mouseY, clickType) -> RectNode.create(0, 0, 100, 100).color(Color.BLUE).attach(node))
		.anchorY(Align.CENTER)
		.attach(this);
	}

}