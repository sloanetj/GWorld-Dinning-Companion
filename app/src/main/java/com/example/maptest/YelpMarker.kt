package com.example.maptest

import com.google.android.gms.maps.model.LatLng

// "id" is used in api call for this restaurant's reviews
data class YelpMarker constructor(val latLng: LatLng, val name: String, val rating: String, val isGWorld: Boolean, val id: String)