package be.zeldown.joid.lib.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.draw.model.utils.IDrawableModel;
import be.zeldown.joid.lib.obj.data.OBJFace;
import be.zeldown.joid.lib.obj.data.OBJGroup;
import be.zeldown.joid.lib.obj.data.OBJTextureCoordinate;
import be.zeldown.joid.lib.obj.data.OBJVertex;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.tessellator.T9R;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class OBJModel implements IDrawableModel {

	private static final Pattern VERTEX_PATTERN       = Pattern.compile("(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
	private static final Pattern NORMAL_PATTERN       = Pattern.compile("(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
	private static final Pattern TEXTURE_PATTERN      = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+(\\.\\d+)?){2,3} *$)");
	private static final Pattern FACE_PATTERN         = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
	private static final Pattern FACE_TEXTURE_PATTERN = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
	private static final Pattern FACE_NORMAL_PATTERN  = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
	private static final Pattern FACE_VERTEX_PATTERN  = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
	private static final Pattern GROUP_PATTERN        = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

	private static Matcher vertexMatcher;
	private static Matcher vertexNormalMatcher;
	private static Matcher textureCoordinateMatcher;
	private static Matcher faceMatcher;
	private static Matcher faceTextureMatcher;
	private static Matcher faceNormalMatcher;
	private static Matcher faceVertexMatcher;
	private static Matcher groupMatcher;

	private List<OBJVertex>            vertices           = new ArrayList<>();
	private List<OBJVertex>            vertexNormals      = new ArrayList<>();
	private List<OBJTextureCoordinate> textureCoordinates = new ArrayList<>();
	private List<OBJGroup>             groups             = new ArrayList<>();

	private String   name;
	private Resource texture;

	private OBJGroup currentGroup;

	protected OBJModel(final @NonNull String name, final @NonNull InputStream resource, final @NonNull Resource texture) {
		this.name    = name;
		this.texture = texture;
		this.load(resource);
	}

	public static @NonNull OBJModel load(final @NonNull String name, final @NonNull InputStream resource, final @NonNull Resource texture) {
		return new OBJModel(name, resource, texture);
	}

	private void load(final @NonNull InputStream inputStream) throws RuntimeException {
		BufferedReader reader = null;
		try {
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
					final OBJVertex vertex = this.parseVertex(currentLine, lineCount);
					if (vertex != null) {
						this.vertices.add(vertex);
					}
				} else if (currentLine.startsWith("vn ")) {
					final OBJVertex vertex = this.parseVertexNormal(currentLine, lineCount);
					if (vertex != null) {
						this.vertexNormals.add(vertex);
					}
				} else if (currentLine.startsWith("vt ")) {
					final OBJTextureCoordinate textureCoordinate = this.parseTextureCoordinate(currentLine, lineCount);
					if (textureCoordinate != null) {
						this.textureCoordinates.add(textureCoordinate);
					}
				} else if (currentLine.startsWith("f ")) {
					if (this.currentGroup == null) {
						this.currentGroup = new OBJGroup("Default");
					}

					final OBJFace face = this.parseFace(currentLine, lineCount);
					if (face != null) {
						this.currentGroup.getFaces().add(face);
					}
				} else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
					final OBJGroup group = this.parseGroup(currentLine, lineCount);
					if (group != null) {
						if (this.currentGroup != null) {
							this.groups.add(this.currentGroup);
						}
					}

					this.currentGroup = group;
				}
			}

			this.groups.add(this.currentGroup);
		} catch (final IOException e) {
			throw new RuntimeException("IO Exception reading model format", e);
		} finally {
			try {
				reader.close();
			} catch (final IOException silent) {}

			try {
				inputStream.close();
			} catch (final IOException silent) {}
		}
	}

	@Override
	public void render() {
		this.texture.bind(() -> {
			final T9R tessellator = T9R.inst().copy();
			if (this.currentGroup != null) {
				tessellator.start(this.currentGroup.getGlDrawingMode());
			} else {
				tessellator.start(GL11.GL_TRIANGLES);
			}

			for (final OBJGroup groupObject : this.groups) {
				groupObject.render();
			}

			tessellator.draw();
		});
	}

	@Override
	public double getWidth() {
		double minX = Double.MIN_VALUE;
		double maxX = Double.MIN_VALUE;
		for (final OBJVertex vertex : this.vertices) {
			minX = Math.min(minX, vertex.getX());
			maxX = Math.max(maxX, vertex.getX());
		}
		return maxX - minX;
	}

	@Override
	public double getHeight() {
		double minY = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		for (final OBJVertex vertex : this.vertices) {
			minY = Math.min(minY, vertex.getY());
			maxY = Math.max(maxY, vertex.getY());
		}
		return maxY - minY;
	}

	@Override
	public double getDepth() {
		double minZ = Double.MIN_VALUE;
		double maxZ = Double.MIN_VALUE;
		for (final OBJVertex vertex : this.vertices) {
			minZ = Math.min(minZ, vertex.getZ());
			maxZ = Math.max(maxZ, vertex.getZ());
		}
		return maxZ - minZ;
	}

	private OBJVertex parseVertex(@NonNull String line, final int lineCount) throws RuntimeException {
		final OBJVertex vertex = null;
		if (!OBJModel.isValidVertexLine(line)) {
			throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Incorrect format");
		}
		line = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = line.split(" ");
		try {
			if (tokens.length == 2) {
				return new OBJVertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
			}
			if (tokens.length == 3) {
				return new OBJVertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
			}
		} catch (final NumberFormatException e) {
			throw new RuntimeException(String.format("Number formatting error at line %d",lineCount), e);
		}

		return vertex;
	}

	private OBJVertex parseVertexNormal(@NonNull String line, final int lineCount) throws RuntimeException {
		final OBJVertex vertexNormal = null;
		if (!OBJModel.isValidVertexNormalLine(line)) {
			throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Incorrect format");
		}
		line = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = line.split(" ");
		try {
			if (tokens.length == 3) {
				return new OBJVertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
			}
		} catch (final NumberFormatException e) {
			throw new RuntimeException(String.format("Number formatting error at line %d",lineCount), e);
		}

		return vertexNormal;
	}

	private OBJTextureCoordinate parseTextureCoordinate(@NonNull String line, final int lineCount) throws RuntimeException {
		final OBJTextureCoordinate textureCoordinate = null;
		if (!OBJModel.isValidTextureCoordinateLine(line)) {
			throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Incorrect format");
		}
		line = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = line.split(" ");
		try {
			if (tokens.length == 2) {
				return new OBJTextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
			}
			if (tokens.length == 3) {
				return new OBJTextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
			}
		} catch (final NumberFormatException e) {
			throw new RuntimeException(String.format("Number formatting error at line %d",lineCount), e);
		}

		return textureCoordinate;
	}

	private OBJFace parseFace(final @NonNull String line, final int lineCount) throws RuntimeException {
		OBJFace face = null;
		if (!OBJModel.isFaceLine(line)) {
			throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Incorrect format");
		}
		face = new OBJFace();

		final String trimmedLine = line.substring(line.indexOf(" ") + 1);
		final String[] tokens = trimmedLine.split(" ");
		String[] subTokens = null;

		if (tokens.length == 3) {
			if (this.currentGroup.getGlDrawingMode() == -1) {
				this.currentGroup.setGlDrawingMode(GL11.GL_TRIANGLES);
			} else if (this.currentGroup.getGlDrawingMode() != GL11.GL_TRIANGLES) {
				throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Invalid number of points for face (expected 4, found " + tokens.length + ")");
			}
		} else if (tokens.length == 4) {
			if (this.currentGroup.getGlDrawingMode() == -1) {
				this.currentGroup.setGlDrawingMode(GL11.GL_QUADS);
			} else if (this.currentGroup.getGlDrawingMode() != GL11.GL_QUADS) {
				throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Invalid number of points for face (expected 3, found " + tokens.length + ")");
			}
		}

		if (OBJModel.isValidFaceLine(line)) {
			face.setVertices(new OBJVertex[tokens.length]);
			face.setTextureCoordinates(new OBJTextureCoordinate[tokens.length]);
			face.setVertexNormals(new OBJVertex[tokens.length]);
			for (int i = 0; i < tokens.length; ++i) {
				subTokens = tokens[i].split("/");
				face.getVertices()[i] = this.vertices.get(Integer.parseInt(subTokens[0]) - 1);
				face.getTextureCoordinates()[i] = this.textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
				face.getVertexNormals()[i] = this.vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
			}
			face.setFaceNormal(face.normal());
		} else if (OBJModel.isValidFaceTextureLine(line)) {
			face.setVertices(new OBJVertex[tokens.length]);
			face.setTextureCoordinates(new OBJTextureCoordinate[tokens.length]);
			for (int i = 0; i < tokens.length; ++i) {
				subTokens = tokens[i].split("/");
				face.getVertices()[i] = this.vertices.get(Integer.parseInt(subTokens[0]) - 1);
				face.getTextureCoordinates()[i] = this.textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
			}
			face.setFaceNormal(face.normal());
		} else if (OBJModel.isValidFaceNormalLine(line)) {
			face.setVertices(new OBJVertex[tokens.length]);
			face.setVertexNormals(new OBJVertex[tokens.length]);
			for (int i = 0; i < tokens.length; ++i) {
				subTokens = tokens[i].split("//");
				face.getVertices()[i] = this.vertices.get(Integer.parseInt(subTokens[0]) - 1);
				face.getVertexNormals()[i] = this.vertexNormals.get(Integer.parseInt(subTokens[1]) - 1);
			}
			face.setFaceNormal(face.normal());
		} else if (OBJModel.isValidFaceVertexLine(line)) {
			face.setVertices(new OBJVertex[tokens.length]);
			for (int i = 0; i < tokens.length; ++i) {
				face.getVertices()[i] = this.vertices.get(Integer.parseInt(tokens[i]) - 1);
			}
			face.setFaceNormal(face.normal());
		} else {
			throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Incorrect format");
		}

		return face;
	}

	private OBJGroup parseGroup(final @NonNull String line, final int lineCount) throws RuntimeException {
		OBJGroup group = null;
		if (!OBJModel.isValidGroupLine(line)) {
			throw new RuntimeException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + this.name + "' - Incorrect format");
		}
		final String trimmedLine = line.substring(line.indexOf(" ") + 1);
		if (trimmedLine.length() > 0) {
			group = new OBJGroup(trimmedLine);
		}

		return group;
	}

	private static boolean isValidVertexLine(final @NonNull String line) {
		if (OBJModel.vertexMatcher != null) {
			OBJModel.vertexMatcher.reset();
		}

		OBJModel.vertexMatcher = OBJModel.VERTEX_PATTERN.matcher(line);
		return OBJModel.vertexMatcher.matches();
	}

	private static boolean isValidVertexNormalLine(final @NonNull String line) {
		if (OBJModel.vertexNormalMatcher != null) {
			OBJModel.vertexNormalMatcher.reset();
		}

		OBJModel.vertexNormalMatcher = OBJModel.NORMAL_PATTERN.matcher(line);
		return OBJModel.vertexNormalMatcher.matches();
	}

	private static boolean isValidTextureCoordinateLine(final @NonNull String line) {
		if (OBJModel.textureCoordinateMatcher != null) {
			OBJModel.textureCoordinateMatcher.reset();
		}

		OBJModel.textureCoordinateMatcher = OBJModel.TEXTURE_PATTERN.matcher(line);
		return OBJModel.textureCoordinateMatcher.matches();
	}

	private static boolean isValidFaceLine(final @NonNull String line) {
		if (OBJModel.faceMatcher != null) {
			OBJModel.faceMatcher.reset();
		}

		OBJModel.faceMatcher = OBJModel.FACE_PATTERN.matcher(line);
		return OBJModel.faceMatcher.matches();
	}

	private static boolean isValidFaceTextureLine(final @NonNull String line) {
		if (OBJModel.faceTextureMatcher != null) {
			OBJModel.faceTextureMatcher.reset();
		}

		OBJModel.faceTextureMatcher = OBJModel.FACE_TEXTURE_PATTERN.matcher(line);
		return OBJModel.faceTextureMatcher.matches();
	}

	private static boolean isValidFaceNormalLine(final @NonNull String line) {
		if (OBJModel.faceNormalMatcher != null) {
			OBJModel.faceNormalMatcher.reset();
		}

		OBJModel.faceNormalMatcher = OBJModel.FACE_NORMAL_PATTERN.matcher(line);
		return OBJModel.faceNormalMatcher.matches();
	}

	private static boolean isValidFaceVertexLine(final @NonNull String line) {
		if (OBJModel.faceVertexMatcher != null) {
			OBJModel.faceVertexMatcher.reset();
		}

		OBJModel.faceVertexMatcher = OBJModel.FACE_VERTEX_PATTERN.matcher(line);
		return OBJModel.faceVertexMatcher.matches();
	}

	private static boolean isFaceLine(final @NonNull String line) {
		return OBJModel.isValidFaceLine(line) || OBJModel.isValidFaceTextureLine(line) || OBJModel.isValidFaceNormalLine(line) || OBJModel.isValidFaceVertexLine(line);
	}

	private static boolean isValidGroupLine(final @NonNull String line) {
		if (OBJModel.groupMatcher != null) {
			OBJModel.groupMatcher.reset();
		}

		OBJModel.groupMatcher = OBJModel.GROUP_PATTERN.matcher(line);
		return OBJModel.groupMatcher.matches();
	}

}