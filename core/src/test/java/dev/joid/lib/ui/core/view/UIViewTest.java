package dev.joid.lib.ui.core.view;

import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.matrix.PixelGrid;

public class UIViewTest {

	@Test
	public void boundsTheZoom() {
		final UIView view = UIView.create(960D, 540D).resize(1920D, 1080D);
		Assert.assertEquals(1D, view.zoom(5D).getZoom(), 1E-9D);
		Assert.assertEquals(0.1D, view.zoom(0D).getZoom(), 1E-9D);
		Assert.assertEquals(4D, view.interfaceScale(0.25D).zoom(10D).getZoom(), 1E-9D);
		Assert.assertEquals(1D, view.interfaceScale(1D).getZoom(), 1E-9D);
	}

	@Test
	public void fillsA16By9Window() {
		final UIView view = UIView.create(960D, 540D).resize(1920D, 1080D);
		Assert.assertEquals(1920D, view.getViewportWidth(), 1E-9D);
		Assert.assertEquals(1080D, view.getViewportHeight(), 1E-9D);
		Assert.assertEquals(0D, view.getOffsetX(), 1E-9D);
		Assert.assertEquals(0D, view.getOffsetY(), 1E-9D);
		Assert.assertEquals(123D, view.toUiX(123D), 1E-9D);
		Assert.assertEquals(456D, view.toUiY(456D), 1E-9D);
	}

	@Test
	public void followsTheWindowSize() {
		final UIView view = UIView.create(960D, 540D).resize(3840D, 2160D);
		Assert.assertEquals(1920D, view.toUiX(3840D), 1E-9D);
		Assert.assertEquals(1080D, view.toUiY(2160D), 1E-9D);
		Assert.assertEquals(2D, view.toScreenWidth(1D), 1E-9D);
		Assert.assertEquals(2D, view.toScreenHeight(1D), 1E-9D);
	}

	@Test
	public void mapsEveryPointBothWays() {
		final double[][] windows = {{1920D, 1080D}, {1280D, 720D}, {2560D, 1080D}, {1080D, 1080D}, {1024D, 768D}};
		for (final double anchor : new double[] {0D, 0.5D, 1D}) {
			for (final double[] window : windows) {
				for (final double interfaceScale : new double[] {1D, 0.5D}) {
					for (final double zoom : new double[] {1D, 0.6D}) {
						final UIView view = UIView.create(anchor * 1920D, anchor * 1080D).resize(window[0], window[1]).interfaceScale(interfaceScale).zoom(zoom);
						for (final double value : new double[] {-100D, 0D, 333.3D, 1920D}) {
							Assert.assertEquals(value, view.toUiX(view.toScreenX(value)), 1E-6D);
							Assert.assertEquals(value, view.toUiY(view.toScreenY(value)), 1E-6D);
						}
					}
				}
			}
		}
	}

	@Test
	public void neverStretchesTheCanvas() {
		final double[][] windows = {{1920D, 1200D}, {1920D, 1017D}, {1440D, 900D}, {1904D, 1001D}, {2560D, 1600D}, {1366D, 768D}};
		for (final double[] window : windows) {
			final UIView view = UIView.create(960D, 540D).resize(window[0], window[1]);
			Assert.assertEquals(view.toScreenWidth(1D), view.toScreenHeight(1D), 1E-9D);
			Assert.assertTrue(view.getViewportWidth() >= 1920D - 1E-9D);
			Assert.assertTrue(view.getViewportHeight() >= 1080D - 1E-9D);
		}
	}

	@Test
	public void rendersExactlyWhatItMaps() {
		final UIView view = UIView.create(960D, 540D).resize(2560D, 1440D).interfaceScale(0.5D).zoom(1.2D);
		final MatrixStack projection = new MatrixStack();
		final MatrixStack modelView = new MatrixStack();
		final double[] measured = new double[4];
		view.render(UIViewTest.bridge(projection, modelView), true, () -> {
			final PixelGrid grid = PixelGrid.of(projection.getMatrix(), modelView.getMatrix(), 2560, 1440);
			measured[0] = grid.getScaleX();
			measured[1] = grid.getScaleY();
			measured[2] = UIViewTest.project(projection, modelView, 123D, 456D, 0) * 2560D;
			measured[3] = UIViewTest.project(projection, modelView, 123D, 456D, 1) * 1440D;
		});

		Assert.assertEquals(view.toScreenWidth(1D), measured[0], 1E-5D);
		Assert.assertEquals(view.toScreenHeight(1D), measured[1], 1E-5D);
		Assert.assertEquals(view.toScreenX(123D), measured[2], 1E-3D);
		Assert.assertEquals(view.toScreenY(456D), measured[3], 1E-3D);
		Assert.assertArrayEquals(new MatrixStack().getMatrix(), modelView.getMatrix(), 0F);
	}

	@Test
	public void pinsTheCanvasToAStartAnchor() {
		final UIView view = UIView.create(0D, 0D).resize(2560D, 1080D);
		Assert.assertEquals(0D, view.getOffsetX(), 1E-9D);
		Assert.assertEquals(0D, view.toUiX(0D), 1E-9D);
		Assert.assertEquals(0D, view.toScreenX(0D), 1E-9D);
	}

	@Test
	public void widensTheCanvasOfAWideWindow() {
		final UIView view = UIView.create(960D, 540D).resize(2560D, 1080D);
		Assert.assertEquals(2560D, view.getViewportWidth(), 1E-9D);
		Assert.assertEquals(1080D, view.getViewportHeight(), 1E-9D);
		Assert.assertEquals(320D, view.getOffsetX(), 1E-9D);
		Assert.assertEquals(0D, view.toUiX(320D), 1E-9D);
		Assert.assertEquals(1920D, view.toUiX(2240D), 1E-9D);
	}

	@Test
	public void extendsTheCanvasOfA16By10Window() {
		final UIView view = UIView.create(960D, 540D).resize(1920D, 1200D);
		Assert.assertEquals(1920D, view.getViewportWidth(), 1E-9D);
		Assert.assertEquals(1200D, view.getViewportHeight(), 1E-9D);
		Assert.assertEquals(60D, view.getOffsetY(), 1E-9D);
		Assert.assertEquals(60D, view.toScreenY(0D), 1E-9D);
	}

	@Test
	public void heightensTheCanvasOfATallWindow() {
		final UIView view = UIView.create(960D, 540D).resize(1080D, 1080D);
		Assert.assertEquals(1920D, view.getViewportWidth(), 1E-9D);
		Assert.assertEquals(1920D, view.getViewportHeight(), 1E-9D);
		Assert.assertEquals(420D, view.getOffsetY(), 1E-9D);
		Assert.assertEquals(236.25D, view.toScreenY(0D), 1E-9D);
	}

	@Test
	public void restoresTheMatricesWhenTheDrawFails() {
		final MatrixStack projection = new MatrixStack();
		final MatrixStack modelView = new MatrixStack();
		projection.ortho(0D, 640D, 360D, 0D, 0D, 1D);
		final float[] projected = projection.getMatrix().clone();
		try {
			UIView.create(960D, 540D).resize(2560D, 1440D).zoom(0.5D).render(UIViewTest.bridge(projection, modelView), true, () -> {
				throw new IllegalStateException("draw failed");
			});
			Assert.fail("The failure of the draw must reach the caller");
		} catch (final IllegalStateException expected) {
			Assert.assertArrayEquals(projected, projection.getMatrix(), 0F);
			Assert.assertArrayEquals(new MatrixStack().getMatrix(), modelView.getMatrix(), 0F);
		}
	}

	@Test
	public void keepsTheBoundProjectionWhenAsked() {
		final MatrixStack projection = new MatrixStack();
		projection.ortho(0D, 640D, 360D, 0D, 0D, 1D);
		final float[] projected = projection.getMatrix().clone();
		UIView.create(960D, 540D).resize(1920D, 1080D).render(UIViewTest.bridge(projection, new MatrixStack()), false, () -> Assert.assertArrayEquals(projected, projection.getMatrix(), 0F));
	}

	@Test
	public void scalesTheInterfaceAroundTheAnchor() {
		final UIView view = UIView.create(960D, 540D).resize(1920D, 1080D).interfaceScale(0.5D);
		Assert.assertEquals(0.5D, view.getScale(), 1E-9D);
		Assert.assertEquals(960D, view.toScreenX(960D), 1E-9D);
		Assert.assertEquals(480D, view.toScreenX(0D), 1E-9D);
		Assert.assertEquals(270D, view.toScreenY(0D), 1E-9D);
		Assert.assertEquals(0D, view.toUiX(480D), 1E-9D);
		Assert.assertEquals(50D, view.toScreenWidth(100D), 1E-9D);
		Assert.assertEquals(3840D, view.getVisibleWidth(), 1E-9D);
		Assert.assertEquals(2160D, view.getVisibleHeight(), 1E-9D);
	}

	@Test
	public void combinesTheInterfaceScaleAndTheZoom() {
		final UIView view = UIView.create(960D, 540D).resize(1920D, 1080D).interfaceScale(0.5D).zoom(1.5D);
		Assert.assertEquals(1.5D, view.getZoom(), 1E-9D);
		Assert.assertEquals(0.75D, view.getScale(), 1E-9D);
	}

	private static IRenderBridge bridge(final MatrixStack projection, final MatrixStack modelView) {
		return (IRenderBridge) Proxy.newProxyInstance(UIViewTest.class.getClassLoader(), new Class<?>[] {IRenderBridge.class}, (proxy, method, arguments) -> {
			switch (method.getName()) {
			case "getProjection":
				return projection;
			case "getModelView":
				return modelView;
			default:
				throw new UnsupportedOperationException(method.getName());
			}
		});
	}

	private static double project(final MatrixStack projection, final MatrixStack modelView, final double x, final double y, final int axis) {
		final float[] p = projection.getMatrix();
		final float[] m = modelView.getMatrix();
		final double[] eye = new double[4];
		for (int row = 0; row < 4; row++) {
			eye[row] = m[row] * x + m[4 + row] * y + m[12 + row];
		}

		final double[] clip = new double[4];
		for (int row = 0; row < 4; row++) {
			clip[row] = p[row] * eye[0] + p[4 + row] * eye[1] + p[8 + row] * eye[2] + p[12 + row] * eye[3];
		}

		final double ndc = clip[axis] / clip[3];
		return axis == 0 ? (ndc + 1D) / 2D : (1D - ndc) / 2D;
	}

}