import SbtProjectInfo._

ThisBuild / scalaVersion := "$scalaVersion$"
ThisBuild / version := SbtProjectInfo.ProjectVersion
ThisBuild / organization := "$organization$"
ThisBuild / organizationName := "$organizationName$"
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

lazy val core = projectCommonSettings("core", ProjectName("core"), file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++=
      libs.circe
  )

lazy val http = projectCommonSettings("http", ProjectName("http"), file("http"))
  .settings(
    libraryDependencies ++=
      libs.circe ++
        Seq(libs.log4s) ++
        libs.http4sClient
  )

lazy val app = projectCommonSettings("cli", ProjectName("app"), file("app"))
  .enablePlugins(JavaAppPackaging)
  .settings(debianPackageInfo: _*)
  .settings(
    maintainer := "Kevin Lee <kevin.code@kevinlee.io>"
  )
  .dependsOn(
    core % props.IncludeTest,
    http % props.IncludeTest,
  )

lazy val props                                   =
  new {
    val GitHubUsername = "$github_username$"
    val RepoName       = "$repo_name$"
    val ProjectName    = "$project_name$"

    val newtypeVersion = "$newtype_version$"
    val refinedVersion = "$refined_version$"

    val hedgehogVersion = "$hedgehog_version$"

    val catsVersion       = "$cats_version$"
    val catsEffectVersion = "$cats_effect_version$"

    val circeVersion = "$circe_version$"

    val http4sVersion = "$http4s_version$"

    val log4sVersion = "$log4s_version$"

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
    lazy val hedgehogLibs: Seq[ModuleID] = Seq(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val newtype = "io.estatico" %% "newtype" % props.newtypeVersion

    lazy val refined = Seq(
      "eu.timepit" %% "refined"      % props.refinedVersion,
      "eu.timepit" %% "refined-cats" % props.refinedVersion,
    )

    lazy val catsAndCatsEffect = Seq(
      "org.typelevel" %% "cats-core"   % props.catsVersion,
      "org.typelevel" %% "cats-effect" % props.catsEffectVersion,
    )

    lazy val log4s = "org.log4s" %% "log4s" % props.log4sVersion

    lazy val http4sClient = Seq(
      "org.http4s" %% "http4s-blaze-server" % props.http4sVersion,
      "org.http4s" %% "http4s-circe"        % props.http4sVersion,
      "org.http4s" %% "http4s-dsl"          % props.http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % props.http4sVersion,
    )

    lazy val circe = Seq(
      "io.circe" %% "circe-generic" % props.circeVersion,
      "io.circe" %% "circe-literal" % props.circeVersion,
      "io.circe" %% "circe-refined" % props.circeVersion,
    )

  }

// format: off
def prefixedProjectName(name: String) = s"\${props.RepoName}\${if (name.isEmpty) "" else s"-\$name"}"
// format: on

def scalacOptionsPostProcess(scalaSemVer: SemVer, options: Seq[String]): Seq[String] =
  scalaSemVer match {
    case SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _) =>
      ((if (patch >= 3) {
          options.distinct.filterNot(_ == "-Xlint:nullary-override")
        } else {
          options.distinct
        }) ++ Seq("-Ymacro-annotations", "-language:implicitConversions")).distinct
    case _: SemVer                                                            =>
      options.distinct
  }

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName),
      addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.11.3" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
      scalacOptions := scalacOptionsPostProcess(
        SemVer.parseUnsafe(scalaVersion.value),
        scalacOptions.value,
      ),
      resolvers ++= Seq(
        Resolver.sonatypeRepo("releases")
      ),
      libraryDependencies ++=
        libs.hedgehogLibs ++ Seq(libs.newtype) ++ libs.refined ++ libs.catsAndCatsEffect ++ libs.effectie
      /* WartRemover and scalacOptions { */
      //      , wartremoverErrors in (Compile, compile) ++= commonWarts((scalaBinaryVersion in update).value)
      //      , wartremoverErrors in (Test, compile) ++= commonWarts((scalaBinaryVersion in update).value)
      ,
      wartremoverErrors ++= commonWarts((scalaBinaryVersion in update).value)
      //      , wartremoverErrors ++= Warts.all
      ,
      Compile / console / wartremoverErrors := List.empty,
      Compile / console / wartremoverWarnings := List.empty,
      Compile / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .distinct
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / wartremoverErrors := List.empty,
      Test / console / wartremoverWarnings := List.empty,
      Test / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .distinct
          .filterNot(option => option.contains("wartremover") || option.contains("import"))
      /* } WartRemover and scalacOptions */,
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),

      /* Ammonite-REPL { */

    )

lazy val noPublish: SettingsDefinition = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in sbt.Keys.`package` := true,
  skip in packagedArtifacts := true,
  skip in publish := true,
)
