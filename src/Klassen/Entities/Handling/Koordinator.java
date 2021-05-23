package Klassen.Entities.Handling;

import java.util.ArrayList;
import java.util.Collections;

// todo: Klassenname ändern, ggf Beschreibung
// todo: prüfe Kollision refactoren -> treffendere Namen

/**
 * Der Koordinator koordiniert verschiede bewegliche Objekte
 * Überprüfungen von Kollisionen mit Rändern und anderen Objekten
 * Bearbeitung der Gegner- und Schussliste
 */
public class Koordinator {

    // Bewegungsrichtung
    private enum xBewegung {
        LINKS,
        RECHTS
    }

    // todo: Verstehen wofür das hier ist
    private xBewegung richtung = xBewegung.RECHTS;

    // Listen von Objekten welche durch Treffer einen Zustand ändern
    private ArrayList<Gegner> gegnerListe;
    private ArrayList<Schuss> schuesseRaumschiff = new ArrayList<Schuss>();
    private ArrayList<Schuss> schuesseMonster = new ArrayList<Schuss>();

    // Koordinaten der Ränder
    private final double RANDRECHTS = 590;
    private final double RANDLINKS = 15;
    private final double RANDUNTENMONSTER = 690;
    private final double RANDUNTENSCHUSS = 670;
    private final double RANDOBEN = 80;

    // Startwert des Punktestands
    private int score = 0;

    public Koordinator() {
    }

    public int erhalteScore() {
        return score;
    }

    private void setzteRichtung(xBewegung richtung) {
        this.richtung = richtung;
    }

    public final double erhalteRandRechts() {
        return RANDRECHTS;
    }

    public final double erhalteRandLinks() {
        return RANDLINKS;
    }

    /**
     * Schuss in momentan existierende Schussliste aufnehmen
     *
     * @param schuss Schuss des Raumschiffs auf die Gegner
     */
    public void hinzufuegenSchussRaumschiff(Schuss schuss) {
        schuesseRaumschiff.add(schuss);
    }

    /**
     * Monster, welches am nächsten zum Raumschiff ist, schießt
     *
     * @param xKoorRaumschiff Position des Raumschiffs um die Nähe zu bestimmen
     */
    public void schiessenMonster(double xKoorRaumschiff) {
        ArrayList<Gegner> naechsteGegner = new ArrayList<Gegner>();

        // nahe Monster auswählen
        for (Gegner gegner : this.gegnerListe) {
            if (gegner.erhalteXKoor() <= xKoorRaumschiff + 10 && gegner.erhalteXKoor() >= xKoorRaumschiff - 10) {
                naechsteGegner.add(gegner);
            }
        }

        double yKoorMonsterMax = 0;

        // Auswahl des Monsters, welches in der untersten Reihe ist
        for (Gegner gegner : naechsteGegner) {
            if (gegner.erhalteYKoor() >= yKoorMonsterMax) {
                yKoorMonsterMax = gegner.erhalteYKoor();
            }
        }

        for (Gegner gegner : naechsteGegner) {
            if (gegner.erhalteYKoor() == yKoorMonsterMax) {
                schuesseMonster.add(gegner.schiessen());
            }
        }
    }

    /**
     * Überprüfen, ob die Gegner an die Seitenränder gelangen
     *
     * @return boolean Wahr, falls eine Bewegung die Ränder überschreiten würde
     */
    private boolean pruefeKollisionRand() {
        if (richtung == xBewegung.RECHTS) {
            for (Gegner gegner : this.gegnerListe) {
                if (gegner.pruefeKollisionRechts(RANDRECHTS) == true) {
                    return true;
                }
            }
        } else {
            for (Gegner gegner : this.gegnerListe) {
                if (gegner.pruefeKollisionLinks(RANDLINKS) == true) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Die Gegner, je nach ihrer Position, bewegen
     * gegebenenfalls die Bewegungsrichtung ändern
     */
    public void ueberpruefenUndBewegenMonster() {
        // Kollision mit Rand überprüfen, Gegner bewegen und ggf. Richtung wechseln
        switch (richtung) {
            case LINKS:
                if (pruefeKollisionRand() == true) {
                    // Gegner in nächste Zeile versetzen
                    for (Gegner gegner : this.gegnerListe) {
                        gegner.bewegenRunter();
                        gegner.erschaffeObjekt(gegner.erhalteBreite(), gegner.erhalteHoehe());
                    }

                    // Richtungswechsel
                    setzteRichtung(xBewegung.RECHTS);
                } else {
                    // Bewegung in X Dimension
                    for (Gegner gegner : this.gegnerListe) {
                        gegner.bewegenLinks();
                    }
                }
                break;

            case RECHTS:
                if (pruefeKollisionRand() == true) {
                    // Gegner in nächste Zeile versetzen
                    for (Gegner gegner : this.gegnerListe) {
                        gegner.bewegenRunter();
                        gegner.erschaffeObjekt(gegner.erhalteBreite(), gegner.erhalteHoehe());
                    }

                    // Richtungswechsel
                    setzteRichtung(xBewegung.LINKS);
                } else {
                    // Bewegung in X Dimension
                    for (Gegner gegner : this.gegnerListe) {
                        gegner.bewegenRechts();
                    }
                }
                break;
        }
    }

    /**
     * Schüsse des Raumschiffs auf Treffer überprüfen
     * bei Treffern werden der Schuss und das Monster gelöscht, die Punktzahl wird erhöht
     * bei keinem Treffer wird der Schuss am oberen Rand entfernt
     */
    public void ueberpruefenMonsterUndBewegeSchuss() {
        // Strukturen von Elementen, welche entfernt werden
        ArrayList<Schuss> loescheSchuesse = new ArrayList<Schuss>();
        ArrayList<Gegner> loescheGegner = new ArrayList<Gegner>();

        for (Schuss schuss : schuesseRaumschiff) {
            // Schuss des Raumschiffs abfeuern
            schuss.schiessenHoch();

            // Gegner testen, ob sie getroffen wurden
            for (Gegner gegner : this.gegnerListe) {
                if (gegner.pruefeKollision(schuss) == true) {
                    score += gegner.erhaltePunkte();

                    // entfernten Gegner erfassen
                    gegner.entferneObjekt();
                    loescheGegner.add(gegner);

                    // entfernten Schuss erfassen
                    schuss.entferneObjekt();
                    loescheSchuesse.add(schuss);
                }
            }
        }

        // Überprüfen, ob die Schüsse den oberen Rand erreichen
        for (Schuss schuss : schuesseRaumschiff) {
            if (schuss.pruefeTrefferOben(RANDOBEN) == true) {
                schuss.entferneObjekt();
                loescheSchuesse.add(schuss);
            }
        }

        //  Getroffene Schüsse und Gegner aus den Listen entfernen
        schuesseRaumschiff.removeAll(loescheSchuesse);
        this.gegnerListe.removeAll(loescheGegner);
    }

    /**
     * Überprüfen, ob die Schüsse der Gegner das Raumschiff treffen
     * Bei einem Treffer den Schuss aus der Schussliste entfernen
     *
     * @param raumschiff Raumschiff, welches gegen die Gegner kämpft
     * @return boolean Wahr, falls das Raumschiff getroffen wurde
     */
    public boolean ueberpruefenRaumschiffUndBewegeSchuss(Raumschiff raumschiff) {
        ArrayList<Schuss> loescheSchuesse = new ArrayList<Schuss>();

        // Treffer-Fall
        for (Schuss schuss : schuesseMonster) {
            // Schuss der Monster abfeuern
            schuss.schiessenRunter();

            if (raumschiff.pruefeKollision(schuss)) {
                return true;
            }
        }

        // Schuss verfehlt -> Objekt entfernen
        for (Schuss schuss : schuesseMonster) {
            if (schuss.pruefeTrefferUnten(RANDUNTENSCHUSS) == true) {
                schuss.entferneObjekt();
                loescheSchuesse.add(schuss);
            }
        }

        schuesseMonster.removeAll(loescheSchuesse);
        return false;
    }

    /**
     * Erstellung einer neuen Welle, wenn eine Welle besiegt wurde
     *
     * @return boolean Wahr, wenn eine neue Welöe erstellt werden muss
     */
    public boolean neueMonsterListeNotwendig() {
        if (this.gegnerListe.isEmpty()) {
            return true;
        }
        return false;
    }

    // todo: bessere Erklärung

    /**
     * Neue Gegnerliste übergeben
     *
     * @param gegner Gegenspieler zum Raumschiff -> muss besiegt werden
     */
    public void neueMonsterListeUebergeben(ArrayList<Gegner> gegner) {
        // todo: Reverse erklären
        Collections.reverse(gegner);

        this.gegnerListe = gegner;
        this.setzteRichtung(xBewegung.RECHTS);
    }

    /**
     * Spiel beenden, falls Niederlagekonditionen erfüllt werden
     *
     * @return boolean Wahr, wenn das Spiel beendet werden soll
     */
    public boolean gameOver() {
        for (Gegner gegner : this.gegnerListe) {
            if (gegner.pruefeKollisionUnten(RANDUNTENMONSTER) == true) {
                return true;
            }
        }

        return false;
    }
}
