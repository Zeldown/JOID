---
summary: Writes a `font.msdf` file: the face, the metrics, the glyphs, the kerning and the distance field, compressed.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## write(File,List,Kerning,double[],String,int,boolean,double,double,int[],int,int)

Writes the file with its signature, then the version, the weight, the italic flag, the name, the atlas size, the range, the glyph size, the metrics, every glyph, the kerning pairs and the pixels.
Throws an `IOException` when the file cannot be written.

## VERSION

The version of the file format, part of the cache key of the atlases that JOID generates.
