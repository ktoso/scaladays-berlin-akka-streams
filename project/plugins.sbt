resolvers += Resolver.url("Bintray", url("https://dl.bintray.com/lightbend/commercial-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.lightbend.cinnamon" % "sbt-cinnamon" % "2.0.0")
