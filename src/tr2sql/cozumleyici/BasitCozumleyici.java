package tr2sql.cozumleyici;

import tr2sql.db.SorguTasiyici;
import tr2sql.db.Kolon;

import java.util.List;


public class BasitCozumleyici {

    private List<SorguCumleBileseni> bilesenler;

    public BasitCozumleyici(List<SorguCumleBileseni> bilesenler) {
        this.bilesenler = bilesenler;
    }

    public SorguTasiyici cozumle() {


        SorguTasiyici st = new SorguTasiyici();

        // basit bicimde tablo ve islem verisini ekle.
        for (SorguCumleBileseni bilesen : bilesenler) {
            switch (bilesen.tip()) {
                case TABLO:
                    st.tablo = ((TabloBileseni) bilesen).tablo;
                    break;
                case ISLEM:
                    st.islemTipi = ((IslemBileseni) bilesen).islem;
                    break;
            }
        }

        for (int i = 0; i < bilesenler.size(); i++) {
            SorguCumleBileseni bilesen = bilesenler.get(i);

            Kolon k = null;
            if (bilesen.tip() == CumleBilesenTipi.KOLON) {
                KolonBileseni kb = (KolonBileseni) bilesen;
                k = kb.kolon;
                continue;
            }
            if(bilesen.tip() == CumleBilesenTipi.KISITLAMA_BILGISI ) {
               
            }

        }

        return st;
    }


    public enum Kalip {

    }




}