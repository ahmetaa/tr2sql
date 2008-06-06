package tr2sql.db;

import net.zemberek.erisim.Zemberek;
import net.zemberek.islemler.cozumleme.CozumlemeSeviyesi;
import net.zemberek.yapi.Kelime;
import net.zemberek.yapi.Kok;
import net.zemberek.tr.yapi.ek.TurkceEkAdlari;
import tr2sql.SozlukIslemleri;
import tr2sql.cozumleyici.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class TurkceSQLCozumleyici {

    private VeriTabani veriTabani;
    private Map<String, Kavram> stringKavramTablosu = new HashMap<String, Kavram>();
    private Map<Kok, Kavram> kokKavramTablosu = new HashMap<Kok, Kavram>();

    private Zemberek zemberek;

    public TurkceSQLCozumleyici(Zemberek zemberek,
                                String veriTabaniDosyasi,
                                String kavramDosyasi) throws IOException {
        this.zemberek = zemberek;
        SozlukIslemleri sozlukIslemleri = new SozlukIslemleri(zemberek.dilBilgisi().kokler());
        KavramOkuyucu kavramOkuyucu = new KavramOkuyucu(sozlukIslemleri);

        // kavramlari okuyup tablolara at.
        Set<Kavram> kavramlar = kavramOkuyucu.oku(kavramDosyasi);
        for (Kavram kavram : kavramlar) {
            stringKavramTablosu.put(kavram.getAd(), kavram);
            for (Kok kok : kavram.getEsKokler()) {
                kokKavramTablosu.put(kok, kavram);
            }
        }
        veriTabani = new XmlVeriTabaniBilgisiOkuyucu(stringKavramTablosu).oku(veriTabaniDosyasi);
    }

    public VeriTabani getVeriTabani() {
        return veriTabani;
    }

    public Map<String, Kavram> getStringKavramTablosu() {
        return stringKavramTablosu;
    }

    public List<SorguCumleBileseni> sorguCumleBilesenleriniAyir(String giris) {
        return new BasitCumleCozumleyici(giris).bilesenler();
    }

    public String sqlDonusum(String giris) {
        SorguTasiyici st = new BasitCozumleyici(sorguCumleBilesenleriniAyir(giris)).cozumle();
        return new MsSqlDonusturucu().donustur(st);
    }

    class BasitCumleCozumleyici {

        private List<String> cumleParcalari = new ArrayList<String>();

        public BasitCumleCozumleyici(String giris) {

            // birden fazla bosluklari tek bosluga indir.
            String c = giris.replaceAll("[ ]+", " ").trim();

            // "ya da" ikilisini "veya" ile degistirelim. coklu kelimelerle ugrasmamak icin.
            c = c.replaceAll("ya da", "veya");

            // bu regular expression ile cumledeki kelimeleri parcaliyoruz.
            // eger kelime '' isareti icinde ise parcalanmiyor, butun olarak aliniyor.
            // virgul sembolu de ayrica listede yer aliyor. mesela
            // "adi 'ayse','ali can' olan ogrencileri goster." cumlesinden
            // [adi]['ayse'][,]['ali can'][olan][ogrencileri][goster]
            // parcalari elde edilir..
            Pattern parcalayici = Pattern.compile("('[^']*')|[^ \\t\\n,.]+|,");
            Matcher m = parcalayici.matcher(c);

            while (m.find())
                cumleParcalari.add(m.group());
        }

        private List<SorguCumleBileseni> bilesenler() {
            List<SorguCumleBileseni> bilesenler = new ArrayList<SorguCumleBileseni>();

            for (String s : cumleParcalari) {
                // virgul, ve, veya
                if (s.equals(",") || s.equals("ve") || s.equals("veya")) {
                    bilesenler.add(new BaglacBileseni(s));
                    continue;
                }

                // bilgi bileseni. '' isareti icinde olur.
                if (s.startsWith("'") && s.length() > 2) {
                    BilgiBileseni bilesen = new BilgiBileseni(s.substring(1, s.length() - 1));
                    bilesenler.add(bilesen);
                    continue;
                }

                // diger bilesenleri ortaya cikarmak icin kelime cozumlenip kokunden hangi kavrama
                // karsilik dustugu belirleniyor.
                Kelime[] sonuclar = zemberek.kelimeCozumle(s, CozumlemeSeviyesi.TUM_KOKLER);
                Kavram kavram;
                Kelime kelime;
                if (sonuclar.length > 0) {
                    kelime = sonuclar[0];
                    kavram = kokKavramTablosu.get(kelime.kok());
                } else {
                    bilesenler.add(new TanimsizBilesen(s));
                    continue;
                }
                SorguCumleBileseni bilesen = bilesenBul(kavram, s, kelime);
                bilesenler.add(bilesen);

            }
            return bilesenler;
        }

        private boolean olumsuzlukEkiVarmi(Kelime kel) {
            return kel.ekler().contains(zemberek.dilBilgisi().ekler().ek(TurkceEkAdlari.FIIL_OLUMSUZLUK_ME));
        }

        // kavrama gore sorgu cumle bilesenini bulur.
        private SorguCumleBileseni bilesenBul(Kavram kavram, String s, Kelime kelime) {

            if (kavram == null)
                return new TanimsizBilesen(s);

            Tablo t = veriTabani.kavramaGoreTabloBul(kavram);
            if (t != null)
                return new TabloBileseni(t, kelime);

            IslemTipi tip = IslemTipi.kavramaGoreIslem(kavram);
            if (tip != IslemTipi.TANIMSIZ)
                return new IslemBileseni(tip, kelime);

            List<Kolon> tumKolonlar = veriTabani.tumKolonlar();
            for (Kolon kolon : tumKolonlar) {
                if (kolon.getKavram().equals(kavram))
                    return new KolonBileseni(kolon, kelime);
            }

            KiyasTipi kiyasTipi = KiyasTipi.kavramdanTipBul(kavram);
            boolean olumsuzlukEkiVar = olumsuzlukEkiVarmi(kelime);

            if (kiyasTipi != null) {
                return new KiyaslamaBileseni(kiyasTipi, olumsuzlukEkiVar);
            }

            if (kavram.getAd().equals("OLMAK")) {
                return new OlmakBIleseni(kelime, olumsuzlukEkiVar);
            }

            return new TanimsizBilesen(s);
        }
    }

    public static void main(String[] args) {

    }
}
