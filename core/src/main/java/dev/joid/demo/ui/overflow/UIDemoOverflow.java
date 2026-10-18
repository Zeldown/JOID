package dev.joid.demo.ui.overflow;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.scrollbar.DemoScrollbarNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.MaskNodeEffect;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoOverflow extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoOverflow.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final IntegerSignal items = new IntegerSignal(6);
		final IntegerSignal offset = new IntegerSignal(0);
		final IntegerSignal ends = new IntegerSignal(0);

		RectNode
		.create(100, 10, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(20, 20, 360, 180).color(Color.WHITE).attach(rect);
			FlexNode
			.vertical(20, 20, 360)
			.margin(10D)
			.height(180)
			.overflow(OverflowProperty.SCROLL)
			.body(flex -> {
				for (int i = 0; i < 8; i++) {
					RectNode.create(10, 10, 340, 40).color(UIDemoOverflow.INK).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Vertical scroll", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 10, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(20, 20, 360, 180).color(Color.WHITE).attach(rect);
			FlexNode
			.horizontal(20, 20, 180)
			.margin(10D)
			.width(360)
			.overflow(OverflowProperty.SCROLL)
			.body(flex -> {
				for (int i = 0; i < 8; i++) {
					RectNode.create(10, 10, 80, 160).color(UIDemoOverflow.INK).attach(flex);
				}
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Horizontal scroll", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 10, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 160)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(0, 168, 360, 12))
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
			TextNode.create(200, 235).text(Text.create("Both axes", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 10, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 170, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.HIDDEN)
			.body(area -> {
				RectNode.create(20, 40, 240, 120).color(UIDemoOverflow.INK).attach(area);
			})
			.attach(rect);
			RectNode
			.create(210, 20, 170, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.NONE)
			.body(area -> {
				RectNode.create(20, 40, 240, 120).color(UIDemoOverflow.INK).attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Hidden and none", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 275, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(20, 20, 340, 180).color(Color.WHITE).attach(rect);
			GridNode
			.create(20, 20, 340, 180)
			.margin(10D)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(348, 0, 12, 180))
			.body(grid -> {
				for (int i = 0; i < 25; i++) {
					RectNode.create(10, 10, 56, 56).color(UIDemoOverflow.INK).attach(grid);
				}
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Grid with scrollbar", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 275, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 160)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(0, 168, 360, 12))
			.body(area -> {
				FlexNode
				.horizontal(10, 10, 140)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						RectNode.create(0, 0, 80, 140).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Horizontal scrollbar", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 275, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
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
			TextNode.create(200, 235).text(Text.create("Scroll speed", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 275, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			final RectNode area = RectNode
			.create(20, 20, 220, 180)
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
			.onClick(event -> area.scrollTo(area.getTargetScrollX(), 0D))
			.label(Text.create("Top", label))
			.attach(rect);
			RectNode
			.create(260, 90, 120, 50)
			.color(UIDemoOverflow.INK)
			.onClick(event -> area.scrollTo(area.getTargetScrollX(), -area.getMaxScrollY()))
			.label(Text.create("Bottom", label))
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Scroll from code", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 540, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 340, 140)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.scrollbar(DemoScrollbarNode.create(348, 0, 12, 140).minThumb(60D))
			.onScrollToEnd(event -> {
				for (int i = 0; i < 4; i++) {
					RectNode.create(0, 0, 320, 40).color(UIDemoOverflow.INK).attach(event.getNode().getChild(0, FlexNode.class));
				}
				items.add(4);
			})
			.body(area -> {
				FlexNode
				.vertical(10, 10, 320)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 6; i++) {
						RectNode.create(0, 0, 320, 40).color(UIDemoOverflow.INK).attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(20, 175).text(Text.create("Items: " + items.get(), info)).attach(rect);
			TextNode.create(200, 235).text(Text.create("Load at the end", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 540, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 140)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.onScroll(event -> offset.set((int) event.getOffset()))
			.onScrollEndReached(event -> ends.increment())
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
			TextNode.create(200, 235).text(Text.create("Scroll events", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 540, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
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
			TextNode.create(200, 235).text(Text.create("Nested scroll", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 540, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				FlexNode
				.vertical(10, 10, 340)
				.margin(10D)
				.body(flex -> {
					for (int i = 0; i < 8; i++) {
						final long shownAt = BridgeHandler.CLOCK.get().currentTimeMillis() + 1000L + i * 500L;
						RectNode
						.create(0, 0, 340, 40)
						.color(UIDemoOverflow.INK)
						.wait(node -> BridgeHandler.CLOCK.get().currentTimeMillis() >= shownAt)
						.attach(flex);
					}
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Loading items", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 805, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				RectNode
				.create(10, 10, 340, 160)
				.color(UIDemoOverflow.PLACEHOLDER)
				.overflow(OverflowProperty.SCROLL)
				.scrollbar(DemoScrollbarNode.create(328, 0, 12, 160))
				.body(inner -> {
					FlexNode
					.vertical(10, 10, 300)
					.margin(10D)
					.body(flex -> {
						for (int i = 0; i < 6; i++) {
							RectNode.create(0, 0, 300, 40).color(UIDemoOverflow.INK).attach(flex);
						}
					})
					.attach(inner);
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Mask in scroll", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 805, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				RectNode.create(10, 10, 160, 160).color(UIDemoOverflow.PLACEHOLDER).overflow(OverflowProperty.HIDDEN).attach(area);
				RectNode
				.create(190, 10, 160, 160)
				.color(UIDemoOverflow.PLACEHOLDER)
				.overflow(OverflowProperty.HIDDEN)
				.body(mask -> {
					RectNode.create(-140, 40, 260, 80).color(UIDemoOverflow.INK).attach(mask);
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Sibling masks", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 805, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				RectNode
				.create(10, 10, 200, 160)
				.color(UIDemoOverflow.PLACEHOLDER)
				.overflow(OverflowProperty.HIDDEN)
				.body(mask -> {
					RectNode.create(20, 20, 260, 40).color(Color.WHITE).attach(mask);
				})
				.attach(area);
				RectNode.create(110, 90, 230, 60).color(UIDemoOverflow.INK).attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Drawn after mask", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 805, 400, 220)
		.color(UIDemoOverflow.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 360, 180)
			.color(Color.WHITE)
			.overflow(OverflowProperty.SCROLL)
			.body(area -> {
				RectNode
				.create(10, 10, 340, 160)
				.color(UIDemoOverflow.PLACEHOLDER)
				.overflow(OverflowProperty.HIDDEN)
				.body(hidden -> {
					RectNode
					.create(10, 10, 220, 140)
					.color(Color.WHITE)
					.overflow(OverflowProperty.SCROLL)
					.body(inner -> {
						RectNode.create(10, 10, 160, 120).color(UIDemoOverflow.INK).effect(MaskNodeEffect.create().width(120D).height(90D)).attach(inner);
						RectNode.create(70, 50, 120, 60).color(UIDemoOverflow.PLACEHOLDER).attach(inner);
					})
					.attach(hidden);
					RectNode.create(170, 60, 160, 40).color(UIDemoOverflow.INK).attach(hidden);
				})
				.attach(area);
			})
			.attach(rect);
			TextNode.create(200, 235).text(Text.create("Three levels", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}