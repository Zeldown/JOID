---
summary: How long a store lives and which UIs share it.
---
Learn: [Stores and properties](/use/learn/state/stores-and-properties/#choose-where-a-value-lives).

## LOCAL

Each UI that asks for the store gets its own, destroyed when that UI closes.

## GLOBAL

One store for the whole application, shared by every UI, kept in memory until the program ends.

## PERMANENT

Like `GLOBAL`, and saved in the data folder so that its values come back at the next run.

## isLocal()

If `true`, each UI gets its own store.

## isGlobal()

If `true`, the store is shared by every UI.
