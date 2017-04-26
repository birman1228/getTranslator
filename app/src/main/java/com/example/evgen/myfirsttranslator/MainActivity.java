package com.example.evgen.myfirsttranslator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evgen.myfirsttranslator.Databases.FavoriteDb;
import com.example.evgen.myfirsttranslator.Databases.HistoryDb;
import com.example.evgen.myfirsttranslator.YandexTranslateApi.ParsingRequestTranslate;
import com.example.evgen.myfirsttranslator.YandexTranslateApi.ParsingSupportedLang;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.security.auth.DestroyFailedException;

public class MainActivity extends AppCompatActivity implements TextView.OnEditorActionListener {
    // Data
    TreeMap<String, String> supportedLangHash;
    ArrayList<String> supportedLangStr;
    long lastFavoriteId;
    boolean isAddedToFavorite;


    // GUI
    private Spinner fromLangSpinner;
    private Spinner toLangSpinner;
    private ArrayAdapter<String> adapter;
    private EditText fieldSourseText;
    private TextView fieldTransText;
    private ImageView favoriteBtn;
    private ImageView clearBtn;
    private ImageView swapBtn;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;

    private Animation rotate_arrows;

    // Database
    HistoryDb historyDb;
    FavoriteDb favoriteDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromLangSpinner = (Spinner) findViewById(R.id.spinner1);
        toLangSpinner = (Spinner) findViewById(R.id.spinner2);
        fieldSourseText = (EditText) findViewById(R.id.editText);
        fieldTransText = (TextView) findViewById(R.id.editText2);
        favoriteBtn = (ImageView) findViewById(R.id.favourite_btn);
        clearBtn = (ImageView) findViewById(R.id.clear_btn);
        swapBtn = (ImageView) findViewById(R.id.swap);
        rotate_arrows = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);

        // Массив названий языков
        supportedLangStr = new ArrayList<>();

        // Флаг добавления в избранное и последнее добавленное id
        isAddedToFavorite = false;
        lastFavoriteId = -1;


        fieldSourseText.setOnEditorActionListener(this);

        View.OnClickListener oclBtn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.favourite_btn:
                        new AddToFavorite().execute();
                        break;
                    case R.id.clear_btn:
                        fieldSourseText.setText("");
                        fieldTransText.setText("");
                        break;
                    case R.id.swap:
                        String tmp1 = fromLangSpinner.getSelectedItem().toString();
                        String tmp2 = toLangSpinner.getSelectedItem().toString();
                        fromLangSpinner.setSelection(supportedLangStr.indexOf(tmp2));
                        toLangSpinner.setSelection(supportedLangStr.indexOf(tmp1));
                        swapBtn.startAnimation(rotate_arrows);
                        break;
                }
            }
        };

        favoriteBtn.setOnClickListener(oclBtn);
        clearBtn.setOnClickListener(oclBtn);
        swapBtn.setOnClickListener(oclBtn);



        // Обрабатываем нажатия на меню
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_history:
                        Intent i = new Intent(MainActivity.this, HistoryActivity.class);
                        startActivity(i);
                        finish();
                        return true;
                    case R.id.navigation_favorite:
                        Intent i2 = new Intent(MainActivity.this, FavoriteActivity.class);
                        startActivity(i2);
                        finish();
                        return true;
                }
                return false;
            }
        };
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Открываем подключение к БД
        historyDb = new HistoryDb(this);
        historyDb.open();
        favoriteDb = new FavoriteDb(this);
        favoriteDb.open();

        // Получаем доступные языки
        if (isOnline()) {
            new GetLang().execute();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Отсутствует интернет соединение");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                                    int which) {
                    finish();
                }
            });
            builder.show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        historyDb.close();
        favoriteDb.close();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO){
            String codeFrom = supportedLangHash.get(fromLangSpinner.getSelectedItem());
            String codeTo = supportedLangHash.get(toLangSpinner.getSelectedItem());

            if (isOnline()) {
                new GetTranslate(codeFrom, codeTo).execute();
            }
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Отсутствует интернет соединение");
                builder.setPositiveButton("OK", null);
                builder.show();
            }

            isAddedToFavorite = false;
            favoriteBtn.setImageDrawable(getResources().getDrawable(R.drawable.favourite_btn_unpressed, null));
            return true;
        }
        return false;
    }

    public boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected())
            return true;
        else return false;
    }

    class GetLang extends AsyncTask<Void, Void, Void> {
        ParsingSupportedLang parsingSupportedLang;
        ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... params) {
            parsingSupportedLang = new ParsingSupportedLang();
            try {
                parsingSupportedLang.makeRequest();
                supportedLangHash = parsingSupportedLang.getSupportedLangHash();
            }
            catch (RuntimeException e){
                Log.e("MyError","Error from GetLang AyncTask");
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Секундочку...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            for(String lang: supportedLangHash.keySet())
                supportedLangStr.add(lang);

            adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, supportedLangStr);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fromLangSpinner.setAdapter(adapter);
            toLangSpinner.setAdapter(adapter);
            try{
                fromLangSpinner.setSelection(supportedLangStr.indexOf("Английский"));
                toLangSpinner.setSelection(supportedLangStr.indexOf("Русский"));
            }catch (Exception e){
                Log.e("NO","Not selected",e);
            }
            dialog.dismiss();
        }
    }

    class GetTranslate extends AsyncTask<Void, Void, String> {
        ParsingRequestTranslate parsingRequestTranslate;

        String codeFrom;
        String codeTo;

        public GetTranslate(String codeFrom, String codeTo) {
            this.codeFrom = codeFrom;
            this.codeTo = codeTo;
        }

        @Override
        protected String doInBackground(Void... params) {
            String targetText = "";

            try {
                parsingRequestTranslate.makeRequest();
                targetText = parsingRequestTranslate.getTranslate();
            }
            catch (RuntimeException e){
                System.exit(0);
            }

            return targetText;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            parsingRequestTranslate = new ParsingRequestTranslate(fieldSourseText.getText().toString(), codeFrom, codeTo);
        }
        @Override
        protected void onPostExecute(String target) {
            super.onPostExecute(target);
            fieldTransText.setText(target);

            String sourseText = fieldSourseText.getText().toString();
            String transText = fieldTransText.getText().toString();
            if(!sourseText.equals("")) {
                historyDb.addRec(sourseText, transText, codeFrom, codeTo);
            }
        }
    }

    class AddToFavorite extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {return null;}

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            String sourseText = fieldSourseText.getText().toString();
            String transText = fieldTransText.getText().toString();
            String codeFrom = supportedLangHash.get(fromLangSpinner.getSelectedItem());
            String codeTo = supportedLangHash.get(toLangSpinner.getSelectedItem());
            if(!sourseText.equals("") && !transText.equals("")) {
                if (isAddedToFavorite == true){
                    isAddedToFavorite = false;
                    favoriteBtn.setImageDrawable(getResources().getDrawable(R.drawable.favourite_btn_unpressed,null));
                    favoriteDb.delRec(lastFavoriteId);
                    Toast.makeText(MainActivity.this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                }
                else{
                    isAddedToFavorite = true;
                    favoriteBtn.setImageDrawable(getResources().getDrawable(R.drawable.favourite_btn_pressed,null));
                    lastFavoriteId = favoriteDb.addRec(sourseText, transText, codeFrom, codeTo);
                    Toast.makeText(MainActivity.this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(MainActivity.this, "Отсутствует перевод", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
