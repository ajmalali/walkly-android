package com.walkly.walkly.ui.map


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.walkly.walkly.R
import com.walkly.walkly.models.Enemy
import com.walkly.walkly.models.Enemy.Companion.generateRandomEnemies
import com.walkly.walkly.models.Player
import com.walkly.walkly.offlineBattle.OfflineBattle
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlin.random.Random

class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener {
    lateinit var v : View
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapViewModel: MapViewModel
    private lateinit var linearLayout: LinearLayout
    private lateinit var mapboxMap: MapboxMap
    private lateinit var  symbol1: Symbol
    private lateinit var  symbol2: Symbol
    private lateinit var  symbol3: Symbol
    private lateinit var  camera: LatLng
    private lateinit var  enemies: Array<Enemy>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.access_token))
        v =  inflater.inflate(R.layout.fragment_map, container, false)
        return v
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Player.level.observe(this, Observer {
            user_level.text = "LEVEL $it"
        })

        Player.progress.observe(this, Observer {
            progressBar2.progress = it.toInt()
        })


        val btn_bg = join_button.background

        Player.stamina.observe(this, Observer {
            Log.d("stamina from map2", it.toString())

            join_button.isClickable = true
            join_button.background.alpha = 255

            if(it >= 300){
                //3 balls
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.VISIBLE
                stamina3full.visibility = View.VISIBLE

            }else if(it >= 200 ){
                //2 balls
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.VISIBLE
                stamina3full.visibility = View.INVISIBLE

            }else if(it >= 100){
                //1 ball
                stamina1full.visibility = View.VISIBLE
                stamina2full.visibility = View.INVISIBLE
                stamina3full.visibility = View.INVISIBLE

            }else{
                //no balls
                stamina1full.visibility = View.INVISIBLE
                stamina2full.visibility = View.INVISIBLE
                stamina3full.visibility = View.INVISIBLE

                // player cannot join a battle
                join_button.isClickable = false
                join_button.background.alpha = 100
            }

        })

        linearLayout = bottom_sheet
        //hide the bottom sheet
        BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_HIDDEN
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        join_button.setOnClickListener {
//            view.findNavController().navigate(R.id.action_navigation_map_to_Battle_Activity_Fragment)

            // decreasing energy on battle join
            Player.joinedBattle()

            val intent = Intent(activity, OfflineBattle::class.java)
            val bundle = Bundle()
            bundle.putString("enemyId", enemies.random().id.value)
            intent.putExtras(bundle)
            startActivity(intent)
            activity?.finish()
        }
    }
    override fun onMapReady(mapboxMap: MapboxMap) {
        enemies = generateRandomEnemies()
        this.mapboxMap = mapboxMap
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.uiSettings.isZoomGesturesEnabled = false
        mapboxMap.uiSettings.isQuickZoomGesturesEnabled = false
        mapboxMap.uiSettings.isScrollGesturesEnabled = false
        //mapboxMap.uiSettings.is
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7"))
        {
            // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            enableLocationComponent(it)
            val symbolManager = SymbolManager(mapView, mapboxMap, it)
            camera = mapboxMap.cameraPosition.target

            //this is where to generate icons
//            //TODO: create a function to automate the process
//            //TODO: find a way to use custom icons in the API
//            //TODO: how to link the icon with the battle instance?
//            symbol1 = symbolManager.create(
//                SymbolOptions()
//                .withLatLng(LatLng(camera.latitude+0.001, camera.longitude+0.001))
//                .withIconImage("zoo-15")
//                .withIconSize(2.5f))
//
//            symbol2 = symbolManager.create(SymbolOptions()
//                .withLatLng(LatLng(camera.latitude+0.0010, camera.longitude))
//                .withIconImage("fire-station-15")
//                .withIconSize(2.5f))
//
//            symbol3 = symbolManager.create(SymbolOptions()
//                .withLatLng(LatLng(camera.latitude, camera.longitude+0.001))
//                .withIconImage("rocket-15")
//                .withIconSize(2.5f))



            mapboxMap.addOnCameraMoveListener {
                Log.d("mapchange:", "onCameraMove")
                symbolManager.deleteAll()
                camera = mapboxMap.cameraPosition.target
                //TODO: create a function to automate the process
                //TODO: find a way to use custom icons in the API
                //TODO: how to link the icon with the battle instance?
                symbol1 = symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(camera.latitude+0.001, camera.longitude+0.001))
                        .withIconImage("zoo-15")
                        .withIconSize(2.5f))

                symbol2 = symbolManager.create(SymbolOptions()
                    .withLatLng(LatLng(camera.latitude+0.0010, camera.longitude))
                    .withIconImage("fire-station-15")
                    .withIconSize(2.5f))

                symbol3 = symbolManager.create(SymbolOptions()
                    .withLatLng(LatLng(camera.latitude, camera.longitude+0.001))
                    .withIconImage("rocket-15")
                    .withIconSize(2.5f))
                Log.d("mapchange:", camera.toString())


            }

            mapboxMap.addOnCameraMoveCancelListener {
                Log.d("mapchange:", "onCameraMoveCanceled")

            }

            mapboxMap.addOnCameraIdleListener {
                Log.d("idle:", "idle")
                enemies[0].name.observe(this, Observer {
                    Log.d("enmy:", it.toString())
                })


            }




            symbolManager?.addClickListener { symbol ->
                //for each battle icon on screen
                //if symbol.LatLng == Battles[i].LatLng
                //display dialogue box with battle details and prompt the user to start battle
                var curen = enemies[Random.nextInt(0,2)]
                curen.name.observe(this, Observer {
                    bottom_sheet_text.setText(it.toString())
                })
                curen.level.observe(this, Observer {
                    bottom_sheet_lvl.setText("Level: "+ it.toString())
                })
                curen.HP.observe(this, Observer {
                    bottom_sheet_health.setText("HP: "+it.toString())
                })

                //TODO: img here
                /*var image = ContextCompat.getDrawable(activity!!.applicationContext, R.drawable.zen)
                bottom_sheet_imageView.setImageDrawable(image)*/

                BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_COLLAPSED
                //Get the battle name from Battles[i] and set this variable to it
                //Same for the image
                //TV.setText(symbol.latLng.toString())
            }
            // }

        }

    }


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity!!.applicationContext)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(activity!!.applicationContext)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(activity!!.applicationContext, R.color.colorPrimary))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(activity!!.applicationContext, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(activity!!)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
       // Toast.makeText(this, "NOOOOOOOOOOOOOOOO", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
          Toast.makeText(activity!!.applicationContext, "YESSSSS", Toast.LENGTH_LONG).show()
          //finish()
        }
    }


}
