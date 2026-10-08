"""Compose the rendered showcase frames (see showcase.py) into the README media.

usage: python compose.py webp <clip|all> [width] [quality] [step]   -> images/showcase-<clip>.webp
       python compose.py mp4                                        -> images/showcase.mp4 (every clip, crossfaded)
       python compose.py still <clip> <frame>                       -> images/showcase-<clip>.png
images is documentation/content/images. The mp4 needs FFmpeg: the FFMPEG variable, the PATH or a WinGet install.
"""
import glob, json, os, shutil, subprocess, sys
from PIL import Image, ImageDraw, ImageFilter

import render
import showcase

FRAMES = showcase.FRAMES
IMAGES = render.IMAGES
ORDER = showcase.ORDER

FADE = 15
SS = 4
ARROW = [(0, 0), (0, 22), (5.5, 17), (9.5, 26), (13, 24.5), (9, 16), (16, 16)]
SCALE = 1.35


def pointer():
	size = 48 * SS
	shape = Image.new("RGBA", (size, size), (0, 0, 0, 0))
	draw = ImageDraw.Draw(shape)
	points = [((x * SCALE + 6) * SS, (y * SCALE + 6) * SS) for x, y in ARROW]
	shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
	ImageDraw.Draw(shadow).polygon([(x + 2 * SS, y + 3 * SS) for x, y in points], fill=(0, 0, 0, 110))
	shadow = shadow.filter(ImageFilter.GaussianBlur(3 * SS))
	draw.polygon(points, fill=(0, 0, 0, 255))
	inner = []
	cx, cy = sum(p[0] for p in points) / len(points), sum(p[1] for p in points) / len(points)
	for x, y in points:
		dx, dy = cx - x, cy - y
		length = max(1e-6, (dx * dx + dy * dy) ** 0.5)
		inner.append((x + dx / length * 1.7 * SS, y + dy / length * 1.7 * SS))
	draw.polygon(inner, fill=(255, 255, 255, 255))
	result = Image.alpha_composite(shadow, shape)
	return result.resize((48, 48), Image.LANCZOS)


POINTER = pointer()


def ring(alpha):
	size = 64 * SS
	image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
	draw = ImageDraw.Draw(image)
	draw.ellipse((6 * SS, 6 * SS, 58 * SS, 58 * SS), fill=(255, 255, 255, int(40 * alpha)), outline=(255, 255, 255, int(150 * alpha)), width=2 * SS)
	return image.resize((64, 64), Image.LANCZOS)


def frames(clip):
	folder = os.path.join(FRAMES, clip)
	cursor = json.load(open(os.path.join(folder, "cursor.json")))
	press = 0.0
	for index, state in enumerate(cursor):
		image = Image.open(os.path.join(folder, "f%04d.png" % index)).convert("RGBA")
		press = min(1.0, press + 0.34) if state["pressed"] else max(0.0, press - 0.25)
		if state["visible"]:
			x, y = state["mouse"]
			if press > 0:
				image.alpha_composite(ring(press), (int(round(x)) - 32, int(round(y)) - 32))
			image.alpha_composite(POINTER, (int(round(x)) - 6, int(round(y)) - 6))
		yield image.convert("RGB")


def webp(clip, width=960, quality=82, step=1):
	images = [image.resize((width, width * 9 // 16), Image.LANCZOS) for index, image in enumerate(frames(clip)) if index % step == 0]
	target = os.path.join(IMAGES, "showcase-%s.webp" % clip)
	images[0].save(target, save_all=True, append_images=images[1:], duration=round(1000 / 30 * step), loop=0, quality=quality, method=6)
	print(target, len(images), "frames", os.path.getsize(target) // 1024, "KB")


def ffmpeg():
	found = os.environ.get("FFMPEG") or shutil.which("ffmpeg")
	if not found:
		found = next(iter(sorted(glob.glob(os.path.join(os.environ.get("LOCALAPPDATA", ""), "Microsoft", "WinGet", "Packages", "Gyan.FFmpeg*", "*", "bin", "ffmpeg.exe")), reverse=True)), None)
	if not found:
		sys.exit("FFmpeg not found: set FFMPEG to the path of the ffmpeg executable")
	return found


def mp4():
	target = os.path.join(IMAGES, "showcase.mp4")
	proc = subprocess.Popen([ffmpeg(), "-y", "-loglevel", "error", "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", "1920x1080", "-r", "30", "-i", "-",
		"-c:v", "libx264", "-preset", "slow", "-crf", "21", "-tune", "animation", "-pix_fmt", "yuv420p", "-movflags", "+faststart", target], stdin=subprocess.PIPE)
	tail = []
	count = 0
	for position, clip in enumerate(ORDER):
		images = list(frames(clip))
		for index, image in enumerate(images):
			if position > 0 and index < FADE:
				image = Image.blend(tail[index], image, (index + 1) / (FADE + 1))
			if position < len(ORDER) - 1 and index >= len(images) - FADE:
				tail.append(image)
				continue
			proc.stdin.write(image.tobytes())
			count += 1
		if position < len(ORDER) - 1:
			tail = tail[-FADE:]
		print(clip, "done")
	proc.stdin.close()
	proc.wait()
	print(target, count, "frames", os.path.getsize(target) // 1024, "KB")


def still(clip, frame):
	images = frames(clip)
	for index, image in enumerate(images):
		if index == frame:
			target = os.path.join(IMAGES, "showcase-%s.png" % clip)
			image.save(target, optimize=True)
			print(target, os.path.getsize(target) // 1024, "KB")
			return


if __name__ == "__main__":
	if len(sys.argv) < 2 or sys.argv[1] not in ("webp", "mp4", "still"):
		sys.exit(__doc__)
	if sys.argv[1] == "webp":
		for clip in (ORDER if sys.argv[2] == "all" else [sys.argv[2]]):
			webp(clip, *[int(a) for a in sys.argv[3:]])
	elif sys.argv[1] == "mp4":
		mp4()
	else:
		still(sys.argv[2], int(sys.argv[3]))