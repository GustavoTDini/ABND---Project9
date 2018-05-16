package com.example.android.project9;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    public InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView inventoryListView  = findViewById( R.id.inventory_list_view );

        // Configura o empty list View
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView( emptyView );

        // Configura o Adapter do DB
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter( mCursorAdapter );

        //Configura o ClickListener para abrir o EditMode do Produto
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // abre o novo intent
                Intent EditIntent = new Intent( InventoryActivity.this, EditActivity.class );

                // Carrega o Uri que contem o item clicado e o carrega como data para o intent
                Uri productUri = ContentUris.withAppendedId( InventoryEntry.CONTENT_URI, id );
                EditIntent.setData( productUri );

                // Abre a nova Activity
                startActivity( EditIntent );
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case (R.id.add_sample_inventory):
                insertSampleInventory();
                break;
            case (R.id.empty_inventory):
                deleteInventory();
                break;
            case (R.id.inventory_summary):
                Intent summaryIntent = new Intent( InventoryActivity.this, SummaryActivity.class );
                startActivity( summaryIntent );
                break;
            case (R.id.add_new_product):
                Intent EditIntent = new Intent( InventoryActivity.this, EditActivity.class );
                startActivity( EditIntent );
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void insertSampleInventory() {

        insertItem( "Cupcake", 1,1.5, 1.5, 3 );
        insertItem( "Donut", 2,1.6, 1.6, 4 );
        insertItem( "Eclair", 3,2.1, 2, 5 );
        insertItem( "Frozen Yogurt", 4,2.3, 2.2, 8 );
        insertItem( "Gingerbread", 5,2.7, 2.3, 9 );
        insertItem( "Honeycomb", 6,3.2, 3.0, 11 );
        insertItem( "Ice Cream Sandwich", 7,4.4, 4.0, 14 );
        insertItem( "Jelly Bean", 8,4.3, 4.1, 16 );
        insertItem( "KitKat", 9,4.4, 4.4, 19 );
        insertItem( "Lollipop", 10,5.1, 5.0, 21 );
        insertItem( "Marshmallow", 11,6.0, 6.0, 23 );
        insertItem( "Nougat", 12,7.1, 7, 24 );
        insertItem( "Oreo", 13,8.1, 8.0, 26 );

    }

    public void insertItem(String name, int code, double sell, double buy, int stock) {

        ContentValues values = new ContentValues();
        values.put( InventoryEntry.COLUMN_PRODUCT_NAME, name );
        values.put( InventoryEntry.COLUMN_PRODUCT_CODE, code );
        values.put( InventoryEntry.COLUMN_PRODUCT_SELL_VALUE, sell );
        values.put( InventoryEntry.COLUMN_PRODUCT_BUY_VALUE, buy );
        values.put( InventoryEntry.COLUMN_PRODUCT_STOCK, stock );

        getContentResolver().insert( InventoryEntry.CONTENT_URI, values );
        Log.v("InventoryTAG", "insertItem: " + values);
    }

    /**
     * Método auxiliar para esvaziar o inventário
     */
    private void deleteInventory() {
        int rowsDeleted = getContentResolver().delete( InventoryEntry.CONTENT_URI, null, null );
        Log.v( "CatalogActivity", rowsDeleted + " rows deleted inventory Database" );
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
