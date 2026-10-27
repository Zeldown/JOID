## warning [JOID] Hot reload is off for …: its classes are not loaded from a folder or a jar file, run it from one of them to reload it

With the dev jar, the UI opened, but its classes come from a place that hot reload cannot watch, such as a class loader of your own.
The UI works; editing its class does not reload it.

Run the application from a folder of classes or from a jar file, as an IDE or Gradle does: [Dev tools](/use/get-started/dev-tools/).

## warning [JOID] The hot reload of … cannot start (…), the UI no longer reloads: check that the folder can be watched

With the dev jar, the watcher of the folder or jar of the UI failed to start, for example because the system limits the number of watched folders.
The UI works without hot reload; the reason is in the parentheses.

Check that the folder exists and can be watched, or raise the limit of watched files of the system: [Reload a UI while you work](/use/get-started/dev-tools/#reload-a-ui-while-you-work).

## warning [JOID] A hot reload watcher cannot stop (…), it stops with the JVM

With the dev jar, the last UI watched from a folder closed, and its watcher failed to stop.
Nothing else is affected: the watcher stops when the JVM exits.

No fix is needed; the reason is in the parentheses: [Reload a UI while you work](/use/get-started/dev-tools/#reload-a-ui-while-you-work).

## warning [JOID] A JOID.onReady callback failed (…), the next callbacks still run: catch the exception inside the callback to handle it

A listener given to [[JOID#onReady]] threw a `RuntimeException`; JOID runs the next listeners anyway.

Catch the exception inside the listener, or fix the code it names: [Run code once JOID is ready](/use/learn/running/backends/#run-code-once-joid-is-ready).

## warning [JOID] …

A warning whose whole text is the message of an exception, printed with its stack trace: a reload that failed on a rule of JOID, a draw that failed, a failed install of the video natives, or an error of the snapshot comparison.
Each of these messages has its own entry on this page.

Read the message and look it up on this page.

## warning [JOID] The property … of … cannot be read into its field … (…), the field keeps its default value

A field marked [[UIProperty]] has a saved value that does not fit its type any more, usually because you changed the type of the field.
The field keeps the value that the UI gives it.

Keep the type of a remembered field, or give it another name: [Remember a field of a UI](/use/learn/state/stores-and-properties/#remember-a-field-of-a-ui).

## warning [JOID] The field … of … cannot be saved as the property … (…), the property is not saved

When the UI closed, the value of a field marked [[UIProperty]] could not be turned into JSON, for example because it refers to itself.
The previous saved value stays on disk.

Remember a value that JSON can hold: numbers, strings, booleans, lists, maps and plain objects of them.
See [Remember a field of a UI](/use/learn/state/stores-and-properties/#remember-a-field-of-a-ui).

## warning [JOID] The store … cannot be restored from … (…), it starts from init(): catch the exception inside fromJson() to handle it

The `fromJson()` of a [[UIStore]] threw while it read the saved file; the store starts from its `init()` values.

Make `fromJson()` accept the files that older versions of your store wrote: [Keep a store on disk](/use/learn/state/stores-and-properties/#keep-a-store-on-disk).

## warning [JOID] The destroy of the store … failed (…), it is destroyed anyway: catch the exception inside destroy() to handle it

The `destroy()` of a [[UIStore]] threw when its scope ended; JOID forgets the store anyway.

Catch the exception inside `destroy()`, or fix the code it names: [Write a store](/use/learn/state/stores-and-properties/#write-a-store).

## warning [JOID] The store … cannot be saved (…), the other stores are saved anyway: catch the exception inside toJson() to handle it

The `toJson()` of a [[UIStore]] threw while JOID saved the stores, at the end of their scope or when the program ended; the previous file stays on disk.

Catch the exception inside `toJson()`, or fix the code it names: [Keep a store on disk](/use/learn/state/stores-and-properties/#keep-a-store-on-disk).

## warning [JOID] The reload of … failed (…), fix init() then reload it again

With the dev jar, the reload of the UI (Ctrl + R, or a hot reload after an edit) threw in `init()`, and the UI shows what the failed build left.

Fix the error named in the parentheses, then reload again: [Dev tools](/use/get-started/dev-tools/).

## warning [JOID] … took …ms to draw a frame, more than the 16.66ms of 60 fps: move the heavy work out of draw() or spread it over several frames

With the dev jar and the profiler of the UI turned on in [[UIDataDebug]], one frame of the UI took longer than a frame at 60 fps.

Move loading and computing out of the draw, onto a worker thread or into several frames: [Performance](/use/in-depth/performance/#find-the-slow-part).

## warning [JOID] The … of … failed (…), the next inputs still run: catch the exception inside the handler to handle it

A handler of the UI threw while JOID delivered an input or the update of a frame: an `update`, a press, a release, a drag, a scroll, a character or a key.
JOID prints it once per kind of input and of exception, and the next inputs still run.

Catch the exception inside the handler, or fix the code it names: [Events](/use/learn/interactivity/events/).

## warning [JOID] No key resolver knows the shortcut binding … of type … yet, the shortcut fires only once an IKeyResolver that knows it is registered: pass a Key or register an IKeyResolver with KeyResolver.register(...)

A [[Shortcut]] was created with a binding that is not a [[Key]], and no registered [[IKeyResolver]] supports it yet.
The shortcut stays silent until a resolver that knows the binding is registered.

Pass a `Key`, or register the resolver of your key bindings before you create the shortcut: [Use the key bindings of your application](/use/learn/interactivity/keyboard/#use-the-key-bindings-of-your-application).

## UncheckedIOException …

An `IOException` wrapped to cross a method that cannot throw it: reading a shader source, watching a folder for hot reload, or reading and writing the files of the testkit.
The message is the one of the cause.

Read the cause: a missing file, a folder that cannot be written, or a damaged file.

## IllegalStateException No IUIBridge can open …: register one whose canHandle accepts it

[[JOID#open]] found no UI bridge whose `canHandle` accepts the UI: no backend is registered yet, or every UI bridge refuses the UI.

Register the backend before you open a UI, or open it from [[JOID#onReady]]; with UI bridges of your own, register one that accepts the UI.
See [Opening a UI throws No IUIBridge can open](/use/troubleshooting/#opening-a-ui-throws-no-iuibridge-can-open).

## IllegalStateException The backend was built for JOID ….x, but JOID … is loaded: update the backend

[[Backend#register]] compares the major version that the backend was built for with the major version of the JOID core on the classpath, and they differ.

Use the backend jar of the same release as the core jar: [Choosing a backend](/use/learn/running/backends/).

## IllegalStateException The dev mode needs the dev classes of JOID, use its dev jar

[[JOIDEnv#devMode]] was asked to turn the dev mode on with the prod jar, which holds no dev tools.

Put the dev jar on the classpath to use the dev mode: [Dev tools](/use/get-started/dev-tools/).

## IllegalStateException The store … has no @UIStoreData annotation, add it to the class

A [[UIStore]] class without [[UIStoreData]] was used; JOID cannot know its scope.

Annotate the class with `@UIStoreData`: [Write a store](/use/learn/state/stores-and-properties/#write-a-store).

## IllegalStateException The UI … is not open, only an open UI can be renewed: open it before renew()

[[UI#renew]] replaces an open UI with a fresh instance, and this UI is not open in any bridge.

Open the UI first, or create a new instance and open it: [Rebuild an open UI](/use/learn/describing-the-ui/uis/#rebuild-an-open-ui).

## IllegalStateException The UI … has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead

[[UI#renew]] creates the fresh instance with the constructor without argument of the class, and the class has none.

Add a constructor without argument, of any visibility, or reload the UI instead: [Rebuild an open UI](/use/learn/describing-the-ui/uis/#rebuild-an-open-ui).

## IllegalStateException The UI … cannot be renewed: its constructor without argument failed (…), fix the constructor from this error

[[UI#renew]] called the constructor without argument of the class, and it threw; the open UI stays as it was.

Fix the constructor from the error in the parentheses: [UIs](/use/learn/describing-the-ui/uis/).

## IllegalStateException The UI … cannot be a popup and an overlay at the same time, keep only one of @UIDataPopup and @UIDataOverlay

The UI is annotated with an active [[UIDataPopup]] and an active [[UIDataOverlay]]; a UI is one of the two at most.

Keep one annotation: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## IllegalStateException The class … has no public method …(...) for the annotation value …, add it

An object that changes a UI setting while the UI runs was applied to a target that lacks the public method of one of its values.

Add the public method that the message names, with the parameter type of the value: [Configure a UI](/use/learn/describing-the-ui/uis/#configure-a-ui).

## IllegalStateException The method … of … failed for an annotation value (…)

The method that applies an annotation value threw; the cause is in the parentheses.

Fix the method from that cause: [Configure a UI](/use/learn/describing-the-ui/uis/#configure-a-ui).

## warning [JOID] The dev panel cannot read the field … of … (…), the panel leaves it out

With the dev jar, the inspector could not read one field of the selected node, often a field that the module system hides.
The panel shows the other fields.

No fix is needed; the reason is in the parentheses: [Inspect a UI](/use/get-started/dev-tools/#inspect-a-ui).

## warning [JOID] The update of … failed (…), the next updates still run: catch the exception inside update() to handle it

The `update()` of a node threw during a frame; JOID prints it once per node and kind of exception, and the other nodes still update.

Catch the exception inside `update()`, or fix the code it names: [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/).

## warning [JOID] The detach of … failed (…), the node is detached anyway: catch the exception inside detach() to handle it

The `detach()` of a node, or one of its detach listeners, threw; JOID detaches the node anyway.

Catch the exception inside `detach()`, or fix the code it names: [Release what a node started](/use/learn/describing-the-ui/node-lifecycle/#release-what-a-node-started).

## warning [JOID] A watch of … cannot apply its property when its signal changes (…), the remaining properties of this watch still apply: check the value the signal gives or the body it replays

A property given to `watch(...)` threw when its signal changed, for example on a value the body does not expect.
The other properties of the watch still apply.

Check the value that the signal gives and the code of the property: [Choose what a change does](/use/learn/state/rebuilding/#choose-what-a-change-does).

## IllegalStateException The … has no X axis to hold the data …, call xAxis(...) before data(...)

`data(...)` was called on a [[ChartNode]] whose X axis is not set yet.

Call `xAxis(...)` first: [Draw a line chart](/use/learn/going-further/charts/#draw-a-line-chart).

## IllegalStateException The … has no X axis to remove the data … from, call xAxis(...) before removeData(...)

`removeData(...)` was called on a [[ChartNode]] whose X axis is not set yet.

Call `xAxis(...)` first: [Update the data](/use/learn/going-further/charts/#update-the-data).

## IllegalArgumentException The min … of the zoom range is above its max …, pass a min of at most …

`zoomRange(min, max)` of a [[ModelViewerNode]] received, or its suppliers returned, a minimum above the maximum.

Pass a minimum lower than or equal to the maximum: [Let the user rotate and zoom](/use/learn/going-further/3d-models/#let-the-user-rotate-and-zoom).

## IllegalArgumentException The min … of the rotation yaw range is above its max …, pass a min of at most …

`rotationYawRange(min, max)` of a [[ModelViewerNode]] received, or its suppliers returned, a minimum above the maximum.

Pass a minimum lower than or equal to the maximum: [Let the user rotate and zoom](/use/learn/going-further/3d-models/#let-the-user-rotate-and-zoom).

## IllegalArgumentException The min … of the rotation pitch range is above its max …, pass a min of at most …

`rotationPitchRange(min, max)` of a [[ModelViewerNode]] received, or its suppliers returned, a minimum above the maximum.

Pass a minimum lower than or equal to the maximum: [Let the user rotate and zoom](/use/learn/going-further/3d-models/#let-the-user-rotate-and-zoom).

## IllegalStateException The resource … of this ResourcePlayerNode has no playback: give it a video or an animated image, or show a still image with a ResourceNode

`play()`, `stop()`, `pause()`, `restart()` or `seek(...)` was called on a [[ResourcePlayerNode]] whose resource is a still image, which has nothing to play.

Give the node a video or an animated image, or show the image with a [[ResourceNode]]: [Play animated images and videos](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## IllegalStateException The … has no …, …

A node of your own declared a setting as required with `require(...)`, and the node was loaded without it.
The end of the message is the advice that the node gives.

Set what the message names before the node is attached: [Give the node a property](/use/learn/going-further/custom-nodes/#give-the-node-a-property).

## IllegalStateException The node … cannot be copied: its constructor … failed (…)

`copy()`, or a drag with `DraggableType.COPY`, created the copy with a constructor of the class, and that constructor threw.

Fix the constructor from the error in the parentheses: [Make your node duplicable](/use/learn/going-further/custom-nodes/#make-your-node-duplicable).

## IllegalStateException The node … cannot be copied: declare a constructor (x, y, width, height) or override instantiate()

`copy()`, or a drag with `DraggableType.COPY`, found no constructor `(x, y, width, height)`, `(x, y)` or without argument in the class of the node.

Declare one of these constructors, of any visibility, or override `instantiate()`: [Make your node duplicable](/use/learn/going-further/custom-nodes/#make-your-node-duplicable).

## IllegalStateException The node … has no DraggableProperty, call draggable(...) first

`startDragging()` was called on a node that is not draggable.

Give the node a [[DraggableProperty]] with `draggable(...)` first: [Make a node draggable](/use/learn/interactivity/drag-and-drop/#make-a-node-draggable).

## IllegalStateException The node … has a disabled DraggableProperty, enable it with enabled(...) first

`startDragging()` was called while the [[DraggableProperty]] of the node is disabled.

Enable the property, or check it before you start the drag: [Make a node draggable](/use/learn/interactivity/drag-and-drop/#make-a-node-draggable).

## IllegalStateException The node … is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()

A drag kept inside the parent started on a node attached to the UI itself, which has no parent node to bound it.

Attach the node to a node, or pick another area, such as `DraggableProperty.ui()`: [Choose where the node may go](/use/learn/interactivity/drag-and-drop/#choose-where-the-node-may-go).

## IllegalStateException The … is dragged with DraggableType.MOVE but its … is computed (a lambda or a signal that cannot be written), so a drag cannot move it: give …(...) a value or a writable Signal, or drag a copy with DraggableType.COPY

A drag that moves the node needs to write its position, and `x` or `y` follows a lambda or a derived signal that cannot be written.

Give the position a value or a writable [[Signal]], or drag a copy: [Leave the original in place](/use/learn/interactivity/drag-and-drop/#leave-the-original-in-place).

## IllegalStateException The node … cannot be copied: its field … cannot be set (…), mark the field transient to leave it out of copy()

`copy()` copies every field of the node by reflection, and one field refused the value, for example a final field that the module system protects.

Mark the field `transient` to leave it out of the copy, and set it in the constructor: [Make your node duplicable](/use/learn/going-further/custom-nodes/#make-your-node-duplicable).

## IllegalStateException A free draggable area has no bounds, check getAreaType() before calling getBounds() or pick a bounded area such as DraggableProperty.parent()

`getBounds(...)` was called on a [[DraggableProperty]] whose area is free, which bounds nothing.

Check `getAreaType()` first, or use a bounded area: [Choose where the node may go](/use/learn/interactivity/drag-and-drop/#choose-where-the-node-may-go).

## IllegalStateException The … has no values, call values(...) before getValue()

`getValue()` was called on a choice control, a switch or a selector, before it received its options.

Give the control its options with `values(...)` first: [Choose one of a few options](/use/learn/interactivity/controls/#choose-one-of-a-few-options).

## IllegalArgumentException The … needs at least one value, pass them to values(...)

`values(...)` of a choice control received an empty list.

Pass at least one option: [Choose one of a few options](/use/learn/interactivity/controls/#choose-one-of-a-few-options).

## IllegalStateException The value … has no events to listen to, declare them with events(...) first

`before(...)` or `on(...)` of an [[InteractiveValue]] received a listener, but the value has no events.

Declare the two events of the value with `events(...)` before: [Hold a value that the user changes](/use/learn/going-further/custom-nodes/#hold-a-value-that-the-user-changes).

## IllegalArgumentException The value … is not an option of the … …, pass one of them or add it with values(...)

`value(...)` or `change(...)` of a choice control received a value that is not among its options.

Pass one of the options, or add the value with `values(...)` first: [Choose one of a few options](/use/learn/interactivity/controls/#choose-one-of-a-few-options).

## IllegalArgumentException The index … is out of the values … of the …, pass an index from 0 to …

`valueIndex(...)` of a choice control received an index outside its options.

Pass an index from `0` to the number of options minus one: [Choose one of a few options](/use/learn/interactivity/controls/#choose-one-of-a-few-options).

## IllegalArgumentException The step … of the IntegerFieldNode must be positive, pass a step of 1 or more

`step(...)` of an [[IntegerFieldNode]] received, or its supplier returned, `0` or less.

Pass a step of `1` or more: [Ask for a number](/use/learn/interactivity/text-fields/#ask-for-a-number).

## IllegalArgumentException The step … of the slider range must be positive, pass a step above 0

`range(min, max, step)` of a [[DoubleSliderNode]] received a step of `0D` or less.

Pass a step above `0D`: [Pick a value on a track](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## IllegalArgumentException The min … of the slider range is above its max …, pass a min of at most …

`range(min, max, step)` of a slider received a minimum above its maximum.

Pass a minimum lower than or equal to the maximum: [Pick a value on a track](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## IllegalArgumentException The slider range from … to … by … holds more than … values, pass a narrower range or a larger step

`range(...)` of an [[IntegerSliderNode]] or a [[DoubleSliderNode]] received a range of more values than `Integer.MAX_VALUE`, such as `range(0, Integer.MAX_VALUE)`.

Pass a narrower range or a larger step: [Pick a value on a track](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## IndexOutOfBoundsException The index … is out of the … values of the slider range

`get(index)` of the `getValueList()` of an [[IntegerSliderNode]] or a [[DoubleSliderNode]] received an index below `0` or past the last option.

Pass an index from `0` to `size()` minus one: [Pick a value on a track](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## IllegalArgumentException The step … of the slider range must be positive, pass a step of 1 or more

`range(min, max, step)` of an [[IntegerSliderNode]] received a step of `0` or less.

Pass a step of `1` or more: [Pick a value on a track](/use/learn/interactivity/controls/#pick-a-value-on-a-track).

## warning [JOID] …(…) on … is brought back to 0: the scroll offsets are negative, from 0 down to minus the overflow, call …(…) instead

In dev mode, `scrollX(...)`, `scrollY(...)`, `scrollTo(...)` or a bound signal gave a node a positive scroll offset; the node scrolls to `0` instead.

Pass the negative offset that the message names: [Scroll from your code](/use/learn/describing-the-ui/layout/#scroll-from-your-code).

## IllegalArgumentException The node … is not a child of this …, attach it to the flex first

`getChildIndex(...)` of a [[ReorderableFlexNode]] received a node that is not one of its children.

Pass a child of the list: [Let the user reorder a list](/use/learn/interactivity/reorderable-lists/#let-the-user-reorder-a-list).

## warning [JOID] The … listener of … failed (…), the other listeners still run: catch the exception inside the listener to handle it

A listener of an event threw; JOID prints it once per listener while the failure stays the same, and the other listeners still run.

Catch the exception inside the listener, or fix the code it names: [Events](/use/learn/interactivity/events/).

## IllegalStateException A MONITOR listener only reads …, it cannot call …(): listen with another priority to … it

A listener registered with the `MONITOR` priority tried to change the event, by cancelling or consuming it.
A monitor listener runs last and sees the final result only.

Register the listener with another priority to change the event: [Order the listeners of one event](/use/learn/interactivity/events/#order-the-listeners-of-one-event).

## warning [JOID] A Signal.from(...) of … reads no signal, so its value never changes: read a signal with get() inside it, or pass a lambda to the setter to read the value at every frame

With the dev jar, a derived value computed by [[Signal#from]] read no signal the first time it computed, so it never computes again.

Read the signals with `get()` inside the function, or give the setter a lambda, which JOID reads at every frame: [Combine several signals](/use/learn/state/derived-values/#combine-several-signals).

## warning [JOID] … but cannot follow it: …. The value stays "…". …

With the dev jar, a setter received an expression that reads signals, and JOID cannot replay its bytecode to find them; the message names the file, the line, the setter and the signals.
The property keeps the value of the first read; the end of the message gives the advice for this case.

Follow the advice, or give the setter a lambda, which JOID reads at every frame: [What JOID follows](/use/learn/interactivity/signals/#what-joid-follows).

## warning [JOID] The replay of a signal read failed on an internal error, the value "…" stays fixed

With the dev jar, the replay of an expression given to a setter failed on an error of JOID itself; the stack trace follows.
The property keeps the value of the first read.

Give the setter a lambda instead, and report the stack trace to JOID: [How signals track reads](/use/in-depth/how-signals-work/).

## warning [JOID] The … of … cannot take its new value (…), it keeps the previous one: check the value its signal gives

A property bound to a signal refused the new value of the signal, for example a value outside its bounds.

Check the values that the signal gives: [Bind a control to a signal](/use/learn/interactivity/controls/#bind-a-control-to-a-signal).

## warning [JOID] … and the other controls bound to … keep correcting each other: their bounds are incompatible, give them bounds that agree

With the dev jar, several controls bound to one signal kept writing it back, each one to fit its own bounds, and JOID stopped after 16 rounds and set the first value back.

Give the controls bound to one signal bounds that agree: [Keep a value that the bounds refuse](/use/learn/interactivity/controls/#keep-a-value-that-the-bounds-refuse).

## IllegalStateException A ComputedSignal cannot read itself while it computes its value, remove the read of this signal from its function

The function of a derived value read the derived value itself, directly or through another one.

Remove that read from the function: [Combine several signals](/use/learn/state/derived-values/#combine-several-signals).

## IllegalArgumentException The IntegerSignal of value … cannot be divided by 0, check the divisor before calling divide(...)

`divide(0)` was called on an [[IntegerSignal]].

Check the divisor before you divide: [Choose a signal type](/use/learn/interactivity/signals/#choose-a-signal-type).

## IllegalArgumentException The LongSignal of value … cannot be divided by 0, check the divisor before calling divide(...)

`divide(0)` was called on a [[LongSignal]].

Check the divisor before you divide: [Choose a signal type](/use/learn/interactivity/signals/#choose-a-signal-type).

## SignalReplayException …

The replay of an expression given to a setter met something it cannot follow; JOID catches it and prints [`… but cannot follow it`](#but-cannot-follow-value) instead.

See that entry.

## IllegalStateException …

An `IllegalStateException` whose message comes from your code: a refusal given to [[Signal#refuse]], thrown again by `set(...)` or `reset()`, or the failure of the copy of an effect.

Read the message that your code gave.

## IllegalArgumentException The … of … is changed by the user, so it cannot follow …, which cannot be written: pass a writable Signal, or its value as a start value

A control received a derived or read-only signal for its value; the user changes that value, so the control must write it back.

Pass a writable [[Signal]], or the value as a start value and listen to the changes: [Bind a control to a signal](/use/learn/interactivity/controls/#bind-a-control-to-a-signal).

## warning [JOID] The update of TweenAnimator failed (…), the other animators still run: catch the exception inside the onChange listeners of the animator or the getter and setter given to Tween.to(...) to handle it

A [[TweenAnimator]] threw while it advanced in a frame: one of its change listeners, or the getter or setter of one of its tweens.
The other animators still run.

Catch the exception in the listener, the getter or the setter, or fix the code it names: [Animation](/use/learn/style-and-motion/animation/).

## IllegalStateException The animator has no timeline, call sequence(...) first

A method of a [[TweenAnimator]] that works on its timeline, such as `start()`, `delay(...)`, `repeat(...)`, `push(...)` or an `onX(...)` listener method, was called before the animator received a timeline.

Call `sequence(...)` first: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## IllegalStateException A tween or timeline that repeats forever needs a duration or a repeat delay above 0, give it one or a finite repeat count

`start()` was called on a tween or a timeline that repeats forever with a duration and a repeat delay of `0`, which would loop forever within one frame.

Give it a duration or a repeat delay above `0`, or a finite repeat count: [Chain, repeat and replay](/use/learn/style-and-motion/animation/#chain-repeat-and-replay).

## IllegalStateException The repetitions of a tween or timeline cannot change once it is started, call …(...) before start()

`repeat(...)` or `repeatYoyo(...)` was called on a tween or a timeline that is already started.

Set the repetitions before `start()`: [Chain, repeat and replay](/use/learn/style-and-motion/animation/#chain-repeat-and-replay).

## IllegalArgumentException The pushed timeline still has an open beginSequence() or beginParallel(), close each one with end() before pushing it

`push(timeline)` received a timeline in which a `beginSequence()` or `beginParallel()` has no matching `end()`.

Close each group with `end()` before you push the timeline: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## IllegalStateException The timeline has no open beginSequence() or beginParallel() to close, remove this end()

`end()` was called on a timeline that has no open group.

Remove the extra `end()`, or open the group it closes: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## IllegalStateException The timeline still has an open beginSequence() or beginParallel(), close each one with end() before start()

The timeline was started while a `beginSequence()` or `beginParallel()` has no matching `end()`.

Close each group with `end()` before `start()`: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## IllegalArgumentException A timeline cannot hold a child that repeats forever (repeat count …), give it a finite repeat count

A timeline was started with a child that repeats forever, which would make the timeline itself endless.

Give the child a finite repeat count, or repeat the whole timeline instead: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## IllegalStateException A timeline takes no new child once it is built (start() builds it), push its children before start()

A child was added to a timeline, or a group opened or closed, after `start()` built it.

Build the whole timeline before `start()`: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## IllegalArgumentException The duration of a tween cannot be negative, got …: pass 0 or more

A [[Tween]] was created with a negative duration.

Pass a duration of `0` or more, in milliseconds: [Animate values of your own](/use/learn/style-and-motion/animation/#animate-values-of-your-own).

## warning [JOID] The font weight … is not loaded in the family of …, … is drawn instead (loaded: …)

With the dev jar, a text asked for a weight that its font family does not hold, and JOID draws the nearest loaded face; JOID prints it once per weight and family.

Load the face of that weight with the family, or ask for a loaded weight: [Let a style pick a face](/use/learn/describing-the-ui/fonts/#let-a-style-pick-a-face).

## warning [JOID] The font … of a text style is not drawn by …, the text keeps its own font: give the style a font drawn by …

With the dev jar, a style inside a text names a font that another text renderer draws, such as a bitmap font inside an MSDF text.
JOID draws that part with the font of the text itself.

Mix only fonts of the same kind in one text: [Choose MSDF or bitmap](/use/learn/describing-the-ui/fonts/#choose-msdf-or-bitmap).

## warning [JOID] The font shader of … cannot be compiled by …, what it draws is skipped: the error printed by JOID when it was created tells why

With the dev jar, the shader that draws a kind of font did not compile on this graphics context, so its texts are not drawn.

Read the compilation error that JOID printed before this message; on OpenGL, updating the graphics driver often fixes it: [Choose MSDF or bitmap](/use/learn/describing-the-ui/fonts/#choose-msdf-or-bitmap).

## IndexOutOfBoundsException The text has no element at index …, give an index from 0 and lower than its element count (…)

`get(index)` of a [[Text]] received an index outside its elements.

Pass an index from `0` to the element count minus one: [Mix styles on one line](/use/learn/describing-the-ui/text/#mix-styles-on-one-line).

## IllegalArgumentException A bitmap cell needs at least one texel, got the texels …, …, …, …: give a right above the left and a bottom above the top

[[BitmapCell#create]] received a rectangle of texels without area.

Give a right edge above the left one and a bottom edge above the top one: [Write a pixel-art font](/use/learn/going-further/fonts/#write-a-pixel-art-font).

## IllegalArgumentException The bounds of a bitmap cell cannot be empty, got …, …, …, …: give a right above the left and a bottom above the top

`bounds(...)` of a [[BitmapCell]] received bounds without area.

Give a right edge above the left one and a bottom edge above the top one: [Write a pixel-art font](/use/learn/going-further/fonts/#write-a-pixel-art-font).

## IllegalArgumentException The bitmap size … of a font is not positive, pass a size above 0

A [[BitmapFont]] was created with a size of `0` or less.

Pass the height of a glyph cell in texels, above `0`: [Write a pixel-art font](/use/learn/going-further/fonts/#write-a-pixel-art-font).

## IllegalArgumentException A font family needs at least one face, got none: pass its faces to FontFamily.of(...)

[[FontFamily#of]] received no face.

Pass at least one face: [Draw glyphs your own way](/use/learn/going-further/fonts/#draw-glyphs-your-own-way).

## IllegalArgumentException Two faces share the weight …, keep one face per weight and style

[[FontFamily#of]] received two faces with the same weight and the same italic flag.

Keep one face per weight and style, or set the weight of a file: [Set the weight of a font file](/use/learn/going-further/fonts/#set-the-weight-of-a-font-file).

## IllegalArgumentException … cannot draw the font …, it is drawn by …: draw it with info.getFont().getTextRenderer()

Code of your own called a text renderer with a [[TextInfo]] whose font belongs to another renderer.

Draw with the renderer of the font, `info.getFont().getTextRenderer()`: [Draw glyphs your own way](/use/learn/going-further/fonts/#draw-glyphs-your-own-way).

## IllegalArgumentException A font face needs a name, got an empty one: give the face the name of its font

An MSDF face was created with an empty name.

Give the face the name of its font: [Load a font family](/use/learn/describing-the-ui/fonts/#load-a-font-family).

## CompletionException …

The future of [[MsdfFontLoader#load]] completed with an error: the cause, such as an `IOException` of a file that cannot be read or generated, is in the message.

Read the cause, often another entry of this page: [Load a font without blocking](/use/learn/describing-the-ui/fonts/#load-a-font-without-blocking).

## IllegalArgumentException A msdf font needs at least one face, got none: pass the files of its faces

[[MsdfFontLoader#load]] received no file; its future completes with this error.

Pass the files of the faces of the font: [Load a font family](/use/learn/describing-the-ui/fonts/#load-a-font-family).

## IOException The file does not start with JOIDMSDF, it is not a font made by the msdf tool of JOID: generate it with that tool

A [[BinaryMsdfSource]] read a file that is not a `.msdf` atlas of JOID, such as a `.ttf` file given the wrong extension.

Load a `.ttf` or `.otf` file with [[OpenTypeMsdfSource]], or generate the atlas: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).

## IOException The msdf font version … is not supported, only the version … loads: generate it again with the msdf tool of JOID

The `.msdf` atlas was written by a generator of another version of JOID.

Generate the atlas again with the generator of this version: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).

## IOException The msdf atlas of the font cannot be generated in …, check that the font is a valid TrueType or OpenType file and that the cache folder can be written

At the first load of a font file, JOID generates its atlas into the cache folder, and the generation failed: the file is not a font that Java can read, or the folder cannot be written.

Check the font file and the [cache folder](/use/learn/running/files/#find-the-folders); [What happens when a font loads](/use/in-depth/msdf-text/#what-happens-when-a-font-loads) explains the cache.

## warning [JOID] The stream … cannot be closed (…), its content is read anyway: check the stream

A [[StreamAsset]] read its stream, and closing it threw; the content is used anyway.

Check the stream that you gave, for example one already closed by its owner: [Read resources from your own source](/use/learn/going-further/custom-formats/#read-resources-from-your-own-source).

## warning [JOID] The retention of the cache area … cannot be written into … (…), check that the cache folder can be written

JOID could not write the file that records the retention of a cache area; the area works, but its files may be cleaned with the default retention.

Check that the [cache folder](/use/learn/running/files/#find-the-folders) can be written.

## warning [JOID] The cache entry … of the area … is in use, it is left to the next cleaning of the cache

An entry of a cache area could not be deleted, usually because a file of it is open, on Windows for example; JOID marks it as old and deletes it at a later cleaning.

No fix is needed: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## warning [JOID] The video … cannot be reopened (…), check that the file can still be read

A video that plays again after its end, or after a seek, could not open its file again.
The video stops.

Check that the file or the URL can still be read: [Play animated images and videos](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## warning [JOID] The video … cannot seek to …us (…), check that the file can still be read

A seek of a video, or its return to the start, failed in the decoder.

Check that the file can still be read; a damaged file can refuse some positions: [Play animated images and videos](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## warning [JOID] The video … stops decoding (…), check that the file can still be read

The decoder of a playing video threw; the video stops where it was.

Check that the file can still be read, and that it is not damaged: [Play animated images and videos](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## warning [JOID] The resource … cannot be read: …. It is drawn as a magenta and black checkerboard in dev mode, and nothing is drawn in production.

With the dev jar, JOID could not read or decode a resource: the file or the URL does not exist, cannot be read, or holds a format that JOID does not decode.
The cause follows the colon; the [[Resource#onError]] listeners receive it.

Check the path or the URL for a read error, convert the file to a supported format otherwise: [Supported formats](/use/learn/describing-the-ui/media/#supported-formats), [My image shows a checkerboard or nothing](/use/troubleshooting/#my-image-shows-a-checkerboard-or-nothing).

## warning [JOID] The video … plays without sound (…), register an audio bridge and check the audio device

The audio bridge could not create the source of a video with a sound track: the audio device refused, or the bridge failed.
The picture plays without sound.
Without any audio bridge, as with `audio(false)`, a video plays without sound and without this warning.

Check the audio device of the machine, or the `createSource` of your audio bridge: [Control the sound of a video](/use/learn/describing-the-ui/media/#control-the-sound-of-a-video).

## UncheckedIOException The start of … cannot be read

JOID reads the first bytes of an asset to detect its format, and reading them failed.

Check that the file or the URL can be read; the cause follows the message: [Load from the jar, the disk or the web](/use/learn/describing-the-ui/media/#load-from-the-jar-the-disk-or-the-web).

## IllegalArgumentException No asset locator knows the input … of type …, register an IAssetLocator that supports it with AssetLocator.register(...)

`Resource.of(...)` or `Asset.of(...)` received an input that no asset locator supports, such as an object of your own.

Pass a path, a URL, a `File` or a stream, or register a locator: [Read resources from your own source](/use/learn/going-further/custom-formats/#read-resources-from-your-own-source).

## IllegalArgumentException The asset … is not found in the classpath nor as a file, check the path or give a URL with its scheme (https:, file:, jar:)

A path without scheme names neither a resource of the classpath nor a file.

Check the path, relative to the root of the classpath or to the working folder, or give a URL: [Load from the jar, the disk or the web](/use/learn/describing-the-ui/media/#load-from-the-jar-the-disk-or-the-web).

## IllegalArgumentException The retention … of the cache area … is not positive, pass a duration above 0

`retention(...)` of a [[FileCacheArea]] received a duration of zero or less.

Pass a duration above zero: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## IllegalStateException The cache area … is already declared with a retention of …, it cannot be declared again with …: give one of them another name

Two parts of the application declared the same cache area with different retentions.

Give each area its own name: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## IOException The content written into the cache area … cannot be hashed at …, the writer of store must write a file

`store(...)` of a [[FileCacheArea]] names its entry by the hash of the file that the writer wrote, and that file cannot be read.

Write a file at the path that the writer receives: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## IllegalArgumentException The cache … name … is not a plain file name: start with a letter or a digit, then use only letters, digits, '.', '-' and '_'

The name of a cache area or of an entry would not be a safe file name.

Use a name of letters, digits, `.`, `-` and `_`, that starts with a letter or a digit: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## IOException The cache area … cannot be created in …, check that the cache folder can be written

JOID could not create the folder of a cache area.

Check that the [cache folder](/use/learn/running/files/#find-the-folders) exists and can be written, or move it: [Move the folders](/use/learn/running/files/#move-the-folders).

## IOException The writer of … wrote nothing at …, it must create the file or the folder it is given

The writer given to `compute(...)` or `store(...)` of a [[FileCacheArea]] returned without creating its entry.

Create the file or the folder at the path that the writer receives: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## IllegalStateException The file cache cannot name the content of …: this JVM has no SHA-1 digest, run JOID on a standard JVM

The JVM offers no SHA-1, which the file cache uses to name its entries.

Run JOID on a standard JVM: [Keep files of your own in the cache](/use/learn/running/files/#keep-files-of-your-own-in-the-cache).

## IllegalArgumentException The PNG has no IHDR chunk before its image data, the file is damaged: export it again

The animated PNG is damaged.

Export the image again: [Media](/use/learn/describing-the-ui/media/).

## IllegalArgumentException The PNG has no acTL chunk or no frame control, it is not an animated PNG: draw it as a still image or export it as an APNG

A PNG was decoded as an animation but holds a single image.

Show it with a [[ResourceNode]], or export it as an APNG: [Play animated images and videos](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## IllegalArgumentException The file does not start with a RIFF WEBP header, it is not a WebP image: check the file and its extension

A file decoded as an animated WebP is not a WebP.

Check the file and its extension: [Media](/use/learn/describing-the-ui/media/).

## IllegalArgumentException The WebP chunk … runs past the end of the file, the file is damaged: export it again

The WebP file is truncated or damaged.

Export the image again: [Media](/use/learn/describing-the-ui/media/).

## IllegalArgumentException The WebP has no ANMF frame or no VP8X size, it is not an animated WebP: draw it as a still image or export it with its animation

A WebP was decoded as an animation but holds a single image.

Show it with a [[ResourceNode]], or export it with its animation: [Media](/use/learn/describing-the-ui/media/).

## IllegalArgumentException An animation needs at least one frame, got none: check that the file holds its frames

[[ResourceAnimation#of]] received no frame, from a reader of your own or from an empty file.

Give the animation at least one frame: [Build an animation from your own frames](/use/learn/going-further/custom-formats/#build-an-animation-from-your-own-frames).

## UncheckedIOException The animation … cannot be read

Reading the file of an animated image failed; the cause follows the message.
The resource fails and prints [`The resource … cannot be read`](#resource-cannot-read-drawn) with the dev jar.

Check that the file or the URL can be read: [Know when an image is loaded](/use/learn/describing-the-ui/media/#know-when-an-image-is-loaded).

## UncheckedIOException The image … cannot be read

Reading the file of an image failed; the cause follows the message.
The resource fails and prints [`The resource … cannot be read`](#resource-cannot-read-drawn) with the dev jar.

Check that the file or the URL can be read: [Know when an image is loaded](/use/learn/describing-the-ui/media/#know-when-an-image-is-loaded).

## IllegalArgumentException … is not an image that ImageIO can decode, convert it to PNG, JPEG or WebP

The file was read, but no decoder of images recognizes its content.

Convert it to PNG, JPEG or WebP: [Supported formats](/use/learn/describing-the-ui/media/#supported-formats).

## UncheckedIOException The SVG … cannot be read

Reading the file of an SVG image failed; the cause follows the message.

Check that the file or the URL can be read: [Know when an image is loaded](/use/learn/describing-the-ui/media/#know-when-an-image-is-loaded).

## IllegalArgumentException … is not an SVG document that can be parsed

The file was read, but it is not a valid SVG document.

Check the file in an SVG editor and export it again: [Media](/use/learn/describing-the-ui/media/).

## UncheckedIOException The video … cannot be read

Opening the file of a video failed; the cause follows the message.

Check that the file or the URL can be read: [Play animated images and videos](/use/learn/describing-the-ui/media/#play-animated-images-and-videos).

## IllegalStateException The video … cannot be decoded

FFmpeg opened the file but could not decode it; the cause follows the message.

Check that the file is a video that FFmpeg plays: [Supported formats](/use/learn/describing-the-ui/media/#supported-formats).

## IllegalStateException The FFmpeg natives are not installed yet, wait for VideoNatives.install()

A video was opened while the FFmpeg natives were still being installed.

Wait for the future of [[VideoNatives#install]] before you play videos: [Play videos on every machine](/use/learn/describing-the-ui/media/#play-videos-on-every-machine).

## VideoNativesException The FFmpeg natives cannot be installed (…), check the video libraries on the classpath

The install of the FFmpeg natives failed when a video was opened; the cause is in the parentheses, often another FFmpeg entry of this page.

Check the video libraries on the classpath: [Play videos on every machine](/use/learn/describing-the-ui/media/#play-videos-on-every-machine).

## IllegalArgumentException … is a HEIF or AVIF image, which JOID cannot decode, convert it to PNG, JPEG or WebP

The file is a HEIF or AVIF image, which JOID recognizes but does not decode.

Convert it to PNG, JPEG or WebP: [Supported formats](/use/learn/describing-the-ui/media/#supported-formats).

## IllegalArgumentException The borrowed texture … is not a texture of the host, borrow a texture that the host created and has not deleted

`Resource.of(...)` received a texture of the host that names no texture when JOID loads the resource: the number is wrong, the texture was deleted, or it belongs to another graphics context.
The resource fails.

Borrow a texture that exists in the graphics context of JOID: [Show a texture of your application](/use/learn/describing-the-ui/media/#show-a-texture-of-your-application).

## VideoNativesException FFmpeg has no natives for the platform …, play the videos on windows-x86_64, linux-x86_64, macosx-x86_64 or macosx-arm64

The machine runs a system or a processor for which JOID has no FFmpeg natives to download.

Play videos on one of the platforms that the message lists, or put the FFmpeg natives of your platform on the classpath: [Play videos without network access](/use/get-started/installation/#play-videos-without-network-access).

## VideoNativesException The FFmpeg natives of … cannot be written into …, check that the cache folder can be written

The downloaded FFmpeg natives could not be saved into the cache folder.

Check that the [cache folder](/use/learn/running/files/#find-the-folders) can be written.

## VideoNativesException The FFmpeg natives of … cannot be downloaded from …, check the connection and the repository, or put org.bytedeco:ffmpeg:…:… on the classpath

The first video of the machine downloads the FFmpeg natives, and the download failed.

Check the connection, or ship the natives with the application: [Play videos on every machine](/use/learn/describing-the-ui/media/#play-videos-on-every-machine).

## VideoNativesException The FFmpeg natives downloaded from … do not match their SHA-256, check the repository

The downloaded natives differ from the files that JOID expects; JOID refuses them.

Check the repository and the network, then try again: [Play videos without network access](/use/get-started/installation/#play-videos-without-network-access).

## warning [JOID] The file of the OBJ model … cannot be closed (…), the model is loaded anyway: check the stream or the file it was read from

Closing the reader of an OBJ file threw after the model was read; the model is used anyway.

Check the stream or the file it was read from: [Load a model](/use/learn/going-further/3d-models/#load-a-model).

## NumberFormatException The color … cannot be decoded, write it as #RRGGBB, #RRGGBBAA, rgb(r, g, b), rgba(r, g, b, a) or gradient(from, to)

[[Color#decode]] received a text in none of its formats.

Write the color in one of the formats of the message: [Create a color](/use/learn/style-and-motion/styling/#create-a-color).

## IllegalArgumentException The dash pattern … is not positive, pass a length above 0

`drawDashedLine(...)` received a dash length of `0` or less.

Pass a dash length above `0`, in design pixels: [Draw lines and curves](/use/learn/going-further/drawing/#draw-lines-and-curves).

## IllegalArgumentException An ObjFace has 3 vertices, got …: split the polygon into triangles (ObjModel does it for OBJ files)

[[ObjFace#of]] received a face that is not a triangle.

Split the polygon into triangles; [[ObjModel]] does it for the faces of an OBJ file: [Load a model](/use/learn/going-further/3d-models/#load-a-model).

## IllegalArgumentException An ObjFace has 3 texture coordinates or none, got …

[[ObjFace#of]] received a number of texture coordinates other than 0 or 3.

Give one texture coordinate per vertex, or none: [Load a model](/use/learn/going-further/3d-models/#load-a-model).

## IllegalArgumentException An ObjFace has 3 vertex normals or none, got …

[[ObjFace#of]] received a number of normals other than 0 or 3.

Give one normal per vertex, or none: [Load a model](/use/learn/going-further/3d-models/#load-a-model).

## UncheckedIOException The OBJ model … cannot be read (…), check that the file exists and can be read

Reading the OBJ file failed; the cause is in the parentheses.

Check that the file exists and can be read: [Load a model](/use/learn/going-further/3d-models/#load-a-model).

## IllegalArgumentException The OBJ model … cannot be read: its line … "…" …, fix this line in the model file

A line of the OBJ file is malformed; the message gives its number, its text and the problem.

Fix that line, or export the model again: [Know what JOID reads in a .obj file](/use/learn/going-further/3d-models/#know-what-joid-reads-in-a-obj-file).

## IllegalStateException The framebuffer of …x… is empty, call fill(...) before draw(...)

`draw(...)` of a [[FrameBuffer]] was called before anything was drawn into it.

Call `fill(...)` first: [Draw once, reuse every frame](/use/learn/going-further/framebuffers-and-models/#draw-once-reuse-every-frame).

## IllegalStateException The Tessellator is already tessellating a … shape, call draw() to end it before start(…)

`start(...)` of the [[Tessellator]] was called while another shape was not drawn yet.

End the previous shape with `draw()`: [Build your own geometry](/use/learn/going-further/framebuffers-and-models/#build-your-own-geometry).

## IllegalStateException The Tessellator has no shape to draw, call start(DrawMode) before draw()

`draw()` of the [[Tessellator]] was called without `start(...)`.

Call `start(DrawMode)`, add the vertices, then `draw()`: [Build your own geometry](/use/learn/going-further/framebuffers-and-models/#build-your-own-geometry).

## IllegalStateException The Tessellator is not tessellating, call start(DrawMode) before giving it vertices

A vertex, a color, texture coordinates or a normal was given to the [[Tessellator]] outside `start(...)` and `draw()`.

Call `start(DrawMode)` first: [Build your own geometry](/use/learn/going-further/framebuffers-and-models/#build-your-own-geometry).

## warning [JOID] The shader … cannot be loaded (…), what it draws is skipped: check the paths of its vertex and fragment sources

`load(...)` of a [[ShaderProgram]] could not read one of its two sources; the shader never draws.

Check the paths of the vertex and fragment sources: [Write a shader](/use/learn/going-further/shaders/#write-a-shader).

## warning [JOID] The shader … cannot be created (…), what it draws is skipped: fix its vertex and fragment sources from this error

The first use of a [[ShaderProgram]] asked the render bridge to create it, and JOID refused the sources: a rule of JOID GLSL is broken, or a feature is missing on this context.
The shader never draws.

Fix the sources from the error, often another entry of this page: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## warning [JOID] The shader … is unavailable, what it draws is skipped

With the dev jar, `canDraw()` was called on a [[ShaderProgram]] that failed to load, to be created or to compile; JOID prints it once per shader.

Fix the error printed before it; for a shader of your own, test `isAvailable()` to draw a fallback: [Write a shader](/use/learn/going-further/shaders/#write-a-shader).

## IllegalArgumentException No key resolver knows the binding … of type …, pass a Key or register an IKeyResolver with KeyResolver.register(...)

[[KeyResolver#resolve]] received a key binding that no registered [[IKeyResolver]] supports.

Pass a [[Key]], or register the resolver of your key bindings: [Use the key bindings of your application](/use/learn/interactivity/keyboard/#use-the-key-bindings-of-your-application).

## warning [JOID] The file … cannot be read (…), it is left untouched and …

The file of a store or of the remembered fields of a UI exists but cannot be read, for example for lack of permission.
The end of the message says what starts from its defaults.

Check the permissions of the [data folder](/use/learn/running/files/#find-the-folders).

## warning [JOID] The file … cannot be saved (…), check that the data folder can be written

JOID could not write the file of a store or of the remembered fields of a UI; the previous file stays as it was.

Check that the [data folder](/use/learn/running/files/#find-the-folders) can be written, or move it: [Move the folders](/use/learn/running/files/#move-the-folders).

## warning [JOID] The file … cannot be read (…), it is kept as … and …

The file of a store or of the remembered fields of a UI holds no valid JSON, or its store refused it.
JOID renames it with the `.corrupt` suffix, so that nothing overwrites it, and starts from the defaults.

Open the `.corrupt` file to recover what you need, then delete it: [Keep a store on disk](/use/learn/state/stores-and-properties/#keep-a-store-on-disk).

## warning [JOID] The file … cannot be read (…) nor renamed to … (…), it is left untouched and …

The file of a store or of the remembered fields of a UI holds no valid JSON, and JOID could not rename it with the `.corrupt` suffix either.
JOID starts from the defaults; the next save overwrites the file.

Check the permissions of the [data folder](/use/learn/running/files/#find-the-folders).

## NullPointerException … is marked non-null but is null

A parameter of JOID that cannot be `null` received `null`; the message names the parameter.

Pass a value; the reference page of the method says which parameters accept `null`.

## IllegalStateException …, use …(...) to get a modified copy

A setter was called on a locked value, such as a color of a palette locked with `lock()`, which never changes.

Call the method that the message names to get a modified copy: [Keep a palette safe](/use/learn/style-and-motion/styling/#keep-a-palette-safe).

## warning [JOID] … refused to close: a beforeClose listener cancelled the event, so … is not opened: do not cancel it to let the UI close

With the dev jar, opening a screen asks the open screen to close, and a listener of its `beforeClose` event cancelled it, so the new screen does not open.

Let the screen close, or open the new UI as a popup or an overlay: [Replace the current screen](/use/learn/describing-the-ui/uis/#replace-the-current-screen).

## warning [JOID] … cannot draw a frame and skips the rest of it (…), fix the draw code from this error

A UI of the bridge threw while it drew; the bridge skips the rest of its frame, puts the matrix and state stacks back, and prints it once per kind of exception.

Fix the draw code from the error and its stack trace.

## warning [JOID] … draws no tooltip, override drawHover(UI, Object, double, double) to draw them

With the dev jar, the pointer rests on a node that has a tooltip, and the UI bridge extends [[UIBridge]] without overriding `drawHover(...)`; JOID prints it once per bridge.

Override `drawHover(...)` in the bridge, or extend [[WindowUIBridge]]: [UI bridge](/build/bridges/ui/).

## warning [JOID] … draws no tooltip, set its hoverInfo(TextInfo) to draw them

With the dev jar, the pointer rests on a node that has a tooltip, and the window bridge has no text style for tooltips; JOID prints it once per bridge.

Give the bridge a style with [[WindowUIBridge#hoverInfo]]: [Style the window bridge](/use/learn/running/backends/#style-the-window-bridge).

## IllegalStateException … is already registered, call register() only once

[[Backend#register]] was called a second time on the same backend.

Register each backend once, at startup: [Start a backend](/use/learn/running/backends/#start-a-backend).

## IllegalStateException ….draw() needs an open frame and the render bridge has none: call render.beginFrame() before and render.endFrame() after, or use BridgeHandler.UI.frame()

`draw()` of a UI bridge was called outside a frame of the render bridge.

Call [[Backend#frame]] or [[UIBridgeRegistry#frame]], which open the frame, or call `beginFrame()` and `endFrame()` of the render bridge around the draw: [UI bridge](/build/bridges/ui/#guarantees).

## warning [JOID] A … shader uses …, which needs …: the OpenGL 2.1 contexts (GLSL 1.20) that JOID supports refuse it, keep to GLSL 1.20 to draw on every backend

With the dev jar, a shader uses a GLSL feature that OpenGL 2.1 contexts refuse; it draws on this machine, but not on those contexts.

Rewrite the shader without the feature that the message names: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## warning [JOID] The FrameThreadBridge was created on the thread … but its frames run on the thread …: create it on the render thread, or JOID takes the render thread for another thread

The thread that runs the frames is not the thread that created the [[FrameThreadBridge]], which is the thread that registered the backend; JOID then takes another thread for the render thread.

Register the backend on the thread that runs the frames, or return a thread bridge of your own: [Thread bridge](/build/bridges/thread/).

## warning [JOID] A task sent to the render thread from another thread failed (…), the next tasks still run: catch the exception inside the task to handle it

A task that another thread sent to the render thread, through [[ThreadUtils#runOnRenderThread]] or `JOID.open`, threw at the start of a frame; JOID prints it once per kind of failure, and the next tasks still run.

Catch the exception inside the task, or fix the code it names: [Threads](/use/learn/running/threads/).

## IllegalStateException No … bridge registered, call BridgeHandler.….register before using JOID

JOID needed a bridge, the render or the window bridge for example, and its registry is empty: the backend is not registered yet.

Register the backend before you use JOID, or run the code from [[JOID#onReady]]: [Start a backend](/use/learn/running/backends/#start-a-backend).
A backend author registers the bridge in its hook: [The bridges](/build/bridges/#who-registers-what-and-when).

## IllegalStateException The JOID frame has already begun, call endFrame() first

`beginFrame()` of the render bridge was called twice without `endFrame()`, for example by a loop that calls [[Backend#frame]] inside a frame of its own.

Pair each `beginFrame()` with one `endFrame()`; `Backend.frame()` opens and closes its own frame: [Render bridge](/build/bridges/render/#provided).

## IllegalStateException … draws only inside a frame and no frame has begun: draw between beginFrame() and endFrame(), from the draw of a UI or of the backend loop

Something drew or cleared outside a frame of the render bridge, such as a draw from an input handler, from a worker thread, or before the loop.

Draw from the draw of a node or of a UI, which runs inside the frame: [Frame lifecycle](/use/in-depth/frame-lifecycle/).

## IllegalArgumentException The shader declares no sampler …, declare it in the shader source or fix its name (samplers: …)

`sampler(name, ...)` of a shader received a name that its sources do not declare; the message lists the declared samplers.

Fix the name, or declare the sampler in the source: [Set uniforms and textures](/use/learn/going-further/shaders/#set-uniforms-and-textures).

## UnsupportedOperationException A borrowed texture belongs to its host: JOID reads it but never allocates it, create a texture with IRenderBridge.createTexture() to draw your own pixels

`allocate(...)` was called on a texture borrowed from the host.

Create a texture of JOID to hold pixels of your own: [Build your own geometry](/use/learn/going-further/framebuffers-and-models/#build-your-own-geometry).

## UnsupportedOperationException A borrowed texture belongs to its host: JOID reads it but never writes it, create a texture with IRenderBridge.createTexture() to draw your own pixels

`upload(...)` was called on a texture borrowed from the host.

Write the pixels with the API of the host, or create a texture of JOID: [Lend a texture of the host](/build/resource-resolvers/#lend-a-texture-of-the-host).

## IllegalStateException The texture is deleted, create a new one with IRenderBridge.createTexture()

`allocate(...)` or `upload(...)` was called on a deleted texture.

Create a new texture: [Textures and samplers](/build/pipeline/#textures-and-samplers).

## IllegalStateException The texture is not allocated, call allocate(width, height) before upload(pixels)

`upload(...)` was called on a texture without storage.

Call `allocate(width, height)` first: [Textures and samplers](/build/pipeline/#textures-and-samplers).

## IllegalArgumentException The texture of …x… needs … pixels and got …, pass one pixel per texel

`upload(...)` received fewer pixels than the texture has texels.

Pass one ARGB pixel per texel, row after row: [Textures and samplers](/build/pipeline/#textures-and-samplers).

## IllegalArgumentException No resource resolver knows the input … of type …, register an IResourceResolver that supports it with ResourceResolver.register(...)

`Resource.of(...)` received an object that is neither an asset input nor a type that a registered [[IResourceResolver]] supports.

Pass a supported input, or register a resolver: [Resource resolvers](/build/resource-resolvers/#write-the-resolver).

## IllegalStateException The core shader … is missing from the classpath, check that the JOID jar is complete

A built-in shader of JOID was not found in the jar, which is damaged or was stripped by a build tool.

Ship the JOID jar whole: [Keep the files of the JOID jars](/use/guides/ship-your-application/#6-keep-the-files-of-the-joid-jars).

## IllegalArgumentException … has no uniform blocks, translate its uniforms with UniformLayout.LOOSE

A [[GlslShaderTranslator]] was created with `UniformLayout.BLOCK` for a dialect without uniform blocks.

Use `UniformLayout.LOOSE` for that dialect: [Pick a uniform layout](/build/shader-translation/#pick-a-uniform-layout).

## UnsupportedOperationException The shader uses …, which needs …, but the dialect is …: rewrite the shader without it or translate it for a newer dialect

The source of a shader uses a GLSL feature that the dialect of the graphics context lacks.

Rewrite the shader without the feature: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).
A backend author can translate for a newer dialect: [Pick a dialect](/build/shader-translation/#pick-a-dialect).

## IllegalArgumentException Vertex shaders cannot declare the input …, use the built-in attributes aPosition, aTexCoord, aColor and aNormal

A vertex source declares an `in` variable; JOID gives every vertex shader its built-in attributes.

Use `aPosition`, `aTexCoord`, `aColor` and `aNormal`: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## IllegalArgumentException Fragment shaders cannot declare the output …, write the color to fragColor

A fragment source declares an `out` variable; JOID declares the output of every fragment shader.

Write the color to `fragColor`: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## IllegalArgumentException The shader declares no builtin uniform …: JOID declares its joid_ uniforms only where they are used, translate the shader with stencil(...) for the stencil ones (uniforms: …)

`uniform(...)` received the name of a `joid_` uniform that the translated shader does not declare: JOID adds a builtin uniform only to the shaders that use it.

Translate the shader with `stencil(...)` to get the stencil uniforms, or leave the builtin uniforms to JOID: [Shader translation](/build/shader-translation/#what-the-translator-adds).

## IllegalArgumentException The shader declares no uniform …, declare it in the shader source or fix its name (uniforms: …)

`uniform(name, ...)` received a name that the sources do not declare; the message lists the declared uniforms.

Fix the name, or declare the uniform in the source: [Set uniforms and textures](/use/learn/going-further/shaders/#set-uniforms-and-textures).

## IllegalArgumentException The length … of the uniform array … is neither a number nor a #define of the shader, write a number or #define … in the shader source

The length of a uniform array is a name that no `#define` of the source gives a number.

Write the length as a number, or `#define` it in the source: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## IllegalArgumentException The uniform … cannot take …

`uniform(name, ...)` received values of another type or count than the uniform declares, such as three floats for a `vec4`.

Pass as many values as the type holds, of its kind: [Set uniforms and textures](/use/learn/going-further/shaders/#set-uniforms-and-textures).

## IllegalArgumentException The uniform type … is not supported, use int, uint, bool, float, vec2 to vec4, ivec2 to ivec4, uvec2 to uvec4, bvec2 to bvec4 or mat2 to mat4

A uniform of the source has a type that JOID cannot lay out, such as a structure.

Use one of the types of the message: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## warning [JOID] The … jar check failed

[[JarCheck]] could not start JOID from a packaged jar, or the UI it opened failed; the stack trace follows, and the check exits with `1`.

Read the cause that follows, often another entry of this page: [Run the tests of your backend](/build/testing-a-backend/#run-the-tests-of-your-backend).

## warning [JOID] Usage: GlslExport <output directory>

[[GlslExport]] was run with another number of arguments than one.

Pass the output folder as the only argument: [Check your translation](/build/shader-translation/#check-your-translation).

## warning [JOID] … rendered on … and … on …: differences may come from the GPUs, run them on one GPU

[[SnapshotComparison]] compares renders whose `renderer.txt` name different GPUs; it compares them anyway.

Render the backends on the same GPU: [Compare with LWJGL 3](/build/testing-a-backend/#compare-with-lwjgl-3).

## warning [JOID] Report: …

[[SnapshotComparison]] found a difference; the message gives the link of the report.

Open the report to see each shot next to its reference: [References and the report](/build/testing-a-backend/#references-and-the-report).

## AssertionError A test of BitmapFontContractSuite fails

A test of [[BitmapFontContractSuite]] found a bitmap glyph drawn differently from the contract:

| Message | Cause |
|---|---|
| `…: partial pixels` | At a whole scale, some pixels are partly covered: the glyph is filtered or not placed on whole pixels. |
| `…: ink` | The glyph holds more or less ink than its texels. |
| `…: columns` | At a fractional scale, the glyph does not show its three columns of texels. |
| `…: column` | At a fractional scale, one column of texels gets another amount of ink than the others. |
| `… at … differs from the ASCII atlas by …` | The same glyph from another atlas gives other pixels. |

Sample bitmap fonts with the nearest filter and place the quads exactly where JOID asks: [The suites](/build/testing-a-backend/#the-suites).

## AssertionError A test of RenderBridgeContractSuite fails

A test of [[RenderBridgeContractSuite]] found a render bridge that breaks the render contract; most tests fail on a pixel, with `Pixel x,y`, the expected and the actual color.
The other messages:

| Message | Cause |
|---|---|
| `The … shader does not compile` | A built-in shader of JOID does not compile with your translation. |
| `A shader that fails to compile must report isActive() == false` | A shader with an error reports that it can draw. |
| `A face turned to the light must be brighter than the ambient light` | The lighting built-in does not reach the shader. |
| `A texture must not be mipmapped by default` | A new texture reports mipmaps. |
| `A texture asked to mipmap must report it` | `mipmap(true)` is lost. |
| `A mipmapped checkerboard must minify to its average (deviation …)` | The mipmap levels are missing or wrong. |
| `Mipmaps must reduce the minification error (plain …, mipmapped …)` | The sampler ignores the mipmap levels. |
| `Mipmaps asked after the upload must keep the content and minify to its average (deviation …)` | `mipmap(true)` after `upload(...)` loses the pixels or builds no level. |
| `Pixel …,32` | A transparent border is not sampled beyond the edges of a `CLAMP_TO_BORDER` texture. |

Fix the rule that the name of the failed test states: [Render bridge](/build/bridges/render/#guarantees).

## AssertionError A test of StateGuardContractSuite fails

A test of [[StateGuardContractSuite]] found a frame of JOID that changes with the state that the host left, or that does not give the host its state back:

| Message | Cause |
|---|---|
| `With the … of the host, the pixel …,… is … instead of …` | A trap of the host changed what JOID draws: the guard does not reset that state. |
| `JOID must give the host its state back before a nested host draw` | `suspend` does not restore the state of the host before its drawing. |
| `IRenderBridge.suspend must run the host draw it is given` | `suspend` does not run its drawing. |
| `JOID must leave the state of the host as the nested host draw left it` | The end of the frame does not give the state back as the host left it. |
| `Reading the pixels must leave the state of the host` | The capture of the snapshot backend changes the state. |
| `JOID must give the host its state back after a raster nested in a host draw` | A raster inside a drawing of the host leaves the state changed. |

Journal every state that your bridge changes, and restore it at the end of the frame and around the drawings of the host: [Check the guard](/build/embedding-in-a-host/#check-the-guard).

## IllegalStateException The … jar threw while it drew …, see the stack traces above

[[JarCheck]] drew 60 frames from a packaged jar, and a stack trace reached the error stream.

Read the stack traces above the message: a class or a file is often missing from the jar: [Run the tests of your backend](/build/testing-a-backend/#run-the-tests-of-your-backend).

## IllegalStateException The demo UI … cannot be created: the class does not exist, check the name and the excludes of gradle/variants.gradle

[[JarCheck]] did not find the UI that it opens in the packaged jar: the name is wrong, or the variant excludes the class.

Check the name, and what the variant includes and excludes: [Three jars per module](/build/demos/#three-jars-per-module).

## IllegalStateException The demo UI … cannot be created: its constructor failed (…), fix the constructor from this error

[[JarCheck]] created the UI that it opens in the packaged jar, and its constructor threw.

Fix the constructor from the error in the parentheses: [Run the tests of your backend](/build/testing-a-backend/#run-the-tests-of-your-backend).

## IllegalStateException The demo UI … cannot be created: give it a public constructor without argument

The UI that [[JarCheck]] opens in the packaged jar has no public constructor without argument.

Give it one: [Run the tests of your backend](/build/testing-a-backend/#run-the-tests-of-your-backend).

## IllegalStateException The … jar runs with the dev mode … and the demo mode …: check the excludes of gradle/variants.gradle

The packaged jar does not hold the classes that its variant needs, or holds the classes of another variant.

Check what each variant includes and excludes: [Three jars per module](/build/demos/#three-jars-per-module).

## IllegalStateException … is no longer open after 60 frames of the … jar, check what closes it in that jar

The UI that [[JarCheck]] opened closed during its 60 frames.

Check what closes the UI with that jar: [Run the tests of your backend](/build/testing-a-backend/#run-the-tests-of-your-backend).

## IOException The folder … cannot be created, check that its parent folder can be written

[[GlslExport]] could not create a folder of its output.

Check that the output folder can be written: [Check your translation](/build/shader-translation/#check-your-translation).

## failure [JOID] No snapshot rendered in …, render the snapshots first

[[SnapshotComparison]] found no PNG in the folder of the reference renders.

Render the reference first, with `renderBaseline`: [Compare with LWJGL 3](/build/testing-a-backend/#compare-with-lwjgl-3).

## IOException The image … cannot be decoded, check that it is a valid PNG

[[SnapshotImage#read]] read a file that is not a PNG, or a damaged one.

Record the reference again: [References and the report](/build/testing-a-backend/#references-and-the-report).

## IllegalArgumentException The snapshot command … is unknown, fix the scenario

A command given to [[SnapshotRunner#execute]] is none of its commands.

Fix the command from the table of `execute`: [Drive the UI with commands](/use/learn/running/testing/#drive-the-ui-with-commands).

## IllegalStateException The window shows the … cursor instead of …

The `cursor` command of a scenario expected another cursor than the one that the UI shows.

Check the cursor that the hovered node sets: [Change the cursor over a node](/use/learn/interactivity/hover-and-tooltips/).

## IllegalArgumentException The snapshot window …x… does not fit the …x… snapshot surface, give a smaller window

A `resize` command asked for a window larger than the 1920×1080 surface of the runner.

Resize to 1920×1080 at most: [Drive the UI with commands](/use/learn/running/testing/#drive-the-ui-with-commands).

## IllegalArgumentException The snapshot UI … cannot be created: its constructor threw …

The `ui` or `open` command created the UI, and its constructor threw.

Fix the constructor from the error: [Drive the UI with commands](/use/learn/running/testing/#drive-the-ui-with-commands).

## IllegalArgumentException The snapshot UI … cannot be created: give it a constructor without arguments, of any visibility

The class given to the `ui` or `open` command has no constructor without argument.

Give the full class name, and a constructor without argument: [Render a screen](/use/learn/running/testing/#render-a-screen).

## IllegalStateException A resource never settled in 30s: it kept decoding or rendering, check that the resources of the scenario stop decoding

During a `wait`, a resource kept decoding for 30 seconds, such as a video that plays in a loop.

Pause or stop what keeps decoding before the shot: [Keep every run identical](/use/learn/running/testing/#keep-every-run-identical).

## IllegalStateException A resource never finished loading in 30s, check that its file exists and that its decoder settles

Before a shot, a resource did not finish loading within 30 seconds.

Check that its file exists and can be read, or that a decoder of your own reports when it is done: [Keep every run identical](/use/learn/running/testing/#keep-every-run-identical).

## IllegalStateException … never became stable while the clock was paused, … pixels kept changing: mask the area that moves or stop what moves it

Before a shot, the picture kept changing while the clock of the test stood still, usually because the UI reads the time of the system.

Read the time from `BridgeHandler.CLOCK`, or mask the area: [Keep every run identical](/use/learn/running/testing/#keep-every-run-identical).

## IllegalStateException … never settled: a resource kept decoding or rendering, check that the resources of the scenario stop decoding

The picture of a shot became stable, but a resource was still decoding.

Pause or stop what keeps decoding before the shot: [Keep every run identical](/use/learn/running/testing/#keep-every-run-identical).

## AssertionError [JOID] No reference for the GPU … in …, the shots of … were not compared: record them on this GPU with updateSnapshots…

The [[SnapshotSuite]] runs on a GPU that has no references yet; the message lists the GPUs that have some.

Record the references of this GPU with `updateSnapshots`: [References and the report](/build/testing-a-backend/#references-and-the-report).

## IllegalArgumentException The snapshot UI … cannot be created: the class does not exist, check the name and the classpath

The class given to the `ui` or `open` command is not on the classpath of the test.

Give the full name of the class, and check that the test sees it: [Render a screen](/use/learn/running/testing/#render-a-screen).

## IllegalArgumentException The snapshot backend … cannot be created: the class does not exist, check the name and the classpath

The class that [[SnapshotBaseline]] received as its first argument is not on its classpath.

Give the full name of a snapshot backend, such as [[Lwjgl3SnapshotBackend]]: [Compare with LWJGL 3](/build/testing-a-backend/#compare-with-lwjgl-3).

## IllegalArgumentException The snapshot backend … cannot be created: its constructor threw …

[[SnapshotBaseline]] created the snapshot backend, and its constructor threw.

Fix the constructor from the error: [Compare with LWJGL 3](/build/testing-a-backend/#compare-with-lwjgl-3).

## IllegalArgumentException The snapshot backend … cannot be created: give it a constructor without arguments, of any visibility

The snapshot backend that [[SnapshotBaseline]] received has no constructor without argument.

Give it one, of any visibility: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface).

## IllegalStateException The snapshot cache cannot name the download of …: this JVM has no SHA-1 digest, run the snapshots on a standard JVM

The JVM offers no SHA-1, which the testkit uses to name its downloads.

Run the tests on a standard JVM.

## IllegalStateException OpenAL cannot open the default audio device, check that an audio output device is connected and enabled

The first video with sound asked OpenAL for the default device, and the machine has none, or it is disabled.

Connect or enable an audio output, or register the backend with `audio(false)`: [What register() does](/use/learn/running/backends/#what-register-does).

## IllegalStateException OpenAL cannot create a context on the default audio device, check that no other application holds it exclusively and that its driver is up to date

The default audio device opened but refused an OpenAL context.

Close the application that holds the device exclusively, or update its driver: [base-openal](/build/base-modules/openal/#create-the-bridge).

## IllegalStateException GLFW cannot be initialized, check that the system has a display (DISPLAY or WAYLAND_DISPLAY on Linux)

A snapshot backend could not start GLFW, usually on a machine without a display.

Run the tests with a display, or a virtual one such as Xvfb on a server: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface).

## IllegalStateException GLFW cannot create the …x… snapshot window, check that the graphics driver supports the graphics API the window asks for (its OpenGL version or Vulkan)

The graphics driver refused the hidden window of a snapshot backend.

Update the graphics driver, or run on a machine that supports the API of the backend: [Hidden windows](/build/base-modules/glfw/#hidden-windows).

## warning [JOID] OpenGL cannot link a shader program, fix its sources from the log of the driver: …

The driver compiled both stages of a shader but could not link them; the log of the driver follows, and the shader never draws.

Fix the sources from the log: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## warning [JOID] OpenGL cannot compile a … shader, fix its source at the line the driver reports: …

The driver refused a stage of a shader; its log, with the line of the error, follows, and the shader never draws.
The line is in the translated source, which adds a header to yours.

Fix the source from the log: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl); [Check your translation](/build/shader-translation/#check-your-translation) shows the translated source.

## warning [JOID] This OpenGL context cannot blit framebuffers (…): mipmaps are drawn, which may not give exactly the pixels of the other backends

With the dev jar, the context lacks framebuffer blits, so the mipmaps are drawn instead; JOID prints it once.

No fix is needed; a newer context gives the pixels of the other backends: [Capabilities and paths](/build/base-modules/opengl/#capabilities-and-paths).

## IllegalStateException No OpenGL context is current: create the context of JOID and make it current before registering the backend

The OpenGL render bridge was created on a thread where no OpenGL context is current.

Create the window, make its context current and load the OpenGL functions before `register()`: [Start a backend](/use/learn/running/backends/#start-a-backend).

## IllegalStateException JOID needs OpenGL 2.1 with GLSL 1.20, this context offers …: update the graphics driver or create a newer OpenGL context

The OpenGL context is older than OpenGL 2.1 with GLSL 1.20.

Update the graphics driver, or ask for a newer context: [Check the prerequisites](/use/learn/running/backends/#check-the-prerequisites).

## IllegalStateException JOID needs framebuffer objects (OpenGL 3.0, GL_ARB_framebuffer_object or GL_EXT_framebuffer_object), this context offers …: update the graphics driver or create a newer OpenGL context

The OpenGL context has no framebuffer objects.

Update the graphics driver, or ask for a newer context: [Check the prerequisites](/use/learn/running/backends/#check-the-prerequisites).

## IllegalStateException Only a compatibility context has fixed-function matrices to import, this context is … with …: create the context with the compatibility profile to import them

[[FixedMatrixImport#create]] was called on a core context, which has no fixed-function matrices.

Create the context with the compatibility profile: [Import the matrices of a fixed-function host](/build/embedding-in-a-host/#import-the-matrices-of-a-fixed-function-host).

## IllegalStateException The shader that draws the mipmaps does not compile on …, update the graphics driver

On a context without framebuffer blits, the shader that draws the mipmaps did not compile.

Update the graphics driver: [Mipmaps](/build/pipeline/#mipmaps).

## IllegalStateException No OpenGL render bridge is registered, register an OpenGL backend before GlBorrowedTexture.of(...)

[[GlBorrowedTexture#of]] was called before an OpenGL backend was registered.

Register the backend first: [Show a texture of your application](/use/learn/describing-the-ui/media/#show-a-texture-of-your-application).

## IllegalArgumentException A texture of …x… exceeds the maximum size … of …, downscale the image

An image is wider or taller than the largest texture of the graphics context or device.

Downscale the image, or split it: [Supported formats](/use/learn/describing-the-ui/media/#supported-formats).

## warning [JOID] JNA is missing or cannot be loaded, the mouse cursor stays the default one: …

With the dev jar, the LWJGL 2 backend sets the cursors of nodes through JNA, which is not on the classpath; the cursor stays the default one.

Put JNA on the classpath to show the cursors: [Cursors](/build/backends/lwjgl2/#cursors).

## warning [JOID] The native cursors of LWJGL 2 cannot be set, the mouse cursor stays the default one: …

With the dev jar, the native cursor of LWJGL 2 could not be reached; the backend stops trying and keeps the default cursor.

Check the version of LWJGL 2 on the classpath: [Cursors](/build/backends/lwjgl2/#cursors).

## warning [JOID] The … cursor cannot be loaded (…), the default one replaces it: check that the system has this cursor

With the dev jar, the system has no cursor for one of the cursors of JOID; that one shows the default cursor.

Check the cursor theme of the system: [Change the cursor over a node](/use/learn/interactivity/hover-and-tooltips/#change-the-cursor-over-a-node).

## warning [JOID] libXcursor cannot be loaded, the X11 cursors come from the cursor font: …

With the dev jar, on Linux, libXcursor is missing, so the cursors come from the older cursor font of X11.

Install libXcursor to get the cursors of the theme: [Change the cursor over a node](/use/learn/interactivity/hover-and-tooltips/#change-the-cursor-over-a-node).

## warning [JOID] The clipboard cannot be read, an empty text replaces it: …

With the dev jar, the LWJGL 2 backend could not read the clipboard through AWT; a paste inserts nothing.

Check that AWT can reach the clipboard, which a headless JVM cannot: [LWJGL 2 backend](/build/backends/lwjgl2/#what-it-is-made-of).

## warning [JOID] The clipboard cannot be written, the text stays out of it: …

With the dev jar, the LWJGL 2 backend could not write the clipboard through AWT; a copy does nothing.

Check that AWT can reach the clipboard, which a headless JVM cannot: [LWJGL 2 backend](/build/backends/lwjgl2/#what-it-is-made-of).

## IllegalStateException LWJGL 2 cannot create the OpenAL context (…), check that an audio output device is connected and enabled

The first video with sound asked LWJGL 2 for an OpenAL context, and it failed.

Connect or enable an audio output, or register the backend with `audio(false)`: [LWJGL 2 backend](/build/backends/lwjgl2/#what-it-is-made-of).

## IllegalStateException The LWJGL 2 native … is missing from the backend

[[Lwjgl2Natives#install]] did not find a native library in the backend jar, which is damaged or was stripped.

Ship the backend jar whole, or provide the natives with `org.lwjgl.librarypath`: [LWJGL 2 backend](/build/backends/lwjgl2/#what-it-is-made-of).

## IllegalStateException Unable to extract the LWJGL 2 natives into …, check that the cache folder can be written

[[Lwjgl2Natives#install]] could not write the natives into the cache folder.

Check that the [cache folder](/use/learn/running/files/#find-the-folders) can be written.

## IllegalStateException LWJGL 2 cannot create the …x… OpenGL context of the snapshots (…), check that the graphics driver supports OpenGL

The LWJGL 2 snapshot backend could not create its offscreen OpenGL context.

Update the graphics driver, or run the tests with a display: [LWJGL 2 tests](/build/backends/lwjgl2/#tests).

## IllegalStateException The snapshot profile … needs OpenGL ….… …, this context offers … …: force its version with MESA_GL_VERSION_OVERRIDE and MESA_GLSL_VERSION_OVERRIDE

The LWJGL 3 snapshot backend runs with a [[SnapshotProfile]] whose version or profile the driver did not give.

On Mesa, force the version with the two variables of the message: [OpenGL profiles](/build/backends/lwjgl3/#opengl-profiles).

## warning [JOID] The Vulkan … shader cannot be compiled, fix its source at the line shaderc reports: …

shaderc refused a translated stage of a shader; its error follows, and the shader never draws.

Fix the source from the error: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## IllegalArgumentException A Vulkan window host needs at least one frame in flight, got …: pass 1 to wait for each frame, 2 or more to record the next frame while the GPU draws the previous ones

[[WindowVulkanHost#create]] received a number of frames in flight below `1`.

Pass `1` or more: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## IllegalStateException The Vulkan window frame has already begun, call endFrame() first

`beginFrame()` of the [[WindowVulkanHost]] was called twice without `endFrame()`.

Pair each `beginFrame()` with one `endFrame()`: [Start the other backends](/use/learn/running/backends/#start-the-other-backends).

## IllegalStateException The Vulkan window frame has not begun, call beginFrame() first

`endFrame()` of the [[WindowVulkanHost]] was called without `beginFrame()`.

Begin the frame of the host before the JOID frame: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## IllegalStateException Vulkan is not supported on this system, update the graphics driver or use an OpenGL backend

GLFW found no Vulkan loader or driver on the machine.

Update the graphics driver, or use an OpenGL backend: [Pick the backend that fits your application](/use/learn/running/backends/#pick-the-backend-that-fits-your-application).

## IllegalStateException No Vulkan 1.3 device able to present to the window was found, update the graphics driver or use an OpenGL backend

No device of the machine supports Vulkan 1.3 and the surface of the window.

Update the graphics driver, or use an OpenGL backend: [Check the prerequisites](/use/learn/running/backends/#check-the-prerequisites).

## IllegalStateException No Vulkan render bridge is registered, register the Vulkan backend before VulkanBorrowedTexture.of(...)

[[VulkanBorrowedTexture#of]] was called before the Vulkan backend was registered.

Register the backend first: [Show a texture of your application](/use/learn/describing-the-ui/media/#show-a-texture-of-your-application).

## IllegalStateException … failed with the Vulkan error …, update the graphics driver or run with the Vulkan validation layers to find the call at fault

A Vulkan call returned an error code; the message names the call.

Update the graphics driver; with a host of your own, run with the validation layers to find the misuse: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## IllegalStateException No Vulkan memory type of the device matches the properties … for the types …, update the graphics driver or use another device

The device offers no memory type that JOID needs for an image or a buffer.

Update the graphics driver, or use another device: [Requirements](/build/backends/vulkan/#requirements).

## IllegalStateException The Vulkan device supports none of the depth formats …, use a device that supports one of them

The device supports none of the depth formats that the renderer can use.

Use another device, or update its driver: [Requirements](/build/backends/vulkan/#requirements).

## IllegalArgumentException The Vulkan host … has … frames in flight, getFramesInFlight() must return 1 or more

A Vulkan host of your own returns `0` or less from `getFramesInFlight()`.

Return `1` or more: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## IllegalStateException The Vulkan host … gives a device without Vulkan 1.3, create the instance and the device with VK_API_VERSION_1_3

A Vulkan host of your own gives a device created for an older version of Vulkan.

Create the instance and the device with `VK_API_VERSION_1_3`: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## IllegalStateException No Vulkan 1.3 device able to draw was found, update the graphics driver or use an OpenGL backend

The Vulkan snapshot host found no device with Vulkan 1.3 and a graphics queue.

Update the graphics driver, or run the Vulkan tests on another machine: [Requirements](/build/backends/vulkan/#requirements).

## IllegalStateException VulkanBackend has no render bridge to destroy: call register() first and destroy() only once

[[VulkanBackend#destroy]] was called before `register()`, or twice.

Destroy the backend once, after the loop: [Run inside a Vulkan host](/build/backends/vulkan/#run-inside-a-vulkan-host).

## IllegalStateException The glyphs do not fit in …x… even at …px, raise --width and --height or shrink the charset

The MSDF generator found no glyph size at which the charset fits in the atlas.

Raise `--width` and `--height`, or pass a smaller `--charset`: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).

## IllegalArgumentException Usage: msdf --font <file.ttf|file.otf> [--output <directory>] [--charset <file|ranges>] [--range <pixels>] [--width <pixels>] [--height <pixels>] [--size <pixels>]

The MSDF generator was run without `--font`, or with an unknown option.

Pass the options of the message: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).

## IllegalArgumentException The charset … is neither a file nor codepoints like [32, 126], 8364: check the path or separate the values with commas

`--charset` received neither the path of a file nor a list of ranges and code points.

Check the path, or write the ranges in brackets and separate the values with commas: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).

## IllegalStateException The glyphs do not fit in …x… at …px, raise --width and --height or lower --size

The glyphs of the charset do not fit in the atlas at the size given with `--size`.

Raise `--width` and `--height`, or lower `--size`: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).

## IllegalArgumentException The option … has no value, give it one after a space: … <value>

The last option on the command line of the MSDF generator has no value.

Give the option its value after a space: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship).
