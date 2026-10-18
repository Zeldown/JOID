package dev.joid.demo.ui.slider;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.slider.DemoDoubleSliderNode;
import dev.joid.demo.ui.node.impl.control.slider.DemoIntegerSliderNode;
import dev.joid.demo.ui.node.impl.control.slider.DemoStringSliderNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoSlider extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoSlider.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final IntegerSignal integer = new IntegerSignal(4);
		final IntegerSignal percent = new IntegerSignal(25);
		final DoubleSignal ratio = new DoubleSignal(0.5D);
		final StringSignal level = new StringSignal("Medium");
		final StringSignal align = new StringSignal("CENTER");
		final IntegerSignal volume = new IntegerSignal(3);
		final IntegerSignal shared = new IntegerSignal(7);
		final IntegerSignal target = new IntegerSignal(0);
		final IntegerSignal refused = new IntegerSignal(0);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode
			.create(40, 40, 240, 40)
			.range(0, 10, 2)
			.value(4)
			.onChange(event -> integer.set(event.getValue()))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + integer.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Integer step", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).values(0, 25, 50, 75, 100).value(percent).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + percent.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Value list", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoDoubleSliderNode.create(40, 40, 240, 40).range(0D, 1D, 0.25D).value(ratio).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + ratio.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Double step", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoStringSliderNode.create(40, 40, 240, 40).values("Low", "Medium", "High").value(level).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + level.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Strings", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoStringSliderNode.create(40, 40, 240, 40).values(Align.values()).value(align).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + align.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Enum", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).range(0, 10).value(volume).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + volume.get(), info)).attach(rect);
			RectNode
			.create(40, 170, 80, 50)
			.color(UIDemoSlider.INK)
			.onClick(event -> volume.set(Math.max(0, volume.peek() - 1)))
			.label(Text.create("-1", label))
			.attach(rect);
			RectNode
			.create(140, 170, 80, 50)
			.color(UIDemoSlider.INK)
			.onClick(event -> volume.set(Math.min(10, volume.peek() + 1)))
			.label(Text.create("+1", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Two-way signal", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).range(0, 10).value(shared).attach(rect);
			DemoIntegerSliderNode.create(40, 100, 240, 40).range(0, 10).value(shared).attach(rect);
			TextNode.create(40, 170).text(Text.create("Value: " + shared.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shared signal", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).range(0, 10).value(target).attach(rect);
			RectNode
			.create(40, 120, 120, 50)
			.color(UIDemoSlider.INK)
			.onClick(event -> target.set((target.peek() + 5) % 15))
			.label(Text.create("Next", label))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Followed value", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode
			.create(40, 40, 240, 40)
			.range(0, 10)
			.value(2)
			.beforeChange(event -> {
				if (event.getValue() > 5) {
					refused.increment();
					event.cancel();
				}
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Refused above 5", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).range(0, 10).value(4).enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).range(0, 10).inset(40D).value(0).attach(rect);
			TextNode.create(160, 275).text(Text.create("Inset 40", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}