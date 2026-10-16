package dev.joid.lib.obj.data;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.vertex.Primitive;

public class ObjGroupTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsWithoutNameNorFaces() {
		final ObjGroup group = new ObjGroup();
		Assert.assertEquals("", group.getName());
		Assert.assertTrue(group.getFaces().isEmpty());
	}

	@Test
	public void takesItsName() {
		Assert.assertEquals("part", new ObjGroup("part").getName());
	}

	@Test
	public void changesItsName() {
		final ObjGroup group = new ObjGroup("part");
		group.setName("renamed");
		Assert.assertEquals("renamed", group.getName());
	}

	@Test
	public void addsAFaceBehindAReadOnlyList() {
		final ObjGroup group = new ObjGroup("part");
		final ObjFace face = ObjGroupTest.face(new ObjVertex(0F, 0F), new ObjVertex(1F, 0F), new ObjVertex(0F, 1F));
		group.addFace(face);
		Assert.assertEquals(Collections.singletonList(face), group.getFaces());
		try {
			group.getFaces().clear();
			Assert.fail("The faces of a group must not change from outside");
		} catch (final UnsupportedOperationException expected) {
			Assert.assertEquals(1, group.getFaces().size());
		}
	}

	@Test
	public void drawsItsFacesInOneCall() {
		final ObjGroup group = new ObjGroup("pair");
		group.addFace(ObjGroupTest.face(new ObjVertex(0F, 0F), new ObjVertex(1F, 0F), new ObjVertex(0F, 1F)));
		group.addFace(ObjGroupTest.face(new ObjVertex(1F, 0F), new ObjVertex(1F, 1F), new ObjVertex(0F, 1F)));
		group.render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(Primitive.TRIANGLES, draws.get(0).getPrimitive());
		Assert.assertEquals(6, draws.get(0).getXs().length);
	}

	@Test
	public void drawsNothingWithoutFaces() {
		new ObjGroup("empty").render();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullName() {
		new ObjGroup(null);
	}

	private static ObjFace face(final ObjVertex... vertices) {
		return ObjFace.create(vertices, null, null);
	}

}