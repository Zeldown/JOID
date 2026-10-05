# License

JOID is released under the **Apache License 2.0**. The full text sits at the root of the repository, in [LICENSE](https://github.com/Zeldown/JOID/blob/main/LICENSE), and ships inside every JAR under `META-INF/LICENSE`.

## What you can do

Use JOID in commercial products, closed-source applications, paid games, internal tools or open-source projects. Fork it, modify it, embed it, resell what you build with it. Nothing to request, no revenue share, no restriction on the kind of interface you draw with it.

## What you owe

Only when you redistribute JOID itself — alone, modified, or embedded in your own artifact. Section 4 of the license asks four things:

| Obligation | In practice |
|---|---|
| Include the license | Keep `META-INF/LICENSE` in the JAR you ship, or put a copy next to your binary. |
| Keep the notices | Do not strip the copyright, patent, trademark and attribution notices from what you redistribute. |
| State your changes | Modified a JOID file? Say so — a changelog line or a header is enough. |
| Pass the NOTICE on | Reproduce the content of `META-INF/NOTICE` in your own notice file, documentation or credits screen. |

Using JOID as a library, without redistributing JOID itself, asks nothing of you.

## Patents and trademark

Every contributor grants you a patent license covering their contribution. Starting patent litigation over JOID terminates your own grant.

The **JOID** name itself is not licensed: give your fork another name. Naming JOID in an attribution notice is expected and permitted.

## Third-party components

| Component | License | Where |
|---|---|---|
| Universal Tween Engine | Apache-2.0 | bundled in `lib/animation/tweenengine` |
| msdfgen, the algorithm | MIT | reimplemented in Java in `msdf/`, released as its own zip |
| LWJGL | BSD-3-Clause | declared by your application |
| JavaCV | Apache-2.0 | declared by your application, for video |
| FFmpeg builds | their own terms | declared by your application, for video |

JOID JARs embed no third-party library: the versions you declare are the ones that run. See [Installation](installation.md).
