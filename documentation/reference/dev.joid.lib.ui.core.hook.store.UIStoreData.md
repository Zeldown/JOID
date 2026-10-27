---
summary: Annotation that gives a store its scope and the name of its file.
---
Every [[UIStore]] subclass needs it.

Learn: [Stores and properties](/use/learn/state/stores-and-properties/#choose-where-a-value-lives).

## id()

Name of the file of a `PERMANENT` store, `store/<id>.store` in the data folder; empty uses the full name of the class.

## scope()

How long the store lives and who shares it.
