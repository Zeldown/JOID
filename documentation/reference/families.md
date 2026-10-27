## ui
The classes that describe a UI, open and close it, keep its state and its files, run its tasks and shortcuts, and animate it when it opens and closes.
Learn: [UIs](/use/learn/describing-the-ui/uis/), [Transitions](/use/learn/style-and-motion/transitions/), [Stores and properties](/use/learn/state/stores-and-properties/).

## annotations
The annotations that configure a UI, an overlay, a popup and its scale, and the objects that hold those options while the UI runs.
Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui), [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## nodes
The base class `Node` and the nodes that draw shapes, text, images, media, progress bars, charts and 3D models, with their tooltips and their drag settings.
Learn: [Nodes and the tree](/use/learn/describing-the-ui/nodes/), [Position and size](/use/learn/describing-the-ui/position-and-size/), [Images and media](/use/learn/describing-the-ui/media/).

## controls
The nodes that hold a value the user changes (checkboxes, toggles, switches, selectors, sliders, text fields) and the scrollbars, each one a behavior class with a skin class of named parts.
Learn: [Controls](/use/learn/interactivity/controls/), [Text fields](/use/learn/interactivity/text-fields/), [UI kit](/use/learn/going-further/ui-kit/).

## layout
The nodes that place their children (containers, flex rows and columns, grids, reorderable lists) and the settings of position, overflow and alignment.
Learn: [Layout](/use/learn/describing-the-ui/layout/), [Flex layout](/use/learn/describing-the-ui/flex-layout/), [Grid layout](/use/learn/describing-the-ui/grid-layout/).

## events
The events that listeners receive, with the bus, the types and the priorities that order the listeners of a node or a UI.
Learn: [Events](/use/learn/interactivity/events/), [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/).

## signals
The signals that hold a value the interface follows, the typed signals and collections, the derived signals computed from them, and the bindings that tie them to nodes and controls.
Learn: [Signals](/use/learn/interactivity/signals/), [Derived values](/use/learn/state/derived-values/), [Rebuilding the tree](/use/learn/state/rebuilding/), [How signals work](/use/in-depth/how-signals-work/).

## animation
The animator, the tween engine (tweens, timelines, manager) and the easing curves and paths that move a value over time.
Learn: [Animation](/use/learn/style-and-motion/animation/).

## text
The text runs and their styles, the fonts (MSDF, bitmap and glyph fonts of your own) with their loader and sources, and the markups, effects, modifiers and converters that change what a text shows.
Learn: [Text](/use/learn/describing-the-ui/text/), [Fonts](/use/learn/describing-the-ui/fonts/), [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/), [Custom fonts](/use/learn/going-further/fonts/).

## resources
Images, animated images, vector images and videos, the assets they are read from, the formats and decoders that read them, their memory cache, the file cache of JOID and the video natives.
Learn: [Images and media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/), [Where JOID stores files](/use/learn/running/files/).

## drawing
Colors and gradients, the immediate drawing helpers, transformations, framebuffers, the tessellator and Wavefront OBJ models that a custom node draws with.
Learn: [Colors and styling](/use/learn/style-and-motion/styling/), [Drawing](/use/learn/going-further/drawing/), [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/), [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/).

## shaders
The shader programs of JOID and of your own, the shader passes and the pipeline that chains them over an offscreen framebuffer.
Learn: [Shaders](/use/learn/going-further/shaders/).

## input
The keys, the mouse buttons and the cursors, with the classes that follow the keyboard layout and turn the key bindings of an application into keys.
Learn: [Keyboard](/use/learn/interactivity/keyboard/), [Hover and tooltips](/use/learn/interactivity/hover-and-tooltips/#change-the-cursor-over-a-node).

## utilities
The small helpers that the other families share: points and boxes, Bézier curves, sorted and registry lists, lockable values, threads, platform folders, JSON files and pixel buffers.
Learn: [Threads](/use/learn/running/threads/), [Where JOID stores files](/use/learn/running/files/).
