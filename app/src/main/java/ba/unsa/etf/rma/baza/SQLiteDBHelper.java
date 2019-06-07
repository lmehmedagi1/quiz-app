package ba.unsa.etf.rma.baza;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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



    private static final String KREIRAJ_TABELU_KVIZOVI = "CREATE TABLE " + TABELA_KVIZOVI + "(" +
            KOL_KVIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KOL_NAZIV + " TEXT, " +
            KOL_DOKUMENT_ID + " TEXT, " +
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
            KOL_ODGOVORI + " TEXT );";

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
        values.put(KOL_PITANJA, "ovdje dodati json pitanja");

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
        values.put(KOL_ODGOVORI, "ovdje dodati json odgovora");

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
}
