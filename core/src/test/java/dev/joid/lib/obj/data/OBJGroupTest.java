package dev.joid.lib.obj.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.vertex.DrawMode;

public class OBJGroupTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsWithoutNameNorFaces() {
		final OBJGroup group = new OBJGroup();
		Assert.assertEquals("", group.getName());
		Assert.assertNull(group.getDrawMode());
		Assert.assertTrue(group.getFaces().isEmpty());
	}

	@Test
	public void takesItsNameAndDrawMode() {
		Assert.assertEquals("part", new OBJGroup("part").getName());
		Assert.assertNull(new OBJGroup("part").getDrawMode());
		Assert.assertSame(DrawMode.QUADS, new OBJGroup("part", DrawMode.QUADS).getDrawMode());
	}

	@Test
	public void changesEveryProperty() {
		final List<OBJFace> faces = new ArrayList<>();
		final OBJGroup group = new OBJGroup("part");
		group.setName("renamed");
		group.setDrawMode(DrawMode.TRIANGLES);
		group.setFaces(faces);
		Assert.assertEquals("renamed", group.getName());
		Assert.assertSame(DrawMode.TRIANGLES, group.getDrawMode());
		Assert.assertSame(faces, group.getFaces());
	}

	@Test
	public void drawsItsFacesInOneCall() {
		final OBJGroup group = new OBJGroup("pair", DrawMode.TRIANGLES);
		group.getFaces().add(OBJGroupTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(0F, 1F)));
		group.getFaces().add(OBJGroupTest.face(new OBJVertex(1F, 0F), new OBJVertex(1F, 1F), new OBJVertex(0F, 1F)));
		group.render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(DrawMode.TRIANGLES, draws.get(0).getMode());
		Assert.assertEquals(6, draws.get(0).getXs().length);
	}

	@Test
	public void splitsItsQuadsIntoTriangles() {
		final OBJGroup group = new OBJGroup("quad", DrawMode.QUADS);
		group.getFaces().add(OBJGroupTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(1F, 1F), new OBJVertex(0F, 1F)));
		group.render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertSame(DrawMode.TRIANGLES, draws.get(0).getMode());
		Assert.assertEquals(6, draws.get(0).getXs().length);
	}

	@Test
	public void drawsNothingWithoutFaces() {
		new OBJGroup("empty", DrawMode.TRIANGLES).render();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullName() {
		new OBJGroup(null);
	}

	private static OBJFace face(final OBJVertex... vertices) {
		final OBJFace face = new OBJFace();
		face.setVertices(vertices);
		return face;
	}

}