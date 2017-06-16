# color-normalization

It's a plugin that work with the [Icy](http://icy.bioimageanalysis.org/) software. A bio-image analysis software made by the [Pasteur Institute](https://www.pasteur.fr/en).

The software was made during an end-of-study internship in the [Bio-Image Analysis](http://www.bioimageanalysis.org/) team in order to validate my DUT in Computer Science at the IUT of Montreuil (Paris 8)

The software allows to normalize the colors from a source image to a target image. I used both Macenko and Reinhard methods.

Macenko's method :

* [M.Macenko et al](https://pdfs.semanticscholar.org/0eac/0ed2c87910dbb7d9f622854efb7c7b7b6f0b.pdf)
* [A.M.Khan et a](http://wwwx.cs.unc.edu/~mn/sites/default/files/macenko2009.pdf)

Reinhard's method :

* [Reinhard et al](https://www.cs.tau.ac.il/~turkel/imagepapers/ColorTransfer.pdf)

# Installation

To use this project you first need to download [Icy](http://icy.bioimageanalysis.org/download) and have to run it from Eclipse.

So just download Icy for [Eclipse](http://icy.bioimageanalysis.org/index.php?display=startDevWithIcy#installEclipse)

Then create a new *Icy project* and include all the source code. 
Don't forget to add [Commons math](http://commons.apache.org/proper/commons-math/) and [Jama](http://math.nist.gov/javanumerics/jama/) *.jar* and **EzPlug** (The **EzPlug** jar should be in the **plugings/adufour** folder) to the project.

**You will have to rename the packages to your developer name located on "Window->Preferences->Icy. Rename then algorithms.yourName.normalization and plugins.yourName.normalization**

Then either run Icy from Eclipse and you'll find the plugin in the plugin list, or create a new *.jar* and add it yourself to the */plugings* folder.

The plugin should appear on the **Other Plugings** tab


# Use

You need at least 2 images, the *source* and *target* images.

Simply open images on Icy and select them on the plugin's menu. Then select a normalization method.

The colors from the *target* image will be transferred to the *source* image. Note that this operation could take a bit of time depending on your image size.



