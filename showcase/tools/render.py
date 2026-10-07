"""Render showcase scenarios with the Vulkan snapshot backend (deterministic clock, 1920x1080 canvas).

usage: python render.py <scenario.txt> <output folder>
As a module: render.render(lines, folder) writes one PNG per "shot <name>" command into the folder.
The first call builds the showcase with Gradle (:showcase:renderClasspath), the next ones reuse its classpath.
Commands are those of the testkit SnapshotRunner: ui <class>, wait <ms>, move <x> <y>, moveto <x> <y> <ms>,
press LEFT, release, scroll <n>, type <text>, key <KEY>, dev true|false, shot <name>. Lines starting with # are skipped.
"""
import os, subprocess, sys

TOOLS = os.path.dirname(os.path.abspath(__file__))
MODULE = os.path.dirname(TOOLS)
ROOT = os.path.dirname(MODULE)
WORK = os.path.join(MODULE, "build", "render")
IMAGES = os.path.join(ROOT, "documentation", "content", "images")
PACKAGE = "dev.joid.showcase."

_classpath = None


def java():
	home = os.environ.get("JAVA_HOME")
	if home:
		for name in ("java.exe", "java"):
			candidate = os.path.join(home, "bin", name)
			if os.path.isfile(candidate):
				return candidate
	return "java"


def classpath():
	global _classpath
	if _classpath is None:
		gradlew = os.path.join(ROOT, "gradlew.bat" if os.name == "nt" else "gradlew")
		r = subprocess.run([gradlew, ":showcase:renderClasspath", "--offline", "-q"], cwd=ROOT, capture_output=True, text=True, encoding="utf-8", errors="replace")
		if r.returncode:
			sys.exit("gradle failed:\n" + r.stdout[-4000:] + r.stderr[-4000:])
		_classpath = open(os.path.join(WORK, "classpath.txt"), encoding="utf-8").read().strip()
	return _classpath


def render(lines, out):
	path = classpath()
	os.makedirs(out, exist_ok=True)
	scenario = os.path.join(WORK, "scenario.txt")
	open(scenario, "w", encoding="utf-8").write("\n".join(lines) + "\n")
	r = subprocess.run([java(), "-Xmx3g", "-Djoid.config=" + os.path.join(WORK, "config"), "-Djoid.snapshot.cache=" + os.path.join(ROOT, ".snapshots", "cache"),
		"-cp", path, PACKAGE + "ShowcaseRender", scenario, out], cwd=WORK, capture_output=True, text=True, encoding="utf-8", errors="replace")
	if r.returncode:
		sys.exit("render failed:\n" + r.stdout[-4000:] + r.stderr[-4000:])
	log = [l for l in (r.stdout + r.stderr).splitlines() if ("[JOID]" in l and "Loading JOID" not in l) or "Exception" in l]
	if log:
		print("console:\n  " + "\n  ".join(log[:40]))


if __name__ == "__main__":
	if len(sys.argv) != 3:
		sys.exit(__doc__)
	render(open(sys.argv[1], encoding="utf-8").read().splitlines(), os.path.abspath(sys.argv[2]))