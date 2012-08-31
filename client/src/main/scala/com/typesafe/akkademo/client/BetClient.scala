/**
 * Copyright (C) 2011-2012 Typesafe <http://typesafe.com/>
 */
package com.typesafe.akkademo.client

import akka.actor.ActorSystem
import akka.dispatch.Await
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import com.typesafe.akkademo.common.{Bet, RetrieveBets}
import com.typesafe.akkademo.common.Bet

object BetClient extends App {

  override def main(args: Array[String]): Unit = {
    println("*** STARTING TEST OF BETTING APPLICATION")

    val config = ConfigFactory.parseString( """
      akka {
        actor {
          provider = "akka.remote.RemoteActorRefProvider"
        }
        remote {
          transport = "akka.remote.netty.NettyRemoteTransport"
          netty {
            hostname = "127.0.0.1"
            port = 2661
          }
        }
      }""")

    val system = ActorSystem("TestActorSystem", ConfigFactory.load(config))
    val service = system.actorFor("akka://BettingServiceActorSystem@127.0.0.1:2552/user/bettingService")

    if (args.size > 0  && args(0) == "send") {
      (1 to 200).foreach {
        p => service ! Bet("ready_player_one", p % 10 + 1, p % 100 + 1)
      }
    } else {
      implicit val timeout = Timeout(2 seconds)
      val fBets = ask(service, RetrieveBets).mapTo[List[Bet]]
      Await.result(fBets, 5 seconds)
      fBets.foreach { b => println(">> " + b) }
    }

    println("*** TESTING OK")
    system.shutdown()
  }
}