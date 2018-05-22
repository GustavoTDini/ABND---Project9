package com.example.android.project9;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;

public class SummaryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks <Cursor> {

    /**
     * Identificador do Loader
     */
    private static final int INVENTORY_LOADER = 0;

    private TextView mTotalProducts;
    private TextView mTotalCost;
    private TextView mTotalValue;
    private TextView mTotalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_summary );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        mTotalProducts = findViewById( R.id.summary_products_kinds );
        mTotalCost = findViewById( R.id.summary_cost_value );
        mTotalValue = findViewById( R.id.summary_sell_value );
        mTotalItems = findViewById( R.id.summary_total_items );

        getLoaderManager().initLoader( INVENTORY_LOADER, null, this );
    }

    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader( this, InventoryEntry.CONTENT_URI, null, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor cursor) {

        // separa as colunas do cursor para cada atributo e abre uma variavel para cada elemento
        int sellColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_SELL_VALUE );
        double sellValue;

        int buyColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_BUY_VALUE );
        double buyValue;

        int stockColumnIndex = cursor.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_STOCK );
        int stockValue;

        // formato para sistema monetário
        NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();

        //abre as variaveis dos dados que iremos utilizar
        int totalProductsValue = cursor.getCount();
        double totalCostValue = 0;
        double totalValueValue = 0;
        int totalItemsValue = 0;


        // move o cursor para o primeiro elemento e corre eles atraves de um for loop
        cursor.moveToFirst();
        for (int itemsIndex = 0; itemsIndex < totalProductsValue; itemsIndex++) {
            // atribui cada valor de cada linha do cursor
            stockValue = cursor.getInt( stockColumnIndex );
            sellValue = cursor.getDouble( sellColumnIndex ) * stockValue;
            buyValue = cursor.getDouble( buyColumnIndex ) * stockValue;
            // incrementa ao total de cada varivel o valor recebido
            totalItemsValue = totalItemsValue + stockValue;
            totalCostValue = totalCostValue + buyValue;
            totalValueValue = totalValueValue + sellValue;
            // passa para a proxima linha
            cursor.moveToNext();
        }

        // atribui a cada TextView
        mTotalProducts.setText( String.valueOf( totalProductsValue ) );
        mTotalCost.setText( moneyFormat.format( totalCostValue ) );
        mTotalValue.setText( moneyFormat.format( totalValueValue ) );
        mTotalItems.setText( String.valueOf( totalItemsValue ) );
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
    }
}
