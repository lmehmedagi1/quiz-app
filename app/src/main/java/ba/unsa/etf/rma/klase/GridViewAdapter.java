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

    private Context context;

    public GridViewAdapter(Context c, ArrayList<Kviz> kvizovi) {
        super(c, R.layout.grid_view_item, kvizovi);
        context = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View customView = inflater.inflate(R.layout.grid_view_item, parent, false);

        final Kviz kviz = getItem(position);
        TextView nazivKvizaTV = (TextView) customView.findViewById(R.id.nazivKvizaTV);
        TextView brojPitanjaTV = (TextView) customView.findViewById(R.id.brojPitanjaTV);
        final ImageView ikona = (ImageView) customView.findViewById(R.id.ikonicaKategorije);


        nazivKvizaTV.setText(kviz.getNaziv());

        if (kviz.getPitanja() != null)
            brojPitanjaTV.setText(String.valueOf(kviz.getPitanja().size()));
        else
            brojPitanjaTV.setText("");

        if (kviz.getKategorija().getNaziv().equals("Svi") || kviz.getKategorija().getId().equals("-1")) ikona.setImageResource(R.drawable.svi_icon);
        else if (kviz.getNaziv().equals("Dodaj kviz")) ikona.setImageResource(R.drawable.dodaj_icon);
        else {
            final IconHelper iconHelper = IconHelper.getInstance(getContext());
            iconHelper.addLoadCallback(new IconHelper.LoadCallback() {
                @Override
                public void onDataLoaded() {
                    ikona.setImageDrawable(iconHelper.getIcon(Integer.valueOf(kviz.getKategorija().getId())).getDrawable(customView.getContext()));
                }
            });
        }

        return customView;
    }
}
