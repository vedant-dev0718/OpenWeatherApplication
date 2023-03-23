package com.company.openweatherapplication.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.openweatherapplication.databinding.ViewholderBinding
import com.company.openweatherapplication.models.WeatherModel
import com.company.openweatherapplication.viewmodel.AdapterViewModel


class Adapter(
    context: Context,
    tempList: ArrayList<String>,
    owner: LifecycleOwner,
    owner2: ViewModelStoreOwner
) :
    RecyclerView.Adapter<Adapter.MyViewHolder>() {
    private val context: Context
    private var tempList: ArrayList<String>?
    private val owner: LifecycleOwner
    private val owner2: ViewModelStoreOwner

    init {
        this.context = context
        this.tempList = tempList
        this.owner = owner
        this.owner2 = owner2
    }

    fun setTempList(tempList: ArrayList<String>?) {
        this.tempList = tempList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(
            ViewholderBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        tempList?.get(position)?.let { holder.bind(it, owner, owner2) }
        Log.d("vedant",position.toString())
    }

    override fun getItemCount(): Int {
       return tempList!!.size
    }

    inner class MyViewHolder(private val binding: ViewholderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var viewmodel: AdapterViewModel

        fun bind(item: String, owner: LifecycleOwner, owner2: ViewModelStoreOwner) {
            viewmodel = ViewModelProvider(owner2)[AdapterViewModel::class.java]
            viewmodel.refreshData(item)
            
            val data = getLiveData(owner)
            
            if (data != null) {
                binding.tvCityCode.text = data.main.temp.toString() + "°C"
                binding.tvCityName.text = item

                Glide.with(context)
                    .load("https://openweathermap.org/img/wn/" + data.weather[0].icon + "@2x.png")
                    .into(binding.imgWeatherPictures)
            }
            

        }

        private fun getLiveData(owner: LifecycleOwner): WeatherModel? {
            var items: WeatherModel?=null

            viewmodel.weather_data.observe(owner, Observer { data ->
                data?.let {
                      items = it
                }
            })
            
            return items
        }

    }
}