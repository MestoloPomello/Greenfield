
# Greenfield

Project for the Distributed and Pervasive Systems exam (UniMi).
This project aims to manage a network of cleaning robots deployed around the city of Greenfield. The robots will generate some values (which simulate pollution sensors) and make them available to the administrators of the network.
Detailed info can be found inside the guidelines PDF in the repository.

## Modules
### Administrator Server
- Reading sensors' averages published by the deployed robots via **MQTT**
- REST interface for requests from the Administrator Client
- REST interface for requests from the deployed robots

### Administrator Client
- List the deployed robots
- Print the average of the last *n* measurements for a specific robot
- Print the average of all of the measurements between two timestamps

### Cleaning Robot
 - Timestamp management (Lamport clock)
 - Communications with other robots via **gRPC** and **Protocol Buffers**
 - Input possibilities (*fix*, *quit*)
 - Reading data from sensors and publishing them to the **MQTT** broker
 - Detection and handling of other robots' crashes

### Mechanic and reparations
- Core problem: mutual exclusion for the shared resource (the mechanic)
- Implementation of the Ricart & Agrawala algorithm
- Each robot has its own request queue

#### Reparation process
- The robot stops its own sensors operations
- The robot starts the Ricart & Agrawala algorithm: it broadcasts a mechanic request to the other robots and waits until every robot replies with OK, then waits 10 seconds and then releases the mechanic

<br />

<p align="center"><img src="https://github.com/MestoloPomello/Greenfield/assets/26629154/89fa1dba-159e-43fb-a21e-dd25970e8773"/></p>

