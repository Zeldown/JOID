package dev.joid.demo.ui.option;

import dev.joid.demo.DemoFont;
import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.option.popup.UIDemoCornerPopup;
import dev.joid.demo.ui.option.popup.UIDemoDebugOffPopup;
import dev.joid.demo.ui.option.popup.UIDemoFixedScalePopup;
import dev.joid.demo.ui.option.popup.UIDemoFixedZoomPopup;
import dev.joid.demo.ui.option.popup.UIDemoInstantPopup;
import dev.joid.demo.ui.option.popup.UIDemoLimitedScalePopup;
import dev.joid.demo.ui.option.popup.UIDemoLockedPopup;
import dev.joid.demo.ui.option.popup.UIDemoPopInPopup;
import dev.joid.demo.ui.option.popup.UIDemoSlidePopup;
import dev.joid.demo.ui.option.popup.UIDemoTintedPopup;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoOption extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoOption.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal ticks = new IntegerSignal(0);
		final DoubleSignal zoom = new DoubleSignal(1D);
		final BooleanSignal locked = new BooleanSignal(false);
		final IntegerSignal refusedZooms = new IntegerSignal(0);
		Task.create(1000L).every(1000L).onRun(event -> ticks.increment()).attach(this);
		super.zoom(zoom);
		super.beforeZoom(event -> {
			if (locked.peek()) {
				refusedZooms.increment();
				event.cancel();
			}
		});

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> {
				if (super.getBridge() instanceof DemoUIBridge) {
					final DemoUIBridge bridge = (DemoUIBridge) super.getBridge();
					final double scale = bridge.getInterfaceScale(this);
					bridge.interfaceScale(scale == 1D ? 1.25D : scale == 1.25D ? 0.75D : 1D);
				}
			})
			.label(Text.create("Next", label))
			.attach(rect);
			TextNode.create(40, 120).text(() -> Text.create("Scale: " + super.getView().getInterfaceScale(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Interface scale", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoFixedScalePopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Scale ignored", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoLimitedScalePopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Scale limit", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoCornerPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Corner anchor", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoTintedPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Tinted backdrop", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoLockedPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Not closeable", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoFixedZoomPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Not zoomable", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoDebugOffPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Debug off", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoInstantPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("No transition", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoPopInPopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Pop in only", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.open(new UIDemoSlidePopup()))
			.label(Text.create("Open", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Custom transition", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(160, 130).text(Text.create("Ticks: " + ticks.get(), info).align(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(160, 275).text(Text.create("Periodic task", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 70, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> super.zoomBy(-0.1D))
			.label(Text.create("-", label))
			.attach(rect);
			RectNode
			.create(125, 40, 70, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> super.zoomBy(0.1D))
			.label(Text.create("+", label))
			.attach(rect);
			RectNode
			.create(210, 40, 70, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> zoom.set(1D))
			.label(Text.create("1:1", label))
			.attach(rect);
			TextNode.create(40, 130).text(() -> Text.create("Zoom: " + Math.round(zoom.get() * 100D) + " %", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Zoom signal", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).checked(locked).attach(rect);
			TextNode.create(120, 55).text(Text.create("Locked", info)).attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refusedZooms.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before zoom", info).alignX(Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}