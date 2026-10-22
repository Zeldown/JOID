package dev.joid.demo.ui.option;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import dev.joid.demo.DemoFont;
import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.UIDemoChoice;
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
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
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
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoOption.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final IntegerSignal ticks = new IntegerSignal(0);
		final DoubleSignal zoom = new DoubleSignal(1D);
		final BooleanSignal locked = new BooleanSignal(false);
		final IntegerSignal refusedZooms = new IntegerSignal(0);
		final List<Supplier<UI>> popups = Arrays.asList(UIDemoFixedScalePopup::new, UIDemoLimitedScalePopup::new, UIDemoCornerPopup::new, UIDemoTintedPopup::new, UIDemoLockedPopup::new, UIDemoFixedZoomPopup::new, UIDemoDebugOffPopup::new, UIDemoInstantPopup::new, UIDemoPopInPopup::new, UIDemoSlidePopup::new);
		final String[] captions = {"Scale ignored", "Scale limit", "Corner anchor", "Tinted backdrop", "Not closeable", "Not zoomable", "Debug off", "No transition", "Pop in only", "Custom transition"};

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
			TextNode.create(160, 275).text(Text.create("Interface scale", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		for (int i = 0; i < popups.size(); i++) {
			final int index = i;
			RectNode
			.create(80 + (index + 1) % 5 * 360, 40 + (index + 1) / 5 * 340, 320, 260)
			.color(UIDemoOption.PLACEHOLDER)
			.body(rect -> {
				RectNode
				.create(100, 105, 120, 50)
				.color(UIDemoOption.INK)
				.onClick(event -> JOID.open(popups.get(index).get()))
				.label(Text.create("Open", label))
				.attach(rect);
				TextNode.create(160, 275).text(Text.create(captions[index], info)).anchorX(Align.CENTER).attach(rect);
			})
			.attach(this);
		}

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			TextNode.create(160, 130).text(Text.create("Ticks: " + ticks.get(), info).align(Align.CENTER)).anchor(Align.CENTER).attach(rect);
			TextNode.create(160, 275).text(Text.create("Periodic task", info)).anchorX(Align.CENTER).attach(rect);
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
			TextNode.create(40, 130).text(Text.create("Zoom: " + Math.round(zoom.get() * 100D) + " %", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Zoom signal", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).checked(locked).attach(rect);
			TextNode.create(120, 55).text(Text.create("Locked", info)).attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refusedZooms.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Before zoom", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoOption.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 105, 120, 50)
			.color(UIDemoOption.INK)
			.onClick(event -> JOID.forceOpen(UIDemoChoice.getMenu().get()))
			.label(Text.create("Menu", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Force open", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}