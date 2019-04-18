package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class IgrajKvizAkt extends AppCompatActivity implements InformacijeFrag.porukaOdInformacija, PitanjeFrag.porukaOdPitanja {

    private InformacijeFrag informacijeFrag;
    private PitanjeFrag pitanjeFrag;

    private static final String INFO_TAG = "info";
    private static final String PITANJE_TAG = "pitanje";

    private Kviz kviz = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        Intent intent = getIntent();
        kviz = (Kviz)intent.getSerializableExtra("kviz");

        FragmentManager manager = getSupportFragmentManager();

        pitanjeFrag = (PitanjeFrag) manager.findFragmentByTag(PITANJE_TAG);
        if (pitanjeFrag == null) {
            pitanjeFrag = new PitanjeFrag();
            manager.beginTransaction().add(R.id.pitanjePlace, pitanjeFrag, PITANJE_TAG).commit();
        }

        informacijeFrag = (InformacijeFrag) manager.findFragmentByTag(INFO_TAG);
        if (informacijeFrag == null) {
            informacijeFrag = new InformacijeFrag();

            Bundle bundle = new Bundle();
            bundle.putSerializable("kviz", kviz);
            informacijeFrag.setArguments(bundle);

            manager.beginTransaction().add(R.id.informacijePlace, informacijeFrag, INFO_TAG).commit();
        }

    }


    @Override
    public void porukaOdInformacija(Pitanje pitanje) {
        pitanjeFrag.primiPorukuOdInformacijaFragment(pitanje);
    }

    @Override
    public void porukaOdPitanja(boolean tacanOdgovor) {
       informacijeFrag.primiPorukuOdPitanjaFragment(tacanOdgovor);
    }

    public Kviz dajKviz() {
        return kviz;
    }
}
