package com.dius.pact.play

import com.dius.pact.runner.PactFileSource
import org.specs2.mutable.Specification
import scala.concurrent.Await
import scala.concurrent.duration._

import org.specs2.specification.{FormattingFragments => FF}
import play.api.libs.ws.WS
import play.api.test.Helpers._
import com.dius.pact.model.Pact
import com.dius.pact.model.Request
import play.api.test.WithServer
import play.api.Play.current
import play.api.libs.json.Json

trait PactPlaySpecification extends Specification {


  //val testJson = s"src/test/resources/pacts"

  def testJson(): String

  def loadPacts(dir:String):Seq[Pact] = PactFileSource.loadFiles(dir)


  //TODO: can use spray client to replace play WS
  def fullUrl(path: String) = WS.url("http://localhost:19001" + path)
  def fullUrlJson(path: String) = fullUrl(path).withHeaders(CONTENT_TYPE -> "application/json")

  def chooseRequest(path: String,input: String, method: String) = method.toLowerCase() match {
    case "get" => fullUrl(path).get()
    case "post" => fullUrlJson(path).post(input)
    case "put" => fullUrlJson(path).put(input)
  }

  private val pacts: Seq[Pact] = loadPacts(testJson)

  pacts.map { j =>
    addFragments(FF.p)
    addFragments(j.provider.name + " should")
    j.interactions.map{ i =>
      i.description >>  new WithServer {
        val result = {
          val request: Request = i.request
          Await.result(chooseRequest(request.path,request.body.getOrElse(""),request.method.toString()), Duration(3, SECONDS))
        }
        result.status === i.response.status

        i.response.body.map(b => Json.parse(result.body) === Json.parse(b)).getOrElse(1 === 1)

      }
    }
  }


}

