package dev.joid.lib.shader.pipeline.pass;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.CircleShader.RoundedShaderType;
import dev.joid.lib.shader.pipeline.dto.ShaderPassContext;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;

public class CircleShaderPassTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private RecordingShader shader;

	@Before
	public void clearTheUniforms() {
		this.shader = (RecordingShader) CircleShader.inst().getShader();
		this.shader.getValues().clear();
	}

	@Test
	public void fitsTheLargestCircleInANode() {
		new CircleShaderPass(RectNode.create(10D, 20D, 100D, 60D)).bindDirect(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertEquals(30F, (Float) values.get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {60F, 50F}, (float[]) values.get("center"), 0F);
	}

	@Test
	public void clipsTheDirectDrawing() {
		new CircleShaderPass(15F, 40F, 50F).bindDirect(this.context());
		final Map<String, Object> values = this.shader.getValues();
		Assert.assertTrue(this.shader.isBound());
		Assert.assertEquals(15F, (Float) values.get("radius"), 0F);
		Assert.assertArrayEquals(new float[] {40F, 50F}, (float[]) values.get("center"), 0F);
		Assert.assertEquals(RoundedShaderType.AUTO.ordinal(), values.get("type"));
	}

	@Test
	public void clipsTheRenderedTexture() {
		new CircleShaderPass(15F, 40F, 50F).bindForTexture(this.context());
		Assert.assertEquals(RoundedShaderType.TEXTURE.ordinal(), this.shader.getValues().get("type"));
	}

	@Test
	public void runsBeforeTheOtherPassesWithoutExpanding() {
		final CircleShaderPass pass = new CircleShaderPass(15F, 40F, 50F);
		Assert.assertEquals(100, pass.priority());
		Assert.assertEquals(0F, pass.expansion(), 0F);
	}

	@Test
	public void releasesTheShader() {
		final CircleShaderPass pass = new CircleShaderPass(15F, 40F, 50F);
		pass.bindForTexture(this.context());
		pass.unbind();
		Assert.assertFalse(this.shader.isBound());
	}

	private ShaderPassContext context() {
		return ShaderPassContext.create(10D, 20D, 100D, 60D, 0D, this.bridges.getRender().getPixelGrid());
	}

}