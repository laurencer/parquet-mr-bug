package com.rouesnel.parquetmr.bug

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path

import parquet.hadoop._

import au.com.cba.omnia.ebenezer.scrooge._
import java.nio.ByteBuffer
import java.io.File


object ParquetExample {
  def createTestFilePath(): Path = {
    val tempFile = File.createTempFile("parquet", ".parquet")
    tempFile.delete()
    new Path(tempFile.getAbsolutePath)
  }

  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    val path = createTestFilePath()

    // Setup the writer.
    ScroogeReadWriteSupport.setThriftClass(conf, classOf[TestStruct])
    val writer = new ParquetWriter[TestStruct](
      path,
      new ScroogeWriteSupport[TestStruct],
      ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
      ParquetWriter.DEFAULT_BLOCK_SIZE,
      ParquetWriter.DEFAULT_PAGE_SIZE,
      ParquetWriter.DEFAULT_PAGE_SIZE,
      ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
      ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
      conf
    )

    // Binary data that does not encode properly to UTF-8
    val nonUtf8Bytes: Array[Byte] = Array[Byte](-123, 20, 33)

    // Create an example of the thrift structure which will exhibit byte
    // arrays being encoded correctly.
    val testStruct: TestStruct = TestStruct(
      ByteBuffer.wrap(nonUtf8Bytes),
      "foo",
      new String(nonUtf8Bytes, "UTF-8")
    )

    // Write the test struct out.
    writer.write(testStruct)
    writer.close()


    // Try to read the serialized Parquet record back.
    val reader = new ParquetReader[TestStruct](conf, path, new ScroogeReadSupport[TestStruct])
    val deserialized = reader.read()
    reader.close()

    // Check whether the binary field after serialization is equal to the original value.
    val deserializedBinaryFieldEqualToOriginal =
      deserialized.binaryField.array().toList == testStruct.binaryField.array().toList

    // Check whether the binary field after serialization is equal to the UTF8 encoded value.
    val deserializedBinaryFieldEqualToUTF8EncodedField =
      testStruct.binaryField.array().toList == deserialized.binaryAsStringField.getBytes("UTF-8").toList

    println(s"Parquet")
    println(s"=======")
    println()
    println(s"Parquet File written to ${path.toString}")
    println()
    println(s"After encoding - binary field is equal to original binary field: ${Console.RED}${ deserializedBinaryFieldEqualToOriginal }${Console.RESET}")
    println(s"After encoding - binary field is equal to UTF8 encoded binary field: ${Console.RED}${ deserializedBinaryFieldEqualToUTF8EncodedField }${Console.RESET}")
    println()
    ThriftExample.prettyPrint("Original", testStruct)
    println()
    ThriftExample.prettyPrint("Thrift Serialized", deserialized)
    println()
    println()
  }
}
