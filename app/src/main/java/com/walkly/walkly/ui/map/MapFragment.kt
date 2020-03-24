package com.walkly.walkly.ui.map


import android.annotation.SuppressLint
import android.content.Intent
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.JsonObject
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
import com.walkly.walkly.offlineBattle.OfflineBattleActivity
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

        val btn_bg = join_button.background

        linearLayout = bottom_sheet
        //hide the bottom sheet
        BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_HIDDEN
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }
    override fun onMapReady(mapboxMap: MapboxMap) {
        enemies = generateRandomEnemies(Player.level.value!!)
        this.mapboxMap = mapboxMap
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.uiSettings.isZoomGesturesEnabled = false
        mapboxMap.uiSettings.isQuickZoomGesturesEnabled = false
        mapboxMap.uiSettings.isScrollGesturesEnabled = false
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/mapbox/cjerxnqt3cgvp2rmyuxbeqme7"))
        {
            enableLocationComponent(it)
            val symbolManager = SymbolManager(mapView, mapboxMap, it)
            camera = mapboxMap.cameraPosition.target

            mapboxMap.addOnCameraMoveListener {
                Log.d("mapchange:", "onCameraMove")
                symbolManager.deleteAll()
                camera = mapboxMap.cameraPosition.target
                //TODO: create a function to automate the process
                //TODO: find a way to use custom icons in the API
                var enemy_1_json = JsonObject()
                enemy_1_json.addProperty("num",0)
                var enemy_2_json = JsonObject()
                enemy_2_json.addProperty("num",1)
                var enemy_3_json = JsonObject()
                enemy_3_json.addProperty("num",2)
                var latRandom = Random.nextDouble(-1.0, 1.0)/1000
                var lonRandom = Random.nextDouble(-1.0, 1.0)/1000
                symbol1 = symbolManager.create(
                    SymbolOptions()
                        .withData(enemy_1_json)
                        .withLatLng(LatLng(camera.latitude+ latRandom, camera.longitude+lonRandom))
                        .withIconImage("zoo-15")
                        .withIconSize(2.5f))


                symbol2 = symbolManager.create(
                    SymbolOptions()
                        .withData(enemy_2_json)
                        .withLatLng(LatLng(camera.latitude+latRandom, camera.longitude))
                        .withIconImage("fire-station-15")
                        .withIconSize(2.5f))

                symbol3 = symbolManager.create(
                    SymbolOptions()
                        .withData(enemy_3_json)
                        .withLatLng(LatLng(camera.latitude, camera.longitude+lonRandom))
                        .withIconImage("rocket-15")
                        .withIconSize(2.5f))
                Log.d("mapchange:", camera.toString())


            }

            mapboxMap.addOnCameraMoveCancelListener {
                Log.d("mapchange:", "onCameraMoveCanceled")

            }

            symbolManager?.addClickListener { symbol ->
                var enemy_num = symbol.data!!.asJsonObject.get("num").asInt
                var curen = enemies[enemy_num]
                join_button.setOnClickListener {

                    // decreasing energy on battle join
                    Player.joinedBattle()
                    val intent = Intent(activity, OfflineBattleActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString("enemyId", curen.id.value)
                    bundle.putLong("enemyHP", curen.HP.value!!)
                    bundle.putLong("enemyDmg", curen.damage.value!!)
                    bundle.putString("enemyLvl", curen.level.value.toString())
                    intent.putExtras(bundle)
                    startActivity(intent)
                    activity?.finish()
                }
                curen.name.observe(this, Observer {
                    bottom_sheet_text.setText(it.toString())
                })
                curen.level.observe(this, Observer {
                    bottom_sheet_lvl.setText("Level: "+ it.toString())
                })
                curen.HP.observe(this, Observer {
                    bottom_sheet_health.setText("HP: "+it.toString())
                })

                curen.id.observe(this, Observer {
                    val imagename = "boss" + it
                    bossgif.setImageResource(resources.getIdentifier(imagename,"drawable",  activity?.packageName))
                })

                curen.image.observe(this, Observer{
//                  TODO: there's a weird bug, simply passing the image name from the databse doesn't work, current fix is concatinating the enemy's id with its ID as seen in the preceding code block
//                  bossgif.setImageResource(resources.getIdentifier(it.toString(),"drawable",  activity?.packageName))

                })

                BottomSheetBehavior.from(linearLayout).state = BottomSheetBehavior.STATE_COLLAPSED
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
