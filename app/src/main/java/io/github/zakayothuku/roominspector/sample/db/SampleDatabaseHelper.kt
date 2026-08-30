package io.github.zakayothuku.roominspector.sample.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory

object SampleDatabaseHelper {

    fun createAndSeedDatabase(context: Context): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("e_commerce_app.db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create Users Table
                    db.execSQL("""
                        CREATE TABLE users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL,
                            email TEXT NOT NULL,
                            role TEXT DEFAULT 'CUSTOMER',
                            wallet_balance REAL DEFAULT 0.0,
                            created_at INTEGER
                        )
                    """.trimIndent())

                    // Create Products Table
                    db.execSQL("""
                        CREATE TABLE products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sku TEXT UNIQUE NOT NULL,
                            title TEXT NOT NULL,
                            category TEXT NOT NULL,
                            price REAL NOT NULL,
                            stock_quantity INTEGER DEFAULT 0
                        )
                    """.trimIndent())

                    // Create Orders Table
                    db.execSQL("""
                        CREATE TABLE orders (
                            order_id TEXT PRIMARY KEY,
                            user_id INTEGER NOT NULL,
                            total_amount REAL NOT NULL,
                            order_status TEXT DEFAULT 'PENDING',
                            created_at INTEGER,
                            FOREIGN KEY(user_id) REFERENCES users(id)
                        )
                    """.trimIndent())

                    // Seed Users
                    db.execSQL("INSERT INTO users (username, email, role, wallet_balance, created_at) VALUES ('zakayo_thuku', 'zakayothuku@gmail.com', 'ADMIN', 15000.00, 1725000000)")
                    db.execSQL("INSERT INTO users (username, email, role, wallet_balance, created_at) VALUES ('alice_w', 'alice@company.com', 'CUSTOMER', 350.50, 1725010000)")
                    db.execSQL("INSERT INTO users (username, email, role, wallet_balance, created_at) VALUES ('bob_m', 'bob@developer.org', 'CUSTOMER', 85.00, 1725020000)")
                    db.execSQL("INSERT INTO users (username, email, role, wallet_balance, created_at) VALUES ('carol_d', 'carol@design.io', 'CUSTOMER', 1240.20, 1725030000)")

                    // Seed Products
                    db.execSQL("INSERT INTO products (sku, title, category, price, stock_quantity) VALUES ('PROD-001', 'Logitech MX Master 3S', 'Electronics', 99.99, 45)")
                    db.execSQL("INSERT INTO products (sku, title, category, price, stock_quantity) VALUES ('PROD-002', 'Keychron Q1 Pro Wireless Keyboard', 'Electronics', 199.00, 20)")
                    db.execSQL("INSERT INTO products (sku, title, category, price, stock_quantity) VALUES ('PROD-003', 'Sony WH-1000XM5 Headphones', 'Audio', 398.00, 15)")
                    db.execSQL("INSERT INTO products (sku, title, category, price, stock_quantity) VALUES ('PROD-004', 'Ergonomic Standing Desk 60x30', 'Furniture', 499.50, 8)")

                    // Seed Orders
                    db.execSQL("INSERT INTO orders (order_id, user_id, total_amount, order_status, created_at) VALUES ('ORD-88219', 1, 597.00, 'COMPLETED', 1725040000)")
                    db.execSQL("INSERT INTO orders (order_id, user_id, total_amount, order_status, created_at) VALUES ('ORD-88220', 2, 99.99, 'SHIPPED', 1725050000)")
                    db.execSQL("INSERT INTO orders (order_id, user_id, total_amount, order_status, created_at) VALUES ('ORD-88221', 3, 199.00, 'PROCESSING', 1725060000)")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        return helper.writableDatabase
    }
}
