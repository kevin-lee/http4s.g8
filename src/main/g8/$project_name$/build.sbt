import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader.SystemV

ThisBuild / scalaVersion := props.ScalaVersion
ThisBuild / version := props.ProjectVersion
ThisBuild / organization := props.Org
ThisBuild / organizationName := props.OrgName
ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "$author_name$",
    "$author_email$",
    url(s"https://github.com/\${props.GitHubUsername}"),
  )
)
ThisBuild / homepage := Some(url(s"https://github.com/\${props.GitHubUsername}/\${props.RepoName}"))
ThisBuild / scmInfo :=
  Some(
    ScmInfo(
      url(s"https://github.com/\${props.GitHubUsername}/\${props.RepoName}"),
      s"https://github.com/\${props.GitHubUsername}/\${props.RepoName}.git",
    )
  )

lazy val root = (project in file("."))
  .settings(
    name := props.ProjectName
  )
  .settings(noPublish)
  .aggregate(core, service, http, app)

lazy val core = subProject("core", file("core"))
  .settings(
    libraryDependencies ++= libs.pureconfig :: libs.circe
  )

lazy val service = subProject("service", file("service"))
  .dependsOn(
    core % props.IncludeTest
  )

lazy val http = subProject("http", file("http"))
  .settings(
    libraryDependencies ++= List(libs.log4s, libs.logback) ++ libs.http4s
  )
  .dependsOn(
    core    % props.IncludeTest,
    service % props.IncludeTest,
  )

lazy val app = subProject("app", file("app"))
  .enablePlugins(JavaAppPackaging)
  .settings(debianPackageInfo)
  .settings(
    maintainer := "$author_name$ <$author_email$>"
  )
  .dependsOn(
    core    % props.IncludeTest,
    service % props.IncludeTest,
    http    % props.IncludeTest,
  )

lazy val props                                   =
  new {
    val ScalaVersion = "$scalaVersion$"
    val Org          = "$organization$"
    val OrgName      = "$organizationName$"

    val GitHubUsername = "$github_username$"
    val RepoName       = "$repo_name$"
    val ProjectName    = "$project_name$"
    val ProjectVersion = "0.1.0-SNAPSHOT"

    val newtypeVersion = "$newtype_version$"
    val refinedVersion = "$refined_version$"

    val hedgehogVersion = "$hedgehog_version$"

    val catsVersion       = "$cats_version$"
    val catsEffectVersion = "$cats_effect_version$"

    val pureconfig = "$pureconfig_version$"

    val circeVersion = "$circe_version$"

    val http4sVersion = "$http4s_version$"

    val log4sVersion = "$log4s_version$"
    val logbackVersion = "$logback_version$"

    val IncludeTest: String = "compile->compile;test->test"
  }
val removeDottyIncompatible: ModuleID => Boolean =
  m =>
    m.name == "wartremover" ||
      m.name == "ammonite" ||
      m.name == "kind-projector" ||
      m.name == "mdoc"

lazy val libs =
  new {
    lazy val hedgehogLibs: Seq[ModuleID] = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val newtype = "io.estatico" %% "newtype" % props.newtypeVersion

    lazy val refined = List(
      "eu.timepit" %% "refined"            % props.refinedVersion,
      "eu.timepit" %% "refined-cats"       % props.refinedVersion,
      "eu.timepit" %% "refined-pureconfig" % props.refinedVersion,
    )

    lazy val catsAndCatsEffect = List(
      "org.typelevel" %% "cats-core"   % props.catsVersion,
      "org.typelevel" %% "cats-effect" % props.catsEffectVersion,
    )

    lazy val log4s   = "org.log4s"     %% "log4s"           % props.log4sVersion
    lazy val logback = "ch.qos.logback" % "logback-classic" % props.logbackVersion

    lazy val http4s = List(
      "org.http4s" %% "http4s-blaze-server" % props.http4sVersion,
      "org.http4s" %% "http4s-circe"        % props.http4sVersion,
      "org.http4s" %% "http4s-dsl"          % props.http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % props.http4sVersion,
    )

    lazy val circe = List(
      "io.circe" %% "circe-generic" % props.circeVersion,
      "io.circe" %% "circe-literal" % props.circeVersion,
      "io.circe" %% "circe-refined" % props.circeVersion,
    )

    lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % props.pureconfig

  }

// format: off
def prefixedProjectName(name: String) = s"\${props.RepoName}\${if (name.isEmpty) "" else s"-\$name"}"
// format: on

def subProject(projectName: String, file: File): Project =
  Project(projectName, file)
    .settings(
      name := prefixedProjectName(projectName),
      addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
      scalacOptions ~= (opts => ("-Ymacro-annotations" +: opts).distinct),
      libraryDependencies ++=
        libs.hedgehogLibs ++ List(libs.newtype) ++ libs.refined ++ libs.catsAndCatsEffect,
      testFrameworks ~= (testFws => (TestFramework("hedgehog.sbt.Framework") +: testFws).distinct),
    )

lazy val debianPackageInfo: SettingsDefinition = List(
  Linux / maintainer := "$author_name$ <$author_email$>",
  Linux / packageSummary := "My App",
  packageDescription := "My app is ...",
  Debian / serverLoading := Some(SystemV),
)

lazy val noPublish: SettingsDefinition = List(
  publish := {},
  publishM2 := {},
  publishLocal := {},
  publishArtifact := false,
  sbt.Keys.`package` / skip := true,
  packagedArtifacts / skip := true,
  publish / skip := true,
)
