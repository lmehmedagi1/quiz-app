package ba.unsa.etf.rma.baza;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.RangListaItem;

public class SQLiteDBHelper extends SQLiteOpenHelper {

    public static final String IME_BAZE = "rma";
    public static final int VERZIJA_BAZE = 1;
    public static final String TABELA_KVIZOVI = "Kvizovi";
    public static final String TABELA_KATEGORIJE = "Kategorije";
    public static final String TABELA_PITANJA = "Pitanja";
    public static final String TABELA_RANG_LISTA = "Rangliste";

    public static final String KOL_KVIZ_ID = "kviz_id";
    public static final String KOL_KATEGORIJA_ID = "kategorija_id";
    public static final String KOL_PITANJE_ID = "pitanje_id";
    public static final String KOL_RANG_LISTA_ID = "rang_lista_id";
    public static final String KOL_NAZIV = "naziv";
    public static final String KOL_DOKUMENT_ID = "dokument";
    public static final String KOL_ID_IKONICE = "id_ikonice";
    public static final String KOL_PITANJA = "pitanja";
    public static final String KOL_ODGOVORI = "odgovori";
    public static final String KOL_NAZIV_KVIZA = "naziv_kviza";
    public static final String KOL_IME_IGRACA = "ime_igraca";
    public static final String KOL_PROCENAT = "procenat";
    public static final String KOL_KATEGORIJA_ID_DOKUMENTA = "kategodija_id_dokumenta";
    public static final String KOL_TACAN = "tacan";


    private static final String KREIRAJ_TABELU_KVIZOVI = "CREATE TABLE " + TABELA_KVIZOVI + "(" +
            KOL_KVIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KOL_NAZIV + " TEXT, " +
            KOL_DOKUMENT_ID + " TEXT, " +
            KOL_KATEGORIJA_ID_DOKUMENTA + " TEXT, " +
            KOL_PITANJA + " TEXT );";

    private static final String KREIRAJ_TABELU_KATEGORIJE = "CREATE TABLE " + TABELA_KATEGORIJE + "(" +
            KOL_KATEGORIJA_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KOL_DOKUMENT_ID + " TEXT, " +
            KOL_NAZIV + " TEXT, " +
            KOL_ID_IKONICE + " TEXT );";

    private static final String KREIRAJ_TABELU_PITANJA = "CREATE TABLE " + TABELA_PITANJA + "(" +
            KOL_PITANJE_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KOL_DOKUMENT_ID + " TEXT, " +
            KOL_NAZIV + " TEXT, " +
            KOL_ODGOVORI + " TEXT, " +
            KOL_TACAN + " TEXT );";

    private static final String KREIRAJ_TABELU_RANG_LISTA = "CREATE TABLE " + TABELA_RANG_LISTA + "(" +
            KOL_RANG_LISTA_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KOL_DOKUMENT_ID + " TEXT, " +
            KOL_NAZIV_KVIZA + " TEXT, " +
            KOL_IME_IGRACA + " TEXT, " +
            KOL_PROCENAT + " FLOAT );";



    public SQLiteDBHelper(Context context) {
        super(context, IME_BAZE, null, VERZIJA_BAZE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABELA_KVIZOVI + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABELA_KATEGORIJE + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABELA_PITANJA + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + TABELA_RANG_LISTA + "'");
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(KREIRAJ_TABELU_KVIZOVI);
        db.execSQL(KREIRAJ_TABELU_KATEGORIJE);
        db.execSQL(KREIRAJ_TABELU_PITANJA);
        db.execSQL(KREIRAJ_TABELU_RANG_LISTA);
    }

    public void dodajKviz(Kviz kviz) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KOL_NAZIV, kviz.getNaziv());
        values.put(KOL_DOKUMENT_ID, kviz.getIdDokumenta());

        ArrayList<String> pitanjaId = new ArrayList<>();
        for (Pitanje p : kviz.getPitanja())
            pitanjaId.add(p.getIdDokumenta());

        Gson gson = new Gson();
        String inputString = gson.toJson(pitanjaId);

        values.put(KOL_PITANJA, inputString);
        values.put(KOL_KATEGORIJA_ID_DOKUMENTA, kviz.getKategorija().getIdDokumenta());

        long id = db.insertWithOnConflict(TABELA_KVIZOVI, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void dodajKategorija(Kategorija kategorija) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KOL_NAZIV, kategorija.getNaziv());
        values.put(KOL_DOKUMENT_ID, kategorija.getIdDokumenta());
        values.put(KOL_ID_IKONICE, kategorija.getId());

        long id = db.insertWithOnConflict(TABELA_KATEGORIJE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void dodajPitanje(Pitanje pitanje) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KOL_NAZIV, pitanje.getNaziv());
        values.put(KOL_DOKUMENT_ID, pitanje.getIdDokumenta());

        Gson gson = new Gson();
        String inputString = gson.toJson(pitanje.getOdgovori());
        values.put(KOL_ODGOVORI, inputString);

        long id = db.insertWithOnConflict(TABELA_PITANJA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void dodajRangListItem(RangListaItem rangListaItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KOL_DOKUMENT_ID, rangListaItem.getIdDokumenta());
        values.put(KOL_NAZIV_KVIZA, rangListaItem.getNazivKviza());
        values.put(KOL_IME_IGRACA, rangListaItem.getImeIgraca());
        values.put(KOL_PROCENAT, rangListaItem.getProcenatTacnih());

        long id = db.insertWithOnConflict(TABELA_RANG_LISTA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public ArrayList<Kviz> dajSveKvizove() {
        ArrayList<Kviz> kvizovi = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABELA_KVIZOVI, null);

        if (cursor.moveToFirst()) {

            String idDokumenta = cursor.getString(cursor.getColumnIndex(KOL_DOKUMENT_ID));
            kvizovi.add(dajKviz(idDokumenta));

            while(cursor.moveToNext()){

                idDokumenta = cursor.getString(cursor.getColumnIndex(KOL_DOKUMENT_ID));
                kvizovi.add(dajKviz(idDokumenta));
            }
        }
        cursor.close();
        db.close();

        return kvizovi;
    }

    public Kviz dajKviz(String idDokumenta) {
        Kviz kviz = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] kolone = new String[]{KOL_NAZIV, KOL_PITANJA, KOL_KATEGORIJA_ID};
        String where = KOL_DOKUMENT_ID + "=" + idDokumenta;
        try {
            cursor = db.query(TABELA_KVIZOVI, kolone, where, null, null, null, null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();

                String naziv = cursor.getString(cursor.getColumnIndex(KOL_NAZIV));
                String idKategorije = cursor.getString(cursor.getColumnIndex(KOL_KATEGORIJA_ID_DOKUMENTA));
                String jsonPitanja = cursor.getString(cursor.getColumnIndex(KOL_PITANJA));

                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                ArrayList<String> pitanjaId = gson.fromJson(jsonPitanja, type);

                ArrayList<Pitanje> pitanjaZaKviz = new ArrayList<>();

                for (int i = 0; i<pitanjaId.size(); i++) {
                    Pitanje p = dajPitanje(pitanjaId.get(i));
                    pitanjaZaKviz.add(p);
                }

                Kategorija kategorija = dajKategoriju(idKategorije);

                kviz = new Kviz(naziv, pitanjaZaKviz, kategorija);
                kviz.setIdDokumenta(idDokumenta);
            }
        }
        finally {
            cursor.close();
        }

        return kviz;
    }

    private Kategorija dajKategoriju(String idDokumenta) {
        Kategorija kategorija = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] kolone = new String[]{KOL_NAZIV, KOL_ID_IKONICE};
        String where = KOL_DOKUMENT_ID + "=" + idDokumenta;
        try {
            cursor = db.query(TABELA_KATEGORIJE, kolone, where, null, null, null, null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();

                String naziv = cursor.getString(cursor.getColumnIndex(KOL_NAZIV));
                String ikonica = cursor.getString(cursor.getColumnIndex(KOL_ID_IKONICE));

                kategorija = new Kategorija(naziv, ikonica);
                kategorija.setIdDokumenta(idDokumenta);
            }
        }
        finally {
            cursor.close();
        }

        return kategorija;
    }

    public Pitanje dajPitanje(String idDokumenta) {
        Pitanje pitanje = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] kolone = new String[]{KOL_NAZIV, KOL_ODGOVORI, KOL_TACAN};
        String where = KOL_DOKUMENT_ID + "=" + idDokumenta;
        try {
            cursor = db.query(TABELA_PITANJA, kolone, where, null, null, null, null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();

                String naziv = cursor.getString(cursor.getColumnIndex(KOL_NAZIV));
                String tacan = cursor.getString(cursor.getColumnIndex(KOL_TACAN));
                String jsonPitanja = cursor.getString(cursor.getColumnIndex(KOL_ODGOVORI));

                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                ArrayList<String> odgovori = gson.fromJson(jsonPitanja, type);

                pitanje = new Pitanje(naziv, naziv, odgovori, tacan);
                pitanje.setIdDokumenta(idDokumenta);
            }
        }
        finally {
            cursor.close();
        }

        return pitanje;
    }

    private RangListaItem dajRangListu(String idDokumenta) {
        RangListaItem rangListaItem = null;
        Cursor cursor = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String[] kolone = new String[]{KOL_NAZIV_KVIZA, KOL_IME_IGRACA, KOL_PROCENAT};
        String where = KOL_DOKUMENT_ID + "=" + idDokumenta;
        try {
            cursor = db.query(TABELA_RANG_LISTA, kolone, where, null, null, null, null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();

                String nazivKviza = cursor.getString(cursor.getColumnIndex(KOL_NAZIV_KVIZA));
                String imeIgraca = cursor.getString(cursor.getColumnIndex(KOL_IME_IGRACA));
                Float procenat = cursor.getFloat(cursor.getColumnIndex(KOL_PROCENAT));

                rangListaItem = new RangListaItem(imeIgraca, nazivKviza, procenat, 1);
                rangListaItem.setIdDokumenta(idDokumenta);
            }
        }
        finally {
            cursor.close();
        }

        return rangListaItem;
    }
}
