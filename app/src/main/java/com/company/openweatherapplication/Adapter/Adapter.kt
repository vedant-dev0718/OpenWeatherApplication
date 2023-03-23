package com.company.openweatherapplication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.openweatherapplication.databinding.ViewholderBinding
import com.company.openweatherapplication.viewmodel.MainViewModel


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
    }

    override fun getItemCount(): Int {
        return if (tempList != null) {
            tempList!!.size
        } else 0
    }

    inner class MyViewHolder(private val binding: ViewholderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private lateinit var viewmodel: MainViewModel

        fun bind(item: String, owner: LifecycleOwner, owner2: ViewModelStoreOwner) {
            viewmodel = ViewModelProvider(owner2)[MainViewModel::class.java]

            viewmodel.refreshData(item)
            getLiveData(owner)
        }

        private fun getLiveData(owner: LifecycleOwner) {

            viewmodel.weather_data.observe(owner, Observer { data ->
                data?.let {

                    binding.tvCityCode.text = data.main.temp.toString() + "°C"
                    binding.tvCityName.text = data.name

                    Glide.with(context)
                        .load("https://openweathermap.org/img/wn/" + data.weather[0].icon + "@2x.png")
                        .into(binding.imgWeatherPictures)

                }
            })
        }

    }
}