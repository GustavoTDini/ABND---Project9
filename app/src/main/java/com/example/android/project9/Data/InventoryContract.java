package com.example.android.project9.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    // Construtor Vazio para esta classe não ser acessada erroneamente
    private InventoryContract() {}

    /**
     * O "Content authority" para o content provider deste contract
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.project9";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "inventory";

    /**
     * Classe que irá representar cada mercadoria de em uma tabela que irá ter como dados o nome da mercadoria,
     * o valor de compra e de venda e o número em estoque
     */
    public static final class InventoryEntry implements BaseColumns {

        /** o Uti para acessar os dados do inventario no provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * O MIME para o inventário
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * O MIME para um único produto
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        /** Name of database table for products */
        public final static String TABLE_NAME = "inventory";

        /**
         * ID único do produto
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Nome do Produto
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Valor de Venda
         *
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_SELL_VALUE = "sell";

        /**
         * Valor de Compra
         *
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_BUY_VALUE = "buy";

        /**
         * Valor de Venda
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_STOCK = "stock";

    }
}
