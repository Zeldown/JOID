package dev.joid.lib.color;

import java.nio.FloatBuffer;
import java.util.function.UnaryOperator;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.point.Point;

public class ColorTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void copiesAGradientWithItsEnds() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), Point.create(0D, 0D), Point.create(1D, 0D));
		final Color copy = gradient.copy();
		Assert.assertTrue(copy.isGradient());
		Assert.assertNotSame(gradient.getGradient(), copy.getGradient());
		Assert.assertNotSame(gradient.getGradient().getStartColor(), copy.getGradient().getStartColor());
		Assert.assertEquals(0.5F, copy.getGradient().getEndColor().getAlphaF(), 0F);
		Assert.assertEquals(1D, copy.getGradient().getEndPoint().getX(), 0D);
	}

	@Test
	public void fadesAGradientWithoutLosingIt() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), Point.create(0D, 0D), Point.create(1D, 0D));
		final Color faded = gradient.withAlpha(0.4F);
		Assert.assertTrue(faded.isGradient());
		Assert.assertEquals(0.4F, faded.getAlphaF(), 0F);
		Assert.assertEquals(0.4F, faded.getGradient().getStartColor().getAlphaF(), 1E-6F);
		Assert.assertEquals(0.2F, faded.getGradient().getEndColor().getAlphaF(), 1E-6F);
		Assert.assertEquals(1F, faded.getGradient().getStartColor().getRedF(), 0F);
		Assert.assertEquals(1F, faded.getGradient().getEndColor().getBlueF(), 0F);
		Assert.assertEquals(1F, gradient.getGradient().getStartColor().getAlphaF(), 0F);
	}

	@Test
	public void fadesAGradientThatStartsTransparent() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 0F), new Color(0F, 0F, 1F, 1F), Point.create(0D, 0D), Point.create(1D, 0D));
		final Color faded = gradient.withAlpha(0.5F);
		Assert.assertEquals(0.5F, faded.getGradient().getStartColor().getAlphaF(), 0F);
		Assert.assertEquals(0.5F, faded.getGradient().getEndColor().getAlphaF(), 0F);
	}

	@Test
	public void keepsAPlainColorPlain() {
		final Color faded = new Color(1F, 0.5F, 0.25F, 1F).withAlpha(0.3F);
		Assert.assertFalse(faded.isGradient());
		Assert.assertEquals(0.3F, faded.getAlphaF(), 0F);
		Assert.assertEquals(0.5F, faded.getGreenF(), 0F);
		Assert.assertFalse(new Color(1F, 0.5F, 0.25F, 1F).copy().isGradient());
	}

	@Test
	public void readsAnRgbInteger() {
		final Color color = Color.argb(0xFF336699);
		Assert.assertEquals(51, color.getRed());
		Assert.assertEquals(102, color.getGreen());
		Assert.assertEquals(153, color.getBlue());
		Assert.assertEquals(255, color.getAlpha());
	}

	@Test
	public void readsTheAlphaOfAnArgbInteger() {
		Assert.assertEquals(128, Color.argb(0x80336699).getAlpha());
		Assert.assertEquals(1F, Color.argb(0xFF336699).getAlphaF(), 0F);
	}

	@Test
	public void readsAnOpaqueRgbInteger() {
		Assert.assertEquals(new Color(51, 102, 153), Color.rgb(0x336699));
		Assert.assertEquals(new Color(51, 102, 153), Color.rgb(0x00336699));
		Assert.assertEquals(1F, Color.rgb(0x80336699).getAlphaF(), 0F);
	}

	@Test
	public void writesBackItsArgbInteger() {
		Assert.assertEquals(0x80336699, Color.argb(0x80336699).getRGB());
		Assert.assertEquals(0xFFFF0000, Color.RED.getRGB());
	}

	@Test
	public void keepsTheZeroAlphaOfAnInteger() {
		final Color color = Color.argb(0x336699);
		Assert.assertEquals(0F, color.getAlphaF(), 0F);
		Assert.assertEquals(51, color.getRed());
		Assert.assertEquals(0x00336699, color.getRGB());
	}

	@Test
	public void copiesTheComponentsOfAnotherColor() {
		final Color color = new Color(Color.ORANGE);
		Assert.assertNotSame(Color.ORANGE, color);
		Assert.assertEquals(Color.ORANGE, color);
	}

	@Test
	public void copiesTheGradientAndTheUpdateOfAnotherColor() {
		final Color gradient = new Color(Color.gradient(Color.RED, Color.BLUE, Point.create(0D, 0D), Point.create(1D, 0D)));
		Assert.assertTrue(gradient.isGradient());
		Assert.assertEquals(Color.BLUE, gradient.getGradient().getEndColor());
		Assert.assertEquals(new Color(1F, 0F, 0F, 0.5F), new Color(new Color(0F, 0F, 0F, 0.5F, color -> new Color(1F, 0F, 0F, color.getAlphaF()))).update());
	}

	@Test
	public void isAPureOrange() {
		Assert.assertEquals(new Color(1F, 0.5F, 0F, 1F), Color.ORANGE);
	}

	@Test
	public void readsItsComponentsAsFloats() {
		final Color color = new Color(0.25F, 0.5F, 0.75F, 0.125F);
		Assert.assertEquals(0.25F, color.getRedF(), 0F);
		Assert.assertEquals(0.5F, color.getGreenF(), 0F);
		Assert.assertEquals(0.75F, color.getBlueF(), 0F);
		Assert.assertEquals(0.125F, color.getAlphaF(), 0F);
		Assert.assertNull(color.getGradient());
	}

	@Test
	public void darkensAndBrightensAGradient() {
		final Color gradient = Color.gradient(new Color(1F, 0.5F, 0F, 1F), new Color(0F, 0.5F, 1F, 0.5F), Point.create(0D, 0D), Point.create(1D, 0D));
		final Color darker = gradient.darker(0.5F);
		Assert.assertTrue(darker.isGradient());
		Assert.assertEquals(new Color(0.5F, 0.25F, 0F, 1F), darker.getGradient().getStartColor());
		Assert.assertEquals(new Color(0F, 0.25F, 0.5F, 0.5F), darker.getGradient().getEndColor());
		Assert.assertEquals(Point.create(1D, 0D), darker.getGradient().getEndPoint());
		Assert.assertEquals(new Color(1F, 0.75F, 0F, 1F), gradient.brighter(0.5F).getGradient().getStartColor());
	}

	@Test
	public void multipliesAGradient() {
		final Color gradient = Color.gradient(Color.WHITE, Color.GRAY, Point.create(0D, 0D), Point.create(1D, 0D));
		final Color multiplied = gradient.multiply(new Color(1F, 0.5F, 0F, 1F));
		Assert.assertEquals(new Color(1F, 0.5F, 0F, 1F), multiplied.getGradient().getStartColor());
		Assert.assertEquals(new Color(0.5F, 0.25F, 0F, 1F), multiplied.getGradient().getEndColor());
	}

	@Test
	public void addsToAndScalesAGradient() {
		final Color gradient = Color.gradient(Color.BLACK, Color.GRAY, Point.create(0D, 0D), Point.create(1D, 0D));
		final Color added = gradient.add(new Color(0.25F, 0F, 0F, 0F));
		Assert.assertTrue(added.isGradient());
		Assert.assertEquals(new Color(0.25F, 0F, 0F, 1F), added.getGradient().getStartColor());
		Assert.assertEquals(new Color(0.75F, 0.5F, 0.5F, 1F), added.getGradient().getEndColor());
		final Color scaled = gradient.scale(0.5F);
		Assert.assertTrue(scaled.isGradient());
		Assert.assertEquals(new Color(0F, 0F, 0F, 0.5F), scaled.getGradient().getStartColor());
		Assert.assertEquals(new Color(0.25F, 0.25F, 0.25F, 0.5F), scaled.getGradient().getEndColor());
	}

	@Test
	public void replacesOneComponentOfAGradient() {
		final Color gradient = Color.gradient(Color.BLACK, Color.WHITE, Point.create(0D, 0D), Point.create(1D, 0D));
		Assert.assertEquals(new Color(1F, 0F, 0F, 1F), gradient.withRed(1F).getGradient().getStartColor());
		Assert.assertEquals(new Color(1F, 0F, 1F, 1F), gradient.withGreen(0F).getGradient().getEndColor());
		Assert.assertEquals(new Color(1F, 1F, 0F, 1F), gradient.withBlue(0F).getGradient().getEndColor());
	}

	@Test
	public void darkensTheFrameOfItsUpdate() {
		final Color animated = new Color(0F, 0F, 0F, 0.5F, color -> new Color(1F, 1F, 0F, color.getAlphaF()));
		Assert.assertEquals(new Color(0.5F, 0.5F, 0F, 0.5F), animated.darker(0.5F).update());
		Assert.assertEquals(new Color(1F, 1F, 0F, 0.5F), animated.brighter(0.5F).update());
		Assert.assertEquals(new Color(0.5F, 0F, 0F, 0.5F), animated.multiply(new Color(0.5F, 0F, 1F, 1F)).update());
		Assert.assertEquals(new Color(1F, 1F, 0F, 0.5F), animated.update());
	}

	@Test
	public void keepsTheRainbowAnimatedWhenDarkened() {
		Assert.assertNotEquals(Color.RAINBOW.darker(), Color.RAINBOW.darker().update());
	}

	@Test
	public void readsFourFloatsFromABuffer() {
		final FloatBuffer buffer = FloatBuffer.wrap(new float[] {0.1F, 0.2F, 0.3F, 0.4F, 0.5F});
		Assert.assertEquals(new Color(0.1F, 0.2F, 0.3F, 0.4F), new Color(buffer));
		Assert.assertEquals(4, buffer.position());
	}

	@Test
	public void convertsAnAwtColor() {
		Assert.assertEquals(new Color(255, 128, 0, 64), new Color(new java.awt.Color(255, 128, 0, 64)));
	}

	@Test
	public void takesTheStartOfAGradient() {
		final ColorGradient gradient = new ColorGradient(Color.RED, Color.BLUE, Point.create(0D, 0D), Point.create(1D, 0D));
		final Color color = new Color(gradient);
		Assert.assertSame(gradient, color.getGradient());
		Assert.assertEquals(Color.RED, color);
	}

	@Test
	public void scalesIntegerComponentsDownToOne() {
		Assert.assertEquals(new Color(1F, 0.2F, 0F, 1F), new Color(255, 51, 0));
		Assert.assertEquals(new Color(1F, 0.2F, 0F, 0.4F), new Color(255, 51, 0, 102));
	}

	@Test
	public void startsOpaqueWithoutAlpha() {
		Assert.assertEquals(1F, new Color(0.25F, 0.5F, 0.75F).getAlphaF(), 0F);
	}

	@Test
	public void capsItsComponentsAtOne() {
		Assert.assertEquals(Color.WHITE, new Color(2F, 1.5F, 1F, 3F));
		Assert.assertEquals(Color.WHITE, new Color(2F, 1.5F, 1F, 3F, color -> color.withRed(0F)));
		Assert.assertEquals(Color.WHITE, new Color(300, 256, 255, 1000));
	}

	@Test
	public void raisesItsComponentsToZero() {
		Assert.assertEquals(Color.TRANSPARENT, new Color(-1F, -0.5F, -0F, -2F));
		Assert.assertEquals(new Color(0F, 0F, 0F, 1F), new Color(-10, -1, 0));
	}

	@Test
	public void keepsEveryDerivedColorBetweenZeroAndOne() {
		final Color color = new Color(0.5F, 0.75F, 0.25F, 0.5F);
		Assert.assertEquals(new Color(1F, 1F, 0.5F, 1F), color.scale(2F));
		Assert.assertEquals(Color.TRANSPARENT, color.scale(-1F));
		Assert.assertEquals(new Color(1F, 1F, 0.5F, 1F), color.add(new Color(0.75F, 0.75F, 0.25F, 0.75F)));
		Assert.assertEquals(new Color(0F, 0F, 0F, 0.5F), color.darker(2F));
		Assert.assertEquals(1F, color.withAlpha(2F).getAlphaF(), 0F);
		Assert.assertEquals(0F, color.withRed(-1F).getRedF(), 0F);
		Assert.assertEquals(1F, color.withGreen(2F).getGreenF(), 0F);
		Assert.assertEquals(0F, color.withBlue(-1F).getBlueF(), 0F);
		Assert.assertEquals(Color.RED, Color.RED.to(Color.BLUE, -1F));
		Assert.assertEquals(Color.BLUE, Color.RED.to(Color.BLUE, 2F));
	}

	@Test
	public void fillsAGray() {
		Assert.assertEquals(new Color(51, 51, 51), Color.fill(51));
		Assert.assertEquals(new Color(0.25F, 0.25F, 0.25F, 1F), Color.fill(0.25F));
	}

	@Test
	public void decodesAHexCode() {
		Assert.assertEquals(Color.rgb(0x336699), Color.decode("#336699"));
		Assert.assertEquals(Color.rgb(0x336699), Color.decode("336699"));
		Assert.assertEquals(Color.rgb(0xFFAA00), Color.decode("#ffaa00"));
	}

	@Test
	public void decodesAStringSurroundedByWhitespace() {
		Assert.assertEquals(Color.rgb(0x336699), Color.decode("  #336699\t"));
		Assert.assertEquals(new Color(255, 128, 0), Color.decode(" rgb(255, 128, 0) "));
		Assert.assertEquals(Color.BLUE, Color.decode("\ngradient(#FF0000, #0000FF) ").getGradient().getEndColor());
		Assert.assertEquals(Color.RAINBOW, Color.decode(" rainbow "));
	}

	@Test
	public void decodesAHexCodeWithAlpha() {
		Assert.assertEquals(new Color(51, 102, 153, 128), Color.decode("#33669980"));
	}

	@Test(expected = NumberFormatException.class)
	public void refusesAHexCodeOfAnotherLength() {
		Color.decode("#36F");
	}

	@Test(expected = NumberFormatException.class)
	public void refusesAnInvalidHexCode() {
		Color.decode("#GG6699");
	}

	@Test(expected = NumberFormatException.class)
	public void refusesANegativeHexCode() {
		Color.decode("#-12345");
	}

	@Test
	public void decodesRgbFunctions() {
		Assert.assertEquals(new Color(255, 128, 0), Color.decode("rgb(255, 128, 0)"));
		Assert.assertEquals(new Color(1F, 128 / 255F, 0F, 0.25F), Color.decode("rgba(255,128,0,0.25)"));
	}

	@Test
	public void readsTheAlphaOfRgbaAsAFraction() {
		Assert.assertEquals(0.5F, Color.decode("rgba(0, 0, 0, 0.5)").getAlphaF(), 0F);
		Assert.assertEquals(1F, Color.decode("rgba(0, 0, 0, 1)").getAlphaF(), 0F);
		Assert.assertEquals(0F, Color.decode("rgba(0, 0, 0, 0)").getAlphaF(), 0F);
		Assert.assertEquals(1F, Color.decode("rgba(0, 0, 0, 64)").getAlphaF(), 0F);
	}

	@Test
	public void decodesFunctionsWhateverTheCaseOfTheirName() {
		Assert.assertEquals(new Color(255, 128, 0), Color.decode("RGB(255, 128, 0)"));
		Assert.assertEquals(new Color(1F, 128 / 255F, 0F, 0.5F), Color.decode("Rgba(255, 128, 0, 0.5)"));
		Assert.assertEquals(Color.BLUE, Color.decode("GRADIENT(#FF0000, #0000FF)").getGradient().getEndColor());
	}

	@Test(expected = NumberFormatException.class)
	public void refusesAFunctionWithAMissingComponent() {
		Color.decode("rgb(255, 128)");
	}

	@Test(expected = NumberFormatException.class)
	public void refusesAFunctionWithoutItsClosingParenthesis() {
		Color.decode("rgb(255, 128, 0");
	}

	@Test
	public void decodesAGradient() {
		final Color color = Color.decode("gradient(#FF0000, #0000FF)");
		Assert.assertTrue(color.isGradient());
		Assert.assertEquals(Color.RED, color.getGradient().getStartColor());
		Assert.assertEquals(Color.BLUE, color.getGradient().getEndColor());
		Assert.assertEquals(Point.create(0D, 0D), color.getGradient().getStartPoint());
		Assert.assertEquals(Point.create(1D, 0D), color.getGradient().getEndPoint());
	}

	@Test
	public void decodesTheDirectionOfAGradient() {
		Assert.assertEquals(Point.create(1D, 0D), Color.decode("gradient(#FF0000, #0000FF, 1, 0, 0.5, 1)").getGradient().getStartPoint());
		Assert.assertEquals(Point.create(0.5D, 1D), Color.decode("gradient(#FF0000, #0000FF, 1, 0, 0.5, 1)").getGradient().getEndPoint());
	}

	@Test
	public void decodesAGradientBetweenFunctions() {
		final Color color = Color.decode("gradient(rgb(255, 0, 0), rgba(0, 0, 255, 0.5), 0, 0, 0, 1)");
		Assert.assertEquals(Color.RED, color.getGradient().getStartColor());
		Assert.assertEquals(new Color(0F, 0F, 1F, 0.5F), color.getGradient().getEndColor());
		Assert.assertEquals(Point.create(0D, 0D), color.getGradient().getStartPoint());
		Assert.assertEquals(Point.create(0D, 1D), color.getGradient().getEndPoint());
	}

	@Test
	public void decodesANestedGradient() {
		final Color color = Color.decode("gradient(gradient(#FF0000, #00FF00), #0000FF)");
		Assert.assertTrue(color.getGradient().getStartColor().isGradient());
		Assert.assertEquals(Color.GREEN, color.getGradient().getStartColor().getGradient().getEndColor());
	}

	@Test(expected = NumberFormatException.class)
	public void refusesAGradientWithAnIncompleteDirection() {
		Color.decode("gradient(#FF0000, #0000FF, 1, 0)");
	}

	@Test
	public void decodesTheAnimatedColors() {
		Assert.assertEquals(Color.RAINBOW, Color.decode("#Rainbow"));
		Assert.assertEquals(Color.LOADING, Color.decode("LOADING"));
		Assert.assertFalse(Color.decode("#Rainbow").isLocked());
		Assert.assertFalse(Color.decode("LOADING").isLocked());
		ColorTest.assertRainbow(Color.decode("#Rainbow").update());
		ColorTest.assertLoading(Color.decode("LOADING").update());
	}

	@Test
	public void animatesADecodedRainbow() {
		final Color rainbow = Color.decode("rainbow");
		final Color first = rainbow.update();
		this.bridges.getClock().advance(1000L);
		Assert.assertNotEquals(first, rainbow.update());
		Assert.assertEquals(Color.RAINBOW(1000L), rainbow.update());
	}

	@Test
	public void keepsTheAlphaOfAFadedRainbow() {
		Assert.assertEquals(0.5F, Color.RAINBOW.withAlpha(0.5F).update().getAlphaF(), 0F);
		Assert.assertEquals(0.5F, Color.LOADING.withAlpha(0.5F).update().getAlphaF(), 0F);
	}

	@Test
	public void cyclesTheRainbowEveryThreeSeconds() {
		Assert.assertEquals(new Color(204, 41, 41), Color.RAINBOW(0L));
		Assert.assertEquals(new Color(41, 204, 41), Color.RAINBOW(1000L));
		Assert.assertEquals(new Color(41, 204, 204), Color.RAINBOW(1500L));
		Assert.assertEquals(Color.RAINBOW(500L), Color.RAINBOW(3500L));
	}

	@Test
	public void followsTheClockWithTheRainbow() {
		ColorTest.assertRainbow(Color.RAINBOW());
		ColorTest.assertRainbow(Color.RAINBOW.copy().update());
	}

	@Test
	public void pulsesADarkGrayWhileLoading() {
		ColorTest.assertLoading(Color.LOADING());
		ColorTest.assertLoading(Color.LOADING.copy().update());
	}

	@Test
	public void keepsTheEndsOfATransition() {
		Assert.assertEquals(Color.RED, Color.transition(Color.RED, Color.BLUE, 0F));
		Assert.assertEquals(Color.BLUE, Color.transition(Color.RED, Color.BLUE, 1F));
		Assert.assertFalse(Color.transition(Color.RED, Color.BLUE, 0F).isLocked());
		Assert.assertFalse(Color.transition(Color.RED, Color.BLUE, 1F).isLocked());
	}

	@Test
	public void blendsTwoColors() {
		Assert.assertEquals(new Color(0.75F, 0F, 0.25F, 0.875F), Color.transition(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), 0.25F));
		Assert.assertEquals(new Color(0.5F, 0.5F, 0F, 1F), Color.RED.to(Color.GREEN, 0.5F));
	}

	@Test
	public void keepsTheUpdateOfTheClosestColor() {
		final UnaryOperator<Color> first = color -> color.withRed(1F);
		final UnaryOperator<Color> second = color -> color.withBlue(1F);
		final Color from = new Color(1F, 0F, 0F, 1F, first);
		final Color to = new Color(0F, 0F, 1F, 1F, second);
		Assert.assertEquals(new Color(1F, 0F, 0.5F, 1F), Color.transition(from, to, 0.5F).update());
		Assert.assertEquals(new Color(0.25F, 0F, 1F, 1F), Color.transition(from, to, 0.75F).update());
	}

	@Test
	public void blendsTwoGradients() {
		final Color from = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 1F, 0F, 1F), Point.create(0D, 0D), Point.create(1D, 0D));
		final Color to = Color.gradient(new Color(0F, 0F, 1F, 1F), new Color(1F, 1F, 1F, 0F), Point.create(1D, 0D), Point.create(0D, 1D));
		final Color color = Color.transition(from, to, 0.5F);
		Assert.assertEquals(new Color(0.5F, 0F, 0.5F, 1F), color.getGradient().getStartColor());
		Assert.assertEquals(new Color(0.5F, 1F, 0.5F, 0.5F), color.getGradient().getEndColor());
		Assert.assertEquals(Point.create(0.5D, 0D), color.getGradient().getStartPoint());
		Assert.assertEquals(Point.create(0.5D, 0.5D), color.getGradient().getEndPoint());
	}

	@Test
	public void blendsAGradientIntoAColor() {
		final Point startPoint = Point.create(0D, 0D);
		final Point endPoint = Point.create(1D, 0D);
		final Color color = Color.transition(Color.gradient(Color.RED, Color.BLUE, startPoint, endPoint), Color.WHITE, 0.5F);
		Assert.assertEquals(new Color(1F, 0.5F, 0.5F, 1F), color.getGradient().getStartColor());
		Assert.assertEquals(new Color(0.5F, 0.5F, 1F, 1F), color.getGradient().getEndColor());
		Assert.assertSame(startPoint, color.getGradient().getStartPoint());
		Assert.assertSame(endPoint, color.getGradient().getEndPoint());
	}

	@Test
	public void blendsAColorIntoAGradient() {
		final Point startPoint = Point.create(0D, 0D);
		final Point endPoint = Point.create(1D, 0D);
		final Color color = Color.transition(Color.WHITE, Color.gradient(Color.RED, Color.BLUE, startPoint, endPoint), 0.5F);
		Assert.assertEquals(new Color(1F, 0.5F, 0.5F, 1F), color.getGradient().getStartColor());
		Assert.assertEquals(new Color(0.5F, 0.5F, 1F, 1F), color.getGradient().getEndColor());
		Assert.assertSame(startPoint, color.getGradient().getStartPoint());
		Assert.assertSame(endPoint, color.getGradient().getEndPoint());
	}

	@Test
	public void startsAGradientWithItsFirstColor() {
		final Point startPoint = Point.create(1D, 0D);
		final Point endPoint = Point.create(0D, 1D);
		final Color gradient = Color.gradient(Color.RED, Color.BLUE, startPoint, endPoint);
		Assert.assertTrue(gradient.isGradient());
		Assert.assertEquals(Color.RED, gradient);
		Assert.assertSame(Color.RED, gradient.getGradient().getStartColor());
		Assert.assertSame(Color.BLUE, gradient.getGradient().getEndColor());
		Assert.assertSame(startPoint, gradient.getGradient().getStartPoint());
		Assert.assertSame(endPoint, gradient.getGradient().getEndPoint());
	}

	@Test
	public void turnsIntoAGradient() {
		final Point startPoint = Point.create(1D, 0D);
		final Point endPoint = Point.create(0D, 1D);
		Assert.assertSame(Color.BLUE, Color.RED.toGradient(Color.BLUE).getGradient().getEndColor());
		Assert.assertEquals(Point.create(0D, 0D), Color.RED.toGradient(Color.BLUE).getGradient().getStartPoint());
		Assert.assertEquals(Point.create(1D, 0D), Color.RED.toGradient(Color.BLUE).getGradient().getEndPoint());
		Assert.assertSame(startPoint, Color.RED.toGradient(Color.BLUE, startPoint, endPoint).getGradient().getStartPoint());
		Assert.assertSame(endPoint, Color.RED.toGradient(Color.BLUE, startPoint, endPoint).getGradient().getEndPoint());
	}

	@Test
	public void encodesItselfAsAnUppercaseHexCode() {
		Assert.assertEquals("#FF7F00FF", new Color(1F, 0.5F, 0F, 1F).encode());
		Assert.assertEquals("#0A0B0C80", new Color(10, 11, 12, 128).encode());
	}

	@Test
	public void describesItself() {
		Assert.assertEquals("Color(255, 127, 0, 255) [#FF7F00FF]", new Color(1F, 0.5F, 0F, 1F).toString());
	}

	@Test
	public void darkensByHalfByDefault() {
		Assert.assertEquals(new Color(0.4F, 0.2F, 0.1F, 0.5F), new Color(0.8F, 0.4F, 0.2F, 0.5F).darker());
	}

	@Test
	public void darkensByAFraction() {
		final Color color = new Color(0.8F, 0.4F, 0.2F, 0.5F).darker(0.25F);
		Assert.assertEquals(0.6F, color.getRedF(), 0.0001F);
		Assert.assertEquals(0.3F, color.getGreenF(), 0.0001F);
		Assert.assertEquals(0.15F, color.getBlueF(), 0.0001F);
		Assert.assertEquals(0.5F, color.getAlphaF(), 0F);
	}

	@Test
	public void brightensByAFifthByDefault() {
		final Color color = new Color(0.5F, 0.25F, 0.9F, 0.5F).brighter();
		Assert.assertEquals(0.6F, color.getRedF(), 0.0001F);
		Assert.assertEquals(0.3F, color.getGreenF(), 0.0001F);
		Assert.assertEquals(1F, color.getBlueF(), 0F);
		Assert.assertEquals(0.5F, color.getAlphaF(), 0F);
	}

	@Test
	public void brightensByAFraction() {
		Assert.assertEquals(new Color(0.5F, 1F, 1F, 0.5F), new Color(0.25F, 0.5F, 0.75F, 0.5F).brighter(1F));
	}

	@Test
	public void multipliesEveryComponent() {
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.25F, 0.5F), new Color(0.5F, 1F, 0.25F, 1F).multiply(new Color(0.5F, 0.5F, 1F, 0.5F)));
	}

	@Test
	public void changesItselfThroughItsSetters() {
		final Color color = new Color(0.25F, 0.5F, 0.75F, 1F);
		Assert.assertSame(color, color.red(1F).green(0F).blue(0.5F).alpha(0.5F));
		Assert.assertEquals(new Color(1F, 0F, 0.5F, 0.5F), color);
	}

	@Test
	public void keepsItsComponentsBetweenZeroAndOneThroughItsSetters() {
		Assert.assertEquals(new Color(1F, 0F, 1F, 0F), new Color(0.5F, 0.5F, 0.5F, 0.5F).red(2F).green(-1F).blue(1.5F).alpha(-0.5F));
	}

	@Test
	public void fadesAGradientThroughItsSetter() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), Point.create(0D, 0D), Point.create(1D, 0D));
		gradient.alpha(0.5F);
		Assert.assertEquals(0.5F, gradient.getAlphaF(), 0F);
		Assert.assertEquals(0.5F, gradient.getGradient().getStartColor().getAlphaF(), 0F);
		Assert.assertEquals(0.25F, gradient.getGradient().getEndColor().getAlphaF(), 0F);
	}

	@Test
	public void locksItsConstants() {
		for (final Color color : new Color[] {Color.RED, Color.BLUE, Color.GRAY, Color.CYAN, Color.PINK, Color.WHITE, Color.GREEN, Color.BLACK, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.DARKGRAY, Color.LIGHTGRAY, Color.TRANSPARENT, Color.RAINBOW, Color.LOADING}) {
			Assert.assertTrue(color.toString(), color.isLocked());
		}
	}

	@Test
	public void refusesToChangeAConstant() {
		try {
			Color.RED.alpha(0.5F);
			Assert.fail("Color.RED must refuse a change");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("Color.RED is a constant, use withAlpha(...) to get a modified copy", expected.getMessage());
		}
		Assert.assertEquals(new Color(1F, 0F, 0F, 1F), Color.RED);
	}

	@Test
	public void refusesChangesOnceLocked() {
		final Color color = new Color(0.25F, 0.5F, 0.75F, 1F);
		Assert.assertSame(color, color.lock());
		try {
			color.green(1F);
			Assert.fail("A locked color must refuse a change");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("This Color is locked, use withGreen(...) to get a modified copy", expected.getMessage());
		}
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.75F, 1F), color);
	}

	@Test
	public void unlocksItsCopies() {
		Assert.assertFalse(Color.RED.copy().isLocked());
		Assert.assertFalse(Color.RED.withAlpha(0.5F).isLocked());
		Assert.assertFalse(Color.RED.darker().isLocked());
		Assert.assertEquals(new Color(1F, 0F, 0F, 0.5F), Color.RED.copy().alpha(0.5F));
	}

	@Test
	public void keepsItsPresetsUnchanged() {
		Color.RED.add(Color.BLUE);
		Color.RED.scale(0.5F);
		Color.RED.withAlpha(0.5F);
		Color.RAINBOW.update();
		Color.LOADING.update();
		Assert.assertEquals(new Color(1F, 0F, 0F, 1F), Color.RED);
		Assert.assertEquals(Color.WHITE, Color.RAINBOW);
		Assert.assertEquals(Color.WHITE, Color.LOADING);
	}

	@Test
	public void addsAnotherColorToACopy() {
		final UnaryOperator<Color> update = color -> color.withRed(1F);
		final Color color = new Color(0.25F, 0.5F, 0F, 0.5F, update);
		final Color sum = color.add(new Color(0.5F, 0.25F, 0.25F, 0.5F));
		Assert.assertEquals(new Color(0.75F, 0.75F, 0.25F, 1F), sum);
		Assert.assertEquals(new Color(1F, 0.75F, 0.25F, 1F), sum.update());
		Assert.assertEquals(new Color(0.25F, 0.5F, 0F, 0.5F), color);
	}

	@Test
	public void scalesACopy() {
		final UnaryOperator<Color> update = color -> color.withRed(1F);
		final Color color = new Color(0.5F, 1F, 0.25F, 1F, update);
		final Color scaled = color.scale(0.5F);
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.125F, 0.5F), scaled);
		Assert.assertEquals(new Color(1F, 0.5F, 0.125F, 0.5F), scaled.update());
		Assert.assertEquals(new Color(0.5F, 1F, 0.25F, 1F), color);
	}

	@Test
	public void convertsItselfToHsb() {
		Assert.assertArrayEquals(new float[] {1F / 18F, 0.75F, 0.8F}, new Color(204, 102, 51).RGBtoHSB(null), 0.0001F);
		Assert.assertArrayEquals(new float[] {7F / 18F, 0.75F, 0.8F}, new Color(51, 204, 102).RGBtoHSB(null), 0.0001F);
		Assert.assertArrayEquals(new float[] {11F / 18F, 0.75F, 0.8F}, new Color(51, 102, 204).RGBtoHSB(null), 0.0001F);
		Assert.assertArrayEquals(new float[] {0.91634F, 1F, 1F}, new Color(255, 0, 128).RGBtoHSB(null), 0.0001F);
	}

	@Test
	public void convertsComponentsToHsb() {
		Assert.assertArrayEquals(new float[] {1F / 18F, 0.75F, 0.8F}, Color.RGBtoHSB(204, 102, 51, null), 0.0001F);
		Assert.assertArrayEquals(new float[] {7F / 18F, 0.75F, 0.8F}, Color.RGBtoHSB(51, 204, 102, null), 0.0001F);
		Assert.assertArrayEquals(new float[] {11F / 18F, 0.75F, 0.8F}, Color.RGBtoHSB(51, 102, 204, null), 0.0001F);
		Assert.assertArrayEquals(new float[] {0.91634F, 1F, 1F}, Color.RGBtoHSB(255, 0, 128, null), 0.0001F);
	}

	@Test
	public void hasNoHueWithoutSaturation() {
		Assert.assertArrayEquals(new float[] {0F, 0F, 0F}, Color.BLACK.RGBtoHSB(null), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F, 128F / 255F}, new Color(128, 128, 128).RGBtoHSB(null), 0.0001F);
		Assert.assertArrayEquals(new float[] {0F, 0F, 0F}, Color.RGBtoHSB(0, 0, 0, null), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F, 128F / 255F}, Color.RGBtoHSB(128, 128, 128, null), 0.0001F);
	}

	@Test
	public void fillsTheGivenHsbArray() {
		final float[] values = new float[3];
		Assert.assertSame(values, Color.GREEN.RGBtoHSB(values));
		Assert.assertArrayEquals(new float[] {1F / 3F, 1F, 1F}, values, 0.0001F);
		Assert.assertSame(values, Color.RGBtoHSB(255, 0, 0, values));
		Assert.assertArrayEquals(new float[] {0F, 1F, 1F}, values, 0F);
	}

	@Test
	public void copiesAColorWithItsUpdate() {
		final UnaryOperator<Color> update = color -> color.withRed(1F);
		final Color color = new Color(0.25F, 0.5F, 0.75F, 0.5F, update);
		final Color copy = color.copy();
		Assert.assertNotSame(color, copy);
		Assert.assertEquals(color, copy);
		Assert.assertEquals(new Color(1F, 0.5F, 0.75F, 0.5F), copy.update());
	}

	@Test
	public void replacesOneComponentInACopy() {
		final UnaryOperator<Color> update = color -> color.withRed(1F);
		final Color color = new Color(0.25F, 0.5F, 0.75F, 0.5F, update);
		Assert.assertEquals(new Color(1F, 0.5F, 0.75F, 0.5F), color.withRed(1F));
		Assert.assertEquals(new Color(0.25F, 0F, 0.75F, 0.5F), color.withGreen(0F));
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.125F, 0.5F), color.withBlue(0.125F));
		Assert.assertEquals(new Color(1F, 0.5F, 0.75F, 0.5F), color.withRed(1F).update());
		Assert.assertEquals(new Color(1F, 0F, 0.75F, 0.5F), color.withGreen(0F).update());
		Assert.assertEquals(new Color(1F, 0.5F, 0.125F, 0.5F), color.withBlue(0.125F).update());
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.75F, 0.5F), color);
	}

	@Test
	public void returnsTheFrameOfItsUpdate() {
		final Color animated = new Color(0F, 0F, 0F, 0.5F, color -> new Color(1F, 0F, 0F, color.getAlphaF()));
		Assert.assertEquals(new Color(1F, 0F, 0F, 0.5F), animated.update());
		Assert.assertEquals(0F, animated.getRedF(), 0F);
	}

	@Test
	public void staysTheSameWithoutUpdate() {
		final Color color = new Color(0.5F, 0.5F, 0.5F);
		Assert.assertSame(color, color.update());
		Assert.assertEquals(new Color(0.5F, 0.5F, 0.5F), color);
	}

	@Test
	public void equalsAColorWithTheSameComponents() {
		Assert.assertEquals(new Color(1F, 0F, 0F, 1F), Color.RED);
		Assert.assertEquals(new Color(1F, 0F, 0F, 1F).hashCode(), Color.RED.hashCode());
		Assert.assertNotEquals(new Color(0.5F, 0F, 0F, 1F), Color.RED);
		Assert.assertNotEquals(new Color(1F, 0.5F, 0F, 1F), Color.RED);
		Assert.assertNotEquals(new Color(1F, 0F, 0.5F, 1F), Color.RED);
		Assert.assertNotEquals(new Color(1F, 0F, 0F, 0.5F), Color.RED);
		Assert.assertNotEquals(Color.RED, "#FF0000FF");
	}

	@Test
	public void hashesEveryComponent() {
		Assert.assertNotEquals(new Color(0.2F, 0.4F, 0F, 1F).hashCode(), new Color(0.4F, 0.2F, 0F, 1F).hashCode());
		Assert.assertNotEquals(new Color(0F, 0.2F, 0.4F, 1F).hashCode(), new Color(0F, 0.4F, 0.2F, 1F).hashCode());
		Assert.assertNotEquals(new Color(0.5F, 0.5F, 0.5F, 1F).hashCode(), new Color(0.5F, 0.5F, 0.5F, 0.9F).hashCode());
		Assert.assertNotEquals(new Color(0.1F, 0F, 0F, 1F).hashCode(), new Color(0.2F, 0F, 0F, 1F).hashCode());
	}

	@Test
	public void hashesLikeAnEqualColor() {
		final Color animated = new Color(0.25F, 0.5F, 0.75F, 1F, color -> Color.RED);
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.75F, 1F), animated);
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.75F, 1F).hashCode(), animated.hashCode());
		Assert.assertEquals(Color.RED.hashCode(), Color.RED.toGradient(Color.BLUE).hashCode());
	}

	private static void assertRainbow(final Color color) {
		final float[] hsb = color.RGBtoHSB(null);
		Assert.assertEquals(0.8F, hsb[1], 0.01F);
		Assert.assertEquals(0.8F, hsb[2], 0.01F);
		Assert.assertEquals(1F, color.getAlphaF(), 0F);
	}

	private static void assertLoading(final Color color) {
		Assert.assertEquals(color.getRedF(), color.getGreenF(), 0F);
		Assert.assertEquals(color.getRedF(), color.getBlueF(), 0F);
		Assert.assertEquals(0.17F, color.getRedF(), 0.0201F);
		Assert.assertEquals(1F, color.getAlphaF(), 0F);
	}

	@Test
	public void bindsItsComponentsOnTheRenderer() {
		new Color(0.2F, 0.4F, 0.6F, 0.8F).bind();
		final RenderState state = this.bridges.getRender().getState();
		Assert.assertEquals(0.2F, state.getRed(), 0F);
		Assert.assertEquals(0.4F, state.getGreen(), 0F);
		Assert.assertEquals(0.6F, state.getBlue(), 0F);
		Assert.assertEquals(0.8F, state.getAlpha(), 0F);
	}

	@Test
	public void bindsTheFrameOfAnAnimatedColor() {
		new Color(0F, 0F, 0F, 1F, color -> new Color(0.2F, 0.4F, 0.6F, 0.8F)).bind();
		final RenderState state = this.bridges.getRender().getState();
		Assert.assertEquals(0.2F, state.getRed(), 0F);
		Assert.assertEquals(0.8F, state.getAlpha(), 0F);
	}

	@Test
	public void resetsTheRendererToWhite() {
		new Color(0.2F, 0.4F, 0.6F, 0.8F).bind();
		Color.reset();
		final RenderState state = this.bridges.getRender().getState();
		Assert.assertEquals(1F, state.getRed(), 0F);
		Assert.assertEquals(1F, state.getAlpha(), 0F);
	}

	@Test
	public void drawsWithAPlainColorThenRestoresThePreviousOne() {
		final float[] seen = new float[1];
		final RenderState state = this.bridges.getRender().getState().color(0.1F, 0.3F, 0.5F, 0.7F);
		new Color(0.2F, 0.4F, 0.6F, 0.8F).bind(() -> seen[0] = state.getBlue(), BoundingBox.create(0D, 0D, 10D, 10D));
		Assert.assertEquals(0.6F, seen[0], 0F);
		Assert.assertEquals(0.1F, state.getRed(), 0F);
		Assert.assertEquals(0.3F, state.getGreen(), 0F);
		Assert.assertEquals(0.5F, state.getBlue(), 0F);
		Assert.assertEquals(0.7F, state.getAlpha(), 0F);
	}

	@Test
	public void drawsAGradientThroughItsShaderAndRestoresThePreviousOne() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 1F), Point.create(0D, 0D), Point.create(1D, 0D));
		final boolean[] shaded = new boolean[1];
		gradient.bind(() -> shaded[0] = this.bridges.getRender().getState().getShader() != null, BoundingBox.create(0D, 0D, 10D, 10D));
		Assert.assertTrue(shaded[0]);
		Assert.assertNull(this.bridges.getRender().getState().getShader());
	}

	@Test
	public void decodesAHexCodeWithAlphaAndABrightRed() {
		Assert.assertEquals(new Color(255, 0, 0, 128), Color.decode("#FF000080"));
	}

	@Test
	public void decodesItsOwnEncoding() {
		Assert.assertEquals(Color.WHITE, Color.decode(Color.WHITE.encode()));
	}

}