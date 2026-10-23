package dev.joid.lib.ui.event;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.control.checkbox.CheckboxNodeTest.Checkbox;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNodeTest.Switch;
import dev.joid.lib.ui.node.impl.control.selector.SelectorNodeTest.Selector;
import dev.joid.lib.ui.node.impl.control.slider.SliderNodeTest;
import dev.joid.lib.ui.node.impl.control.slider.SliderNodeTest.Slider;
import dev.joid.lib.ui.node.impl.control.toggle.ToggleNodeTest.Toggle;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.shortcut.Shortcut;
import lombok.NonNull;

public class EventFlowTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private final List<String> trace = new ArrayList<>();

	@Test
	public void sendsAPressToTheChildrenAboveThenToTheNodeThenToTheChildrenBelowThenToTheMonitors() {
		final FlowNode above = new FlowNode("above", this.trace);
		final FlowNode below = new FlowNode("below", this.trace).zindex(-1);
		final FlowNode parent = new FlowNode("parent", this.trace).append(above, below);
		this.bridges.open(new FlowUI(parent)).move(150D, 150D).frames(2);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("above handle", "above internal", "above monitor", "parent handle", "parent internal", "below handle", "below internal", "below monitor", "parent monitor"), this.trace);
	}

	@Test
	public void skipsTheNextListenersOnceAChildConsumedThePress() {
		final FlowNode above = new FlowNode("above", this.trace).handleMousePressed(event -> event.consume());
		final FlowNode below = new FlowNode("below", this.trace).zindex(-1);
		final FlowNode parent = new FlowNode("parent", this.trace).append(above, below);
		this.bridges.open(new FlowUI(parent)).move(150D, 150D).frames(2);
		this.trace.clear();
		Assert.assertTrue(this.bridges.getUi().mousePressed(MouseButton.LEFT));
		Assert.assertEquals(Arrays.asList("above handle", "above internal consumed", "above monitor", "parent internal consumed", "below internal consumed", "below monitor", "parent monitor"), this.trace);
	}

	@Test
	public void ordersTheListenersOfOneStepByPriority() {
		final RectNode node = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> this.trace.add("normal")).onClick(EventListener.priority(EventPriority.LOWEST, event -> this.trace.add("lowest"))).onClick(EventListener.priority(EventPriority.HIGHEST, event -> this.trace.add("highest")));
		this.bridges.open(new FlowUI(node)).move(150D, 150D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("highest", "normal", "lowest"), this.trace);
	}

	@Test
	public void keepsTheClickOfAButtonInsideACard() {
		final RectNode button = RectNode.create(10D, 10D, 20D, 20D).onClick(event -> this.trace.add("button"));
		final RectNode card = RectNode.create(100D, 100D, 100D, 100D).onClick(event -> this.trace.add("card")).handleMousePressed(EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("card monitor " + event.isConsumed()))).append(button);
		this.bridges.open(new FlowUI(card)).move(115D, 115D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("button", "card monitor true"), this.trace);
	}

	@Test
	public void sendsTheInputOfAWaitingNodeToItsSkeletonOnly() {
		final BooleanSignal ready = new BooleanSignal(false);
		final FlowNode skeleton = new FlowNode("skeleton", this.trace);
		final FlowNode child = new FlowNode("child", this.trace);
		final FlowNode node = new FlowNode("node", this.trace).append(child).wait(waiting -> ready.get()).skeleton(waiting -> skeleton);
		this.bridges.open(new FlowUI(node)).move(150D, 150D).frames(2);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("skeleton handle", "skeleton internal", "skeleton monitor"), this.trace);
		ready.set(true);
		this.bridges.frame();
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("child handle", "child internal", "child monitor", "node handle", "node internal", "node monitor"), this.trace);
	}

	@Test
	public void sendsAPressToEveryChildWhenOneLeavesItsParent() {
		final FlowNode first = new FlowNode("first", this.trace);
		final FlowNode second = new FlowNode("second", this.trace);
		final FlowNode third = new FlowNode("third", this.trace);
		final FlowNode parent = new FlowNode("parent", this.trace).append(first, second, third);
		second.handleMousePressed(event -> parent.remove(second));
		this.bridges.open(new FlowUI(parent)).move(150D, 150D).frames(2);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("third handle", "third internal", "third monitor", "second handle", "second internal", "second monitor", "first handle", "first internal", "first monitor", "parent handle", "parent internal", "parent monitor"), this.trace);
	}

	@Test
	public void sendsAPressOnceToEachChildWhenOneAppendsASibling() {
		final FlowNode first = new FlowNode("first", this.trace);
		final FlowNode second = new FlowNode("second", this.trace);
		final FlowNode parent = new FlowNode("parent", this.trace).append(first, second);
		second.handleMousePressed(event -> parent.append(new FlowNode("added", this.trace)));
		this.bridges.open(new FlowUI(parent)).move(150D, 150D).frames(2);
		this.trace.clear();
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList("second handle", "second internal", "second monitor", "first handle", "first internal", "first monitor", "parent handle", "parent internal", "parent monitor"), this.trace);
	}

	@Test
	public void sendsAKeyToTheNodesThenToTheUiThenToItsShortcutsThenToItsMonitors() {
		final RectNode node = RectNode.create(0D, 0D, 10D, 10D).handleKeyPressed(event -> this.trace.add("node"));
		final FlowUI ui = new FlowUI(node);
		this.bridges.open(ui);
		ui.handleKeyPressed(event -> this.trace.add("ui"));
		ui.handleKeyPressed(EventListener.priority(EventPriority.MONITOR, event -> this.trace.add("monitor " + event.isConsumed())));
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Shortcut.create(Key.F).overrideFocus(true).onTrigger(event -> this.trace.add("forced")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertTrue(ui.fireKeyPressed(Key.F, Key.F));
		Assert.assertFalse(ui.fireKeyPressed(Key.Q, Key.Q));
		Assert.assertEquals(Arrays.asList("node", "ui", "shortcut", "monitor true", "forced", "monitor true", "node", "ui", "monitor false"), this.trace);
	}

	@Test
	public void letsTheUiConsumeTheKeysBeforeItsShortcuts() {
		final FlowUI ui = new FlowUI();
		this.bridges.open(ui);
		ui.handleKeyPressed(event -> event.consume());
		Shortcut.create(Key.S).onTrigger(event -> this.trace.add("shortcut")).attach(ui);
		Assert.assertTrue(ui.fireKeyPressed(Key.S, Key.S));
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void keepsAUiOpenWhenItRefusesToClose() {
		final BooleanSignal locked = new BooleanSignal(true);
		final FlowUI ui = new FlowUI();
		this.bridges.open(ui);
		ui.beforeClose(event -> {
			this.trace.add("before " + event.getUi().isClosed());
			if (locked.peek()) {
				event.cancel();
			}
		});
		ui.onClose(event -> this.trace.add("closed " + event.getUi().isClosed()));
		Assert.assertFalse(ui.fireClose());
		Assert.assertFalse(ui.isClosed());
		locked.set(false);
		Assert.assertTrue(ui.fireClose());
		Assert.assertEquals(Arrays.asList("before false", "before false", "closed true"), this.trace);
	}

	@Test
	public void closesAUiFromEscapeOnlyWhenItAccepts() {
		final FlowUI ui = new FlowUI();
		this.bridges.open(ui);
		ui.beforeClose(event -> event.cancel());
		this.bridges.getUi().keyPressed(Key.ESCAPE, Key.ESCAPE);
		Assert.assertTrue(JOID.isOpen(ui));
	}

	@Test
	public void clearsTheListenersOfItsInitBeforeAReloadButKeepsThoseOfItsConstructor() {
		final ListenerUI ui = new ListenerUI(this.trace);
		this.bridges.open(ui);
		ui.reload();
		ui.reload();
		ui.fireKeyPressed(Key.A, Key.A);
		Assert.assertEquals(Arrays.asList("constructor", "init"), this.trace);
	}

	@Test
	public void refusesOnlyTheUser() {
		final BooleanSignal checked = new BooleanSignal(false);
		final List<Object> changes = new ArrayList<>();
		final Checkbox checkbox = new Checkbox();
		checkbox.checked(checked).<Checkbox>beforeChange(event -> {
			changes.add("refused " + event.getValue());
			event.cancel();
		}).onChange(event -> changes.add("changed " + event.getPrevious() + " " + event.getValue()));
		this.bridges.open(new FlowUI(checkbox)).move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(checkbox.isChecked());
		checked.set(true);
		Assert.assertTrue(checkbox.isChecked());
		checkbox.checked(false);
		Assert.assertFalse(checkbox.isChecked());
		Assert.assertTrue(checked.get());
		Assert.assertEquals(Arrays.asList("refused true", "changed false true", "changed true false"), changes);
	}

	@Test
	public void changesEveryControlLikeTheUserWithoutReplacingItsSource() {
		final BooleanSignal checked = new BooleanSignal(false);
		final BooleanSignal toggled = new BooleanSignal(false);
		final StringSignal state = new StringSignal("one");
		final StringSignal option = new StringSignal("first");
		final IntegerSignal number = new IntegerSignal(1);
		final List<Object> refused = new ArrayList<>();
		final Checkbox checkbox = new Checkbox().checked(checked);
		final Toggle toggle = new Toggle().state("on", 1).toggle(toggled);
		final Switch control = new Switch().values("one", "two").value(state);
		final Selector selector = new Selector().values("first", "second").value(option);
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(1, 2, 3).value(number).<Slider>beforeChange(event -> {
			refused.add(event.getValue());
			event.cancel();
		});
		this.bridges.open(new FlowUI(checkbox, toggle, control, selector, slider));
		checkbox.change(true);
		toggle.change(true);
		control.change("two");
		selector.change("second");
		slider.change(3);
		Assert.assertTrue(checked.get());
		Assert.assertTrue(toggled.get());
		Assert.assertEquals("two", state.get());
		Assert.assertEquals("second", option.get());
		Assert.assertEquals(1, number.get().intValue());
		Assert.assertEquals(Collections.singletonList(3), refused);
		checked.set(false);
		state.set("one");
		Assert.assertFalse(checkbox.isChecked());
		Assert.assertEquals("one", control.getValue());
	}

	@Test
	public void skipsTheChangeListenersOfARefusedToggle() {
		final List<Object> changes = new ArrayList<>();
		final Toggle toggle = new Toggle().state("on", 1).beforeChange(EventListener.priority(EventPriority.HIGH, event -> event.cancel())).beforeChange(event -> changes.add("normal")).onChange(event -> changes.add("changed"));
		this.bridges.open(new FlowUI(toggle)).move(110D, 110D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		Assert.assertFalse(toggle.isToggle());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void followsTheSignalsAndSettersOfEveryControlWhateverTheirRefusals() {
		final StringSignal state = new StringSignal("one");
		final StringSignal option = new StringSignal("first");
		final IntegerSignal number = new IntegerSignal(1);
		final List<Object> changes = new ArrayList<>();
		final Switch control = new Switch().values("one", "two").value(state).<Switch>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getValue()));
		final Selector selector = new Selector().values("first", "second").value(option).<Selector>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getValue()));
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(1, 2, 3).value(number).<Slider>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new FlowUI(control, selector, slider));
		state.set("two");
		option.set("second");
		number.set(3);
		slider.value(1);
		slider.value(2);
		Assert.assertEquals("two", control.getValue());
		Assert.assertEquals("second", selector.getValue());
		Assert.assertEquals(2, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList("two", "second", 3, 1, 2), changes);
	}

	@Test
	public void namesTheNodeAndTheUiOfAFailingListener() {
		final Checkbox checkbox = new Checkbox();
		checkbox.<Checkbox>onChange(event -> {
			throw new IllegalStateException("broken");
		}).onChange(event -> this.trace.add("next"));
		this.bridges.open(new FlowUI(checkbox));
		final String printed = EventFlowTest.capture(() -> checkbox.checked(true));
		Assert.assertTrue(printed, printed.startsWith("[JOID] The onChange listener of Checkbox in FlowUI failed (java.lang.IllegalStateException: broken"));
		Assert.assertEquals(Collections.singletonList("next"), this.trace);
		Assert.assertTrue(checkbox.isChecked());
	}

	private static String capture(final Runnable runnable) {
		final PrintStream error = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(error);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	public static final class FlowNode extends Node {

		private final String       name;
		private final List<String> trace;

		private FlowNode(final String name, final List<String> trace) {
			super(0D, 0D, 300D, 300D);
			this.name = name;
			this.trace = trace;
			super.handleMousePressed(event -> this.trace.add(this.name + " handle"));
			super.handleMousePressed(EventListener.priority(EventPriority.MONITOR, event -> this.trace.add(this.name + " monitor")));
		}

		@Override
		public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
			this.trace.add(this.name + " internal" + (event.isConsumed() ? " consumed" : ""));
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class FlowUI extends UI {

		private final Node[] nodes;

		private FlowUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

	public static final class ListenerUI extends UI {

		private final List<String> trace;

		private ListenerUI(final List<String> trace) {
			this.trace = trace;
			super.handleKeyPressed(event -> this.trace.add("constructor"));
		}

		@Override
		public void init() {
			super.handleKeyPressed(event -> this.trace.add("init"));
		}

	}

}