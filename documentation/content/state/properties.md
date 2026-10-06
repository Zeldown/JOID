# Persistent UI Properties

`@UIProperty` marks a field of a UI to save when the UI closes and to restore the next time the UI initializes, even after a restart. Use it for small view state the user expects to find again: the selected tab, a sort order, a collapsed panel.

## Saving a field with @UIProperty

```java
public class InventoryUI extends UI {

    @UIProperty
    private int tab;

    @UIProperty("sort")
    private String sortOrder = "name";

    @Override
    public void init() {
        RectNode
        .create(100, 100, 200, 60)
        .color(Color.WHITE)
        .onClick((node, mouseX, mouseY, clickType) -> this.tab = (this.tab + 1) % 3)
        .attach(this);
    }

}
```

When the UI closes, `tab` and `sortOrder` are written to `config/property/<package>.InventoryUI.property`, for example `{"tab":1,"sort":"name"}`. When an `InventoryUI` initializes again, the saved values replace the field initializers before `init()` runs. `UIProperty` and `UIPropertyHook` are in `dev.joid.lib.ui.core.hook.property`.

## Fields and keys

| Rule | Detail |
|---|---|
| Annotation | `@UIProperty` or `@UIProperty("key")`. |
| Key | The annotation value, or the field name when the value is empty (the default). |
| Fields | Every annotated field of the UI class and of its superclasses, whatever its visibility. `final` fields are ignored. |
| Types | Anything Gson can write and read back through the declared type of the field, generics included: primitives and their wrappers, `String`, enums, arrays, lists, sets, maps, plain objects. |
| `null` | A field holding `null` is removed from the file, so the next load keeps its initializer. |

## The property file

- Path: `<config dir>/property/<fully qualified UI class name>.property`, where the config dir is `JOID.inst().getConfigDir()` (`config` in the working directory unless you call `setConfigDir(File)`). The folder is created on the first save.
- Content: one JSON object, written in UTF-8 by Gson, with one entry per key.
- One file per UI class: every instance of the class reads and writes the same file.
- Saving merges into the existing file: the entries of fields you renamed or removed stay in it.

## Lifecycle

| Moment | What happens |
|---|---|
| The UI initializes (first open, `UI.reload()`, the dev reload shortcut, hot reload) | The file is read before `init()`; each key found in it is written into its field. Fields without a saved key keep their current value. |
| The UI closes (`JOID.close`, Escape on a closeable UI, a forced close) | Every annotated field is written to the file. |

> WARNING: A reload reads the file again, so the values changed since the last save go back to the saved ones. Save first when the current values must survive the reload: `UIPropertyHook.save(this)` then `this.reload()`.

Nothing is saved when the application exits with the UI still open; call `UIPropertyHook.save(ui)` in your shutdown path when that matters.

## UIPropertyHook

| Method | Description |
|---|---|
| `UIPropertyHook.load(UI ui)` | Reads the file of the UI's class and writes the saved values into its annotated fields. |
| `UIPropertyHook.save(UI ui)` | Writes the annotated fields of the UI to its file. |

## Errors

| Situation | Behavior |
|---|---|
| The file cannot be parsed | Prints `Failed to load property file: <path>`, deletes the file, and every field keeps its value. |
| A saved value does not match the field type | Prints the stack trace; that field keeps its value, the others are restored. |
| The folder or the file cannot be written | Prints `Failed to create property directory: <path>` or `Failed to save property file: <path>`. |

## Properties or stores

| Need | Use |
|---|---|
| A few fields of one UI class, restored when it opens again | `@UIProperty` |
| State shared between UIs, or saved in a format you control, or holding signals | A [store](stores.md) (`GLOBAL` or `PERMANENT`) |

## See also

- [Stores](stores.md)
- [The UI Class](../ui/ui-class.md)
- [Opening and Closing UIs](../ui/managing-uis.md)