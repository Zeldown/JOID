package dev.joid.lib.color;

import java.lang.reflect.Modifier;
import java.nio.FloatBuffer;
import java.util.function.UnaryOperator;

import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.state.RenderState;

public class ColorTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void copiesAGradientWithItsEnds() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), new Vector4f(0F, 0F, 1F, 0F));
		final Color copy = gradient.copy();
		Assert.assertTrue(copy.isGradient());
		Assert.assertNotSame(gradient.gradient, copy.gradient);
		Assert.assertNotSame(gradient.gradient.getStartColor(), copy.gradient.getStartColor());
		Assert.assertEquals(0.5F, copy.gradient.getEndColor().a, 0F);
		Assert.assertEquals(1F, copy.gradient.getDirection().z, 0F);
	}

	@Test
	public void fadesAGradientWithoutLosingIt() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), new Vector4f(0F, 0F, 1F, 0F));
		final Color faded = gradient.copyAlpha(0.4F);
		Assert.assertTrue(faded.isGradient());
		Assert.assertEquals(0.4F, faded.a, 0F);
		Assert.assertEquals(0.4F, faded.gradient.getStartColor().a, 1E-6F);
		Assert.assertEquals(0.2F, faded.gradient.getEndColor().a, 1E-6F);
		Assert.assertEquals(1F, faded.gradient.getStartColor().r, 0F);
		Assert.assertEquals(1F, faded.gradient.getEndColor().b, 0F);
		Assert.assertEquals(1F, gradient.gradient.getStartColor().a, 0F);
	}

	@Test
	public void fadesAGradientThatStartsTransparent() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 0F), new Color(0F, 0F, 1F, 1F), new Vector4f(0F, 0F, 1F, 0F));
		final Color faded = gradient.copyAlpha(0.5F);
		Assert.assertEquals(0.5F, faded.gradient.getStartColor().a, 0F);
		Assert.assertEquals(0.5F, faded.gradient.getEndColor().a, 0F);
	}

	@Test
	public void keepsAPlainColorPlain() {
		final Color faded = new Color(1F, 0.5F, 0.25F, 1F).copyAlpha(0.3F);
		Assert.assertFalse(faded.isGradient());
		Assert.assertEquals(0.3F, faded.a, 0F);
		Assert.assertEquals(0.5F, faded.g, 0F);
		Assert.assertFalse(new Color(1F, 0.5F, 0.25F, 1F).copy().isGradient());
	}

	@Test
	public void readsAnRgbInteger() {
		final Color color = new Color(0xFF336699);
		Assert.assertEquals(51, color.getRed());
		Assert.assertEquals(102, color.getGreen());
		Assert.assertEquals(153, color.getBlue());
		Assert.assertEquals(255, color.getAlpha());
	}

	@Test
	public void readsTheAlphaOfAnArgbInteger() {
		Assert.assertEquals(128, new Color(0x80336699).getAlpha());
		Assert.assertEquals(1F, new Color(0xFF336699).a, 0F);
	}

	@Test
	public void writesBackItsArgbInteger() {
		Assert.assertEquals(0x80336699, new Color(0x80336699).getRGB());
		Assert.assertEquals(0xFFFF0000, Color.RED.getRGB());
	}

	@Test
	public void keepsTheZeroAlphaOfAnInteger() {
		final Color color = new Color(0x336699);
		Assert.assertEquals(0F, color.a, 0F);
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
		final ColorGradient gradient = new ColorGradient(Color.RED, Color.BLUE, new Vector4f(0F, 0F, 1F, 0F));
		final Color color = new Color(gradient);
		Assert.assertSame(gradient, color.gradient);
		Assert.assertEquals(Color.RED, color);
	}

	@Test
	public void scalesIntegerComponentsDownToOne() {
		Assert.assertEquals(new Color(1F, 0.2F, 0F, 1F), new Color(255, 51, 0));
		Assert.assertEquals(new Color(1F, 0.2F, 0F, 0.4F), new Color(255, 51, 0, 102));
	}

	@Test
	public void startsOpaqueWithoutAlpha() {
		Assert.assertEquals(1F, new Color(0.25F, 0.5F, 0.75F).a, 0F);
	}

	@Test
	public void capsItsComponentsAtOne() {
		Assert.assertEquals(Color.WHITE, new Color(2F, 1.5F, 1F, 3F));
		Assert.assertEquals(Color.WHITE, new Color(2F, 1.5F, 1F, 3F, color -> color.copyRed(0F)));
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
		Assert.assertEquals(new Color(1F, 1F, 0.5F, 1F), color.scaleCopy(2F));
		Assert.assertEquals(Color.TRANSPARENT, color.scaleCopy(-1F));
		Assert.assertEquals(new Color(1F, 1F, 0.5F, 1F), color.addToCopy(new Color(0.75F, 0.75F, 0.25F, 0.75F)));
		Assert.assertEquals(new Color(0F, 0F, 0F, 0.5F), color.darker(2F));
		Assert.assertEquals(1F, color.copyAlpha(2F).a, 0F);
		Assert.assertEquals(0F, color.copyRed(-1F).r, 0F);
		Assert.assertEquals(1F, color.copyGreen(2F).g, 0F);
		Assert.assertEquals(0F, color.copyBlue(-1F).b, 0F);
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
		Assert.assertEquals(new Color(0xFF336699), Color.decode("#336699"));
		Assert.assertEquals(new Color(0xFF336699), Color.decode("336699"));
		Assert.assertEquals(new Color(0xFFFFAA00), Color.decode("#ffaa00"));
	}

	@Test
	public void decodesAStringSurroundedByWhitespace() {
		Assert.assertEquals(new Color(0xFF336699), Color.decode("  #336699\t"));
		Assert.assertEquals(new Color(255, 128, 0), Color.decode(" rgb(255, 128, 0) "));
		Assert.assertEquals(Color.BLUE, Color.decode("\ngradient(#FF0000, #0000FF) ").gradient.getEndColor());
		Assert.assertSame(Color.RAINBOW, Color.decode(" rainbow "));
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
		Assert.assertEquals(0.5F, Color.decode("rgba(0, 0, 0, 0.5)").a, 0F);
		Assert.assertEquals(1F, Color.decode("rgba(0, 0, 0, 1)").a, 0F);
		Assert.assertEquals(0F, Color.decode("rgba(0, 0, 0, 0)").a, 0F);
		Assert.assertEquals(1F, Color.decode("rgba(0, 0, 0, 64)").a, 0F);
	}

	@Test
	public void decodesFunctionsWhateverTheCaseOfTheirName() {
		Assert.assertEquals(new Color(255, 128, 0), Color.decode("RGB(255, 128, 0)"));
		Assert.assertEquals(new Color(1F, 128 / 255F, 0F, 0.5F), Color.decode("Rgba(255, 128, 0, 0.5)"));
		Assert.assertEquals(Color.BLUE, Color.decode("GRADIENT(#FF0000, #0000FF)").gradient.getEndColor());
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
		Assert.assertEquals(Color.RED, color.gradient.getStartColor());
		Assert.assertEquals(Color.BLUE, color.gradient.getEndColor());
		Assert.assertEquals(new Vector4f(0F, 0F, 1F, 0F), color.gradient.getDirection());
	}

	@Test
	public void decodesTheDirectionOfAGradient() {
		Assert.assertEquals(new Vector4f(1F, 0F, 0.5F, 1F), Color.decode("gradient(#FF0000, #0000FF, 1, 0, 0.5, 1)").gradient.getDirection());
	}

	@Test
	public void decodesAGradientBetweenFunctions() {
		final Color color = Color.decode("gradient(rgb(255, 0, 0), rgba(0, 0, 255, 0.5), 0, 0, 0, 1)");
		Assert.assertEquals(Color.RED, color.gradient.getStartColor());
		Assert.assertEquals(new Color(0F, 0F, 1F, 0.5F), color.gradient.getEndColor());
		Assert.assertEquals(new Vector4f(0F, 0F, 0F, 1F), color.gradient.getDirection());
	}

	@Test
	public void decodesANestedGradient() {
		final Color color = Color.decode("gradient(gradient(#FF0000, #00FF00), #0000FF)");
		Assert.assertTrue(color.gradient.getStartColor().isGradient());
		Assert.assertEquals(Color.GREEN, color.gradient.getStartColor().gradient.getEndColor());
	}

	@Test(expected = NumberFormatException.class)
	public void refusesAGradientWithAnIncompleteDirection() {
		Color.decode("gradient(#FF0000, #0000FF, 1, 0)");
	}

	@Test
	public void decodesTheAnimatedColors() {
		Assert.assertSame(Color.RAINBOW, Color.decode("#Rainbow"));
		Assert.assertSame(Color.LOADING, Color.decode("LOADING"));
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
		Assert.assertEquals(0.5F, Color.RAINBOW.copyAlpha(0.5F).update().a, 0F);
		Assert.assertEquals(0.5F, Color.LOADING.copyAlpha(0.5F).update().a, 0F);
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
		Assert.assertSame(Color.RED, Color.transition(Color.RED, Color.BLUE, 0F));
		Assert.assertSame(Color.BLUE, Color.transition(Color.RED, Color.BLUE, 1F));
	}

	@Test
	public void blendsTwoColors() {
		Assert.assertEquals(new Color(0.75F, 0F, 0.25F, 0.875F), Color.transition(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 0.5F), 0.25F));
		Assert.assertEquals(new Color(0.5F, 0.5F, 0F, 1F), Color.RED.to(Color.GREEN, 0.5F));
	}

	@Test
	public void keepsTheUpdateOfTheClosestColor() {
		final UnaryOperator<Color> first = color -> color.copyRed(1F);
		final UnaryOperator<Color> second = color -> color.copyBlue(1F);
		final Color from = new Color(1F, 0F, 0F, 1F, first);
		final Color to = new Color(0F, 0F, 1F, 1F, second);
		Assert.assertSame(first, Color.transition(from, to, 0.5F).update);
		Assert.assertSame(second, Color.transition(from, to, 0.75F).update);
	}

	@Test
	public void blendsTwoGradients() {
		final Color from = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 1F, 0F, 1F), new Vector4f(0F, 0F, 1F, 0F));
		final Color to = Color.gradient(new Color(0F, 0F, 1F, 1F), new Color(1F, 1F, 1F, 0F), new Vector4f(1F, 0F, 0F, 1F));
		final Color color = Color.transition(from, to, 0.5F);
		Assert.assertEquals(new Color(0.5F, 0F, 0.5F, 1F), color.gradient.getStartColor());
		Assert.assertEquals(new Color(0.5F, 1F, 0.5F, 0.5F), color.gradient.getEndColor());
		Assert.assertEquals(new Vector4f(0.5F, 0F, 0.5F, 0.5F), color.gradient.getDirection());
	}

	@Test
	public void blendsAGradientIntoAColor() {
		final Vector4f direction = new Vector4f(0F, 0F, 1F, 0F);
		final Color color = Color.transition(Color.gradient(Color.RED, Color.BLUE, direction), Color.WHITE, 0.5F);
		Assert.assertEquals(new Color(1F, 0.5F, 0.5F, 1F), color.gradient.getStartColor());
		Assert.assertEquals(new Color(0.5F, 0.5F, 1F, 1F), color.gradient.getEndColor());
		Assert.assertSame(direction, color.gradient.getDirection());
	}

	@Test
	public void blendsAColorIntoAGradient() {
		final Vector4f direction = new Vector4f(0F, 0F, 1F, 0F);
		final Color color = Color.transition(Color.WHITE, Color.gradient(Color.RED, Color.BLUE, direction), 0.5F);
		Assert.assertEquals(new Color(1F, 0.5F, 0.5F, 1F), color.gradient.getStartColor());
		Assert.assertEquals(new Color(0.5F, 0.5F, 1F, 1F), color.gradient.getEndColor());
		Assert.assertSame(direction, color.gradient.getDirection());
	}

	@Test
	public void startsAGradientWithItsFirstColor() {
		final Vector4f direction = new Vector4f(1F, 0F, 0F, 1F);
		final Color gradient = Color.gradient(Color.RED, Color.BLUE, direction);
		Assert.assertTrue(gradient.isGradient());
		Assert.assertEquals(Color.RED, gradient);
		Assert.assertSame(Color.RED, gradient.gradient.getStartColor());
		Assert.assertSame(Color.BLUE, gradient.gradient.getEndColor());
		Assert.assertSame(direction, gradient.gradient.getDirection());
	}

	@Test
	public void turnsIntoAGradient() {
		final Vector4f direction = new Vector4f(1F, 0F, 0F, 1F);
		Assert.assertSame(Color.BLUE, Color.RED.toGradient(Color.BLUE).gradient.getEndColor());
		Assert.assertEquals(new Vector4f(0F, 0F, 1F, 0F), Color.RED.toGradient(Color.BLUE).gradient.getDirection());
		Assert.assertSame(direction, Color.RED.toGradient(Color.BLUE, direction).gradient.getDirection());
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
		Assert.assertEquals(0.6F, color.r, 0.0001F);
		Assert.assertEquals(0.3F, color.g, 0.0001F);
		Assert.assertEquals(0.15F, color.b, 0.0001F);
		Assert.assertEquals(0.5F, color.a, 0F);
	}

	@Test
	public void brightensByAFifthByDefault() {
		final Color color = new Color(0.5F, 0.25F, 0.9F, 0.5F).brighter();
		Assert.assertEquals(0.6F, color.r, 0.0001F);
		Assert.assertEquals(0.3F, color.g, 0.0001F);
		Assert.assertEquals(1F, color.b, 0F);
		Assert.assertEquals(0.5F, color.a, 0F);
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
	public void cannotBeModified() throws ReflectiveOperationException {
		for (final String field : new String[] {"r", "g", "b", "a", "update", "gradient"}) {
			Assert.assertTrue(field, Modifier.isFinal(Color.class.getField(field).getModifiers()));
		}
	}

	@Test
	public void keepsItsPresetsUnchanged() {
		Color.RED.addToCopy(Color.BLUE);
		Color.RED.scaleCopy(0.5F);
		Color.RED.copyAlpha(0.5F);
		Color.RAINBOW.update();
		Color.LOADING.update();
		Assert.assertEquals(new Color(1F, 0F, 0F, 1F), Color.RED);
		Assert.assertEquals(Color.WHITE, Color.RAINBOW);
		Assert.assertEquals(Color.WHITE, Color.LOADING);
	}

	@Test
	public void addsAnotherColorToACopy() {
		final UnaryOperator<Color> update = color -> color.copyRed(1F);
		final Color color = new Color(0.25F, 0.5F, 0F, 0.5F, update);
		final Color sum = color.addToCopy(new Color(0.5F, 0.25F, 0.25F, 0.5F));
		Assert.assertEquals(new Color(0.75F, 0.75F, 0.25F, 1F), sum);
		Assert.assertSame(update, sum.update);
		Assert.assertEquals(new Color(0.25F, 0.5F, 0F, 0.5F), color);
	}

	@Test
	public void scalesACopy() {
		final UnaryOperator<Color> update = color -> color.copyRed(1F);
		final Color color = new Color(0.5F, 1F, 0.25F, 1F, update);
		final Color scaled = color.scaleCopy(0.5F);
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.125F, 0.5F), scaled);
		Assert.assertSame(update, scaled.update);
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
		final UnaryOperator<Color> update = color -> color.copyRed(1F);
		final Color color = new Color(0.25F, 0.5F, 0.75F, 0.5F, update);
		final Color copy = color.copy();
		Assert.assertNotSame(color, copy);
		Assert.assertEquals(color, copy);
		Assert.assertSame(update, copy.update);
	}

	@Test
	public void replacesOneComponentInACopy() {
		final UnaryOperator<Color> update = color -> color.copyRed(1F);
		final Color color = new Color(0.25F, 0.5F, 0.75F, 0.5F, update);
		Assert.assertEquals(new Color(1F, 0.5F, 0.75F, 0.5F), color.copyRed(1F));
		Assert.assertEquals(new Color(0.25F, 0F, 0.75F, 0.5F), color.copyGreen(0F));
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.125F, 0.5F), color.copyBlue(0.125F));
		Assert.assertSame(update, color.copyRed(1F).update);
		Assert.assertSame(update, color.copyGreen(0F).update);
		Assert.assertSame(update, color.copyBlue(0.125F).update);
		Assert.assertEquals(new Color(0.25F, 0.5F, 0.75F, 0.5F), color);
	}

	@Test
	public void returnsTheFrameOfItsUpdate() {
		final Color animated = new Color(0F, 0F, 0F, 0.5F, color -> new Color(1F, 0F, 0F, color.a));
		Assert.assertEquals(new Color(1F, 0F, 0F, 0.5F), animated.update());
		Assert.assertEquals(0F, animated.r, 0F);
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
		Assert.assertEquals(1F, color.a, 0F);
	}

	private static void assertLoading(final Color color) {
		Assert.assertEquals(color.r, color.g, 0F);
		Assert.assertEquals(color.r, color.b, 0F);
		Assert.assertEquals(0.17F, color.r, 0.0201F);
		Assert.assertEquals(1F, color.a, 0F);
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
	public void drawsWithAPlainColorThenResetsIt() {
		final float[] seen = new float[1];
		new Color(0.2F, 0.4F, 0.6F, 0.8F).bind(() -> seen[0] = this.bridges.getRender().getState().getBlue(), new Vector4f(0F, 0F, 10F, 10F));
		Assert.assertEquals(0.6F, seen[0], 0F);
		Assert.assertEquals(1F, this.bridges.getRender().getState().getBlue(), 0F);
	}

	@Test
	public void drawsAGradientThroughItsShaderAndRestoresThePreviousOne() {
		final Color gradient = Color.gradient(new Color(1F, 0F, 0F, 1F), new Color(0F, 0F, 1F, 1F), new Vector4f(0F, 0F, 1F, 0F));
		final boolean[] shaded = new boolean[1];
		gradient.bind(() -> shaded[0] = this.bridges.getRender().getShader() != null, new Vector4f(0F, 0F, 10F, 10F));
		Assert.assertTrue(shaded[0]);
		Assert.assertNull(this.bridges.getRender().getShader());
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