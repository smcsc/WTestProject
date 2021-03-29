package com.example.wtest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wtest.model.CodigoPostalModel
import kotlinx.android.synthetic.main.codigos_postais_rv.view.*

class CodigosPostaisAdapter(
    private var myData: ArrayList<CodigoPostalModel>,
    private val context: Context
) : RecyclerView.Adapter<CodigosPostaisAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = myData[position]
        holder.itemView.txtCodPostal.text = "${data.numCodPostal} - ${data.extCodPostal}"
        holder.itemView.txtDesigCodPostal.text = "${data.desigPostal}"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.codigos_postais_rv, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myData.size
    }

    fun updateList(list: ArrayList<CodigoPostalModel>){
        myData = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}