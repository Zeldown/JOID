package dev.joid.lib.obj.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.render.tessellator.TessellatorAccess;

public class ObjFaceTest {

	@Test
	public void computesTheNormalFromItsFirstThreeVertices() {
		final ObjVertex normal = ObjFaceTest.face(new ObjVertex(0F, 0F), new ObjVertex(1F, 0F), new ObjVertex(0F, 1F)).getFaceNormal();
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(0F, normal.getY(), 0F);
		Assert.assertEquals(1F, normal.getZ(), 0F);
	}

	@Test
	public void turnsItsNormalWithItsWinding() {
		Assert.assertEquals(-1F, ObjFaceTest.face(new ObjVertex(0F, 0F), new ObjVertex(0F, 1F), new ObjVertex(1F, 0F)).getFaceNormal().getZ(), 0F);
	}

	@Test
	public void normalizesItsNormal() {
		final ObjVertex normal = ObjFaceTest.face(new ObjVertex(0F, 0F), new ObjVertex(4F, 0F), new ObjVertex(0F, 0F, 3F)).getFaceNormal();
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(-1F, normal.getY(), 0F);
		Assert.assertEquals(0F, normal.getZ(), 0F);
	}

	@Test
	public void pointsTheNormalOfADegenerateFaceAlongZ() {
		final ObjVertex normal = ObjFaceTest.face(new ObjVertex(0F, 0F), new ObjVertex(1F, 1F), new ObjVertex(2F, 2F)).getFaceNormal();
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(0F, normal.getY(), 0F);
		Assert.assertEquals(1F, normal.getZ(), 0F);
	}

	@Test
	public void rendersItsVerticesWithItsNormal() {
		final Tessellator tessellator = ObjFaceTest.tessellator();
		final ObjFace face = ObjFaceTest.face(new ObjVertex(0F, 0F, 2F), new ObjVertex(1F, 0F, 2F), new ObjVertex(0F, 1F, 2F));
		face.render(tessellator);
		Assert.assertEquals(3, TessellatorAccess.vertexCount(tessellator));
		Assert.assertTrue(TessellatorAccess.hasNormals(tessellator));
		Assert.assertEquals(127 << 16, TessellatorAccess.normal(tessellator));
		Assert.assertFalse(TessellatorAccess.hasTexture(tessellator));
		Assert.assertEquals(1F, ObjFaceTest.value(tessellator, 1, 0), 0F);
		Assert.assertEquals(1F, ObjFaceTest.value(tessellator, 2, 1), 0F);
		Assert.assertEquals(2F, ObjFaceTest.value(tessellator, 2, 2), 0F);
	}

	@Test
	public void rendersEachVertexWithItsOwnNormal() {
		final Tessellator tessellator = ObjFaceTest.tessellator();
		final ObjFace face = ObjFace.create(ObjFaceTest.triangle(), null, new ObjVertex[] {new ObjVertex(1F, 0F, 0F), new ObjVertex(0F, 1F, 0F), new ObjVertex(0F, 0F, 1F)});
		face.render(tessellator);
		Assert.assertEquals(127, TessellatorAccess.rawBuffer(tessellator)[6]);
		Assert.assertEquals(127 << 8, TessellatorAccess.rawBuffer(tessellator)[14]);
		Assert.assertEquals(127 << 16, TessellatorAccess.rawBuffer(tessellator)[22]);
	}

	@Test
	public void normalizesTheVertexNormals() {
		final Tessellator tessellator = ObjFaceTest.tessellator();
		final ObjFace face = ObjFace.create(ObjFaceTest.triangle(), null, new ObjVertex[] {new ObjVertex(0F, 4F, 0F), new ObjVertex(0F, 0F, 0F), new ObjVertex(0F, 0F, 2F)});
		face.render(tessellator);
		Assert.assertEquals(127 << 8, TessellatorAccess.rawBuffer(tessellator)[6]);
		Assert.assertEquals(127 << 16, TessellatorAccess.rawBuffer(tessellator)[14]);
		Assert.assertEquals(127 << 16, TessellatorAccess.rawBuffer(tessellator)[22]);
	}

	@Test
	public void pullsItsTextureCoordinatesTowardsTheirCenter() {
		final Tessellator tessellator = ObjFaceTest.tessellator();
		final ObjFace face = ObjFace.create(ObjFaceTest.triangle(), new ObjTextureCoordinate[] {new ObjTextureCoordinate(0F, 0F), new ObjTextureCoordinate(1F, 0F), new ObjTextureCoordinate(0F, 1F)}, null);
		face.render(tessellator);
		Assert.assertTrue(TessellatorAccess.hasTexture(tessellator));
		Assert.assertEquals(0.0005F, ObjFaceTest.value(tessellator, 0, 3), 0.000001F);
		Assert.assertEquals(0.0005F, ObjFaceTest.value(tessellator, 0, 4), 0.000001F);
		Assert.assertEquals(0.9995F, ObjFaceTest.value(tessellator, 1, 3), 0.000001F);
		Assert.assertEquals(0.0005F, ObjFaceTest.value(tessellator, 1, 4), 0.000001F);
		Assert.assertEquals(0.0005F, ObjFaceTest.value(tessellator, 2, 3), 0.000001F);
		Assert.assertEquals(0.9995F, ObjFaceTest.value(tessellator, 2, 4), 0.000001F);
	}

	@Test
	public void ignoresAnEmptyListOfTextureCoordinates() {
		final Tessellator tessellator = ObjFaceTest.tessellator();
		final ObjFace face = ObjFace.create(ObjFaceTest.triangle(), new ObjTextureCoordinate[0], null);
		face.render(tessellator);
		Assert.assertEquals(3, TessellatorAccess.vertexCount(tessellator));
		Assert.assertFalse(TessellatorAccess.hasTexture(tessellator));
	}

	@Test
	public void keepsItsVertexNormals() {
		final ObjVertex[] normals = {new ObjVertex(0F, 0F, 1F)};
		final ObjFace face = ObjFace.create(ObjFaceTest.triangle(), null, normals);
		Assert.assertArrayEquals(normals, face.getVertexNormals());
		Assert.assertNotSame(normals, face.getVertexNormals());
	}

	private static ObjFace face(final ObjVertex... vertices) {
		return ObjFace.create(vertices, null, null);
	}

	private static ObjVertex[] triangle() {
		return new ObjVertex[] {new ObjVertex(0F, 0F), new ObjVertex(1F, 0F), new ObjVertex(0F, 1F)};
	}

	private static Tessellator tessellator() {
		final Tessellator tessellator = Tessellator.create();
		tessellator.start(DrawMode.TRIANGLES);
		return tessellator;
	}

	private static float value(final Tessellator tessellator, final int vertex, final int offset) {
		return Float.intBitsToFloat(TessellatorAccess.rawBuffer(tessellator)[vertex * 8 + offset]);
	}

}