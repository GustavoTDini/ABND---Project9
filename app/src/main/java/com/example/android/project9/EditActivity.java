package com.example.android.project9;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;
import java.util.Locale;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks <Cursor> {

    /**
     * Identificador do Loader
     */
    private static final int INVENTORY_LOADER = 0;

    /**
     * Uri para o produto para edição - null se for adicionar novo item
     */
    private Uri mCurrentProductUri;

    private EditText mProductNameView;
    private EditText mProductCodeView;
    private EditText mProductBuyView;
    private EditText mProductSellView;
    private EditText mProductStockView;
    private ImageView mProductImageView;
    private Button mDeleteButton;
    private Button mEditImageButton;

    /**
     * Variavel para Verificar se houve mudança na Edição
     */
    private boolean mHasChanges = false;

    /**
     * OnTouchListener que verifica se houve toque na tela e consequente mudança na Edição
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHasChanges = true;
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDialogAsNavigateUp();
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_edit );
        Toolbar toolbar = findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        // Verifica se a Intent que foi usada para abrir esta activity contém dados
        // que indicam o uso para edição de um produto.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mDeleteButton = findViewById( R.id.delete_button );
        mProductNameView = findViewById( R.id.edit_name );
        mProductCodeView = findViewById( R.id.edit_code );
        mProductBuyView = findViewById( R.id.edit_buy );
        mProductSellView = findViewById( R.id.edit_sell );
        mProductStockView = findViewById( R.id.edit_stock );
        mProductImageView = findViewById( R.id.edit_image );
        mEditImageButton = findViewById( R.id.edit_image_button );

        // Verifica se a Activity está sendo usada para editar ou adicionar um produto e
        // modifica o Titulo, mostra ou não o botão de Apagar
        if (mCurrentProductUri == null) {
            setTitle( R.string.edit_new_product_title );
            mDeleteButton.setVisibility( View.GONE );
        } else {
            mDeleteButton.setVisibility( View.VISIBLE );
            getLoaderManager().initLoader( INVENTORY_LOADER, null, this );
        }

        mProductNameView.setOnTouchListener( mTouchListener );
        mProductCodeView.setOnTouchListener( mTouchListener );
        mProductBuyView.setOnTouchListener( mTouchListener );
        mProductSellView.setOnTouchListener( mTouchListener );
        mProductStockView.setOnTouchListener( mTouchListener );
        mEditImageButton.setOnTouchListener( mTouchListener );

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEditChanges();
                finish();
            }
        } );

        mDeleteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        } );
    }

    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader( this, mCurrentProductUri, null, null, null, null );
    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();

            // separa as colunas do cursor para cada atributo e lê cada atributo
            int nameColumnIndex = data.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_NAME );
            final String productName = data.getString( nameColumnIndex );

            int codeColumnIndex = data.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_CODE );
            String productCode = String.format( Locale.getDefault(), "%06d", Integer.parseInt( data.getString( codeColumnIndex ) ) );

            int sellColumnIndex = data.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_SELL_VALUE );
            String sellValue = moneyFormat.format( data.getDouble( sellColumnIndex ) );

            int buyColumnIndex = data.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_BUY_VALUE );
            String buyValue = moneyFormat.format( data.getDouble( buyColumnIndex ) );

            int stockColumnIndex = data.getColumnIndex( InventoryEntry.COLUMN_PRODUCT_STOCK );
            String stockQuantity = data.getString( stockColumnIndex );

            // Atribui cada valor para o respectivo View
            setTitle( String.format( getResources().getString( R.string.edit_edit_product_title ), productName ) );
            mProductNameView.setText( productName );
            mProductCodeView.setText( productCode );
            mProductSellView.setText( sellValue );
            mProductBuyView.setText( buyValue );
            mProductStockView.setText( stockQuantity );
        }
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        mProductNameView.setText( "" );
        mProductCodeView.setText( "" );
        mProductBuyView.setText( "" );
        mProductSellView.setText( "" );
        mProductStockView.setText( "" );
        mProductImageView.setImageResource( R.drawable.image_placeholder );
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.edit_changes_dialog_message );
        builder.setPositiveButton( R.string.edit_changes_dialog_discard, discardButtonClickListener );
        builder.setNegativeButton( R.string.edit_changes_dialog_keep, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        openDialogAsNavigateUp();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( String.format( getResources().getString( R.string.edit_dialog_delete_message, mProductNameView.getText().toString() ) ) );
        builder.setPositiveButton( R.string.edit_dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        } );
        builder.setNegativeButton( R.string.edit_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete( mCurrentProductUri, null, null );
            if (rowsDeleted == 0) {
                Toast.makeText( this, String.format( getResources().getString( R.string.edit_delete_error_message, mProductNameView.getText().toString() ) ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, String.format( getResources().getString( R.string.edit_delete_success_message, mProductNameView.getText().toString() ) ), Toast.LENGTH_SHORT ).show();
            }
            finish();
        }
    }

    private void openDialogAsNavigateUp() {
        if (!mHasChanges) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog( discardButtonClickListener );
    }

    private void saveEditChanges() {

        String nameString = mProductNameView.getText().toString().trim();
        String codeString = mProductCodeView.getText().toString().trim();
        String buyString = mProductBuyView.getText().toString().trim().replaceAll( "[^\\d.]", "" );
        String sellString = mProductSellView.getText().toString().trim().replaceAll( "[^\\d.]", "" );
        String stockString = mProductStockView.getText().toString().trim();
        int codeInt = 0;
        double buyDouble = 0;
        double sellDouble = 0;
        int stockInt = 0;

        if (mCurrentProductUri == null && TextUtils.isEmpty( nameString ) && TextUtils.isEmpty( codeString ) && TextUtils.isEmpty( buyString ) && TextUtils.isEmpty( sellString ) && TextUtils.isEmpty( stockString )) {
            return;
        }

        if (!codeString.isEmpty()) {
            codeInt = Integer.parseInt( codeString );
        }
        if (!buyString.isEmpty()) {
            buyDouble = Double.parseDouble( buyString );
        }
        if (!sellString.isEmpty()) {
            sellDouble = Double.parseDouble( sellString );
        }
        if (!stockString.isEmpty()) {
            stockInt = Integer.parseInt( stockString );
        }

        ContentValues values = new ContentValues();
        values.put( InventoryEntry.COLUMN_PRODUCT_NAME, nameString );
        values.put( InventoryEntry.COLUMN_PRODUCT_CODE, codeInt );
        values.put( InventoryEntry.COLUMN_PRODUCT_BUY_VALUE, buyDouble );
        values.put( InventoryEntry.COLUMN_PRODUCT_SELL_VALUE, sellDouble );
        values.put( InventoryEntry.COLUMN_PRODUCT_STOCK, stockInt );


        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert( InventoryEntry.CONTENT_URI, values );

            if (newUri == null) {
                Toast.makeText( this, getString( R.string.edit_insert_error_message, nameString ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.edit_insert_success_message, nameString ), Toast.LENGTH_SHORT ).show();
            }
        } else {

            int rowsAffected = getContentResolver().update( mCurrentProductUri, values, null, null );

            if (rowsAffected == 0) {
                Toast.makeText( this, getString( R.string.edit_update_error_message, nameString ), Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.edit_update_success_message, nameString ), Toast.LENGTH_SHORT ).show();
            }
        }
    }

}
