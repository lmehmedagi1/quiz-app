package ba.unsa.etf.rma.fragmenti;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_detail, container, false);

        gridKvizovi = (GridView) view.findViewById(R.id.gridKvizovi);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        kvizAdapter = new GridViewAdapter(view.getContext(), kvizovi);
        gridKvizovi.setAdapter(kvizAdapter);

        azurirajKvizove(new ArrayList<Kviz>());

        dodajListenerNaGrid();
    }

    private void dodajListenerNaGrid() {
        gridKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz odabraniKviz = (Kviz) parent.getItemAtPosition(position);
                if (odabraniKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt)getActivity()).otvoriNovuAktivnost(null);
                else
                    ((KvizoviAkt)getActivity()).otvoriAktivnostZaIgranjeKviza(odabraniKviz);
            }
        });

        gridKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Kviz odabraniKviz = (Kviz) parent.getItemAtPosition(position);
                if (odabraniKviz.getNaziv().equals("Dodaj kviz"))
                    ((KvizoviAkt)getActivity()).otvoriNovuAktivnost(null);
                else
                    ((KvizoviAkt)getActivity()).otvoriNovuAktivnost(odabraniKviz);
                return true;
            }
        });
    }

    public void primiPorukuOdListeFrag(String nazivKategorije) {
        ArrayList<Kviz> noviKvizovi = new ArrayList<>();

        for (Kviz k : kvizovi) {
            if (k.getKategorija().getNaziv().equals(nazivKategorije))
                noviKvizovi.add(k);
        }
        noviKvizovi.add(new Kviz("Dodaj kviz", null, new Kategorija("-10", "-10")));

        kvizAdapter = new GridViewAdapter(view.getContext(), kvizovi);
        gridKvizovi.setAdapter(kvizAdapter);
    }

    public void azurirajKvizove(ArrayList<Kviz> noviKvizovi) {
        noviKvizovi.add(new Kviz("Dodaj kviz", null, new Kategorija("-10", "-10")));
        kvizovi.clear();
        kvizovi.addAll(noviKvizovi);
        kvizAdapter.notifyDataSetChanged(); //možda će trebat pozvat primiPoruku i držat trenutnuKategoriju
    }
}
