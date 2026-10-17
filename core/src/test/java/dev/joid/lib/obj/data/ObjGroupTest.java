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
	public void startsWithItsNameWithoutFaces() {
		final ObjGroup group = ObjGroup.create("part");
		Assert.assertEquals("part", group.getName());
		Assert.assertTrue(group.getFaces().isEmpty());
	}

	@Test
	public void addsAFaceBehindAReadOnlyList() {
		final ObjGroup group = ObjGroup.create("part");
		final ObjFace face = ObjGroupTest.face(ObjVertex.of(0F, 0F), ObjVertex.of(1F, 0F), ObjVertex.of(0F, 1F));
		Assert.assertSame(group, group.face(face));
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
		final ObjGroup group = ObjGroup.create("pair");
		group.face(ObjGroupTest.face(ObjVertex.of(0F, 0F), ObjVertex.of(1F, 0F), ObjVertex.of(0F, 1F)));
		group.face(ObjGroupTest.face(ObjVertex.of(1F, 0F), ObjVertex.of(1F, 1F), ObjVertex.of(0F, 1F)));
		group.render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(Primitive.TRIANGLES, draws.get(0).getPrimitive());
		Assert.assertEquals(6, draws.get(0).getXs().length);
	}

	@Test
	public void drawsNothingWithoutFaces() {
		ObjGroup.create("empty").render();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullName() {
		ObjGroup.create(null);
	}

	private static ObjFace face(final ObjVertex... vertices) {
		return ObjFace.create(vertices, null, null);
	}

}