# BitSieve – High-Performance Prime Calculation Server

**BitSieve** ist ein moderner, in Java 24 entwickelter Server zur effizienten Berechnung und Speicherung von Primzahlen. Er nutzt eine **Hexagonale Architektur** (Ports and Adapters), um Geschäftslogik, Datenbank und Netzwerk strikt voneinander zu trennen.

---

## 🚀 Features

- **Segmentiertes Sieb des Eratosthenes**: Effiziente Primzahlberechnung in Blöcken von 10.000 Zahlen.

- **Virtual Threads (JDK 24)**: Skalierbarer TCP-Server, der tausende Client-Anfragen gleichzeitig bearbeiten kann, ohne das Betriebssystem zu belasten.

- **Hexagonale Architektur**: Der Kern ist vollständig unabhängig von der Datenbank oder dem Übertragungsprotokoll.

- **In-Memory & Persistenz**: Aktuell unterstützt der Server ein In-Memory-Repository für schnelle Tests (PostgreSQL-Anbindung in Arbeit).

---

## 🛠 Architektur

Das Projekt folgt dem Prinzip der **Clean Architecture**:

- ```domain.model```: Enthält den ```PrimeBlock```, das BitSet-Mapping und die mathematische Definition eines Blocks.

- ```domain.service```: Das Herzstück mit dem Sieve-Algorithmus und der ```calculateUpTo-Logik```.

- ```ports```: Interfaces, die definieren, wie der Service mit der Außenwelt kommuniziert.

- ```adapters```: Konkrete Implementierungen für den TCP-Server und die Datenhaltung.

---

## 📋 Protokoll & Benutzung

Der Server kommuniziert über ein einfaches Text-Protokoll auf Port ```1337```.
### Starten des Servers

1. Stelle sicher, dass du **JDK 24** installiert hast.

2. Starte die ```Main```-Klasse über deine IDE oder Gradle.

### Verbindung mit dem Client

Du kannst dich mit jedem TCP-Client verbinden (z. B. ```netcat``` oder ```telnet```):
```bash
nc localhost 1337
```  
### Verfügbare Befehle
| Befehl | Beschreibung |
| :--- | :--- |
| `help` | Zeigt die Hilfe an. |
| `is [Zahl]` | Prüft, ob eine Zahl eine Primzahl ist (rechnet bei Bedarf nach). |
| `exit` | Trennt die Verbindung zum Server. |

## 🛠 Installation & Setup
```bash

# Repository klonen
git clone https://github.com/Fabian648/BitSieve.git

# In das Verzeichnis wechseln
cd BitSieve/PrimeCalcServer

# Kompilieren (falls Gradle genutzt wird)
./gradlew build
```

## 🐳 Schnellstart mit Docker
Der einfachste Weg, BitSieve inklusive Datenbank zu starten:
```bash
docker compose up --build
```
---

## 📅 Roadmap

- [x] Initiales Architektur-Setup

- [x] Sieve-Algorithmus Implementierung

- [x] TCP-Server mit Virtual Threads

- [x] PostgreSQL Adapter für dauerhafte Speicherung

- [x] Docker-Compose Setup

- [ ] Web-Interface (Spring Boot Adapter)
