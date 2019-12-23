package com.example.maptest

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync

class YelpActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_activity_yelp)
        setContentView(R.layout.activity_yelp)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // get the list of reviews. Networking done asynchronously
        doAsync {
            val id = intent.getStringExtra("ID")
            val networkManager = NetworkManager()
            val yelpReviews = networkManager.retrieveYelpReviews(this@YelpActivity, id)

            runOnUiThread{
                // handle error/empty
                if (yelpReviews.isEmpty() || yelpReviews.get(0).rating == -1.0f){
                    val builder = AlertDialog.Builder(this@YelpActivity)
                    builder.setTitle("There was a problem")
                    builder.setMessage("We could not find any review information for this establishment. Please Return to the map and try again")
                    builder.show()
                }
                else{
                    // set the adapter
                    recyclerView.adapter = YelpReviewsAdapter(yelpReviews)

                    // set up button and external intent
                    button = findViewById(R.id.button)
                    button.isEnabled = true
                    button.setOnClickListener {

                        // Create an Intent to open an external web browser to www.android.com
                        val uri: Uri = Uri.parse(yelpReviews.get(0).url)
                        val webIntent = Intent(Intent.ACTION_VIEW, uri)
                        // Execute the Intent - in this case, it will launch the user’s browser app
                        startActivity(webIntent)

                    }
                }

            }
        }




    }
}