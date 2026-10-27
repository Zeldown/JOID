---
summary: The system properties of a snapshot run: where the references and the renders go, and whether the references are recorded.
---

Learn: [References and the report](/build/testing-a-backend/#references-and-the-report).

## isUpdate()

If `true`, the property `joid.snapshot.update` is set: every render replaces its reference.

## isRecord()

If `true`, the property `joid.snapshot.record` is set: a shot without a reference becomes its reference.

## getOutput()

Returns the folder of the renders and of the report, `joid.snapshot.output`, `build/snapshots/renders` by default.

## getReferences()

Returns the folder of the references, `joid.snapshot.references`, `.snapshots/references` by default.
