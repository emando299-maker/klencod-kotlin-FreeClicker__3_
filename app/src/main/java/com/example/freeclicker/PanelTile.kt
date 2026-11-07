package com.example.freeclicker

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class PanelTile : TileService() {
    private val prefs get() = getSharedPreferences("cfg", MODE_PRIVATE)

    override fun onStartListening() { refresh() }

    override fun onClick() {
        prefs.edit().putBoolean("panelVisible", !prefs.getBoolean("panelVisible", true)).apply()
        refresh()
    }

    private fun refresh() {
        val t = qsTile ?: return
        t.label = "Clicker panel"
        t.state = if (prefs.getBoolean("panelVisible", true)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        t.updateTile()
    }
}
