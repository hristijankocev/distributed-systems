# Service for decentralized system of "micro VMs" implemented with AMQP (RabbitMQ) (Колоквиум 2, 20.01.2022)

Scenario: Decentralized system of micro VMs (virtual machines). Producers make requests for VM machines. Consumers 
execute the different types of VMs.  
The requests consist of:
- VM type (compute, storage)
- amount of RAM (in MB)
- number of CPU cores
- execution time (in seconds)

Usage:

- make sure the maven dependencies are installed (`ampq-client`, `slf4j-simple`, `slf4j-api`)
- `docker-compose up` (to start up local instance of RabbitMQ)
- in the `Exchange.java` class you can specify your RabbitMQ host (default is "localhost")
- run consumer(s) (`Consumer.java`)
- run the producer (`Producer.java`)

Exchange type: **topic**  
Route keys have the form of: `vmType.amountRAM.numCores.executionTime`

What happens:

- Producer (`Producer.java`) emits requests to random routes (i.e. Route key: "compute.2048.4.7", message:
  currentDateTime).
- Consumers:
    - Consumer (`Consumer.java`) starts two consumers bound to two different routes
      - the first one accepts requests for "Compute" type of VMs
      - the second one accepts requests for "Storage" type of VMs
Both of the consumers "execute the VMs" (sleep for the amount of time specified in the requests).
Execution is parallel.
Example console and CSV output logs:

#### *Producer console:*
> Mon Jan 09 15:55:02 CET 2023 [*] Connecting to RabbitMQ server...  
Mon Jan 09 15:55:02 CET 2023 [*] Connected to server.  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'storage' with routing key storage.2048.16.10  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'storage' with routing key storage.4096.8.3  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'compute' with routing key compute.1024.8.3  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'storage' with routing key storage.16384.2.8  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'compute' with routing key compute.2048.16.6  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'storage' with routing key storage.2048.16.7  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'storage' with routing key storage.2048.8.3  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'compute' with routing key compute.4096.2.6  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'compute' with routing key compute.4096.8.9  
Mon Jan 09 15:55:02 CET 2023 [*] Requested VM of type 'compute' with routing key compute.1024.32.2  
Mon Jan 09 15:55:02 CET 2023 [*] Finished producing requests.  


#### *Consumers console:*
> Mon Jan 09 15:54:58 CET 2023 [*] Consumer 1 Connecting to RabbitMQ server...  
Mon Jan 09 15:54:58 CET 2023 [*] Consumer 2 Connecting to RabbitMQ server...  
Mon Jan 09 15:54:58 CET 2023 [*] Consumer 2 connected to server.  
Mon Jan 09 15:54:58 CET 2023 [*] Consumer 1 connected to server.  
Mon Jan 09 15:54:58 CET 2023 [*] Consumer 2 waiting for messages...  
Mon Jan 09 15:54:58 CET 2023 [*] Consumer 1 waiting for messages...  
Mon Jan 09 15:55:02 CET 2023 [*] Consumer 2 received VM request 'storage.2048.16.10' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:02 CET 2023 [*] Consumer 1 received VM request 'compute.1024.8.3' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 1 received VM request 'compute.2048.16.6' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 2 received VM request 'storage.4096.8.3' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Starting compute VM with ID: c740dc47-cc60-470d-9a49-e529b213d0e7, RAM: 1024, Cores: 8  
Mon Jan 09 15:55:03 CET 2023 [*] Starting storage VM with ID: aae058e5-5e6a-49db-a4e4-d689da370de0, RAM: 2048, Cores: 16  
Mon Jan 09 15:55:03 CET 2023 [*] Starting storage VM with ID: ef48ac3e-308e-4a5c-8e2a-9cd4e2c205d7, RAM: 4096, Cores: 8  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 1 received VM request 'compute.4096.2.6' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 2 received VM request 'storage.16384.2.8' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Starting compute VM with ID: e9883e9b-a3e1-4126-b2a2-36131fbf1e0d, RAM: 2048, Cores: 16  
Mon Jan 09 15:55:03 CET 2023 [*] Starting compute VM with ID: 97d1da1f-f438-46ab-857f-99afede103fd, RAM: 4096, Cores: 2  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 1 received VM request 'compute.4096.8.9' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Starting storage VM with ID: 84c1d654-be0d-4951-83b2-e17fdeb89307, RAM: 16384, Cores: 2  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 2 received VM request 'storage.2048.16.7' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Starting compute VM with ID: f4c5354a-45c0-4aad-b2db-c17ae3402f4f, RAM: 4096, Cores: 8  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 1 received VM request 'compute.1024.32.2' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Starting storage VM with ID: cc691473-5164-4caa-a48b-d1abd7559062, RAM: 2048, Cores: 16  
Mon Jan 09 15:55:03 CET 2023 [*] Consumer 2 received VM request 'storage.2048.8.3' on Mon Jan 09 15:55:02 CET 2023  
Mon Jan 09 15:55:03 CET 2023 [*] Starting compute VM with ID: 9e919430-426c-487e-b8c8-9cdf8aca17b4, RAM: 1024, Cores: 32  
Mon Jan 09 15:55:03 CET 2023 [*] Starting storage VM with ID: c0ec1b8a-eb77-43c3-833b-81a1aebfaed1, RAM: 2048, Cores: 8  
Mon Jan 09 15:55:05 CET 2023 [*] Finished execution VM with ID 9e919430-426c-487e-b8c8-9cdf8aca17b4  
Mon Jan 09 15:55:06 CET 2023 [*] Finished execution VM with ID ef48ac3e-308e-4a5c-8e2a-9cd4e2c205d7  
Mon Jan 09 15:55:06 CET 2023 [*] Finished execution VM with ID c740dc47-cc60-470d-9a49-e529b213d0e7  
Mon Jan 09 15:55:06 CET 2023 [*] Finished execution VM with ID c0ec1b8a-eb77-43c3-833b-81a1aebfaed1  
Mon Jan 09 15:55:09 CET 2023 [*] Finished execution VM with ID 97d1da1f-f438-46ab-857f-99afede103fd  
Mon Jan 09 15:55:09 CET 2023 [*] Finished execution VM with ID e9883e9b-a3e1-4126-b2a2-36131fbf1e0d  
Mon Jan 09 15:55:10 CET 2023 [*] Finished execution VM with ID cc691473-5164-4caa-a48b-d1abd7559062  
Mon Jan 09 15:55:11 CET 2023 [*] Finished execution VM with ID 84c1d654-be0d-4951-83b2-e17fdeb89307  
Mon Jan 09 15:55:12 CET 2023 [*] Finished execution VM with ID f4c5354a-45c0-4aad-b2db-c17ae3402f4f  
Mon Jan 09 15:55:13 CET 2023 [*] Finished execution VM with ID aae058e5-5e6a-49db-a4e4-d689da370de0  

