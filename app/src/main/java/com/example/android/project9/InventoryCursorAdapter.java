package com.example.android.project9;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;
import com.example.android.project9.Data.InventoryProvider;

import java.text.NumberFormat;
import java.util.Locale;

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
    public void bindView(View view, final Context context, final Cursor cursor) {

        //Localiza os Views para serem populados
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView codeTextView = view.findViewById(R.id.code_value);
        TextView sellValueTextView = view.findViewById(R.id.sell_value);
        TextView buyValueTextView = view.findViewById(R.id.buy_value);
        TextView stockQuantityTextView = view.findViewById(R.id.stock_quantity);
        ImageView imageThumbNailView = view.findViewById(R.id.product_image);
        Button sellButton = view.findViewById(R.id.sell_button);

        NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

        // separa as colunas do cursor para cada atributo e lê cada atributo
        int IdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        final int columnId = cursor.getInt(IdColumnIndex);

        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        final String productName = cursor.getString(nameColumnIndex);

        int codeColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_CODE);
        String productCode = String.format(Locale.getDefault(), "%06d", Integer.parseInt(cursor.getString(codeColumnIndex)));

        int sellColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE);
        String sellValue = moneyFormat.format(cursor.getDouble(sellColumnIndex));

        int buyColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE);
        String buyValue = moneyFormat.format(cursor.getDouble(buyColumnIndex));

        int stockColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_STOCK);
        String stockQuantity = cursor.getString(stockColumnIndex);
        final int stockValue = cursor.getInt(stockColumnIndex);

        int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
        String imageFilePath = cursor.getString(imageColumnIndex);

        // Atribui cada valor para o respectivo View
        nameTextView.setText(productName);
        codeTextView.setText(productCode);
        sellValueTextView.setText(sellValue);
        buyValueTextView.setText(buyValue);
        stockQuantityTextView.setText(stockQuantity);
        if (!TextUtils.isEmpty(imageFilePath)) {
            imageThumbNailView.setImageBitmap(InventoryProvider.openImageFile(context, imageFilePath));
        }

        // botão de venda, faz a verificação se o estoque não é 0, caso contrario decrementa 1 unidade, e mostra uma mensagem, para cada situação
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources res = context.getResources();
                String toastMessage;
                if (stockValue == 0) {
                    toastMessage = String.format(res.getString(R.string.no_more_products_toast), productName);
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                } else {
                    InventoryProvider.changeStock(context, columnId, stockValue, -1);
                    toastMessage = String.format(res.getString(R.string.sold_one_more_toast), productName);
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
