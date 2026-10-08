# Component Catalog

Every node JOID ships, grouped by purpose, with what to use it for. You have used several of them in the Tutorial; this section describes each one in detail, starting with this catalog and with [Building a UI Kit](ui-kit.md), which draws the interactive ones in your own style.

![A grid of cards, one per component: rectangle, circle, text, image, progress bar, flex, grid, scroll area, chart, text field, checkbox, toggle, switch, slider and selector](../images/components-catalog.png "The components drawn with the neutral demo kit; your kit gives them your own look.")

JOID is design-neutral: no component imposes a look. The display and layout nodes draw plain shapes, text and images that you color and style. The input and data components marked abstract handle the state, the input and the values, and leave the drawing to a small subclass you write once. Those subclasses are your UI kit: your screens use them with the same code whatever kit draws them, so another kit gives the same screens a different design. [Building a UI Kit](ui-kit.md) shows how; each component page starts with a subclass you can copy.

## What every component shares

Every component is a node, so everything [Nodes and the Node Tree](../concepts/nodes.md) teaches applies to it: a factory, `attach`, `body`, the chaining rule, `visible`, `zindex`, the `on...` callbacks of [Input and Callbacks](../concepts/input.md), the effects of [Styling and Effects](../concepts/styling.md), and setters that take a value or follow a signal ([Signals and Reactivity](../concepts/signals.md)). The component pages only describe what each node adds.

Positions and sizes are units of the 1920×1080 virtual canvas, fitted to the window without stretching; wider or taller windows show extra canvas around it.

![The 1920×1080 canvas fitted into a 16:9, a 21:9 and a 4:3 window; the extra visible area is hatched](../images/diagram-canvas.png "One canvas, fitted into every window")

See [The Virtual Canvas](../concepts/canvas.md).

## Layout

Nodes that group and place other nodes. They draw nothing themselves.

| Component | Use it for |
| --- | --- |
| [ContainerNode](../nodes/layout/container.md) | Grouping children under a common origin to move, hide, clip, scroll, rebuild or load them together. |
| [FlexNode](../nodes/layout/flex.md) | Lists, toolbars and menus: children one after the other in a column or a row. |
| [GridNode](../nodes/layout/grid.md) | Tiles that wrap into rows: inventories, galleries, icon pickers. |
| [ReorderableFlexNode](../nodes/layout/reorderable-flex.md) | A column or row the user reorders by dragging, with locked slots. |
| [Overflow and Scrolling](../nodes/layout/overflow-and-scroll.md) | Clipping or scrolling the children of any node, with the wheel, from code or with a `ScrollbarNode`. |

## Display

Nodes that draw something.

| Component | Use it for |
| --- | --- |
| [RectNode](../nodes/visual/rect.md) | Backgrounds, cards, buttons, separators, click areas. |
| [CircleNode](../nodes/visual/circle.md) | Dots, status indicators, avatar placeholders. |
| [TextNode](../nodes/visual/text.md) | A line, a truncated line or a wrapped paragraph of text. |
| [ResourceNode](../nodes/visual/resource.md) | Images, SVGs and animated images, fitted with STRETCH, CONTAIN or COVER. |
| [ResourcePlayerNode](../nodes/visual/resource-player.md) | Videos and animations with playback control and sound. |
| [ModelNode and ModelViewerNode](../nodes/visual/model.md) | 3D models, still or turned with the mouse. |
| [ProgressNode](../nodes/visual/progress.md) | Loading bars, health bars, gauges. |

## Inputs

Nodes the user edits. Each one keeps its value, calls `onChange` on every change, follows a value or a signal given to its value setter, and binds both ways to a signal with `signal(...)`, as in [Input Controls](../essentials/controls.md).

| Component | Abstract | Use it for |
| --- | --- | --- |
| [TextFieldNode](../nodes/input/text-field.md) | No | A single line of text or a number (`IntegerFieldNode`): names, search boxes, amounts. |
| [MultilineTextFieldNode](../nodes/input/multiline-text-field.md) | No | A text area that wraps and scrolls. |
| [SliderNode](../nodes/input/slider.md) | Yes | Picking a number or a value from an ordered set. |
| [CheckboxNode](../nodes/input/checkbox.md) | Yes | A yes / no choice. |
| [ToggleNode](../nodes/input/toggle.md) | Yes | A two-state switch that maps each state to a value. |
| [SwitchNode](../nodes/input/switch.md) | Yes | Tabs and segmented controls: one state out of several. |
| [SelectorNode](../nodes/input/selector.md) | Yes | A dropdown list. |

## Data

| Component | Abstract | Use it for |
| --- | --- | --- |
| [ChartNode](../nodes/data/chart.md) | Yes | Line charts over labeled values. |
| [RadarChartNode](../nodes/data/radar-chart.md) | Yes | Radar (spider) charts. |

## Not finding what you need

- Combine nodes: a button is a `RectNode` with a `TextNode` child and an `onClick` callback.
- Change how any node looks with [effects](../concepts/styling.md#effects): rounded corners, borders, blur, shadows, masks, transforms.
- Draw anything yourself in a [custom node](../nodes/custom-nodes.md).

## See also

- Next: [Building a UI Kit](ui-kit.md)
- [Nodes and the Node Tree](../concepts/nodes.md)
- [Input Controls](../essentials/controls.md)
- [Node Fundamentals](../nodes/node-fundamentals.md): the complete node API, in the Guides.
- [Custom Nodes](../nodes/custom-nodes.md)