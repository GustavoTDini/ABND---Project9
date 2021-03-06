package com.example.android.project9.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * {@link ContentProvider} para o DB Inventory.
 */
public class InventoryProvider extends ContentProvider {


    /**
     * Tag para as mensagens de LOG
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * Codigo estático para o URI de tabela inteira
     */
    private static final int INVENTORY = 100;

    /**
     * Codigo estático para o URI de um único Produto
     */
    private static final int INVENTORY_ID = 101;

    /**
     * Objeto UriMatcher para selecionar o respectivo código de URI
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // URI de seleção de tabela inteira ou multiplas linhas
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        // URI de seleção de linha unica de um produto da Tabela
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    /**
     * Método que verifica se já existe o produto na database
     */
    public static boolean checkIfContains(SQLiteDatabase readDatabase, String name, int code) {
        String nameQuery = "SELECT * FROM " + InventoryEntry.TABLE_NAME + " WHERE " + InventoryEntry.COLUMN_PRODUCT_NAME + " = ? OR " + InventoryEntry.COLUMN_PRODUCT_CODE + " = ?";
        Cursor cursor = readDatabase.rawQuery(nameQuery, new String[]{name, String.valueOf(code)});
        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        } else {
            cursor.close();
            return true;
        }
    }

    /**
     * Método para modificar a quantidade do estoque, usado no botão de venda, poderá ser utilizado em outras partes do app, pois
     * poderá ser modificado a quantidade modificada
     */
    static public Uri changeStock(Context context, int columnId, int quantity, int changeQuantity) {

        quantity = quantity + changeQuantity;

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_STOCK, quantity);

        Uri updateUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, columnId);

        int rowsAffected = context.getContentResolver().update(updateUri, values, null, null);

        return updateUri;
    }

    /**
     * Método para abrir um arquivo de imagem, retornando o Bitmap da mesma
     */
    static public Bitmap openImageFile(Context context, String imagePath) {
        // caminho to /data/data/yourapp/app_data/imageDir
        File directory = context.getDir("imageDir", Context.MODE_PRIVATE);
        try {
            // abre o arquivo de acordo com o caminho
            File imageFile = new File(directory, imagePath);
            // retorna a imagem em Bitmap a ser utilizada
            return BitmapFactory.decodeStream(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Método salvar um arquivo de imagem, retornando o caminho do arquivo
     */
    static public String saveImageFile(Context context, String productName, Bitmap productImage) {
        // caminho to /data/data/yourapp/app_data/imageDir
        File directory = context.getDir("imageDir", Context.MODE_PRIVATE);
        // cria um novo arquivo com o nome do produto
        String filename = productName + ".jpg";
        File file = new File(directory, filename);

        try {
            // salva em um bitmap usando um FileOutputStream
            FileOutputStream fileStream = new FileOutputStream(file);
            productImage.compress(Bitmap.CompressFormat.JPEG, 100, fileStream);
            fileStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // retorna o caminho para ser utilizado na DB
        return filename;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Faz o Query da Tabela conforme a selection
     * retorna o cursor da seleção
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        // Verifica se o URIMatcher é Valido, isto é se é ou INVENTORY ou INVENTORY_ID
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                //Retorna o Cursor da tabela inteira
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                //Retorna o Cursor especifico da produto do inventory
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // define o set notificationURI deste cursor, para identificarmos a troca de dados desta URI
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Apaga o Inventário conforme a selection, pode ser a tabela toda ou uma ou algumas linhas
     * retorna o numero de linhas apagadas
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Verifica o número de linhas apagadas
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Apaga ttodo o Inventário
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Apaga um ou vários produto do inventário baseado em seu ID e da selection
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // se alguma linha foi apagada a variavel rowsdeleted será diferente de 0 e será notificado a mudança de Uri
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertDb(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Método de Inserção do novo produto ao Inventário, vetifica se
     * Retorna e nova Uri, se o produto já existir atualiza com o incremento do estoque
     */
    private Uri insertDb(Uri uri, ContentValues values) {

        //Recebe os respectivos valores e os armazena
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        int code = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_CODE);
        double sellValue = values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE);
        double buyValue = values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE);
        int stockQuantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_STOCK);
        String imagepath = values.getAsString(InventoryEntry.COLUMN_PRODUCT_IMAGE);

        // cria o  database de escrita
        SQLiteDatabase writeDatabase = mDbHelper.getWritableDatabase();

        // abre o database de leitura
        SQLiteDatabase readDatabase = mDbHelper.getReadableDatabase();

        // Verifica se o nome e o codigo já existem na tabela, neste caso não é adicionado um novo produto
        if (checkIfContains(readDatabase, name, code)) {
            return null;
        } else {

            // Verifica se o nome não é Null
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }

            // Verifica se o Codigo não é Null
            if (code == 0) {
                throw new IllegalArgumentException("Product requires a code");
            }

            // Verifica se o Valor de venda é maior que 0
            if (!(sellValue >= 0)) {
                throw new IllegalArgumentException("Sell Value must be Greater than 0");
            }

            // Verifica se o Valor de compra é maior que 0
            if (!(buyValue >= 0)) {
                throw new IllegalArgumentException("Buy Value must be Greater than 0");
            }

            // Verifica se quantidade em estoque é maior ou igual a 0
            if (!(stockQuantity >= 0)) {
                throw new IllegalArgumentException("Stock quantity must be greater or equal 0");
            }

            // Se passar em todas as verificações insere un novo item no banco de dados
            long id = writeDatabase.insert(InventoryEntry.TABLE_NAME, null, values);

            // se o valor retornado for igual a -1, então a verificação falhou e irá retornar null e avisar no Log
            if (id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }

            // Notifica a mudança do Uri aos Listeners
            getContext().getContentResolver().notifyChange(uri, null);

            return ContentUris.withAppendedId(uri, id);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateDb(uri, values, selection, selectionArgs);
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDb(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Atualiza a Tabela com as mudanças solicitadas que podem ser em uma ou apenas em algumas linhas, definido pela selection
     * retorna o numero de linhas atualizadas
     */
    private int updateDb(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Verifica se o nome é presente e não é Null
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        // Verifica se o código é presente e não é Null
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_CODE)) {
            String code = values.getAsString(InventoryEntry.COLUMN_PRODUCT_CODE);
            if (code == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        // Verifica se o Valor de venda é presente e maior que 0
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE)) {
            double sellValue = values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE);
            if (!(sellValue > 0)) {
                throw new IllegalArgumentException("Sell Value must be Greater than 0");
            }
        }
        // Verifica se o Valor de compra é presente e maior que 0
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE)) {
            double buyValue = values.getAsDouble(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE);
            if (!(buyValue > 0)) {
                throw new IllegalArgumentException("Buy Value must be Greater than 0");
            }
        }
        // Verifica se a quantidade em estoque é presente e maior ou igual a 0
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_STOCK)) {
            int stockQuantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_STOCK);
            if (!(stockQuantity >= 0)) {
                throw new IllegalArgumentException("Stock quantity must be greater or equal 0");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Se passar em todas as verificações atualiza os itens no banco de dados
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // Se houver mudanças, isto é rows updated for diferente de 0, Notifica a mudança do Uri aos Listeners
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

}
