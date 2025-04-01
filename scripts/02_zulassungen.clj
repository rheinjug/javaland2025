#!/usr/bin/env bb
(ns zulassungen
  (:require [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [babashka.cli :as cli]))

;; Ziel dieses Scripts ist es zu prüfen, ob alle Studierenden in allen CSV-Dateien bestanden haben.
;; Das Skript erwartet, dass die CSV-Dateien im Verzeichnis "resources/zulassungen" liegen.
;; Die CSV-Dateien haben die Spalten: Matrikelnummer, Vorname, Nachname, Punkte.
;; Das Skript gibt die Matrikelnummern der Studierenden aus, die in allen CSV-Dateien bestanden haben.
;; Eine Studierende hat bestanden, wenn sie mindestens 50 Punkte hat.

;; Beispielaufrufe:
;;
;; bb scripts/02_zulassungen.clj
;; bb scripts/02_zulassungen.clj --path resources/zulassungen/

;; => ("100100" "100028" "100061" "100039" "100006" "100072" "100037" "100023" "100035" "100029" "100036" "100089" "100003" "100009" "100082")


(defn parse-csv
  "Lies die CSV Datei ein, speichere die Zeilen in `zeilen` und konvertiere sie in eine Liste von Maps.
  Jede Zeile wird in ein Map umgewandelt, wobei die Schlüssel die Spaltenüberschriften sind."
  [file]
  (let [zeilen (with-open [reader (io/reader (.toString file))]
                 (doall (csv/read-csv reader)))]
    (map (fn [[matrikelnummer vorname nachname punkte]]
           {:matrikelnummer matrikelnummer
            :vorname vorname
            :nachname nachname
            :punkte (Integer/parseInt punkte)})
         (rest zeilen))))

(defn bestanden?
  "Kriterium für bestanden: Punkte >= 50"
  [{:keys [punkte]}]
  (>= punkte 50))

(defn alle-bestandenen
  "Filtere die Studierenden, die in allen CSV-Dateien bestanden haben."
  [resultate]
  (filter bestanden? resultate))

;; Wenden nun alle Funktionen an und gib die Matrikelnummern aus von den Studierenden,
;; die in allen CSV-Dateien bestanden haben.
(let [cli-options {:path {:default "resources/zulassungen"}}
      path (:path (cli/parse-opts *command-line-args* {:spec cli-options}))
      zulassungen (fs/glob path "*.csv")]
  (->> zulassungen
       (map parse-csv)
       (mapcat alle-bestandenen)
       (map :matrikelnummer)
       frequencies
       (filter #(= (second %) (count zulassungen)))
       (map first)
       prn))
