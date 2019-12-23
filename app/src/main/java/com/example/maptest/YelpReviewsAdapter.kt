package com.example.maptest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class YelpReviewsAdapter (val reviews: List<YelpReview>) : RecyclerView.Adapter<YelpReviewsAdapter.ReviewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        // Open & parse our XML file
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_review, parent, false)

        // Create a new ViewHolder
        return ReviewsViewHolder(view)
    }

    // Returns the number of rows to render
    override fun getItemCount(): Int = reviews.size

    // onBindViewHolder is called when the RecyclerView is ready to display a new row at [position]
    // and needs you to fill that row with the necessary data.
    //
    // It passes you a ViewHolder, either from what you returned from onCreateViewHolder *or*
    // it's passing you an existing ViewHolder as a part of the "recycling" mechanism.
    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val currentReview = reviews[position]

        holder.rating.rating = currentReview.rating
        holder.name.text = currentReview.name
        holder.review.text = currentReview.review
    }

    // A ViewHolder is a class which *holds* references to *views* that we care about in each
    // individual row. The findViewById function is somewhat inefficient, so the idea is to the lookup
    // for each view once and then reuse the object.
    class ReviewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val rating: RatingBar = view.findViewById(R.id.ratingBar)
        val name: TextView = view.findViewById(R.id.author)
        val review: TextView = view.findViewById(R.id.review)
    }
}