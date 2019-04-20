package ba.unsa.etf.rma.klase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maltaisn.icondialog.IconHelper;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;

public class GridViewAdapter extends ArrayAdapter<Kviz> {

    public GridViewAdapter(Context context, ArrayList<Kviz> kvizovi) {
        super(context, R.layout.grid_view_item, kvizovi);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.grid_view_item, parent, false);

        final Kviz kviz = getItem(position);
        TextView nazivKvizaTV = (TextView) customView.findViewById(R.id.nazivKvizaTV);
        TextView brojPitanjaTV = (TextView) customView.findViewById(R.id.brojPitanjaTV);
        final ImageView ikona = (ImageView) customView.findViewById(R.id.ikonicaKategorije);


        nazivKvizaTV.setText(kviz.getNaziv());
        brojPitanjaTV.setText(String.valueOf(kviz.getPitanja().size()));
        if (kviz.getKategorija().getNaziv().equals("Svi") || kviz.getKategorija().getId().equals("-1")) ikona.setImageResource(R.drawable.svi_icon);
        else if (kviz.getKategorija().getNaziv().equals("Dodaj kviz")) ikona.setImageResource(R.drawable.dodaj_icon);
        else {
            final IconHelper iconHelper = IconHelper.getInstance(getContext());
            iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                @Override
                public void onDataLoaded() {
                    ikona.setImageDrawable(iconHelper.getIcon(Integer.parseInt(kviz.getKategorija().getId())).getDrawable(getContext()));
                }
            });
        }

        return customView;
    }

    public View dajElementZaDodavanje(ViewGroup parent) {
        View element = LayoutInflater.from(getContext()).inflate(R.layout.element_liste_kvizova, parent, false);
        ((TextView) element.findViewById(R.id.brojPitanjaTV)).setText("Dodaj kviz");
        ((ImageView) element.findViewById(R.id.ikonicaKategorije)).setImageResource(R.drawable.dodaj_icon);
        ((TextView) element.findViewById(R.id.brojPitanjaTV)).setText("");
        return element;
    }
}
