package dev.joid.lib.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.draw.model.IDrawableModel;
import dev.joid.lib.obj.data.ObjFace;
import dev.joid.lib.obj.data.ObjGroup;
import dev.joid.lib.obj.data.ObjTextureCoordinate;
import dev.joid.lib.obj.data.ObjVertex;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.Resource;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ObjModel implements IDrawableModel {

	private static final Pattern VERTEX_PATTERN       = Pattern.compile("v( [-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?){3,4} *");
	private static final Pattern NORMAL_PATTERN       = Pattern.compile("vn( [-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?){3,4} *");
	private static final Pattern TEXTURE_PATTERN      = Pattern.compile("vt( [-+]?(\\d+(\\.\\d*)?|\\.\\d+)([eE][-+]?\\d+)?){2,3} *");
	private static final Pattern FACE_PATTERN         = Pattern.compile("f( -?\\d+/-?\\d+/-?\\d+){3,} *");
	private static final Pattern FACE_TEXTURE_PATTERN = Pattern.compile("f( -?\\d+/-?\\d+){3,} *");
	private static final Pattern FACE_NORMAL_PATTERN  = Pattern.compile("f( -?\\d+//-?\\d+){3,} *");
	private static final Pattern FACE_VERTEX_PATTERN  = Pattern.compile("f( -?\\d+){3,} *");
	private static final Pattern GROUP_PATTERN        = Pattern.compile("([go]( [\\w\\d\\.]+)+ *\\n)|([go]( [\\w\\d\\.]+)+ *$)");

	private static Matcher faceMatcher;
	private static Matcher groupMatcher;
	private static Matcher vertexMatcher;
	private static Matcher faceNormalMatcher;
	private static Matcher faceVertexMatcher;
	private static Matcher faceTextureMatcher;
	private static Matcher vertexNormalMatcher;
	private static Matcher textureCoordinateMatcher;

	private final List<ObjVertex>            vertices           = new ArrayList<>();
	private final List<ObjVertex>            vertexNormals      = new ArrayList<>();
	private final List<ObjTextureCoordinate> textureCoordinates = new ArrayList<>();
	private final List<ObjGroup>             groups             = new ArrayList<>();

	private final String   name;
	private final Resource texture;

	private ObjGroup currentGroup;

	protected ObjModel(final @NonNull String name, final @NonNull Object handle, final @NonNull Resource texture) {
		this.name    = name;
		this.texture = texture;
		this.load(handle);
	}

	public static @NonNull ObjModel load(final @NonNull String name, final @NonNull Object handle, final @NonNull Resource texture) {
		return new ObjModel(name, handle, texture);
	}

	private void load(final @NonNull Object handle) throws RuntimeException {
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = Asset.of(handle).open();
			reader = new BufferedReader(new InputStreamReader(inputStream));

			int lineCount = 0;
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				lineCount++;
				currentLine = currentLine.replaceAll("\\s+", " ").trim();

				if (currentLine.startsWith("#") || currentLine.length() == 0) {
					continue;
				}
				if (currentLine.startsWith("v ")) {
					final ObjVertex vertex = this.parseVertex(currentLine, lineCount);
					if (vertex != null) {
						this.vertices.add(vertex);
					}
				} else if (currentLine.startsWith("vn ")) {
					final ObjVertex vertex = this.parseVertexNormal(currentLine, lineCount);
					if (vertex != null) {
						this.vertexNormals.add(vertex);
					}
				} else if (currentLine.startsWith("vt ")) {
					final ObjTextureCoordinate textureCoordinate = this.parseTextureCoordinate(currentLine, lineCount);
					if (textureCoordinate != null) {
						this.textureCoordinates.add(textureCoordinate);
					}
				} else if (currentLine.startsWith("f ")) {
					if (this.currentGroup == null) {
						this.currentGroup = new ObjGroup("Default");
					}

					for (final ObjFace face : this.parseFaces(currentLine, lineCount)) {
						this.currentGroup.addFace(face);
					}
				} else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
					final ObjGroup group = this.parseGroup(currentLine, lineCount);
					if (group != null) {
						if (this.currentGroup != null) {
							this.groups.add(this.currentGroup);
						}
					}

					this.currentGroup = group;
				}
			}

			if (this.currentGroup != null) {
				this.groups.add(this.currentGroup);
			}
		} catch (final IOException e) {
			throw new UncheckedIOException("The OBJ model " + this.name + " cannot be read: " + e.getMessage(), e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}

				if (inputStream != null) {
					inputStream.close();
				}
			} catch (final IOException silent) {}
		}
	}

	@Override
	public void render() {
		this.texture.bind(TextureWrap.REPEAT, () -> {
			final Tessellator tessellator = Tessellator.inst().copy();
			if (this.currentGroup != null) {
				tessellator.start(this.currentGroup.getDrawMode());
			} else {
				tessellator.start(DrawMode.TRIANGLES);
			}

			for (final ObjGroup groupObject : this.groups) {
				groupObject.render();
			}

			tessellator.draw();
		});
	}

	public final @NonNull List<ObjVertex> getVertices() {
		return Collections.unmodifiableList(this.vertices);
	}

	public final @NonNull List<ObjVertex> getVertexNormals() {
		return Collections.unmodifiableList(this.vertexNormals);
	}

	public final @NonNull List<ObjTextureCoordinate> getTextureCoordinates() {
		return Collections.unmodifiableList(this.textureCoordinates);
	}

	public final @NonNull List<ObjGroup> getGroups() {
		return Collections.unmodifiableList(this.groups);
	}

	@Override
	public double getDepth() {
		double minZ = Double.POSITIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		for (final ObjVertex vertex : this.vertices) {
			minZ = Math.min(minZ, vertex.getZ());
			maxZ = Math.max(maxZ, vertex.getZ());
		}
		return this.vertices.isEmpty() ? 0D : maxZ - minZ;
	}

	@Override
	public double getWidth() {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		for (final ObjVertex vertex : this.vertices) {
			minX = Math.min(minX, vertex.getX());
			maxX = Math.max(maxX, vertex.getX());
		}
		return this.vertices.isEmpty() ? 0D : maxX - minX;
	}

	@Override
	public double getHeight() {
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (final ObjVertex vertex : this.vertices) {
			minY = Math.min(minY, vertex.getY());
			maxY = Math.max(maxY, vertex.getY());
		}
		return this.vertices.isEmpty() ? 0D : maxY - minY;
	}

	private ObjVertex parseVertex(final @NonNull String line, final int lineCount) {
		if (!ObjModel.isValidVertexLine(line)) {
			throw this.malformed(line, lineCount, "is not a vertex \"v x y z\"");
		}
		final String values = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = values.split(" ");
		return new ObjVertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
	}

	private ObjVertex parseVertexNormal(final @NonNull String line, final int lineCount) {
		if (!ObjModel.isValidVertexNormalLine(line)) {
			throw this.malformed(line, lineCount, "is not a normal \"vn x y z\"");
		}
		final String values = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = values.split(" ");
		return new ObjVertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
	}

	private ObjTextureCoordinate parseTextureCoordinate(final @NonNull String line, final int lineCount) {
		if (!ObjModel.isValidTextureCoordinateLine(line)) {
			throw this.malformed(line, lineCount, "is not a texture coordinate \"vt u v\" or \"vt u v w\"");
		}
		final String values = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = values.split(" ");
		if (tokens.length == 2) {
			return new ObjTextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
		}

		return new ObjTextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
	}

	private List<ObjFace> parseFaces(final @NonNull String line, final int lineCount) {
		if (!ObjModel.isFaceLine(line)) {
			throw this.malformed(line, lineCount, "is not a face of 3 vertices or more \"f v1 v2 v3\" with one format for all its vertices");
		}

		final String[] tokens = line.substring(line.indexOf(" ") + 1).split(" ");
		final DrawMode drawMode = tokens.length == 4 ? DrawMode.QUADS : DrawMode.TRIANGLES;
		if (this.currentGroup.getDrawMode() == null) {
			this.currentGroup.setDrawMode(drawMode);
		} else if (this.currentGroup.getDrawMode() != drawMode) {
			throw this.malformed(line, lineCount, drawMode == DrawMode.QUADS ? "has 4 vertices while the faces before it in its group are triangles, keep quads in their own group" : "has " + tokens.length + " vertices while the faces before it in its group have 4, keep quads in their own group");
		}

		final boolean full = ObjModel.isValidFaceLine(line);
		final boolean textured = full || ObjModel.isValidFaceTextureLine(line);
		final boolean normal = full || ObjModel.isValidFaceNormalLine(line);
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
		if (tokens.length <= 4) {
			faces.add(ObjModel.face(vertices, textureCoordinates, vertexNormals, IntStream.range(0, tokens.length).toArray()));
		} else {
			for (int i = 1; i < tokens.length - 1; i++) {
				faces.add(ObjModel.face(vertices, textureCoordinates, vertexNormals, 0, i, i + 1));
			}
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

	private ObjGroup parseGroup(final @NonNull String line, final int lineCount) {
		ObjGroup group = null;
		if (!ObjModel.isValidGroupLine(line)) {
			throw this.malformed(line, lineCount, "is not a group or an object \"g name\" or \"o name\" with names of letters, digits, underscores and dots");
		}
		final String trimmedLine = line.substring(line.indexOf(" ") + 1);
		if (trimmedLine.length() > 0) {
			group = new ObjGroup(trimmedLine);
		}

		return group;
	}

	private @NonNull IllegalArgumentException malformed(final @NonNull String line, final int lineCount, final @NonNull String problem) {
		return new IllegalArgumentException("The OBJ model " + this.name + " cannot be read: its line " + lineCount + " \"" + line + "\" " + problem + ", fix this line in the model file");
	}

	private static ObjFace face(final ObjVertex[] vertices, final ObjTextureCoordinate[] textureCoordinates, final ObjVertex[] vertexNormals, final int... corners) {
		final ObjFace face = new ObjFace();
		face.setVertices(new ObjVertex[corners.length]);
		face.setTextureCoordinates(textureCoordinates != null ? new ObjTextureCoordinate[corners.length] : null);
		face.setVertexNormals(vertexNormals != null ? new ObjVertex[corners.length] : null);
		for (int i = 0; i < corners.length; i++) {
			face.getVertices()[i] = vertices[corners[i]];
			if (textureCoordinates != null) {
				face.getTextureCoordinates()[i] = textureCoordinates[corners[i]];
			}

			if (vertexNormals != null) {
				face.getVertexNormals()[i] = vertexNormals[corners[i]];
			}
		}
		face.setFaceNormal(face.normal());
		return face;
	}

	private static boolean isFaceLine(final @NonNull String line) {
		return ObjModel.isValidFaceLine(line) || ObjModel.isValidFaceTextureLine(line) || ObjModel.isValidFaceNormalLine(line) || ObjModel.isValidFaceVertexLine(line);
	}

	private static boolean isValidFaceLine(final @NonNull String line) {
		if (ObjModel.faceMatcher != null) {
			ObjModel.faceMatcher.reset();
		}

		ObjModel.faceMatcher = ObjModel.FACE_PATTERN.matcher(line);
		return ObjModel.faceMatcher.matches();
	}

	private static boolean isValidGroupLine(final @NonNull String line) {
		if (ObjModel.groupMatcher != null) {
			ObjModel.groupMatcher.reset();
		}

		ObjModel.groupMatcher = ObjModel.GROUP_PATTERN.matcher(line);
		return ObjModel.groupMatcher.matches();
	}

	private static boolean isValidVertexLine(final @NonNull String line) {
		if (ObjModel.vertexMatcher != null) {
			ObjModel.vertexMatcher.reset();
		}

		ObjModel.vertexMatcher = ObjModel.VERTEX_PATTERN.matcher(line);
		return ObjModel.vertexMatcher.matches();
	}

	private static boolean isValidFaceNormalLine(final @NonNull String line) {
		if (ObjModel.faceNormalMatcher != null) {
			ObjModel.faceNormalMatcher.reset();
		}

		ObjModel.faceNormalMatcher = ObjModel.FACE_NORMAL_PATTERN.matcher(line);
		return ObjModel.faceNormalMatcher.matches();
	}

	private static boolean isValidFaceVertexLine(final @NonNull String line) {
		if (ObjModel.faceVertexMatcher != null) {
			ObjModel.faceVertexMatcher.reset();
		}

		ObjModel.faceVertexMatcher = ObjModel.FACE_VERTEX_PATTERN.matcher(line);
		return ObjModel.faceVertexMatcher.matches();
	}

	private static boolean isValidFaceTextureLine(final @NonNull String line) {
		if (ObjModel.faceTextureMatcher != null) {
			ObjModel.faceTextureMatcher.reset();
		}

		ObjModel.faceTextureMatcher = ObjModel.FACE_TEXTURE_PATTERN.matcher(line);
		return ObjModel.faceTextureMatcher.matches();
	}

	private static boolean isValidVertexNormalLine(final @NonNull String line) {
		if (ObjModel.vertexNormalMatcher != null) {
			ObjModel.vertexNormalMatcher.reset();
		}

		ObjModel.vertexNormalMatcher = ObjModel.NORMAL_PATTERN.matcher(line);
		return ObjModel.vertexNormalMatcher.matches();
	}

	private static boolean isValidTextureCoordinateLine(final @NonNull String line) {
		if (ObjModel.textureCoordinateMatcher != null) {
			ObjModel.textureCoordinateMatcher.reset();
		}

		ObjModel.textureCoordinateMatcher = ObjModel.TEXTURE_PATTERN.matcher(line);
		return ObjModel.textureCoordinateMatcher.matches();
	}

}