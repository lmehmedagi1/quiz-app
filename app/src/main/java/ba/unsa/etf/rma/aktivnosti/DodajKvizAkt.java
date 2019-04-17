package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.PitanjeAdapter;

public class DodajKvizAkt extends AppCompatActivity {

    private ListView listaDodanihPitanja;
    private ListView listaMogucihPitanja;
    private Button dodajKvizButton;
    private EditText nazivKviza;
    private Spinner spinner;
    private Button importButton;

    private Kviz kviz = null;
    private ArrayList<Kategorija> kategorije = new ArrayList<>();
    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private ArrayList<Pitanje> dodanaPitanja = new ArrayList<>();
    private ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

    private ArrayAdapter<Kategorija> kategorijaAdapter = null;
    private PitanjeAdapter dodanaPitanjaAdapter = null;
    private ArrayAdapter<Pitanje> mogucaPitanjaAdapter = null;

    private View elementZaDodavanje;
    private String odabranaKategorija;
    private int trenutnaKategorija;

    private boolean izmjena = false;
    private String imeOdabranogKviza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        listaDodanihPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        dodajKvizButton = (Button) findViewById(R.id.btnDodajKviz);
        nazivKviza = (EditText) findViewById(R.id.etNaziv);
        spinner = (Spinner) findViewById(R.id.spKategorije);
        importButton = (Button) findViewById(R.id.btnImportKviz);

        Intent intent = getIntent();

        kviz = (Kviz)intent.getSerializableExtra("kviz");
        kvizovi = (ArrayList<Kviz>)intent.getSerializableExtra("kvizovi");
        kategorije = (ArrayList<Kategorija>)intent.getSerializableExtra("kategorije");
        kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));

        dodajDodanaPitanja();

        kategorijaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategorije);
        kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        mogucaPitanjaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mogucaPitanja);

        elementZaDodavanje = dodanaPitanjaAdapter.dajElementZaDodavanje(listaDodanihPitanja);
        listaDodanihPitanja.addFooterView(elementZaDodavanje);

        spinner.setAdapter(kategorijaAdapter);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
        listaMogucihPitanja.setAdapter(mogucaPitanjaAdapter);

        dodajNazivKviza();
        dodajKategorijuKviza();

        dodajListenerNaListe();
        dodajListenerNaButton();
        dodajListenerNaSpinner();
        dodajListenerNaEditText();
    }

    private void ocistiBoje() {
        nazivKviza.setBackgroundColor(0);
    }

    private void dodajListenerNaEditText() {
        nazivKviza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocistiBoje();
            }
        });
    }

    private void dodajListenerNaSpinner() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String odabrana = parent.getItemAtPosition(position).toString();

                if (odabrana.equals("Dodaj kategoriju"))
                    otvoriAktivnostZaDodavanjeKategorije();
                else {
                    odabranaKategorija = odabrana;
                    trenutnaKategorija = position;
                }
                ocistiBoje();
            }
            public void onNothingSelected(AdapterView<?> parent) {
                dodajKategorijuKviza();
                ocistiBoje();
            }
        });
    }

    private void dodajListenerNaButton() {
        dodajKvizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validniPodaci()) return;
                dodajKviz();
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("text/plain");

                startActivityForResult(intent, 42);
            }
        });
    }

    private void dodajListenerNaListe() {
        odabranElementListe(listaDodanihPitanja, mogucaPitanja, dodanaPitanja);
        odabranElementListe(listaMogucihPitanja, dodanaPitanja, mogucaPitanja);

        elementZaDodavanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otvoriAktivnostZaDodavanjePitanja();
            }
        });
    }


    private void dodajKategorijuKviza() {
        if (kviz != null) {
            for (int i = 0; i<kategorije.size(); i++)
                if (kategorije.get(i).getNaziv().equals(kviz.getKategorija().getNaziv())) {
                    spinner.setSelection(i);
                    kategorijaAdapter.notifyDataSetChanged();
                    trenutnaKategorija = i;
                }
        }
        else
            trenutnaKategorija = 0;
    }
    private void dodajNazivKviza() {
        if (kviz != null) {
            nazivKviza.setText(kviz.getNaziv());
            imeOdabranogKviza = kviz.getNaziv();
            izmjena = true;
        }
    }
    private void dodajDodanaPitanja() {
        if (kviz != null)
            dodanaPitanja.addAll(kviz.getPitanja());
    }


    private void dodajKviz() {
        Intent intent = new Intent();

        if (kviz == null) intent.putExtra("izmjena", "dodavanje");
        else intent.putExtra("izmjena", "izmjena");

        kviz = new Kviz(nazivKviza.getText().toString(), dodanaPitanja, (Kategorija) spinner.getSelectedItem());
        intent.putExtra("kviz", kviz);
        intent.putExtra("odabraniKviz", imeOdabranogKviza);

        kategorije.remove(kategorije.size()-1);
        intent.putExtra("kategorije", kategorije);
        setResult(10, intent);

        finish();
    }

    private boolean validniPodaci() {
        boolean ispravniPodaci = true;

        if (nazivKviza.getText() == null || nazivKviza.getText().toString().length() == 0) {
            nazivKviza.setBackgroundColor(Color.RED);
            ispravniPodaci = false;
        }
        else if (!izmjena || (izmjena && !nazivKviza.getText().toString().equals(imeOdabranogKviza))) {   //ili dodavanje ili promjena trenutnog kviza
            for (Kviz k : kvizovi) {
                if (nazivKviza.getText().toString().equals(k.getNaziv())) {
                    nazivKviza.setBackgroundColor(Color.RED);
                    ispravniPodaci = false;
                }
            }
        }
        return ispravniPodaci;
    }

    private void odabranElementListe(ListView lista, final ArrayList<Pitanje> trenutnaPitanja, final ArrayList<Pitanje> odredisnaPitanja) {
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Pitanje odabranoPitanje = (Pitanje)adapter.getItemAtPosition(position);

                trenutnaPitanja.add(odabranoPitanje);
                odredisnaPitanja.remove(odabranoPitanje);

                dodanaPitanjaAdapter.notifyDataSetChanged();
                mogucaPitanjaAdapter.notifyDataSetChanged();
            }
        });
    }

    private void otvoriAktivnostZaDodavanjePitanja() {
        Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
        intent.putExtra("listaDodanih", dodanaPitanja);
        intent.putExtra("listaMogucih", mogucaPitanja);
        DodajKvizAkt.this.startActivityForResult(intent, 1);
    }

    private void otvoriAktivnostZaDodavanjeKategorije() {
        Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
        intent.putExtra("kategorije", kategorije);
        DodajKvizAkt.this.startActivityForResult(intent, 2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == 1) {
                Pitanje pitanje = (Pitanje)data.getSerializableExtra("pitanje");
                dodajPitanje(pitanje);
            }
        }
        else if (requestCode == 2) {
            if (resultCode == 2) {
                Kategorija kategorija = (Kategorija) data.getSerializableExtra("kategorija");
                dodajKategoriju(kategorija);
            }
            else
                spinner.setSelection(0);
        }
        else if (requestCode == 42  && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                try {
                    ArrayList<String> importData = readTextFromUri(uri);
                    dodajImportovaniKviz(importData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void dodajImportovaniKviz(ArrayList<String> importData) {

        String naziv = "";
        Kategorija kategorija = null;
        ArrayList<Pitanje> pitanjaZaImportovaniKviz = new ArrayList<>();


        if (importData == null || importData.size()==0) {
            izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
            return;
        }

        String prviRed = importData.get(0);

        StringTokenizer tokenizer = new StringTokenizer(prviRed, ",");
        int i = 0;
        String nazivKategorije = "";
        int brojPitanja = -1;

        while (tokenizer.hasMoreTokens()) {
            if (i == 0) naziv = tokenizer.nextToken();
            if (i == 1) nazivKategorije = tokenizer.nextToken();
            if (i == 2) brojPitanja = Integer.parseInt(tokenizer.nextToken());
            i++;
        }

        if (i != 3) {
            izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
            return;
        }

        for (Kviz k : kvizovi) {
            if (k.getNaziv().equals(naziv)) {
                izbaciAlert("Kviz kojeg importujete već postoji!");
                return;
            }
        }

        for (Kategorija k : kategorije) {
            if (k.getNaziv().equals(nazivKategorije)) {
                kategorija = k;
            }
        }
        // ako ne postoji vec dodajemo je sa icon id -1
        if (kategorija == null) {
            kategorija = new Kategorija(nazivKategorije, "-1");
        }


        if (brojPitanja != importData.size()-1) {
            izbaciAlert("Kviz kojeg imporujete ima neispravan broj pitanja!");
            return;
        }



        for (int j = 1; j<importData.size(); j++) {
            String pitanjeInfo = importData.get(j);

            if (pitanjeInfo == null || pitanjeInfo.length() == 0) {
                izbaciAlert("Datoteka kviza kojeg importujete nema ispravan format!");
                return;
            }

            StringTokenizer tokenizerPitanja = new StringTokenizer(pitanjeInfo, ",");
            int k = 0;
            String nazivPitanja = "";
            int brojOdgovora = -1, indeksTacnog = -1;

            ArrayList<String> odgovori = new ArrayList<>();

            while (tokenizerPitanja.hasMoreTokens()) {
                if (k == 0) nazivPitanja = tokenizerPitanja.nextToken();
                if (k == 1) brojOdgovora = Integer.parseInt(tokenizerPitanja.nextToken());
                if (k == 2) indeksTacnog = Integer.parseInt(tokenizerPitanja.nextToken());

                if (k>2) {
                    String odgovor = tokenizerPitanja.nextToken();

                    for (String o : odgovori) {
                        if (odgovor.equals(o)) {
                            izbaciAlert("Kviz kojeg importujete nije ispravan postoji ponavljanje odgovora!");
                            return;
                        }
                    }

                    odgovori.add(odgovor);
                }

                k++;
            }

            for (Pitanje p : pitanjaZaImportovaniKviz) {
                if (p.getNaziv().equals(nazivPitanja)) {
                    izbaciAlert("Kviz nije ispravan postoje dva pitanja sa istim nazivom!");
                    return;
                }
            }

            if (brojOdgovora != odgovori.size() || brojOdgovora == 0) {
                izbaciAlert("Kviz kojeg importujete ima neispravan broj odgovora!");
                return;
            }

            if (indeksTacnog<0 || indeksTacnog>=odgovori.size()) {
                izbaciAlert("Kviz kojeg importujete ima neispravan index tačnog odgovora!");
                return;
            }

            pitanjaZaImportovaniKviz.add(new Pitanje(nazivPitanja, nazivPitanja, odgovori, odgovori.get(indeksTacnog)));
        }


        nazivKviza.setText(naziv);
        dodajKategoriju(kategorija);

        // sta se treba desiti sa starim pitanjima i sa mogucim
        dodanaPitanja.addAll(pitanjaZaImportovaniKviz);

        //radilo je i bez ovog i swear
        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
    }

    private void izbaciAlert(String poruka) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(poruka);
        builder.create().show();
    }

    private ArrayList<String> readTextFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        ArrayList<String> importData = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            importData.add(line);
        }
        inputStream.close();
        return importData;
    }


    private void dodajKategoriju(Kategorija kategorija) {
        kategorije.remove(kategorije.size()-1);
        kategorije.add(kategorija);
        kategorije.add(new Kategorija("Dodaj kategoriju", "Dodaj kategoriju"));
        kategorijaAdapter.notifyDataSetChanged();

        odabranaKategorija = kategorija.getNaziv();
    }

    private void dodajPitanje(Pitanje pitanje) {
        dodanaPitanja.add(pitanje);

        //radilo je i bez ovog i swear
        dodanaPitanjaAdapter = new PitanjeAdapter(this, dodanaPitanja);
        listaDodanihPitanja.setAdapter(dodanaPitanjaAdapter);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();

        kategorije.remove(kategorije.size()-1);
        intent.putExtra("kategorije", kategorije);
        setResult(11, intent);

        finish();
    }

}
