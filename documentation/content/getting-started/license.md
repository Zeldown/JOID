# License

JOID is released under the Apache License, Version 2.0. The full text is in the [`LICENSE`](https://github.com/Zeldown/JOID/blob/main/LICENSE) file at the root of the repository and in `META-INF/LICENSE` inside every JOID jar; the attribution notices are in [`NOTICE`](https://github.com/Zeldown/JOID/blob/main/NOTICE) and `META-INF/NOTICE`.

## What the license allows

You can use, copy, modify and distribute JOID, in source or compiled form, in commercial or non-commercial, open or closed software, and sublicense it as part of your own work (section 2). Each contributor also grants a patent license for their contributions (section 3); that license ends for anyone who starts patent litigation claiming that JOID infringes a patent.

## What it asks when you redistribute JOID

When you distribute JOID or a work that contains it, for example an application that ships a JOID jar, section 4 asks you to:

| Obligation | In practice |
| --- | --- |
| Give recipients a copy of the license | Keep `META-INF/LICENSE` in the jar you ship, or ship the `LICENSE` file with your product. |
| Mark modified files | State in each JOID file you changed that you changed it. |
| Keep the notices | Keep the copyright, patent, trademark and attribution notices of the source form. |
| Pass the `NOTICE` on | Include the attribution notices of `NOTICE` in a `NOTICE` file, in your documentation, or in a display your product shows where third-party notices normally appear. |

The license does not grant the use of the JOID name or trademarks beyond describing the origin of the work (section 6). JOID comes without warranty (section 7) and without liability of its contributors (section 8).

A backend written from the [backend template](installation.md#backend-template) stays yours: license it as you want, as long as you keep the JOID notices in what you redistribute.

## Third-party software

The JOID jars include the following software:

| Software | License | Where |
| --- | --- | --- |
| Universal Tween Engine, by Aurelien Ribon | Apache License 2.0 | The classes under `dev.joid.lib.animation.tweenengine` are derived from it. |
| JSVG | MIT License | Embedded in the core and backend jars, relocated under `dev.joid.shaded.jsvg`. |
| TwelveMonkeys ImageIO | BSD 3-Clause License | Embedded in the core and backend jars, relocated under `dev.joid.shaded.twelvemonkeys`. |
| ASM | BSD 3-Clause License | Embedded in the core and backend jars, relocated under `dev.joid.shaded.asm`. |
| JavaCV and JavaCPP | Apache License 2.0 | Embedded in the core and backend jars. |
| FFmpeg, built by the JavaCPP Presets | GNU LGPL 2.1 or later | Embedded in the core and backend jars. |
| Montserrat, Pacifico, Playfair Display | SIL Open Font License 1.1 | Embedded in the `-dev` jars only, with the license text next to each font. |
| LWJGL 2 natives | BSD 3-Clause License | Embedded in the LWJGL 2 backend jar. |
| OpenAL Soft, as built for LWJGL 2 | GNU LGPL 2 or later | Embedded in the LWJGL 2 backend jar. |

The libraries you declare yourself (Guava, Gson, Apache Commons, vecmath, LWJGL) come with their own licenses. The `msdf` module reimplements in Java the multi-channel signed distance field algorithm of msdfgen by Viktor Chlumský.

## See also

- [Introduction](introduction.md)
- [Installation](installation.md)
- [Developer Tools](dev-tools.md)