---
summary: Code that runs once after a delay, or on a period, while it is attached to a UI or a node.
---
Runs its listeners on the render thread while its UI or its node is drawn ([Know how long a task lives](/use/learn/state/tasks/#know-how-long-a-task-lives)).

Learn: [Tasks and background updates](/use/learn/state/tasks/).

## create(long)

Creates a task that runs once, this many ms after it is attached.

## every(long)

Makes the task run again every `period` ms after its first run, until it is detached.

## onRun(EventListener)

Adds a listener that runs each time the task runs.

## fireRun()

Runs the listeners now and plans the next run, or detaches a one-shot task.
The UI calls it when the task is due.

## attach(UI)

Attaches the task to the UI and starts counting its delay; it leaves its previous owner first.

## attach(Node)

Attaches the task to the node and starts counting its delay.

## detach()

Detaches the task, which stops running.

## owner(UI,Node)

Records the owner of the task and, when it has one, starts counting its delay.
`UI.addTask` and `Node.addTask` call it; you attach with `attach`.

## isAttached()

If `true`, the task has a UI or a node.

## isDue()

If `true`, the task is attached and its next run time has come.

## getDelay()

Time, in ms, between the attach and the first run.

## getPeriod()

Time, in ms, between two runs; negative for a task that runs once.

## getNextRun()

Time of the next run, in nanoseconds of the clock bridge (`nanoTime()`).

## getEventBus()

The listeners of the task.

## getUi()

The UI the task is attached to, or `null`.

## getNode()

The node the task is attached to, or `null`.
