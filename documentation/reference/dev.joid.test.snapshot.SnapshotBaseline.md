---
summary: Renders every snapshot scenario with a given snapshot backend into a folder, the baseline that the other backends are compared with.
---

Learn: [Compare with LWJGL 3](/build/testing-a-backend/#compare-with-lwjgl-3).

## main(String[])

Takes the class name of a snapshot backend and an output folder: deletes the PNG files of the folder, writes `renderer.txt` and one PNG per shot of every scenario, prints the count and exits.
The backend needs a constructor without argument, of any visibility; a class that is missing or cannot be created throws an `IllegalArgumentException` that says why.
