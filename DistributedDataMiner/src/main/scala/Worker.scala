/******************************************************************************
  Worker.scala

  Description:
    Implementation of the Worker actor

  Scala 2.10

  Author: David Sharpe

******************************************************************************/
package DistributedDataMiner

///////////////////////////////////////////////////////////////////////////////
//Imports
import akka.actor._
import collection.mutable.ArrayBuffer


///////////////////////////////////////////////////////////////////////////////
//Worker Actor
//Searches for hashes in the given range of inputs
class Worker extends Actor {  
    
  //Buffer for storing valid hashes and corresponding input
  var results = new ArrayBuffer[ResultData]() 
  
  //Record the name of this actor
  val name = self.path.name
  
  //Get a SHA256 object
  val md = java.security.MessageDigest.getInstance("SHA-256")
    
  //Function to convert byte array to hex string
  def bytes2HexString(bytes : Array[Byte]) = bytes.map{ b => String.format("%02X", new java.lang.Integer(b & 0xff)) }.mkString
  
  //Function to generate a random string of specified length
  def randomString(len: Int): String = {
    val rand = new scala.util.Random(System.nanoTime)
    val sb = new StringBuilder(len)
    for (i <- 0 until len) {
      sb.append(Config.charPool(rand.nextInt(Config.charPool.length)))
    }
    sb.toString
  }
  
  //Function to compute the next string value to test given the current value
  def nextString(sb: StringBuilder): StringBuilder = {    
    for (i <- 0 until sb.length - 1) {
      if(Config.charPool.indexOf(sb.charAt(i)) == Config.charPool.length-1) {
        sb.setCharAt(i, Config.charPool(0))
      }
      else {      
        sb.setCharAt(i, Config.charPool(Config.charPool.indexOf(sb.charAt(i)) + 1))
        sb
      }
    }      
    sb
  }
  
  //Receive actor messages
  def receive = {
    case StartWork(numZeros, range, start) => {
      
      //Clear the results buffer
      results.clear()
      
      //Set the starting string         
      var sb = new StringBuilder(start.length())
      sb.append(start)      
      
      println(f"$name: Trying $range inputs starting at " + sb.toString)
      
      //Go through range and try to find hashes with numZeros leading zeros.
      for(i <- 1 to range) {

        //Get the input for SHA256
        val in: String = Config.seed ++ sb.toString
        sb = nextString(sb)
        
        //Compute the SHA256 hash and create a hex string of the hash
        val hash: String = bytes2HexString(md.digest(in.getBytes("UTF-8")))
        //println(f"$in $hash")
        
        //If hash has the correct number of leading zeros
        //then store hash and corresponding input
        if(hash.startsWith("0" * numZeros)) {          
          results += new ResultData(in, hash)
        }            
      }
      
      println(f"$name: Finished searching...")
      
      //Work is complete. Send results to Boss.
      sender ! HashesFound(results)
    }
    
    case Connect(host, port) => {
        //Tell the boss we are available to do work
        val remoteBoss = context.actorFor("akka.tcp://MainSystem@" + host + ":" + port + "/user/Boss")
        remoteBoss ! RemoteWorkerNotification()
      }
    
    case StopWork() => {      
        println(f"$name: Commanded to stop...")
      
        //If there are any hashes found send them to the boss.
        if(results.size > 0) {      
          sender ! HashesFound(results)
        }

        context.stop(self)
    }
    
    case _ => println("$name recveived unknown message")
  }
}
