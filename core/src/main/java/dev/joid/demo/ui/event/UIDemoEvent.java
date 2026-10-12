package dev.joid.demo.ui.event;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.hover.impl.CustomHoverElement;
import dev.joid.lib.ui.node.hover.impl.NodeHoverElement;
import dev.joid.lib.ui.node.impl.design.shape.CircleNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoEvent extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoEvent.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal pressed = IntegerSignal.of(0);
		final IntegerSignal released = IntegerSignal.of(0);
		final DoubleSignal knob = DoubleSignal.of(0D);
		final IntegerSignal scrolled = IntegerSignal.of(0);
		final DoubleSignal hoverX = DoubleSignal.of(120D);
		final DoubleSignal hoverY = DoubleSignal.of(90D);
		final IntegerSignal inits = IntegerSignal.of(0);
		final IntegerSignal appended = IntegerSignal.of(0);
		final IntegerSignal detached = IntegerSignal.of(0);
		final IntegerSignal clicks = IntegerSignal.of(0);
		final RectNode follow = RectNode.create(16, 0, 140, 50).color(UIDemoEvent.INK).body(tooltip -> {
			TextNode.create(tooltip.dw(2), tooltip.dh(2)).text(Text.create("A node", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(tooltip);
		});
		final RectNode fixed = RectNode.create(120, 795, 240, 50).color(UIDemoEvent.INK).body(tooltip -> {
			TextNode.create(tooltip.dw(2), tooltip.dh(2)).text(Text.create("Always here", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(tooltip);
		});
		final RectNode relative = RectNode.create(0, -8, 120, 40).color(Color.WHITE).body(tooltip -> {
			TextNode.create(tooltip.dw(2), tooltip.dh(2)).text(Text.create("Above", info, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(tooltip);
		});

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(0, 0, 320, 260)
			.onMousePressed((node, mouseX, mouseY, button) -> pressed.increment())
			.onMouseReleased((node, mouseX, mouseY, button) -> released.increment())
			.attach(rect);
			TextNode.create(40, 80).text(Text.create("Pressed: " + pressed.get(), info)).attach(rect);
			TextNode.create(40, 125).text(Text.create("Released: " + released.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Press anywhere", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 240, 60)
			.color(Color.WHITE)
			.onMouseDragged((node, mouseX, mouseY, button, deltaTime) -> {
				if (node.isHovered()) {
					knob.set(Math.max(0D, Math.min(180D, mouseX - node.getAbsoluteX() - 30D)));
				}
			})
			.body(bar -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoEvent.INK).x(knob).interactive(false).attach(bar);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Mouse dragged", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 80)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.onMouseScroll((node, mouseX, mouseY, notchesX, notchesY) -> {
				if (node.isHovered()) {
					scrolled.set(scrolled.get() + (int) Math.signum(notchesY));
				}
			})
			.attach(rect);
			TextNode.create(40, 145).text(Text.create("Scroll: " + scrolled.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Mouse scroll", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 30, 240, 180)
			.color(Color.WHITE)
			.onHover((node, mouseX, mouseY) -> {
				hoverX.set(mouseX - node.getAbsoluteX());
				hoverY.set(mouseY - node.getAbsoluteY());
			})
			.body(area -> {
				CircleNode.create(0, 0, 20).color(UIDemoEvent.INK).x(() -> hoverX.get() - 10D).y(() -> hoverY.get() - 10D).interactive(false).attach(area);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Hover", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 40, 120, 80).color(UIDemoEvent.INK).onInit(node -> inits.increment()).attach(rect);
			TextNode.create(100, 145).text(Text.create("Init: " + inits.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Init", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 100, 60, 60).color(UIDemoEvent.INK).onUpdate(node -> node.width(60D + 180D * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000L) / 2000D)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Update", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 100, 60, 60).color(UIDemoEvent.INK).onRender((node, mouseX, mouseY) -> node.x(Math.max(40D, Math.min(220D, mouseX - rect.getAbsoluteX() - 30D)))).attach(rect);
			TextNode.create(160, 275).text(Text.create("Render", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			final FlexNode list = FlexNode.horizontal(20, 20, 50).margin(8D).onAppend((node, child) -> appended.increment()).attach(rect);
			RectNode
			.create(20, 90, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick((node, mouseX, mouseY, button) -> {
				if (list.getChildren().size() < 6) {
					RectNode.create(0, 0, 40, 50).color(UIDemoEvent.INK).onDetach(child -> detached.increment()).attach(list);
				}
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Add", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(160, 90, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick((node, mouseX, mouseY, button) -> {
				if (!list.getChildren().isEmpty()) {
					list.remove(list.getChildren().getLast());
				}
			})
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Remove", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(20, 160).text(Text.create("Appended: " + appended.get(), info)).attach(rect);
			TextNode.create(20, 200).text(Text.create("Detached: " + detached.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Append and detach", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(NodeHoverElement.follow(follow))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Node tooltip", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(CustomHoverElement.follow((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(-24D, -24D, 48D, 48D, UIDemoEvent.INK, 24F, 3D)))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Custom tooltip", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 90, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(NodeHoverElement.fixed(fixed))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Fixed tooltip", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(NodeHoverElement.relative(relative))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Relative tooltip", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick((node, mouseX, mouseY, button) -> clicks.set(clicks.get() + (UI.isShiftKeyDown() ? 10 : 1)))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Click", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Count: " + clicks.get(), info)).attach(rect);
			TextNode.create(40, 165).text(Text.create("Shift adds 10", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shift click", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}