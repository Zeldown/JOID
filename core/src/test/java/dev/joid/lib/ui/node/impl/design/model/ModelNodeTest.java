package dev.joid.lib.ui.node.impl.design.model;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.draw.model.utils.IDrawableModel;
import dev.joid.lib.obj.ObjModel;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

public class ModelNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsAtItsNaturalSize() {
		final ModelNode node = ModelNode.create(100D, 100D, 200D, 200D);
		Assert.assertNull(node.getModel());
		Assert.assertEquals(1D, node.getSize(), 0D);
		Assert.assertEquals(0D, node.getRotationYaw(), 0D);
		Assert.assertEquals(0D, node.getRotationPitch(), 0D);
	}

	@Test
	public void fitsAnObjModelToItsWidth() {
		this.bridges.open(new NodeUI(ModelNode.create(100D, 100D, 200D, 200D).model(ModelNodeTest.cube()))).frame();
		final Draw draw = this.model();
		Assert.assertEquals(100D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(100D, draw.getTop(), 1E-3D);
		Assert.assertEquals(300D, draw.getRight(), 1E-3D);
		Assert.assertEquals(300D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void scalesItsModelAroundItsCenter() {
		this.bridges.open(new NodeUI(ModelNode.create(100D, 100D, 200D, 200D).model(ModelNodeTest.cube()).size(0.5D))).frame();
		final Draw draw = this.model();
		Assert.assertEquals(150D, draw.getLeft(), 1E-3D);
		Assert.assertEquals(250D, draw.getRight(), 1E-3D);
		Assert.assertEquals(250D, draw.getBottom(), 1E-3D);
	}

	@Test
	public void drawsNothingWithoutModel() {
		this.bridges.open(new NodeUI(ModelNode.create(100D, 100D, 200D, 200D))).frame();
		Assert.assertEquals(1, this.bridges.getRender().getDraws().size());
	}

	@Test
	public void drawsNothingAtANullSize() {
		final RecordingModel model = new RecordingModel(2D, 2D, 2D);
		this.bridges.open(new NodeUI(ModelNode.create(100D, 100D, 200D, 200D).model(model).size(0D))).frame();
		Assert.assertNull(model.grid);
	}

	@Test
	public void turnsItsModelAroundTheVerticalAxis() {
		final RecordingModel model = new RecordingModel(2D, 2D, 2D);
		final ModelNode node = ModelNode.create(100D, 100D, 200D, 200D).model(model);
		this.bridges.open(new NodeUI(node)).frame();
		Assert.assertEquals(100D, model.grid.getUnitX(), 1E-3D);
		Assert.assertSame(node, node.rotationYaw(180D));
		this.bridges.frame();
		Assert.assertEquals(180D, node.getRotationYaw(), 0D);
		Assert.assertEquals(-100D, model.grid.getUnitX(), 1E-3D);
		Assert.assertEquals(200D, model.grid.toScreenX(0D), 1E-3D);
	}

	@Test
	public void tiltsItsModelAroundTheHorizontalAxis() {
		final RecordingModel model = new RecordingModel(2D, 2D, 2D);
		final ModelNode node = ModelNode.create(100D, 100D, 200D, 200D).model(model);
		this.bridges.open(new NodeUI(node)).frame();
		final double unit = model.grid.getUnitY();
		Assert.assertSame(node, node.rotationPitch(180D));
		this.bridges.frame();
		Assert.assertEquals(180D, node.getRotationPitch(), 0D);
		Assert.assertEquals(-unit, model.grid.getUnitY(), 1E-3D);
		Assert.assertEquals(100D, Math.abs(unit), 1E-3D);
	}

	@Test
	public void givesAFlatModelAUnitSize() {
		final RecordingModel model = new RecordingModel(0D, 0D, 0D);
		this.bridges.open(new NodeUI(ModelNode.create(100D, 100D, 200D, 200D).model(model))).frame();
		Assert.assertEquals(200D, model.grid.getUnitX(), 1E-3D);
		Assert.assertEquals(200D, Math.abs(model.grid.getUnitY()), 1E-3D);
	}

	@Test
	public void leavesTheNextNodesAtItsDepth() {
		final NodeUI ui = new NodeUI(ModelNode.create(100D, 100D, 200D, 200D).model(new RecordingModel(2D, 2D, 2D)));
		this.bridges.open(ui).frame();
		Assert.assertEquals(0D, ui.getDepthLevel(), 0D);
	}

	@Test
	public void keepsTheProportionsOfAWideModel() {
		final WideModel model = new WideModel();
		this.bridges.open(new NodeUI(ModelNode.create(100D, 100D, 200D, 200D).model(model))).frame();
		Assert.assertEquals(50D, Math.abs(model.grid.getUnitX()), 1E-3D);
		Assert.assertEquals(50D, Math.abs(model.grid.getUnitY()), 1E-3D);
	}

	private Draw model() {
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(2, draws.size());
		return draws.get(1);
	}

	private static ObjModel cube() {
		return ObjModel.load("cube", ModelNodeTest.class.getResourceAsStream("/dev/joid/lib/ui/node/impl/design/model/cube.obj"), ResourceBuilder.create().cache(null).compute("cube", () -> new ResourceData("cube", null).textures(new ITexture[] {new RecordingTexture().allocate(4, 4)})));
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

	@RequiredArgsConstructor
	private static final class RecordingModel implements IDrawableModel {

		private final double width;
		private final double height;
		private final double depth;

		private PixelGrid grid;

		@Override
		public void render() {
			this.grid = BridgeHandler.RENDER.get().getPixelGrid();
		}

		@Override
		public double getDepth() {
			return this.depth;
		}

		@Override
		public double getWidth() {
			return this.width;
		}

		@Override
		public double getHeight() {
			return this.height;
		}

	}

	private static final class WideModel implements IDrawableModel {

		private PixelGrid grid;

		@Override
		public void render() {
			this.grid = BridgeHandler.RENDER.get().getPixelGrid();
		}

		@Override
		public double getDepth() {
			return 2D;
		}

		@Override
		public double getWidth() {
			return 4D;
		}

		@Override
		public double getHeight() {
			return 2D;
		}

	}

}