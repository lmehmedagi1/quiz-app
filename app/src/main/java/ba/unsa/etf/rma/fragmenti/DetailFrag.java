package ba.unsa.etf.rma.fragmenti;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.GridViewAdapter;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;


public class DetailFrag extends Fragment {

    private ArrayList<Kviz> kvizovi = new ArrayList<>();
    private GridView gridKvizovi;
    private GridViewAdapter kvizAdapter = null;
    private View view;


    public DetailFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;
        view = inflater.inflate(R.layout.fragment_detail, container, false);

        gridKvizovi = (GridView) view.findViewById(R.id.gridKvizovi);
        kvizovi = new ArrayList<>();

        Log.wtf("DETAIL", "Pozvan je on create view fragmenta detail" + String.valueOf(kvizovi.size()));


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (view == null) return;

        Log.wtf("On Activity create DETAIL", "On Activity create  DETAIL FRAG");

        dodajListenerNaGrid();
    }

    private void dodajListenerNaGrid() {
        gridKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz odabraniKviz = (Kviz) parent.getItemAtPosition(position);
                if (odabraniKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt)getActivity()).otvoriAktivnostZaDodavanjeKviza(null);
                else
                    ((KvizoviAkt)getActivity()).otvoriAktivnostZaIgranjeKviza(odabraniKviz);
            }
        });

        gridKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz odabraniKviz = (Kviz) parent.getItemAtPosition(position);
                if (odabraniKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt)getActivity()).otvoriAktivnostZaDodavanjeKviza(null);
                else
                    ((KvizoviAkt)getActivity()).otvoriAktivnostZaDodavanjeKviza(odabraniKviz);
                return true;
            }
        });
    }

    public void primiPorukuOdListeFrag(ArrayList<Kviz> noviKvizovi) {
        azurirajKvizove(noviKvizovi);
    }

    public void azurirajKvizove(ArrayList<Kviz> noviKvizovi) {

        Log.wtf("On Activity create LISTAFRAG", "Azuriraj kvizove DETAIL FRAG");

        int i = 0;

        for (i = 0; i<noviKvizovi.size(); i++) {
            if (noviKvizovi.get(i).getNaziv().equals("Dodaj kviz")) {
                if (i == noviKvizovi.size()-1) break;
                noviKvizovi.remove(i);
                i = noviKvizovi.size();
                break;
            }
        }

        if (i == noviKvizovi.size())
            noviKvizovi.add(new Kviz("Dodaj kviz", null, new Kategorija("-10", "-10")));

        kvizovi = noviKvizovi;
        kvizAdapter = new GridViewAdapter(view.getContext(), kvizovi);
        gridKvizovi.setAdapter(kvizAdapter);
    }
}
