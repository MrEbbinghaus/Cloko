# Dokumentation

Folgende Dokumentation handelt von dem Design des Projekts für die "Funktionale Programmierung" im Wintersemester 2016/17.
Dieses Projekt wurde erstellt von Björn Ebbinghaus (bjoern.ebbinghaus@uni-duesseldorf.de).

## Kern
Den Kern von Cloko bildet der Namespace `cloko.core` in ihm sind alle Funktionen enthalten um das Spiel in der REPL zu spielen.

### State
Bei einem Spiel wie diesem ist ein Zustand unabdingbar, dieser wird in einem einzlenen Atom, dem so genannten `game-state` gehalten.
Mit Ausnahme der für den Spieler notwendigen öffentlichen Funktionen sind alle Funktionen ohne Nebeneffekte. Bei jedem Aufruf einer öffentlichen Funktion durch den Spieler wird der Zustand immer
nur genau *einmal* dereferenziert, sei es durch `@` oder aber auch innerhalb von `swap!` oder `reset!`.
In den meisten Fällen wrappen die öffentlichen Funktionen nur eine pure Funktion, die die ganzen Änderungen auf dem `game-state` Wert  macht. Die öffentliche Funktion ist dann nur zum dereferenzieren, bzw zum swappen.

Bei dem Atom handelt es sich um ein reagent/atom, dies verhält  sich praktisch wie das Clojurescript Atom. Nur hält es zusätzlich Informationen, welche Reagent erlaubt Komponenten, die dieses Atom dereferenzieren automatisch neu zu zeichnen. Mehr dazu im Abschnitt Frontend.

### Simplicity
Die meisten Funktionen haben eine einzige Aufgabe und bekommen nicht mehr, als sie unbedingt benötigen, werden also nicht kompliziert _aneinandergekettet._ Zusammengeführt werden diese Funktionen dann in einzelnen Funktionen, wie  z.B.

```clojure
(defn- end-round
  [game-state]
  (-> game-state
      (assoc-in [:whoseTurn] 0)
      (update-in [:world :planets] generate-all-ships)
      (update-in [:movements] update-movements)
      (#(update-in % [:world :planets] fight-for-planets (arrived-fleets (:movements %))))
      (update-in [:movements] clear-movements)
      (update-in [:round] inc)
      (set-remaining-players)
      (#(do
          (when (victory? %) (print-victory-message (first (:players %))))
          %))))
```

Hier kann man gut sehen, dass ein `game-state` der Funktion übergben wird, eine Transformations Pipline durchläuft und dann wieder ausgespuckt wird. Einzelne Bestandteile dieser Pipeline wie Reihenfolge von Schiffgenerierung, Kampfberechnung, u.s.w. können hier einfach ausgetauscht, bzw die Pipeline kann durch weitere Schritte erweitert oder verkürzt werden.

### Tests
Dadurch, dass beinahe alle Funktionen pur sind und nur mit vorhandenen Datenstrukturen gearbeitet worden ist, lassen sich sehr einfach Unit-Tests schreiben. Diese finden sich im Namespace `cloko.core-test`. Die Tests decken die Kern Funktionen

#### Generative Tests
Generative Tests wurden z.B. für die `fight` Funktion verwendet, welche sich im Laufe der Zeit mehrfach verändert hat. Unit Tests sind hier nicht angebracht, da sich durch veränderliche Verteidigungs Boni und unterschiedliche Verrechnung dieser oft die tatsächlichen Ergebnisse ändern. Aus Balancing Gründen sollte diese Funktion auch einfach zu verändern sein.

Dies führt dazu, dass hier generative Tests besser geeignet sind, welche gegen im Spiel niemals erlaubte Eigenschaften absichern. Dazu gehören z.B.

- niemals darf der Gewinner mit mehr Schiffen den Kampf verlassen, als er vor dem Kampf hatte
- niemals darf ein Spieler mit mehr Schiffen den Kampf verlieren, wenn der Verteidigungsbonus aus oder für ihn ist.

Zudem wurde ein Test geschrieben, welcher mit sehr wenig Aufwand einen möglichen Bug in einer externen Bibliothek gefunden hat. Dieser Test hat einfach Roundtrips meiner `save!`/`load!` Funktionen mit verschiedensten Eingaben getestet. ([cognitect/transit-cljs#31](https://github.com/cognitect/transit-cljs/issues/31))

## Frontend
Bei der Entwicklung des Frontends wurde auf [Reagent](https://reagent-project.github.io/), eine Clojurescript Implementierung von Facebooks ReactJS, gesetzt. Das Interface ist also mit HTML und CSS gestaltet und lässt sich bequem über jeden gängigen Browser auf quasi jedem System nutzen.

### index.html
Als Kern dient eine HTML Seite, welche nur Schönheits Informationen, wie die zu ladenden CSS Dateien, enthält. Hier wird Twitters Bootstrap eingebunden.
Zentral befindet sich ein leerer Container, welcher als Ankerpunkt für die von React dynamisch generierte WebApp dient.

### Die WebApp
In den oben genannten Container wird die App von `cloko.frontend.core` aus hereingeladen. Die App selbst besteht aus kleinen getrennten Komponennten, welche Teilweise ihren eigenen State halten, ist allerdings selber auch nur eine weitere Komponente. Dieser State ist **nur** für diese Komponenten und hält nur Eingaben, die der Nutzer in der Lebenszeit dieser Komponente macht. Wird die Komponente neu gerendert entfällt dieser.

Zustand welcher Komponenten übergreifend geregelt werden musste liegt in `cloko.frontend.utils`. Der Zustand der dort gehalten wird ist die Position des momentan ausgewählten Planeten. Dieser bestimmt maßgeblich das Verhalten diverser Komponenten. Niemals allerdings das Spiel im Kern. Dieser Zustand wird beim Neuladen der App gelöscht.

### Komponenten
Die einzelnen Komponenten, dienen er logischen Trennung von Funktionen innerhalb der UI. Komponenten werden effizient einzeln neu gerendert oder können gar wieder verwendet werden. Jede Komponente wird neu gerendert wenn ein reagent/atom geändert wird, welches sie dereferenziert.

## Bedienungsanleitung
### REPL
| Kommando              | Bedeutung|
|---|---|
| `(init! 9 9 3 2)`     | Initialisiert ein neues Spiel mit Weltgröße 9x9, 3 neutralen Planeten und zwei Spielern. |
| `(whose-turn)`         | Gibt den Spieler zurück, welcher gerade an der Reihe ist. |
| `(show-board!)`         | Zeigt das Spielfeld und Details zu allen eigenen Planeten an |
| `(send! [0 0] [1 1] 5)`  | Sendet 5 Schiffe von [0 0] nach [1 1]. (Gibt eine Fehlermeldung aus, wenn etwas schief lief) |
| `(movements!)`                | Zeigt die eigenen Flottenbewegungen an. |
| `(end-turn!)`                 | Beendet den Zug des aktuellen Spielers. Beendet die Runde, wenn der letzte Spieler seinen Zug beendet.|
| `(distance [0 0] [1 1])`      | Berechnet die Distanz in Runden zwischen zwei Koordinaten aus. |
| `(save!)`                     | Gibt einen transit String des aktuellen Spielzustands zurück. Diesen irgendwo speichern! |
| `(load! "<transit string>")`  | Setzt den Spielzustand auf den Wert des transit Strings |

#### Beispielausgabe
```
dev:cljs.user=> (cloko.core/show-board!)
(. . . . . N . . .
 . . 1 . . N . . .
 . . . . . . . . .
 . . . . . . . . .
 . . . . . . . . .
 . . . N . . . . .
 . . . . . . . . .
 . . . . . . . . .
 . . . . . . . 2 .)

| :position | :ships | :ships-per-turn |
|-----------+--------+-----------------|
|     [2 1] |      0 |               5 |
```

### UI
Mit der UI kann unter [0.0.0.0:3449](0.0.0.0:3449) oder unter [cloko.ebbinghaus.me](http://cloko.ebbinghaus.me) gespielt werden.

![Screenshot](https://github.com/MrOerni/Cloko/blob/master/screenshot.png?raw=true "Screenshot from cloko.ebbinghaus.me")

#### Erklärung der UI von oben nach unten.

##### Titel
Der Titel der App

##### Spiel Infos
links: aktueller und nächster Spieler
mitte: Wenn ein Spieler gewonnen hat, wird dies hier angezeigt.
rechts: aktuelle Runde

##### Spielfeld
Orange Planeten: neutral
Braune Planeten: von einem Spieler
Bei Klick auf einen Planeten wird dieser ausgewählt und die folgenden Komponenten aktualisiert.

##### Planeten Info
Zeigt Infos (Position, Besitzer, Schiffe pro Runde) über Planeten an. Wenn der Besitzer gerade am Zug ist, werden zusätzlich noch stationierte Schiffe angezeigt.

Ist kein Planet ausgewählt, steht dies in dem Feld.

##### Bewegungs Infos
Zeigt eine Liste aller Bewegungen des aktuellen Spielers an.

Gibt es keine Bewegungen des aktuellen Spielers, steht dies hier.

##### Flotten senden
Diese Komponente erlaubt es Flotten vom aktuell ausgewählten Planeten zu einem Planeten der Liste zu schicken. Unter der Liste wird die Distanz in Runden bis dorthin angezeigt. Der Schieberegler steuert die Anzahl der Schiffe. Das Kommando wird mit dem _Senden_ Button bestätigt.

**Wichtig!** Gehört der ausgewählte Planet nicht dem aktuellen Spieler oder gibt es keine Schiffe auf dem Planeten, so wird das Feld ausgeblendet!

##### Zug beenden
Durch einen Druck auf den Knopf ist der nächste Spieler am Zug. Eventuell wird davor auch die Runde beendet.

##### Ausklapp-Bereich
In diesem Bereich befinden sich nicht direkt Spiel wichtige Komponenten.

##### Sichern
Hier wird ein Feld angezeigt, welches den Transit String des aktuellen Zustand hält. Dieser kann irgendwohin kopiert werden oder z.B. per Post verschickt werden.

##### Laden
Hier kann man einen Transit String eintragen und als aktuellen Zustand laden. Alle Komponenten werden sofort neu geladen.

##### Initialisieren
Hier kann man mit den Schiebereglern Spiel Parameter Einstellen. Durch einen klick auf den Knopf wird ein neues Spiel erzeugt

##### Footer
Credits an den Autor der Icons und die aktuelle Versionsnummer





## Weiter Entwicklungshilfen
- `kibit` zur statischen Code Analyse
- `lein-ancient` zum aktuell halten von Abhängikeiten.
- `devcards` für dynamisch neuladende Tests mit hübscher Anzeige im Browser
- `doo` als test runner
- `travis-ci` für automatisches testen und deployen
- `github-pages` für fertige deploys für externe beta Tester
