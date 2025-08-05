package be.zeldown.joid.demo.ui.draggable;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;

public class UIDemoDraggable extends UIDemo {

	@Override
	public void init() {
		final DraggableProperty drag = DraggableProperty.screen().type(DraggableType.COPY).snap(DraggableSnapType.OVERLAP);

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
		.create(100, 100, 1920 - 200, 1080 - 200)
		.color(Color.BLACK)
		.body(rect -> {
			RectNode
			.create(100, 100, rect.aw(-200), rect.ah(-200))
			.color(Color.WHITE)
			.body(rect2 -> {
				RectNode
				.create(rect2.dw(2) - 50, rect2.dh(2) - 50, 100, 100)
				.color(Color.RED)
				.draggable(drag)
				.onDragStart(node -> System.out.println("start"))
				.onDrag(node -> System.out.println("drag"))
				.onDragEnd(node -> System.out.println("end"))
				.onSnap((node, snapNode) -> System.out.println("snap on " + snapNode))
				.body(draggable -> {
					RectNode
					.create(draggable.dw(2) - 25, draggable.dh(2) - 25, 50, 50)
					.color(Color.GREEN)
					.attach(draggable);
				})
				.attach(rect2);
			})
			.attach(rect);
		})
		.attach(this);

	}

}