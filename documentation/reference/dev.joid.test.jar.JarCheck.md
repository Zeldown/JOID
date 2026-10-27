---
summary: Starts JOID from a packaged jar, opens a UI, draws 60 frames and exits with the result, for the jar checks of a backend.
---

Call it from a main of your own, run on a packaged jar with its libraries, to check that the jar starts JOID in the modes of its variant.

Learn: [Three jars per module](/build/demos/#three-jars-per-module).

## run(String,Supplier,Runnable)

Registers the backend that the supplier returns, checks that the dev and demo modes match the variant (`prod`, `dev` or `demo`), opens a UI (a demo for `demo`), draws 60 frames with a present after each, and exits with `0`.
Exits with `1` when the modes do not match, when a stack trace reaches the error stream, or when the UI closes.
