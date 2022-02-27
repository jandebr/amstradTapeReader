# Amstrad Tape Reader
Restores Amstrad Basic source code from WAV file tape recordings

## Screenshots
Main screen
![screenshot](https://github.com/jandebr/amstradTapeReader/blob/main/screenshots/screenshot1.png)

Code inspection shows the lineage from source code over byte code to audio samples
![screenshot](https://github.com/jandebr/amstradTapeReader/blob/main/screenshots/screenshot2.png)

Code emulation with an integrated JavaCPC
![screenshot](https://github.com/jandebr/amstradTapeReader/blob/main/screenshots/screenshot3.png)


## WAV file assumptions
- Single audio channel (**mono track**)
- The audio sample size is **16-bit**
-- stored as 2's-complement signed integers, ranging from -32768 to 32767
-- byte ordering is little-endian
