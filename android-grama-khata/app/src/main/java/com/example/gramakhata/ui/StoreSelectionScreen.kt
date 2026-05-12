package com.example.gramakhata.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gramakhata.data.Store
import com.example.gramakhata.viewmodel.GramaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSelectionScreen(
    viewModel: GramaViewModel,
    onStoreSelected: (Store) -> Unit,
    onAddNewStore: () -> Unit
) {
    val stores by viewModel.allStores.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Store", fontWeight = FontWeight.Black) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNewStore, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Store", color = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (stores.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No stores found. Add your first store!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(stores) { store ->
                        StoreItem(store) { onStoreSelected(store) }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreItem(store: Store, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(48.dp).padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(store.shopName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(store.ownerName, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
