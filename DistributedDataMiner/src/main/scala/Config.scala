/******************************************************************************
  Config.scala

  Description:
    Application configuration settings

  Scala 2.10

  Author: David Sharpe

******************************************************************************/
package DistributedDataMiner
 
 
///////////////////////////////////////////////////////////////////////////////
//Common configuration data object
object Config {

  val seed : String = "somestring"
  val hashSize : Int = 256
  
  //Characters to test inputs with
  val charPool = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()-_+={}[]|:;'<,>.?/~`"
    
  //Size of the string that is concatenated with seed to create inputs
  val inputSize = 4

  //Minimum number of leading zeros to search for
  val numZeros = 4

  //Number of of workers created on each machine
  val numWorkersPerMachine = 4
}

