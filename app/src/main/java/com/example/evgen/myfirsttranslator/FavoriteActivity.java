package com.example.evgen.myfirsttranslator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.evgen.myfirsttranslator.Databases.FavoriteDb;

public class FavoriteActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{

    FavoriteDb favoriteDb;
    ListView lvData;
    SimpleCursorAdapter scAdapter;
    ImageView deleteBtn;
    Animation shake_brush;

    private static final int CM_DELETE_ID = 1;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        favoriteDb = new FavoriteDb(this);
        favoriteDb.open();

        String[] from = new String[]{ favoriteDb.KEY_SOURSE_TEXT, favoriteDb.KEY_TRANS_TEXT,
                favoriteDb.KEY_SOURSE_LANG, favoriteDb.KEY_TRANS_LANG};
        int[] to = new int[]{ R.id.sourseText, R.id.transText, R.id.direction1, R.id.direction2};

        scAdapter = new SimpleCursorAdapter(this, R.layout.item, null, from, to, 0);
        lvData = (ListView) findViewById(R.id.lvData2);
        lvData.setAdapter(scAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        // Обрабатываем нажатия на меню
        mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_translate:
                        Intent i = new Intent(FavoriteActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        return true;
                    case R.id.navigation_history:
                        Intent i2 = new Intent(FavoriteActivity.this, HistoryActivity.class);
                        startActivity(i2);
                        finish();
                        return true;
                }
                return false;
            }
        };
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        deleteBtn = (ImageView) findViewById(R.id.deleteBtn2);
        deleteBtn.setOnClickListener(this);
        shake_brush = AnimationUtils.loadAnimation(this, R.anim.shake_brush);

        registerForContextMenu(lvData);

    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            favoriteDb.delRec(acmi.id);
            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();
            return true;
        }
        return super.onContextItemSelected(item);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        favoriteDb.close();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new FavoriteActivity.MyCursorLoader(this, favoriteDb);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        scAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.deleteBtn2){
            deleteBtn.startAnimation(shake_brush);
            AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this);
            builder.setMessage("Очистить избранное?");
            builder.setNegativeButton("Отмена", null);
            builder.setPositiveButton("Продолжить",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            favoriteDb.delAll();
                            getSupportLoaderManager().getLoader(0).forceLoad();
                        }
                    });
            builder.show();
        }
    }


    static class MyCursorLoader extends android.support.v4.content.CursorLoader {

        FavoriteDb db;

        public MyCursorLoader(Context context, FavoriteDb db) {
            super(context);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllData();
            return cursor;
        }
    }
}
