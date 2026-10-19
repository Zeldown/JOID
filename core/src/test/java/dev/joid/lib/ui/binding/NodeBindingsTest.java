package dev.joid.lib.ui.binding;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.ui.UIBridgeCursorTest.NodeUI;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.node.impl.control.choice.SwitchNodeTest.Switch;
import dev.joid.lib.ui.node.impl.control.field.IntegerFieldNode;
import dev.joid.lib.ui.node.impl.control.field.TextFieldNodeTest.FieldFont;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNodeTest.Slider;
import dev.joid.lib.ui.node.impl.control.slider.SliderNodeTest;

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