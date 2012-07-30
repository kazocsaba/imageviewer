Changelog
=========

Version 1.2.3 (2012.07.30): Fixed exception when the viewer appeared under the mouse cursor.

Version 1.2.2 (2012.06.04):

- Made image mouse position reporting more robust when the change is
due to something other than mouse movement (synthetic mouse movement events).
- Viewer synchronization is now usable when the images have different sizes.
- Added `ImageViewerUtil.synchronizedViewer(Collection<ImageViewer>)`.
- Added option to disable default popup menu.
- Exposed the default popup menu as class `DefaultViewerPopup`.

Version 1.2.1 (2012.03.29): Fixed image mouse events when viewer had overlay.

Version 1.2.0 (2012.03.29):

- Fix synchronization of viewers when some of them were empty.
- Added PixelInfoStatusBar which displays information on an image pixel.
- Added ImageViewerUtil.synchronizePixelInfoStatusBars.
- Fixed image mouse motion events caused by non-mouse-motion changes (e.g. when the resize strategy is changed).
- Fixed flicker when the viewer rescrolls after a resize.

Version 1.1.1 (2012.03.01):

- Enhanced DefaultStatusBar to display alpha too, and to use a shorter display format when there's not enough room.
- Added a message about the error when saving the image failed.
- Fixed exception when calling certain methods on a viewer before it is shown.
- Fixed ImageViewerUtil.synchronizeViewers. (It didn't use to synchronize current state, only future changes.)

Version 1.1.0 (2012.02.28):

- **Changed the default resize strategy to SHRINK_TO_FIT.**
- Enhancements to the 'save image' dialog: help text and image file filter.
- New property: interpolationType.
- Various performance improvements.

Version 1.0.1 (2012.02.07): Fixed removeOverlay.

Version 1.0.0 (2012.01.26):

- Moved everything to package `hu.kazocsaba.imageviewer`.
- Renamed ImageMouseMove to ImageMouseMotion for consistency with Java API.
- ImageSequenceViewer no longer extends JPanel.
- An overlay can now be added to more than one viewer at once.

Legacy changes
--------------

*These are the changes from the time before the library made it into the central Maven repository and had different group ID and artifact ID.*

Version 1.2.5 (2011.10.20): Fix handling of large images or large zoom. Add PixelMarkerOverlay.

Version 1.2.4 (2011.09.23): Fix DefaultStatusBar so that it updates when the image is changed.

Version 1.2.3 (2011.09.07): Add support for custom zoom factors.

Version 1.2.2 (2011.09.06): Add new property: pixelatedZoom; fix small background color bug.

Version 1.2.1 (2011.07.22): No changes, project metadata update.

Version 1.2.0 (2011.04.27): Modify ImageViewerUtil.synchronizeViewers to allow multiple viewers; use bicubic interpolation when resizing

Version 1.1.5 (2011.03.01): Add "Save image" menu item to popup menu

Version 1.1.4 (2011.03.01): Fix the viewer not updating when overlays are added and removed

Version 1.1.3 (2011.02.21): New function: ImageViewer.pointToPixel

Version 1.1.2 (2011.02.21): Add support for mouse drag event

Version 1.1.1 (2010.07.22): Fix subpixel position calculation

Version 1.1.0 (2010.07.19): Remove legacy package; new RESIZE_TO_FIT strategy, other small fixes

Version 1.0.5 (2009.04.23): Make the original MouseEvent accessible from ImageMouseEvent

Version 1.0.4 (2008.09.19): Make ImageViewer the source of events

Version 1.0.3 (2008.09.08): Add ImageSequenceViewer

Version 1.0.2 (2008.08.11):

- Fix image viewer synchronization when one of the viewers doesn't have an image making the other one unscrollable.
- Allow adding mouse listeners to the image viewer.
- Make the image transformation accessible on the public image viewer interface.

Version 1.0.0 (2008.08.08)
