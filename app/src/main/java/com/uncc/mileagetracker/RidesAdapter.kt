package com.uncc.mileagetracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.uncc.mileagetracker.Models.Ride

class RidesAdapter : BaseAdapter {

    private var ridessList = ArrayList<Ride>()
    private var context: Context? = null

    constructor(context: Context, usersList: ArrayList<Ride>) : super() {
        this.ridessList = usersList
        this.context = context
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val vh: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(parent!!.getContext()).inflate(R.layout.rides_list_item, parent, false);
            vh = ViewHolder(view)
            view!!.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        vh.tvRideId.text = ridessList[position].id

        return view
    }

    override fun getItem(position: Int): Any {
        return ridessList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return ridessList.size
    }
}

private class ViewHolder(view: View?) {
    val tvRideId: TextView

    init {
        this.tvRideId = view?.findViewById(R.id.rideIdListItem)!!
    }
}