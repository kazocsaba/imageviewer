This project provides a Swing image viewer component.

See the [wiki](https://github.com/kazocsaba/imageviewer/wiki) for additional details and screenshots,
and the Javadoc API located [here](http://kazocsaba.github.com/imageviewer/apidocs/index.html).

Features
--------

* integrated scroll pane
* multiple [resize policies][resize]
* popup menu to set viewing settings and save the image
* mouse listeners in image coordinate system
* [overlays][overlay] to draw over the image
* status bar support

[resize]: http://kazocsaba.github.com/imageviewer/apidocs/hu/kazocsaba/imageviewer/ImageViewer.html#setResizeStrategy(hu.kazocsaba.imageviewer.ResizeStrategy)
[overlay]: http://kazocsaba.github.com/imageviewer/apidocs/hu/kazocsaba/imageviewer/ImageViewer.html#addOverlay(hu.kazocsaba.imageviewer.Overlay)

Using
-----

The library resides in the central Maven repository with
group ID `hu.kazocsaba` and artifact ID `image-viewer`. If
you use a project management system which can fetch dependencies
from there, you can just add the library as a dependency. E.g.
in Maven:

	<dependency>
		<groupId>hu.kazocsaba</groupId>
		<artifactId>image-viewer</artifactId>
		<version>a.b.c</version>
	</dependency>

Otherwise you can [download it directly](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22hu.kazocsaba%22%20AND%20a%3A%22image-viewer%22).