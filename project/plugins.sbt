resolvers += Resolver.url("commbank-releases-ivy", new URL("http://commbank.artifactoryonline.com/commbank/ext-releases-local-ivy"))(Patterns("[organization]/[module]_[scalaVersion]_[sbtVersion]/[revision]/[artifact](-[classifier])-[revision].[ext]"))

val uniformVersion = "0.6.0-20141114023858-cc10b9f-CDH5"

addSbtPlugin("au.com.cba.omnia" % "uniform-core" % uniformVersion)

addSbtPlugin("au.com.cba.omnia" % "uniform-dependency" % uniformVersion)

addSbtPlugin("au.com.cba.omnia" % "uniform-thrift" % uniformVersion)

addSbtPlugin("au.com.cba.omnia" % "uniform-assembly" % uniformVersion)
