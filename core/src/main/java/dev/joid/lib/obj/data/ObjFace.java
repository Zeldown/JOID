package dev.joid.lib.obj.data;

import dev.joid.lib.render.tessellator.Tessellator;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ObjFace {

	private final ObjVertex[]            vertices;
	private final ObjVertex              faceNormal;
	private final ObjVertex[]            vertexNormals;
	private final ObjTextureCoordinate[] textureCoordinates;

	private ObjFace(final ObjVertex[] vertices, final ObjTextureCoordinate[] textureCoordinates, final ObjVertex[] vertexNormals) {
		this.vertices           = vertices;
		this.vertexNormals      = vertexNormals;
		this.textureCoordinates = textureCoordinates;
		this.faceNormal         = this.normal();
	}

	public static @NonNull ObjFace of(final @NonNull ObjVertex @NonNull [] vertices, final ObjTextureCoordinate[] textureCoordinates, final ObjVertex[] vertexNormals) {
		if (vertices.length != 3) {
			throw new IllegalArgumentException("An ObjFace has 3 vertices, got " + vertices.length + ": split the polygon into triangles (ObjModel does it for OBJ files)");
		}
		if (textureCoordinates != null && textureCoordinates.length != 0 && textureCoordinates.length != 3) {
			throw new IllegalArgumentException("An ObjFace has 3 texture coordinates or none, got " + textureCoordinates.length);
		}
		if (vertexNormals != null && vertexNormals.length != 3) {
			throw new IllegalArgumentException("An ObjFace has 3 vertex normals or none, got " + vertexNormals.length);
		}
		return new ObjFace(vertices, textureCoordinates == null || textureCoordinates.length == 0 ? null : textureCoordinates, vertexNormals);
	}

	public void render(final @NonNull Tessellator tessellator) {
		final float textureOffset = 0.0005F;
		final boolean smooth = this.vertexNormals != null;
		if (!smooth) {
			tessellator.setNormal(this.faceNormal.getX(), this.faceNormal.getY(), this.faceNormal.getZ());
		}

		float averageU = 0F;
		float averageV = 0F;
		if (this.textureCoordinates != null) {
			for (int i = 0; i < this.textureCoordinates.length; ++i) {
				averageU += this.textureCoordinates[i].getU();
				averageV += this.textureCoordinates[i].getV();
			}

			averageU = averageU / this.textureCoordinates.length;
			averageV = averageV / this.textureCoordinates.length;
		}

		for (int i = 0; i < this.vertices.length; ++i) {
			if (smooth) {
				final ObjVertex normal = this.vertexNormals[i];
				final float length = (float) Math.sqrt(normal.getX() * normal.getX() + normal.getY() * normal.getY() + normal.getZ() * normal.getZ());
				if (length > 0F) {
					tessellator.setNormal(normal.getX() / length, normal.getY() / length, normal.getZ() / length);
				} else {
					tessellator.setNormal(this.faceNormal.getX(), this.faceNormal.getY(), this.faceNormal.getZ());
				}
			}

			if (this.textureCoordinates != null) {
				final ObjTextureCoordinate coordinate = this.textureCoordinates[i];
				tessellator.addVertexWithUV(this.vertices[i].getX(), this.vertices[i].getY(), this.vertices[i].getZ(), coordinate.getU() + (coordinate.getU() > averageU ? -textureOffset : textureOffset), coordinate.getV() + (coordinate.getV() > averageV ? -textureOffset : textureOffset));
			} else {
				tessellator.addVertex(this.vertices[i].getX(), this.vertices[i].getY(), this.vertices[i].getZ());
			}
		}
	}

	private @NonNull ObjVertex normal() {
		final double x1 = this.vertices[1].getX() - this.vertices[0].getX();
		final double y1 = this.vertices[1].getY() - this.vertices[0].getY();
		final double z1 = this.vertices[1].getZ() - this.vertices[0].getZ();
		final double x2 = this.vertices[2].getX() - this.vertices[0].getX();
		final double y2 = this.vertices[2].getY() - this.vertices[0].getY();
		final double z2 = this.vertices[2].getZ() - this.vertices[0].getZ();
		final double x = y1 * z2 - z1 * y2;
		final double y = z1 * x2 - x1 * z2;
		final double z = x1 * y2 - y1 * x2;
		final double length = x * x + y * y + z * z;
		if (length == 0D) {
			return ObjVertex.of(0F, 0F, 1F);
		}

		final double norm = 1D / Math.sqrt(length);
		return ObjVertex.of((float) (x * norm), (float) (y * norm), (float) (z * norm));
	}

	public ObjVertex[] getVertices() {
		return this.vertices.clone();
	}

	public ObjVertex[] getVertexNormals() {
		return this.vertexNormals == null ? null : this.vertexNormals.clone();
	}

	public ObjTextureCoordinate[] getTextureCoordinates() {
		return this.textureCoordinates == null ? null : this.textureCoordinates.clone();
	}

}