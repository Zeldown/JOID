package dev.joid.demo.ui.draggable;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.impl.draggable.NodeDragCallback;
import dev.joid.lib.ui.node.callback.impl.draggable.NodeSnapCallback;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.container.ContainerNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableSnapType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import lombok.NonNull;

public class UIDemoDraggable extends UIDemo {

	private static final Color PLACEHOLDER = new Color(221, 221, 221);
	private static final Color INK         = new Color(153, 153, 153);

	private final IntegerSignal starts = IntegerSignal.of(0);
	private final IntegerSignal drags  = IntegerSignal.of(0);
	private final IntegerSignal snaps  = IntegerSignal.of(0);
	private final IntegerSignal ends   = IntegerSignal.of(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoDraggable.INK);
		final NodeDragCallback<Node> start = node -> this.starts.increment();
		final NodeDragCallback<Node> drag = node -> this.drags.increment();
		final NodeDragCallback<Node> end = node -> this.ends.increment();
		final NodeSnapCallback<Node> snap = (node, slot) -> this.snaps.increment();

		RectNode.create(80, 100, 100, 100).color(UIDemoDraggable.INK).draggable(DraggableProperty.free()).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode
		.create(260, 60, 300, 240)
		.color(UIDemoDraggable.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(20, 20, 100, 100)
			.color(UIDemoDraggable.INK)
			.draggable(DraggableProperty.parent())
			.onDragStart(start)
			.onDrag(drag)
			.onDragEnd(end)
			.attach(rect);
		})
		.attach(this);
		final RectNode area = RectNode.create(620, 60, 300, 240).color(UIDemoDraggable.PLACEHOLDER).attach(this);
		RectNode.create(640, 80, 100, 100).color(UIDemoDraggable.INK).draggable(DraggableProperty.node(area)).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode.create(980, 60, 300, 240).color(UIDemoDraggable.PLACEHOLDER).attach(this);
		RectNode.create(1000, 80, 100, 100).color(UIDemoDraggable.INK).draggable(DraggableProperty.custom(980, 60, 300, 240)).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode.create(1340, 100, 100, 100).color(UIDemoDraggable.INK).draggable(DraggableProperty.ui()).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode.create(1500, 100, 100, 100).color(UIDemoDraggable.INK).draggable(DraggableProperty.screen()).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);

		final DraggableProperty nearest = DraggableProperty.free();
		nearest.snap(RectNode.create(80, 380, 100, 100).color(UIDemoDraggable.PLACEHOLDER).attach(this));
		nearest.snap(RectNode.create(200, 380, 100, 100).color(UIDemoDraggable.PLACEHOLDER).attach(this));
		nearest.snap(RectNode.create(320, 380, 100, 100).color(UIDemoDraggable.PLACEHOLDER).attach(this));
		RectNode.create(200, 520, 100, 100).color(UIDemoDraggable.INK).draggable(nearest).onSnap(snap).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);

		final DraggableProperty overlap = DraggableProperty.free().type(DraggableType.COPY).snap(DraggableSnapType.OVERLAP);
		overlap.snap(RectNode.create(500, 380, 100, 100).color(UIDemoDraggable.PLACEHOLDER).attach(this));
		overlap.snap(RectNode.create(620, 380, 100, 100).color(UIDemoDraggable.PLACEHOLDER).attach(this));
		overlap.snap(RectNode.create(740, 380, 100, 100).color(UIDemoDraggable.PLACEHOLDER).attach(this));
		RectNode
		.create(620, 520, 100, 100)
		.color(UIDemoDraggable.INK)
		.draggable(overlap)
		.onSnap((node, slot) -> RectNode.create(0, 0, 100, 100).color(UIDemoDraggable.INK).attach(slot))
		.onSnap(snap)
		.onDragStart(start)
		.onDrag(drag)
		.onDragEnd(end)
		.attach(this);

		RectNode
		.create(80, 700, 100, 100)
		.color(UIDemoDraggable.INK)
		.draggable(DraggableProperty.free())
		.onDragStart(new NodeDragCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode rect) {}

			@Override
			public void pre(final @NonNull RectNode rect, final @NonNull InternalContext context) {
				context.cancel();
			}

		})
		.onDragStart(start)
		.onDrag(drag)
		.onDragEnd(end)
		.attach(this);
		RectNode
		.create(240, 700, 100, 100)
		.color(UIDemoDraggable.INK)
		.draggable(DraggableProperty.free())
		.onDragEnd(new NodeDragCallback<RectNode>() {

			@Override
			public void apply(final @NonNull RectNode rect) {}

			@Override
			public void pre(final @NonNull RectNode rect, final @NonNull InternalContext context) {
				context.cancel();
			}

		})
		.onDragStart(start)
		.onDrag(drag)
		.onDragEnd(end)
		.attach(this);
		RectNode.create(420, 700, 100, 100).color(UIDemoDraggable.PLACEHOLDER).draggable(DraggableProperty.free()).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode.create(470, 750, 100, 100).color(UIDemoDraggable.INK).draggable(DraggableProperty.free()).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode.create(680, 700, 100, 100).color(UIDemoDraggable.PLACEHOLDER).draggable(DraggableProperty.disabled()).onDragStart(start).onDrag(drag).onDragEnd(end).attach(this);
		RectNode
		.create(880, 640, 400, 300)
		.color(UIDemoDraggable.PLACEHOLDER)
		.overflow(OverflowProperty.SCROLL)
		.body(rect -> {
			ContainerNode.create(0, 0, 400, 900).attach(rect);
			RectNode
			.create(20, 100, 100, 100)
			.color(UIDemoDraggable.INK)
			.draggable(DraggableProperty.parent())
			.onDragStart(start)
			.onDrag(drag)
			.onDragEnd(end)
			.attach(rect);
		})
		.onMount(rect -> rect.scrollOffsetY(-40D))
		.attach(this);

		TextNode.create(80, 990).text(Text.create("onDragStart " + this.starts.get(), info)).attach(this);
		TextNode.create(380, 990).text(Text.create("onDrag " + this.drags.get(), info)).attach(this);
		TextNode.create(680, 990).text(Text.create("onSnap " + this.snaps.get(), info)).attach(this);
		TextNode.create(980, 990).text(Text.create("onDragEnd " + this.ends.get(), info)).attach(this);
	}

}