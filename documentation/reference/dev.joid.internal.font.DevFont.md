---
summary: The font of the dev tools, Montserrat in its nine weights, loaded at startup in dev mode only.
---

The inspector draws with it.
It is not in the prod jar, and JOID ships no production font: load a font of your own for your text.

Learn: [Dev tools](/use/get-started/dev-tools/).

## load()

Loads the nine weights and waits for them; JOID calls it once when it starts in dev mode.

## MONTSERRAT

The loaded font, `null` outside dev mode.
