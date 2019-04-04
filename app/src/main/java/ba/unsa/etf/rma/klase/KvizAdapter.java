package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ba.unsa.etf.rma.R;

public class KvizAdapter extends ArrayAdapter<Kviz> {

    public KvizAdapter(Context context, List<Kviz> kvizovi) {
        super(context, R.layout.element_liste, kvizovi);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.element_liste, parent, false);

        Kviz kviz = getItem(position);
        TextView tekst = (TextView) customView.findViewById(R.id.tekst);
        ImageView ikona = (ImageView) customView.findViewById(R.id.ikona);

        tekst.setText(kviz.getNaziv());
        ikona.setImageResource(R.drawable.item);

        return customView;
    }

    public View dajElementZaDodavanje(ViewGroup parent) {
        View element = LayoutInflater.from(getContext()).inflate(R.layout.element_liste, parent, false);
        ((TextView) element.findViewById(R.id.tekst)).setText("Dodaj kviz!");
        return element;
    }
}
