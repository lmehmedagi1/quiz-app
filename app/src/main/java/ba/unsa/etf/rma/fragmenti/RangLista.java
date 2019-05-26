package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.HttpGetRequest;
import ba.unsa.etf.rma.klase.HttpPatchRequest;
import ba.unsa.etf.rma.klase.RangListaAdapter;
import ba.unsa.etf.rma.klase.RangListaItem;

public class RangLista extends Fragment {

    private ListView lista;
    private RangListaAdapter adapter = null;
    private String token = "";
    private RangListaItem noviIgrac = null;

    private View v = null;
    private ArrayList<RangListaItem> rangListaItems = new ArrayList<>();
    private ArrayList<RangListaItem> novaRangLista = new ArrayList<>();

    public RangLista() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_rang_lista, container, false);

        String id = UUID.randomUUID().toString();
        Bundle bundle = this.getArguments();
        noviIgrac = (RangListaItem) bundle.getSerializable("item");
        noviIgrac.setIdDokumenta(id);

        token = bundle.getString("token");
        patchIgrac(noviIgrac, 1);

        lista = (ListView) v.findViewById(R.id.listaLV);
        try {
            HttpGetRequest getRequest = new HttpGetRequest();
            ArrayList<Object> result = getRequest.execute(token, "rang lista").get();
            rangListaItems = (ArrayList<RangListaItem>)result.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (RangListaItem rangListaItem : rangListaItems)
            if (rangListaItem.getNazivKviza().equals(noviIgrac.getNazivKviza()))
                novaRangLista.add(rangListaItem);

        Collections.sort(novaRangLista, new Comparator<RangListaItem>() {
            @Override
            public int compare(RangListaItem o1, RangListaItem o2) {
                return (int)(o1.getProcenatTacnih() - o2.getProcenatTacnih());
            }
        });

        for (int i = 0; i<novaRangLista.size(); i++) {
            if (novaRangLista.get(i).getPozicija() != i + 1)
                patchIgrac(novaRangLista.get(i), i + 1);
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new RangListaAdapter(v.getContext(), novaRangLista);
        lista.setAdapter(adapter);
        Log.wtf("FRAGMENT OVAJ", "Udje u ovo");
    }

    private void patchIgrac(RangListaItem igrac, int pozicija) {
        String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Rangliste/" + igrac.getIdDokumenta() + "?access_token=";
        String dokument = "{\"fields\": { \"nazivKviza\": {\"stringValue\": \"" + igrac.getNazivKviza() + "\"}," +
                                         "\"lista\": {\"mapValue\": {\"fields\": { \"pozicija\": { \"integerValue\": \"" + pozicija + "\"}, " +
                                                                                  "\"informacije\": {\"mapValue\": {\"fields\": {\"imeIgraca\": {\"stringValue\": \"" + igrac.getImeIgraca() + "\"}," +
                                                                                                                                "\"procenatTacnih\": {\"doubleValue\": " + igrac.getProcenatTacnih() + "}}}}}}}}}";
        HttpPatchRequest patchRequest = new HttpPatchRequest();
        patchRequest.execute(url, token, dokument);
    }
}