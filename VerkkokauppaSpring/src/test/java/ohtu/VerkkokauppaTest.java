package ohtu;

import ohtu.verkkokauppa.Kauppa;
import ohtu.verkkokauppa.Pankki;
import ohtu.verkkokauppa.Tuote;
import ohtu.verkkokauppa.Varasto;
import ohtu.verkkokauppa.Viitegeneraattori;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author amoroz
 */
public class VerkkokauppaTest {

    //Asiointi yhdellä tuotteella
    @Test
    public void ostoksenPaaytyttyaPankinMetodiaTilisiirtoKutsutaan() {
        Pankki pankki = mock(Pankki.class);
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        Varasto varasto = mock(Varasto.class);
        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        Kauppa k = new Kauppa(varasto, pankki, viite);

        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        //Maksaja, viite, tilinumero, pankin tili, ostoskorin kokonaishinta
        verify(pankki).tilisiirto("pekka", 42, "12345", "33333-44455", 5);
    }

    @Test
    public void asiointiKahdellaEriTuotteella() {
        Pankki pankki = mock(Pankki.class);
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        //Määritellään viitegeneraattorin palauttamaksi arvoksi 2
        when(viite.uusi()).thenReturn(2);
        Varasto varasto = mock(Varasto.class);
        //Määritellään tuotteet seuraavasti
        //(id)nro 1 - maito, hinta 2, saldo 2
        //(id)nro 2 - kahvi, hinta 4, saldo 1
        when(varasto.saldo(1)).thenReturn(2);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 2));
        when(varasto.saldo(2)).thenReturn(2);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(2, "kahvi", 4));

        Kauppa smarket = new Kauppa(varasto, pankki, viite);
        //Lisätään koriin yksi kpl id 1 (maito) ja yksi kpl id 2 (kahvi) tuotteita
        //ALOITA ASIOINTI -> Ostoskori alustetaan -> Ostoskorin ARRAYLIST alustetaan
        smarket.aloitaAsiointi();
        smarket.lisaaKoriin(1);
        smarket.lisaaKoriin(2);
        //Parametrinä käytännössä luokkien ulkopuolinen maksaja (asiakas)
        smarket.tilimaksu("Anton", "678910");
        verify(pankki).tilisiirto("Anton", 2, "678910", "33333-44455", 6);
    }

    @Test
    public void asiointiKahdellaSamallaTuotteella() {
        Pankki pan = mock(Pankki.class);
        Viitegeneraattori vii = mock(Viitegeneraattori.class);
        Varasto var = mock(Varasto.class);
        //Tilisiirron tuleva viite tulee olemaan 1
        when(vii.uusi()).thenReturn(1);

        when(var.saldo(1)).thenReturn(2);
        when(var.haeTuote(1)).thenReturn(new Tuote(1, "kahvi", 4));

        Kauppa spar = new Kauppa(var, pan, vii);
        spar.aloitaAsiointi();
        spar.lisaaKoriin(1);
        spar.lisaaKoriin(1);
        spar.tilimaksu("Anton", "012345");
        verify(pan).tilisiirto("Anton", 1, "012345", "33333-44455", 8);
    }

    @Test
    public void asiointiKahdellaTuotteellaMuttaToinenOnLoppu() {
        Pankki pan = mock(Pankki.class);
        Viitegeneraattori vii = mock(Viitegeneraattori.class);
        Varasto var = mock(Varasto.class);
        //Tilisiirron tuleva viite tulee olemaan 5
        when(vii.uusi()).thenReturn(5);
        when(var.saldo(1)).thenReturn(2);
        when(var.haeTuote(1)).thenReturn(new Tuote(1, "kahvi", 4));

        //Loppuunmyyty/Olematon tuote
        when(var.saldo(2)).thenReturn(0);
        when(var.haeTuote(2)).thenReturn(new Tuote(2, "Oltermanni", 5));

        Kauppa walmart = new Kauppa(var, pan, vii);
        walmart.aloitaAsiointi();
        walmart.lisaaKoriin(1);
        //Yritetään ostaa Oltermannia - sitä ei kuitenkaan ole olemassa
        walmart.lisaaKoriin(2);
        walmart.tilimaksu("Anton", "0123456789");
        verify(pan).tilisiirto("Anton", 5, "0123456789", "33333-44455", 4);
    }

    @Test
    public void uusiAsiointiNollaaEdellisenSekaTiliiirtoLuoUudenViitteen() {
        Pankki pankki = mock(Pankki.class);
        //Viite palauttaa aina arvon 0, mockiton tehtävänä ei ole 
        //selvästikään tarkastaa arvoja, vaan toiminnallisuutta
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        Varasto varasto = mock(Varasto.class);

        when(varasto.saldo(1)).thenReturn(2);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "kahvi", 4));
        Kauppa kauppa = new Kauppa(varasto, pankki, viite);
        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);

        kauppa.tilimaksu("Anton", "123");
        verify(pankki).tilisiirto("Anton", 0, "123", "33333-44455", 4);

        //Viite on sama kuin edellisessä ostoksessa (mock ei muuta arvoja ellei niin haluta explisiittisesti), mutta edellisen ostoksen hinta
        //ei näy uuden ostoksen hinnassa
        kauppa.aloitaAsiointi();
        kauppa.tilimaksu("Toni", "456");
        verify(pankki).tilisiirto("Toni", 0, "456", "33333-44455", 0);

        //Kauppa pyytää uuden viitenumeron jokaiselle tapahtumalle
        //tapahtumia on tässä testissä kaksi
        verify(viite, times(2)).uusi();
    }

    @Test
    public void asiointiTuotteellaJaTuotteenPoistolla() {
        Pankki pankki = mock(Pankki.class);
        Viitegeneraattori viite = mock(Viitegeneraattori.class);
        Varasto varasto = mock(Varasto.class);
        Kauppa kauppa = new Kauppa(varasto, pankki, viite);

        when(viite.uusi()).thenReturn(5);
        when(varasto.saldo(1)).thenReturn(2);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "kahvi", 2));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.lisaaKoriin(1);
        kauppa.poistaKorista(1);

        kauppa.tilimaksu("Anton", "12345");
        verify(pankki).tilisiirto("Anton", 5, "12345", "33333-44455", 4);
    }

}
