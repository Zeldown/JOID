---
summary: One shot of a snapshot report: its name, its status, its different pixels and its two files.
---

Learn: [References and the report](/build/testing-a-backend/#references-and-the-report).

## of(String,SnapshotStatus,int,File,File)

Returns the entry of that shot.

## compare(String,SnapshotImage,File,File,int,String,List)

Compares the image with the reference file at that tolerance, adds a message to the list of failures when they differ, and returns the entry.

## getName()

Returns the name of the shot.

## getStatus()

Returns the status of the shot.

## getPixels()

Returns the number of different pixels.

## getRender()

Returns the file of the render.

## getReference()

Returns the file of the reference.
