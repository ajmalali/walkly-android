package com.walkly.walkly.models

import android.location.Location


class Enemy() {
    private var name: String = ""
    private var HP: Double = 0.0
    private var damage: Double = 0.0
    private var level: Int = 0
    private lateinit var location: Location

    init{
        //TODO: replace with database calls and player observer data
        this.name ="Doll Enemy"
        this.HP = 0.0
        this.damage = 0.0
        this.level = 0
        this.location.latitude= 0.0
        this.location.longitude  = 0.0
    }

    fun generateRandomEnemies(l : Location): Array<Enemy>{
        //get
        var enemy1: Enemy = Enemy()
        var enemy2: Enemy = Enemy()
        var enemy3: Enemy = Enemy()

        return arrayOf(enemy1, enemy2, enemy3)
    }

    fun setEnemyLocation(lat: Double, long: Double){
        this.location.latitude= lat
        this.location.longitude  = long
    }

    fun decreaseHP(damage: Double){
        this.HP -= damage
    }

}