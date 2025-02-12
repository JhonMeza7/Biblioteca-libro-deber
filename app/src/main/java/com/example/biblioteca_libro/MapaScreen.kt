package com.example.biblioteca_libro

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.biblioteca_libro.model.BibliotecaDao
import com.example.biblioteca_libro.model.BibliotecaEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapaScreen(bibliotecaId: Int, bibliotecaDao: BibliotecaDao, navController: NavHostController) {
    val biblioteca by bibliotecaDao.getBibliotecaById(bibliotecaId).collectAsState(initial = null)

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Ubicación Biblioteca") }) }
    ) { innerPadding ->
        if (biblioteca != null && biblioteca!!.latitud != null && biblioteca!!.longitud != null) {
            val ubicacion = LatLng(biblioteca!!.latitud!!, biblioteca!!.longitud!!)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(ubicacion, 15f)
            }

            GoogleMap(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = rememberMarkerState(position = ubicacion),
                    title = biblioteca!!.nombre,
                    snippet = biblioteca!!.direccion
                )
            }
        } else {
            Text("No hay datos de ubicación disponibles", modifier = Modifier.padding(16.dp))
        }
    }
}
