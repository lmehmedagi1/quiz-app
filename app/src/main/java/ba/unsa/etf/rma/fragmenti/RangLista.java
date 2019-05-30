package ba.unsa.etf.rma.fragmenti;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import ba.unsa.etf.rma.klase.GetRequestIntentService;
import ba.unsa.etf.rma.klase.GetRequestResultReceiver;
import ba.unsa.etf.rma.klase.HttpGetRequest;
import ba.unsa.etf.rma.klase.HttpPatchRequest;
import ba.unsa.etf.rma.klase.RangListaAdapter;
import ba.unsa.etf.rma.klase.RangListaItem;

public class RangLista extends Fragment implements GetRequestResultReceiver.Receiver {

    private ListView lista;
    private RangListaAdapter adapter = null;
    private String token = "";
    private RangListaItem noviIgrac = null;

    private View v = null;
    private ArrayList<RangListaItem> novaRangLista = new ArrayList<>();

    private GetRequestResultReceiver receiver = null;

    public RangLista() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_rang_lista, container, false);

        Log.wtf("TAG TAG TAG", "On create view rang liste");

        String id = UUID.randomUUID().toString();
        Bundle bundle = this.getArguments();
        noviIgrac = (RangListaItem) bundle.getSerializable("item");
        noviIgrac.setIdDokumenta(id);

        token = bundle.getString("token");
        patchIgrac(noviIgrac, 1);

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(RangLista.this);

        lista = (ListView) v.findViewById(R.id.listaLV);
        ucitajRangListu();
        return v;
    }

    private void ucitajRangListu() {
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), GetRequestIntentService.class);
        intent.putExtra("TOKEN", token);
        intent.putExtra("akcija", GetRequestIntentService.AKCIJA_RANGLISTE);
        intent.putExtra("nazivkviza", noviIgrac.getNazivKviza());
        intent.putExtra("receiver", receiver);
        getActivity().startService(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new RangListaAdapter(v.getContext(), novaRangLista);
        lista.setAdapter(adapter);
        Log.wtf("FRAGMENT OVAJ", "on activity created rang listeeeee");
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

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.RANGLISTE_UPDATE) {

                novaRangLista.clear();
                novaRangLista.addAll((ArrayList<RangListaItem>)resultData.getSerializable("ranglista"));

                Log.wtf("vkifhobher", "Uzele se kategorije u rang listi ima ih " + novaRangLista.size());

                Collections.sort(novaRangLista, new Comparator<RangListaItem>() {
                    @Override
                    public int compare(RangListaItem o1, RangListaItem o2) {
                        return (int)(o2.getProcenatTacnih() - o1.getProcenatTacnih());
                    }
                });

                for (int i = 0; i<novaRangLista.size(); i++) {
                    if (novaRangLista.get(i).getPozicija() != i + 1) {
                        novaRangLista.get(i).setPozicija(i + 1);
                        // patchIgrac(novaRangLista.get(i), i + 1);
                    }
                }

                adapter = new RangListaAdapter(v.getContext(), novaRangLista);
                lista.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }


}