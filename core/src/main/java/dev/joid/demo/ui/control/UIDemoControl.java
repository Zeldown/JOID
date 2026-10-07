package dev.joid.demo.ui.control;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.checkbox.node.DemoCheckboxNode;
import dev.joid.demo.ui.sw.node.DemoSwitchNode;
import dev.joid.demo.ui.toggle.node.DemoToggleNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.checkbox.callback.NodeCheckboxChangeCallback;
import dev.joid.lib.ui.node.impl.structure.sw.callback.NodeSwitchChangeCallback;
import dev.joid.lib.ui.node.impl.structure.toggle.callback.NodeToggleChangeCallback;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.utils.signal.impl.primitive.StringSignal;
import lombok.NonNull;

public class UIDemoControl extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoControl.INK);
		final IntegerSignal checkboxChanges = IntegerSignal.of(0);
		final BooleanSignal sharedCheck = BooleanSignal.of(true);
		final IntegerSignal refusedChecks = IntegerSignal.of(0);
		final StringSignal toggleValue = StringSignal.of("Off");
		final BooleanSignal sharedToggle = BooleanSignal.of(true);
		final IntegerSignal refusedToggles = IntegerSignal.of(0);
		final StringSignal switchState = StringSignal.of("One");
		final StringSignal sharedState = StringSignal.of("Two");
		final IntegerSignal refusedStates = IntegerSignal.of(0);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode
			.create(40, 40, 60, 60)
			.onChange((node, checked) -> {
				System.out.println("[UIDemoControl] checkbox checked: " + checked);
				checkboxChanges.increment();
			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Changes: " + checkboxChanges.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Checkbox", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).checked(true).attach(rect);
			TextNode.create(160, 275).text(Text.create("Checked", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).signal(sharedCheck).attach(rect);
			DemoCheckboxNode.create(120, 40, 60, 60).signal(sharedCheck).attach(rect);
			TextNode.create(40, 140).text(Text.create(sharedCheck.get() ? "Checked" : "Unchecked", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shared signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode
			.create(40, 40, 60, 60)
			.onChange(new NodeCheckboxChangeCallback<DemoCheckboxNode>() {

				@Override
				public void apply(final @NonNull DemoCheckboxNode node, final boolean checked) {}

				@Override
				public void pre(final @NonNull DemoCheckboxNode node, final @NonNull InternalContext context, final boolean checked) {
					refusedChecks.increment();
					context.cancel();
				}

			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refusedChecks.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Refused", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 40, 60, 60).checked(true).enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode
			.create(40, 40, 120, 60)
			.state("On", "Off")
			.onChange((node, toggle) -> {
				System.out.println("[UIDemoControl] toggle value: " + node.getValue());
				toggleValue.set(node.getValue());
			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Value: " + toggleValue.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Toggle", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode.create(40, 40, 120, 60).toggle(true).attach(rect);
			TextNode.create(160, 275).text(Text.create("Toggled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode.create(40, 40, 120, 60).signal(sharedToggle).attach(rect);
			DemoToggleNode.create(180, 40, 120, 60).signal(sharedToggle).attach(rect);
			TextNode.create(40, 140).text(Text.create(sharedToggle.get() ? "Toggled" : "Not toggled", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shared signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode
			.create(40, 40, 120, 60)
			.onChange(new NodeToggleChangeCallback<DemoToggleNode, String, String>() {

				@Override
				public void apply(final @NonNull DemoToggleNode node, final boolean toggle) {}

				@Override
				public void pre(final @NonNull DemoToggleNode node, final @NonNull InternalContext context, final boolean toggle) {
					refusedToggles.increment();
					context.cancel();
				}

			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refusedToggles.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Refused", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode.create(40, 40, 120, 60).toggle(true).enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode
			.create(40, 40, 240, 50)
			.states("One", "Two", "Three")
			.onChange((node, value) -> {
				System.out.println("[UIDemoControl] switch state: " + value);
				switchState.set(value);
			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("State: " + switchState.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Switch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode.create(40, 40, 240, 50).states("One", "Two", "Three").index(2).attach(rect);
			TextNode.create(160, 275).text(Text.create("Index", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode.create(40, 40, 240, 50).states("One", "Two", "Three").signal(sharedState).attach(rect);
			DemoSwitchNode.create(40, 110, 240, 50).states("One", "Two", "Three").signal(sharedState).attach(rect);
			TextNode.create(40, 190).text(Text.create("State: " + sharedState.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shared signal", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode
			.create(40, 40, 240, 50)
			.states("One", "Two", "Three")
			.onChange(new NodeSwitchChangeCallback<DemoSwitchNode>() {

				@Override
				public void apply(final @NonNull DemoSwitchNode node, final @NonNull String value) {}

				@Override
				public void pre(final @NonNull DemoSwitchNode node, final @NonNull InternalContext context, final @NonNull String value) {
					if ("Three".equals(value)) {
						refusedStates.increment();
						context.cancel();
					}
				}

			})
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refusedStates.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Refused", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode.create(40, 40, 240, 50).states("One", "Two", "Three").index(1).enabled(false).attach(rect);
			TextNode.create(160, 275).text(Text.create("Disabled", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}