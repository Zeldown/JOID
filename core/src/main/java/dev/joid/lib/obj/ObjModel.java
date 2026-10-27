package dev.joid.lib.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.draw.model.IDrawableModel;
import dev.joid.lib.obj.data.ObjFace;
import dev.joid.lib.obj.data.ObjGroup;
import dev.joid.lib.obj.data.ObjTextureCoordinate;
import dev.joid.lib.obj.data.ObjVertex;
import dev.joid.lib.resource.Resource;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ObjModel implements IDrawableModel {

	private static final Pattern FACE_PATTERN         = Pattern.compile("f( -?\\d+/-?\\d+/-?\\d+){3,} *");
	private static final Pattern VERTEX_PATTERN       = Pattern.compile("v( [-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?){3,7} *");
	private static final Pattern NORMAL_PATTERN       = Pattern.compile("vn( [-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?){3,4} *");
	private static final Pattern TEXTURE_PATTERN      = Pattern.compile("vt( [-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?){2,3} *");
	private static final Pattern FACE_VERTEX_PATTERN  = Pattern.compile("f( -?\\d+){3,} *");
	private static final Pattern FACE_NORMAL_PATTERN  = Pattern.compile("f( -?\\d+//-?\\d+){3,} *");
	private static final Pattern FACE_TEXTURE_PATTERN = Pattern.compile("f( -?\\d+/-?\\d+){3,} *");

	private final List<ObjGroup>             groups             = new ArrayList<>();
	private final List<ObjVertex>            vertices           = new ArrayList<>();
	private final List<ObjVertex>            vertexNormals      = new ArrayList<>();
	private final List<ObjTextureCoordinate> textureCoordinates = new ArrayList<>();

	private final String   name;
	private final Resource texture;

	private ObjModel(final String name, final Object handle, final Resource texture) {
		this.name    = name;
		this.texture = texture;
		this.load(handle);
	}

	public static @NonNull ObjModel of(final @NonNull String name, final @NonNull Object handle, final @NonNull Resource texture) {
		return new ObjModel(name, handle, texture);
	}

	@Override
	public void render() {
		this.texture.bind(TextureWrap.REPEAT, () -> this.groups.forEach(ObjGroup::render));
	}

	@Override
	public double getDepth() {
		return this.extent(ObjVertex::getZ);
	}

	@Override
	public double getWidth() {
		return this.extent(ObjVertex::getX);
	}

	@Override
	public double getHeight() {
		return this.extent(ObjVertex::getY);
	}

	public @NonNull List<ObjVertex> getVertices() {
		return Collections.unmodifiableList(this.vertices);
	}

	public @NonNull List<ObjVertex> getVertexNormals() {
		return Collections.unmodifiableList(this.vertexNormals);
	}

	public @NonNull List<ObjTextureCoordinate> getTextureCoordinates() {
		return Collections.unmodifiableList(this.textureCoordinates);
	}

	public @NonNull List<ObjGroup> getGroups() {
		return Collections.unmodifiableList(this.groups);
	}

	private void load(final Object handle) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(Asset.of(handle).open(), StandardCharsets.UTF_8));
			ObjGroup group = null;
			Boolean textured = null;
			int lineCount = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				lineCount++;
				line = line.replaceAll("\\s+", " ").trim();

				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				if (line.startsWith("v ")) {
					this.vertices.add(this.parseVertex(line, lineCount, ObjModel.VERTEX_PATTERN, "is not a vertex \"v x y z\""));
				} else if (line.startsWith("vn ")) {
					this.vertexNormals.add(this.parseVertex(line, lineCount, ObjModel.NORMAL_PATTERN, "is not a normal \"vn x y z\""));
				} else if (line.startsWith("vt ")) {
					this.textureCoordinates.add(this.parseTextureCoordinate(line, lineCount));
				} else if (line.startsWith("f ")) {
					if (group == null) {
						group = ObjGroup.create("Default");
					}

					final List<ObjFace> faces = this.parseFaces(textured, line, lineCount);
					if (textured == null) {
						textured = faces.get(0).getTextureCoordinates() != null;
					}

					for (final ObjFace face : faces) {
						group.face(face);
					}
				} else if (line.startsWith("g ") || line.startsWith("o ")) {
					if (group != null) {
						this.groups.add(group);
					}

					group = ObjGroup.create(line.substring(2));
					textured = null;
				}
			}

			if (group != null) {
				this.groups.add(group);
			}
		} catch (final IOException e) {
			throw new UncheckedIOException("The OBJ model " + this.name + " cannot be read (" + e.getMessage() + "), check that the file exists and can be read", e);
		} finally {
			if (reader != null) {
				this.close(reader);
			}
		}
	}

	private void close(final BufferedReader reader) {
		try {
			reader.close();
		} catch (final IOException e) {
			JOIDLog.warn("The file of the OBJ model " + this.name + " cannot be closed (" + e.getMessage() + "), the model is loaded anyway: check the stream or the file it was read from");
		}
	}

	private double extent(final ToDoubleFunction<ObjVertex> axis) {
		final DoubleSummaryStatistics statistics = this.vertices.stream().mapToDouble(axis).summaryStatistics();
		return this.vertices.isEmpty() ? 0D : statistics.getMax() - statistics.getMin();
	}

	private ObjVertex parseVertex(final String line, final int lineCount, final Pattern pattern, final String problem) {
		if (!pattern.matcher(line).matches()) {
			throw this.malformed(line, lineCount, problem);
		}
		final String[] tokens = line.substring(line.indexOf(" ") + 1).split(" ");
		return ObjVertex.of(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
	}

	private ObjTextureCoordinate parseTextureCoordinate(final String line, final int lineCount) {
		if (!ObjModel.TEXTURE_PATTERN.matcher(line).matches()) {
			throw this.malformed(line, lineCount, "is not a texture coordinate \"vt u v\" or \"vt u v w\"");
		}
		final String[] tokens = line.substring(line.indexOf(" ") + 1).split(" ");
		if (tokens.length == 2) {
			return ObjTextureCoordinate.of(Float.parseFloat(tokens[0]), 1F - Float.parseFloat(tokens[1]));
		}

		return ObjTextureCoordinate.of(Float.parseFloat(tokens[0]), 1F - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
	}

	private List<ObjFace> parseFaces(final Boolean groupTextured, final String line, final int lineCount) {
		final boolean full = ObjModel.FACE_PATTERN.matcher(line).matches();
		final boolean textured = full || ObjModel.FACE_TEXTURE_PATTERN.matcher(line).matches();
		final boolean normal = full || ObjModel.FACE_NORMAL_PATTERN.matcher(line).matches();
		if (!textured && !normal && !ObjModel.FACE_VERTEX_PATTERN.matcher(line).matches()) {
			throw this.malformed(line, lineCount, "is not a face of 3 vertices or more \"f v1 v2 v3\" with one format for all its vertices");
		}

		final String[] tokens = line.substring(line.indexOf(" ") + 1).split(" ");
		if (groupTextured != null && groupTextured != textured) {
			throw this.malformed(line, lineCount, textured ? "has texture coordinates while the faces before it in its group have none, keep one face format per group" : "has no texture coordinates while the faces before it in its group have some, keep one face format per group");
		}

		final ObjVertex[] vertices = new ObjVertex[tokens.length];
		final ObjTextureCoordinate[] textureCoordinates = textured ? new ObjTextureCoordinate[tokens.length] : null;
		final ObjVertex[] vertexNormals = normal ? new ObjVertex[tokens.length] : null;
		for (int i = 0; i < tokens.length; i++) {
			final String[] indices = tokens[i].split("/");
			vertices[i] = this.find(this.vertices, indices[0], "vertices", line, lineCount);
			if (textured) {
				textureCoordinates[i] = this.find(this.textureCoordinates, indices[1], "texture coordinates", line, lineCount);
			}

			if (normal) {
				vertexNormals[i] = this.find(this.vertexNormals, indices[2], "normals", line, lineCount);
			}
		}

		final List<ObjFace> faces = new ArrayList<>();
		for (int i = 1; i < tokens.length - 1; i++) {
			faces.add(ObjFace.of(ObjModel.corners(vertices, i), ObjModel.corners(textureCoordinates, i), ObjModel.corners(vertexNormals, i)));
		}
		return faces;
	}

	private <T> T find(final List<T> list, final String token, final String kind, final String line, final int lineCount) {
		final int index = Integer.parseInt(token);
		final int position = index < 0 ? list.size() + index : index - 1;
		if (index == 0 || position < 0 || position >= list.size()) {
			throw this.malformed(line, lineCount, "has the index " + index + " while " + list.size() + " " + kind + " come before it, use an index from 1 to " + list.size() + " or from -1 to -" + list.size());
		}
		return list.get(position);
	}

	private @NonNull IllegalArgumentException malformed(final String line, final int lineCount, final String problem) {
		return new IllegalArgumentException("The OBJ model " + this.name + " cannot be read: its line " + lineCount + " \"" + line + "\" " + problem + ", fix this line in the model file");
	}

	private static <T> T[] corners(final T[] values, final int corner) {
		if (values == null) {
			return null;
		}

		final T[] corners = Arrays.copyOf(values, 3);
		corners[1] = values[corner];
		corners[2] = values[corner + 1];
		return corners;
	}

}