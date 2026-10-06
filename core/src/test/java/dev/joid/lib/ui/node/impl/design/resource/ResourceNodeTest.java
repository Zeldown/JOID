package dev.joid.lib.ui.node.impl.design.resource;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.RecordingTexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;

public class ResourceNodeTest {

	private static final Color TINT  = new Color(0.2F, 0.4F, 0.6F, 1F);
	private static final Color HOVER = new Color(0.6F, 0.4F, 0.2F, 1F);

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsEmptyWhiteAndStretched() {
		final ResourceNode node = ResourceNode.create(10D, 20D);
		Assert.assertNull(node.getResource());
		Assert.assertNull(node.getHoveredResource());
		Assert.assertSame(Color.WHITE, node.getColor());
		Assert.assertNull(node.getHoveredColor());
		Assert.assertSame(StretchType.STRETCH, node.getStretchType());
		Assert.assertEquals(0D, node.getWidth(), 0D);
		Assert.assertEquals(0D, node.getHeight(), 0D);
	}

	@Test
	public void drawsASkeletonWithoutResource() {
		this.bridges.open(new NodeUI(ResourceNode.create(300D, 300D, 50D, 40D)));
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
	}

	@Test
	public void drawsASkeletonUntilItsResourceLoads() {
		final Resource resource = ResourceBuilder.create().cache(null).compute("pending", () -> new ResourceData("pending", null));
		this.bridges.open(new NodeUI(ResourceNode.create(300D, 300D, 50D, 40D).resource(resource).color(ResourceNodeTest.TINT))).frame();
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
		Assert.assertTrue(this.draws(ResourceNodeTest.TINT).isEmpty());
	}

	@Test
	public void drawsAnEmptyResourceAsASkeleton() {
		this.bridges.open(new NodeUI(ResourceNode.create(300D, 300D, 50D, 40D).resource(ResourceNodeTest.resource(0, 0)).color(ResourceNodeTest.TINT))).frame();
		this.assertBounds(this.skeleton(), 300D, 300D, 350D, 340D);
		Assert.assertTrue(this.draws(ResourceNodeTest.TINT).isEmpty());
	}

	@Test
	public void takesTheSizeOfItsResource() {
		final ResourceNode node = ResourceNode.create(100D, 100D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(20D, node.getWidth(), 0D);
		Assert.assertEquals(10D, node.getHeight(), 0D);
		Assert.assertTrue(this.draws(ResourceNodeTest.TINT).isEmpty());
		this.bridges.frame();
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 100D, 120D, 110D);
	}

	@Test
	public void derivesItsHeightFromItsWidth() {
		final ResourceNode node = ResourceNode.create(100D, 100D).resource(ResourceNodeTest.resource(20, 10)).width(40D);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(40D, node.getWidth(), 0D);
		Assert.assertEquals(20D, node.getHeight(), 0D);
	}

	@Test
	public void derivesItsWidthFromItsHeight() {
		final ResourceNode node = ResourceNode.create(100D, 100D).resource(ResourceNodeTest.resource(20, 10)).height(30D);
		this.bridges.open(new NodeUI(node));
		Assert.assertEquals(60D, node.getWidth(), 0D);
		Assert.assertEquals(30D, node.getHeight(), 0D);
	}

	@Test
	public void stretchesItsResourceOverItsBounds() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 200D, 50D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 100D, 300D, 150D);
	}

	@Test
	public void containsItsResourceInItsBounds() {
		final ResourceNode node = ResourceNode.create(100D, 100D, 200D, 50D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT).stretch(StretchType.CONTAIN);
		this.bridges.open(new NodeUI(node));
		Assert.assertSame(StretchType.CONTAIN, node.getStretchType());
		this.assertBounds(this.single(ResourceNodeTest.TINT), 150D, 100D, 250D, 150D);
	}

	@Test
	public void tintsItsResource() {
		final ResourceNode node = ResourceNode.create(100D, 100D, 20D, 10D).resource(ResourceNodeTest.resource(20, 10));
		Assert.assertSame(node, node.color(ResourceNodeTest.TINT));
		this.bridges.open(new NodeUI(node));
		Assert.assertSame(ResourceNodeTest.TINT, node.getColor());
		Assert.assertEquals(1F, this.single(ResourceNodeTest.TINT).getAlpha(), 0F);
	}

	@Test
	public void turnsToItsHoveredColor() {
		final ResourceNode node = ResourceNode.create(100D, 100D, 20D, 10D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT);
		Assert.assertSame(node, node.hoveredColor(ResourceNodeTest.HOVER));
		this.bridges.open(new NodeUI(node)).frames(30);
		Assert.assertSame(ResourceNodeTest.HOVER, node.getHoveredColor());
		Assert.assertEquals(1, this.draws(ResourceNodeTest.TINT).size());
		this.bridges.move(105D, 105D).frames(30);
		Assert.assertTrue(this.draws(ResourceNodeTest.TINT).isEmpty());
		Assert.assertEquals(1, this.draws(ResourceNodeTest.HOVER).size());
	}

	@Test
	public void fadesInItsHoveredResourceInItsHoveredColor() {
		final Resource main = ResourceNodeTest.resource(20, 10);
		final Resource hovered = ResourceNodeTest.resource(20, 10);
		final ResourceNode node = ResourceNode.create(100D, 100D, 20D, 10D).resource(main, hovered).color(ResourceNodeTest.TINT).hoveredColor(ResourceNodeTest.HOVER);
		Assert.assertSame(main, node.getResource());
		Assert.assertSame(hovered, node.getHoveredResource());
		this.bridges.open(new NodeUI(node)).frames(30);
		Assert.assertEquals(1F, this.single(ResourceNodeTest.TINT).getAlpha(), 0F);
		Assert.assertEquals(0F, this.single(ResourceNodeTest.HOVER).getAlpha(), 0F);
		this.bridges.move(105D, 105D).frames(30);
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1F, this.single(ResourceNodeTest.TINT).getAlpha(), 0F);
		Assert.assertEquals(1F, this.single(ResourceNodeTest.HOVER).getAlpha(), 0F);
		Assert.assertTrue(draws.indexOf(this.single(ResourceNodeTest.TINT)) < draws.indexOf(this.single(ResourceNodeTest.HOVER)));
	}

	@Test
	public void fadesInItsHoveredResourceInItsOwnColor() {
		final ResourceNode node = ResourceNode.create(100D, 100D, 20D, 10D).resource(ResourceNodeTest.resource(20, 10)).hoverResource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT);
		this.bridges.open(new NodeUI(node)).frames(30);
		this.bridges.move(105D, 105D).frames(30);
		final List<Draw> draws = this.draws(ResourceNodeTest.TINT);
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(1F, draws.get(0).getAlpha(), 0F);
		Assert.assertEquals(1F, draws.get(1).getAlpha(), 0F);
	}

	@Test
	public void drawsAResourceSharedWithItsHoverOnce() {
		final Resource resource = ResourceNodeTest.resource(20, 10);
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 20D, 10D).resource(resource, resource).color(ResourceNodeTest.TINT).hoveredColor(ResourceNodeTest.HOVER)));
		Assert.assertEquals(1, this.draws(ResourceNodeTest.TINT).size());
		Assert.assertTrue(this.draws(ResourceNodeTest.HOVER).isEmpty());
	}

	@Test
	public void preparesItsHoveredResourceWithoutMainOne() {
		final Resource hovered = ResourceNodeTest.resource(20, 10);
		final ResourceNode node = ResourceNode.create(100D, 100D, 20D, 10D).hoverResource(hovered);
		this.bridges.open(new NodeUI(node));
		Assert.assertNull(node.getResource());
		Assert.assertTrue(hovered.isGenerated());
		this.assertBounds(this.skeleton(), 100D, 100D, 120D, 110D);
	}

	@Test
	public void replacesItsResources() {
		final Resource main = ResourceNodeTest.resource(20, 10);
		final Resource hovered = ResourceNodeTest.resource(20, 10);
		final ResourceNode node = ResourceNode.create(100D, 100D);
		Assert.assertSame(node, node.resource(main));
		Assert.assertSame(node, node.hoverResource(hovered));
		Assert.assertSame(main, node.getResource());
		Assert.assertSame(hovered, node.getHoveredResource());
		node.hoverResource((String) null);
		Assert.assertSame(hovered, node.getHoveredResource());
		node.hoverResource((Resource) null);
		Assert.assertNull(node.getHoveredResource());
		node.resource(hovered, null);
		Assert.assertSame(hovered, node.getResource());
	}

	@Test
	public void loadsItsResourcesFromUrls() {
		final ResourceNode node = ResourceNode.create(100D, 100D).resource("https://joid.invalid/main.png");
		Assert.assertEquals("https://joid.invalid/main.png", node.getResource().getUniqueId());
		node.resource("https://joid.invalid/other.png", null);
		Assert.assertEquals("https://joid.invalid/other.png", node.getResource().getUniqueId());
		Assert.assertNull(node.getHoveredResource());
		node.resource("https://joid.invalid/main.png", "https://joid.invalid/hovered.png");
		Assert.assertEquals("https://joid.invalid/main.png", node.getResource().getUniqueId());
		Assert.assertEquals("https://joid.invalid/hovered.png", node.getHoveredResource().getUniqueId());
		node.hoverResource("https://joid.invalid/other.png");
		Assert.assertEquals("https://joid.invalid/other.png", node.getHoveredResource().getUniqueId());
	}

	@Test
	public void switchesTheFilteringOfBothResources() {
		final Resource main = ResourceNodeTest.resource(20, 10);
		final Resource hovered = ResourceNodeTest.resource(20, 10);
		final ResourceNode node = ResourceNode.create(100D, 100D).resource(main, hovered);
		Assert.assertSame(node, node.linear(true));
		Assert.assertSame(TextureFilter.LINEAR, main.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.LINEAR, hovered.getProperties().getInterpolation());
		node.linear(false);
		Assert.assertSame(TextureFilter.NEAREST, main.getProperties().getInterpolation());
		Assert.assertSame(TextureFilter.NEAREST, hovered.getProperties().getInterpolation());
		final ResourceNode empty = ResourceNode.create(100D, 100D);
		Assert.assertSame(empty, empty.linear(true));
	}

	@Test
	public void namesItsStretchTypes() {
		Assert.assertArrayEquals(new StretchType[] {StretchType.STRETCH, StretchType.CONTAIN, StretchType.COVER}, StretchType.values());
		Assert.assertSame(StretchType.CONTAIN, StretchType.valueOf("CONTAIN"));
	}

	@Test
	public void containsATallResourceInsideItsBounds() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 100D, 100D).resource(ResourceNodeTest.resource(10, 20)).color(ResourceNodeTest.TINT).stretch(StretchType.CONTAIN)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 125D, 100D, 175D, 200D);
	}

	@Test
	public void containsAWideResourceInsideItsBounds() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 100D, 100D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT).stretch(StretchType.CONTAIN)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 125D, 200D, 175D);
	}

	@Test
	public void containsATallResourceInATallerNode() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 100D, 400D).resource(ResourceNodeTest.resource(10, 20)).color(ResourceNodeTest.TINT).stretch(StretchType.CONTAIN)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 200D, 200D, 400D);
	}

	@Test
	public void cropsATallResourceToItsBounds() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 100D, 100D).resource(ResourceNodeTest.resource(10, 20)).color(ResourceNodeTest.TINT).stretch(StretchType.COVER)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 100D, 200D, 200D);
	}

	@Test
	public void cropsAWideResourceToItsBounds() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 100D, 100D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT).stretch(StretchType.COVER)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 100D, 200D, 200D);
	}

	@Test
	public void cropsAWideResourceToAWideNode() {
		this.bridges.open(new NodeUI(ResourceNode.create(100D, 100D, 400D, 100D).resource(ResourceNodeTest.resource(20, 10)).color(ResourceNodeTest.TINT).stretch(StretchType.COVER)));
		this.assertBounds(this.single(ResourceNodeTest.TINT), 100D, 100D, 500D, 200D);
	}

	private Draw skeleton() {
		final Color loading = Color.LOADING();
		final List<Draw> draws = this.bridges.getRender().getDraws(loading.r, loading.g, loading.b);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private Draw single(final Color color) {
		final List<Draw> draws = this.draws(color);
		Assert.assertEquals(1, draws.size());
		return draws.get(0);
	}

	private List<Draw> draws(final Color color) {
		return this.bridges.getRender().getDraws(color.r, color.g, color.b);
	}

	private void assertBounds(final Draw draw, final double left, final double top, final double right, final double bottom) {
		Assert.assertEquals(left, draw.getLeft(), 1E-3D);
		Assert.assertEquals(top, draw.getTop(), 1E-3D);
		Assert.assertEquals(right, draw.getRight(), 1E-3D);
		Assert.assertEquals(bottom, draw.getBottom(), 1E-3D);
	}

	private static Resource resource(final int width, final int height) {
		return ResourceBuilder.create().cache(null).of(new RecordingTexture().allocate(width, height));
	}

	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

}