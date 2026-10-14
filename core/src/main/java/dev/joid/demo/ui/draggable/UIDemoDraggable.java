package dev.joid.demo.ui.draggable;

import java.util.function.Consumer;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoDraggable extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

	private final IntegerSignal ends   = IntegerSignal.of(0);
	private final IntegerSignal drags  = IntegerSignal.of(0);
	private final IntegerSignal snaps  = IntegerSignal.of(0);
	private final IntegerSignal moves  = IntegerSignal.of(0);
	private final IntegerSignal starts = IntegerSignal.of(0);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoDraggable.INK);
		final TextInfo counter = TextInfo.create(DemoFont.MONTSERRAT, 22, UIDemoDraggable.INK);
		final Consumer<Node> grip = node -> {
			RectNode.create(22, 28, 36, 4).color(UIDemoDraggable.PLACEHOLDER).attach(node);
			RectNode.create(22, 38, 36, 4).color(UIDemoDraggable.PLACEHOLDER).attach(node);
			RectNode.create(22, 48, 36, 4).color(UIDemoDraggable.PLACEHOLDER).attach(node);
		};
		final Consumer<Node> outline = node -> node.layer((mouseX, mouseY) -> {
			DrawUtils.SHAPE.drawBorder(node.getX(), node.getY(), node.getX() + node.getWidth(), node.getY() + node.getHeight(), UIDemoDraggable.INK, 2D);
		});

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(160, 90, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.free())
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Free", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 320, 180)
			.color(UIDemoDraggable.PLACEHOLDER)
			.body(container -> {
				RectNode
				.create(20, 20, 80, 80)
				.color(UIDemoDraggable.INK)
				.body(grip)
				.draggable(DraggableProperty.parent())
				.attach(container);
			})
			.self(outline)
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Parent area", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			final RectNode area = RectNode.create(40, 40, 320, 180).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect);
			RectNode
			.create(60, 60, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.node(area))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Node area", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 40, 320, 180).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect);
			RectNode
			.create(60, 60, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.custom(1460, 80, 320, 180))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Custom area", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			final DraggableProperty nearest = DraggableProperty.free();
			nearest.snap(RectNode.create(20, 30, 100, 100).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			nearest.snap(RectNode.create(150, 30, 100, 100).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			nearest.snap(RectNode.create(280, 30, 100, 100).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			RectNode
			.create(160, 150, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(nearest)
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Snap", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			final DraggableProperty overlap = DraggableProperty.free().type(DraggableType.COPY).snap(DraggableSnapType.OVERLAP);
			overlap.snap(RectNode.create(40, 40, 80, 80).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			overlap.snap(RectNode.create(160, 40, 80, 80).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			overlap.snap(RectNode.create(280, 40, 80, 80).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			RectNode
			.create(160, 150, 80, 80)
			.color(() -> this.moves.get() / 10 % 2 == 0 ? UIDemoDraggable.INK : UIDemoDraggable.PLACEHOLDER)
			.body(grip)
			.draggable(overlap)
			.onDrag(event -> this.moves.increment())
			.onDragSnap(event -> RectNode.create(0, 0, 80, 80).color(UIDemoDraggable.INK).attach(event.getTarget()))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Copy", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 40, 320, 180).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect);
			RectNode
			.create(40, 40, 320, 180)
			.color(UIDemoDraggable.PLACEHOLDER)
			.overflow(OverflowProperty.SCROLL)
			.body(container -> {
				ContainerNode.create(0, 0, 320, 600).attach(container);
				RectNode
				.create(20, 100, 80, 80)
				.color(UIDemoDraggable.INK)
				.body(grip)
				.draggable(DraggableProperty.parent())
				.attach(container);
			})
			.onMount(event -> event.getNode().scrollOffsetY(-40D))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Scrolled parent", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(120, 60, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.free())
			.attach(rect);
			RectNode
			.create(180, 100, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.free())
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Overlap: front only", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(160, 90, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.free())
			.beforeDragStart(event -> event.cancel())
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused start", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(160, 90, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(DraggableProperty.free())
			.beforeDragDrop(event -> event.cancel())
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused end", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 720, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(160, 90, 80, 80).color(UIDemoDraggable.INK.withAlpha(0.4F)).draggable(DraggableProperty.disabled()).attach(rect);
			TextNode.create(200, 275).text(Text.create("Disabled", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 720, 400, 260)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			final DraggableProperty slots = DraggableProperty.free();
			slots.snap(RectNode.create(40, 30, 80, 80).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			slots.snap(RectNode.create(40, 150, 80, 80).color(UIDemoDraggable.PLACEHOLDER).self(outline).attach(rect));
			RectNode
			.create(40, 30, 80, 80)
			.color(UIDemoDraggable.INK)
			.body(grip)
			.draggable(slots)
			.onDragStart(event -> this.starts.increment())
			.onDrag(event -> this.drags.increment())
			.onDragSnap(event -> this.snaps.increment())
			.onDragDrop(event -> this.ends.increment())
			.attach(rect);
			TextNode.create(170, 40).text(Text.create("onDragStart " + this.starts.get(), counter)).attach(rect);
			TextNode.create(170, 90).text(Text.create("onDrag " + this.drags.get(), counter)).attach(rect);
			TextNode.create(170, 140).text(Text.create("onSnap " + this.snaps.get(), counter)).attach(rect);
			TextNode.create(170, 190).text(Text.create("onDragEnd " + this.ends.get(), counter)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Callbacks", caption, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}