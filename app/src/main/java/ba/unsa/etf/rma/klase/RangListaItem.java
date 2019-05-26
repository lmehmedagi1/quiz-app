package ba.unsa.etf.rma.klase;

import java.io.Serializable;

public class RangListaItem implements Serializable {
    private String imeIgraca = "";
    private String nazivKviza = "";
    private double procenatTacnih = 0;
    private String idDokumenta = "";

    public RangListaItem() {
    }

    public RangListaItem(String imeIgraca, String nazivKviza, double procenatTacnih) {
        this.imeIgraca = imeIgraca;
        this.nazivKviza = nazivKviza;
        this.procenatTacnih = procenatTacnih; }

    public String getImeIgraca() {
        return imeIgraca;
    }

    public void setImeIgraca(String imeIgraca) {
        this.imeIgraca = imeIgraca;
    }

    public String getNazivKviza() {
        return nazivKviza;
    }

    public void setNazivKviza(String nazivKviza) {
        this.nazivKviza = nazivKviza;
    }

    public double getProcenatTacnih() {
        return procenatTacnih;
    }

    public void setProcenatTacnih(double procenatTacnih) {
        this.procenatTacnih = procenatTacnih;
    }

    public String getIdDokumenta() {
        return idDokumenta;
    }

    public void setIdDokumenta(String idDokumenta) {
        this.idDokumenta = idDokumenta;
    }
}
