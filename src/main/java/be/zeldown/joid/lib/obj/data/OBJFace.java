package be.zeldown.joid.lib.obj.data;

import javax.vecmath.Vector3d;

import be.zeldown.joid.lib.tessellator.T9R;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class OBJFace {

	private OBJVertex[]            vertices;
	private OBJVertex[]            vertexNormals;
	private OBJTextureCoordinate[] textureCoordinates;
	private OBJVertex              faceNormal;

	public void render(final T9R tessellator) {
		final float textureOffset = 0.0005F;
		if (this.faceNormal == null) {
			this.faceNormal = this.normal();
		}

		tessellator.setNormal(this.faceNormal.getX(), this.faceNormal.getY(), this.faceNormal.getZ());

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

	public @NonNull OBJVertex normal() {
		final Vector3d v1 = new Vector3d(this.vertices[1].getX() - this.vertices[0].getX(), this.vertices[1].getY() - this.vertices[0].getY(), this.vertices[1].getZ() - this.vertices[0].getZ());
		final Vector3d v2 = new Vector3d(this.vertices[2].getX() - this.vertices[0].getX(), this.vertices[2].getY() - this.vertices[0].getY(), this.vertices[2].getZ() - this.vertices[0].getZ());
		final Vector3d normalVector = this.crossProduct(v1, v2);
		normalVector.normalize();
		return new OBJVertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
	}

	private Vector3d crossProduct(final Vector3d vector1, final Vector3d vector2) {
		return new Vector3d(vector1.y * vector2.z - vector1.z * vector2.y, vector1.z * vector2.x - vector1.x * vector2.z, vector1.x * vector2.y - vector1.y * vector2.x);
	}

}