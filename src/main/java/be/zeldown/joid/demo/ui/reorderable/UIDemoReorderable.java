package be.zeldown.joid.demo.ui.reorderable;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.overflow.node.DemoScrollbarNode;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode.FlexDirection;
import be.zeldown.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.box.BoundingBox;
import lombok.NonNull;

public class UIDemoReorderable extends UIDemo {

	private static final int   ITEM_COUNT = 12;
	private static final float ITEM_SATURATION = 0.65F;
	private static final float ITEM_LIGHTNESS  = 0.55F;

	@Override
	public void init() {
		TextNode
		.create(280D, 60D)
		.text(Text.create("Vertical / Auto (hold-click)", TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
		.attach(this);

		RectNode
		.create(280D, 100D, 400D, 360D)
		.color(Color.BLACK.copyAlpha(0.15F))
		.overflow(OverflowProperty.SCROLL)
		.scrollbar(DemoScrollbarNode.create(410D, 0D, 10D, 30D, BoundingBox.create(410D, 0D, 10D, 360D)))
		.body(container -> {
			ReorderableFlexNode
			.vertical(0D, 0D, 400D)
			.margin(10D)
			.onReorderStart((flex, child) -> System.out.println("vertical auto reorder start " + child))
			.onReorder((flex, child) -> System.out.println("vertical auto reorder " + child))
			.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("vertical auto reorder end " + oldIndex + " -> " + newIndex))
			.body(flex -> {
				for (int i = 0; i < UIDemoReorderable.ITEM_COUNT; i++) {
					final int index = i;
					RectNode
					.create(0D, 0D, 400D, 60D)
					.color(this.colorAt(index, UIDemoReorderable.ITEM_COUNT))
					.onDragStart(node -> System.out.println("vertical auto drag start " + index))
					.onDrag(node -> System.out.println("vertical auto drag " + index))
					.onDragEnd(node -> System.out.println("vertical auto drag end " + index))
					.body(rect -> {
						TextNode
						.create(rect.dw(2D), rect.dh(2D))
						.text(Text.create("Item " + (index + 1), TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
						.anchor(Align.CENTER)
						.attach(rect);
					})
					.attach(flex);
				}
			})
			.attach(container);
		})
		.attach(this);

		TextNode
		.create(1240D, 60D)
		.text(Text.create("Vertical / Manual (drag handle only)", TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
		.attach(this);

		RectNode
		.create(1240D, 100D, 400D, 360D)
		.color(Color.BLACK.copyAlpha(0.15F))
		.overflow(OverflowProperty.SCROLL)
		.scrollbar(DemoScrollbarNode.create(410D, 0D, 10D, 30D, BoundingBox.create(410D, 0D, 10D, 360D)))
		.body(container -> {
			final ReorderableFlexNode manual = ReorderableFlexNode
					.vertical(0D, 0D, 400D)
					.margin(10D)
					.auto(false)
					.onReorderStart((flex, child) -> System.out.println("vertical manual reorder start " + child))
					.onReorder((flex, child) -> System.out.println("vertical manual reorder " + child))
					.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("vertical manual reorder end " + oldIndex + " -> " + newIndex));

			manual.body(flex -> {
				for (int i = 0; i < UIDemoReorderable.ITEM_COUNT; i++) {
					final int index = i;
					RectNode
					.create(0D, 0D, 400D, 60D)
					.color(this.colorAt(index, UIDemoReorderable.ITEM_COUNT))
					.onDragStart(node -> System.out.println("vertical manual drag start " + index))
					.onDrag(node -> System.out.println("vertical manual drag " + index))
					.onDragEnd(node -> System.out.println("vertical manual drag end " + index))
					.body(rect -> {
						RectNode
						.create(10D, 15D, 30D, 30D)
						.color(Color.BLACK.copyAlpha(0.35F))
						.body(handle -> {
							TextNode
							.create(handle.dw(2D), handle.dh(2D))
							.text(Text.create("≡", TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE)))
							.anchor(Align.CENTER)
							.attach(handle);
						})
						.onClick((node, mouseX, mouseY, clickType) -> {
							if (clickType.isLeft()) {
								manual.startDrag(rect);
							}
						})
						.attach(rect);

						TextNode
						.create(rect.dw(2D), rect.dh(2D))
						.text(Text.create("Item " + (index + 1), TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
						.anchor(Align.CENTER)
						.attach(rect);
					})
					.attach(flex);
				}
			});

			manual.attach(container);
		})
		.attach(this);

		TextNode
		.create(110D, 510D)
		.text(Text.create("Horizontal / Auto (hold-click)", TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
		.attach(this);

		RectNode
		.create(110D, 550D, 1700D, 130D)
		.color(Color.BLACK.copyAlpha(0.15F))
		.overflow(OverflowProperty.SCROLL)
		.scrollbar(DemoScrollbarNode.create(0D, 135D, 30D, 10D, BoundingBox.create(0D, 135D, 1700D, 10D)))
		.body(container -> {
			ReorderableFlexNode
			.horizontal(0D, 0D, 110D)
			.margin(10D)
			.direction(FlexDirection.ROW)
			.onReorderStart((flex, child) -> System.out.println("horizontal auto reorder start " + child))
			.onReorder((flex, child) -> System.out.println("horizontal auto reorder " + child))
			.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("horizontal auto reorder end " + oldIndex + " -> " + newIndex))
			.body(flex -> {
				for (int i = 0; i < UIDemoReorderable.ITEM_COUNT; i++) {
					final int index = i;
					RectNode
					.create(0D, 0D, 200D, 110D)
					.color(this.colorAt(index, UIDemoReorderable.ITEM_COUNT))
					.onDragStart(node -> System.out.println("horizontal auto drag start " + index))
					.onDrag(node -> System.out.println("horizontal auto drag " + index))
					.onDragEnd(node -> System.out.println("horizontal auto drag end " + index))
					.body(rect -> {
						TextNode
						.create(rect.dw(2D), rect.dh(2D))
						.text(Text.create("Item " + (index + 1), TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
						.anchor(Align.CENTER)
						.attach(rect);
					})
					.attach(flex);
				}
			})
			.attach(container);
		})
		.attach(this);

		TextNode
		.create(110D, 730D)
		.text(Text.create("Horizontal / Manual (drag handle only)", TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
		.attach(this);

		RectNode
		.create(110D, 770D, 1700D, 130D)
		.color(Color.BLACK.copyAlpha(0.15F))
		.overflow(OverflowProperty.SCROLL)
		.scrollbar(DemoScrollbarNode.create(0D, 135D, 30D, 10D, BoundingBox.create(0D, 135D, 1700D, 10D)))
		.body(container -> {
			final ReorderableFlexNode manual = ReorderableFlexNode
					.horizontal(0D, 0D, 110D)
					.margin(10D)
					.auto(false)
					.onReorderStart((flex, child) -> System.out.println("horizontal manual reorder start " + child))
					.onReorder((flex, child) -> System.out.println("horizontal manual reorder " + child))
					.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("horizontal manual reorder end " + oldIndex + " -> " + newIndex));

			manual.body(flex -> {
				for (int i = 0; i < UIDemoReorderable.ITEM_COUNT; i++) {
					final int index = i;
					RectNode
					.create(0D, 0D, 200D, 110D)
					.color(this.colorAt(index, UIDemoReorderable.ITEM_COUNT))
					.onDragStart(node -> System.out.println("horizontal manual drag start " + index))
					.onDrag(node -> System.out.println("horizontal manual drag " + index))
					.onDragEnd(node -> System.out.println("horizontal manual drag end " + index))
					.body(rect -> {
						RectNode
						.create(10D, 10D, 30D, 30D)
						.color(Color.BLACK.copyAlpha(0.35F))
						.body(handle -> {
							TextNode
							.create(handle.dw(2D), handle.dh(2D))
							.text(Text.create("≡", TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE)))
							.anchor(Align.CENTER)
							.attach(handle);
						})
						.onClick((node, mouseX, mouseY, clickType) -> {
							if (clickType.isLeft()) {
								manual.startDrag(rect);
							}
						})
						.attach(rect);

						TextNode
						.create(rect.dw(2D), rect.dh(2D))
						.text(Text.create("Item " + (index + 1), TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE)))
						.anchor(Align.CENTER)
						.attach(rect);
					})
					.attach(flex);
				}
			});

			manual.attach(container);
		})
		.attach(this);
	}

	private @NonNull Color hsl(final float hue, final float saturation, final float lightness) {
		final float h = (hue % 360F + 360F) % 360F;
		final float c = (1F - Math.abs(2F * lightness - 1F)) * saturation;
		final float x = c * (1F - Math.abs(h / 60F % 2F - 1F));
		final float m = lightness - c / 2F;

		final float r;
		final float g;
		final float b;
		if (h < 60F) {
			r = c;
			g = x;
			b = 0F;
		} else if (h < 120F) {
			r = x;
			g = c;
			b = 0F;
		} else if (h < 180F) {
			r = 0F;
			g = c;
			b = x;
		} else if (h < 240F) {
			r = 0F;
			g = x;
			b = c;
		} else if (h < 300F) {
			r = x;
			g = 0F;
			b = c;
		} else {
			r = c;
			g = 0F;
			b = x;
		}

		return new Color(r + m, g + m, b + m);
	}

	private @NonNull Color colorAt(final int index, final int total) {
		return this.hsl((float) index / total * 360F, UIDemoReorderable.ITEM_SATURATION, UIDemoReorderable.ITEM_LIGHTNESS);
	}

}