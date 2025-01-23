package com.example.biblioteca_libro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.biblioteca_libro.model.Biblioteca
import com.example.biblioteca_libro.ui.theme.BibliotecaLibroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BibliotecaLibroTheme {
                val navController = rememberNavController()
                val bibliotecas = remember {
                    mutableStateListOf(
                        Biblioteca(1, "Biblioteca Central", "Calle 123", "2000-01-01", true),
                        Biblioteca(2, "Biblioteca Secundaria", "Calle 456", "2010-05-15", false)
                    )
                }
                val libros = remember {
                    mutableStateListOf<Pair<Int, MutableList<String>>>(
                        Pair(1, mutableListOf("Kotlin Básico", "Android Avanzado")),
                        Pair(2, mutableListOf("Java para Principiantes", "Estructuras de Datos"))
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = "bibliotecas"
                ) {
                    composable("bibliotecas") {
                        BibliotecaList(navController, bibliotecas)
                    }
                    composable("libros/{bibliotecaId}") { backStackEntry ->
                        val bibliotecaId = backStackEntry.arguments?.getString("bibliotecaId")?.toInt()
                        LibroList(bibliotecaId, navController, libros)
                    }
                    composable("crearBiblioteca") {
                        CrearBibliotecaScreen(
                            onGuardar = { nuevaBiblioteca ->
                                bibliotecas.add(nuevaBiblioteca)
                                navController.popBackStack()
                            },
                            onCancelar = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("crearLibro/{bibliotecaId}") { backStackEntry ->
                        val bibliotecaId = backStackEntry.arguments?.getString("bibliotecaId")?.toInt()
                        CrearLibroScreen(
                            bibliotecaId = bibliotecaId ?: 0,
                            onGuardar = { id, titulo, autor ->
                                val bibliotecaLibros = libros.find { it.first == id }
                                bibliotecaLibros?.second?.add("$titulo - $autor")
                                navController.popBackStack()
                            },
                            onCancelar = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("editarBiblioteca/{bibliotecaId}") { backStackEntry ->
                        val bibliotecaId = backStackEntry.arguments?.getString("bibliotecaId")?.toInt()
                        val biblioteca = bibliotecas.find { it.id == bibliotecaId }
                        if (biblioteca != null) {
                            EditarBibliotecaScreen(
                                biblioteca = biblioteca,
                                onGuardar = { bibliotecaEditada ->
                                    val index = bibliotecas.indexOfFirst { it.id == bibliotecaEditada.id }
                                    if (index != -1) {
                                        bibliotecas[index] = bibliotecaEditada
                                    }
                                    navController.popBackStack()
                                },
                                onCancelar = {
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            // Manejar el caso de un bibliotecaId inválido
                            navController.popBackStack()
                        }
                    }

                    composable("editarLibro/{bibliotecaId}/{libroId}") { backStackEntry ->
                        val libroId = backStackEntry.arguments?.getString("libroId") ?: ""
                        val bibliotecaId = backStackEntry.arguments?.getString("bibliotecaId")?.toInt() ?: 0
                        val bibliotecaLibros = libros.find { it.first == bibliotecaId }?.second

                        if (bibliotecaLibros != null && libroId.isNotEmpty()) {
                            val tituloInicial = libroId.split(" - ").getOrNull(0) ?: ""
                            val autorInicial = libroId.split(" - ").getOrNull(1) ?: ""

                            EditarLibroScreen(
                                libroId = libroId.hashCode(),
                                tituloInicial = tituloInicial,
                                autorInicial = autorInicial,
                                onGuardar = { _, titulo, autor ->
                                    bibliotecaLibros.remove(libroId)
                                    bibliotecaLibros.add("$titulo - $autor")
                                    navController.popBackStack()
                                },
                                onCancelar = {
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            // Manejar el caso de un bibliotecaId o libroId inválido
                            navController.popBackStack()
                        }
                    }

                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibliotecaList(navController: NavHostController, bibliotecas: MutableList<Biblioteca>) {
    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Bibliotecas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("crearBiblioteca")
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bibliotecas) { biblioteca ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(biblioteca.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(biblioteca.direccion, style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                navController.navigate("libros/${biblioteca.id}")
                            }) {
                                Text("Ver")
                            }
                            Button(onClick = {
                                navController.navigate("editarBiblioteca/${biblioteca.id}")
                            }) {
                                Text("Editar")
                            }
                            Button(onClick = {
                                bibliotecas.remove(biblioteca)
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }




                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibroList(
    bibliotecaId: Int?,
    navController: NavHostController,
    libros: MutableList<Pair<Int, MutableList<String>>>
) {
    // Encuentra los libros de la biblioteca específica
    val bibliotecaLibros = libros.find { it.first == bibliotecaId }?.second ?: mutableListOf()

    // Utiliza un estado mutable para rastrear los libros
    val librosBiblioteca = remember { mutableStateListOf(*bibliotecaLibros.toTypedArray()) }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Libros") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("crearLibro/$bibliotecaId")
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(librosBiblioteca) { libro ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(libro)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                navController.navigate("editarLibro/$bibliotecaId/$libro")
                            }) {
                                Text("Editar")
                            }
                            Button(onClick = {
                                // Eliminar el libro y actualizar la lista
                                librosBiblioteca.remove(libro)
                                val index = libros.indexOfFirst { it.first == bibliotecaId }
                                if (index != -1) {
                                    libros[index] = Pair(
                                        bibliotecaId!!,
                                        librosBiblioteca.toMutableList()
                                    )
                                }
                            }) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearBibliotecaScreen(onGuardar: (Biblioteca) -> Unit, onCancelar: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var fechaInauguracion by remember { mutableStateOf("") }
    var abiertaAlPublico by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Nueva Biblioteca") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") }
            )
            TextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") }
            )
            TextField(
                value = fechaInauguracion,
                onValueChange = { fechaInauguracion = it },
                label = { Text("Fecha de Inauguración (YYYY-MM-DD)") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = abiertaAlPublico,
                    onCheckedChange = { abiertaAlPublico = it }
                )
                Text("Abierta al Público")
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onCancelar) {
                    Text("Cancelar")
                }
                Button(onClick = {
                    val nuevaBiblioteca = Biblioteca(
                        id = (0..1000).random(),
                        nombre = nombre,
                        direccion = direccion,
                        fechaInauguracion = fechaInauguracion,
                        abiertaAlPublico = abiertaAlPublico
                    )
                    onGuardar(nuevaBiblioteca)
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarBibliotecaScreen(
    biblioteca: Biblioteca,
    onGuardar: (Biblioteca) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(biblioteca.nombre) }
    var direccion by remember { mutableStateOf(biblioteca.direccion) }
    var fechaInauguracion by remember { mutableStateOf(biblioteca.fechaInauguracion) }
    var abiertaAlPublico by remember { mutableStateOf(biblioteca.abiertaAlPublico) }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Editar Biblioteca") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") }
            )
            TextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") }
            )
            TextField(
                value = fechaInauguracion,
                onValueChange = { fechaInauguracion = it },
                label = { Text("Fecha de Inauguración (YYYY-MM-DD)") }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = abiertaAlPublico,
                    onCheckedChange = { abiertaAlPublico = it }
                )
                Text("Abierta al Público")
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onCancelar) {
                    Text("Cancelar")
                }
                Button(onClick = {
                    val bibliotecaEditada = biblioteca.copy(
                        nombre = nombre,
                        direccion = direccion,
                        fechaInauguracion = fechaInauguracion,
                        abiertaAlPublico = abiertaAlPublico
                    )
                    onGuardar(bibliotecaEditada)
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearLibroScreen(
    bibliotecaId: Int,
    onGuardar: (Int, String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Nuevo Libro") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Libro") }
            )
            TextField(
                value = autor,
                onValueChange = { autor = it },
                label = { Text("Autor del Libro") }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onCancelar) {
                    Text("Cancelar")
                }
                Button(onClick = {
                    onGuardar(bibliotecaId, titulo, autor)
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarLibroScreen(
    libroId: Int,
    tituloInicial: String,
    autorInicial: String,
    onGuardar: (Int, String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf(tituloInicial) }
    var autor by remember { mutableStateOf(autorInicial) }

    Scaffold(
        topBar = { SmallTopAppBar(title = { Text("Editar Libro") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del Libro") }
            )
            TextField(
                value = autor,
                onValueChange = { autor = it },
                label = { Text("Autor del Libro") }
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onCancelar) {
                    Text("Cancelar")
                }
                Button(onClick = {
                    onGuardar(libroId, titulo, autor)
                }) {
                    Text("Guardar")
                }
            }
        }
    }
}

