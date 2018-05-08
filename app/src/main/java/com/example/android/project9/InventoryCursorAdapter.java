package com.example.android.project9;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Infla a View relativa a um item da Lista
        return LayoutInflater.from(context).inflate(R.layout.inventory_product_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //Localiza os Views para serem populados
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView sellValueTextView = view.findViewById(R.id.sell_value);
        TextView buyValueTextView = view.findViewById(R.id.buy_value);
        TextView stockQunatityTextView = view.findViewById(R.id.stock_quantity);

        // separa as colunas do cursor para cada atributo e lê cada atributo
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        String productName = cursor.getString(nameColumnIndex);

        int sellColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE);
        String sellValue = cursor.getString(sellColumnIndex);

        int buyColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE);
        String buyValue = cursor.getString(buyColumnIndex);

        int stockColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_STOCK);
        String stockQuantity = cursor.getString(stockColumnIndex);

        // Atribui cada valor para o respectivo View
        nameTextView.setText(productName);
        sellValueTextView.setText(sellValue);
        buyValueTextView.setText(buyValue);
        stockQunatityTextView.setText(stockQuantity);

    }
}
