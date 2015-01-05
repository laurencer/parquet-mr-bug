import sbt._, Keys._
import au.com.cba.omnia.uniform.dependency.UniformDependencyPlugin._

object dependencies {
  val hadoopVersion = depend.versions.hadoop

  object compile {
    val hadoopClient  = "org.apache.hadoop"        %  "hadoop-client"                % "2.5.0-mr1-cdh5.3.0"
    val hadoopCore    = "org.apache.hadoop"        %  "hadoop-core"                  % "2.5.0-mr1-cdh5.3.0"


    val parquet    = "com.twitter"                 %%  "parquet-scrooge"             % "1.5.0-cdh5.3.0"

  }

  import compile._

  val parquetMrBug = depend.hadoop() ++ depend.scaldingproject() ++ List(parquet, hadoopCore, hadoopClient)
}
