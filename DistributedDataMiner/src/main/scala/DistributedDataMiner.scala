/******************************************************************************

  DistributedDataMiner.scala

  Description:
    Distributed Data Miner using Scala and AKKA actors

    Performs brute force mining to find inputs whose SHA256 hash codes have a
    specified number of leading zeros. Main (boss) actor controls the work and
    as worker actors connect the boss distributes portions of the workload to
    the workers.

  Scala 2.10

  Author: David Sharpe

******************************************************************************/
package DistributedDataMiner

///////////////////////////////////////////////////////////////////////////////
//Imports
import akka.actor._
import com.typesafe.config.ConfigFactory

///////////////////////////////////////////////////////////////////////////////
//Main Application
object Main extends App {
    
  //Display the command line arguments
  //println("Cmd Line Args: " + (args mkString ", "))

  //Get the configuration object
  val config = ConfigFactory.load()

  //If there is a command line argument then this is a remote worker
  //Command line argument is the hostname of the main boss
  if(args.length > 0) {
        
    //Get boss's hostname and port from command line
    val host = args(0)
    val port = args(1).toInt

    //Start remote actor system
    val system = ActorSystem("RemoteWorkerSystem", config.getConfig("worker").withFallback(config))

    //Start the number of workers specified in config and connect them to the boss
    for(i <- 1 to Config.numWorkersPerMachine) {
      val worker = system.actorOf(Props[Worker], name = f"RemoteWorker$i")
      worker ! Connect(host, port)
    }
  }
  else {

    //Create main system
    val system = ActorSystem("MainSystem", config.getConfig("boss").withFallback(config))
    val boss = system.actorOf(Props[Boss], name = "Boss")
  
    //Start the boss with the minimum number of hex zeros the hash
    //should contain and the number of workers on the boss machine
    boss ! Start(Config.numZeros, Config.numWorkersPerMachine)
  }
}
