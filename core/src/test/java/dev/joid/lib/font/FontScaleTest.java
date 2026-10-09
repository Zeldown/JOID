package dev.joid.lib.font;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.CapturingRenderBridge;

public class FontScaleTest {

	@Test
	public void measuresOnePixelPerUnitWithoutRenderBridge() {
		Assert.assertEquals(1D, FontScale.getScale(), 0D);
	}

	@Test
	public void followsTheRenderGridOutsideAScope() {
		final CapturingRenderBridge render = new CapturingRenderBridge(1280, 720);
		BridgeHandler.RENDER.register(render);
		try {
			render.pushMatrix();
			render.scale(0.5D, 0.75D, 1D);
			Assert.assertEquals(0.5D, FontScale.getScale(), 1E-6D);
			render.popMatrix();
			Assert.assertEquals(1D, FontScale.getScale(), 1E-6D);
		} finally {
			BridgeHandler.RENDER.unregister(render);
		}
	}

	@Test
	public void prefersItsScopeToTheRenderGrid() {
		final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);
		BridgeHandler.RENDER.register(render);
		try {
			final List<Double> seen = new ArrayList<>();
			FontScale.run(() -> 0.26D, () -> seen.add(FontScale.getScale()));
			Assert.assertEquals(0.26D, seen.get(0), 0D);
		} finally {
			BridgeHandler.RENDER.unregister(render);
		}
	}

	@Test
	public void readsItsScopeAtEachMeasure() {
		final double[] scale = {0.5D};
		final List<Double> seen = new ArrayList<>();
		FontScale.run(() -> scale[0], () -> {
			seen.add(FontScale.getScale());
			scale[0] = 2D;
			seen.add(FontScale.getScale());
		});
		Assert.assertEquals(0.5D, seen.get(0), 0D);
		Assert.assertEquals(2D, seen.get(1), 0D);
	}

	@Test
	public void restoresTheOuterScaleAfterANestedScope() {
		final List<Double> seen = new ArrayList<>();
		FontScale.run(() -> 0.25D, () -> {
			FontScale.run(() -> 3D, () -> seen.add(FontScale.getScale()));
			seen.add(FontScale.getScale());
		});
		Assert.assertEquals(3D, seen.get(0), 0D);
		Assert.assertEquals(0.25D, seen.get(1), 0D);
		Assert.assertEquals(1D, FontScale.getScale(), 0D);
	}

	@Test
	public void restoresTheScaleWhenItsScopeFails() {
		try {
			FontScale.run(() -> 0.25D, () -> {
				throw new IllegalStateException("measure failed");
			});
			Assert.fail("The failure of the scope must reach the caller");
		} catch (final IllegalStateException expected) {
			Assert.assertEquals("measure failed", expected.getMessage());
			Assert.assertEquals(1D, FontScale.getScale(), 0D);
		}
	}

}