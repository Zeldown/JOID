package dev.joid.demo.ui.overflow;

import java.util.concurrent.TimeUnit;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.overflow.node.DemoScrollbarNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.impl.structure.grid.GridNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.box.BoundingBox;

public class UIDemoOverflow extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoOverflow.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal items = IntegerSignal.of(6);
		final IntegerSignal offset = IntegerSignal.of(0);
		final IntegerSignal ends = IntegerSignal.of(0);

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 340, 40).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Vertical scroll", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				FlexNode
				.horizontal(10, 10, 180)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 80, 180).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Horizontal scroll", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(0, 188, 60, 12, BoundingBox.create(0, 188, 360, 12)))
			.body(area -> {
				GridNode
				.create(10, 10, 690, 410)
				.margin(10D)
				.body(grid -> {
					for (int i = 0; i < 60; i++) {
						RectNode.create(0, 0, 60, 60).color(i % 2 == 0 ? UIDemoOverflow.INK : UIDemoOverflow.PLACEHOLDER).attach(grid);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Both axes", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 170, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.HIDDEN)
			.body(area -> {
				RectNode.create(20, 40, 240, 120).color(UIDemoOverflow.INK).attach(area);
			})
			.attach(rect);
			RectNode
			.create(210, 20, 170, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.NONE)
			.body(area -> {
				RectNode.create(20, 40, 240, 120).color(UIDemoOverflow.INK).attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Hidden and none", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 340, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(348, 0, 12, 40, BoundingBox.create(348, 0, 12, 200)))
			.body(area -> {
				FlexNode
				.vertical(10, 10, 320)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 320, 40).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Vertical scrollbar", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(0, 188, 60, 12, BoundingBox.create(0, 188, 360, 12)))
			.body(area -> {
				FlexNode
				.horizontal(10, 10, 160)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 80, 160).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Horizontal scrollbar", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollSpeed(4D)
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 16; i++) {
						RectNode.create(0, 0, 340, 40).color(i % 2 == 0 ? UIDemoOverflow.INK : UIDemoOverflow.PLACEHOLDER).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Scroll speed", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			final RectNode area = RectNode
					.create(20, 20, 220, 200)
					.color(Color.WHITE)
					.overflow(OverflowProperty.SCROLL)
					.body(container -> {
						FlexNode
						.vertical(10, 10, 200)
						.margin(10D)
						.body(flex -> {
							for (int i = 0; i < 10; i++) {
								RectNode.create(0, 0, 200, 40).color(i % 2 == 0 ? UIDemoOverflow.INK : UIDemoOverflow.PLACEHOLDER).attach(flex);
							}
						})
						.attach(container);
					})
					.attach(rect);
			RectNode
			.create(260, 20, 120, 50)
			.color(UIDemoOverflow.INK)
			.onClick((node, mouseX, mouseY, button) -> area.scrollRatioY(0F))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Top", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(260, 90, 120, 50)
			.color(UIDemoOverflow.INK)
			.onClick((node, mouseX, mouseY, button) -> area.scrollRatioY(1F))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Bottom", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Scroll from code", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 160)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.onScrollEnding((node, scrollX, scrollY) -> {
				if (items.get() < 24) {
					for (int i = 0; i < 4; i++) {
						RectNode.create(0, 0, 340, 40).color(UIDemoOverflow.INK).attach(node.getChild(0, FlexNode.class));
					}
					items.add(4);
				}
			})
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 6; i++) {
						RectNode.create(0, 0, 340, 40).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(20, 200).text(Text.create("Items: " + items.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Load at the end", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 140)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.onScrollUpdate((node, value) -> offset.set((int) value))
			.onScrollEnd((node, scrollX, scrollY) -> {
				System.out.println("[UIDemoOverflow] scroll end: " + scrollX + ", " + scrollY);
				ends.increment();
			})
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 340, 40).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(20, 175).text(Text.create("Offset: " + offset.get(), info)).attach(rect);
			TextNode.create(220, 175).text(Text.create("Ends: " + ends.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Scroll events", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 4; i++) {
						RectNode
						.create(0, 0, 340, 80)
						.color(UIDemoOverflow.PLACEHOLDER)
						.overflow(OverflowProperty.SCROLL)
						.body(line -> {
							FlexNode
							.horizontal(10, 10, 60)
							.margin(10D)
							.body(row -> {
								for (int j = 0; j < 8; j++) {
									RectNode.create(0, 0, 60, 60).color(UIDemoOverflow.INK).attach(row);
								}
							})
							.attach(line);
						})
						.attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Nested scroll", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 200)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode
						.create(0, 0, 340, 40)
						.color(UIDemoOverflow.INK)
						.wait(1000L + i * 500L, TimeUnit.MILLISECONDS)
						.skeleton(node -> RectNode.create(0, 0, node.getWidth(), node.getHeight()).color(Color.LOADING))
						.attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Loading items", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}