"""Render the README showcase clips frame by frame on the Vulkan snapshot backend.

usage: python showcase.py <clip|all> [first_frame] [last_frame] [--batch N]
Clips: hero, reactive, type, effects, motion, form, player, model, lists, drag, theme, inspect, design (in the README order), or all.
Frames land in showcase/build/frames/<clip>/f0000.png (raw 1920x1080), plus cursor.json (the pointer of every frame).
Each batch replays the clip from its first frame, so a batch of N frames keeps at most N images in memory.
"""
import json, os, sys

import render

FRAMES = os.path.join(render.MODULE, "build", "frames")
STEPS = [33, 33, 34]


def smooth(t):
	t = max(0.0, min(1.0, t))
	return t * t * t * (t * (t * 6 - 15) + 10)


class Script:

	def __init__(self, ui, start=(2100, 1200)):
		self.ui = ui
		self.frames = []
		self.pending = []
		self.mouse = start
		self.pressed = False
		self.visible = False

	def _step(self):
		return STEPS[len(self.frames) % 3]

	def _frame(self, command):
		self.frames.append({"commands": self.pending + [command], "mouse": self.mouse, "pressed": self.pressed, "visible": self.visible})
		self.pending = []

	def idle(self, count):
		for _ in range(count):
			self._frame("wait %d" % self._step())
		return self

	def move(self, x, y, count, ease=smooth):
		self.visible = True
		x0, y0 = self.mouse
		for i in range(1, count + 1):
			p = ease(i / count)
			self.mouse = (x0 + (x - x0) * p, y0 + (y - y0) * p)
			self._frame("moveto %.2f %.2f %d" % (self.mouse[0], self.mouse[1], self._step()))
		return self

	def jump(self, x, y):
		self.mouse = (x, y)
		self.pending.append("move %.2f %.2f" % (x, y))
		return self

	def press(self):
		self.pressed = True
		self.pending.append("press LEFT")
		return self

	def release(self):
		self.pressed = False
		self.pending.append("release")
		return self

	def hide(self):
		self.visible = False
		return self

	def raw(self, command):
		self.pending.append(command)
		return self

	def click(self, hold=2):
		self.press().idle(hold).release()
		return self

	def type(self, value, every=2):
		chunks = []
		for char in value:
			if chunks and chunks[-1].endswith(" "):
				chunks[-1] += char
			else:
				chunks.append(char)
		for chunk in chunks:
			self.pending.append("type " + chunk)
			self.idle(every)
		return self

	def scroll(self, value, count=1, every=3):
		for _ in range(count):
			self.pending.append("scroll %d" % value)
			self.idle(every)
		return self


def knob(x, width, value):
	return x + 18 + value * (width - 36)


def reactive():
	s = Script("ShowReactive", start=(1250, 1150))
	s.idle(12)
	s.move(knob(156, 610, 0.35), 368, 22)
	s.idle(4).press().idle(3)
	s.move(knob(156, 610, 0.93), 368, 34)
	s.idle(5)
	s.move(knob(156, 610, 0.62), 368, 22)
	s.idle(3).release().idle(6)
	s.move(knob(156, 610, 0.2), 508, 20)
	s.idle(3).press().idle(3)
	s.move(knob(156, 610, 0.88), 508, 40)
	s.idle(3).release().idle(6)
	s.move(knob(156, 610, 0.3), 648, 20)
	s.idle(3).press().idle(3)
	s.move(knob(156, 610, 0.82), 648, 30)
	s.idle(3)
	s.move(knob(156, 610, 0.58), 648, 20)
	s.idle(3).release().idle(6)
	s.move(728, 757, 24)
	s.idle(4).press().idle(2).release()
	s.idle(40)
	return s


def hero():
	s = Script("ShowHero")
	s.idle(180)
	return s


def typography():
	s = Script("ShowType")
	s.idle(225)
	return s


def drag():
	s = Script("ShowDrag", start=(1500, 1180))
	s.idle(10)
	s.move(1250, 830, 20)
	s.move(595, 790, 26)
	s.idle(10).press().idle(4)
	s.move(640, 520, 20)
	s.move(672, 330, 18)
	s.idle(4).release()
	s.idle(40)
	s.move(925, 800, 24)
	s.idle(12)
	s.move(1585, 800, 30)
	s.idle(16)
	s.move(1700, 1150, 22)
	s.idle(20)
	return s


def effects():
	s = Script("ShowEffects", start=(1300, 1150))
	s.idle(15)
	for i in range(7):
		s.move(688, 321 + 88 * i, 20 if i == 0 else 11)
		s.idle(2).click().idle(11)
	s.idle(30)
	s.move(1500, 1150, 24)
	s.idle(20)
	return s


def motion():
	s = Script("ShowMotion")
	s.idle(264)
	return s


def form():
	s = Script("ShowForm", start=(1100, 1150))
	s.idle(12)
	s.move(560, 366, 24).idle(3).click().idle(6)
	s.type("Ada Lovelace")
	s.idle(10)
	s.move(560, 506, 14).idle(3).click().idle(6)
	s.type("ada@lovelace.dev")
	s.idle(14)
	s.move(292, 366, 22).idle(6)
	s.click().idle(2).click().idle(14)
	s.type("Byron", 3)
	s.idle(16)
	s.move(600, 1150, 26)
	s.idle(30)
	return s


def player():
	s = Script("ShowPlayer", start=(1500, 1180))
	s.idle(20)
	s.move(1000, 640, 26).idle(16)
	s.move(338, 828, 24).idle(6).click().idle(30)
	s.move(700, 828, 18).idle(4).press().idle(3)
	s.move(1150, 828, 30).idle(6).release().idle(24)
	s.move(338, 828, 26).idle(5).click().idle(50)
	s.move(1500, 1180, 26)
	s.idle(36)
	return s


def model():
	s = Script("ShowModel", start=(1500, 1150))
	s.idle(15)
	s.move(560, 520, 26).idle(4).press().idle(3)
	s.move(980, 540, 40).idle(3)
	s.move(760, 420, 30).idle(3).release().idle(16)
	s.scroll(120, 6, 3).idle(16)
	s.scroll(-120, 5, 3).idle(10)
	s.press().idle(3)
	s.move(420, 560, 36).idle(3).release().idle(30)
	s.move(1500, 1150, 24)
	s.idle(20)
	return s


def lists():
	s = Script("ShowLists", start=(1500, 1150))
	s.idle(15)
	s.move(900, 284, 26).idle(6).press().idle(4)
	s.move(900, 500, 36).idle(10).release().idle(24)
	s.scroll(-360, 8, 4).idle(30)
	s.scroll(360, 8, 4).idle(26)
	s.move(1500, 1150, 24)
	s.idle(20)
	return s


def theme():
	s = Script("ShowTheme", start=(1200, 700))
	s.idle(24)
	s.move(1786, 98, 26).idle(6).click().idle(70)
	s.click().idle(64)
	s.move(1200, 700, 26)
	s.idle(10)
	return s


def inspect():
	s = Script("ShowInspect", start=(1240, 760))
	s.visible = True
	s.raw("dev true").raw("key F5").raw("key F3").raw("key I").idle(16)
	s.move(1720, 1034, 28).idle(10).click().idle(6)
	s.move(980, 600, 32).idle(6)
	s.move(720, 400, 22).idle(8)
	s.move(1720, 1034, 32).idle(4).click().idle(8)
	s.move(1686, 1034, 10).idle(6).click().idle(24)
	s.move(1824, 1034, 12).idle(4).press().idle(3)
	s.move(1560, 640, 26)
	s.move(1829, 307, 24).idle(3).release().idle(8)
	s.move(1657, 307, 16).idle(6).click().idle(8)
	s.move(620, 150, 36).idle(12)
	s.move(900, 470, 26).idle(12)
	s.move(1000, 572, 14).idle(8).click().idle(14)
	s.move(1660, 200, 30).idle(8)
	s.scroll(-120, 9, 6).idle(30)
	return s


def design():
	s = Script("ShowDesign", start=(1320, 1000))
	s.visible = True
	s.idle(14)
	s.move(1682, 366, 22).idle(4).click().idle(10)
	s.move(1251, 602, 18).idle(4).press().idle(3)
	s.move(1586, 602, 24).idle(3).release().idle(8)
	s.move(456, 752, 28).idle(4).click().idle(40)
	s.move(1682, 452, 28).idle(4).click().idle(10)
	s.move(1586, 602, 14).idle(4).press().idle(3)
	s.move(1380, 602, 20).idle(3).release().idle(8)
	s.move(1560, 862, 18).idle(4).click().idle(20)
	s.move(246, 752, 30).idle(4).click().idle(40)
	s.move(560, 940, 24).idle(16)
	return s


ORDER = ["hero", "reactive", "type", "effects", "motion", "form", "player", "model", "lists", "drag", "theme", "inspect", "design"]

CLIPS = {"design": design, "inspect": inspect, "drag": drag, "reactive": reactive, "hero": hero, "type": typography, "effects": effects, "motion": motion, "form": form, "player": player, "model": model, "lists": lists, "theme": theme}


def frames(clip, first, last, batch):
	script = CLIPS[clip]()
	out = os.path.join(FRAMES, clip)
	os.makedirs(out, exist_ok=True)
	json.dump([{"mouse": f["mouse"], "pressed": f["pressed"], "visible": f["visible"]} for f in script.frames], open(os.path.join(out, "cursor.json"), "w"))
	last = len(script.frames) - 1 if last is None else min(last, len(script.frames) - 1)
	start = first
	while start <= last:
		end = min(last, start + batch - 1)
		lines = ["ui " + render.PACKAGE + script.ui]
		for index, frame in enumerate(script.frames[:end + 1]):
			lines += frame["commands"]
			if index >= start:
				lines.append("shot f%04d" % index)
		render.render(lines, out)
		print("%s: frames %d-%d of %d done" % (clip, start, end, len(script.frames)))
		start = end + 1


if __name__ == "__main__":
	args = [a for a in sys.argv[1:] if not a.startswith("--")]
	batch = 110
	if "--batch" in sys.argv:
		batch = int(sys.argv[sys.argv.index("--batch") + 1])
		args.remove(str(batch))
	if not args or args[0] not in list(CLIPS) + ["all"]:
		sys.exit(__doc__)
	first = int(args[1]) if len(args) > 1 else 0
	last = int(args[2]) if len(args) > 2 else None
	for clip in (ORDER if args[0] == "all" else [args[0]]):
		frames(clip, first, last, batch)