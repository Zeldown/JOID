---
summary: Mixes interleaved audio of 3 to 8 channels down to stereo, for an audio API that plays two channels at most.
---

Learn: [Audio bridge](/build/bridges/audio/#provided).

## stereo(short[],int)

Returns the samples mixed down to interleaved stereo, with the usual weights of the center, back and surround channels, scaled so that no channel clips.
Returns the same array for mono and stereo; channels after the eighth are dropped.
