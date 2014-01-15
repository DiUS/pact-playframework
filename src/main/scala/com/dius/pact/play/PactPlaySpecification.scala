package com.dius.pact.play

import com.dius.pact.runner.PactFileSource
import org.specs2.mutable.Specification
import scala.concurrent.{Promise, Future, Await}
import scala.concurrent.duration._

import org.specs2.specification.{FormattingFragments => FF, Step, Fragments}
import com.dius.pact.model.{ResponseMatching, Response, Request}
import play.api.test.WithServer
import java.io.File
import com.dius.pact.model.Matching.MatchFound
import akka.actor.ActorSystem
import spray.http._
import spray.http.HttpRequest
import com.dius.pact.runner.PactConfiguration
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpResponse
import play.api.test.FakeApplication
import com.dius.pact.model.spray.Conversions

trait PactPlaySpecification extends Specification {

  def pactRoot: File
  def pactConfig: PactConfiguration
  def startAppInState(state: String): FakeApplication

  lazy val playExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val aSys = Promise[ActorSystem]()

  private def startActorSystem = {
    aSys.success(ActorSystem("Pact-Provider-Verification-Actor-System"))
  }

  private def stopActorSystem = {
    aSys.future.map(_.shutdown())(playExecutionContext)
  }

  private def invoke(baseUrl: String, request: Request): Future[Response] = {
    aSys.future.flatMap{ actorSystem =>
      implicit val system = actorSystem
      implicit val executionContext = system.dispatcher
      val pipeline: HttpRequest => Future[HttpResponse] = _root_.spray.client.pipelining.sendReceive
      val method = HttpMethods.getForKey(request.method.toString.toUpperCase).get
      val uri = Uri(s"$baseUrl${request.path}")
      val headers: List[HttpHeader] = request.headers.map(_.toList.map{case (key, value) => RawHeader(key, value)}).getOrElse(Nil)
      val entity: HttpEntity = request.bodyString.map(HttpEntity(_)).getOrElse(HttpEntity.Empty)
      pipeline(HttpRequest(method, uri, headers, entity)).map{ sprayResponse =>
        Conversions.sprayToPactResponse(sprayResponse)
      }(playExecutionContext)
    }(playExecutionContext)
  }

  PactFileSource.loadFiles(pactRoot).map { pact =>
    addFragments(FF.p)
    addFragments(pact.provider.name + " should")
    pact.interactions.map { interaction =>
      interaction.description >>  new WithServer(startAppInState(interaction.providerState)) {
        val actualResponse = {
          val request: Request = interaction.request
          Await.result(invoke(s"http://localhost:$port", request), Duration(3, SECONDS))
        }
        ResponseMatching.matchRules(interaction.response, actualResponse) must beEqualTo(MatchFound)
      }
    }
  }

  override def map(fs: =>Fragments) =  Step(startActorSystem) ^ fs ^ Step(stopActorSystem)
}

