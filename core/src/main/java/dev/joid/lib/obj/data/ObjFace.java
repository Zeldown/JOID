package dev.joid.lib.obj.data;

import javax.vecmath.Vector3d;

import dev.joid.lib.render.tessellator.Tessellator;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class ObjFace {

	private ObjVertex[]            vertices;
	private ObjVertex              faceNormal;
	private ObjVertex[]            vertexNormals;
	private ObjTextureCoordinate[] textureCoordinates;

	public void render(final @NonNull Tessellator tessellator) {
		final float textureOffset = 0.0005F;
		if (this.faceNormal == null) {
			this.faceNormal = this.normal();
		}

		final boolean smooth = this.vertexNormals != null && this.vertexNormals.length == this.vertices.length;
		if (!smooth) {
			tessellator.setNormal(this.faceNormal.getX(), this.faceNormal.getY(), this.faceNormal.getZ());
		}

		float averageU = 0F;
		float averageV = 0F;
		if (this.textureCoordinates != null && this.textureCoordinates.length > 0) {
			for (int i = 0; i < this.textureCoordinates.length; ++i) {
				averageU += this.textureCoordinates[i].getU();
				averageV += this.textureCoordinates[i].getV();
			}

			averageU = averageU / this.textureCoordinates.length;
			averageV = averageV / this.textureCoordinates.length;
		}

		float offsetU;
		float offsetV;
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

			if (this.textureCoordinates != null && this.textureCoordinates.length > 0) {
				offsetU = textureOffset;
				offsetV = textureOffset;

				if (this.textureCoordinates[i].getU() > averageU) {
					offsetU = -offsetU;
				}

				if (this.textureCoordinates[i].getV() > averageV) {
					offsetV = -offsetV;
				}

				tessellator.addVertexWithUV(this.vertices[i].getX(), this.vertices[i].getY(), this.vertices[i].getZ(), this.textureCoordinates[i].getU() + offsetU, this.textureCoordinates[i].getV() + offsetV);
			} else {
				tessellator.addVertex(this.vertices[i].getX(), this.vertices[i].getY(), this.vertices[i].getZ());
			}
		}
	}

	public @NonNull ObjVertex normal() {
		final Vector3d v1 = new Vector3d(this.vertices[1].getX() - this.vertices[0].getX(), this.vertices[1].getY() - this.vertices[0].getY(), this.vertices[1].getZ() - this.vertices[0].getZ());
		final Vector3d v2 = new Vector3d(this.vertices[2].getX() - this.vertices[0].getX(), this.vertices[2].getY() - this.vertices[0].getY(), this.vertices[2].getZ() - this.vertices[0].getZ());
		final Vector3d normalVector = this.crossProduct(v1, v2);
		normalVector.normalize();
		return new ObjVertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
	}

	private Vector3d crossProduct(final @NonNull Vector3d vector1, final @NonNull Vector3d vector2) {
		return new Vector3d(vector1.y * vector2.z - vector1.z * vector2.y, vector1.z * vector2.x - vector1.x * vector2.z, vector1.x * vector2.y - vector1.y * vector2.x);
	}

}