#														THIS IS THE README DOCUMENT OF THIS PROJECT

![Finnair Plus](/app/src/main/res/drawable-v24/finnair_logo.jpg "Finnair Plus")
## Branch: MapAndLocation SPRINT 1 Closed on 3.12.


In this branch _Lari Alakukku_ worked on adding a visible and usable Google Map to the project and the functionality
for asking to use the user's location information.

### The tasks that were completed:

 * Defaul location Helsinki
 * Ask permission to use the internet
 * Zoom on current location
 * Handle the situation where user doesn't give location permissions
 * BUG: Remove 'don't ask again' from location permission widnow


### Major parts of implementation:

 * Added an Android Studio Google Maps template as a basis.
 * Added calls to requestPermission and overrode onRequestPermissionsResult.
 * Added the layout file location_permission_layout for a custom dialog.
 * Added functionality to center the map on Helsinki in case location data isn't available.



--------------------------------------------------------------------------------------------


In this branch _Santeri Niemelä_ modified the styling of the Google Map. The modifications were 
accidentlly done in the same branch as default location and permissions.

### The tasks that were completed:
* Hid unnecessary parts of the map (road numbers, points of interest etc.)
* Changed coloring to match Finnair's brand coloring


### Major parts of implementation:

* Created a JSON file res/raw/style_json.json which includes styling
* Added a call to method GoogleMap.setMapStyle() in MapsActivity.onMapReady()

### TODO:
* Modify how markers show depending on zoom level  <-- DONE (see below)
- This will be implemented in another issue ("Design the look of partner dots")


## Branch: Design the look of partner dots SPRINT 1 Closed on 7.12.
_Santeri Niemelä_ worked on Markers. 

### The tasks that were completed:
* Added a (default) balloon Marker with title and text snippet
* Added an image balloon Marker with Aalto university logo
* Changed behaviour of the markers so that zooming in changes the Marker from default balloon to image Marker
* Clicking the Marker focuses on that Marker and zooms in
* Clicing a Marker's infowindow causes an event to launch (currently only changes Marker blue)


### Major parts of implementation:

* Added a new class MarkerClass which helps create different Markers
* Modified class MapsActivity to implement most of the functionality
    - @override onMarkerClick()
    - @override onInfoWindowClick()
    - mMap.setOnCameraMoveListener()

