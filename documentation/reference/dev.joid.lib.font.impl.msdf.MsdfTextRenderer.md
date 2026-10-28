---
summary: The text renderer of every `MsdfFont`, which draws each glyph from the distance field of its face.
---

You never call it yourself: [[MsdfFont#getTextRenderer]] returns it to [[DrawText]].
It draws each glyph as a quad of the atlas through the font shader of JOID; the baseline snaps to the pixel grid so that a line keeps the same sharpness wherever it sits.

Learn: [MSDF text](/use/in-depth/msdf-text/).

## inst()

Returns the shared renderer.
