/******************************************************************************
  Messages.scala

  Scala 2.10

  Author: David Sharpe

  Description:
    Messages for actor communications

******************************************************************************/
package DistributedDataMiner

///////////////////////////////////////////////////////////////////////////////
//Imports
import collection.mutable.ArrayBuffer


///////////////////////////////////////////////////////////////////////////////
//Class to store input and resulting hash
class ResultData extends Serializable {
  var input : String = ""
  var hash : String = ""
  def this(input: String, hash: String) = { this(); this.input = input; this.hash = hash }
}


///////////////////////////////////////////////////////////////////////////////
//Define actor messages
sealed trait Messages
case class Start(numOfZeros: Int, numWorkers: Int) extends Messages
case class StartWork(numZeros:Int, range: Int, start: String) extends Messages
case class StopWork() extends Messages
case class HashesFound(results: ArrayBuffer[ResultData]) extends Messages
case class Connect(host: String, port: Int) extends Messages
case class RemoteWorkerNotification() extends Messages

