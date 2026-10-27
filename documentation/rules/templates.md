# Page templates

Front-matter on every page: `title`, `description` (one sentence, used by search and link previews), and `genre` (tutorial | learn | guide | reference | in-depth | build).

## Learn page (Use JOID > Learn)
1. Intro: 1 to 3 sentences that state the problem this page solves.
2. "You will learn": the line `**You will learn**`, then 3 to 5 bullets, in the order of the H2 sections. Each bullet starts with a capital letter and has no final punctuation.
3. Sections (H2), each one notion, titled by goal. Inside: example + capture, then the explanation, then the rule.
4. At most two callouts (see voice.md).
5. "Recap": 3 to 9 bullets that restate the rules, with the key terms in bold.
6. "Try it": 0 to 4 small challenges, all in one form: a short title in bold on its own line that ends with a period and names what the challenge trains (`**Faster wheel.**`), the task on the next line without a blank line, then the solution in `<details>` with `<summary>Solution</summary>`, also for a question without code. Never a number (`1.`), even for a single challenge; a blank line between `</details>` and the next title. The `challenges` check applies it.
7. Prev/next follows the learning order (sidebar order).
Length: as long as needed to teach the notion completely, no more. If a page grows past ~2 000 words, split it.

## Chapter opener (first page of each Learn chapter)
- The problem the chapter solves in 2 to 3 sentences.
- For each page of the chapter: one paragraph + one tiny example + "Read *Page* to learn …".
- No callout, no challenge, no full explanation (the owner pages explain).

## Tutorial lesson
- Goal of the lesson and the capture of the expected result.
- Starting point (runs, with a visible gap) → numbered steps (instruction, code with changed lines highlighted, "Run it: you should see …").
- A challenge with a collapsible solution. "What you learned" (3 to 5 bullets) linking to the Learn pages.
- The tutorial never explains a notion in depth: it links to the Learn page that owns it.

## Guide (How-to)
- Title as a task ("Make a modal popup").
- What you will build (capture), prerequisites (links).
- Numbered steps: action, code, "Check": what you should see.
- "If it does not work": 3 to 5 ordered checks (dev-mode warnings, common mistakes).
- Links to the reference pages used.

## Reference description file (`documentation/reference/<fqcn>.md`)
Generated pages take signatures, defaults, inheritance and qualifiers from the code; writers only write text:
- `summary`: one sentence, max 200 characters. Verb in the third person for a type that does something ("Draws a filled rectangle…"), noun phrase otherwise. Never paraphrase the name.
- `description`: 1 to 5 paragraphs. For central classes (`Node`, `UI`, `NodeEffect`, `Signal`, animators, bridges), a mini-guide: what it is, when to use it, one compiled example (a region include), then a link to its Learn page; it never re-teaches the concept (see voice.md). For the others, short.
- The description ends with a line `Learn: [Page](/url/), ...` that names the owner page first (the `learn-targets` check compares it with `owners.md` when the type is named there); a guide is listed on the same line (never a `Guides:` line, the `learn-lines` check refuses it). The docgen merges it with the pages that link the class into one "Learn more" section, so the description never lists them twice: the owner page of `owners.md` first, then the `Learn:` links, then the other pages that link the class, 5 links at most; the chapter openings and the Quick Start never appear there.
- The first line of every entry is a sentence that stands alone: the docgen copies it into the summary column of the tables ("Summary" for an event).
- One entry per documented member (signature key from docgen): what it does in one or two sentences; for a property, what the value changes on screen; for a boolean, "If `true`, …"; for a nullable value, what `null` means; for an event method, when its listener runs (`handle`: the raw input, before the node handles it; `before`: a change that the listener can cancel; `on`: what happened) and when it does not run; for a hook, when JOID calls it and what an override must do; for a factory, what the arguments are. Default values are NOT written by hand (docgen prints them), except when the value read is misleading: a sentinel (`0` for "the size of the node", `-1` for "no limit"), a `null` that falls back on another value, or the neutral argument of a fresh instance for a value set at creation. Then the entry ends with a line `Default: <explanation>`, which replaces the printed default in the tables.
- A behavior that differs by backend: one "Backend note" paragraph at the end.
- Optional front-matter: `capture` and `alt` (a capture other than the catalog card, with its alt text), `example` (`file#region`), `backends` (the backends that support the type, comma-separated).

## Generated reference pages
- Each type page shows the declaration, the summary, the capture with the alt text of its catalog card, the description, "Learn more", then the tables.
- Tables of more than 12 rows are grouped by role (lifecycle, children, position and size, hover, input, scrolling, drag and drop, effects, drawing, animation, reactivity, then queries, actions and static methods); the members that only a subclass uses come last, under "For subclasses".
- Inherited members are folded by class of origin, in the order of the origin page, without the members for subclasses. The page outline goes down to level 3.
- A family index opens with its text from `documentation/reference/families.md` (one `## <family>` entry: one sentence, then a `Learn:` line), then, for nodes, one sentence that names the nodes that need a skin and, for effects, one sentence when every effect runs on the three backends or the backend matrix when one does not, its main types, its related types and its supporting types (nested types, callbacks, listeners). When most classes of a family extend one class (`Node`, `Signal`), the main types are that class, first, then its subclasses; the other types of the family (interfaces, enums, helpers such as the tooltip elements) go under "Related types".

## Warnings and errors
- `documentation/reference/errors.md` holds one `## <key>` entry per message (the key is printed by the docgen): the cause, then the fix, with links to the owner pages.
- Every message is on `reference/errors`, grouped by the family of the type that emits it. Anchors are short slugs of the message, stable while its first words stay the same.

## In depth
- Opens with "You need this page only if …".
- Diagram first, then the mechanism step by step, then what it means for your code (links to Learn).

## Build on JOID page
- Contracts as tables: **Required** (what you implement), **Provided** (what you get), **Guarantees** (what you must ensure), each row linked to the contributor reference.
- Procedures as numbered steps ending with a verification (tests to run, expected output).
- Never re-teach Use JOID: link to it.

## Diagrams
- A diagram is a JOID scene in `documentation/examples` (compiled), rendered by `npm run captures` to WebP (animated WebP for motion). JOID draws its own diagrams.
- One diagram per notion, on its owner page only (see `owners.md`); other pages link to it.
- On technical pages (in depth, build), the diagram comes before the code.
- Readable in both site themes (light and dark): the opaque `#DDDDDD` backdrop of the common palette (voice.md, Images) serves both.
- Alt text describes the diagram completely; the caption says why it matters.
- Every captioned image, capture or diagram, has one form: `<figure>`, a blank line, the image, a blank line, then `<figcaption>`, the caption (one sentence per line), `</figcaption>` and `</figure>`. The caption may hold inline Markdown (`code`, links, `[[Type]]`): the documentation plugin renders it. An italic line under an image is never a caption.
