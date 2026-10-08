"""Render a few frames of a showcase clip and lay them out on a contact sheet, to check a clip before rendering it all.

usage: python probe.py <clip> <frame> [frame ...]   -> sample/showcase/build/probe/<clip>/f0000.png + sample/showcase/build/probe/<clip>.png
"""
import os, sys
from PIL import Image

import render
import showcase

if len(sys.argv) < 3 or sys.argv[1] not in showcase.CLIPS:
	sys.exit(__doc__)
clip, picks = sys.argv[1], sorted(int(a) for a in sys.argv[2:])
script = showcase.CLIPS[clip]()
lines = ["ui " + render.PACKAGE + script.ui]
for index, frame in enumerate(script.frames[:picks[-1] + 1]):
	lines += frame["commands"]
	if index in picks:
		lines.append("shot f%04d" % index)
out = os.path.join(render.MODULE, "build", "probe", clip)
render.render(lines, out)
cols = 3
sheet = Image.new("RGB", (640 * cols, 360 * ((len(picks) + cols - 1) // cols)))
for k, i in enumerate(picks):
	image = Image.open(os.path.join(out, "f%04d.png" % i)).convert("RGB").resize((640, 360))
	sheet.paste(image, ((k % cols) * 640, (k // cols) * 360))
sheet.save(out + ".png")
print(out + ".png")