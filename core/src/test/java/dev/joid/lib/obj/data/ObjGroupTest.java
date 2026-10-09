package dev.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.render.tessellator.DrawMode;

public class ObjGroupTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsWithoutNameNorFaces() {
		final ObjGroup group = new ObjGroup();
		Assert.assertEquals("", group.getName());
		Assert.assertNull(group.getDrawMode());
		Assert.assertTrue(group.getFaces().isEmpty());
	}

	@Test
	public void takesItsNameAndDrawMode() {
		Assert.assertEquals("part", new ObjGroup("part").getName());
		Assert.assertNull(new ObjGroup("part").getDrawMode());
		Assert.assertSame(DrawMode.QUADS, new ObjGroup("part", DrawMode.QUADS).getDrawMode());
	}

	@Test
	public void changesEveryProperty() {
		final List<ObjFace> faces = new ArrayList<>();
		final ObjGroup group = new ObjGroup("part");
		group.setName("renamed");
		group.setDrawMode(DrawMode.TRIANGLES);
		group.setFaces(faces);
		Assert.assertEquals("renamed", group.getName());
		Assert.assertSame(DrawMode.TRIANGLES, group.getDrawMode());
		Assert.assertSame(faces, group.getFaces());
	}

	@Test
	public void drawsItsFacesInOneCall() {
		final ObjGroup group = new ObjGroup("pair", DrawMode.TRIANGLES);
		group.getFaces().add(ObjGroupTest.face(new ObjVertex(0F, 0F), new ObjVertex(1F, 0F), new ObjVertex(0F, 1F)));
		group.getFaces().add(ObjGroupTest.face(new ObjVertex(1F, 0F), new ObjVertex(1F, 1F), new ObjVertex(0F, 1F)));
		group.render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(Primitive.TRIANGLES, draws.get(0).getPrimitive());
		Assert.assertEquals(6, draws.get(0).getXs().length);
	}

	@Test
	public void splitsItsQuadsIntoTriangles() {
		final ObjGroup group = new ObjGroup("quad", DrawMode.QUADS);
		group.getFaces().add(ObjGroupTest.face(new ObjVertex(0F, 0F), new ObjVertex(1F, 0F), new ObjVertex(1F, 1F), new ObjVertex(0F, 1F)));
		group.render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertSame(Primitive.TRIANGLES, draws.get(0).getPrimitive());
		Assert.assertEquals(6, draws.get(0).getXs().length);
	}

	@Test
	public void drawsNothingWithoutFaces() {
		new ObjGroup("empty", DrawMode.TRIANGLES).render();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullName() {
		new ObjGroup(null);
	}

	private static ObjFace face(final ObjVertex... vertices) {
		final ObjFace face = new ObjFace();
		face.setVertices(vertices);
		return face;
	}

}