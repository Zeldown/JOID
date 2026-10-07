package dev.joid.demo.ui.slider;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.slider.node.DemoDoubleSliderNode;
import dev.joid.demo.ui.slider.node.DemoIntegerSliderNode;
import dev.joid.demo.ui.slider.node.DemoStringSliderNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.slider.callback.NodeSliderChangeCallback;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;
import lombok.NonNull;

public class UIDemoSlider extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoSlider.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal integer = IntegerSignal.of(5);
		final IntegerSignal percent = IntegerSignal.of(25);
		final DoubleSignal ratio = DoubleSignal.of(0.5D);
		final StringSignal level = StringSignal.of("Medium");
		final StringSignal align = StringSignal.of("CENTER");
		final IntegerSignal volume = IntegerSignal.of(3);
		final IntegerSignal shared = IntegerSignal.of(7);
		final IntegerSignal target = IntegerSignal.of(0);
		final IntegerSignal refused = IntegerSignal.of(0);

		RectNode
		.create(80, 210, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode
			.create(40, 40, 240, 40)
			.values(0, 10, 5)
			.onChange((node, value) -> {
				System.out.println("[UIDemoSlider] slider value: " + value);
				integer.set(value);
			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + integer.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Integer range", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 210, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).values(25, 0, 25, 50, 75, 100).signal(percent).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + percent.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Value list", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 210, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoDoubleSliderNode.create(40, 40, 240, 40).values(0D, 1D, 0.25D, 0.5D).signal(ratio).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + ratio.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Double step", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 210, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoStringSliderNode.create(40, 40, 240, 40).values("Medium", "Low", "Medium", "High").signal(level).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + level.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Strings", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 210, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoStringSliderNode.create(40, 40, 240, 40).values(Align.CENTER, Align.values()).signal(align).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + align.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Enum", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 550, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).values(0, 10, 3).signal(volume).attach(rect);
			TextNode.create(40, 120).text(Text.create("Value: " + volume.get(), info)).attach(rect);
			RectNode
			.create(40, 170, 80, 50)
			.color(UIDemoSlider.INK)
			.onClick((node, mouseX, mouseY, clickType) -> volume.set(Math.max(0, volume.get() - 1)))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("-1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(140, 170, 80, 50)
			.color(UIDemoSlider.INK)
			.onClick((node, mouseX, mouseY, clickType) -> volume.set(Math.min(10, volume.get() + 1)))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Two-way signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 550, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).values(0, 10, 7).signal(shared).attach(rect);
			DemoIntegerSliderNode.create(40, 100, 240, 40).values(0, 10, 7).signal(shared).attach(rect);
			TextNode.create(40, 170).text(Text.create("Value: " + shared.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shared signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 550, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).values(0, 10, 0).value(target).attach(rect);
			RectNode
			.create(40, 120, 120, 50)
			.color(UIDemoSlider.INK)
			.onClick((node, mouseX, mouseY, clickType) -> target.set((target.get() + 5) % 15))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Next", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Followed value", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 550, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode
			.create(40, 40, 240, 40)
			.values(0, 10, 2)
			.onChange(new NodeSliderChangeCallback<DemoIntegerSliderNode, Integer>() {

				@Override
				public void apply(final @NonNull DemoIntegerSliderNode node, final @NonNull Integer value) {}

				@Override
				public void pre(final @NonNull DemoIntegerSliderNode node, final @NonNull InternalContext context, final @NonNull Integer value) {
					if (value > 5) {
						refused.increment();
						context.cancel();
					}
				}

			})
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Refused: " + refused.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Refused above 5", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 550, 320, 260)
		.color(UIDemoSlider.PLACEHOLDER)
		.body(rect -> {
			DemoIntegerSliderNode.create(40, 40, 240, 40).values(0, 10, 4).enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}