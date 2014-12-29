import sbt._, Keys._
import au.com.cba.omnia.uniform.dependency.UniformDependencyPlugin._

object dependencies {
  val hadoopVersion = depend.versions.hadoop

  object compile {
    val hadoopClient  = "org.apache.hadoop"        %  "hadoop-client"                % "2.5.0-mr1-cdh5.2.0"
    val hadoopCore    = "org.apache.hadoop"        %  "hadoop-core"                  % "2.5.0-mr1-cdh5.2.0"

    val ebenezer      = "au.com.cba.omnia"        %% "ebenezer"                      % "0.11.1-20141218043514-36d6638"

    val parquet       = "com.twitter"              % "parquet-cascading"             % "1.2.5-cdh5.1.2"
  }

  import compile._

  val parquetMrBug = depend.hadoop() ++ depend.parquet() ++ depend.scaldingproject() ++ List(ebenezer, hadoopCore, hadoopClient, parquet)
}
