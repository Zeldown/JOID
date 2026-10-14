package dev.joid.demo.ui.control;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.structure.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.node.impl.structure.sw.DemoSwitchNode;
import dev.joid.demo.ui.node.impl.structure.toggle.DemoToggleNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoControl extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoControl.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal checkboxChanges = new IntegerSignal(0);
		final BooleanSignal sharedCheck = new BooleanSignal(true);
		final IntegerSignal refusedChecks = new IntegerSignal(0);
		final StringSignal toggleValue = new StringSignal("Off");
		final BooleanSignal sharedToggle = new BooleanSignal(true);
		final IntegerSignal refusedToggles = new IntegerSignal(0);
		final StringSignal switchState = new StringSignal("One");
		final StringSignal sharedState = new StringSignal("Two");
		final IntegerSignal refusedStates = new IntegerSignal(0);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoControl.PLACEHOLDER)
		.body(rect -> {
			DemoCheckboxNode
			.create(40, 40, 60, 60)
			.onChange(event -> {
				System.out.println("[UIDemoControl] checkbox checked: " + event.getValue());
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
			.beforeChange(event -> {
				refusedChecks.increment();
				event.cancel();
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
			.onChange(event -> {
				System.out.println("[UIDemoControl] toggle value: " + event.getNode().getValue());
				toggleValue.set(event.getNode().getValue());
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
			.beforeChange(event -> {
				refusedToggles.increment();
				event.cancel();
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
			.onChange(event -> {
				System.out.println("[UIDemoControl] switch state: " + event.getValue());
				switchState.set(event.getValue());
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
			final DemoSwitchNode refused = DemoSwitchNode
			.create(40, 40, 240, 50)
			.states("One", "Two", "Three")
			.beforeChange(event -> {
				if ("Three".equals(event.getValue())) {
					refusedStates.increment();
					event.cancel();
				}
			})
			.attach(rect);
			RectNode
			.create(40, 110, 110, 40)
			.color(UIDemoControl.INK)
			.onClick(event -> refused.state("Three"))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("State", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			RectNode
			.create(170, 110, 110, 40)
			.color(UIDemoControl.INK)
			.onClick(event -> refused.forceState("Three"))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Force", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(40, 180).text(Text.create("Refused: " + refusedStates.get(), info)).attach(rect);
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