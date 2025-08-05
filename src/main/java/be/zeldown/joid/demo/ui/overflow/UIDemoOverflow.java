package be.zeldown.joid.demo.ui.overflow;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.overflow.node.DemoScrollbarNode;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.ui.core.data.UIData;
import be.zeldown.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.structure.container.ContainerNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.box.BoundingBox;

@UIData(anchorY = Align.START, anchorX = Align.CENTER)
public class UIDemoOverflow extends UIDemo {

	@Override
	public void init() {
		final ContainerNode container = ContainerNode.create(0D, 0D, 1920D, 1080D);

		/* append children */
		container.body(() -> {
			RectNode.create(
					1920D / 4D,
					1080D / 4D,
					1920D / 2D,
					120D
					)
			.color(Color.BLUE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(0, -20D, 30D, 10D, BoundingBox.create(0, -20D, 1920D / 2D, 10D)))
			.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
			.onScrollEnd((node, scrollX, scrollY) -> {
				RectNode.create(
						0D,
						10D,
						100D,
						100D
						)
				.color(Color.RAINBOW())
				.body(rect -> {
					RectNode.create(
							0,
							0,
							rect.getWidth(),
							rect.getHeight()
							)
					.color(Color.GREEN)
					.effect(MaskNodeEffect.create(rect.getWidth(), 10D))
					.attach(rect);
				})
				.onClick((child, mouseX, mouseY, clickType) -> System.out.println(child))
				.attach(node.getChild(0, FlexNode.class));
			})
			.body(n -> {
				FlexNode
				.horizontal(10D, 0D, 120D)
				.margin(10D)
				.align(Align.CENTER)
				.body(flex -> {
					for (int i = 0; i < 20; i++) {
						RectNode.create(
								0D,
								0D,
								100D,
								100D
								)
						.color(Color.RED)
						.body(rect -> {
							RectNode.create(
									0,
									0,
									rect.getWidth(),
									rect.getHeight()
									)
							.color(Color.GREEN)
							.effect(MaskNodeEffect.create(rect.getWidth(), 10D))
							.attach(rect);
						})
						.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
						.attach(flex);
					}
				})
				.attach(n);
			})
			.attach(container);

			RectNode.create(
					1920D / 4D + 1920D / 2D + 10D,
					1080D / 4D,
					120D,
					1080 / 2D
					)
			.color(Color.BLUE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(130D, 0, 10D, 30D, BoundingBox.create(130D, 0, 10D, 1080 / 2D)))
			.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
			.body(n -> {
				FlexNode
				.vertical(0D, 10D, 120D)
				.margin(10D)
				.align(Align.CENTER)
				.body(flex -> {
					for (int i = 0; i < 20; i++) {
						RectNode.create(
								0D,
								0D,
								100D,
								100D
								)
						.color(Color.RED)
						.body(rect -> {
							RectNode.create(
									0,
									0,
									rect.getWidth(),
									10D
									)
							.color(Color.GREEN)
							.attach(rect);
						})
						.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
						.attach(flex);
					}
				})
				.attach(n);
			})
			.attach(container);
		});

		container.attach(this);
	}

	@Override
	public void preDraw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(0D, 0D, 1920D, 1080D, Color.BLACK);
	}

	@Override
	public void postDraw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(mouseX , mouseY, Color.BLUE, 10D);
	}

}