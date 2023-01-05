# Simple messaging application implemented with Apache Kafka

### Usage:

- make sure the maven dependencies are installed (kafka-clients, kafka_2.13)
- run the three consumers (`C1.java`, `C2.java`, `C3.java`)
- run the action emitter (`ActionEmitter.java`)

### What happens:

- Producer (`ActionEmitter.java`) emits random actions (i.e. Topic: "Tony.student.19.laboratory", message: "Thu Jan 05
  20:23:35 CET 2023").
- Consumers:
    - Consumer 1 (`C1.java`) logs (prints in the console) all students that have opened an office
    - Consumer 2 (`C2.java`) logs (writes in a csv file) the information of which professor entered which room at a what
      time.
    - Consumer 3 (`C3.java`) logs (writes in a csv file) the information of which student entered which room at a what
      time.


### Example console and CSV output logs:

#### *Producer console:*
> Starting emitter...  
Thu Jan 05 20:23:35 CET 2023 [x] Sent to topic: 192029.AJ.professor.18.classroom  
Thu Jan 05 20:23:37 CET 2023 [x] Sent to topic: 192029.Meadow.student.4.laboratory  
Thu Jan 05 20:23:37 CET 2023 [x] Sent to topic: 192029.Walter.student.4.office  
Thu Jan 05 20:23:38 CET 2023 [x] Sent to topic: 192029.Joseph.professor.9.classroom  
Thu Jan 05 20:23:39 CET 2023 [x] Sent to topic: 192029.Astrid.professor.8.classroom  
Thu Jan 05 20:23:40 CET 2023 [x] Sent to topic: 192029.AJ.professor.17.classroom  
Thu Jan 05 20:23:41 CET 2023 [x] Sent to topic: 192029.Walter.professor.9.office  
Thu Jan 05 20:23:41 CET 2023 [x] Sent to topic: 192029.AJ.professor.1.laboratory  
Thu Jan 05 20:23:42 CET 2023 [x] Sent to topic: 192029.Meadow.student.13.laboratory  
Thu Jan 05 20:23:43 CET 2023 [x] Sent to topic: 192029.Walter.professor.12.classroom  
Emitter finished.  


#### *"Consumer 1" console:*
> Thu Jan 05 20:23:40 CET 2023 [*] Student 'Walter' entered office number 4 at Thu Jan 05 20:23:37 CET 2023  


#### *"Consumer 2" creates a csv file with the contents:*
> Walter,classroom,12,Thu Jan 05 20:23:43 CET 2023  
Astrid,classroom,8,Thu Jan 05 20:23:39 CET 2023  
AJ,laboratory,1,Thu Jan 05 20:23:41 CET 2023  
Joseph,classroom,9,Thu Jan 05 20:23:38 CET 2023  
AJ,classroom,18,Thu Jan 05 20:23:35 CET 2023  
AJ,classroom,17,Thu Jan 05 20:23:40 CET 2023  
Walter,office,9,Thu Jan 05 20:23:41 CET 2023  

#### *"Consumer 3" creates a csv file with the contents:*
> Walter,office,4,Thu Jan 05 20:23:37 CET 2023  
Meadow,laboratory,4,Thu Jan 05 20:23:37 CET 2023  
Meadow,laboratory,13,Thu Jan 05 20:23:42 CET 2023  


