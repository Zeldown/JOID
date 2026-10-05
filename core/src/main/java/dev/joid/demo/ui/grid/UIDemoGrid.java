package dev.joid.demo.ui.grid;

import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.structure.grid.GridNode;

public class UIDemoGrid extends UIDemo {

	@Override
	public void init() {
		GridNode
		.create(10D, 10D, 500D, 500D)
		.verticalMargin(5D)
		.horizontalMargin(5D)
		.body(grid -> {
			for (int i=0;i<50;i++) {
				RectNode.create(0, 0, 50, 50).color(Color.RED).attach(grid);
			}
		})
		.attach(this);
	}

}