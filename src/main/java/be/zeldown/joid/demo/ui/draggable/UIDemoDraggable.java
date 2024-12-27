package be.zeldown.joid.demo.ui.draggable;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;

public class UIDemoDraggable extends UIDemo {

	@Override
	public void init() {
		final DraggableProperty drag = DraggableProperty.screen().snap(DraggableSnapType.OVERLAP);

		drag.snap(
				RectNode
				.create(50, 50, 100, 100)
				.color(Color.BLUE)
				.attach(this)
				);

		drag.snap(
				RectNode
				.create(50, 200, 100, 100)
				.color(Color.BLUE)
				.attach(this)
				);

		drag.snap(
				RectNode
				.create(50, 350, 100, 100)
				.color(Color.BLUE)
				.attach(this)
				);

		RectNode
		.create(1920 / 2 - 50, 1080 / 2 - 50, 100, 100)
		.color(Color.RED)
		.draggable(drag)
		.onDragStart(node -> System.out.println("start"))
		.onDrag(node -> System.out.println("drag"))
		.onDragEnd(node -> System.out.println("end"))
		.onSnap((node, snapNode) -> System.out.println("snap on " + snapNode))
		.attach(this);
	}

}