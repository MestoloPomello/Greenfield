# Greenfield

### ToDo:
- l'assegnazione del distretto sembra non essere correttamente spartita
- se si avviano fix contemporaneamente, si gestiscono correttamente.
Se si prova con uno separato dopo, manca il self-OK e non parte niente

- FINITA Opzionale parte 1: autobalance distretti quando un robot crasha
  - OK il robot che si accorge (quello prima) rimuove quello crashato dalla sua lista
  e calcola la distribuzione nei distretti usando la lista
    - OK calcola numero di robot per distretto
    - OK ordina per numero di distretti
    - OK sceglie il primo robot che trova con il distreto designato
  - OK comunica a tutti che questo robot deve cambiare distretto in quello nuovo
    - OK alla ricezione, tutti i robot aggiornano la propria lista
    - OK in più, il robot in questione aggiorna posX e posY locali

- OK quando un robot rileva che l'altro è crashato, manda un botto di messaggi al server