package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.klase.HttpPatchRequest;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.klase.RangListaItem;

public class IgrajKvizAkt extends AppCompatActivity implements InformacijeFrag.porukaOdInformacija, PitanjeFrag.porukaOdPitanja {

    private InformacijeFrag informacijeFrag;
    private PitanjeFrag pitanjeFrag;
    private RangLista rangListaFrag;
    private ArrayList<RangListaItem> rangListaItems = new ArrayList<>();

    private static final String INFO_TAG = "info";
    private static final String PITANJE_TAG = "pitanje";
    private static final String RANG_LISTA_TAG = "ranglista";

    private Kviz kviz = null;
    private String token = "";

    private FragmentManager manager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) return;

        setContentView(R.layout.activity_igraj_kviz_akt);

        Intent intent = getIntent();
        kviz = (Kviz)intent.getSerializableExtra("kviz");
        token = intent.getStringExtra("token");
        rangListaItems = (ArrayList<RangListaItem>) intent.getSerializableExtra("rangLista");


        manager = getSupportFragmentManager();

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
    public void porukaOZadnjemPitanju(String ime, double procenat) {
        String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Rangliste?access_token=";
        String dokument = "{\"fields\": { \"nazivKviza\": {\"stringValue\": \"" + kviz.getNaziv() + "\"}," +
                                         "\"lista\": {\"mapValue\": {\"fields\": { \"pozicija\": { \"integerValue\": \"0\"}, " +
                                                                                  "\"informacije\": {\"mapValue\": {\"fields\": {\"imeIgraca\": {\"stringValue\": \"" + ime + "\"}," +
                                                                                                                                "\"procenatTacnih\": {\"integerValue\": \"" + procenat + "\"}}}}}}}}}}";
        HttpPatchRequest postRequest = new HttpPatchRequest();
        postRequest.execute(url, token, dokument);

        ArrayList<RangListaItem> novaRangLista = new ArrayList<>();

        for (RangListaItem rangListaItem : rangListaItems)
            if (rangListaItem.getNazivKviza().equals(kviz.getNaziv()))
                novaRangLista.add(rangListaItem);

        novaRangLista.add(new RangListaItem(ime, kviz.getNaziv(), procenat));
        Collections.sort(novaRangLista, new Comparator<RangListaItem>() {
            @Override
            public int compare(RangListaItem o1, RangListaItem o2) {
                return (int)(o1.getProcenatTacnih() - o2.getProcenatTacnih());
            }
        });

        rangListaFrag = (RangLista) manager.findFragmentByTag(RANG_LISTA_TAG);
        if (rangListaFrag == null) {
            rangListaFrag = new RangLista();
            Bundle bundle = new Bundle();
            bundle.putSerializable("rangLista", novaRangLista);
            informacijeFrag.setArguments(bundle);
            manager.beginTransaction().add(R.id.pitanjePlace, rangListaFrag, RANG_LISTA_TAG).commit();
        }
    }

    @Override
    public void porukaOdPitanja(boolean tacanOdgovor) {
       informacijeFrag.primiPorukuOdPitanjaFragment(tacanOdgovor);
    }

    public Kviz dajKviz() {
        return kviz;
    }
}
