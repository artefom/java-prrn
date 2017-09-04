# This is a description of a script that creates mosaic of images


[![Foo](https://i.imgur.com/D5wjsv9.png)](https://drive.google.com/open?id=0BxWRyyRCfA3gS0JEaTFpMWRLcXM)

## User story
I (user) would like to have a tool build on top of GDAL that would perfrom mosaicing of large ammount of images in a mult-threaded way on a cluster. I must be able to select target projection, extents and so on.. just like [gdalwarp](http://www.gdal.org/gdalwarp.html), but performing IR-MAD based on PRRN for remote sensing image mosaics.

### What is the outcome?
The result will be a command-line java application that utilizes same functionality as [gdalwarp](http://www.gdal.org/gdalwarp.html) but with special features (command line parameters of this application.

### What is done:

Algorithm for IR-MAD. Single threaded. 2 Images.

### Notes from paper (PRRN)

#### Calculation steps for RRN
RRN method addresses two overlaping images.
1. IR-MAD is performed
2. Othogonal regression with the selected invariant pixels is employed to calculate normalistion coefficients for each band of the subject image.

#### Calculation steps for IR-MAD
Look at python program

#### Calculaion steps for Parallel implementation

1. Build a tree with reference image in the center. This is done by finding intersecting images (parallel?)

2. Calculate IR-MAD coefficients for each pair of adjacent images (parallel)

3. Apply sequential transofrmation for coefficients (calculate normalization coefficients), so they all match reference image
(sequential part)

> TODO: 
1. Determine what components will final product need
2. Find more atomic pieces which are needed to build a software product, their dependencies. Like IR-MAD depends on CCA or RRN depends on IR-MAD and so on...

# Q & A

**What is the input?**
The program will have a terminal interface with following options:
mosaic [options] input_files... output_directory

**What is the outcome?**
There are few options:
: 1. Folder with reprojected and recolored images. Since program will in either case do a reprojecting, it'll have to store reprojected files in some tempoary directory, this temporary directory may be result directory.
2. JSON-File, describing the reference image and band transfomation matrices
3. Color-transfomated images, but without reprojecting. May be usefull, if we'll do reprojection later.

Also, there should be debug output, such as log. Log should contain:
: 1. Tree structure of performed tree building
2. Error reports, if any
3. Warnings

**How color transfomation is perfomed?**
IDK

**How much policemen does it take to screw a light bulb?**
- None, they just beat the shit out of room for being black

# Programm structure

Calculation steps:

: 1. User inputs files.
2. Program detects intersecting images.
3. Program builds a tree structure based on intersecting images
4. Program reprojects and aligns pixels with reference (root) image in parallel. On this stage, we have a set of pairs of images, which we have to process. Multiple threads have to be spawned.
5. Program spawns sub-threads. These sub-threads have to determina color tansformation matrix and push it to common pool
6. Pogram joins all sub-threads. On this stage, we have a set of neatly placed color transformation matrices into one array.
7. Program calculates transformation matrices with reference image as pivot.
8. (Optional) program updates band values of each image.

> TODO:
We need to create domain model, class diagram, parallel diagram, sequence diagram. 

# Glossary

* [MAD](your mom) Multivariate Alternation Detection. Detects changes between two images

* [IR-MAD]() Iterative reweighted version of MAD
some references: [python implementation](https://github.com/mortcanty/CRCDocker/blob/master/src/iMad.py) [port from R](https://rdrr.io/rforge/imad/man/iMad_original.html)
Scientific paper: [Canty and Nielsen (2008)](http://www2.imm.dtu.dk/pubdb/views/edoc_download.php/5362/pdf/imm5362.pdf)

* [RRN](http://www.sciencedirect.com/science/article/pii/0924271696000184) - **Relative Radiometric Normalization**
is a procedure used to prepare multitemporal image data sets for the detection of spectral changes associated with phenomena such as land cover change. This procedure reduces the numeric differences between two images that have been induced by disparities in the acquisition conditions (e.g. sensor performance, solar irradiance, atmospheric effects) rather than changes in surface reflectance.

*[PRRN]: Parallel relative radiometric normalisation
*[IR-MAD]: Iteratively Reweighted Multivariate Alteration Detection
*[GDAL]: Geospatial Data Abstraction Library.
*[RRN]: Relative radiometric normalisation
*[R]: R scientific programming language