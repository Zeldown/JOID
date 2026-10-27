# Voice and writing rules (every writer and reviewer loads this file)

## Audience
- **Use JOID** pages speak to a Java developer who builds interfaces with JOID and one of our backends. They never explain bridge contracts, backend internals or repository conventions.
- **Build on JOID** pages speak to a backend author. They assume the reader knows Use JOID and link to it freely instead of re-teaching it.
- Build on JOID documents how to write a backend, not how to contribute to JOID: it never mentions the JOID repository, its tests, its hooks or "contributors".
- A page belongs to exactly one documentation and one genre (tutorial, learn, guide, reference, in depth, build).
- The JOID documentation never mentions an external project built on JOID, nor a particular game or host that JOID runs in: such a project documents itself and links to JOID's documentation, never the other way.
- Use JOID never mentions the demos or the demo jar: they belong to Build on JOID (`build/demos`).
- Use JOID links to Build on JOID from two pages only: `use/index` (which presents both documentations) and `use/learn/running/backends` (which points the reader who writes a backend to Build on JOID). No other Use page links to `/build/`. Build on JOID links to Use JOID freely, and both link to the reference (`/reference/`), which covers every public type and belongs to neither.

## Language
- English only. Second person ("you"), present tense, active voice.
- One sentence per source line (semantic line breaks).
- Short sentences. One idea per paragraph. Paragraphs of 1 to 4 sentences.
- Banned words and phrases: simply, just, easy, easily, obviously, basically, actually, currently, new (for a feature), of course, it's worth noting, note that, please. No "we" for the reader. No future tense to describe behavior. No anthropomorphism ("the node wants").
- No history ("since 8.0.0", "previously", "migrating from"): the documentation describes the current version and has no changelog.
- No edge cases that nobody hits. A limit that a real user can hit goes in a Pitfall or a "Limits" section, once.
- Name things exactly as the code does (`RectNode`, `onClick`, `@UIDataOverlay`), in `code` font, linked to their reference page on first mention in a page: write `[[Type]]` there (the `first-mention` check refuses a type of the reference written in bare `code` before its first link; headings and code blocks do not count).
- Numbers follow the API: percentages on 1 (`0.5F` = 50 %), units named (design pixels, ms), literals in the code style (`1D`, `0.5F`).

## Teaching
- Example before theory. Show the result (capture) right under the code that produces it.
- One notion per section; section titles describe the problem or the goal ("React to a click", "Share a value between two nodes"), not the API name.
- Motivate by failure when it helps: the naive version, what goes wrong, then the right version (mark wrong code with `// ✗` and the reason on the same line, right code with `// ✓`).
- Recommended way first, then the alternative and the condition to use it. Never present two equivalent ways as a choice.
- A forward reference is allowed only as a one-line Note ("The Signals chapter explains this"), never as an explanation.

## Zero repetition
- Every notion has exactly one owner page (see `owners.md`). Anywhere else: one sentence at most, then a link to the owner page anchor.
- The learn pages explain the common case; the reference lists everything. A learn page never copies a reference table; a reference page never re-teaches a concept (it links to the learn page).
- The only teaching in the reference is the mini-guide of a central class (`Node`, `UI`, `Signal`, `NodeEffect`, the animators, the bridges): what it is, when to use it, one compiled example, then a link to its Learn page. It never explains the concept again.
- A guide is a short task: it links the Learn page of each notion it uses and never redoes a Learn chapter.
- A diagram is drawn once, on the owner page.

## Callouts (Starlight asides)
- At most two per page, never two of the same type in a row.
- `note`: one extra fact. `tip`: a better way. `caution`: a mistake that compiles but misbehaves. `danger`: data loss, crash, GPU leak.
- One or two sentences each. More than that becomes a section.
- Deep dives use a collapsible `<details>` titled as a question.

## Code
- Every Java snippet compiles: it comes from `documentation/examples` through a region include. A fragment that cannot compile is marked explicitly and has 5 lines at most.
- Owner style: tabs, `final` on parameters and locals, `this.`/`super.`/`Class.` qualification, explicit visibility, no comments in code except the `✗`/`✓` markers, long chains one call per line aligned under the class. A line of displayed code (region or fragment) is 120 columns at most, a tab counting as 4 (the `line-width` check refuses a longer line).
- Neutral demo visuals unless the page is about design: `#DDDDDD` background, `#999999` text.
- Show only what matters; elide with `...` in fragments. Full runnable files live in the examples: the site adds a "Full file" link under every region include, so a page never links them by hand.

## Images
- Captures come from testkit scenarios (deterministic). WebP. A capture that shows interaction or motion (a click, a hover, a drag, a scroll, typing, a transition, an animation) is a film made with `# film`, with the pointer drawn, never a still whose caption says that it moves. A film runs at 30 frames per second at least and at 60 for fast or fluid motion (drags, scrolls, transitions, tweens), lasts 2 to 6 s, ends in a state that loops back to its start, and plays at the real pace of the scenario. Films are lossless, except a film whose source is itself lossy, such as a video: `# quality <shot> <0-100>` encodes it as lossy WebP. A heavy film is cut down by its crop and its length, never by its frame rate.
- Every image has alt text that states what it shows and a caption that states why it matters. The alt text describes what the picture shows, never only the name of a class.
- A capture shows exactly what its alt text and its caption say. A state that a scenario cannot produce reliably is shown by a scene built for it, or the capture is removed with its caption.
- Every scene and diagram uses the common palette: an opaque `#DDDDDD` background drawn by the scene, text `#999999`, accents white, `#999999` and the dark grey `#555555`, and in the diagrams the labels in `#555555` (`DiagramUI.LABEL`) and the violet accent `new Color(124, 92, 255)` (`DiagramUI.ACCENT`); a `# crop` exactly on that background, with at least 10 px of it around the content (only the pointer and the soft edge of a blur or a shadow come closer), never the `#323232` window of the testkit nor a black frame. A capture of the window (`describing-canvas-pinned`, `-wide`, `-tall`) is cropped on the window, and a capture of a popup on its dark backdrop. A tutorial capture is cropped on the useful area and stays readable in the column (1200 px wide at most).
- An animated capture gets a still version for reduced motion: the last frame, or the frame named by `# still <shot> <index>` in its scenario. A click on a film, or Enter or Space once it has the focus, switches it between the film and that still.