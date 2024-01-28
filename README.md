# Amstrad Tape Reader
Restores Amstrad Basic source code from tape recordings (WAV files)


## Screenshots
Setup screen
![screenshot](https://github.com/jandebr/amstradTapeReader/blob/main/screenshots/screenshot0.png)

Results screen, featuring [Amstrad PC](https://github.com/jandebr/amstradPc) for code emulation
![screenshot](https://github.com/jandebr/amstradTapeReader/blob/main/screenshots/screenshot3.png)

Code inspection shows the lineage from source code over byte code to audio samples
![screenshot](https://github.com/jandebr/amstradTapeReader/blob/main/screenshots/screenshot2.png)


## WAV file assumptions
* Single audio channel (**mono track**)
* The audio sample size is **16-bit**
    * stored as 2's-complement signed integers, ranging from -32768 to 32767
    * byte ordering is little-endian
