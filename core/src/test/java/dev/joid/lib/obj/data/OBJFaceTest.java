package dev.joid.lib.obj.data;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;

public class OBJFaceTest {

	@Test
	public void computesTheNormalFromItsFirstThreeVertices() {
		final OBJVertex normal = OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(0F, 1F)).normal();
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(0F, normal.getY(), 0F);
		Assert.assertEquals(1F, normal.getZ(), 0F);
	}

	@Test
	public void turnsItsNormalWithItsWinding() {
		Assert.assertEquals(-1F, OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(0F, 1F), new OBJVertex(1F, 0F)).normal().getZ(), 0F);
	}

	@Test
	public void normalizesItsNormal() {
		final OBJVertex normal = OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(4F, 0F), new OBJVertex(0F, 0F, 3F)).normal();
		Assert.assertEquals(0F, normal.getX(), 0F);
		Assert.assertEquals(-1F, normal.getY(), 0F);
		Assert.assertEquals(0F, normal.getZ(), 0F);
	}

	@Test
	public void rendersItsVerticesWithItsNormal() {
		final Tessellator tessellator = OBJFaceTest.tessellator();
		final OBJFace face = OBJFaceTest.face(new OBJVertex(0F, 0F, 2F), new OBJVertex(1F, 0F, 2F), new OBJVertex(0F, 1F, 2F));
		face.render(tessellator);
		Assert.assertEquals(3, tessellator.getVertexCount());
		Assert.assertTrue(tessellator.isHasNormals());
		Assert.assertEquals(127 << 16, tessellator.getNormal());
		Assert.assertFalse(tessellator.isHasTexture());
		Assert.assertEquals(1F, OBJFaceTest.value(tessellator, 1, 0), 0F);
		Assert.assertEquals(1F, OBJFaceTest.value(tessellator, 2, 1), 0F);
		Assert.assertEquals(2F, OBJFaceTest.value(tessellator, 2, 2), 0F);
	}

	@Test
	public void computesAMissingNormalOnRender() {
		final OBJFace face = OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(0F, 1F));
		Assert.assertNull(face.getFaceNormal());
		face.render(OBJFaceTest.tessellator());
		Assert.assertEquals(1F, face.getFaceNormal().getZ(), 0F);
	}

	@Test
	public void keepsAGivenNormal() {
		final Tessellator tessellator = OBJFaceTest.tessellator();
		final OBJFace face = OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(0F, 1F));
		final OBJVertex normal = new OBJVertex(1F, 0F, 0F);
		face.setFaceNormal(normal);
		face.render(tessellator);
		Assert.assertSame(normal, face.getFaceNormal());
		Assert.assertEquals(127, tessellator.getNormal());
	}

	@Test
	public void pullsItsTextureCoordinatesTowardsTheirCenter() {
		final Tessellator tessellator = OBJFaceTest.tessellator();
		final OBJFace face = OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(0F, 1F));
		face.setTextureCoordinates(new OBJTextureCoordinate[] {new OBJTextureCoordinate(0F, 0F), new OBJTextureCoordinate(1F, 0F), new OBJTextureCoordinate(0F, 1F)});
		face.render(tessellator);
		Assert.assertTrue(tessellator.isHasTexture());
		Assert.assertEquals(0.0005F, OBJFaceTest.value(tessellator, 0, 3), 0.000001F);
		Assert.assertEquals(0.0005F, OBJFaceTest.value(tessellator, 0, 4), 0.000001F);
		Assert.assertEquals(0.9995F, OBJFaceTest.value(tessellator, 1, 3), 0.000001F);
		Assert.assertEquals(0.0005F, OBJFaceTest.value(tessellator, 1, 4), 0.000001F);
		Assert.assertEquals(0.0005F, OBJFaceTest.value(tessellator, 2, 3), 0.000001F);
		Assert.assertEquals(0.9995F, OBJFaceTest.value(tessellator, 2, 4), 0.000001F);
	}

	@Test
	public void ignoresAnEmptyListOfTextureCoordinates() {
		final Tessellator tessellator = OBJFaceTest.tessellator();
		final OBJFace face = OBJFaceTest.face(new OBJVertex(0F, 0F), new OBJVertex(1F, 0F), new OBJVertex(0F, 1F));
		face.setTextureCoordinates(new OBJTextureCoordinate[0]);
		face.render(tessellator);
		Assert.assertEquals(3, tessellator.getVertexCount());
		Assert.assertFalse(tessellator.isHasTexture());
	}

	@Test
	public void keepsItsVertexNormals() {
		final OBJVertex[] normals = {new OBJVertex(0F, 0F, 1F)};
		final OBJFace face = new OBJFace();
		face.setVertexNormals(normals);
		Assert.assertSame(normals, face.getVertexNormals());
	}

	private static OBJFace face(final OBJVertex... vertices) {
		final OBJFace face = new OBJFace();
		face.setVertices(vertices);
		return face;
	}

	private static Tessellator tessellator() {
		final Tessellator tessellator = Tessellator.inst().copy();
		tessellator.start(DrawMode.TRIANGLES);
		return tessellator;
	}

	private static float value(final Tessellator tessellator, final int vertex, final int offset) {
		return Float.intBitsToFloat(tessellator.getRawBuffer()[vertex * 8 + offset]);
	}

}