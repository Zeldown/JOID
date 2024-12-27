package be.zeldown.joid.demo.ui.simple;

import java.util.Arrays;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.core.data.UIData;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.structure.container.ContainerNode;
import be.zeldown.joid.lib.utils.align.Align;

@UIData(anchorX = Align.END, anchorY = Align.START)
public class UIDemoSimple extends UIDemo {

	@Override
	public void init() {
		final ContainerNode container = ContainerNode.create(0D, 0D, 1920D, 1080D);

		/* append children */
		container.body(() -> {
			RectNode.create(
					1920D / 4D,
					1080D / 4D,
					1920D / 2D,
					1080D / 2D
					)
			.color(Color.RED, Color.GREEN)
			.border(Color.GREEN, Color.RED, 3D, true)
			.body(n -> {
				final double childWidth = n.dw(3D);
				final double childHeight = n.dh(2D);

				RectNode.create(
						0D,
						n.dh(2D) - childHeight/2,
						childWidth,
						childHeight
						)
				.color(Color.RED, Color.WHITE)
				.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
				.hover(() -> "hover1")
				.attach(n);

				RectNode.create(
						n.aw(-childWidth),
						n.dh(2D) - childHeight/2,
						childWidth,
						childHeight
						)
				.color(Color.RED, Color.WHITE)
				.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
				.hover(() -> Arrays.asList("hover1", "hover2"))
				.body(n1 -> {
					RectNode.create(
							n1.dw(4D),
							n1.dh(4D),
							n1.dw(2D),
							n1.dh(2D)
							)
					.color(Color.RED, Color.MAGENTA)
					.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
					.hover(() -> Arrays.asList("hover1", "hover2", "hover3"))
					.attach(n1);
				})
				.attach(n);
			})
			.attach(container);
		});

		container.attach(this);
	}

	@Override
	public void preDraw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(0D, 0D, 1920D, 1080D, Color.BLUE.toGradient(Color.RED));
	}

	@Override
	public void postDraw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.BLUE, 10D);
	}

}