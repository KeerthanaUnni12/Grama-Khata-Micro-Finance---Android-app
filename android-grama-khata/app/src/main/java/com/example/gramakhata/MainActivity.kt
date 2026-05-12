package com.example.gramakhata

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.gramakhata.data.*
import com.example.gramakhata.ui.*
import com.example.gramakhata.viewmodel.GramaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(this)
        val repository = GramaRepository(database.gramaDao())
        val gramaViewModel = GramaViewModel(repository)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF10B981),
                    onPrimary = Color.White,
                    secondary = Color(0xFF047857)
                )
            ) {
                GramaGate(gramaViewModel)
            }
        }
    }
}

@Composable
fun GramaGate(viewModel: GramaViewModel) {
    val stores by viewModel.allStores.collectAsState(initial = null)
    var appView by remember { mutableStateOf("splash") } // splash, store_selection, setup, pin, main
    var selectedStore by remember { mutableStateOf<Store?>(null) }

    LaunchedEffect(stores) {
        if (stores != null && appView == "splash") {
            kotlinx.coroutines.delay(1000)
            if (stores!!.isEmpty()) {
                appView = "setup"
            } else {
                appView = "store_selection"
            }
        }
    }

    when (appView) {
        "splash" -> SplashScreen()
        "store_selection" -> StoreSelectionScreen(
            viewModel = viewModel,
            onStoreSelected = { store ->
                selectedStore = store
                appView = "pin"
            },
            onAddNewStore = { appView = "setup" }
        )
        "setup" -> SetupScreen(
            onComplete = { name, owner, pin ->
                viewModel.createStore(name, owner, pin) { id ->
                    viewModel.selectStore(id)
                    appView = "main"
                }
            },
            onCancel = { 
                if (stores.isNullOrEmpty()) {
                    // Stay or handle differently if no stores
                } else {
                    appView = "store_selection"
                }
            }
        )
        "pin" -> PinScreen(
            correctPin = selectedStore?.pin ?: "",
            onVerified = {
                viewModel.selectStore(selectedStore?.id ?: 0L)
                appView = "main"
            }
        )
        "main" -> GramaApp(viewModel) {
            appView = "store_selection"
        }
    }
}

@Composable
fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("GRAMA-KHATA", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Digital Ledger System", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun GramaApp(viewModel: GramaViewModel, onSwitchStore: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(viewModel, navController, onSwitchStore) }
        composable("add_customer") { AddCustomerScreen(viewModel, navController) }
        composable("customer_detail/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId")?.toLong() ?: 0L
            CustomerDetailScreen(customerId, viewModel, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: GramaViewModel, navController: androidx.navigation.NavController, onSwitchStore: () -> Unit) {
    val currentStore by viewModel.currentStore.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCustomers = customers.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }.sortedByDescending { viewModel.getCustomerBalance(it.id, transactions) }

    val totalPending = customers.sumOf { 
        val bal = viewModel.getCustomerBalance(it.id, transactions)
        if (bal > 0) bal else 0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentStore?.shopName ?: "Store", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(currentStore?.ownerName ?: "Owner", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = onSwitchStore) {
                        Icon(Icons.Default.Logout, contentDescription = "Switch Store")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_customer") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Total Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Total Pending Amount", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text("₹$totalPending", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                placeholder = { Text("Search customer...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = TextFieldDefaults.textFieldColors(containerColor = Color(0xFFF3F4F6), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (filteredCustomers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No customers found", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredCustomers) { customer ->
                        val balance = viewModel.getCustomerBalance(customer.id, transactions)
                        CustomerItem(customer, balance) {
                            navController.navigate("customer_detail/${customer.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerItem(customer: Customer, balance: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFD1FAE5)), contentAlignment = Alignment.Center) {
                if (customer.photoUri != null) {
                    AsyncImage(model = customer.photoUri, contentDescription = null, contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF059669))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(customer.phone, color = Color.Gray, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (balance == 0.0) "₹0" else "₹${Math.abs(balance)}",
                    color = if (balance > 0) Color.Red else if (balance < 0) Color(0xFF059669) else Color.Gray,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = if (balance > 0) "Pending" else if (balance < 0) "Advance" else "Settled",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(viewModel: GramaViewModel, navController: androidx.navigation.NavController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Text("Add Customer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                viewModel.insertCustomer(name, phone)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = name.isNotEmpty() && phone.isNotEmpty()
        ) {
            Text("Save Customer")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(customerId: Long, viewModel: GramaViewModel, navController: androidx.navigation.NavController) {
    val customers by viewModel.customers.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currentStore by viewModel.currentStore.collectAsState()
    val customer = customers.find { it.id == customerId } ?: return
    val customerTransactions = transactions.filter { it.customerId == customerId }
    val balance = viewModel.getCustomerBalance(customerId, transactions)
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf<String?>(null) } // "CREDIT" or "PAYMENT"
    var amount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance > 0) Color(0xFFFEF2F2) else if (balance < 0) Color(0xFFECFDF5) else Color(0xFFF9FAFB)
                )
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CURRENT BALANCE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("₹${Math.abs(balance)}", fontSize = 48.sp, fontWeight = FontWeight.Black, color = if (balance > 0) Color.Red else Color(0xFF059669))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { 
                        val storeName = currentStore?.shopName ?: "Store"
                        val msg = "Namaskara ${customer.name}, your balance at $storeName is ₹$balance. Please settle soon."
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/91${customer.phone}?text=$msg")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1fr),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("WhatsApp")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Transactions", fontWeight = FontWeight.Bold, color = Color.Gray)
            
            LazyColumn(modifier = Modifier.weight(1fr), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(customerTransactions) { t ->
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(if (t.type == "CREDIT") "Credit +" else "Payment -", fontWeight = FontWeight.Bold, color = if (t.type == "CREDIT") Color.Red else Color(0xFF059669))
                            Text(java.text.SimpleDateFormat("dd MMM, hh:mm a").format(java.util.Date(t.timestamp)), fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("₹${t.amount}", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { showAddDialog = "PAYMENT" },
                    modifier = Modifier.weight(1fr).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) { Text("PAYMENT (-)") }
                Button(
                    onClick = { showAddDialog = "CREDIT" },
                    modifier = Modifier.weight(1fr).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("CREDIT (+)") }
            }
        }
    }
    
    if (showAddDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = null },
            title = { Text("Add $showAddDialog") },
            text = {
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = { Text("Enter amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addTransaction(customerId, amount.toDoubleOrNull() ?: 0.0, showAddDialog!!)
                    showAddDialog = null
                    amount = ""
                }) { Text("Confirm") }
            }
        )
    }
}
