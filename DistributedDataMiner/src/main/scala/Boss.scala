/******************************************************************************
  Boss.scala

  Description:
    Implementation of the Boss actor

  Scala 2.10

  Author: David Sharpe

******************************************************************************/
package DistributedDataMiner

///////////////////////////////////////////////////////////////////////////////
//Imports
import akka.actor._
import scala.math.pow


///////////////////////////////////////////////////////////////////////////////
//Boss Actor
//Manages the workers
class Boss extends Actor {
  val worker = context.actorOf(Props[Worker], name = "Worker")
  var numWorkersRunning = 0
  var numOfZeros = 0
  
  //Build an array of strings for the start of the workers range
  //Strings start with first character in pool repeated input size minus 1 times.
  //Then the rightmost character is incremented through all possible values in the pool
  val startString = new Array[String](Config.charPool.length())
  var workPosition = 0 //Tracks which input strings have been tested
  for(i <- 0 to Config.charPool.length()-1) {
    startString(i) = Config.charPool(0).toString * (Config.inputSize - 1) ++ Config.charPool(i).toString
  }
  
  //Range for each worker is number of characters in pool to the input size minus 1 power since
  //repetitions are allowed and all combinations of input size - 1 will be tested for each message
  val range = pow(Config.charPool.length() , (Config.inputSize - 1)).toInt
  //println(f"Range = $range")

  
  def receive = {
    case Start(_numOfZeros, numWorkers) => {
      
      numOfZeros = _numOfZeros
      println(f"Starting $numWorkers Workers to find hashes with $numOfZeros zeros")
            
      //Start the workers
      for(i <- 1 to numWorkers) {
        val worker = context.actorOf(Props[Worker], name = f"worker$i")
        worker ! StartWork(numOfZeros, range, startString(workPosition))
        
        workPosition += 1
        numWorkersRunning += 1
        //println(f"Workers running $numWorkersRunning")
      }
    }

    case HashesFound(results)  => {
      //Print the hashes that were found
      for (i <- 0 to results.size-1) {
        println("Input: " ++ results(i).input ++ "\tHash: " ++ results(i).hash)
      }
      
      //If there is another range of inputs to work on then start it
      if(workPosition < Config.charPool.length()) {
        sender ! StartWork(numOfZeros, range, startString(workPosition))
        workPosition += 1
      }
      else {        
        //Wait for all workers to finish and then shutdown
        numWorkersRunning -= 1
        //println(f"Workers running $numWorkersRunning")
        if(numWorkersRunning == 0) {
          println("Shuting down...")
          context.stop(self)
          context.system.shutdown
        } 
      }                          
    }
    
    case RemoteWorkerNotification() => {
      //Tell the remote worker to start finding hashes for the specified input range
      println("Remote worker (" + sender.path.name + ") connected from " + sender.path.address.host.get)
      sender ! StartWork(numOfZeros, range, startString(workPosition))
      workPosition += 1
      numWorkersRunning += 1
    }
    
    case _ => println("Boss recveived unknown message")
  }
}

