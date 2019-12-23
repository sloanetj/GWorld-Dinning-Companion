package com.example.maptest

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.maps.model.LatLng
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.net.UnknownServiceException

class NetworkManager {

    private val okHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()

        // This causes all network traffic to be logged to the console
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(logging)

        okHttpClient = builder.build()
    }




    fun retrieveGWRating(context: Context):YelpMarker {

        val key = context.getString(R.string.yelp_key)

        // Create the Request object
        val request = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/search?term=The%20George%20Washington%20University&location=Washington%20DC")
            .header(
                "Authorization",
                "Bearer $key"
            )
            .build()
        // Actually makes the API call, blocking the thread until it completes
        val response = okHttpClient.newCall(request).execute()

        // Get the JSON string body, if there was one
        val responseString = response.body?.string()

        // Make sure the server responded successfully, and with some JSON data
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {
            // Parse our JSON string

            // Represents the JSON from the root level
            val json = JSONObject(responseString)
            val businesses = json.getJSONArray("businesses")
            val gwu = businesses.getJSONObject(0)
            val rating = gwu.getString("rating")
            val id = gwu.getString("id")

            // we are using hardcoded coordinates
            return YelpMarker(LatLng(0.0,0.0), "gwu", rating,true, id)

        } else {
            // error indicator
            return YelpMarker(LatLng(0.0,0.0), "NETWORK ERROR", "0",false, "0" )
        }

    }



    fun retrieveYelpMarkers(context: Context, latLng: LatLng, radius: String?, food: BooleanArray): List<YelpMarker> {

        val key = context.getString(R.string.yelp_key)

        val markers = mutableListOf<YelpMarker>()
        val lat = latLng.latitude
        val lon = latLng.longitude

        // get food categories from intent
        var foodQuery = ""
        for(i in 0 until 5){
            if (food[i]){
                if(i == 0) foodQuery += ",burgers"
                if(i == 1) foodQuery += ",pizza"
                if(i == 2) foodQuery += ",chinese"
                if(i == 3) foodQuery += ",japanese"
                if(i == 4) foodQuery += ",indpak"
                if(i == 5) foodQuery += ",salad"
            }
        }

        // get restaurants within search area radius of long press using Yelp API
        val yelpRequest = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/search?latitude=$lat&longitude=$lon&radius=$radius&categories=food$foodQuery")
            .header(
                "Authorization",
                "Bearer 8fWZHHQ9IZ6PbCWm0EVPA--GGHtr2uJTPB5-uioPljqUIm6qwMsP1t-XVrkD6inT_P1caNsPW-CkMc4TFFHHh5qeFflYnzLG7ExSFdVW2mcOwrgNY9eoIIly9Ai2XXYx"
            )
            .build()
        val response: Response
        try{
            response = okHttpClient.newCall(yelpRequest).execute()
        } catch (exception: UnknownHostException) {
            // return an indicator of error
            markers.add(YelpMarker(LatLng(0.0,0.0), "NETWORK ERROR", "0", false,"0" ))
            return markers
        } catch (exception: IOException) {
            // return an indicator of error
            markers.add(YelpMarker(LatLng(0.0,0.0), "NETWORK ERROR", "0", false,"0" ))
            return markers
        }
        val responseString = response.body?.string()


        // get all the gworld restaurants so that we can cross-reference with yelp results
        val gwRequest = Request.Builder()
            .url("http://www.mocky.io/v2/5d7e80913300008e00f0ad94")
            .build()
        val gwResponse: Response
        try{
            gwResponse = okHttpClient.newCall(gwRequest).execute()
        } catch (exception: UnknownHostException) {
            // return an indicator of error
            markers.add(YelpMarker(LatLng(0.0,0.0), "NETWORK ERROR", "0",false, "0" ))
            return markers
        } catch (exception: UnknownServiceException) {
            // return an indicator of error
            markers.add(YelpMarker(LatLng(0.0,0.0), "NETWORK ERROR", "0", false,"0" ))
            return markers
        }
        val gwResponseString = gwResponse.body?.string()


        if (!responseString.isNullOrEmpty()){
            val json = JSONObject(responseString)
            val businesses = json.getJSONArray("businesses")

            // add each restaurant's data to the list
            for (i in 0 until businesses.length()) {

                // get restaurant
                val restaurant = businesses.getJSONObject(i)

                // get restaurant coordinates
                val temp = restaurant.getJSONObject("coordinates")
                val rlat: Double? = temp.getDouble("latitude")
                val rlon: Double? = temp.getDouble("longitude")
                val coords: LatLng
                if (rlat == null || rlon == null){
                    // return an indicator of error
                    markers.add(YelpMarker(LatLng(0.0,0.0), "NETWORK ERROR", "0",false,"0" ))
                    return markers
                }
                else{
                    coords = LatLng(rlat, rlon)
                }

                // get name
                val name = restaurant.getString("name")
                // get rating
                val rating = restaurant.getString("rating")

                // get id
                val id = restaurant.getString("id")

                // cross-reference with gworld addresses to see if gworld compatible
                val location = restaurant.getJSONObject("location")
                val displayAddress = location.getJSONArray("display_address")
                val address = displayAddress.getString(0)
                val gwjson = JSONObject(gwResponseString)
                val gworldPlaces = gwjson.getJSONArray("gworld")
                var isGworld = false
                for (j in 0 until gworldPlaces.length()){
                    val gworldPlace = gworldPlaces.getJSONObject(j)
                    val gworldAdress = gworldPlace.getString("address")

                    if (address.equals(gworldAdress)){
                        isGworld = true
                    }
                }
                // make a new YelpMarker object to hold this information
                val yelpMarker = YelpMarker(coords, name, rating, isGworld, id)

                // add yelp marker to list
                markers.add(yelpMarker)
            }
            return markers
        }
        else {
            return emptyList()
        }
    }





    fun retrieveYelpReviews(context: Context, id: String):List<YelpReview>{

        val key = context.getString(R.string.yelp_key)

        val yelpReviews = mutableListOf<YelpReview>()
        // get reviews from api
        val yelpRequest = Request.Builder()
            .url("https://api.yelp.com/v3/businesses/$id/reviews")
            .header(
                "Authorization",
                "Bearer 8fWZHHQ9IZ6PbCWm0EVPA--GGHtr2uJTPB5-uioPljqUIm6qwMsP1t-XVrkD6inT_P1caNsPW-CkMc4TFFHHh5qeFflYnzLG7ExSFdVW2mcOwrgNY9eoIIly9Ai2XXYx"
            )
            .build()
        val response: Response
        try{
            response = okHttpClient.newCall(yelpRequest).execute()
        } catch (exception: UnknownHostException) {
            // return an indicator of error
            yelpReviews.add(YelpReview(-1.0f,"ERROR","ERROR", "ERROR"))
            return yelpReviews
        } catch (exception: IOException) {
            yelpReviews.add(YelpReview(-1.0f,"ERROR","ERROR", "ERROR"))
            return yelpReviews
        }
        val responseString = response.body?.string()

        // handle case where no reviews
        if (!responseString.isNullOrEmpty()){
            val json = JSONObject(responseString)
            val reviews = json.getJSONArray("reviews")

            // add each review's data to the list
            for (i in 0 until reviews.length()) {

                val review = reviews.getJSONObject(i)

                // get rating
                val rating = review.getDouble("rating").toFloat()

                // get review text
                val text = review.getString("text")

                // get author
                val user = review.getJSONObject("user")
                val author = user.getString("name")

                // get url
                val url = review.getString("url")

                // add data to list
                val yelpReview = YelpReview(rating, author, text, url)
                yelpReviews.add(yelpReview)
            }
            return yelpReviews
        }
        else {
            return emptyList()
        }


    }
}