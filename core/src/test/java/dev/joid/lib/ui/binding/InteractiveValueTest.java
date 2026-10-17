package dev.joid.lib.ui.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class InteractiveValueTest {

	private static final EventType<NodeChangeEvent.Post<RectNode, Integer>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<RectNode, Integer>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private static final EventType<NodeChangeEvent.Post<RectNode, Boolean>> OPEN         = EventType.create("onOpen");
	private static final EventType<NodeChangeEvent.Post<RectNode, Boolean>> CLOSE        = EventType.create("onClose");
	private static final EventType<NodeChangeEvent.Pre<RectNode, Boolean>>  BEFORE_OPEN  = EventType.create("beforeOpen");
	private static final EventType<NodeChangeEvent.Pre<RectNode, Boolean>>  BEFORE_CLOSE = EventType.create("beforeClose");

	private final List<String> trace    = new ArrayList<>();
	private final RectNode     node     = RectNode.create(0D, 0D, 10D, 10D);
	private final NodeBindings bindings = NodeBindings.create(this.node, EventBus.create(() -> "RectNode"), () -> "RectNode", Runnable::run);

	private int     amount;
	private boolean opened;

	@Test
	public void takesAStartValueWithoutNotifyingBeforeItIsSubscribed() {
		final InteractiveValue<Integer> amount = this.amount();
		Assert.assertSame(this.node, amount.start(3));
		Assert.assertEquals(3, this.amount);
		Assert.assertTrue(this.trace.isEmpty());
		this.bindings.subscribe(false);
		amount.start(4);
		Assert.assertEquals(Arrays.asList("on 3 > 4"), this.trace);
	}

	@Test
	public void notifiesAGestureThatItsListenersCanRefuse() {
		final InteractiveValue<Integer> amount = this.amount();
		this.bindings.subscribe(false);
		amount.before(event -> {
			if (((NodeChangeEvent.Pre<?, ?>) event).getValue().equals(5)) {
				((NodeChangeEvent.Pre<?, ?>) event).cancel();
			}
		});
		Assert.assertTrue(amount.change(4));
		Assert.assertFalse(amount.change(5));
		Assert.assertTrue(amount.change(4));
		Assert.assertEquals(4, this.amount);
		Assert.assertEquals(Arrays.asList("on 0 > 4"), this.trace);
	}

	@Test
	public void writesItsGesturesIntoItsSignalAndFollowsIt() {
		final Signal<Integer> signal = Signal.create(2);
		final InteractiveValue<Integer> amount = this.amount();
		amount.bind(signal);
		this.bindings.subscribe(false);
		Assert.assertEquals(2, this.amount);
		amount.change(7);
		Assert.assertEquals(7, signal.peek().intValue());
		signal.set(9);
		Assert.assertEquals(9, this.amount);
		Assert.assertEquals(Arrays.asList("on 2 > 7", "on 7 > 9"), this.trace);
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesASignalThatCannotBeWritten() {
		this.amount().bind(Signal.from(1));
	}

	@Test
	public void holdsTheSetsOfAnInputUntilItsCommit() {
		final InteractiveValue<Integer> amount = this.amount();
		this.bindings.subscribe(false);
		this.bindings.mark();
		amount.set(2);
		amount.set(amount.get() + 1);
		Assert.assertEquals(3, amount.get().intValue());
		Assert.assertEquals(0, this.amount);
		Assert.assertTrue(this.trace.isEmpty());
		this.bindings.commit();
		Assert.assertEquals(3, this.amount);
		Assert.assertEquals(Arrays.asList("on 0 > 3"), this.trace);
	}

	@Test
	public void goesBackToItsStateWhenAnInputIsRefused() {
		final InteractiveValue<Integer> amount = this.amount();
		this.bindings.subscribe(false);
		amount.before(event -> ((NodeChangeEvent.Pre<?, ?>) event).cancel());
		this.bindings.mark();
		amount.set(5);
		this.bindings.commit();
		Assert.assertEquals(0, this.amount);
		Assert.assertEquals(0, amount.get().intValue());
		Assert.assertTrue(this.trace.isEmpty());
	}

	@Test
	public void clampsEveryValueItTakes() {
		final InteractiveValue<Integer> amount = this.amount().clamp(value -> Math.min(value, 10));
		amount.start(20);
		Assert.assertEquals(10, this.amount);
		amount.change(30);
		Assert.assertEquals(10, this.amount);
	}

	@Test
	public void firesTheCoupleOfEachDirectionOfAToggle() {
		final InteractiveValue<Boolean> opened = InteractiveValue.create(this.bindings, "opened", () -> this.opened, (final Boolean value) -> this.opened = value)
		.events(true, InteractiveValueTest.BEFORE_OPEN, (previous, value) -> NodeChangeEvent.Pre.create(this.node, previous, value), InteractiveValueTest.OPEN, (previous, value) -> NodeChangeEvent.Post.create(this.node, previous, value))
		.events(false, InteractiveValueTest.BEFORE_CLOSE, (previous, value) -> NodeChangeEvent.Pre.create(this.node, previous, value), InteractiveValueTest.CLOSE, (previous, value) -> NodeChangeEvent.Post.create(this.node, previous, value));
		this.bindings.subscribe(false);
		opened.before(true, event -> this.trace.add("before open"));
		opened.on(true, event -> this.trace.add("open"));
		opened.before(false, event -> this.trace.add("before close"));
		opened.on(false, event -> this.trace.add("close"));
		opened.change(true);
		opened.change(false);
		Assert.assertEquals(Arrays.asList("before open", "open", "before close", "close"), this.trace);
		opened.change(false);
		Assert.assertEquals(4, this.trace.size());
	}

	private InteractiveValue<Integer> amount() {
		final InteractiveValue<Integer> amount = InteractiveValue.create(this.bindings, "amount", () -> this.amount, value -> this.amount = value)
		.events(InteractiveValueTest.BEFORE_CHANGE, (previous, value) -> NodeChangeEvent.Pre.create(this.node, previous, value), InteractiveValueTest.CHANGE, (previous, value) -> NodeChangeEvent.Post.create(this.node, previous, value));
		amount.on(event -> this.trace.add("on " + ((NodeChangeEvent.Post<?, ?>) event).getPrevious() + " > " + ((NodeChangeEvent.Post<?, ?>) event).getValue()));
		return amount;
	}

}