package ba.unsa.etf.rma.fragmenti;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.RangListaAdapter;
import ba.unsa.etf.rma.klase.RangListaItem;

public class RangLista extends Fragment {

    private ListView lista;
    private RangListaAdapter adapter = null;

    private View v = null;

    private ArrayList<RangListaItem> rangListaItems = new ArrayList<>();


    public RangLista() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_pitanje, container, false);
        lista = (ListView) v.findViewById(R.id.listaLV);

        Bundle bundle = this.getArguments();
        rangListaItems = (ArrayList<RangListaItem>) bundle.getSerializable("rangLista");

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new RangListaAdapter(v.getContext(), rangListaItems);
        lista.setAdapter(adapter);
    }

    public void primiPodatke(String ime, double procenat) {

    }
}