package dev.joid.lib.ui.node.impl.design.progress;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode.ProgressDirection;
import lombok.AllArgsConstructor;

public class ProgressNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsEmptyFromTheLeftInBlackAndWhite() {
		final ProgressNode progress = ProgressNode.create(100D, 100D, 200D, 50D);
		Assert.assertEquals(0F, progress.getProgress(), 0F);
		Assert.assertSame(ProgressDirection.LEFT_TO_RIGHT, progress.getDirection());
		Assert.assertSame(Color.BLACK, progress.getBackground());
		Assert.assertSame(Color.WHITE, progress.getForeground());
		Assert.assertNull(progress.getBackgroundResource());
		Assert.assertNull(progress.getForegroundResource());
	}

	@Test
	public void fillsFromTheLeft() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).progress(0.25F));
		this.assertFill(100D, 100D, 150D, 150D);
	}

	@Test
	public void fillsFromTheRight() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).progress(0.25F).direction(ProgressDirection.RIGHT_TO_LEFT));
		this.assertFill(250D, 100D, 300D, 150D);
	}

	@Test
	public void fillsFromTheTop() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).progress(0.4F).direction(ProgressDirection.TOP_TO_BOTTOM));
		this.assertFill(100D, 100D, 300D, 120D);
	}

	@Test
	public void fillsFromTheBottom() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).progress(0.4F).direction(ProgressDirection.BOTTOM_TO_TOP));
		this.assertFill(100D, 130D, 300D, 150D);
	}

	@Test
	public void drawsItsBackgroundUnderTheFill() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).progress(0.5F).background(new Color(0.2F, 0.4F, 0.6F, 1F)).foreground(new Color(0.6F, 0.4F, 0.2F, 1F)));
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw background = this.single(0.2F, 0.4F, 0.6F);
		Assert.assertEquals(100D, background.getLeft(), 1E-3D);
		Assert.assertEquals(300D, background.getRight(), 1E-3D);
		Assert.assertTrue(draws.indexOf(background) < draws.indexOf(this.single(0.6F, 0.4F, 0.2F)));
	}

	@Test
	public void replacesEachColor() {
		final ProgressNode progress = ProgressNode.create(100D, 100D, 200D, 50D).progress(0.5F);
		final Color background = new Color(0.2F, 0.4F, 0.6F, 1F);
		final Color foreground = new Color(0.6F, 0.4F, 0.2F, 1F);
		Assert.assertSame(progress, progress.background(background).foreground(foreground));
		Assert.assertSame(background, progress.getBackground());
		Assert.assertSame(foreground, progress.getForeground());
	}

	@Test
	public void needsBothResourcesToDrawThem() {
		this.open(ProgressNode.create(100D, 100D, 200D, 50D).progress(0.5F).backgroundResource(ProgressNodeTest.resource()).background(new Color(0.2F, 0.4F, 0.6F, 1F)).foreground(new Color(0.6F, 0.4F, 0.2F, 1F)));
		Assert.assertEquals(200D, this.single(0.2F, 0.4F, 0.6F).getRight() - this.single(0.2F, 0.4F, 0.6F).getLeft(), 1E-3D);
		Assert.assertEquals(100D, this.single(0.6F, 0.4F, 0.2F).getRight() - this.single(0.6F, 0.4F, 0.2F).getLeft(), 1E-3D);
	}

	@Test
	public void masksTheForegroundResourceToTheProgress() {
		final Resource background = ProgressNodeTest.resource();
		final Resource foreground = ProgressNodeTest.resource();
		final ProgressNode progress = ProgressNode.create(100D, 100D, 200D, 50D).progress(0.25F).direction(ProgressDirection.RIGHT_TO_LEFT).backgroundResource(background).foregroundResource(foreground);
		Assert.assertSame(background, progress.getBackgroundResource());
		Assert.assertSame(foreground, progress.getForegroundResource());
		this.open(progress);
		final List<Draw> draws = this.bridges.getRender().getDraws();
		final Draw mask = this.single(1F, 0F, 0F);
		final int index = draws.indexOf(mask);
		Assert.assertEquals(250D, mask.getLeft(), 1E-3D);
		Assert.assertEquals(300D, mask.getRight(), 1E-3D);
		Assert.assertEquals(100D, draws.get(index - 1).getLeft(), 1E-3D);
		Assert.assertEquals(300D, draws.get(index - 1).getRight(), 1E-3D);
		Assert.assertEquals(100D, draws.get(index + 1).getLeft(), 1E-3D);
		Assert.assertEquals(300D, draws.get(index + 1).getRight(), 1E-3D);
		Assert.assertEquals(index + 2, draws.size());
	}

	@Test
	public void replacesEachResource() {
		final Resource background = ProgressNodeTest.resource();
		final Resource foreground = ProgressNodeTest.resource();
		final ProgressNode progress = ProgressNode.create(100D, 100D, 200D, 50D);
		Assert.assertSame(progress, progress.backgroundResource(background).foregroundResource(foreground));
		Assert.assertSame(background, progress.getBackgroundResource());
		Assert.assertSame(foreground, progress.getForegroundResource());
	}

	private void open(final ProgressNode progress) {
		this.bridges.open(new NodeUI(progress)).frame();
	}

	private void assertFill(final double left, final double top, final double right, final double bottom) {
		final Draw fill = this.single(1F, 1F, 1F);
		Assert.assertEquals(left, fill.getLeft(), 1E-3D);
		Assert.assertEquals(top, fill.getTop(), 1E-3D);
		Assert.assertEquals(right, fill.getRight(), 1E-3D);
		Assert.assertEquals(bottom, fill.getBottom(), 1E-3D);
		Assert.assertEquals(200D, this.single(0F, 0F, 0F).getRight() - this.single(0F, 0F, 0F).getLeft(), 1E-3D);
	}

	private Draw single(final float red, final float green, final float blue) {
		final List<Draw> draws = this.bridges.getRender().getDraws(red, green, blue);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private static Resource resource() {
		return ResourceBuilder.create().cache(null).compute("progress", () -> new ResourceData("progress", null).textures(new ITexture[] {new RecordingTexture().allocate(4, 4)}));
	}

	@AllArgsConstructor
	public static final class NodeUI extends UI {

		private final Node node;

		@Override
		public void init() {
			super.add(this.node);
		}

	}

}