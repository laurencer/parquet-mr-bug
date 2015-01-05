package com.rouesnel.parquetmr.bug

import java.io.File
import java.nio.ByteBuffer

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import parquet.hadoop._
import parquet.thrift._
import parquet.hadoop.thrift.ThriftReadSupport

object ParquetExample {
  def createTestFilePath(): Path = {
    val tempFile = File.createTempFile("parquet", ".parquet")
    tempFile.delete()
    new Path(tempFile.getAbsolutePath)
  }

  def main(args: Array[String]): Unit = {
    val numberOfStructs = try {
      args(1).toInt
    } catch {
      case ex: Exception => {
        throw new Exception("The first argument should be the number of structs to write to Parquet", ex)
      }
    }

    val conf = new Configuration()
    val path = createTestFilePath()

    // Setup the writer.
    val writer = new ThriftParquetWriter[TestStruct](
      path,
      classOf[TestStruct],
      ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
      ParquetWriter.DEFAULT_BLOCK_SIZE,
      ParquetWriter.DEFAULT_PAGE_SIZE,
      ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
      ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
      conf
    )

    // Binary data that does not encode properly to UTF-8
    val nonUtf8Bytes: Array[Byte] = Array[Byte](-123, 20, 33)

    // Create an example of the thrift structure which will exhibit byte
    // arrays being encoded correctly.
    val testStruct: TestStruct = new TestStruct(
      ByteBuffer.wrap(nonUtf8Bytes),
      "foo",
      new String(nonUtf8Bytes, "UTF-8")
    )

    // Write the test struct out.
    for (i <- 0 until numberOfStructs) {
      writer.write(testStruct)
    }
    writer.close()


    println(s"Parquet")
    println(s"=======")
    println()
    println(s"Parquet File written to ${path.toString}")
    println(s"Wrote ${numberOfStructs} identical records to Parquet")
    println()

    // Try to read the serialized Parquet record back.
    val reader = ParquetReader.builder[TestStruct](new ThriftReadSupport[TestStruct](), path).withConf(conf).build()
    for (i <- 0 until numberOfStructs) {
      println(s"Record ${i}")
      println(s"===========")

      // Read the record.
      val deserialized = reader.read()

      // Check whether the binary field after serialization is equal to the original value.
      val deserializedBinaryFieldEqualToOriginal =
        deserialized.binaryField.array().toList == testStruct.binaryField.array().toList

      // Check whether the binary field after serialization is equal to the UTF8 encoded value.
      val deserializedBinaryFieldEqualToUTF8EncodedField =
        testStruct.binaryField.array().toList == deserialized.binaryAsStringField.getBytes("UTF-8").toList

      println(s"After encoding - binary field is equal to original binary field: ${Console.RED}${ deserializedBinaryFieldEqualToOriginal }${Console.RESET}")
      println(s"After encoding - binary field is equal to UTF8 encoded binary field: ${Console.RED}${ deserializedBinaryFieldEqualToUTF8EncodedField }${Console.RESET}")
      println()
      ThriftExample.prettyPrint("Original", testStruct)
      println()
      ThriftExample.prettyPrint("Parquet Serialized", deserialized)
      println()
      println()
    }
    reader.close()
  }
}
