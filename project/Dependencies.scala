import sbt._

object Dependencies {
  object V {
    val bm4             = "0.3.1"
    val cats            = "2.3.0"
    val ctxApplied      = "0.1.4"
    val disciplineMunit = "1.0.4"
    val fs2             = "2.5.0"
    val kp              = "0.11.2"
    val monocle         = "2.0.3"
    val munit           = "0.7.20"
    val newtype         = "0.4.4"
  }

  val catsLaws        = "org.typelevel"              %% "cats-laws"        % V.cats
  val disciplineMunit = "org.typelevel"              %% "discipline-munit" % V.disciplineMunit
  val fs2Core         = "co.fs2"                     %% "fs2-core"         % V.fs2
  val monocleCore     = "com.github.julien-truffaut" %% "monocle-core"     % V.monocle
  val monocleMacro    = "com.github.julien-truffaut" %% "monocle-macro"    % V.monocle
  val munitCore       = "org.scalameta"              %% "munit"            % V.munit
  val munitScalaCheck = "org.scalameta"              %% "munit-scalacheck" % V.munit
  val newtype         = "io.estatico"                %% "newtype"          % V.newtype

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy"     %% "better-monadic-for" % V.bm4)
    val contextApplied   = compilerPlugin("org.augustjune" %% "context-applied"    % V.ctxApplied)
    val kindProjector    = compilerPlugin("org.typelevel"  %% "kind-projector"     % V.kp cross CrossVersion.full)
  }
}
