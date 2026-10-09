package dev.joid.lib.obj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.obj.data.ObjFace;
import dev.joid.lib.obj.data.ObjGroup;
import dev.joid.lib.obj.data.ObjTextureCoordinate;
import dev.joid.lib.obj.data.ObjVertex;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.resource.Resource;

import lombok.NonNull;

public class ObjModelTest {

	private static final Resource TEXTURE = Resource.of(new Texture());

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void readsATexturedCube() {
		final ObjModel model = ObjModelTest.cube();
		Assert.assertEquals("cube", model.getName());
		Assert.assertSame(ObjModelTest.TEXTURE, model.getTexture());
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
	public void readsTheDemoModelWithItsOwnNormalAndTextureCoordinateOnEveryVertex() {
		final ObjModel model = ObjModel.load("demo", ObjModel.class.getResourceAsStream("/assets/demo/models/model.obj"), ObjModelTest.TEXTURE);
		Assert.assertEquals(2D, model.getWidth(), 1E-4D);
		Assert.assertEquals(0.9792D, model.getHeight(), 1E-4D);
		Assert.assertEquals(1.2434D, model.getDepth(), 1E-4D);
		Assert.assertEquals(model.getVertices().size(), model.getVertexNormals().size());
		Assert.assertEquals(model.getVertices().size(), model.getTextureCoordinates().size());
		Assert.assertSame(DrawMode.TRIANGLES, model.getCurrentGroup().getDrawMode());
		for (final ObjFace face : model.getCurrentGroup().getFaces()) {
			Assert.assertEquals(3, face.getVertexNormals().length);
			Assert.assertEquals(3, face.getTextureCoordinates().length);
		}
	}

	@Test
	public void computesTheNormalOfEveryFace() {
		for (final ObjFace face : ObjModelTest.cube().getCurrentGroup().getFaces()) {
			Assert.assertEquals(face.getVertexNormals()[0].getX(), face.getFaceNormal().getX(), 0F);
			Assert.assertEquals(face.getVertexNormals()[0].getY(), face.getFaceNormal().getY(), 0F);
			Assert.assertEquals(face.getVertexNormals()[0].getZ(), face.getFaceNormal().getZ(), 0F);
		}
	}

	@Test
	public void measuresItsBoundingBox() {
		final ObjModel model = ObjModelTest.cube();
		Assert.assertEquals(2D, model.getWidth(), 0D);
		Assert.assertEquals(2D, model.getHeight(), 0D);
		Assert.assertEquals(2D, model.getDepth(), 0D);
	}

	@Test
	public void readsSignedAndWholeCoordinates() {
		final ObjVertex vertex = ObjModelTest.load("v -1.5 2 3").getVertices().get(0);
		Assert.assertEquals(-1.5F, vertex.getX(), 0F);
		Assert.assertEquals(2F, vertex.getY(), 0F);
		Assert.assertEquals(3F, vertex.getZ(), 0F);
	}

	@Test
	public void readsTheVertexNormals() {
		final ObjVertex normal = ObjModelTest.load("vn 0 -1 0.5").getVertexNormals().get(0);
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(-1F, normal.getY(), 0F);
		Assert.assertEquals(0.5F, normal.getZ(), 0F);
	}

	@Test
	public void flipsTheVerticalTextureCoordinate() {
		final ObjModel model = ObjModelTest.load("vt 0.25 0.75", "vt 0.5 0.125 0.5");
		final ObjTextureCoordinate flat = model.getTextureCoordinates().get(0);
		Assert.assertEquals(0.25F, flat.getU(), 0F);
		Assert.assertEquals(0.25F, flat.getV(), 0F);
		Assert.assertEquals(0F, flat.getW(), 0F);
		final ObjTextureCoordinate deep = model.getTextureCoordinates().get(1);
		Assert.assertEquals(0.5F, deep.getU(), 0F);
		Assert.assertEquals(0.875F, deep.getV(), 0F);
		Assert.assertEquals(0.5F, deep.getW(), 0F);
	}

	@Test
	public void collapsesTheWhitespace() {
		final ObjModel model = ObjModelTest.load("  v\t1   2  3  ", "vt  0.5\t0.5", "vn 0 0  1 ");
		Assert.assertEquals(3F, model.getVertices().get(0).getZ(), 0F);
		Assert.assertEquals(0.5F, model.getTextureCoordinates().get(0).getV(), 0F);
		Assert.assertEquals(1F, model.getVertexNormals().get(0).getZ(), 0F);
	}

	@Test
	public void skipsCommentsBlankLinesAndOtherStatements() {
		final ObjModel model = ObjModelTest.load("# exported", "", "mtllib cube.mtl", "usemtl red", "s off", "l 1 2", "v 1 2 3");
		Assert.assertEquals(1, model.getVertices().size());
		Assert.assertTrue(model.getTextureCoordinates().isEmpty());
		Assert.assertTrue(model.getVertexNormals().isEmpty());
	}

	@Test
	public void gathersTheFacesWithoutGroupInADefaultOne() {
		final ObjModel model = ObjModelTest.triangle("f 1 2 3");
		Assert.assertEquals(1, model.getGroups().size());
		Assert.assertEquals("Default", model.getGroups().get(0).getName());
		Assert.assertSame(DrawMode.TRIANGLES, model.getGroups().get(0).getDrawMode());
	}

	@Test
	public void splitsTheFacesByGroup() {
		final ObjModel model = ObjModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "o first", "f 1 2 3", "g second.part", "f 1 2 4 3", "f 1 2 4 3");
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
		final ObjModel model = ObjModelTest.triangle("f 1/3/1 2/2/1 3/1/1");
		final ObjFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertArrayEquals(model.getVertices().toArray(), face.getVertices());
		Assert.assertSame(model.getTextureCoordinates().get(2), face.getTextureCoordinates()[0]);
		Assert.assertSame(model.getTextureCoordinates().get(0), face.getTextureCoordinates()[2]);
		Assert.assertSame(model.getVertexNormals().get(0), face.getVertexNormals()[1]);
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void readsAFaceWithTexturesOnly() {
		final ObjModel model = ObjModelTest.triangle("f 1/1 2/2 3/3");
		final ObjFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertArrayEquals(model.getVertices().toArray(), face.getVertices());
		Assert.assertArrayEquals(model.getTextureCoordinates().toArray(), face.getTextureCoordinates());
		Assert.assertNull(face.getVertexNormals());
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void readsAFaceWithNormalsOnly() {
		final ObjModel model = ObjModelTest.triangle("f 1//1 2//1 3//1");
		final ObjFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertArrayEquals(model.getVertices().toArray(), face.getVertices());
		Assert.assertNull(face.getTextureCoordinates());
		Assert.assertSame(model.getVertexNormals().get(0), face.getVertexNormals()[2]);
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void readsAFaceWithVerticesOnly() {
		final ObjModel model = ObjModelTest.triangle("f 3 2 1");
		final ObjFace face = model.getCurrentGroup().getFaces().get(0);
		Assert.assertSame(model.getVertices().get(2), face.getVertices()[0]);
		Assert.assertSame(model.getVertices().get(0), face.getVertices()[2]);
		Assert.assertNull(face.getTextureCoordinates());
		Assert.assertNull(face.getVertexNormals());
		Assert.assertEquals(-1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void refusesQuadsInAGroupOfTriangles() {
		try {
			ObjModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "f 1 2 3", "f 1 2 4 3");
			Assert.fail("A quad must not join a group of triangles");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("line 6"));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("expected 3, found 4"));
		}
	}

	@Test
	public void refusesTrianglesInAGroupOfQuads() {
		try {
			ObjModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "f 1 2 4 3", "f 1 2 3");
			Assert.fail("A triangle must not join a group of quads");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("line 6"));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().contains("expected 4, found 3"));
		}
	}

	@Test
	public void namesTheFileAndTheLineOfAMalformedEntry() {
		try {
			ObjModelTest.load("# header", "v 1 2");
			Assert.fail("A vertex with two coordinates must be refused");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Error parsing entry ('v 1 2', line 2) in file 'test' - Incorrect format", expected.getMessage());
		}
	}

	@Test(expected = RuntimeException.class)
	public void refusesANormalWithTwoCoordinates() {
		ObjModelTest.load("vn 1 2");
	}

	@Test(expected = RuntimeException.class)
	public void refusesATextureCoordinateWithOneValue() {
		ObjModelTest.load("vt 0.5");
	}

	@Test(expected = RuntimeException.class)
	public void refusesAFaceOfFivePoints() {
		ObjModelTest.triangle("f 1 2 3 1 2");
	}

	@Test(expected = RuntimeException.class)
	public void refusesAFaceMixingFormats() {
		ObjModelTest.triangle("f 1/1 2//1 3");
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void refusesAFacePointingPastTheVertices() {
		ObjModelTest.triangle("f 1 2 4");
	}

	@Test
	public void readsAModelFromAnyAssetHandle() throws IOException {
		final File file = this.folder.newFile("triangle.obj");
		Files.write(file.toPath(), "v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals(3, ObjModel.load("file", file, ObjModelTest.TEXTURE).getVertices().size());
		Assert.assertEquals(3, ObjModel.load("url", file.toURI().toString(), ObjModelTest.TEXTURE).getVertices().size());
	}

	@Test
	public void wrapsAReadFailure() {
		final IOException failure = new IOException("unreadable");
		try {
			ObjModel.load("broken", new FailingStream(failure), ObjModelTest.TEXTURE);
			Assert.fail("A read failure must reach the caller");
		} catch (final RuntimeException expected) {
			Assert.assertSame(failure, expected.getCause());
		}
	}

	@Test
	public void closesItsStream() {
		final TrackedStream stream = new TrackedStream("v 1 2 3", false);
		ObjModel.load("tracked", stream, ObjModelTest.TEXTURE);
		Assert.assertTrue(stream.closed);
	}

	@Test
	public void closesItsStreamAfterAFailure() {
		final TrackedStream stream = new TrackedStream("v 1 2", false);
		try {
			ObjModel.load("tracked", stream, ObjModelTest.TEXTURE);
			Assert.fail("A vertex with two coordinates must be refused");
		} catch (final RuntimeException expected) {
			Assert.assertTrue(stream.closed);
		}
	}

	@Test
	public void ignoresAStreamFailingToClose() {
		final TrackedStream stream = new TrackedStream("v 1 2 3", true);
		Assert.assertEquals(1, ObjModel.load("tracked", stream, ObjModelTest.TEXTURE).getVertices().size());
		Assert.assertTrue(stream.closed);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingTexture() {
		ObjModel.load("cube", new ByteArrayInputStream(new byte[0]), null);
	}

	@Test
	public void startsEmpty() {
		final ObjModel model = new ObjModel();
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
		final ObjGroup group = new ObjGroup("part");
		final ObjModel model = new ObjModel("model", ObjModelTest.TEXTURE, group);
		Assert.assertEquals("model", model.getName());
		Assert.assertSame(ObjModelTest.TEXTURE, model.getTexture());
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
		ObjModelTest.cube().render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertSame(Primitive.TRIANGLES, draws.get(0).getPrimitive());
		Assert.assertEquals(36, draws.get(0).getXs().length);
	}

	@Test
	public void drawsEveryGroupThenUnbindsItsTexture() {
		ObjModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "v 1 1 0", "o first", "f 1 2 3", "g second", "f 1 2 4 3", "f 1 2 4 3").render();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(2, draws.size());
		Assert.assertEquals(3, draws.get(0).getXs().length);
		Assert.assertEquals(12, draws.get(1).getXs().length);
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void drawsNothingWithoutGroup() {
		new ObjModel("empty", ObjModelTest.TEXTURE, (ObjGroup) null).render();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
		Assert.assertNull(this.bridges.getRender().getState().getTexture());
	}

	@Test
	public void measuresAModelAwayFromTheOrigin() {
		final ObjModel model = ObjModelTest.load("v 1 1 1", "v 3 4 5");
		Assert.assertEquals(2D, model.getWidth(), 0D);
		Assert.assertEquals(3D, model.getHeight(), 0D);
		Assert.assertEquals(4D, model.getDepth(), 0D);
	}

	@Test
	public void measuresAModelBelowTheOrigin() {
		final ObjModel model = ObjModelTest.load("v -3 -4 -5", "v -1 -1 -1");
		Assert.assertEquals(2D, model.getWidth(), 0D);
		Assert.assertEquals(3D, model.getHeight(), 0D);
		Assert.assertEquals(4D, model.getDepth(), 0D);
	}

	@Test
	public void keepsAVertexWithAWeight() {
		final ObjModel model = ObjModelTest.load("v 1 2 3 1", "v 4 5 6");
		Assert.assertEquals(2, model.getVertices().size());
		Assert.assertEquals(4F, model.getVertices().get(1).getX(), 0F);
	}

	@Test
	public void hasNoGroupWithoutFaces() {
		Assert.assertTrue(ObjModelTest.load("v 1 2 3").getGroups().isEmpty());
	}

	@Test
	public void readsAGroupWithSeveralNames() {
		final ObjModel model = ObjModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "g left right", "f 1 2 3");
		Assert.assertEquals(1, model.getGroups().size());
	}

	@Test
	public void namesTheLineOfAMalformedGroup() {
		try {
			ObjModelTest.load("v 0 0 0", "o left-wing");
			Assert.fail("A group name with a dash must be refused");
		} catch (final RuntimeException expected) {
			Assert.assertEquals("Error parsing entry ('o left-wing', line 2) in file 'test' - Incorrect format", expected.getMessage());
		}
	}

	private static ObjModel cube() {
		return ObjModel.load("cube", ObjModelTest.class.getResourceAsStream("/dev/joid/lib/obj/cube.obj"), ObjModelTest.TEXTURE);
	}

	private static ObjModel triangle(final String face) {
		return ObjModelTest.load("v 0 0 0", "v 1 0 0", "v 0 1 0", "vt 0 0", "vt 1 0", "vt 0 1", "vn 0 0 1", face);
	}

	private static ObjModel load(final String... lines) {
		return ObjModel.load("test", new ByteArrayInputStream(String.join("\n", lines).getBytes(StandardCharsets.UTF_8)), ObjModelTest.TEXTURE);
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