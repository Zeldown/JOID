package dev.joid.lib.obj;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.obj.data.OBJFace;
import dev.joid.lib.obj.data.OBJGroup;
import dev.joid.lib.obj.data.OBJTextureCoordinate;
import dev.joid.lib.obj.data.OBJVertex;
import dev.joid.lib.resource.Resource;

import lombok.NonNull;

public class OBJModelTest {

	private static final Resource TEXTURE = Resource.of(new Texture());

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void readsATexturedCube() {
		final OBJModel model = OBJModelTest.cube();
		Assert.assertEquals("cube", model.getName());
		Assert.assertSame(OBJModelTest.TEXTURE, model.getTexture());
		Assert.assertEquals(8, model.getVertices().size());
		Assert.assertEquals(4, model.getTextureCoordinates().size());
		Assert.assertEquals(6, model.getVertexNormals().size());
		Assert.assertEquals(1, model.getGroups().size());
		Assert.assertSame(model.getGroups().get(0), model.getCurrentGroup());
		Assert.assertEquals("Cube", model.getCurrentGroup().getName());
		Assert.assertSame(DrawMode.QUADS, model.getCurrentGroup().getDrawMode());
		Assert.assertEquals(6, model.getCurrentGroup().getFaces().size());
	}

	@Test
	public void computesTheNormalOfEveryFace() {
		for (final OBJFace face : OBJModelTest.cube().getCurrentGroup().getFaces()) {
			Assert.assertEquals(face.getVertexNormals()[0].getX(), face.getFaceNormal().getX(), 0F);
			Assert.assertEquals(face.getVertexNormals()[0].getY(), face.getFaceNormal().getY(), 0F);
			Assert.assertEquals(face.getVertexNormals()[0].getZ(), face.getFaceNormal().getZ(), 0F);
		}
	}

	@Test
	public void measuresItsBoundingBox() {
		final OBJModel model = OBJModelTest.cube();
		Assert.assertEquals(2D, model.getWidth(), 0D);
		Assert.assertEquals(2D, model.getHeight(), 0D);
		Assert.assertEquals(2D, model.getDepth(), 0D);
	}

	@Test
	public void readsSignedAndWholeCoordinates() {
		final OBJVertex vertex = OBJModelTest.load("v -1.5 2 3").getVertices().get(0);
		Assert.assertEquals(-1.5F, vertex.getX(), 0F);
		Assert.assertEquals(2F, vertex.getY(), 0F);
		Assert.assertEquals(3F, vertex.getZ(), 0F);
	}

	@Test
	public void readsTheVertexNormals() {
		final OBJVertex normal = OBJModelTest.load("vn 0 -1 0.5").getVertexNormals().get(0);
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(-1F, normal.getY(), 0F);
		Assert.assertEquals(0.5F, normal.getZ(), 0F);
	}

	@Test
	public void flipsTheVerticalTextureCoordinate() {
		final OBJModel model = OBJModelTest.load("vt 0.25 0.75", "vt 0.5 0.125 0.5");
		final OBJTextureCoordinate flat = model.getTextureCoordinates().get(0);
		Assert.assertEquals(0.25F, flat.getU(), 0F);
		Assert.assertEquals(0.25F, flat.getV(), 0F);
		Assert.assertEquals(0F, flat.getW(), 0F);
		final OBJTextureCoordinate deep = model.getTextureCoordinates().get(1);
		Assert.assertEquals(0.5F, deep.getU(), 0F);
		Assert.assertEquals(0.875F, deep.getV(), 0F);
		Assert.assertEquals(0.5F, deep.getW(), 0F);
	}

	@Test
	public void collapsesTheWhitespace() {
		final OBJModel model = OBJModelTest.load("  v\t1   2  3  ", "vt  0.5\t0.5", "vn 0 0  1 ");
		Assert.assertEquals(3F, model.getVertices().get(0).getZ(), 0F);
		Assert.assertEquals(0.5F, model.getTextureCoordinates().get(0).getV(), 0F);
		Assert.assertEquals(1F, model.getVertexNormals().get(0).getZ(), 0F);
	}

	@Test
	public void skipsCommentsBlankLinesAndOtherStatements() {
		final OBJModel model = OBJModelTest.load("# exported", "", "mtllib cube.mtl", "usemtl red", "s off", "l 1 2", "v 1 2 3");
		Assert.assertEquals(1, model.getVertices().size());
		Assert.assertTrue(model.getTextureCoordinates().isEmpty());
		Assert.assertTrue(model.getVertexNormals().isEmpty());
	}

	@Test
	public void gathersTheFacesWithoutGroupInADefaultOne() {
		final OBJModel model = OBJModelTest.triangle("f 1 2 3");
		Assert.assertEquals(1, model.getGroups().size());
		Assert.assertEquals("Default", model.getGroups().get(0).getName());
		Assert.assertSame(DrawMode.TRIANGLES, model.getGroups().get(0).getDrawMode());
	}

	@Test
	public void splitsTheFacesByGroup() {
		final OBJModel model = OBJModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "o first", "f 1 2 3", "g second.part", "f 1 2 4 3", "f 1 2 4 3");
		Assert.assertEquals(2, model.getGroups().size());
		Assert.assertEquals("first", model.getGroups().get(0).getName());
		Assert.assertSame(DrawMode.TRIANGLES, model.getGroups().get(0).getDrawMode());
		Assert.assertEquals(1, model.getGroups().get(0).getFaces().size());
		Assert.assertEquals("second.part", model.getGroups().get(1).getName());
		Assert.assertSame(DrawMode.QUADS, model.getGroups().get(1).getDrawMode());
		Assert.assertEquals(2, model.getGroups().get(1).getFaces().size());
		Assert.assertSame(model.getGroups().get(1), model.getCurrentGroup());
	}

	@Test
	public void readsAFaceWithTexturesAndNormals() {
		final OBJModel model = OBJModelTest.triangle("f 1/3/1 2/2/1 3/1/1");
		final OBJFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertArrayEquals(model.getVertices().toArray(), face.getVertices());
		Assert.assertSame(model.getTextureCoordinates().get(2), face.getTextureCoordinates()[0]);
		Assert.assertSame(model.getTextureCoordinates().get(0), face.getTextureCoordinates()[2]);
		Assert.assertSame(model.getVertexNormals().get(0), face.getVertexNormals()[1]);
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void readsAFaceWithTexturesOnly() {
		final OBJModel model = OBJModelTest.triangle("f 1/1 2/2 3/3");
		final OBJFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertArrayEquals(model.getVertices().toArray(), face.getVertices());
		Assert.assertArrayEquals(model.getTextureCoordinates().toArray(), face.getTextureCoordinates());
		Assert.assertNull(face.getVertexNormals());
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void readsAFaceWithNormalsOnly() {
		final OBJModel model = OBJModelTest.triangle("f 1//1 2//1 3//1");
		final OBJFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertArrayEquals(model.getVertices().toArray(), face.getVertices());
		Assert.assertNull(face.getTextureCoordinates());
		Assert.assertSame(model.getVertexNormals().get(0), face.getVertexNormals()[2]);
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void readsAFaceWithVerticesOnly() {
		final OBJModel model = OBJModelTest.triangle("f 3 2 1");
		final OBJFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertSame(model.getVertices().get(2), face.getVertices()[0]);
		Assert.assertSame(model.getVertices().get(0), face.getVertices()[2]);
		Assert.assertNull(face.getTextureCoordinates());
		Assert.assertNull(face.getVertexNormals());
		Assert.assertEquals(-1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void refusesQuadsInAGroupOfTriangles() {
		try {
			OBJModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "f 1 2 3", "f 1 2 4 3");
			Assert.fail("A quad must not join a group of triangles");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("line 6"));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("expected 3, found 4"));
		}
	}

	@Test
	public void refusesTrianglesInAGroupOfQuads() {
		try {
			OBJModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "f 1 2 4 3", "f 1 2 3");
			Assert.fail("A triangle must not join a group of quads");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("line 6"));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("expected 4, found 3"));
		}
	}

	@Test
	public void namesTheFileAndTheLineOfAMalformedEntry() {
		try {
			OBJModelTest.load("# header", "v 1 2");
			Assert.fail("A vertex with two coordinates must be refused");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Error parsing entry ('v 1 2', line 2) in file 'test' - Incorrect format", expected.getMessage());
		}
	}

	@Test(expected = RuntimeException.class)
	public void refusesANormalWithTwoCoordinates() {
		OBJModelTest.load("vn 1 2");
	}

	@Test(expected = RuntimeException.class)
	public void refusesATextureCoordinateWithOneValue() {
		OBJModelTest.load("vt 0.5");
	}

	@Test(expected = RuntimeException.class)
	public void refusesAFaceOfFivePoints() {
		OBJModelTest.triangle("f 1 2 3 1 2");
	}

	@Test(expected = RuntimeException.class)
	public void refusesAFaceMixingFormats() {
		OBJModelTest.triangle("f 1/1 2//1 3");
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void refusesAFacePointingPastTheVertices() {
		OBJModelTest.triangle("f 1 2 4");
	}

	@Test
	public void wrapsAReadFailure() {
		final IOException failure = new IOException("unreadable");
		try {
			OBJModel.load("broken", new FailingStream(failure), OBJModelTest.TEXTURE);
			Assert.fail("A read failure must reach the caller");
		} catch (final RuntimeException expected) {
			Assert.assertSame(failure, expected.getCause());
		}
	}

	@Test
	public void closesItsStream() {
		final TrackedStream stream = new TrackedStream("v 1 2 3", false);
		OBJModel.load("tracked", stream, OBJModelTest.TEXTURE);
		Assert.assertTrue(stream.closed);
	}

	@Test
	public void closesItsStreamAfterAFailure() {
		final TrackedStream stream = new TrackedStream("v 1 2", false);
		try {
			OBJModel.load("tracked", stream, OBJModelTest.TEXTURE);
			Assert.fail("A vertex with two coordinates must be refused");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(stream.closed);
		}
	}

	@Test
	public void ignoresAStreamFailingToClose() {
		final TrackedStream stream = new TrackedStream("v 1 2 3", true);
		Assert.assertEquals(1, OBJModel.load("tracked", stream, OBJModelTest.TEXTURE).getVertices().size());
		Assert.assertTrue(stream.closed);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingTexture() {
		OBJModel.load("cube", new ByteArrayInputStream(new byte[0]), null);
	}

	@Test
	public void startsEmpty() {
		final OBJModel model = new OBJModel();
		Assert.assertNull(model.getName());
		Assert.assertNull(model.getTexture());
		Assert.assertNull(model.getCurrentGroup());
		Assert.assertTrue(model.getVertices().isEmpty());
		Assert.assertTrue(model.getGroups().isEmpty());
		Assert.assertEquals(0D, model.getWidth(), 0D);
		Assert.assertEquals(0D, model.getHeight(), 0D);
		Assert.assertEquals(0D, model.getDepth(), 0D);
	}

	@Test
	public void changesItsParts() {
		final OBJGroup group = new OBJGroup("part");
		final OBJModel model = new OBJModel("model", OBJModelTest.TEXTURE, group);
		Assert.assertEquals("model", model.getName());
		Assert.assertSame(OBJModelTest.TEXTURE, model.getTexture());
		Assert.assertSame(group, model.getCurrentGroup());

		final Resource texture = Resource.of(new Texture());
		model.setName("renamed");
		model.setTexture(texture);
		model.setCurrentGroup(null);
		Assert.assertEquals("renamed", model.getName());
		Assert.assertSame(texture, model.getTexture());
		Assert.assertNull(model.getCurrentGroup());
	}

	@Test
	public void drawsTheCubeInOneCall() {
		OBJModelTest.cube().render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(DrawMode.TRIANGLES, draws.get(0).getMode());
		Assert.assertEquals(36, draws.get(0).getXs().length);
	}

	@Test
	public void drawsEveryGroupThenUnbindsItsTexture() {
		OBJModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "o first", "f 1 2 3", "g second", "f 1 2 4 3", "f 1 2 4 3").render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(3, draws.get(0).getXs().length);
		Assert.assertEquals(12, draws.get(1).getXs().length);
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void drawsNothingWithoutGroup() {
		new OBJModel("empty", OBJModelTest.TEXTURE, null).render();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void measuresAModelAwayFromTheOrigin() {
		final OBJModel model = OBJModelTest.load("v 1 1 1", "v 3 4 5");
		Assert.assertEquals(2D, model.getWidth(), 0D);
		Assert.assertEquals(3D, model.getHeight(), 0D);
		Assert.assertEquals(4D, model.getDepth(), 0D);
	}

	@Test
	public void measuresAModelBelowTheOrigin() {
		final OBJModel model = OBJModelTest.load("v -3 -4 -5", "v -1 -1 -1");
		Assert.assertEquals(2D, model.getWidth(), 0D);
		Assert.assertEquals(3D, model.getHeight(), 0D);
		Assert.assertEquals(4D, model.getDepth(), 0D);
	}

	@Test
	public void keepsAVertexWithAWeight() {
		final OBJModel model = OBJModelTest.load("v 1 2 3 1", "v 4 5 6");
		Assert.assertEquals(2, model.getVertices().size());
		Assert.assertEquals(4F, model.getVertices().get(1).getX(), 0F);
	}

	@Test
	public void hasNoGroupWithoutFaces() {
		Assert.assertTrue(OBJModelTest.load("v 1 2 3").getGroups().isEmpty());
	}

	@Test
	public void readsAGroupWithSeveralNames() {
		final OBJModel model = OBJModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "g left right", "f 1 2 3");
		Assert.assertEquals(1, model.getGroups().size());
	}

	@Test
	public void namesTheLineOfAMalformedGroup() {
		try {
			OBJModelTest.load("v 0 0 0", "o left-wing");
			Assert.fail("A group name with a dash must be refused");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Error parsing entry ('o left-wing', line 2) in file 'test' - Incorrect format", expected.getMessage());
		}
	}

	private static OBJModel cube() {
		return OBJModel.load("cube", OBJModelTest.class.getResourceAsStream("/dev/joid/lib/obj/cube.obj"), OBJModelTest.TEXTURE);
	}

	private static OBJModel triangle(final String face) {
		return OBJModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "vt 0 0", "vt 1 0", "vt 0 1", "vn 0 0 1", face);
	}

	private static OBJModel load(final String... lines) {
		return OBJModel.load("test", new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8)), OBJModelTest.TEXTURE);
	}

	private static final class FailingStream extends InputStream {

		private final IOException failure;

		private FailingStream(final IOException failure) {
			this.failure = failure;
		}

		@Override
		public int read() throws IOException {
			throw this.failure;
		}

	}

	private static final class TrackedStream extends ByteArrayInputStream {

		private final boolean failing;

		private boolean closed;

		private TrackedStream(final String content, final boolean failing) {
			super(content.getBytes(StandardCharsets.UTF_8));
			this.failing = failing;
		}

		@Override
		public void close() throws IOException {
			this.closed = true;
			if (this.failing) {
				throw new IOException("unclosable");
			}
		}

	}

	private static final class Texture implements ITexture {

		@Override
		public @NonNull ITexture mipmap(final boolean mipmap) {
			return this;
		}

		@Override
		public @NonNull ITexture allocate(final int width, final int height) {
			return this;
		}

		@Override
		public @NonNull ITexture upload(final @NonNull int[] pixels, final int width, final int height) {
			return this;
		}

		@Override
		public int getWidth() {
			return 1;
		}

		@Override
		public int getHeight() {
			return 1;
		}

		@Override
		public boolean isMipmapped() {
			return false;
		}

		@Override
		public void delete() {}

	}

}