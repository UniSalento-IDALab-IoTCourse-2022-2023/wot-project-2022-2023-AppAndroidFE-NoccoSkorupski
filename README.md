# wot-project-BESpringboot-SkorupskiNocco

## contenuti
1. [Descrizione del progetto](#descrizione-progetto)
2. [Architettura del sistema](#architettura)
3. [Link alle repository](#links)
4. [Descrizione del componente](#descrizione-componente)


### Descrizione del progetto <a name="descrizione-progetto"></a>

Il progetto ha come obiettivo quello di guidare un individo all'interno di un ospedale(adattabile a strutture differenti) al fine di raggiungere un determinato reparto/stanza mediante l'uso di beacon. 
L'utente, installando l'applicazione sul proprio dispositivo android avrà la possibilità di selezionare un reparto di destinazione ed essere guidato attraverso a delle indicazioni visualizzate sullo schermo.
Le applicazioni sono due, una per l'utente ed una per l'amministratore. 
L'amministratore avrà il compito di aggiungere e mappare inizialmente i beacon attraverso l'applicazione fornita, assegnando un reparto ed un insieme di stanze al singolo beacon, inoltre dovrà inizialmente posizionarsi all'ingresso dell'edificio con il dispositivo puntato verso l'interno per calibrare le coordinate attraverso un determinato pulsante via app, in quanto l'applicazione usa un magnetometro per capire il verso di percorrenza rispetto al singolo beacon.
Una volta completato questo processo basterà semplicemente installare l'applicazione Hospital Maps per poter usufluire del navigatore. 
L'applicazione offre la possibilità di selezionare un percorso adeguato per persone diversamente abili.


### Architettura del sistema <a name="architettura"></a>

I beacon comunicano con il FrontEnd inviando costantemenet i loro segnali BLE.
Entrambe le applicaioni comunicano con i beacon, ricevendo i loro segnali con una determinata potenza RSSI.
Le applicazioni amministratore e utente utilizzano un BackEnd in cloud deployato su AWS che mette a disposizione diverse API.
Infine il BE utilizza un database MongoDb, anch'esso deployato su AWS, entrambi in un unica istanza di EC2.


### Link delle repository <a name="links"></a>

- FrontEnd Utente: [Link](https://github.com/UniSalento-IDALab-IoTCourse-2022-2023/wot-project-2022-2023-AppAndroidFE-NoccoSkorupski)
- FrontEnd Amministratore: [Link](https://github.com/UniSalento-IDALab-IoTCourse-2022-2023/wot-project-AdminAppAndroidFE-NoccoSkorupski)
- BackEnd: [Link](https://github.com/UniSalento-IDALab-IoTCourse-2022-2023/wot-project-BESpringboot-SkorupskiNocco)


### Descrizione del Componente <a name="descrizione-componente"></a>
L'applicazione  Hospital Maps può essere scaricata dall'utente, se vuole beneficiare di essa per orientarsi nell'ospedale. Infatti, l'app permette di':
1. Iniziare la propria attività da una schermata di start, nella quale l'utente può selezionare il tipo di percorso che desidera fare, se accessibile (per persone diversamente abili) o non.
2. Ricevere la lista dei reparti raggiungibili con la possibilità di selezionarne uno
3. Ricevere la lista delle stanze appartenenti al reparto selezionato con la possibilittà di selezionare una di queste come destinazione
4. Ricevere le indicazioni, fino all'arrivo della destiazione che verrà segnalata, in tempo reale, in modo dipedente dìrispetto a come si sta muovendo l'utente, in che direzione e in che verso.
