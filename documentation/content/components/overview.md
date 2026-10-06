# Component Catalog

Every node JOID ships, grouped by purpose. All of them share the [node fundamentals](../nodes/node-fundamentals.md): position and size, `attach`, `body`, callbacks, effects and the fluent API. If you are new to nodes, read [Essentials: Nodes](../essentials/nodes.md) first.

JOID is design-neutral: no component imposes a look. Some components are **abstract**: they handle the state, the input and the values, and you draw them once in a small subclass so they match your design. Those subclasses are your **UI kit**: your screens use them with the same code whatever kit draws them, so another kit gives the same screens a completely different design. [Building a UI Kit](ui-kit.md) shows two complete kits rendering the same code; each component page also starts with a subclass you can copy.

## Layout

Nodes that group and place other nodes. They draw nothing by themselves.

| Component | Use it for |
| --- | --- |
| [ContainerNode](../nodes/layout/container.md) | Grouping children under a common origin, clipping, scrolling, or loading a section of a UI. |
| [FlexNode](../nodes/layout/flex.md) | Lists, toolbars and menus: children one after the other in a row or a column. |
| [GridNode](../nodes/layout/grid.md) | Tiles of the same size: inventories, galleries, icon pickers. |
| [ReorderableFlexNode](../nodes/layout/reorderable-flex.md) | A row or column the user reorders by dragging. |
| [Overflow and Scrolling](../nodes/layout/overflow-and-scroll.md) | Clipping or scrolling the children of any node, with or without a scrollbar. |

## Display

Nodes that draw something.

| Component | Use it for |
| --- | --- |
| [RectNode](../nodes/visual/rect.md) | Backgrounds, cards, buttons, separators, click areas. |
| [CircleNode](../nodes/visual/circle.md) | Dots, status indicators, avatar placeholders. |
| [TextNode](../nodes/visual/text.md) | A line, a truncated line or a wrapped paragraph of text. |
| [ResourceNode](../nodes/visual/resource.md) | Images, SVGs and animated images, fitted with STRETCH, CONTAIN or COVER. |
| [ResourcePlayerNode](../nodes/visual/resource-player.md) | Videos and animations with playback control and sound. |
| [ModelNode and ModelViewerNode](../nodes/visual/model.md) | 3D models, static or with drag-to-rotate and zoom. |
| [ProgressNode](../nodes/visual/progress.md) | Loading bars, health bars, gauges. |

## Inputs

Nodes the user edits. Each one keeps its value and calls you back when it changes.

| Component | Abstract | Use it for |
| --- | --- | --- |
| [TextFieldNode](../nodes/input/text-field.md) | No | A single line of text: names, search boxes. |
| [MultilineTextFieldNode](../nodes/input/multiline-text-field.md) | No | A text area that wraps and scrolls. |
| [SliderNode](../nodes/input/slider.md) | Yes | Picking a number or a value from an ordered set. |
| [CheckboxNode](../nodes/input/checkbox.md) | Yes | A yes / no choice. |
| [ToggleNode](../nodes/input/toggle.md) | Yes | A two-state switch that maps each state to a value. |
| [SwitchNode](../nodes/input/switch.md) | Yes | Tabs and segmented controls: one state out of several. |
| [SelectorNode](../nodes/input/selector.md) | Yes | A dropdown list. |

## Data

| Component | Abstract | Use it for |
| --- | --- | --- |
| [ChartNode](../nodes/data/chart.md) | Yes | Line, bar and area charts over labeled values. |
| [RadarChartNode](../nodes/data/radar-chart.md) | Yes | Radar (spider) charts. |

## Not finding what you need?

- Combine nodes: a button is a `RectNode` with a `TextNode` child and an `onClick` callback.
- Change how any node looks with [effects](../styling/effects.md): rounded corners, borders, blur, masks, transforms.
- Draw anything yourself in a [custom node](../nodes/custom-nodes.md).