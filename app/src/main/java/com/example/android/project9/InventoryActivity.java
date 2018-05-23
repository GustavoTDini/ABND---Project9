package com.example.android.project9;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.android.project9.Data.InventoryProvider;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;

    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView inventoryListView = findViewById(R.id.inventory_list_view);

        // Configura o empty list View
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        // Configura o Adapter do DB
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);

        //Configura o ClickListener para abrir o EditMode do Produto
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v("InventoryActivity", "ID " + id);
                // abre o novo intent
                Intent editIntent = new Intent(InventoryActivity.this, EditActivity.class);

                // Carrega o Uri que contem o item clicado e o carrega como data para o intent
                Uri productUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                Log.v("InventoryActivity", "setOnClikID " + productUri);
                editIntent.setData(productUri);

                // Abre a nova Activity
                startActivity(editIntent);
            }
        });

        // inicia o loader
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

        switch (id) {
            case (R.id.add_sample_inventory):
                insertSampleInventory();
                break;
            case (R.id.empty_inventory):
                deleteInventory();
                break;
            case (R.id.inventory_summary):
                Intent summaryIntent = new Intent(InventoryActivity.this, SummaryActivity.class);
                startActivity(summaryIntent);
                break;
            case (R.id.add_new_product):
                Intent EditIntent = new Intent(InventoryActivity.this, EditActivity.class);
                startActivity(EditIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // metodo para adicionar um Invenatário de exemplo
    public void insertSampleInventory() {

        insertItem("Cupcake", 1, 1.5, 1.5, 3, R.drawable.cupcake);
        insertItem("Donut", 2, 1.6, 1.6, 4, R.drawable.donut);
        insertItem("Eclair", 3, 2.1, 2, 5, R.drawable.eclair);
        insertItem("Frozen Yogurt", 4, 2.3, 2.2, 8, R.drawable.froyo);
        insertItem("Gingerbread", 5, 2.7, 2.3, 9, R.drawable.gingerbread);
        insertItem("Honeycomb", 6, 3.2, 3.0, 11, R.drawable.honeycomb);
        insertItem("Ice Cream Sandwich", 7, 4.4, 4.0, 14, R.drawable.icecreamsandwich);
        insertItem("Jelly Bean", 8, 4.3, 4.1, 16, R.drawable.jellybean);
        insertItem("KitKat", 9, 4.4, 4.4, 19, R.drawable.kitkat);
        insertItem("Lollipop", 10, 5.1, 5.0, 21, R.drawable.lollipop);
        insertItem("Marshmallow", 11, 6.0, 6.0, 23, R.drawable.marshmallow);
        insertItem("Nougat", 12, 7.1, 7, 24, R.drawable.nougat);
        insertItem("Oreo", 13, 8.1, 8.0, 26, R.drawable.oreo);

    }

    // metodo para adicionar um produto de exemplo
    public void insertItem(String name, int code, double sell, double buy, int stock, int imageId) {

        Bitmap productBitmap = BitmapFactory.decodeResource(getResources(), imageId);
        String imagePathString = InventoryProvider.saveImageFile(this, name, productBitmap);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_PRODUCT_CODE, code);
        values.put(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE, sell);
        values.put(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE, buy);
        values.put(InventoryEntry.COLUMN_PRODUCT_STOCK, stock);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, imagePathString);
        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

    }

    /**
     * Método auxiliar para esvaziar o inventário
     */
    private void deleteInventory() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted inventory Database");
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
