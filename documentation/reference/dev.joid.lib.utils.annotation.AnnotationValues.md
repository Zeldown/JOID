---
summary: Copies the values of an annotation into the object that holds the same options, through its setters.
---
A [[UI]] reads its `@UIData...` annotations into their `*Object` classes with it.
For each attribute of the annotation, the target needs a public method of the same name that takes the value; a nested annotation goes to the object that the method without argument returns.

Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui).

## read(Annotation,T)

Sets every attribute of the annotation on the target and returns it; a `null` annotation leaves the target unchanged.
Throws an `IllegalStateException` when the target has no method for an attribute, or when that method fails.

## merge(T,Annotation,Annotation)

Sets on the target only the attributes whose value differs between the previous and the next annotation, so that a change made at run time on an attribute that the annotation did not change is kept.
A `null` annotation stands for the default values; it throws an `IllegalStateException` like `read`.
