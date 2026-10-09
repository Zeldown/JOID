package dev.joid.demo.ui.layout;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode.FlexDirection;
import dev.joid.lib.ui.node.impl.structure.grid.GridNode;
import dev.joid.lib.ui.node.property.position.PositionProperty;
import dev.joid.lib.utils.align.Align;

public class UIDemoLayout extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoLayout.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final TextInfo small = TextInfo.create(DemoFont.MONTSERRAT, 18, UIDemoLayout.INK);
		final BooleanSignal shown = BooleanSignal.of(false);
		final BooleanSignal row = BooleanSignal.of(false);
		final BooleanSignal grown = BooleanSignal.of(false);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 200, 40).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 120, 40).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 240, 40).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 160, 40).color(UIDemoLayout.INK).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Vertical flex", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.horizontal(20, 20, 200)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 40, 120).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 70, 80).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 40, 200).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 90, 60).color(UIDemoLayout.INK).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Horizontal flex", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 85, 150)
			.color(Color.WHITE)
			.body(box -> {
				FlexNode
				.vertical(0, 0, 85)
				.margin(8D)
				.align(Align.START)
				.body(flex -> {
					RectNode.create(0, 0, 30, 40).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 70, 40).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 50, 40).color(UIDemoLayout.INK).attach(flex);
				})
				.attach(box);
			})
			.attach(rect);
			RectNode
			.create(117, 20, 85, 150)
			.color(Color.WHITE)
			.body(box -> {
				FlexNode
				.vertical(0, 0, 85)
				.margin(8D)
				.align(Align.CENTER)
				.body(flex -> {
					RectNode.create(0, 0, 30, 40).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 70, 40).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 50, 40).color(UIDemoLayout.INK).attach(flex);
				})
				.attach(box);
			})
			.attach(rect);
			RectNode
			.create(215, 20, 85, 150)
			.color(Color.WHITE)
			.body(box -> {
				FlexNode
				.vertical(0, 0, 85)
				.margin(8D)
				.align(Align.END)
				.body(flex -> {
					RectNode.create(0, 0, 30, 40).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 70, 40).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 50, 40).color(UIDemoLayout.INK).attach(flex);
				})
				.attach(box);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Column align", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 280, 60)
			.color(Color.WHITE)
			.body(box -> {
				FlexNode
				.horizontal(0, 0, 60)
				.margin(8D)
				.align(Align.START)
				.body(flex -> {
					RectNode.create(0, 0, 40, 20).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 40, 50).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 40, 35).color(UIDemoLayout.INK).attach(flex);
				})
				.attach(box);
			})
			.attach(rect);
			RectNode
			.create(20, 95, 280, 60)
			.color(Color.WHITE)
			.body(box -> {
				FlexNode
				.horizontal(0, 0, 60)
				.margin(8D)
				.align(Align.CENTER)
				.body(flex -> {
					RectNode.create(0, 0, 40, 20).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 40, 50).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 40, 35).color(UIDemoLayout.INK).attach(flex);
				})
				.attach(box);
			})
			.attach(rect);
			RectNode
			.create(20, 170, 280, 60)
			.color(Color.WHITE)
			.body(box -> {
				FlexNode
				.horizontal(0, 0, 60)
				.margin(8D)
				.align(Align.END)
				.body(flex -> {
					RectNode.create(0, 0, 40, 20).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 40, 50).color(UIDemoLayout.INK).attach(flex);
					RectNode.create(0, 0, 40, 35).color(UIDemoLayout.INK).attach(flex);
				})
				.attach(box);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Row align", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.horizontal(20, 40, 50)
			.body(flex -> {
				RectNode.create(0, 0, 50, 50).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 50, 50).color(Color.WHITE).attach(flex);
				RectNode.create(0, 0, 50, 50).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 50, 50).color(Color.WHITE).attach(flex);
			})
			.attach(rect);
			FlexNode
			.horizontal(20, 140, 50)
			.margin(20D)
			.body(flex -> {
				RectNode.create(0, 0, 50, 50).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 50, 50).color(Color.WHITE).attach(flex);
				RectNode.create(0, 0, 50, 50).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 50, 50).color(Color.WHITE).attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Margin", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.horizontal(20, 40, 60)
			.margin(10D)
			.body(flex -> {
				RectNode.create(0, 0, 50, 60).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 50, 60).color(Color.WHITE).visible(shown).attach(flex);
				RectNode.create(0, 0, 50, 60).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 50, 60).color(UIDemoLayout.INK).attach(flex);
			})
			.attach(rect);
			RectNode
			.create(20, 150, 120, 50)
			.color(UIDemoLayout.INK)
			.onClick((node, mouseX, mouseY, button) -> shown.toggle())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Toggle", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Hidden child", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			final FlexNode list = FlexNode.horizontal(20, 40, 50).margin(8D).attach(rect);
			RectNode
			.create(20, 150, 120, 50)
			.color(UIDemoLayout.INK)
			.onClick((node, mouseX, mouseY, button) -> {
				if (list.getChildren().size() < 6) {
					RectNode.create(0, 0, 40, 50).color(UIDemoLayout.INK).attach(list);
				}
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Add", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(160, 150, 120, 50)
			.color(UIDemoLayout.INK)
			.onClick((node, mouseX, mouseY, button) -> list.clearChildren())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Clear", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Add and clear", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 40)
			.margin(8D)
			.direction(row.map(value -> value ? FlexDirection.ROW : FlexDirection.COLUMN))
			.body(flex -> {
				RectNode.create(0, 0, 40, 40).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 40, 40).color(Color.WHITE).attach(flex);
				RectNode.create(0, 0, 40, 40).color(UIDemoLayout.INK).attach(flex);
				RectNode.create(0, 0, 40, 40).color(Color.WHITE).attach(flex);
			})
			.attach(rect);
			RectNode
			.create(180, 150, 120, 50)
			.color(UIDemoLayout.INK)
			.onClick((node, mouseX, mouseY, button) -> row.toggle())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Switch", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Direction", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			FlexNode
			.vertical(20, 20, 280)
			.margin(10D)
			.body(flex -> {
				FlexNode
				.horizontal(0, 0, 60)
				.margin(10D)
				.body(line -> {
					RectNode.create(0, 0, 80, 60).color(UIDemoLayout.INK).attach(line);
					RectNode.create(0, 0, 120, 60).color(UIDemoLayout.INK).attach(line);
				})
				.attach(flex);
				FlexNode
				.horizontal(0, 0, 60)
				.margin(10D)
				.body(line -> {
					RectNode.create(0, 0, 60, 60).color(Color.WHITE).attach(line);
					RectNode.create(0, 0, 60, 60).color(Color.WHITE).attach(line);
					RectNode.create(0, 0, 60, 60).color(Color.WHITE).attach(line);
				})
				.attach(flex);
				FlexNode
				.horizontal(0, 0, 60)
				.margin(10D)
				.body(line -> {
					RectNode.create(0, 0, 200, 60).color(UIDemoLayout.INK).attach(line);
				})
				.attach(flex);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Nested flex", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			GridNode
			.create(20, 20, 280, 200)
			.margin(10D)
			.body(grid -> {
				for (int i = 0; i < 14; i++) {
					RectNode.create(0, 0, 46, 46).color(i % 2 == 0 ? UIDemoLayout.INK : Color.WHITE).attach(grid);
				}
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Grid", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			GridNode
			.create(20, 20, 280, 200)
			.verticalMargin(4D)
			.horizontalMargin(20D)
			.body(grid -> {
				RectNode.create(0, 0, 60, 40).color(UIDemoLayout.INK).attach(grid);
				RectNode.create(0, 0, 100, 40).color(UIDemoLayout.INK).attach(grid);
				RectNode.create(0, 0, 40, 40).color(UIDemoLayout.INK).attach(grid);
				RectNode.create(0, 0, 120, 40).color(Color.WHITE).attach(grid);
				RectNode.create(0, 0, 80, 40).color(Color.WHITE).attach(grid);
				RectNode.create(0, 0, 40, 40).color(UIDemoLayout.INK).attach(grid);
				RectNode.create(0, 0, 140, 40).color(UIDemoLayout.INK).attach(grid);
				RectNode.create(0, 0, 60, 40).color(Color.WHITE).attach(grid);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Grid margins", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 140, 140)
			.color(Color.WHITE)
			.body(parent -> {
				RectNode.create(80, 80, 100, 100).color(UIDemoLayout.INK).zindex(-1).attach(parent);
			})
			.attach(rect);
			RectNode.create(180, 50, 80, 80).color(Color.WHITE).borderColor(UIDemoLayout.INK).borderStroke(4D).zindex(1).attach(rect);
			RectNode.create(210, 80, 80, 80).color(UIDemoLayout.INK).attach(rect);
			TextNode.create(160, 275).text(Text.create("Z-index", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(20, 40, 160, 0).color(UIDemoLayout.INK).aspectRatio(2D).attach(rect);
			RectNode.create(200, 40, 0, 160).color(UIDemoLayout.INK).aspectRatio(0.5D).attach(rect);
			TextNode.create(20, 140).text(Text.create("2 : 1", small)).attach(rect);
			TextNode.create(200, 210).text(Text.create("1 : 2", small)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Aspect ratio", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 280, 200)
			.color(Color.WHITE)
			.body(container -> {
				RectNode.create(20, 20, 60, 60).color(UIDemoLayout.INK).attach(container);
				TextNode.create(100, 40).text(Text.create("Relative", small)).attach(container);
				RectNode.create(1200, 840, 60, 60).color(UIDemoLayout.INK).position(PositionProperty.ABSOLUTE).attach(container);
				TextNode.create(100, 140).text(Text.create("Absolute", small)).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Position", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoLayout.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(159, 20, 2, 160).color(Color.WHITE).attach(rect);
			RectNode.create(160, 30, 0, 40).color(UIDemoLayout.INK).width(grown.map(value -> value ? 140D : 60D)).anchorX(Align.START).attach(rect);
			RectNode.create(160, 80, 0, 40).color(UIDemoLayout.INK).width(grown.map(value -> value ? 140D : 60D)).anchorX(Align.CENTER).attach(rect);
			RectNode.create(160, 130, 0, 40).color(UIDemoLayout.INK).width(grown.map(value -> value ? 140D : 60D)).anchorX(Align.END).attach(rect);
			RectNode
			.create(100, 195, 120, 50)
			.color(UIDemoLayout.INK)
			.onClick((node, mouseX, mouseY, button) -> grown.toggle())
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Grow", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Anchors", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}