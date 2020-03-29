package com.walkly.walkly.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Quest
import com.walkly.walkly.repositories.QuestsRepository
import kotlinx.android.synthetic.main.fragment_quests.*
import kotlinx.android.synthetic.main.list_quest.view.*

class QuestsFragment : Fragment(), QuestAdapter.QuestClickListener {

    private var MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION = System.identityHashCode(activity).and(0xFFF)

    private var quests: MutableList<Quest> = mutableListOf<Quest>()
    private lateinit var adapter: QuestAdapter
    private lateinit var locationManager: LocationManager

    private lateinit var locationListner: LocationListener


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quests, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        QuestsRepository.getQuests {
            quests = it.toMutableList()
            adapter = QuestAdapter(quests, this)
            quests_recycler_view.adapter = adapter
            initLocation()
        }

    }

    private fun initLocation() {

        locationManager = ContextCompat.getSystemService(context as Context, LocationManager::class.java) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this.context as Context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.context as Context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            Log.e("Location_ERROR", "permission is not granted")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this.activity as Activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACESS_FINE_LOCATION)
            }
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }

        locationListner  = object : LocationListener{
            override fun onLocationChanged(location: Location?) {
                if (location != null) {
                    quests.forEach {
                        it.calculateDistance(location)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                // no op
            }
            override fun onProviderEnabled(provider: String?) {
                Log.i(TAG, "$provider is enabled")
            }
            override fun onProviderDisabled(provider: String?) {
                Log.i(TAG, "$provider is disabled")
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5F, locationListner)
    }

    override fun onQuestClicker(postion: Int) {
        val bottomSheet = QuestBottomSheetDialog(
            quests[postion]
        ) {
            quests.remove(it)
            adapter.notifyDataSetChanged()
        }
        bottomSheet.show(parentFragmentManager, "qbs")
    }

    companion object{
        private const val TAG = "Quest Fragment"
    }
}
class QuestAdapter(private val quests: List<Quest>, val clickListener: QuestClickListener) : RecyclerView.Adapter<QuestViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_quest, parent, false)
        return QuestViewHolder(view, clickListener)
    }

    override fun getItemCount() = quests.size

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(quests[position])
    }
    interface QuestClickListener{
        fun onQuestClicker(postion: Int)
    }

}

class QuestViewHolder(view: View, val clickListener: QuestAdapter.QuestClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
    private val questName: TextView = view.tv_quest_name
    private val questDistance: TextView = view.tv_quest_distance

    init {
        view.setOnClickListener(this)
    }

    fun bind(quest: Quest){
        questName.text = quest.name
        questDistance.text = meter2text(quest.distance)
    }
    private fun meter2text(distance: Int) : String{
        if (distance > 1000){
            val d = distance / 1000.0
            return "$d KM"
        } else {
            return "$distance Meter"
        }
    }

    override fun onClick(v: View?) {
        clickListener.onQuestClicker(adapterPosition)
    }
}



