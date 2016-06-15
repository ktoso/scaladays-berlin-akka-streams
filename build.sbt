name := "akka-streams-rock"

version := "1.0"

// Enable the Cinnamon Lightbend Monitoring sbt plugin
lazy val akkaStreamsRock = project in file(".") enablePlugins (Cinnamon)

// Add the Monitoring Agent for run and test
cinnamon in run := true
cinnamon in test := true

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.7"

libraryDependencies ++= Seq(
  // TODO: show a bottleneck
  "com.typesafe.akka" %% "akka-actor"             % akkaVersion,
  
  // TODO: use persistent entities to shard out some load
  "com.typesafe.akka" %% "akka-cluster-sharding"  % akkaVersion,
  // TODO: use persistent actors...
  "com.typesafe.akka" %% "akka-persistence"                    % akkaVersion,
  // TODO: use persistence query to build a view model - update a living actor there
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion,
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8",

  // TODO explain status experimentla here
  "com.typesafe.akka" %% "akka-http-core"                    % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental"            % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,

  // Lightbend monitoring
  // TODO explain futures and streams are next to be monitored
  Cinnamon.library.cinnamonCHMetrics,
  Cinnamon.library.cinnamonAkka,
  "com.lightbend.cinnamon" %% "cinnamon-chmetrics-statsd-reporter" % "2.0.0",
  "com.lightbend.cinnamon" %% "cinnamon-slf4j-mdc" % "2.0.0", // TODO show MDC
  
  // akka-stream-kafka A.K.A. "reactive kafka"
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.11-M3", // TODO explain milestone status
  

  "com.typesafe.akka" %% "akka-testkit"      % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % "test",
  "org.scalatest"     %% "scalatest"         % "2.2.6"     % "test",
  "junit"             %  "junit"             % "4.12"      % "test",
  "com.novocode"      %  "junit-interface"   % "0.11"      % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
