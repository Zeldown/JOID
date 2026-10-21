package dev.joid.lib.ui.binding;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.ui.UIBridgeCursorTest.NodeUI;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNodeTest.Switch;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNode;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNodeTest.FieldFont;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNodeTest.Slider;
import dev.joid.lib.ui.node.impl.control.slider.SliderNodeTest;
import dev.joid.lib.ui.node.impl.model.ModelViewerNode;
import dev.joid.lib.ui.node.impl.model.ModelViewerNodeTest.FixedModel;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class NodeBindingsTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void keepsTheValueOfTheGestureWhenTheBoundsOfItsControlsCannotAgree() {
		final IntegerSignal signal = new IntegerSignal(50);
		final Slider slider = this.slider(signal);
		final IntegerFieldNode field = this.field(signal).max(95);
		this.bridges.open(new NodeUI(slider, field)).frame();
		NodeBindingsTest.guard(signal);
		slider.change(100);
		this.bridges.frames(3);
		Assert.assertEquals(100, (int) signal.peek());
		Assert.assertEquals(100, (int) slider.getValue());
		Assert.assertEquals(95, (int) field.getValue());
		field.change(43);
		this.bridges.frames(3);
		Assert.assertEquals(43, (int) signal.peek());
		Assert.assertEquals(100, (int) slider.getValue());
		Assert.assertEquals(43, (int) field.getValue());
	}

	@Test
	public void keepsTheValueOfTheGestureOnEitherSideOfTwoDisjointBounds() {
		final IntegerSignal signal = new IntegerSignal(15);
		final IntegerFieldNode lower = this.field(signal).max(10);
		final IntegerFieldNode upper = this.field(signal).min(20);
		this.bridges.open(new NodeUI(lower, upper)).frame();
		NodeBindingsTest.guard(signal);
		lower.change(3);
		this.bridges.frames(3);
		Assert.assertEquals(3, (int) signal.peek());
		Assert.assertEquals(3, (int) lower.getValue());
		Assert.assertEquals(20, (int) upper.getValue());
		upper.change(25);
		this.bridges.frames(3);
		Assert.assertEquals(25, (int) signal.peek());
		Assert.assertEquals(10, (int) lower.getValue());
		Assert.assertEquals(25, (int) upper.getValue());
	}

	@Test
	public void keepsTheZoomOfTheGestureWhenTheZoomRangesOfItsViewersCannotAgree() {
		final DoubleSignal signal = new DoubleSignal(1D);
		final ModelViewerNode near = ModelViewerNode.create(0D, 0D, 100D, 100D).<ModelViewerNode>model(new FixedModel()).zoomRange(0.1D, 1D).zoom(signal);
		final ModelViewerNode far = ModelViewerNode.create(0D, 0D, 100D, 100D).<ModelViewerNode>model(new FixedModel()).zoomRange(1.5D, 2D).zoom(signal);
		this.bridges.open(new NodeUI(near, far)).frame();
		NodeBindingsTest.guard(signal);
		near.zoomTo(0.5D);
		this.bridges.frames(3);
		Assert.assertEquals(0.5D, signal.peek(), 0D);
		Assert.assertEquals(0.5D, near.getZoom(), 0D);
		Assert.assertEquals(1.5D, far.getZoom(), 0D);
		far.zoomTo(1.8D);
		this.bridges.frames(3);
		Assert.assertEquals(1.8D, signal.peek(), 0D);
		Assert.assertEquals(1D, near.getZoom(), 0D);
		Assert.assertEquals(1.8D, far.getZoom(), 0D);
	}

	@Test
	public void keepsThePitchOfTheGestureWhenItsYawAndItsPitchShareASignal() {
		final DoubleSignal signal = new DoubleSignal(5D);
		final ModelViewerNode viewer = ModelViewerNode.create(0D, 0D, 100D, 100D).<ModelViewerNode>model(new FixedModel()).rotationYawRange(0D, 10D).rotationPitchRange(20D, 30D).rotationYaw(signal).rotationPitch(signal);
		this.bridges.open(new NodeUI(viewer)).frame();
		NodeBindingsTest.guard(signal);
		viewer.rotate(8D, 25D);
		this.bridges.frames(3);
		Assert.assertEquals(25D, signal.peek(), 0D);
		Assert.assertEquals(10D, viewer.getRotationYaw(), 0D);
		Assert.assertEquals(25D, viewer.getRotationPitch(), 0D);
	}

	@Test
	public void settlesOnOpeningWhenAControlRefusesTheValueOfItsSignal() {
		final IntegerSignal signal = new IntegerSignal(0);
		final Slider slider = this.slider(signal);
		final IntegerFieldNode field = this.field(signal).min(5);
		NodeBindingsTest.guard(signal);
		this.bridges.open(new NodeUI(slider, field)).frames(3);
		Assert.assertEquals(5, (int) signal.peek());
		Assert.assertEquals(0, (int) slider.getValue());
		Assert.assertEquals(5, (int) field.getValue());
	}

	@Test
	public void settlesWhenTwoChoicesShareNoValue() {
		final StringSignal signal = new StringSignal("a");
		final Switch first = new Switch().<Switch>values("a", "b").value(signal);
		final Switch second = new Switch().<Switch>values("c", "d").value(signal);
		NodeBindingsTest.guard(signal);
		this.bridges.open(new NodeUI(first, second)).frame();
		first.change("b");
		this.bridges.frames(3);
		Assert.assertEquals("b", signal.peek());
		Assert.assertEquals("b", first.getValue());
		Assert.assertEquals("c", second.getValue());
	}

	@Test
	public void settlesOnTheValueItsControlsAgreeOn() {
		final IntegerSignal signal = new IntegerSignal(50);
		final IntegerFieldNode capped = this.field(signal).max(85);
		final Slider slider = this.slider(signal);
		final IntegerFieldNode free = this.field(signal).max(100);
		this.bridges.open(new NodeUI(capped, slider, free)).frame();
		free.change(97);
		this.bridges.frame();
		Assert.assertEquals(50, (int) signal.peek());
		Assert.assertEquals(50, (int) capped.getValue());
		Assert.assertEquals(50, (int) slider.getValue());
		Assert.assertEquals(50, (int) free.getValue());
	}

	@Test
	public void showsAWriteOfItsSignalMadeInAClickRightAway() {
		final DoubleSignal x = new DoubleSignal(0D);
		final StringSignal text = new StringSignal("hello");
		final List<Object> read = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D).x(x);
		final TextFieldNode field = TextFieldNode.create(0D, 100D, 200D).<TextFieldNode>info(TextInfo.create(new FieldFont(), 10F)).text(text);
		final RectNode button = RectNode.create(500D, 500D, 100D, 100D).onClick(event -> {
			x.set(50D);
			text.set("world");
			read.add(node.getX());
			read.add(field.getText());
		});
		this.bridges.open(new NodeUI(node, field, button)).frame();
		this.bridges.move(550D, 550D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(Arrays.asList(50D, "world"), read);
	}

	@Test
	public void showsAWriteOfASignalMadeInTheChangeOfABlurRightAway() {
		final DoubleSignal x = new DoubleSignal(0D);
		final List<Object> read = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D).x(x);
		final IntegerFieldNode field = this.field(new IntegerSignal(0));
		this.bridges.open(new NodeUI(node, field)).frame();
		field.focus();
		field.change("");
		for (final char digit : "007".toCharArray()) {
			this.bridges.getUi().charTyped(digit);
		}
		this.bridges.frame();
		field.onChange(event -> {
			x.set(77D);
			read.add(node.getX());
		});
		field.blur();
		Assert.assertEquals(Arrays.asList(77D), read);
	}

	@Test
	public void showsAWriteOfASignalMadeInTheCursorMoveOfASelectionRightAway() {
		final DoubleSignal x = new DoubleSignal(0D);
		final List<Object> read = new ArrayList<>();
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D).x(x);
		final TextFieldNode field = this.text(new StringSignal("hello world"));
		this.bridges.open(new NodeUI(node, field)).frame();
		field.focus().moveCursor(0);
		field.onCursorMove(event -> {
			x.set(55D);
			read.add(node.getX());
		});
		field.select(2, 5);
		Assert.assertEquals(Arrays.asList(55D), read);
	}

	@Test
	public void showsTheChangeOfAFieldInTheOtherFieldsOfItsSignalWhenItFiresTheChange() {
		final StringSignal signal = new StringSignal("a");
		final List<Object> read = new ArrayList<>();
		final TextFieldNode first = this.text(signal);
		final TextFieldNode second = this.text(signal);
		this.bridges.open(new NodeUI(first, second)).frames(2);
		first.onChange(event -> read.add(second.getText()));
		first.change("hello");
		Assert.assertEquals(Arrays.asList("hello"), read);
	}

	@Test
	public void showsWhatIsTypedInTheOtherFieldsOfItsSignalWhenItFiresTheChange() {
		final StringSignal signal = new StringSignal("a");
		final List<Object> read = new ArrayList<>();
		final TextFieldNode first = this.text(signal);
		final TextFieldNode second = this.text(signal);
		this.bridges.open(new NodeUI(first, second)).frames(2);
		first.focus().moveCursor(1);
		this.bridges.frames(2);
		first.onChange(event -> read.add(second.getText()));
		this.bridges.getUi().charTyped('b');
		Assert.assertEquals(Arrays.asList("ab"), read);
	}

	@Test
	public void showsTheChangeOfAFieldInTheSliderOfItsSignalWhenItFiresTheChange() {
		final IntegerSignal signal = new IntegerSignal(10);
		final List<Object> read = new ArrayList<>();
		final IntegerFieldNode field = this.field(signal);
		final Slider slider = this.slider(signal);
		this.bridges.open(new NodeUI(field, slider)).frames(2);
		field.onChange(event -> read.add(slider.getValue()));
		field.change(50);
		Assert.assertEquals(Arrays.asList(50), read);
	}

	@Test
	public void showsItsSignalInEveryControlBoundToItWhenOneOfThemFiresTheChange() {
		final IntegerSignal signal = new IntegerSignal(0);
		final List<Object> read = new ArrayList<>();
		final Slider first = this.slider(signal);
		final Slider second = this.slider(signal);
		final Slider third = this.slider(signal);
		this.bridges.open(new NodeUI(first, second, third)).frame();
		second.onChange(event -> read.add(third.getValue()));
		first.change(80);
		Assert.assertEquals(Arrays.asList(80), read);
	}

	@Test
	public void showsAWriteOfItsSignalInEveryControlBoundToItWhenTheyFireTheChange() {
		final IntegerSignal signal = new IntegerSignal(0);
		final List<Object> read = new ArrayList<>();
		final Slider first = this.slider(signal);
		final Slider second = this.slider(signal);
		final Slider third = this.slider(signal);
		this.bridges.open(new NodeUI(first, second, third)).frame();
		first.onChange(event -> read.add("first " + second.getValue() + " " + third.getValue()));
		second.onChange(event -> read.add("second " + first.getValue() + " " + third.getValue()));
		third.onChange(event -> read.add("third " + first.getValue() + " " + second.getValue()));
		signal.set(60);
		Assert.assertEquals(Arrays.asList("first 60 60", "second 60 60", "third 60 60"), read);
	}

	@Test
	public void showsTheChangeOfAFieldInEveryFieldOfItsSignalWhenTheyFireTheChange() {
		final StringSignal signal = new StringSignal("a");
		final List<Object> read = new ArrayList<>();
		final TextFieldNode first = this.text(signal);
		final TextFieldNode second = this.text(signal);
		final TextFieldNode third = this.text(signal);
		this.bridges.open(new NodeUI(first, second, third)).frames(2);
		second.onChange(event -> read.add(third.getText()));
		first.change("hello");
		signal.set("world");
		Assert.assertEquals(Arrays.asList("hello", "world"), read);
	}

	@Test
	public void firesNoEventOfAFieldAListenerOfItsChangeDetaches() {
		final List<Object> fired = new ArrayList<>();
		final TextFieldNode field = this.text(new StringSignal("a"));
		final NodeUI ui = new NodeUI(field);
		this.bridges.open(ui).frames(2);
		field.focus().moveCursor(1);
		this.bridges.frame();
		field.onChange(event -> {
			fired.add("change");
			ui.remove(field);
		});
		field.onCursorMove(event -> fired.add("cursor"));
		field.change("hello");
		Assert.assertEquals(Arrays.asList("change"), fired);
	}

	@Test
	public void firesNoChangeOfABoundControlAnEarlierListenerDetaches() {
		final IntegerSignal signal = new IntegerSignal(0);
		final List<Object> fired = new ArrayList<>();
		final Slider first = this.slider(signal);
		final Slider second = this.slider(signal);
		final Slider third = this.slider(signal);
		final NodeUI ui = new NodeUI(first, second, third);
		this.bridges.open(ui).frame();
		second.onChange(event -> {
			fired.add("second change");
			ui.remove(third);
		});
		third.onChange(event -> fired.add("third change"));
		third.onDetach(event -> fired.add("third detach"));
		first.change(80);
		Assert.assertEquals(80, (int) third.getValue());
		Assert.assertEquals(Arrays.asList("second change", "third detach"), fired);
	}

	@Test
	public void firesTheDetachOfANodeASignalListenerRemovesRightAway() {
		final IntegerSignal signal = new IntegerSignal(0);
		final List<Object> fired = new ArrayList<>();
		final Slider slider = this.slider(signal);
		final RectNode node = RectNode.create(0D, 0D, 50D, 50D);
		final NodeUI ui = new NodeUI(slider, node);
		this.bridges.open(ui).frame();
		slider.onChange(event -> fired.add("change"));
		node.onDetach(event -> fired.add("detach"));
		signal.onChange(value -> {
			ui.remove(node);
			fired.add("removed");
		});
		signal.set(60);
		Assert.assertEquals(Arrays.asList("detach", "removed", "change"), fired);
	}

	@Test
	public void keepsTheCursorAListenerOfItsChangePlaces() {
		final List<Object> fired = new ArrayList<>();
		final TextFieldNode field = this.text(new StringSignal("hello"));
		this.bridges.open(new NodeUI(field)).frames(2);
		field.focus().moveCursor(5);
		field.onChange(event -> {
			fired.add(event.getText());
			field.moveCursor(0);
		});
		field.onCursorMove(event -> fired.add(field.getCursorPosition()));
		field.change("world!");
		Assert.assertEquals(0, field.getCursorPosition());
		Assert.assertEquals(Arrays.asList("world!", 0, 0), fired);
	}

	@Test
	public void warnsOnceInDevModeWhenTheBoundsOfItsControlsCannotAgree() {
		final IntegerSignal signal = new IntegerSignal(50);
		final Slider slider = this.slider(signal);
		final IntegerFieldNode field = this.field(signal).max(95);
		this.bridges.open(new NodeUI(slider, field)).frame();
		NodeBindingsTest.guard(signal);
		final boolean devMode = JOIDEnv.isDevMode();
		JOIDEnv.devMode(true);
		try {
			final String warning = NodeBindingsTest.capture(() -> {
				slider.change(100);
				this.bridges.frame();
				slider.change(90);
				slider.change(100);
				this.bridges.frame();
			});
			Assert.assertEquals("[JOID] IntegerFieldNode and the other controls bound to IntegerSignal{100} keep correcting each other: their bounds are incompatible, give them bounds that agree" + System.lineSeparator(), warning);
		} finally {
			JOIDEnv.devMode(devMode);
		}
	}

	private Slider slider(final IntegerSignal signal) {
		return new Slider().<Slider>thumb(SliderNodeTest.thumb()).<Slider>range(0, 100, 10).value(signal);
	}

	private IntegerFieldNode field(final IntegerSignal signal) {
		return IntegerFieldNode.create(0D, 0D, 200D).<IntegerFieldNode>info(TextInfo.create(new FieldFont(), 10F)).value(signal);
	}

	private TextFieldNode text(final StringSignal signal) {
		return TextFieldNode.create(0D, 100D, 300D).<TextFieldNode>info(TextInfo.create(new FieldFont(), 10F)).text(signal);
	}

	private static void guard(final Signal<?> signal) {
		final int[] dispatches = new int[1];
		signal.onChange(value -> {
			if (++dispatches[0] > 1000) {
				throw new AssertionError(signal + " never settles");
			}
		});
	}

	private static String capture(final Runnable runnable) {
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setErr(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setErr(previous);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

}