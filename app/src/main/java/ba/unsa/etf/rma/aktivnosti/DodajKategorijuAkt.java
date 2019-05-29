package ba.unsa.etf.rma.aktivnosti;

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
import ba.unsa.etf.rma.klase.GetRequestIntentService;
import ba.unsa.etf.rma.klase.GetRequestResultReceiver;
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback, GetRequestResultReceiver.Receiver {

    private EditText nazivKategorije;
    private EditText ikona;
    private Button dodajIkonuButton;
    private Button dodajKategorijuButton;

    private Icon[] selectedIcons;
    private IconDialog iconDialog;

    private ArrayList<Kategorija> kategorije;
    private String TOKEN;
    private GetRequestResultReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        // da tastatura ne pomjeri layout
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        nazivKategorije       = (EditText)findViewById(R.id.etNaziv);
        ikona                 = (EditText)findViewById(R.id.etIkona);
        dodajIkonuButton      = (Button)findViewById(R.id.btnDodajIkonu);
        dodajKategorijuButton = (Button)findViewById(R.id.btnDodajKategoriju);

        ikona.setEnabled(false);

        Intent intent = getIntent();
        kategorije = (ArrayList<Kategorija>)intent.getSerializableExtra("kategorije");
        TOKEN      = intent.getStringExtra("TOKEN");

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
                vratiKategorijuUPrethodnuAktivnost();
            }
        });
    }

    private boolean validniPodaci() {
        boolean ispravniPodaci = true;

        if (nazivKategorije.getText() == null || nazivKategorije.getText().toString().length() == 0) {
            ispravniPodaci = false;
            nazivKategorije.setBackgroundColor(Color.RED);
        }
        else {

            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, GetRequestIntentService.class);
            intent.putExtra("TOKEN", TOKEN);
            intent.putExtra("trebaKategorije", true);

            receiver = new GetRequestResultReceiver(new Handler());
            receiver.setReceiver(this);

            intent.putExtra("receiver", receiver);
            startService(intent);

            // možda treba sad ovdje alert dialog al to kasnije

            for (Kategorija k : kategorije) {
                if (nazivKategorije.getText().toString().equals(k.getNaziv())) {
                    ispravniPodaci = false;
                    nazivKategorije.setBackgroundColor(Color.RED);
                }
            }
        }

        if (ikona.getText() == null || ikona.getText().toString().length() == 0) {
            ispravniPodaci = false;
            ikona.setBackgroundColor(Color.RED);
        }

        return ispravniPodaci;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == GetRequestIntentService.KATEGORIJE_UPDATE) {
            ArrayList<Kategorija> noveKategorije = (ArrayList<Kategorija>) resultData.getSerializable("kategorije");

            kategorije.clear();
            kategorije.add(new Kategorija("Svi", "-1"));
            kategorije.addAll(noveKategorije);
        }
    }

    private void vratiKategorijuUPrethodnuAktivnost() {
        Kategorija kategorija = new Kategorija(nazivKategorije.getText().toString(), ikona.getText().toString());

        Intent intent = new Intent();
        intent.putExtra("kategorija", kategorija);
        intent.putExtra("kategorije", kategorije);
        setResult(2, intent);
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
