Parquet MR Bug
==============

This repository demonstrates [Parquet-MR](https://github.com/Parquet/parquet-mr/)'s inability to correctly serialize binary fields.

Thrift is capable of serializing/deserializing binary fields (and does not perform any encoding),
whereas Parquet-MR will always apply UTF-8 encoding (treating binary fields as though they are
UTF-8 encoded strings). This causes data corruption.

The Thrift struct that is encoded can be found in [src/main/thrift/TestStruct.thrift](src/main/thrift/TestStruct.thrift).

An example of Thrift performing correctly can be found in [src/main/scala/com/rouesnel/parquetmr/bug/ThriftExample.scala](src/main/scala/com/rouesnel/parquetmr/bug/ThriftExample.scala).

An example of Parquet performing badly can be found in [src/main/scala/com/rouesnel/parquetmr/bug/ParquetExample.scala](src/main/scala/com/rouesnel/parquetmr/bug/ParquetExample.scala).

The Parquet example uses [Ebenezer](https://github.com/CommBank/ebenezer) which is a library that wraps Scrooge
generated thrift classes and calls into [Parquet-MR](https://github.com/Parquet/parquet-mr/).
Specifically, [au.com.cba.omnia.ebenezer.scrooge.ScroogeStructConverter](https://github.com/CommBank/ebenezer/blob/master/core/src/main/scala/au/com/cba/omnia/ebenezer/scrooge/ScroogeStructConverter.scala)
is a simple transformation of [parquet.thrift.ThriftSchemaConverter.ThriftStructConverter](https://github.com/Parquet/parquet-mr/blob/master/parquet-thrift/src/main/java/parquet/thrift/ThriftSchemaConverter.java).

How to run
==========

Thrift Example
--------------

    ./sbt "run-main com.rouesnel.parquetmr.bug.ThriftExample"

This should print the following:

    Thrift
    ======

    After encoding - binary field is equal to original binary field: true
    After encoding - binary field is equal to UTF8 encoded binary field: false


    Original
    -----
    binaryField:         -123, 20, 33
    stringField:         foo
    binaryAsStringField: -17, -65, -67, 20, 33



    Parquet Serialized
    -----
    binaryField:         -123, 20, 33
    stringField:         foo
    binaryAsStringField: -17, -65, -67, 20, 33


    binaryAsStringField should equal the binaryField in the deserialized version if Thrift does not support raw binary fields (e.g. without any encoding)
    Since it does not we can conclude that Thrift actually supports raw encodings.

Parquet Example
---------------

    ./sbt "run-main com.rouesnel.parquetmr.bug.ParquetExample"

This should print the following:

    Parquet
    =======

    Parquet File written to /some/random/location/test-324324-foo.parquet

    After encoding - binary field is equal to original binary field: false
    After encoding - binary field is equal to UTF8 encoded binary field: false


    Original
    -----
    binaryField:         -123, 20, 33
    stringField:         foo
    binaryAsStringField: -17, -65, -67, 20, 33



    Thrift Serialized
    -----
    binaryField:         3, 0, 0, 0, -123, 20, 33
    stringField:         foo
    binaryAsStringField: -17, -65, -67, 20, 33
