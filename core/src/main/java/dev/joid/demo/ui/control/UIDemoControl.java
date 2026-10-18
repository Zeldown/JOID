package dev.joid.demo.ui.control;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.node.impl.control.choice.DemoSwitchNode;
import dev.joid.demo.ui.node.impl.control.toggle.DemoPillToggleNode;
import dev.joid.demo.ui.node.impl.control.toggle.DemoToggleNode;
import dev.joid.demo.ui.node.impl.layout.DemoSplitNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoControl extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoControl.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final IntegerSignal checkboxChanges = new IntegerSignal(0);
		final BooleanSignal sharedCheck = new BooleanSignal(true);
		final IntegerSignal refusedChecks = new IntegerSignal(0);
		final StringSignal toggleValue = new StringSignal("Off");
		final BooleanSignal sharedToggle = new BooleanSignal(true);
		final IntegerSignal refusedToggles = new IntegerSignal(0);
		final StringSignal switchState = new StringSignal("One");
		final StringSignal sharedState = new StringSignal("Two");
		final IntegerSignal refusedStates = new IntegerSignal(0);
		final BooleanSignal pill = new BooleanSignal(false);

		RectNode
		.create(80, 30, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode
			.create(40, 30, 60, 60)
			.onChange(event -> checkboxChanges.increment())
			.attach(rect);
			TextNode.create(40, 100).text(Text.create("Changes: " + checkboxChanges.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Checkbox", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 30, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 30, 60, 60).checked(true).attach(rect);
			TextNode.create(160, 195).text(Text.create("Checked", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 30, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 30, 60, 60).checked(sharedCheck).attach(rect);
			DemoCheckboxNode.create(120, 30, 60, 60).checked(sharedCheck).attach(rect);
			TextNode.create(40, 100).text(Text.create(sharedCheck.get() ? "Checked" : "Unchecked", info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Shared signal", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 30, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode
			.create(40, 30, 60, 60)
			.beforeChange(event -> {
				refusedChecks.increment();
				event.cancel();
			})
			.attach(rect);
			TextNode.create(40, 100).text(Text.create("Refused: " + refusedChecks.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Refused", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 30, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode.create(40, 30, 60, 60).checked(true).enabled(false).attach(rect);
			TextNode.create(160, 195).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 280, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode
			.create(40, 30, 120, 60)
			.state("On", "Off")
			.onChange(event -> toggleValue.set(event.getNode().getValue()))
			.attach(rect);
			TextNode.create(40, 100).text(Text.create("Value: " + toggleValue.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Toggle", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 280, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode.create(40, 30, 120, 60).toggle(true).attach(rect);
			TextNode.create(160, 195).text(Text.create("Toggled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 280, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode.create(40, 30, 120, 60).toggle(sharedToggle).attach(rect);
			DemoToggleNode.create(180, 30, 120, 60).toggle(sharedToggle).attach(rect);
			TextNode.create(40, 100).text(Text.create(sharedToggle.get() ? "Toggled" : "Not toggled", info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Shared signal", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 280, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode
			.create(40, 30, 120, 60)
			.beforeChange(event -> {
				refusedToggles.increment();
				event.cancel();
			})
			.attach(rect);
			TextNode.create(40, 100).text(Text.create("Refused: " + refusedToggles.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Refused", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 280, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoToggleNode.create(40, 30, 120, 60).toggle(true).enabled(false).attach(rect);
			TextNode.create(160, 195).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 530, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode
			.<String>create(40, 30, 240, 50)
			.values("One", "Two", "Three")
			.onChange(event -> switchState.set(event.getValue()))
			.attach(rect);
			TextNode.create(40, 100).text(Text.create("State: " + switchState.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Switch", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 530, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode.<Integer>create(40, 30, 240, 50).values(1, 2, 3).valueIndex(2).attach(rect);
			TextNode.create(160, 195).text(Text.create("Index", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 530, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode.<String>create(40, 30, 240, 50).values("One", "Two", "Three").value(sharedState).attach(rect);
			DemoSwitchNode.<String>create(40, 90, 240, 50).values("One", "Two", "Three").value(sharedState).attach(rect);
			TextNode.create(40, 150).text(Text.create("State: " + sharedState.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Shared signal", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 530, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			final DemoSwitchNode<String> refused = DemoSwitchNode
			.<String>create(40, 30, 240, 50)
			.values("One", "Two", "Three")
			.beforeChange(event -> {
				if ("Three".equals(event.getValue())) {
					refusedStates.increment();
					event.cancel();
				}
			})
			.attach(rect);
			RectNode
			.create(40, 90, 110, 40)
			.color(UIDemoControl.INK)
			.onClick(event -> refused.change("Three"))
			.label(Text.create("Change", label))
			.attach(rect);
			TextNode.create(40, 140).text(Text.create("Refused: " + refusedStates.get(), info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Refused", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 530, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoSwitchNode.<String>create(40, 30, 240, 50).values("One", "Two", "Three").valueIndex(1).enabled(false).attach(rect);
			TextNode.create(160, 195).text(Text.create("Disabled", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(620, 780, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoPillToggleNode.create(40, 30, 120, 60).toggle(pill).attach(rect);
			TextNode.create(40, 100).text(Text.create(pill.get() ? "Toggled" : "Not toggled", info)).attach(rect);
			TextNode.create(160, 195).text(Text.create("Custom", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 780, 320, 180)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			final DemoSplitNode split = DemoSplitNode
			.create(40, 20, 240, 80)
			.body(layout -> {
				RectNode.create(0, 0, 0, 0).color(UIDemoControl.INK).attach(layout);
				RectNode.create(0, 0, 0, 40).color(Color.WHITE).attach(layout);
			})
			.attach(rect);
			RectNode
			.create(40, 120, 110, 40)
			.color(UIDemoControl.INK)
			.onClick(event -> {
				if (split.getChildren().size() < 5) {
					RectNode.create(0, 0, 0, 0).color(UIDemoControl.INK).attach(split);
				}
			})
			.label(Text.create("Add", label))
			.attach(rect);
			RectNode
			.create(170, 120, 110, 40)
			.color(UIDemoControl.INK)
			.onClick(event -> split.clearChildren())
			.label(Text.create("Clear", label))
			.attach(rect);
			TextNode.create(160, 195).text(Text.create("Custom layout", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}