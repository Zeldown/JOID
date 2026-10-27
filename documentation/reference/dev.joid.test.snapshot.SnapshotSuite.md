---
summary: The snapshot tests of a backend: every scenario of the testkit, pixel for pixel against the references of the machine.
---

Extend it in the tests of your backend and return your snapshot backend from `createBackend()`; the demo classes must be on the test classpath.

Learn: [The suites](/build/testing-a-backend/#the-suites), [References and the report](/build/testing-a-backend/#references-and-the-report).

## SnapshotSuite()

Creates the suite, for a subclass.

## stopRunner()

Deletes the references that no scenario takes any more, writes the report and stops the runner, after the last test of the class.

## matchesDevSnapshots()

Checks the shots of the `dev` scenario.

## matchesPopupSnapshots()

Checks the shots of the `popup` scenario.

## matchesStaticSnapshots()

Checks the shots of the `static` scenario.

## matchesWindowSnapshots()

Checks the shots of the `window` scenario.

## matchesResourceSnapshots()

Checks the shots of the `resource` scenario.

## matchesTransitionSnapshots()

Checks the shots of the `transition` scenario.

## matchesInteractionSnapshots()

Checks the shots of the `interaction` scenario.

## createBackend()

JOID calls it once, at the first test of the class.
Return a new snapshot backend of your backend, not created yet.
