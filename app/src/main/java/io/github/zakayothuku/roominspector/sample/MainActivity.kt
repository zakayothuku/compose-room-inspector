package io.github.zakayothuku.roominspector.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.zakayothuku.roominspector.ComposeRoomInspector
import io.github.zakayothuku.roominspector.sample.db.SampleDatabaseHelper
import io.github.zakayothuku.roominspector.ui.ComposeRoomInspectorOverlay
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private lateinit var database: SupportSQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SQLite/Room database
        database = SampleDatabaseHelper.createAndSeedDatabase(applicationContext)

        // Register Database into Compose Room Inspector
        ComposeRoomInspector.registerDatabase("E-Commerce DB", database)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SampleDatabaseContent(database = database)

                        // Attach floating Room Inspector Overlay
                        ComposeRoomInspectorOverlay()
                    }
                }
            }
        }
    }
}

@Composable
fun SampleDatabaseContent(database: SupportSQLiteDatabase) {
    var statusMessage by remember { mutableStateOf("Tap a button below to mutate the local database in real-time.") }
    var insertedCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🗄️ Compose Room Inspector",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Browse tables, inspect column schemas, and execute raw SQL on-device.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                insertedCount++
                val randomSku = "PROD-GEN-${Random.nextInt(1000, 9999)}"
                val price = (Random.nextDouble(10.0, 500.0) * 100).toInt() / 100.0
                database.execSQL("INSERT INTO products (sku, title, category, price, stock_quantity) VALUES ('$randomSku', 'Automated Test Product #$insertedCount', 'Gadgets', $price, 50)")
                statusMessage = "✅ Inserted Product: $randomSku ($$price)"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Insert Random Product Record")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val randomId = Random.nextInt(100, 999)
                database.execSQL("INSERT INTO users (username, email, role, wallet_balance, created_at) VALUES ('user_$randomId', 'user$randomId@test.com', 'CUSTOMER', 100.00, ${System.currentTimeMillis() / 1000})")
                statusMessage = "✅ Inserted User: user_$randomId"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Insert Random User Record")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val deleted = database.execSQL("DELETE FROM orders WHERE order_status = 'PENDING'")
                statusMessage = "🗑️ Cleared pending orders from database."
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🗑️ Delete Pending Orders")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = statusMessage,
                modifier = Modifier.padding(14.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
