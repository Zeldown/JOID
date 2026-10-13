package dev.joid.lib.obj.data;

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
		final double x1 = this.vertices[1].getX() - this.vertices[0].getX();
		final double y1 = this.vertices[1].getY() - this.vertices[0].getY();
		final double z1 = this.vertices[1].getZ() - this.vertices[0].getZ();
		final double x2 = this.vertices[2].getX() - this.vertices[0].getX();
		final double y2 = this.vertices[2].getY() - this.vertices[0].getY();
		final double z2 = this.vertices[2].getZ() - this.vertices[0].getZ();
		final double x = y1 * z2 - z1 * y2;
		final double y = z1 * x2 - x1 * z2;
		final double z = x1 * y2 - y1 * x2;
		final double norm = 1D / Math.sqrt(x * x + y * y + z * z);
		return new ObjVertex((float) (x * norm), (float) (y * norm), (float) (z * norm));
	}

}