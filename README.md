# Distributed Data Miner
Distributed data miner that searches for SHA256 hashes with a minimum number of leading zeros (specified in Config.scala). Implemented in Scala using AKKA actors. 

Runs a boss actor which mines for the desired data. The boss actor also accepts connections from worker actors that assist with the data mining. Numerous worker actors can be started on numerous remote machines (or the local machine if desired). Multiple worker actors are started for each worker instance (exact number specified in Config.scala). The boss actor controls division and distribution of the labor among the available workers (if any are connected).

A pool of characters is used to build strings to input to the SHA256 hashing algorithm. The pool size is 92 characters. The string size is variable (specified in Config.scala). For a string N characters in length, every combination of N-1 characters will be tested and the Nth character will remain constant. Each worker is assigned a different value for the fixed character and mines through all the possible character combinations of the remaining N-1 characters. This creates 92^(N-1) input values for each worker to process per message sent to the worker.


## Instructions to compile/run
The application.conf has unique sections for the boss and worker. The boss section 
specifies the machine/IP address of the boss actor. Currently it is set to localhost. 
If it is to be run on a different machine then application.conf will have to be modified 
accordingly. The worker configuration uses any available IP address and an ephemeral port. The worker configuration can be left unchanged unless a specific IP address and port are desired.

To run the boss, change to the top level directory with the build.sbt file and execute: 

      sbt “run”

To run the worker, first start the boss, then change to the directory with the build.sbt file and execute: 

     sbt “run hostname port” 

where hostname is the hostname or IP address of the boss node and port is the port of the boss node.

