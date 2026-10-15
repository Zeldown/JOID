package dev.joid.demo.ui.event;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.control.checkbox.DemoCheckboxNode;
import dev.joid.demo.ui.node.impl.control.field.DemoTextFieldNode;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.hover.impl.CustomHoverElement;
import dev.joid.lib.ui.node.hover.impl.NodeHoverElement;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoEvent extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoEvent.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final IntegerSignal pressed = new IntegerSignal(0);
		final IntegerSignal released = new IntegerSignal(0);
		final DoubleSignal knob = new DoubleSignal(0D);
		final IntegerSignal scrolled = new IntegerSignal(0);
		final DoubleSignal hoverX = new DoubleSignal(120D);
		final DoubleSignal hoverY = new DoubleSignal(90D);
		final IntegerSignal inits = new IntegerSignal(0);
		final IntegerSignal appended = new IntegerSignal(0);
		final IntegerSignal detached = new IntegerSignal(0);
		final IntegerSignal clicks = new IntegerSignal(0);
		final IntegerSignal shortcuts = new IntegerSignal(0);
		final IntegerSignal forced = new IntegerSignal(0);
		final IntegerSignal seconds = new IntegerSignal(0);
		final BooleanSignal running = new BooleanSignal(true);
		final Task timer = Task.create(1000L).every(1000L).onRun(event -> seconds.increment());
		final Shortcut space = Shortcut.create(Key.SPACE).ctrl(true).onTrigger(event -> shortcuts.increment());
		super.addTask(timer);
		super.addShortcut(space);

		RectNode
		.create(80, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			ContainerNode
			.create(0, 0, 320, 260)
			.handleMousePressed(event -> pressed.increment())
			.handleMouseReleased(event -> released.increment())
			.attach(rect);
			TextNode.create(40, 80).text(Text.create("Pressed: " + pressed.get(), info)).attach(rect);
			TextNode.create(40, 125).text(Text.create("Released: " + released.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Press anywhere", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 100, 240, 60)
			.color(Color.WHITE)
			.handleMouseDragged(event -> {
				if (event.getNode().isHovered()) {
					knob.set(Math.max(0D, Math.min(180D, event.getMouseX() - event.getNode().getAbsoluteX() - 30D)));
				}
			})
			.body(bar -> {
				RectNode.create(0, 0, 60, 60).color(UIDemoEvent.INK).x(knob).interactive(false).attach(bar);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Mouse dragged", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 80)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.handleMouseScroll(event -> {
				if (event.getNode().isHovered()) {
					scrolled.set(scrolled.get() + (int) Math.signum(event.getNotchesY()));
				}
			})
			.attach(rect);
			TextNode.create(40, 145).text(Text.create("Scroll: " + scrolled.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Mouse scroll", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 30, 240, 180)
			.color(Color.WHITE)
			.onHover(event -> {
				hoverX.set(event.getMouseX() - event.getNode().getAbsoluteX());
				hoverY.set(event.getMouseY() - event.getNode().getAbsoluteY());
			})
			.body(area -> {
				CircleNode.create(0, 0, 20).color(UIDemoEvent.INK).x(hoverX.get() - 10D).y(hoverY.get() - 10D).interactive(false).attach(area);
			})
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Hover", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 40, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(100, 40, 120, 80).color(UIDemoEvent.INK).onInit(event -> inits.increment()).attach(rect);
			TextNode.create(100, 145).text(Text.create("Init: " + inits.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Init", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 100, 60, 60).color(UIDemoEvent.INK).onUpdate(event -> event.getNode().width(60D + 180D * (BridgeHandler.CLOCK.get().currentTimeMillis() % 2000L) / 2000D)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Update", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode.create(40, 100, 60, 60).color(UIDemoEvent.INK).onRender(event -> event.getNode().x(Math.max(40D, Math.min(220D, event.getMouseX() - rect.getAbsoluteX() - 30D)))).attach(rect);
			TextNode.create(160, 275).text(Text.create("Render", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			final FlexNode list = FlexNode.horizontal(20, 20, 50).margin(8D).onChildAppend(event -> appended.increment()).attach(rect);
			RectNode
			.create(20, 90, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick(event -> {
				if (list.getChildren().size() < 6) {
					RectNode.create(0, 0, 40, 50).color(UIDemoEvent.INK).onDetach(removed -> detached.increment()).attach(list);
				}
			})
			.label(Text.create("Add", label))
			.attach(rect);
			RectNode
			.create(160, 90, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick(event -> {
				if (!list.getChildren().isEmpty()) {
					list.remove(list.getChildren().get(list.getChildren().size() - 1));
				}
			})
			.label(Text.create("Remove", label))
			.attach(rect);
			TextNode.create(20, 160).text(Text.create("Appended: " + appended.get(), info)).attach(rect);
			TextNode.create(20, 200).text(Text.create("Detached: " + detached.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Append and detach", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(NodeHoverElement.follow(RectNode.create(16, -50, 140, 50).color(UIDemoEvent.INK).label(Text.create("A node", label))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Node tooltip", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 380, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(CustomHoverElement.follow((node, mouseX, mouseY) -> DrawUtils.SHAPE.drawRoundedBorder(-24D, -24D, 48D, 48D, UIDemoEvent.INK, 24F, 3D)))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Custom tooltip", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(80, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 90, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(NodeHoverElement.fixed(RectNode.create(120, 745, 240, 50).color(UIDemoEvent.INK).label(Text.create("Always here", label))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Fixed tooltip", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(440, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(100, 50, 120, 120)
			.color(UIDemoEvent.INK)
			.hoveredColor(Color.WHITE)
			.hover(NodeHoverElement.relative(RectNode.create(0, -48, 120, 40).color(Color.WHITE).label(Text.create("Above", info))))
			.attach(rect);
			TextNode.create(160, 275).text(Text.create("Relative tooltip", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(800, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick(event -> clicks.set(clicks.get() + (UI.isShiftKeyDown() ? 10 : 1)))
			.label(Text.create("Click", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Count: " + clicks.get(), info)).attach(rect);
			TextNode.create(40, 165).text(Text.create("Shift adds 10", info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shift click", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1160, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			Shortcut.create(Key.F2).overrideFocus(true).onTrigger(event -> forced.increment()).attach(rect);
			DemoTextFieldNode.create(40, 40, 240, 50).info(info).marginHorizontal(12D).placeholder("Focus me").attach(rect);
			TextNode.create(40, 120).text(Text.create("Ctrl + Space: " + shortcuts.get(), info)).attach(rect);
			DemoCheckboxNode
			.create(250, 112, 40, 40)
			.checked(true)
			.onChange(event -> {
				if (event.getValue()) {
					super.addShortcut(space);
				} else {
					super.removeShortcut(space);
				}
			})
			.attach(rect);
			TextNode.create(40, 165).text(Text.create("F2: " + forced.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Shortcut", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1520, 720, 320, 260)
		.color(UIDemoEvent.PLACEHOLDER)
		.body(rect -> {
			RectNode
			.create(40, 40, 120, 50)
			.color(UIDemoEvent.INK)
			.onClick(event -> {
				if (running.peek()) {
					super.removeTask(timer);
				} else {
					super.addTask(timer);
				}
				running.toggle();
			})
			.label(Text.create(running.get() ? "Stop" : "Start", label))
			.attach(rect);
			TextNode.create(40, 120).text(Text.create("Seconds: " + seconds.get(), info)).attach(rect);
			TextNode.create(160, 275).text(Text.create("Task", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}