package dev.joid.lib.ui.node.impl.control.slider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.shape.RectNode;

import lombok.AllArgsConstructor;

public class SliderNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyOptionList() {
		new Slider().values();
	}

	@Test
	public void refusesAValueOutsideItsSet() {
		try {
			new Slider().values(3, 6, 9).value(10);
			Assert.fail("A value outside the options must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertEquals("The value 10 is not an option of the Slider [3, 6, 9], pass one of them or add it with values(...)", expected.getMessage());
		}
	}

	@Test
	public void startsOnItsFirstOptionWithoutReportingIt() {
		final List<Integer> changes = new ArrayList<>();
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).onChange(event -> changes.add(event.getValue())).values(3, 6, 9);
		this.bridges.open(new NodeUI(slider));
		Assert.assertEquals(3, slider.getValue().intValue());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void takesAValueGivenBeforeItsOptions() {
		Assert.assertEquals(7, new Slider().value(7).values(SliderNodeTest.digits()).getValue().intValue());
	}

	@Test
	public void takesTheValueOfItsSignalWhenItsOptionsArrive() {
		final Signal<Integer> volume = new Signal<>(7);
		final Slider slider = new Slider().value(volume).values(SliderNodeTest.digits());
		Assert.assertEquals(7, slider.getValue().intValue());
		Assert.assertEquals(7, volume.get().intValue());
	}

	@Test
	public void neverRefusesItsSettersNorItsSignal() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> volume = new Signal<>(5);
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(volume).<Slider>beforeChange(event -> event.cancel()).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(slider)).frame();
		slider.value(9);
		Assert.assertEquals(9, slider.getValue().intValue());
		volume.set(2);
		Assert.assertEquals(9, slider.getValue().intValue());
		slider.value(volume);
		Assert.assertEquals(2, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(9, 2), changes);
	}

	@Test
	public void keepsItsValueWhenItsOptionsAreRebuilt() {
		final List<Integer> changes = new ArrayList<>();
		final Slider slider = new Slider().values(SliderNodeTest.digits()).value(5).onChange(event -> changes.add(event.getValue()));
		slider.values(5, 6, 7);
		Assert.assertEquals(5, slider.getValue().intValue());
		Assert.assertTrue(changes.isEmpty());
	}

	@Test
	public void selectsItsFirstOptionWhenItsValueIsGone() {
		final List<Integer> changes = new ArrayList<>();
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(9).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(slider));
		slider.values(1, 2);
		Assert.assertEquals(1, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(1), changes);
	}

	@Test
	public void takesBackTheValueOfItsSignalOnceItsRangeAllowsIt() {
		final Signal<Integer> volume = new Signal<>(50);
		final IntegerSlider slider = new IntegerSlider().<IntegerSlider>range(0, 10).value(volume);
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertEquals(0, slider.getValue().intValue());
		Assert.assertEquals(0, volume.get().intValue());
		slider.range(0, 100);
		this.bridges.frame();
		Assert.assertEquals(50, slider.getValue().intValue());
		Assert.assertEquals(50, volume.get().intValue());
	}

	@Test
	public void movesItsThumbWhenItsOptionsAreRebuilt() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		slider.values(5, 6, 7);
		this.bridges.frame();
		Assert.assertEquals(0D, slider.getThumb().getX(), 1E-9D);
	}

	@Test
	public void placesItsThumbOnItsValue() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertEquals(175D, slider.getThumb().getX(), 1E-9D);
		Assert.assertEquals(275D, this.thumbDraw().getLeft(), 1E-3D);
		Assert.assertEquals(5, slider.getValue().intValue());
	}

	@Test
	public void placesAThumbSizedByItsHeightOnItsFirstFrame() {
		final Slider slider = new Slider().speed(0.2F).thumb(RectNode.create(0D, 0D, 0D, 0D)).values(SliderNodeTest.digits());
		this.bridges.open(new NodeUI(slider));
		Assert.assertEquals(0D, slider.getThumb().getX(), 1E-9D);
		Assert.assertEquals(50D, slider.getThumb().getWidth(), 0D);
	}

	@Test
	public void movesItsThumbToANewValue() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertSame(slider, slider.value(9));
		this.bridges.frame();
		Assert.assertEquals(450D, this.thumbDraw().getLeft(), 1E-3D);
		Assert.assertEquals(9, slider.getValue().intValue());
	}

	@Test
	public void changesItsValueBeforeBeingShown() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5).value(7);
		Assert.assertEquals(7, slider.getValue().intValue());
		Assert.assertEquals(0D, slider.getThumb().getX(), 0D);
	}

	@Test
	public void refusesToBeAttachedWithoutValues() {
		try {
			this.bridges.open(new NodeUI(new Slider().thumb(SliderNodeTest.thumb())));
			Assert.fail("A slider without values cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Slider has no values, call values(...) before attaching it", expected.getMessage());
		}
	}

	@Test
	public void refusesToBeAttachedWithoutThumb() {
		try {
			this.bridges.open(new NodeUI(new Slider().values(SliderNodeTest.digits())));
			Assert.fail("A slider without thumb cannot be attached");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("The Slider has no thumb, give it one with thumb(...)", expected.getMessage());
		}
	}

	@Test
	public void releasesItsThumbWhenDetached() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		SliderNodeTest.drag(slider);
		slider.fireDetach();
		Assert.assertFalse(slider.isSliding());
	}

	@Test
	public void jumpsToTheClickedPosition() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(5);
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5).value(signal).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frame();
		Assert.assertTrue(slider.isSliding());
		Assert.assertEquals(7, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(7), changes);
		Assert.assertEquals(7, signal.get().intValue());
	}

	@Test
	public void followsItsSignal() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(5);
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(signal).onChange(event -> changes.add(event.getValue()));
		Assert.assertEquals(5, slider.getValue().intValue());
		this.bridges.open(new NodeUI(slider)).frame();
		signal.set(2);
		this.bridges.frame();
		Assert.assertEquals(2, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(2), changes);
	}

	@Test
	public void followsOnlyItsLastSignal() {
		final Signal<Integer> volume = new Signal<>(5);
		final Signal<Integer> balance = new Signal<>(3);
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(volume).value(balance);
		this.bridges.open(new NodeUI(slider)).frame();
		balance.set(7);
		this.bridges.frame();
		Assert.assertEquals(5, volume.get().intValue());
		volume.set(2);
		this.bridges.frame();
		Assert.assertEquals(7, slider.getValue().intValue());
		Assert.assertTrue(SliderNodeTest.subscribers(volume).isEmpty());
		Assert.assertEquals(1, SliderNodeTest.subscribers(balance).size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesASignalItCannotWrite() {
		new Slider().values(SliderNodeTest.digits()).value(new Signal<>(5).map(value -> value + 1));
	}

	@Test
	public void writesAChosenValueIntoItsSignal() {
		final List<Integer> changes = new ArrayList<>();
		final Signal<Integer> signal = new Signal<>(5);
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5).value(signal).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(slider)).frame();
		slider.values(1, 2, 3);
		Assert.assertEquals(1, signal.get().intValue());
		slider.value(3);
		slider.value(3);
		this.bridges.frame();
		Assert.assertEquals(1, signal.get().intValue());
		signal.set(2);
		Assert.assertEquals(3, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(1, 3), changes);
	}

	@Test
	public void catchesUpWithAWriteOfItsSignalItNeverSawBeforeItsDetach() {
		final Signal<Integer> volume = new Signal<>(1);
		final IntegerSlider slider = new IntegerSlider().<IntegerSlider>range(0, 10).value(volume);
		final NodeUI ui = new NodeUI(slider);
		this.bridges.open(ui).frame();
		Signal.silent(() -> volume.set(4));
		ui.remove(slider);
		ui.add(slider);
		this.bridges.frame();
		Assert.assertEquals(4, slider.getValue().intValue());
		Signal.batch(() -> {
			volume.set(7);
			ui.remove(slider);
		});
		ui.add(slider);
		this.bridges.frame();
		Assert.assertEquals(7, slider.getValue().intValue());
	}

	@Test
	public void followsItsDraggedThumb() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.drag(150D, 125D).frames(2);
		Assert.assertEquals(25D, slider.getThumb().getX(), 1E-9D);
		Assert.assertEquals(2, slider.getValue().intValue());
	}

	@Test
	public void snapsItsThumbOnTheStepOfItsValueOnRelease() {
		final List<Integer> changes = new ArrayList<>();
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5).onChange(event -> changes.add(event.getValue()));
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.drag(150D, 125D).frames(2);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertFalse(slider.isSliding());
		Assert.assertEquals(2, slider.getValue().intValue());
		this.bridges.frame();
		Assert.assertEquals(43.75D, slider.getThumb().getX(), 1E-9D);
		Assert.assertEquals(Arrays.asList(7, 2), changes);
	}

	@Test
	public void picksTheValueUnderItsThumbOnRelease() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		SliderNodeTest.drag(slider);
		slider.fireMouseDragged(425D, 125D, MouseButton.LEFT, 0L);
		this.bridges.move(425D, 125D).frame();
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		Assert.assertEquals(8, slider.getValue().intValue());
		this.bridges.frame();
		Assert.assertEquals(306.25D, slider.getThumb().getX(), 1E-9D);
	}

	@Test
	public void ignoresAPressBesideIt() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(1000D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frame();
		Assert.assertFalse(slider.isSliding());
		Assert.assertEquals(5, slider.getValue().intValue());
	}

	@Test
	public void replacesItsThumb() {
		final RectNode first = SliderNodeTest.thumb();
		final RectNode second = SliderNodeTest.thumb();
		final Slider slider = new Slider().thumb(first).thumb(second);
		Assert.assertSame(second, slider.getThumb());
		Assert.assertSame(slider, second.getParent());
		Assert.assertEquals(2, slider.getChildren().size());
		Assert.assertTrue(slider.getChildren().contains(second));
		Assert.assertNull(first.getParent());
	}

	@Test
	public void keepsTheValueItIsGivenOnItsFirstFrames() {
		final IntegerSlider slider = new IntegerSlider().range(1, 4).value(2);
		this.bridges.open(new NodeUI(slider)).frames(2);
		Assert.assertEquals(2, slider.getValue().intValue());
	}

	@Test
	public void placesASingleValueAtTheStartOfItsTrack() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(4);
		this.bridges.open(new NodeUI(slider)).frames(2);
		Assert.assertEquals(0D, slider.getThumb().getX(), 0D);
		Assert.assertEquals(0F, slider.getProgress(), 0F);
		Assert.assertEquals(4, slider.getValue().intValue());
	}

	@Test
	public void keepsSlidingWhenAChangeIsRefused() {
		final List<Integer> proposals = new ArrayList<>();
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5).beforeChange(event -> {
			proposals.add(event.getValue());
			event.cancel();
		});
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frames(3);
		Assert.assertTrue(slider.isSliding());
		Assert.assertEquals(5, slider.getValue().intValue());
		Assert.assertEquals(Arrays.asList(7), proposals);
		this.bridges.drag(150D, 125D).frames(2);
		Assert.assertEquals(Arrays.asList(7, 2), proposals);
		this.bridges.getUi().mouseReleased(MouseButton.LEFT);
		this.bridges.frames(3);
		Assert.assertFalse(slider.isSliding());
		Assert.assertEquals(175D, slider.getThumb().getX(), 1E-9D);
	}

	@Test
	public void ignoresAPressAlreadyConsumed() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(5);
		final NodeUI ui = new NodeUI(slider);
		this.bridges.open(ui).frame();
		RectNode.create(100D, 100D, 400D, 50D).onClick(event -> {}).attach(ui);
		this.bridges.move(400D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.frame();
		Assert.assertFalse(slider.isSliding());
		Assert.assertEquals(5, slider.getValue().intValue());
	}

	@Test
	public void readsItsProgressOnOne() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(SliderNodeTest.digits()).value(3);
		Assert.assertEquals(0F, new Slider().getProgress(), 0F);
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertEquals(0.25F, slider.getProgress(), 1E-6F);
		slider.value(9);
		Assert.assertEquals(1F, slider.getProgress(), 1E-6F);
	}

	@Test
	public void centersItsThumbVertically() {
		final Slider slider = new Slider().thumb(RectNode.create(0D, 0D, 16D, 16D)).values(SliderNodeTest.digits()).value(5);
		this.bridges.open(new NodeUI(slider)).frame();
		Assert.assertEquals(17D, slider.getThumb().getY(), 1E-9D);
	}

	@Test
	public void keepsItsThumbOnItsTrack() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(1, 2, 3);
		this.bridges.open(new NodeUI(slider)).frame();
		SliderNodeTest.drag(slider);
		slider.fireMouseDragged(0D, 125D, MouseButton.LEFT, 0L);
		this.bridges.move(0D, 125D).frame();
		Assert.assertEquals(0D, slider.getThumb().getX(), 1E-9D);
		slider.fireMouseDragged(1900D, 125D, MouseButton.LEFT, 0L);
		this.bridges.move(1900D, 125D).frame();
		Assert.assertEquals(350D, slider.getThumb().getX(), 1E-9D);
	}

	@Test
	public void keepsItsDragOnTheReleaseOfAnotherButton() {
		final Slider slider = new Slider().thumb(SliderNodeTest.thumb()).values(1, 2, 3);
		this.bridges.open(new NodeUI(slider)).frame();
		this.bridges.move(110D, 125D).frames(2);
		this.bridges.getUi().mousePressed(MouseButton.LEFT);
		this.bridges.getUi().mouseReleased(MouseButton.RIGHT);
		Assert.assertTrue(slider.isSliding());
	}

	private Draw thumbDraw() {
		final List<Draw> draws = this.bridges.getRender().getDraws(0.6F, 0.4F, 0.2F);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	public static RectNode thumb() {
		return RectNode.create(0D, 0D, 50D, 50D).color(new Color(0.6F, 0.4F, 0.2F, 1F));
	}

	private static void drag(final SliderNode<?> slider) {
		try {
			final Field field = BaseSliderNode.class.getDeclaredField("sliding");
			field.setAccessible(true);
			field.setBoolean(slider, true);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static Integer[] digits() {
		return new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	}

	private static Set<?> subscribers(final Signal<?> signal) {
		try {
			final Field field = Signal.class.getDeclaredField("listenerSet");
			field.setAccessible(true);
			return (Set<?>) field.get(signal);
		} catch (final ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	public static final class Slider extends SliderNode<Integer> {

		public Slider() {
			super(100D, 100D, 400D, 50D);
			super.track(RectNode.create(0D, 0D, 0D, 0D).color(new Color(0.2F, 0.4F, 0.6F, 1F)));
		}

	}

	public static final class IntegerSlider extends IntegerSliderNode {

		public IntegerSlider() {
			super(100D, 100D, 400D, 50D);
			super.thumb(SliderNodeTest.thumb());
		}

	}

}