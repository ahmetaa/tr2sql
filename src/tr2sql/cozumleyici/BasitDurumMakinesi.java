package tr2sql.cozumleyici;

import tr2sql.db.Kolon;
import tr2sql.db.KolonKisitlamaBileseni;
import tr2sql.db.SQLUretimHatasi;
import tr2sql.db.SorguTasiyici;

import java.util.ArrayList;
import java.util.List;

public class BasitDurumMakinesi {

    public enum Durum {
        BASLA,
        KOLON_ALINDI,
        COKLU_KOLON_ALINDI,
        BILGI_ALINDI,
        COKLU_BILGI_ALINDI,
        KIYAS_ALINDI,
        SONUC_KOLONU_ALINDI,
        OLMAK_ALINDI,
        TABLO_BULUNDU,
        SONUC_KISITLAMA_SAYISI_BEKLE,
        SONUC_KISITLAMA_SAYISI_ALINDI,
        SINIR_BELIRLENDI,
        ISLEM_BELIRLENDI,
        SON
    }

    private Durum suAnkiDurum = Durum.BASLA;

    private SorguTasiyici sorguTasiyici = new SorguTasiyici();

    private List<CumleBileseni> bilesenler;

    private List<KolonKisitlamaBileseni> kolonBilesenleri =
            new ArrayList<KolonKisitlamaBileseni>();

    private List<KolonBileseni> islenenKolonBileenleri = new ArrayList<KolonBileseni>();
    private List<BilgiBileseni> islenenilgiBilesenleri = new ArrayList<BilgiBileseni>();
    private List<KiyaslamaBileseni> islenenKiyaslamaBileseni = new ArrayList<KiyaslamaBileseni>();
    private List<Kolon> sonucKolonlari = new ArrayList<Kolon>();

    // calisma sirasianda olup bitenlerin bir yerde tutulmasini saglar.
    private StringBuffer cozumRaporu = new StringBuffer();

    public BasitDurumMakinesi(List<CumleBileseni> bilesenler) {
        this.bilesenler = bilesenler;
    }

    public SorguTasiyici islet() {
        for (int i = 0; i < bilesenler.size(); i++) {
            CumleBileseni bilesen = bilesenler.get(i);
            if (bilesen.tip == CumleBilesenTipi.TANIMSIZ) {
                raporla("Islenemyen bilesen:" + bilesen.icerik);
                continue;
            }
            suAnkiDurum = gecis(bilesen);
        }
        sorguTasiyici.sonucKolonlari = sonucKolonlari;
        sorguTasiyici.aciklamalar = cozumRaporu.toString();

        return sorguTasiyici;
    }

    private Durum gecis(CumleBileseni bilesen) {

        CumleBilesenTipi gecis = bilesen.tip();

        switch (suAnkiDurum) {

            case BASLA:
                switch (gecis) {
                    case KOLON:
                        return kolonBileseniGecisi(bilesen);
                    case TABLO:
                        return tabloBileseniGecisi(bilesen);
                    case SONUC_MIKTAR:
                        return Durum.SONUC_KISITLAMA_SAYISI_BEKLE;
                }
                break;

            case KOLON_ALINDI:
                switch (gecis) {
                    case KISITLAMA_BILGISI:
                        return bilgiBileseniGecisi(bilesen);
                    case KOLON:
                        break;
                }
                break;

            case COKLU_KOLON_ALINDI:
                switch (gecis) {
                    case KISITLAMA_BILGISI:
                        return bilgiBileseniGecisi(bilesen);
                    case KOLON:
                        break;
                }
                break;

            case COKLU_BILGI_ALINDI:
                break;

            case BILGI_ALINDI:
                switch (gecis) {
                    case OLMAK:
                        break;
                    case KIYASLAYICI:
                        break;
                    case KOLON:
                        break;
                    case KISITLAMA_BILGISI:
                        break;
                }
                break;

            case OLMAK_ALINDI:
                switch (gecis) {
                    case KOLON:
                        break;
                }
                break;

            case KIYAS_ALINDI:
                switch (gecis) {

                }
                break;

            case TABLO_BULUNDU:
                switch (gecis) {
                    case ISLEM:
                        return islemBileseniGecisi(bilesen);
                    case KOLON:
                        Kolon k = ((KolonBileseni) bilesen).getKolon();
                        sonucKolonlari.add(k);
                        return Durum.SONUC_KOLONU_ALINDI;
                    case SONUC_MIKTAR:
                        return Durum.SONUC_KISITLAMA_SAYISI_BEKLE;
                }
                break;

            case SONUC_KISITLAMA_SAYISI_BEKLE:
                switch (gecis) {
                    case SAYI:
                        SayiBileseni sb = (SayiBileseni) bilesen;
                        sorguTasiyici.sonucMiktarKisitlamaDegeri = sb.deger;
                        return Durum.SONUC_KISITLAMA_SAYISI_ALINDI;
                }
                break;

            case SONUC_KISITLAMA_SAYISI_ALINDI:
                switch (gecis) {
                    case TABLO:
                        return tabloBileseniGecisi(bilesen);
                    case ISLEM:
                        return islemBileseniGecisi(bilesen);
                    case KOLON:
                        Kolon k = ((KolonBileseni) bilesen).getKolon();
                        sonucKolonlari.add(k);
                        return Durum.SONUC_KOLONU_ALINDI;
                }
                break;


            case SONUC_KOLONU_ALINDI:
                switch (gecis) {
                    case ISLEM:
                        return islemBileseniGecisi(bilesen);
                    case KOLON:
                        Kolon k = ((KolonBileseni) bilesen).getKolon();
                        sonucKolonlari.add(k);
                        return Durum.SONUC_KOLONU_ALINDI;
                }
                break;

        }
        throw new SQLUretimHatasi("Beklenmeyen cumle bilseni:" + bilesen.toString() + ". " +
                "Su anki durum:" + suAnkiDurum.name());
    }

    private Durum islemBileseniGecisi(CumleBileseni bilesen) {
        IslemBileseni b = (IslemBileseni) bilesen;
        sorguTasiyici.islemTipi = b.getIslem();
        return Durum.ISLEM_BELIRLENDI;
    }

    private Durum tabloBileseniGecisi(CumleBileseni bilesen) {
        TabloBileseni tabloBil = (TabloBileseni) bilesen;
        sorguTasiyici.tablo = tabloBil.getTablo();
        return Durum.TABLO_BULUNDU;
    }

    private Durum kolonBileseniGecisi(CumleBileseni bilesen) {
        KolonBileseni kb = (KolonBileseni) bilesen;
        islenenKolonBileenleri.add(kb);
        if (kb.baglacVar())
            return Durum.COKLU_KOLON_ALINDI;
        else return Durum.KOLON_ALINDI;
    }

    private Durum bilgiBileseniGecisi(CumleBileseni bilesen) {
        BilgiBileseni kb = (BilgiBileseni) bilesen;
        islenenilgiBilesenleri.add(kb);
        if (kb.baglacVar())
            return Durum.COKLU_BILGI_ALINDI;
        else return Durum.BILGI_ALINDI;
    }

    private void raporla(String s) {
        cozumRaporu.append(s).append("\n");
    }
}