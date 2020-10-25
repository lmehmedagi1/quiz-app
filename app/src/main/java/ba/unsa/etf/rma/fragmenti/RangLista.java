package ba.unsa.etf.rma.fragmenti;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.baza.GetRequestIntentService;
import ba.unsa.etf.rma.baza.GetRequestResultReceiver;
import ba.unsa.etf.rma.adapteri.RangListaAdapter;
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

        String id = UUID.randomUUID().toString();
        Bundle bundle = this.getArguments();
        noviIgrac = (RangListaItem) bundle.getSerializable("item");
        noviIgrac.setIdDokumenta(id);

        receiver = new GetRequestResultReceiver(new Handler());
        receiver.setReceiver(RangLista.this);

        lista = (ListView) v.findViewById(R.id.listaLV);

        token = bundle.getString("token");

        KvizoviAkt.getDatabaseHelper().dodajRangListItem(noviIgrac);

        if (!KvizoviAkt.isOnline) {
            ucitajRangListuIzSQLite();
        }
        else {
            String url = "https://firestore.googleapis.com/v1/projects/rma18174-firebase/databases/(default)/documents/Rangliste/" + noviIgrac.getIdDokumenta() + "?access_token=";
            String dokument = "{\"fields\": { \"nazivKviza\": {\"stringValue\": \"" + noviIgrac.getNazivKviza() + "\"}," +
                    "\"lista\": {\"mapValue\": {\"fields\": { \"pozicija\": { \"integerValue\": \"" + noviIgrac.getPozicija() + "\"}, " +
                    "\"informacije\": {\"mapValue\": {\"fields\": {\"imeIgraca\": {\"stringValue\": \"" + noviIgrac.getImeIgraca() + "\"}," +
                    "\"procenatTacnih\": {\"doubleValue\": " + noviIgrac.getProcenatTacnih() + "}}}}}}}}}";

            Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), GetRequestIntentService.class);
            intent.putExtra("TOKEN", token);
            intent.putExtra("akcija", GetRequestIntentService.AKCIJA_PATCH_IGRAC);
            intent.putExtra("url", url);
            intent.putExtra("dokument", dokument);
            intent.putExtra("receiver", receiver);
            getActivity().startService(intent);
        }

        return v;
    }

    private void ucitajRangListuIzSQLite() {
        novaRangLista.clear();
        novaRangLista.addAll(KvizoviAkt.getDatabaseHelper().dajRangListuZaKviz(noviIgrac.getNazivKviza()));

        Collections.sort(novaRangLista, new Comparator<RangListaItem>() {
            @Override
            public int compare(RangListaItem o1, RangListaItem o2) {
                return (int)(o2.getProcenatTacnih()*100.0 - o1.getProcenatTacnih()*100.0);
            }
        });

        for (int i = 0; i<novaRangLista.size(); i++) {
            if (novaRangLista.get(i).getPozicija() != i + 1) {
                novaRangLista.get(i).setPozicija(i + 1);
            }
        }

        adapter = new RangListaAdapter(v.getContext(), novaRangLista);
        lista.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void ucitajRangListuIzFirebasa() {
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
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData != null) {
            if (resultCode == GetRequestIntentService.AKCIJA_RANGLISTE) {

                novaRangLista.clear();
                novaRangLista.addAll((ArrayList<RangListaItem>)resultData.getSerializable("ranglista"));

                Collections.sort(novaRangLista, new Comparator<RangListaItem>() {
                    @Override
                    public int compare(RangListaItem o1, RangListaItem o2) {
                        return (int)(o2.getProcenatTacnih()*100.0 - o1.getProcenatTacnih()*100.0);
                    }
                });

                for (int i = 0; i<novaRangLista.size(); i++) {
                    if (novaRangLista.get(i).getPozicija() != i + 1) {
                        novaRangLista.get(i).setPozicija(i + 1);
                    }
                }

                adapter = new RangListaAdapter(v.getContext(), novaRangLista);
                lista.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
            else if (resultCode == GetRequestIntentService.AKCIJA_PATCH_IGRAC) {
                ucitajRangListuIzFirebasa();
            }
        }
    }


}