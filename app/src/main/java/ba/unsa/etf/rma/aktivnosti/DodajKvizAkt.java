package ba.unsa.etf.rma.aktivnosti;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import ba.unsa.etf.rma.R;

public class DodajKvizAkt extends AppCompatActivity {

    private ListView listaDodanihPitanja = (ListView) findViewById(R.id.lvDodanaPitanja);
    private ListView listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
    private Button dodajKvizButton = (Button) findViewById(R.id.btnDodajKviz);
    private EditText nazivKviza = (EditText) findViewById(R.id.etNaziv);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);




    }
}
