package dev.joid.demo.ui.option;

import dev.joid.demo.DemoFont;
import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.UIDemo;
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
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoOption extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoOption.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal ticks = new IntegerSignal(0);
		Task.create(1000L).every(1000L).onRun(event -> ticks.increment()).attach(this);

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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Next", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 120).text(() -> Text.create("Scale: " + super.getView().getInterfaceScale(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Interface scale", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Scale ignored", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Scale limit", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Corner anchor", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Tinted backdrop", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Not closeable", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Not zoomable", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Debug off", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("No transition", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Pop in only", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
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
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Open", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Custom transition", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(160, 130).text(Text.create("Ticks: " + ticks.get(), info, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(160, 275).text(Text.create("Periodic task", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}