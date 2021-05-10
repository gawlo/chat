package org.tigase.messenger.phone.pro.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MyDBHelper extends SQLiteOpenHelper {
    public static final  String DATABASE_NAME = "db_numero";
    public static final String NOM_TABLE = "tb_numero";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NUMERO = "numero";
    public static final String COLUMN_ETAT = "etat";


    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table  " +NOM_TABLE+
                        " ("+COLUMN_ID+" integer primary key autoincrement,"+COLUMN_NUMERO+" text,"+COLUMN_ETAT+" text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+NOM_TABLE);
        onCreate(db);
    }

    // inserer une ligne
    public boolean insertVerification (String numero, Integer etat) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("numero", numero);
        contentValues.put("etat", etat);
        db.insert(NOM_TABLE, null, contentValues);
        return true;
    }

    // get cursor by numero
    public Cursor getDataByNumero(String numero){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from "+NOM_TABLE+ " where "+COLUMN_NUMERO+" ="+"'"+numero+"'", null);
        return res;
    }

    // lister toutes les lignes de la table
    public ArrayList<String> getAllNumero() {
        ArrayList<String> liste = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select DISTINCT "+COLUMN_NUMERO+" from "+NOM_TABLE, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            liste.add(res.getString(res.getColumnIndex(COLUMN_NUMERO)));
            res.moveToNext();
        }
        return liste;
    }

    // supprimer une ligne em fonction du numero
    public void deleteallnumero(String numero) {

        SQLiteDatabase db = this.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(NOM_TABLE, COLUMN_NUMERO + "= ?", new String[] { String.valueOf(numero) });
        db.close(); // Closing database connection
    }
}
