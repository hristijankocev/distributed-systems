## Simple messaging application implemented with AMQP (RabbitMQ)

Scenario: The Faculty has implemented smart doors with wireless tokens. When a door is opened, information is sent to
the RabbitMQ exchange. The information consists of:

- who opened the door (the name of the person)
- type of person (student, professor)
- which room (ID)
- type of room (classroom, laboratory, office)

Usage:

- make sure the maven dependencies are installed (ampq-client, slf4j-simple, slf4j-api)
- `docker-compose up` (to start up **RabbitMQ**)
- run the three consumers (`C1.java`, `C2.java`, `C3.java`)
- run the emitter (`ActionEmitter.java`)

Exchange type: **topic**  
Route keys have the form of: `personName.userType.roomId.roomType`

What happens:

- Producer (`ActionEmitter.java`) emits random actions (i.e. Route key: "Tony.student.19.laboratory", message:
  currentDateTime).
- Consumers:
    - Consumer 1 (`C1.java`) logs (prints in the console) all students that have opened an office
    - Consumer 2 (`C2.java`) logs (writes in a csv file) the information of which professor entered which room at a
      specific time.
    - Consumer 3 (`C3.java`) logs (writes in a csv file) the information of which student entered which room at a
      specific time.

Example console and CSV output logs:

#### *Producer console:*

> Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Astrid.student.14.laboratory'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Walter.professor.5.office'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Meadow.professor.1.classroom'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Astrid.professor.10.classroom'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Peter.student.9.laboratory'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'AJ.student.1.office'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Astrid.professor.3.classroom'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'Joseph.professor.3.office'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'AJ.student.7.laboratory'  
Sun Dec 11 22:52:01 CET 2022 [x] Sent 'Sun Dec 11 22:52:01 CET 2022', with routing key 'AJ.student.15.office'

#### *Consumer 1 console:*

> Sun Dec 11 22:51:51 CET 2022 [*] Waiting for messages. To exit press CTRL+C  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'AJ.student.1.office'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'AJ.student.15.office'Message: 'Sun Dec 11 22:52:01 CET 2022'

#### *Consumer 2 console:*

> Sun Dec 11 22:51:54 CET 2022 [*] Waiting for messages. To exit press CTRL+C  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Walter.professor.5.office'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Meadow.professor.1.classroom'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Astrid.professor.10.classroom'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Astrid.professor.3.classroom'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Joseph.professor.3.office'Message: 'Sun Dec 11 22:52:01 CET 2022'

#### *Consumer 2 CSV file:*

> Walter,office,5,Sun Dec 11 22:52:01 CET 2022  
Meadow,classroom,1,Sun Dec 11 22:52:01 CET 2022  
Astrid,classroom,10,Sun Dec 11 22:52:01 CET 2022  
Astrid,classroom,3,Sun Dec 11 22:52:01 CET 2022  
Joseph,office,3,Sun Dec 11 22:52:01 CET 2022

#### *Consumer 3 console:*

> Sun Dec 11 22:51:58 CET 2022 [*] Waiting for messages. To exit press CTRL+C  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Astrid.student.14.laboratory'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'Peter.student.9.laboratory'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'AJ.student.1.office'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'AJ.student.7.laboratory'Message: 'Sun Dec 11 22:52:01 CET 2022'  
Sun Dec 11 22:52:01 CET 2022 [x] Received 'AJ.student.15.office'Message: 'Sun Dec 11 22:52:01 CET 2022'

#### *Consumer 3 CSV file:*

> Astrid,laboratory,14,Sun Dec 11 22:52:01 CET 2022  
Peter,laboratory,9,Sun Dec 11 22:52:01 CET 2022  
AJ,office,1,Sun Dec 11 22:52:01 CET 2022  
AJ,laboratory,7,Sun Dec 11 22:52:01 CET 2022  
AJ,office,15,Sun Dec 11 22:52:01 CET 2022  

