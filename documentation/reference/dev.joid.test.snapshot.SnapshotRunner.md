---
summary: Runs commands and scenarios on a hidden 1920×1080 surface with a manual clock, and returns the shots by name.
---

Start it once per test class with a snapshot backend, run commands, and stop it at the end.

Learn: [Testing your UIs](/use/learn/running/testing/), [References and the report](/build/testing-a-backend/#references-and-the-report).

## start(ISnapshotBackend)

Creates the surface at 1920×1080 pixels with the testkit's own UI bridge, then registers a window bridge, a manual clock at 2025-01-01 00:00 UTC, a silent audio bridge and a locator that caches downloads in the `snapshot` area of the cache folder; turns the dev mode off.

## stop()

Closes every UI, destroys the surface, gives back the bridges that were there before and the dev mode.

## getRenderer()

Returns the name of the renderer with every run of other characters than letters and digits replaced by `-`, the name of its folder of references.

## writeRenderer(File)

Writes the full name of the renderer into `renderer.txt` in that folder.

## getScenarios()

Returns the names of the scenarios of the testkit: `dev`, `popup`, `static`, `window`, `resource`, `transition` and `interaction`.

## getShots(String)

Returns the names of the shots of a scenario of the testkit.

## run(String)

Runs a scenario of the testkit and returns its shots by name.

## execute(String[])

Runs the commands, one per string, and returns the shots by name, in order.
It starts from a clean state: no UI open, the window at 1920×1080, an interface scale of `1D`, no key held, no mask, the clock at its start and the dev mode off.
Empty lines and lines that start with `#` are skipped; an unknown command throws an `IllegalArgumentException`.

| Command | Effect |
|---|---|
| `ui <class>` | Creates the UI with its constructor without argument, of any visibility, and shows it alone, with the mouse outside the window. |
| `open <class>` | Creates the UI the same way and opens it with `JOID.open`, over what is open. |
| `wait <ms>` | Advances the clock by that time, one frame every 16 ms, and waits for the media to settle. |
| `move <x> <y>` | Puts the mouse at that position, in screen pixels, and sends a move. |
| `moveto <x> <y> <ms>` | Moves the mouse there over that time, one frame and one move every 16 ms. |
| `press [button]` | Presses a mouse button, `LEFT` by default. |
| `release [button]` | Releases a mouse button, `LEFT` by default. |
| `scroll [x] <y>` | Scrolls by that many notches, vertically, or horizontally and vertically. |
| `type <text>` | Types the text, key by key, with Shift held for capitals and shifted symbols. |
| `key <keys>` | Presses and releases a key with the others held, such as `LEFT_CONTROL+A`. |
| `down <key>` | Holds a key down. |
| `up <key>` | Releases a held key. |
| `resize <width> <height>` | Resizes the window, at most 1920×1080. |
| `zoom <level>` | Sets the zoom of every open UI. |
| `scale <factor>` | Sets the interface scale of the UI bridge. |
| `dev <true\|false>` | Turns the dev mode on or off. |
| `mask <x> <y> <width> <height>` | Fills that rectangle of every later shot with magenta, to hide what changes between runs. |
| `unmask` | Removes every mask. |
| `cursor <cursor>` | Throws an `IllegalStateException` when the window does not show that cursor. |
| `shot <name>` | Waits until every resource is loaded and two frames in a row are identical, then captures the window under that name. |

A `shot` throws an `IllegalStateException` when a resource does not settle within 30 seconds, or when the picture keeps changing while the clock is paused.
