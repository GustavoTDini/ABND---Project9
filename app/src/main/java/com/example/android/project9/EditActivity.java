package com.example.android.project9;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.project9.Data.InventoryContract.InventoryEntry;
import com.example.android.project9.Data.InventoryDbHelper;
import com.example.android.project9.Data.InventoryProvider;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identificador  para o retorno do intent da camera
     */
    static final int REQUEST_CAPTURE = 101;
    /**
     * Identificador  para o retorno do intent da galeria
     */
    static final int REQUEST_PICK = 111;
    /**
     * Identificador do Loader
     */
    private static final int INVENTORY_LOADER = 0;

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    /**
     * Uri para o produto para edição - null se for adicionar novo item
     */
    private Uri mCurrentProductUri;
    /**
     * Variavel para Verificar se houve mudança na Edição
     */
    private boolean mHasChanges = false;

    private EditText mProductNameView;
    private EditText mProductCodeView;
    private EditText mProductBuyView;
    private EditText mProductSellView;
    private EditText mProductStockView;
    private ImageView mProductImageView;
    private Button mDeleteButton;
    private Button mEditImageButton;
    private Button mAddStockButton;
    private Button mRemoveStockButton;
    private Button mOrderButton;
    private LinearLayout mStockControlView;
    private Spinner mChangeStockView;

    /**
     * Variavel para Acompanhar a mudança no Spinner
     */
    private int mSelectedChangeStock;

    // Métodos de Controle da Activity -------------------------------------------------------------
    private ImageButton mRotateImageButton;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Verifica se a Intent que foi usada para abrir esta activity contém dados
        // que indicam o uso para edição de um produto.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mDeleteButton = findViewById(R.id.delete_button);
        mProductNameView = findViewById(R.id.edit_name);
        mProductCodeView = findViewById(R.id.edit_code);
        mProductBuyView = findViewById(R.id.edit_buy);
        mProductSellView = findViewById(R.id.edit_sell);
        mProductStockView = findViewById(R.id.edit_stock);
        mProductImageView = findViewById(R.id.edit_image);
        mEditImageButton = findViewById(R.id.edit_image_button);
        mRotateImageButton = findViewById(R.id.rotate_image_button);
        mStockControlView = findViewById(R.id.stock_control_view);
        mAddStockButton = findViewById(R.id.edit_add_stock_button);
        mRemoveStockButton = findViewById(R.id.edit_remove_stock_button);
        mOrderButton = findViewById(R.id.order_button);
        mChangeStockView = findViewById(R.id.change_stock_quantity);


        // Verifica se a Activity está sendo usada para editar ou adicionar um produto e
        // modifica o Titulo, mostra ou não o botão de Apagar, os controles de estoque e o botão de pedir
        if (mCurrentProductUri == null) {
            setTitle(R.string.edit_new_product_title);
            mOrderButton.setVisibility(View.GONE);
            mStockControlView.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);
        } else {
            mOrderButton.setVisibility(View.VISIBLE);
            mStockControlView.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            // Bloqueia a edição do estoque, deixando esta funcionalidade para os botões de Controle
            mProductStockView.setKeyListener(null);
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }


        mProductNameView.setOnTouchListener(mTouchListener);
        mProductCodeView.setOnTouchListener(mTouchListener);
        mProductBuyView.setOnTouchListener(mTouchListener);
        mProductSellView.setOnTouchListener(mTouchListener);
        mProductStockView.setOnTouchListener(mTouchListener);
        mEditImageButton.setOnTouchListener(mTouchListener);


        // Define a Função do FAB que será de salvar as alterações, aqui se verifica se os dados estão preenchidos ou não, para não escrever nada null
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // recebe os Strings dos EditTexts
                String nameString = mProductNameView.getText().toString().trim();
                String codeString = mProductCodeView.getText().toString().trim();
                String buyString = mProductBuyView.getText().toString().trim();
                String sellString = mProductSellView.getText().toString().trim();
                String stockString = mProductStockView.getText().toString().trim();

                // caso os strings sejam de tamanho 0, significa que não está preenchido, logo será uma entry não valida,
                if (nameString.length() == 0 || codeString.length() == 0 || buyString.length() == 0 || sellString.length() == 0 || stockString.length() == 0) {
                    // caso seja não valida, abre um dialog para evitar salvar a entry incorreta
                    confirmDiscardOrEditDialog(getResources().getString(R.string.edit_fill_dialog_message));
                } else {
                    // caso seja valida, salvará a entry
                    saveEditChanges(nameString, codeString, buyString, sellString, stockString);
                }

            }
        });

        // Botão de Adicionar ao estoque - verifica o valor do Spinner e incrementa ao estoque, montrando um toast de sucesso,
        // só aparecera na atualização de um produto
        mAddStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recebe o nome do produto para ser utilizado no toast
                String productName = mProductNameView.getText().toString().trim();
                changeStockEditActivity(mSelectedChangeStock);
                // caso o valor adicionado seja maior que 1, adiciona o plural
                if (mSelectedChangeStock > 1) {
                    productName = productName + "s";
                }
                Toast.makeText(EditActivity.this, String.format(getResources().getString(R.string.add_more_stock_toast), mSelectedChangeStock, productName), Toast.LENGTH_SHORT).show();
            }
        });

        // Botão de Remover do estoque - verifica o valor do Spinner e incrementa ao estoque, montrando um toast de sucesso,
        // irá verificar se o estoque é 0, ou se o valor é maior que o estoque atual nestes casos nada acontecerá e mostrara um Toast de Alerta
        // só aparecera na atualização de um produto
        mRemoveStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // recebe o nome do produto para ser utilizado no toast e o valor do estoque para teste
                String productName = mProductNameView.getText().toString().trim();
                int stockValue = Integer.parseInt(mProductStockView.getText().toString().trim());
                if (stockValue == 0) {
                    Toast.makeText(EditActivity.this, String.format(getResources().getString(R.string.no_more_stock_toast), productName), Toast.LENGTH_SHORT).show();
                } else if (mSelectedChangeStock > stockValue) {
                    Toast.makeText(EditActivity.this, String.format(getResources().getString(R.string.value_larger_than_stock_toast), mSelectedChangeStock), Toast.LENGTH_SHORT).show();
                } else {
                    changeStockEditActivity(mSelectedChangeStock * -1);
                    if (mSelectedChangeStock > 1) {
                        productName = productName + "s";
                    }
                    Toast.makeText(EditActivity.this, String.format(getResources().getString(R.string.remove_more_stock_toast), mSelectedChangeStock, productName), Toast.LENGTH_SHORT).show();
                }
            }

        });

        // Botão de Apagar - acessa o método para apagar o produto, só aparecera na atualização de um produto
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        // Botão de selecionar imagem, abre um dialog que mostra as opções de para pegar a imagem
        mEditImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageSource();
            }
        });

        // Botão para rodar a imagem, para deixá-la na posicão correta
        mRotateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // compara a imagem do ImageView, caso seja o placeholder, nada faz
                if (mProductImageView.getDrawable().getConstantState() != getResources().getDrawable(R.drawable.image_placeholder).getConstantState()) {
                    //recebe o valor da atual rotação da imagem e a rotaciona em 90
                    float currentRotation = mProductImageView.getRotation();
                    mProductImageView.setRotation(currentRotation + 90);
                }
            }
        });

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrderServiceDialog(mProductNameView.getText().toString().trim());
            }
        });

        populateSpinner();

    }

    // Criação do menu para a função do botão home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                openDialogAsNavigateUp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Métodos de Controle do Loader ---------------------------------------------------------------

    @Override
    public void onBackPressed() {
        openDialogAsNavigateUp();
    }

    // na criação do Cursor Loader, só será aberto caso a edit venha de uma atualização de produto, neste caso usa-se a respectiva uri
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mCurrentProductUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // testa se a data é invalida, neste caso returna sem fazer nada
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();

            // separa as colunas do cursor para cada atributo e lê cada atributo
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            final String productName = data.getString(nameColumnIndex);

            int codeColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_CODE);
            String productCode = String.format(Locale.getDefault(), "%06d", Integer.parseInt(data.getString(codeColumnIndex)));

            int sellColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE);
            String sellValue = moneyFormat.format(data.getDouble(sellColumnIndex));

            int buyColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE);
            String buyValue = moneyFormat.format(data.getDouble(buyColumnIndex));

            int stockColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_STOCK);
            String stockQuantity = data.getString(stockColumnIndex);

            int imageColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            String imageFilePath = data.getString(imageColumnIndex);

            // Atribui cada valor para o respectivo View
            setTitle(String.format(getResources().getString(R.string.edit_edit_product_title), productName));
            mProductNameView.setText(productName);
            mProductCodeView.setText(productCode);
            mProductSellView.setText(sellValue);
            mProductBuyView.setText(buyValue);
            mProductStockView.setText(stockQuantity);
            if (!TextUtils.isEmpty(imageFilePath)) {
                mProductImageView.setImageBitmap(InventoryProvider.openImageFile(this, imageFilePath));
            }

        }
    }

    // Métodos de CRUD da DataBase -----------------------------------------------------------------

    // no reset, limpa todos os editText e coloca de volta o placeholder
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameView.setText("");
        mProductCodeView.setText("");
        mProductBuyView.setText("");
        mProductSellView.setText("");
        mProductStockView.setText("");
        mProductImageView.setImageResource(R.drawable.image_placeholder);
    }

    // método de Delete do produto, acessado pelo botão delete que só aparecerá caso a activity seja utilizada apara atualizar, mesmo assim ha um teste para esta verificação
    private void deleteProduct() {
        // testa se mCurrentProductUri é diferente de null, indicando edição de produto já existente, case positvo, faz o delete com o contentResolver
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // teste se o rows deleted é igual ou diferente de 0, indicando o sucesso do delete, e mostrando um toast, ou o oposto
            if (rowsDeleted == 0) {
                Toast.makeText(this, String.format(getResources().getString(R.string.edit_delete_error_message), mProductNameView.getText().toString()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, String.format(getResources().getString(R.string.edit_delete_success_message), mProductNameView.getText().toString()), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    // Métodos de Diálogo --------------------------------------------------------------------------

    /**
     * Método para salvar as alterações, seja salvando um produto novo ou atualizando um antigo
     * <p>
     * oa parametros são os Strings Recebidos dos EditText
     */
    private void saveEditChanges(String nameString, String codeString, String buyString, String sellString, String stockString) {

        // transforma os Strings Recebidos em Int ou doubles para ser utilizado no DB
        int codeInt = Integer.parseInt(codeString);
        double buyDouble = moneyToDouble(buyString);
        double sellDouble = moneyToDouble(sellString);
        int stockInt = Integer.parseInt(stockString);

        // carrega a imagem do image view e a salva em um arquivo, colocando o caminho do arquivo no banco de dados
        mProductImageView.buildDrawingCache();
        Bitmap productBitmap = mProductImageView.getDrawingCache();
        String imagePathString = InventoryProvider.saveImageFile(this, nameString, productBitmap);
        // Abre um ContentValues e coloca os dados
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_CODE, codeInt);
        values.put(InventoryEntry.COLUMN_PRODUCT_BUY_VALUE, buyDouble);
        values.put(InventoryEntry.COLUMN_PRODUCT_SELL_VALUE, sellDouble);
        values.put(InventoryEntry.COLUMN_PRODUCT_STOCK, stockInt);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, imagePathString);

        // caso o mCurrentProductUri seja null, se trata de um novo produto, fazemos os testes para ver se um produto igual existe
        if (mCurrentProductUri == null) {
            InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
            // abre o database de leitura
            SQLiteDatabase readDatabase = mDbHelper.getReadableDatabase();

            // Verifica se o nome e o codigo já existem na tabela, neste caso não é adicionado um novo produto e abre um dialog
            if (InventoryProvider.checkIfContains(readDatabase, nameString, codeInt)) {
                confirmDiscardOrEditDialog(getResources().getString(R.string.edit_exist_dialog_message));

            } else {
                // caso o codigo ou o nome não existam, adiciona um novo produto
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
                // caso a inserção tenha dado certo mostra uma mensagem de sucesso, caso contrário uma de erro
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.edit_insert_error_message, nameString), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.edit_insert_success_message, nameString), Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        } else {
            // caso seja um produto atualizado, faz o ContentResolver Updata
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // caso a atualização tenha dado certo mostra uma mensagem de sucesso, caso contrário uma de erro
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.edit_update_error_message, nameString), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.edit_update_success_message, nameString), Toast.LENGTH_SHORT).show();
            }
            finish();
        }

    }

    private void openDialogAsNavigateUp() {
        if (!mHasChanges) {
            super.onBackPressed();
            return;
        }

        confirmDiscardOrEditDialog(getResources().getString(R.string.edit_changes_dialog_message));
    }

    // dialogo de confirmação de mudanças, pode ser modificada a mensagem de acordo com a ocorrencia
    private void confirmDiscardOrEditDialog(String message) {
        Log.v("EditActicity", "confirmDiscardOrEditDialog: " + message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.edit_dialog_discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.edit_dialog_keep, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // dialogo de confirmação de delete
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getResources().getString(R.string.edit_dialog_delete_message), mProductNameView.getText().toString()));
        builder.setPositiveButton(R.string.edit_dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.edit_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // dialogo de Abrir Solicitação de Estoque
    private void openOrderServiceDialog(final String productName) {
        String unit = "unidade";
        // teste se o pedido é maior que uma unidade para corrigir o plural
        if (mSelectedChangeStock > 1) {
            unit = "unidades";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(String.format(getResources().getString(R.string.edit_order_dialog_message), mSelectedChangeStock, unit, productName));
        builder.setPositiveButton(R.string.edit_dialog_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                emailOrder(productName, mSelectedChangeStock);
            }
        });
        builder.setNegativeButton(R.string.edit_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Métodos Auxiliares --------------------------------------------------------------------------

    // Dialog para definir de onde virá a imagem
    private void selectImageSource() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.edit_image_selection_message);
        builder.setPositiveButton(R.string.edit_image_selection_gallery, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                selectPictureIntent();
            }
        });
        builder.setNegativeButton(R.string.edit_image_selection_camera, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                takePictureIntent();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Método para converter o String formatado em currency em double
     *
     * @param money String recebida do EditText
     * @return Double corrigido
     */
    private Double moneyToDouble(String money) {
        // testa se o String contem uma virgula (notação portuguesa), nesta caso mantem a virgula e retira todos os outros
        // caracteres, depoiis troca a virgula pelo ponto
        if (money.contains(",")) {
            money = money.replaceAll("[^0-9,]", "");
            money = money.replace(",", ".");
            // caso contrario (demais notações), retira todos os caracteres e mantem o ponto
        } else money = money.replaceAll("[^0-9.]", "");
        return Double.parseDouble(money);
    }


    //método para modificar
    private void changeStockEditActivity(int changeQuantity) {
        mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        String selection = InventoryEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentProductUri))};
        Cursor cursor = database.query(InventoryEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null, null);
        cursor.moveToFirst();
        int IdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        final int columnId = cursor.getInt(IdColumnIndex);

        int stockColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_STOCK);
        final int stockValue = cursor.getInt(stockColumnIndex);

        InventoryProvider.changeStock(this, columnId, stockValue, changeQuantity);
        cursor.close();
    }


    // Metéodo para montar e definir a função do Spinner de seleção de quantidade a ser modificado do estoque
    private void populateSpinner() {
        // valor maximo do Array
        int maxArray = 100;

        // Criação do ArrayList para popular o Spinner
        ArrayList maxStockArray = new ArrayList(maxArray);
        for (int i = 1; i <= maxArray; ++i) {
            maxStockArray.add(i);
        }

        //criando um Array Adapter com a ArrayList para e aplica-lo ao Spinner
        ArrayAdapter stockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maxStockArray);
        stockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mChangeStockView.setAdapter(stockAdapter);

        mChangeStockView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // definir o Valor da seleção
                int selected = (int) parent.getItemAtPosition(position);
                if (selected != 0) {
                    mSelectedChangeStock = selected;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSelectedChangeStock = 0;
            }
        });
    }

    // abre o intent de Email
    private void emailOrder(String productName, int stockRequest) {
        String greeting;
        String unit = getString(R.string.string_format_unit);
        // teste se o pedido é maior que uma unidade para corrigir o plural
        if (stockRequest > 1) {
            unit = getString(R.string.string_format_units);
        }
        // recebe a hora atual
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        // mudamos a saudação dependendo da hora do dia
        if (currentHour >= 6 && currentHour <= 12) {
            greeting = getString(R.string.greeting_day);
        } else if (currentHour > 13 && currentHour <= 18) {
            greeting = getString(R.string.greeting_afternoon);
        } else greeting = getString(R.string.greeting_night);
        productName = productName.replaceAll("\\s", "").toLowerCase();
        String[] email = {String.format(getResources().getString(R.string.email_order_address), productName)};
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setType(getString(R.string.intent_email_type));
        emailIntent.setData(Uri.parse(getString(R.string.intent_email_data)));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, String.format(getResources().getString(R.string.email_order_subject), productName));
        emailIntent.putExtra(Intent.EXTRA_TEXT, String.format(getResources().getString(R.string.email_order_content), greeting, stockRequest, unit, productName));
        startActivity(Intent.createChooser(emailIntent, getString(com.example.android.project9.R.string.edit_intent_send_email)));
    }


    // abre o intent da camera com resultado
    private void takePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CAPTURE);
        }
    }

    // abre o intent da galeria com resultado
    private void selectPictureIntent() {
        Intent selectPicture = new Intent();
        selectPicture.setType(getString(R.string.image_intent_type));
        selectPicture.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectPicture, getString(R.string.select_picture_intent)), REQUEST_PICK);
    }

    // Verifica o resultado do intent e coloca a imagem selecionada no ImageView
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // testa se os dados são nulos, neste caso nada faz
        if (data == null) {
            return;
            // caso o resul code seja ok pega os dados
        } else if (resultCode == RESULT_OK) {
            // recebe os dados e abre um bitmap
            Bundle extras = data.getExtras();
            Bitmap productImageBitmap;
            // testa o resquestCode
            switch (requestCode) {
                // caso o resultado venha da camera, aplica essa imagem na ImageView diretamente
                case REQUEST_CAPTURE:
                    productImageBitmap = (Bitmap) extras.get(getString(R.string.intent_data));
                    mProductImageView.setImageBitmap(productImageBitmap);
                    break;
                //  caso o resultado venha da galeria, temos que decodificar essa imagem com um Uri e codifica-la em um bitmap para utilizarmos
                case REQUEST_PICK:
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        productImageBitmap = BitmapFactory.decodeStream(imageStream);
                        mProductImageView.setImageBitmap(productImageBitmap);
                        // caso não seja possivel decodificar uma mensagem de erro é mostrada
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, R.string.edit_not_use_image, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    // no caso de falha, mantem o placeholder e manda uma mensagem de erro
                    mProductImageView.setImageResource(R.drawable.image_placeholder);
                    Toast.makeText(this, R.string.edit_not_use_image, Toast.LENGTH_SHORT).show();
                    break;
            }

        }

    }

}
