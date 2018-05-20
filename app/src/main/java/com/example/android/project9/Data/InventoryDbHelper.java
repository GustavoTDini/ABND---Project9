package com.example.android.project9.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    /**
     * Nome do banco de Dados
     */
    private static final String DATABASE_NAME = "store.db";

    /**
     * Versão do banco de dados - ainda é a primeira
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Construtor de {@link InventoryDbHelper}.
     *
     * @param context of the app
     */
    public InventoryDbHelper(Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    /**
     * Usado na Criação do arquivo de banco de dados
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_CODE + " INTEGER NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_IMAGE + " BLOB, " + InventoryEntry.COLUMN_PRODUCT_SELL_VALUE + " REAL NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_BUY_VALUE + " REAL NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_STOCK + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL( SQL_CREATE_INVENTORY_TABLE );
    }

    /**
     * Methodo de Upgrade necessário para a classe - caso se decida modificar o banco de dados
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Mantido vazio pelo fato de ainda ser a primeira versão
    }
}
