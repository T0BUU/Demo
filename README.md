#														THIS IS THE README DOCUMENT OF THIS PROJECT

![Finnair Plus](/app/src/main/res/drawable-v24/finnair_logo.jpg) "Title")
## Branch: MapAndLocation SPRINT 1 Closed on 3.12.


In this branch _Lari Alakukku_ worked on adding a visible and usable Google Map to the project and the functionality
for asking to use the user's location information.

### The tasks that were completed:

 * Defaul location Helsinki
 * Ask permission to use the internet
 * Zoom on current location
 * Handle the situation where user doesn't give location permissions
 * BUG: Remove 'don't ask again' from location permission widnow



## Branch: MapAndLocation SPRINT 1 Closed on 3.12.

In this branch _Santeri Niemelä_ modified the styling of the Google Map. The modifications were 
accidentlly done in the same branch as default location and permissions.

### The tasks that were completed:
* Hid unnecessary parts of the map (road numbers, points of interest etc.)
* Changed coloring to match Finnair's brand coloring


### Major parts of implementation:

* Created a JSON file res/raw/style_json.json which includes styling
* Added a call to method GoogleMap.setMapStyle() in MapsActivity.onMapReady()

### TODO:
* Modify how markers show depending on zoom level
- This will be implemented in another issue ("Design the look of partner dots")