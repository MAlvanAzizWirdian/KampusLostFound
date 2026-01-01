package com.alpan.lostfound.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

object LocationUtil {

    fun getCurrentLocation(context: Context, onResult: (Double, Double) -> Unit) {
        val fused = LocationServices.getFusedLocationProviderClient(context)

        // The permission check is already handled in ReportActivity before calling this.
        // We still need to include it here for the linter.
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Use getCurrentLocation for a fresh location, not lastLocation.
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener {
                it?.let { loc ->
                    onResult(loc.latitude, loc.longitude)
                }
            }
    }

    fun openGoogleMaps(context: Context, lat: Double, lng: Double) {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
