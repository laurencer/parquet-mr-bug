import sbt._, Keys._

import au.com.cba.omnia.uniform.core.standard.StandardProjectPlugin._
import au.com.cba.omnia.uniform.core.version.UniqueVersionPlugin._
import au.com.cba.omnia.uniform.dependency.UniformDependencyPlugin._
import au.com.cba.omnia.uniform.thrift.UniformThriftPlugin._
import au.com.cba.omnia.uniform.assembly.UniformAssemblyPlugin._

object build extends Build {
  type Sett = Def.Setting[_]

  lazy val standardSettings: Seq[Sett] =
    uniformDependencySettings ++
    uniformThriftSettings ++
    uniform.docSettings("https://github.com/laurencer/parquet-mr-bug") ++
    Seq[Sett](incOptions := incOptions.value.withNameHashing(true))

  lazy val all = Project(
    id = "parquet-mr-bug",
    base = file("."),
    settings =
      standardSettings ++
      uniform.project("parquet-mr-bug", "com.rouesnel.parquetmr.bug") ++
      Seq[Sett](
        publishArtifact := false,
        libraryDependencies ++= dependencies.parquetMrBug
      )
  )
}
