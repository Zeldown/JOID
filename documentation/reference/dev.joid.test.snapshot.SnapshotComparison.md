---
summary: Compares the renders of several backends with those of a reference backend, and writes a report.
---

Learn: [Compare with LWJGL 3](/build/testing-a-backend/#compare-with-lwjgl-3).

## main(String[])

Takes the folder of the report, the folder of the reference renders, then one folder per candidate backend.
Compares every shot with a tolerance of one level per channel, warns when two `renderer.txt` name different GPUs, writes `report.html`, and exits with `1` when a shot differs or is missing.
