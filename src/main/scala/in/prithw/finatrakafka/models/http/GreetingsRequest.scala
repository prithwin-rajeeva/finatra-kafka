package in.prithw.finatrakafka.models.http

import com.twitter.finatra.request.RouteParam

final case class GreetingsRequest(@RouteParam name: String)
