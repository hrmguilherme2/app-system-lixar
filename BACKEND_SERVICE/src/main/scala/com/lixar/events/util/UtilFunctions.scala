package com.lixar.events.util

//Spark
import org.apache.spark.sql.types._

object UtilFunctions {
  
  def buildSQL(x:Any):String = {
    x match {
      case _:String => s"'$x'"
      case _        => s"$x"
    }
  }

  def getCols(x:StructType):Seq[String] = {
    x.fields.flatMap( f =>
      f.dataType match {
        case _ => Array(f.name)
      }
    ).toSeq
  }

}
