package ba.unsa.etf.rma.fragmenti;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class ListaFrag extends Fragment {


    public interface porukaOdListeFrag {
        void porukaOdListeFrag (ArrayList<Kviz> kvizovi);
    }

    private porukaOdListeFrag callback;

    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    private ListView listaKategorija;
    private ArrayAdapter<Kategorija> kategorijaAdapter = null;

    private View view = null;


    public ListaFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;
        view = inflater.inflate(R.layout.fragment_lista, container, false);

        listaKategorija = (ListView) view.findViewById(R.id.listaKategorija);

        kategorije = new ArrayList<>();

        kategorijaAdapter = new ArrayAdapter<Kategorija>(view.getContext(), android.R.layout.simple_list_item_1, kategorije);
        listaKategorija.setAdapter(kategorijaAdapter);
        dodajListenerNaListu();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (view == null) return;

        ((KvizoviAkt)getActivity()).azurirajPodatke(null);
    }

    private void dodajListenerNaListu() {
        listaKategorija.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                Kategorija odabranaKategorija = (Kategorija) adapter.getItemAtPosition(position);
                ((KvizoviAkt)getActivity()).azurirajPodatke(odabranaKategorija);
            }
        });

    }

    public void azurirajKategorije(ArrayList<Kategorija> noveKategorije, ArrayList<Kviz> kvizovi) {
        kategorije.clear();
        kategorije.addAll(noveKategorije);
        if (kategorijaAdapter == null)
            return;
        kategorijaAdapter.notifyDataSetChanged();
        if (kvizovi != null)
            callback.porukaOdListeFrag(kvizovi);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof porukaOdListeFrag)
            callback = (porukaOdListeFrag) context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
