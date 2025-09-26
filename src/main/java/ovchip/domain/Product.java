package ovchip.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private int productNummer;
    private String naam;
    private String beschrijving;
    private BigDecimal prijs;

    private List<OVChipkaart> kaarten = new ArrayList<>();

    public Product(int productNummer, String naam, String beschrijving, BigDecimal prijs) {
        this.productNummer = productNummer;
        this.naam = naam;
        this.beschrijving = beschrijving;
        this.prijs = prijs;
    }

    public int getProductNummer() { return productNummer; }
    public void setProductNummer(int productNummer) { this.productNummer = productNummer; }

    public String getNaam() { return naam; }
    public void setNaam(String naam) { this.naam = naam; }

    public String getBeschrijving() { return beschrijving; }
    public void setBeschrijving(String beschrijving) { this.beschrijving = beschrijving; }

    public BigDecimal getPrijs() { return prijs; }
    public void setPrijs(BigDecimal prijs) { this.prijs = prijs; }

    public List<OVChipkaart> getKaarten() { return kaarten; }

    public void addKaart(OVChipkaart kaart) {
        if (kaart == null) return;
        if (!kaarten.contains(kaart)) {
            kaarten.add(kaart);
            if (!kaart.getProducts().contains(this)) {
                kaart.getProducts().add(this);
            }
        }
    }

    public void removeKaart(OVChipkaart kaart) {
        if (kaart == null) return;
        if (kaarten.remove(kaart)) {
            kaart.getProducts().remove(this);
        }
    }

    @Override
    public String toString() {
        return "Product{#" + productNummer +
                ", naam='" + naam + '\'' +
                ", prijs=" + prijs +
                ", kaarten=" + kaarten.stream().map(k -> k.getKaartNummer()).toList() +
                "}";
    }
}
