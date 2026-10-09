package dev.joid.demo.ui.reorderable;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderCallback;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class UIDemoReorderable extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color HANDLE      = new Color(128, 128, 128);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoReorderable.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 18, Color.WHITE);
		final IntegerSignal starts = IntegerSignal.of(0);
		final IntegerSignal moves = IntegerSignal.of(0);
		final StringSignal last = StringSignal.of("none");
		final IntegerSignal refused = IntegerSignal.of(0);

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 220)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				ReorderableFlexNode
				.vertical(0, 0, 360)
				.margin(6D)
				.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("[UIDemoReorderable] vertical auto list: reorder end " + oldIndex + " -> " + newIndex))
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						final int index = i;
						RectNode
						.create(0, 0, 360, 32)
						.color(UIDemoReorderable.INK)
						.body(item -> {
							RectNode.create(10, 9, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
							RectNode.create(10, 15, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
							RectNode.create(10, 21, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
							TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
						})
						.attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Vertical auto", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			final ReorderableFlexNode manual = ReorderableFlexNode
					.vertical(20, 20, 360)
					.margin(6D)
					.autoDrag(false)
					.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("[UIDemoReorderable] vertical handle list: reorder end " + oldIndex + " -> " + newIndex));
			manual.body(flex -> {
			for (int i = 0; i < 5; i++) {
				final int index = i;
				RectNode
				.create(0, 0, 360, 34)
				.color(UIDemoReorderable.INK)
				.body(item -> {
					RectNode
					.create(0, 0, 40, 34)
					.color(UIDemoReorderable.HANDLE)
					.onClick((node, mouseX, mouseY, button) -> manual.startDrag(item))
					.body(handle -> {
						RectNode.create(10, 10, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(handle);
						RectNode.create(10, 16, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(handle);
						RectNode.create(10, 22, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(handle);
					})
					.attach(item);
					TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
				})
				.attach(flex);
			}
			});
			manual.attach(rect);
			TextNode.create(200, 275).text(Text.create("Vertical handle", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			ReorderableFlexNode
			.vertical(20, 20, 200)
			.margin(6D)
			.onReorderStart((flex, child) -> starts.increment())
			.onReorder((flex, child) -> moves.increment())
			.onReorderEnd((flex, child, oldIndex, newIndex) -> last.set(oldIndex + " -> " + newIndex))
			.body(flex -> {
				for (int i = 0; i < 5; i++) {
					final int index = i;
					RectNode
					.create(0, 0, 200, 34)
					.color(UIDemoReorderable.INK)
					.body(item -> {
						RectNode.create(10, 10, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 16, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 22, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(240, 40).text(Text.create("Start: " + starts.get(), info)).attach(rect);
			TextNode.create(240, 100).text(Text.create("Moves: " + moves.get(), info)).attach(rect);
			TextNode.create(240, 160).text(Text.create("End: " + last.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Callbacks", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			final ReorderableFlexNode slots = ReorderableFlexNode
					.vertical(20, 20, 360)
					.margin(6D);
			slots.body(flex -> {
				for (int i = 0; i < 5; i++) {
					final int index = i;
					final boolean locked = index == 0 || index == 2;
					final RectNode node = RectNode
							.create(0, 0, 360, 34)
							.color(locked ? UIDemoReorderable.INK.copyAlpha(0.4F) : UIDemoReorderable.INK)
							.body(item -> {
								if (!locked) {
									RectNode.create(10, 10, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
									RectNode.create(10, 16, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
									RectNode.create(10, 22, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
								}
								TextNode.create(50, item.dh(2)).text(Text.create((locked ? "Locked " : "Item ") + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
							})
							.attach(flex);
					if (locked) {
						slots.lock(node);
					}
				}
			});
			slots.attach(rect);
			TextNode.create(200, 275).text(Text.create("Locked", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 840, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 800, 220)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				ReorderableFlexNode
				.horizontal(10, 10, 200)
				.margin(10D)
				.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("[UIDemoReorderable] horizontal auto list: reorder end " + oldIndex + " -> " + newIndex))
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						final int index = i;
						RectNode
						.create(0, 0, 150, 200)
						.color(UIDemoReorderable.INK)
						.body(item -> {
							RectNode.create(65, 13, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
							RectNode.create(65, 19, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
							RectNode.create(65, 25, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
							TextNode.create(item.dw(2), item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(item);
						})
						.attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(420, 275).text(Text.create("Horizontal auto", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 840, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 800, 220)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				final ReorderableFlexNode manual = ReorderableFlexNode
						.horizontal(10, 10, 200)
						.margin(10D)
						.autoDrag(false)
						.onReorderEnd((flex, child, oldIndex, newIndex) -> System.out.println("[UIDemoReorderable] horizontal handle list: reorder end " + oldIndex + " -> " + newIndex));
				manual.body(flex -> {
					for (int i = 0; i < 8; i++) {
						final int index = i;
						RectNode
						.create(0, 0, 150, 200)
						.color(UIDemoReorderable.INK)
						.body(item -> {
							RectNode
							.create(0, 0, 150, 40)
							.color(UIDemoReorderable.HANDLE)
							.onClick((node, mouseX, mouseY, button) -> manual.startDrag(item))
							.body(handle -> {
								RectNode.create(65, 13, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(handle);
								RectNode.create(65, 19, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(handle);
								RectNode.create(65, 25, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(handle);
							})
							.attach(item);
							TextNode.create(item.dw(2), item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(item);
						})
						.attach(flex);
					}
				});
				manual.attach(area);
			})
			.attach(rect);
			TextNode.create(420, 275).text(Text.create("Horizontal handle", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			ReorderableFlexNode
			.vertical(20, 20, 360)
			.margin(6D)
			.align(Align.CENTER)
			.body(flex -> {
				for (int i = 0; i < 5; i++) {
					final int index = i;
					RectNode
					.create(0, 0, 160 + index * 70 % 200, 34)
					.color(UIDemoReorderable.INK)
					.body(item -> {
						RectNode.create(10, 10, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 16, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 22, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Center align", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			ReorderableFlexNode
			.vertical(20, 20, 360)
			.body(flex -> {
				for (int i = 0; i < 6; i++) {
					final int index = i;
					RectNode
					.create(0, 0, 360, 34)
					.color(UIDemoReorderable.INK)
					.body(item -> {
						RectNode.create(10, 10, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 16, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 22, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("No margin", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			ReorderableFlexNode
			.vertical(20, 20, 360)
			.margin(6D)
			.onReorder(new NodeReorderCallback() {

				@Override
				public void apply(final @NonNull ReorderableFlexNode node, final @NonNull Node child) {}

				@Override
				public void pre(final @NonNull ReorderableFlexNode node, final @NonNull DispatchContext context, final @NonNull Node child) {
					refused.increment();
					context.cancel();
				}

			})
			.body(flex -> {
				for (int i = 0; i < 4; i++) {
					final int index = i;
					RectNode
					.create(0, 0, 360, 34)
					.color(UIDemoReorderable.INK)
					.body(item -> {
						RectNode.create(10, 10, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 16, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						RectNode.create(10, 22, 20, 3).color(UIDemoReorderable.PLACEHOLDER).attach(item);
						TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(20, 200).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused moves", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoReorderable.PLACEHOLDER)
		.body(rect -> {
			ReorderableFlexNode
			.vertical(20, 20, 360)
			.margin(6D)
			.enabled(false)
			.body(flex -> {
				for (int i = 0; i < 5; i++) {
					final int index = i;
					RectNode
					.create(0, 0, 360, 34)
					.color(UIDemoReorderable.INK.copyAlpha(0.4F))
					.body(item -> {
						TextNode.create(50, item.dh(2)).text(Text.create("Item " + (index + 1), label, Align.START, Align.CENTER)).anchorY(Align.CENTER).attach(item);
					})
					.attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}