package com.rouesnel.parquetmr.bug

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.ByteBuffer

import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

object ThriftExample {

  /** Print to stdout the TestStruct and all its fields in a human-readable format */
  def prettyPrint(name: String, struct: TestStruct): Unit = {
    println(
      s"""
        |$name
        |-----
        |binaryField:         ${struct.binaryField.array().toList.mkString(", ")}
        |stringField:         ${struct.stringField}
        |binaryAsStringField: ${struct.binaryAsStringField.getBytes("UTF-8").toList.mkString(", ")}
      """.stripMargin)
  }

  def serialize(ts: TestStruct): Array[Byte] = {
    val os = new ByteArrayOutputStream()
    val transport = new TIOStreamTransport(os)
    val protocol = (new TCompactProtocol.Factory).getProtocol(transport)
    ts.write(protocol)
    os.toByteArray
  }

  def deserialize(bytes: Array[Byte]): TestStruct = {
    val is = new ByteArrayInputStream(bytes)
    val transport = new TIOStreamTransport(is)
    val protocol = (new TCompactProtocol.Factory).getProtocol(transport)
    val ts = new TestStruct()
    ts.read(protocol)
    ts
  }

  def main(args: Array[String]): Unit = {
    // Binary data that does not encode properly to UTF-8
    val nonUtf8Bytes: Array[Byte] = Array[Byte](-123, 20, 33)

    // Create an example of the thrift structure which will exhibit byte
    // arrays being encoded correctly.
    val testStruct: TestStruct = new TestStruct(
      ByteBuffer.wrap(nonUtf8Bytes),
      "foo",
      new String(nonUtf8Bytes, "UTF-8")
    )

    // Serialize the byte array.
    val serialized: Array[Byte] = serialize(testStruct)

    // Deserialize the byte array and confirm that the strings are correct.
    val deserialized: TestStruct = deserialize(serialized)

    // Check whether the binary field after serialization is equal to the original value.
    val deserializedBinaryFieldEqualToOriginal =
      deserialized.binaryField.array().toList == testStruct.binaryField.array().toList

    // Check whether the binary field after serialization is equal to the UTF8 encoded value.
    val deserializedBinaryFieldEqualToUTF8EncodedField =
      testStruct.binaryField.array().toList == deserialized.binaryAsStringField.getBytes("UTF-8").toList

    println(s"Thrift")
    println(s"======")
    println()
    println(s"After encoding - binary field is equal to original binary field: ${Console.GREEN}${ deserializedBinaryFieldEqualToOriginal }${Console.RESET}")
    println(s"After encoding - binary field is equal to UTF8 encoded binary field: ${Console.RED}${ deserializedBinaryFieldEqualToUTF8EncodedField }${Console.RESET}")
    println()
    prettyPrint("Original", testStruct)
    println()
    prettyPrint("Parquet Serialized", deserialized)
    println()
    println("binaryAsStringField should equal the binaryField in the deserialized version if Thrift does not support raw binary fields (e.g. without any encoding)")
    println("Since it does not we can conclude that Thrift actually supports raw encodings.")
    println()
  }
}
