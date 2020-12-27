package com.gvolpe.fsmstreams

import cats.effect._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    IO(println("Hello world!")).as(ExitCode.Success)

}
