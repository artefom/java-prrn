# This is a description of a script that creates mosaic of images


[![Foo](https://i.imgur.com/D5wjsv9.png)](https://drive.google.com/open?id=0BxWRyyRCfA3gS0JEaTFpMWRLcXM)

## User story
I (user) would like to have a tool build on top of [GDAL] that would perfrom mosaicing of large ammount of images in a mult-threaded way on a cluster, performing [IR-MAD] based on [PRRN] for remote sensing image mosaics. All input images are already aligned (projection and pixels).

### What is the outcome?
The result will be a command-line java application that recieves inputs, flags, and produces output. Input parameters preferably be done mimicing [gdalwarp]

### What is done:

Algorithm for [IR-MAD]. Single threaded. 2 Images.

### Assumptions:

*Since we can use python and CCA needs to be parallel following assumtion is outdated, but for development simplicity, we still assume that:*
> **Если это упростит задачу, можно считать, что две любые сцены помещаются польностью в память**
> I assume, that any 2 datasets fit into memory (without compression).
Without this assumption some algorithms will need to be implemented in batch fashion.
i.e [CCA] and Orthogonal Regression will need to be fed with small batches, other
than whole dataset. Such implementation, though easily done with TensorFlow, seems difficult to implement in Java language.

- Any 2 images are aligned (projection and pixel)

### Notes from paper [PRRN]

#### Calculation steps for [RRN]
[RRN] method addresses two overlaping images.
1. [IR-MAD] is performed
2. Othogonal regression with the selected invariant pixels is employed to calculate normalistion coefficients for each band of the subject image.

#### Calculation steps for [IR-MAD]
Look at python program

#### Calculaion steps for Parallel implementation

1. Build a tree with reference image in the center. This is done by finding intersecting images (parallel?)

2. Calculate [IR-MAD] coefficients for each pair of adjacent images (parallel)

3. Apply sequential transofrmation for coefficients (calculate normalization coefficients), so they all match reference image
(sequential part)

> TODO:
> 1. Determine what components will final product need
> 2. Find more atomic pieces which are needed to build a software product, their dependencies. Like [IR-MAD] depends on [CCA] or [RRN] depends on [IR-MAD] and so on...

# Q & A

**What is the input?**
The program will have a terminal interface with following options:
mosaic [options] input_files... output_directory

**What is the outcome?**
There are few options:
1. Folder with reprojected and recolored images. Since program will in either case do a reprojecting, it'll have to store reprojected files in some tempoary directory, this temporary directory may be result directory.
2. JSON-File, describing the reference image and band transfomation matrices
3. Color-transfomated images, but without reprojecting. May be usefull, if we'll do reprojection later.

Also, there should be debug output, such as log. Log should contain:
1. Tree structure of performed tree building
2. Error reports, if any
3. Warnings

**How color transfomation is perfomed?**
IDK

**How much policemen does it take to screw a light bulb?**
- None, they just beat the shit out of room for being black

# Programm structure

Calculation steps:

1. User inputs files.
2. Program detects intersecting images.
3. Program builds a tree structure based on intersecting images
4. Program reprojects and aligns pixels with reference (root) image in parallel. On this stage, we have a set of pairs of images, which we have to process. Multiple threads have to be spawned.
5. Program spawns sub-threads. These sub-threads have to determina color tansformation matrix and push it to common pool
6. Pogram joins all sub-threads. On this stage, we have a set of neatly placed color transformation matrices into one array.
7. Program calculates transformation matrices with reference image as pivot.
8. (Optional) program updates band values of each image.

# Useful links
[netlib-java] Fast linear algebra library can be used for implementing CCA, IR-MAD, and Orthogonal Regression

[draw.io paper](https://github.com/artefom/prrn-mosaic) about CCA calulation process and benchmarks.

# Glossary

* [MAD] Multivariate Alternation Detection. Detects changes between two images

* [IR-MAD] Iterative reweighted version of MAD
some references: [python implementation](https://github.com/mortcanty/CRCDocker/blob/master/src/iMad.py) [port from R](https://rdrr.io/rforge/imad/man/iMad_original.html)
Scientific paper: [Canty and Nielsen (2008)](http://www2.imm.dtu.dk/pubdb/views/edoc_download.php/5362/pdf/imm5362.pdf)

* [RRN] - **Relative Radiometric Normalization**
is a procedure used to prepare multitemporal image data sets for the detection of spectral changes associated with phenomena such as land cover change. This procedure reduces the numeric differences between two images that have been induced by disparities in the acquisition conditions (e.g. sensor performance, solar irradiance, atmospheric effects) rather than changes in surface reflectance.

* [CCA] - Canonical Correlation Analysis. Used to decrease dimensionality of dataset. 

[PRRN]: https://drive.google.com/open?id=0BxWRyyRCfA3gS0JEaTFpMWRLcXM "Parallel relative radiometric normalisation."
[RRN]: http://www.sciencedirect.com/science/article/pii/0924271696000184 "Relative radiometric normalisation"
[IR-MAD]: http://www2.imm.dtu.dk/pubdb/views/edoc_download.php/5362/pdf/imm5362.pdf "Iteratively Reweighted Multivariate Alteration Detection"
[MAD]: http://www2.imm.dtu.dk/pubdb/views/edoc_download.php/5362/pdf/imm5362.pdf "Multivariate Alteration Detection"
[GDAL]: http://www.gdal.org/ "Geospatial Data Abstraction Library"
[R]: https://en.wikipedia.org/wiki/R_(programming_language) "R scientific programming language"
[gdalwarp]: http://www.gdal.org/gdalwarp.html "tool from gdal library for mosaicing"
[CCA]: https://en.wikipedia.org/wiki/Canonical_correlation "Canonical Correlation Analysis"
[netlib-java]: https://github.com/fommil/netlib-java "Fast Linear Algebra for Java"