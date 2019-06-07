package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.baza.GetRequestIntentService;
import ba.unsa.etf.rma.baza.GetRequestResultReceiver;
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, GetRequestResultReceiver.Receiver {

    private EditText nazivKategorije;
    private EditText ikona;
    private Button dodajIkonuButton;
    private Button dodajKategorijuButton;

    private Icon[] selectedIcons;
    private IconDialog iconDialog;

    private ArrayList<Kategorija> azuriraneKategorije = null;
    private String TOKEN;
    private GetRequestResultReceiver receiver = null;

    private String trenutniNaziv = "";
    private String trenutnaIkona = "";
    private Kategorija kategorija = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(this);

        nazivKategorije       = (EditText)findViewById(R.id.etNaziv);
        ikona                 = (EditText)findViewById(R.id.etIkona);
        dodajIkonuButton      = (Button)findViewById(R.id.btnDodajIkonu);
        dodajKategorijuButton = (Button)findViewById(R.id.btnDodajKategoriju);

        ikona.setEnabled(false);

        Intent intent = getIntent();
        TOKEN = intent.getStringExtra("TOKEN");

        iconDialog = new IconDialog();

        dodajListenerNaButtons();
        dodajListenerNaEditText();
    }

    private void dodajListenerNaEditText() {
        nazivKategorije.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocistiBoje();
            }
        });
    }
    private void ocistiBoje() {
        nazivKategorije.setBackgroundColor(0);
        ikona.setBackgroundColor(0);
    }

    private void dodajListenerNaButtons() {
        dodajIkonuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });

        dodajKategorijuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validniPodaci()) return;
                trenutniNaziv = nazivKategorije.getText().toString();
                trenutnaIkona = ikona.getText().toString();
                uzmiNoveKategorije();
            }
        });
    }

    private void uzmiNoveKategorije() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
        intent.putExtra("TOKEN", TOKEN);
        intent.putExtra("akcija", GetRequestIntentService.AKCIJA_KATEGORIJE);
        intent.putExtra("receiver", receiver);
        startService(intent);
    }

    private boolean validniPodaci() {
        boolean ispravniPodaci = true;

        if (nazivKategorije.getText() == null || nazivKategorije.getText().toString().length() == 0) {
            ispravniPodaci = false;
            nazivKategorije.setBackgroundColor(Color.RED);
            nazivKategorije.setError("Morate unijeti naziv kategorije");
        }

        if (ikona.getText() == null || ikona.getText().toString().length() == 0) {
            ispravniPodaci = false;
            ikona.setBackgroundColor(Color.RED);
            ikona.setError("Morate unijeti ikonu");
        }

        return ispravniPodaci;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.AKCIJA_KATEGORIJE) {
                azuriraneKategorije = new ArrayList<>();
                azuriraneKategorije.addAll((ArrayList<Kategorija>) resultData.getSerializable("kategorije"));

                kategorija = new Kategorija(trenutniNaziv, trenutnaIkona);

                for (Kategorija k : azuriraneKategorije) {
                    if (k.getNaziv().equals(kategorija.getNaziv())) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("Unesena kategorija vec postoji");
                        builder.setNeutralButton("OK", null);
                        builder.create().show();
                        return;
                    }
                }

                vratiKategorijuUPrethodnuAktivnost();
            }
        }
    }

    private void vratiKategorijuUPrethodnuAktivnost() {
        Intent intent = new Intent();
        intent.putExtra("kategorija", kategorija);
        intent.putExtra("kategorije", azuriraneKategorije);
        setResult(DodajKvizAkt.ADDED_KATEGORIJA, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("kategorije", azuriraneKategorije);
        setResult(DodajKvizAkt.BACK_FROM_KATEGORIJE, intent);
        finish();
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        if (icons.length != 0) {
            ikona.setText(Integer.toString(icons[0].getId()));
            ocistiBoje();
        }
    }

}
