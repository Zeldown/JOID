"""Generate the JOID demo model: a teapot built from the Utah teapot profile curves (public domain data).

The body and the lid are one surface of revolution swept from the cubic Bezier profile of the original patches.
The handle and the spout are swept tubes between the inner and outer Bezier curves of their patches.
Every vertex carries its own smooth normal and texture coordinate.

usage: python teapot.py   -> core/src/main/resources/assets/demo/models/model.obj + texture.png (neutral)
                             showcase/src/main/resources/assets/showcase/teapot.png (colored, for the showcase)
"""
import math, os
from PIL import Image, ImageDraw, ImageFilter

MODULE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODELS = os.path.join(os.path.dirname(MODULE), "core", "src", "main", "resources", "assets", "demo", "models")
SHOWCASE = os.path.join(MODULE, "src", "main", "resources", "assets", "showcase")

AROUND = 48
BODY_U = 0.75

PROFILE = [
	([(0.0, 3.15), (0.8, 3.15), (0.0, 2.85), (0.2, 2.7)], 7),
	([(0.2, 2.7), (0.4, 2.55), (1.3, 2.55), (1.3, 2.4)], 7),
	([(1.3, 2.4), (1.333, 2.4), (1.367, 2.4), (1.4, 2.4)], 1),
	([(1.4, 2.4), (1.3375, 2.53125), (1.4375, 2.53125), (1.5, 2.4)], 5),
	([(1.5, 2.4), (1.75, 1.875), (2.0, 1.35), (2.0, 0.9)], 9),
	([(2.0, 0.9), (2.0, 0.45), (1.5, 0.225), (1.5, 0.15)], 8),
	([(1.5, 0.15), (1.5, 0.0), (0.8, 0.0), (0.0, 0.0)], 5),
]
HANDLE_IN = [[(-1.6, 2.025), (-2.3, 2.025), (-2.7, 2.025), (-2.7, 1.6875)], [(-2.7, 1.6875), (-2.7, 1.35), (-2.5, 0.975), (-2.0, 0.75)]]
HANDLE_OUT = [[(-1.5, 2.25), (-2.5, 2.25), (-3.0, 2.25), (-3.0, 1.6875)], [(-3.0, 1.6875), (-3.0, 1.125), (-2.65, 0.7875), (-1.9, 0.45)]]
SPOUT_LOW = [[(1.7, 0.45), (3.1, 0.675), (2.4, 1.875), (3.3, 2.25)], [(3.3, 2.25), (3.525, 2.34375), (3.45, 2.3625), (3.2, 2.25)]]
SPOUT_UP = [[(1.7, 1.275), (2.6, 1.275), (2.3, 1.95), (2.7, 2.25)], [(2.7, 2.25), (2.8, 2.325), (2.9, 2.325), (2.8, 2.25)]]


def bezier(points, t):
	a, b, c, d = points
	s = 1 - t
	return tuple(s * s * s * a[i] + 3 * s * s * t * b[i] + 3 * s * t * t * c[i] + t * t * t * d[i] for i in range(2))


def sub(a, b):
	return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def cross(a, b):
	return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def norm(a):
	length = math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2])
	return (a[0] / length, a[1] / length, a[2] / length) if length > 1e-9 else None


def world(x, y, z):
	return (x, z, -y)


class Grid:

	def __init__(self, rows, cols, positions, uvs, center):
		self.rows, self.cols, self.positions, self.uvs = rows, cols, positions, uvs
		self.normals = []
		flip = 0.0
		for r in range(rows):
			for c in range(cols):
				du = sub(positions[r][(c + 1) % (cols - 1)], positions[r][(c - 1) % (cols - 1)])
				dv = sub(positions[min(r + 1, rows - 1)][c], positions[max(r - 1, 0)][c])
				n = norm(cross(dv, du))
				self.normals.append(n)
				if n:
					flip += sum(n[i] * (positions[r][c][i] - center(r, c)[i]) for i in range(3))
		sign = 1 if flip >= 0 else -1
		self.normals = [tuple(sign * v for v in n) if n else None for n in self.normals]


def lathe():
	rows = []
	for index, (segment, steps) in enumerate(PROFILE):
		for step in range(0 if index == 0 else 1, steps + 1):
			rows.append(bezier(segment, step / steps))
	lengths = [0.0]
	for a, b in zip(rows, rows[1:]):
		lengths.append(lengths[-1] + math.hypot(b[0] - a[0], b[1] - a[1]))
	positions, uvs = [], []
	for r, (radius, z) in enumerate(rows):
		line, uv = [], []
		for c in range(AROUND + 1):
			angle = c / AROUND * 2 * math.pi
			line.append(world(radius * math.cos(angle), radius * math.sin(angle), z))
			uv.append((c / AROUND * BODY_U, 1 - lengths[r] / lengths[-1]))
		positions.append(line)
		uvs.append(uv)
	grid = Grid(len(rows), AROUND + 1, positions, uvs, lambda r, c: world(0, 0, rows[r][1]))
	for c in range(AROUND + 1):
		grid.normals[c] = grid.normals[c] or (0.0, 1.0, 0.0)
		grid.normals[(len(rows) - 1) * (AROUND + 1) + c] = grid.normals[(len(rows) - 1) * (AROUND + 1) + c] or (0.0, -1.0, 0.0)
	return grid


def tube(inner, outer, widths, steps, around):
	sections = []
	for index in range(len(inner)):
		for step in range(0 if index == 0 else 1, steps + 1):
			t = step / steps
			a = bezier(inner[index], t)
			b = bezier(outer[index], t)
			sections.append((a, b, widths[index][0] + (widths[index][1] - widths[index][0]) * t))
	positions, uvs, centers = [], [], []
	for r, (a, b, width) in enumerate(sections):
		cx, cz = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
		ax, az = (b[0] - a[0]) / 2, (b[1] - a[1]) / 2
		line, uv = [], []
		for c in range(around + 1):
			angle = c / around * 2 * math.pi
			line.append(world(cx + math.cos(angle) * ax, math.sin(angle) * width, cz + math.cos(angle) * az))
			uv.append((BODY_U + 0.03 + c / around * (1 - BODY_U - 0.06), 1 - r / (len(sections) - 1)))
		positions.append(line)
		uvs.append(uv)
		centers.append(world(cx, 0, cz))
	return Grid(len(sections), around + 1, positions, uvs, lambda r, c: centers[r])


def write(grids, path):
	lines = ["# JOID demo model: a teapot swept from the Utah teapot Bezier profiles, with smooth vertex normals and texture coordinates", "", "o Teapot", ""]
	vertices, normals, coordinates, faces = [], [], [], []
	every = [p for g in grids for row in g.positions for p in row]
	low = [min(p[i] for p in every) for i in range(3)]
	high = [max(p[i] for p in every) for i in range(3)]
	middle = [(low[i] + high[i]) / 2 for i in range(3)]
	scale = 2.0 / (high[0] - low[0])
	offset = 0
	for g in grids:
		for r in range(g.rows):
			for c in range(g.cols):
				p = g.positions[r][c]
				vertices.append("v %.4f %.4f %.4f" % tuple((p[i] - middle[i]) * scale for i in range(3)))
				n = g.normals[r * g.cols + c] or (0.0, 1.0, 0.0)
				normals.append("vn %.4f %.4f %.4f" % n)
				coordinates.append("vt %.4f %.4f" % g.uvs[r][c])
		for r in range(g.rows - 1):
			for c in range(g.cols - 1):
				a = offset + r * g.cols + c + 1
				b, d = a + 1, a + g.cols
				e = d + 1
				faces.append("f %d/%d/%d %d/%d/%d %d/%d/%d" % (a, a, a, d, d, d, e, e, e))
				faces.append("f %d/%d/%d %d/%d/%d %d/%d/%d" % (a, a, a, e, e, e, b, b, b))
		offset += g.rows * g.cols
	text = "\n".join(lines + vertices + [""] + normals + [""] + coordinates + [""] + faces)
	text = text.replace("-0.0000", "0.0000")
	open(path, "w", newline="\r\n").write(text)
	size = [(high[i] - low[i]) * scale for i in range(3)]
	print(path, len(vertices), "vertices", len(faces), "triangles", os.path.getsize(path) // 1024, "KB", "size %.4f x %.4f x %.4f" % tuple(size))


def texture(path, top, bottom, band, accent, glaze):
	size = 1024
	image = Image.new("RGB", (size, size))
	draw = ImageDraw.Draw(image)
	for y in range(size):
		t = y / (size - 1)
		draw.line([(0, y), (size, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
	body = int(size * BODY_U)
	ornament = Image.new("RGBA", (size, size), (0, 0, 0, 0))
	o = ImageDraw.Draw(ornament)
	repeats = 12
	cell = body / repeats
	for k in range(repeats):
		cx = (k + 0.5) * cell
		for cy, radius in ((0.62 * size, 0.30 * cell), (0.42 * size, 0.12 * cell), (0.82 * size, 0.12 * cell)):
			o.polygon([(cx, cy - radius * 1.6), (cx + radius, cy), (cx, cy + radius * 1.6), (cx - radius, cy)], fill=band)
		o.ellipse((cx - 0.06 * cell + cell / 2, 0.62 * size - 0.06 * cell, cx + 0.06 * cell + cell / 2, 0.62 * size + 0.06 * cell), fill=accent)
	for y, height in ((0.36, 4), (0.475, 4), (0.765, 4), (0.875, 4)):
		o.rectangle((0, int(y * size), body, int(y * size) + height), fill=band)
	o.rectangle((0, int(0.155 * size), body, int(0.155 * size) + 6), fill=accent)
	image = image.convert("RGBA")
	image.alpha_composite(ornament.filter(ImageFilter.GaussianBlur(0.8)))
	shine = Image.new("RGBA", (size, size), (0, 0, 0, 0))
	ImageDraw.Draw(shine).rectangle((body + 30, 0, size - 30, size), fill=glaze)
	image.alpha_composite(shine.filter(ImageFilter.GaussianBlur(40)))
	image.convert("RGB").resize((512, 512), Image.LANCZOS).save(path, optimize=True)
	print(path, os.path.getsize(path) // 1024, "KB")


if __name__ == "__main__":
	grids = [lathe(), tube(HANDLE_IN, HANDLE_OUT, [(0.3, 0.3), (0.3, 0.3)], 10, 16), tube(SPOUT_LOW, SPOUT_UP, [(0.66, 0.25), (0.25, 0.17)], 12, 20)]
	write(grids, os.path.join(MODELS, "model.obj"))
	texture(os.path.join(MODELS, "texture.png"), (252, 252, 252), (238, 238, 238), (153, 153, 153, 255), (153, 153, 153, 255), (255, 255, 255, 0))
	os.makedirs(SHOWCASE, exist_ok=True)
	texture(os.path.join(SHOWCASE, "teapot.png"), (139, 92, 246), (236, 72, 153), (253, 230, 138, 255), (255, 255, 255, 255), (255, 255, 255, 40))