# The LijOLo-Demo-Project

Dieses Projekt zeigt den Eimsatz von LiquiBAse, jOOQ und LocalStack

## Requirements

Um dsa Projekt erfolgreich aszuführen, werden folgende Lokale Installationen erwartet:

* Java 21
* Docker
* Maven 3
* AWS CLI

## Vorbereitung

Im ersten Schritt folgendes Command ausführen:

```bash
docker volume create pg-admin-config
```

### Datenbank ausführen

**Achtung:** Die Postgres-Datenbank läuft auf Port: 5462 um Konflikten mit anderen Installationen zu vermeinden

#### Datenbank starten

```bash
docker compose up
```

#### Datenbank stoppen

```bash
docker compose stop
```

#### Datenbank stoppen und Daten löschen

```bash
docker compose down
```

















