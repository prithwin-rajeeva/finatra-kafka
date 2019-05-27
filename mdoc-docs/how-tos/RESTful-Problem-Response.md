# How to report a RESTful API's problem?

Finatra.g8 project template provides you a simple [RFC8707](https://tools.ietf.org/html/rfc7807.html) based solution to generate an HTTP response when your service needs to report a problem to the requester. This document does require you to know the specification of RFC7807 in order to understand the following Problem response data structure.

The main type you need to know is `Problem` or `NoExtensionProblem` type.
These two types represent a `Problem` response's data structure.

### Problem
Problem type allows you to define a Problem response with an extension that is not defined in RFC7807.

```
final case class Problem[T, E](
    `type`: Uri = Uri.parse("about:blank"),
    title: Title,
    status: Status,
    detail: Detail[T],
    extension: E
)
```

All you need to do is to define your extension type, for example:

```
object SampleExtension {
  final case class TraceId(id: String)
  final case class AccountId(id: String)
}

final case class SampleExtension(traceId: TraceId, accountId: AccountId)
```

#### Sample

```scala mdoc
import com.github.mehmetakiftutuncu.errors.{Errors, SimpleError}
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status._
import in.prithw.finatrakafka.problem.Problem
import in.prithw.finatrakafka.problem.ProblemResponse
import in.prithw.finatrakafka.problem.ProblemResponse._

object SampleExtension {
  final case class TraceId(id: String)   extends AnyVal
  final case class AccountId(id: String) extends AnyVal
}

import SampleExtension._

final case class SampleExtension(traceId: TraceId, accountId: AccountId)

val p = Problem[Errors, SampleExtension](title = Title("File Not Found"),
                                         status = NotFound,
                                         detail = ErrorsDetail(Errors(SimpleError.notFound)),
                                         extension = SampleExtension(TraceId("tc-2846673"), AccountId("ac-42567833")))

val problemResp: Response = ProblemResponse(p)

val respContent: String = problemResp.contentString
```

### NoExtensionProblem

Other than `Problem`, sometimes you just have no need of data extension, `NoExtensionProblem` is the type you should use.

```
final case class NoExtensionProblem[T](
    `type`: Uri = Uri.parse("about:blank"),
    title: Title,
    status: Status,
    detail: Detail[T]
)
```

#### Sample

```scala mdoc
import java.lang.Throwable
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status._
import in.prithw.finatrakafka.problem.NoExtensionProblem
import in.prithw.finatrakafka.problem.ProblemResponse
import in.prithw.finatrakafka.problem.ProblemResponse._

val noExtP = NoExtensionProblem[Throwable](title = Title("File Not Found"),
                                           status = NotFound,
                                           detail = ThrowableDetail(new Throwable("File Not Found")))

val noExtProblemResp: Response = ProblemResponse(noExtP)

val noExtRespContent: String = noExtProblemResp.contentString
```

### Problem detail

Two problem detail types are provided for regular use. However, we understand you need flexibility as well.
Therefore, a `Detail[T]` is defined, you can have your own T design to suit your need.
Usually, this T type represents your own error type. Thus we give you two pre-defined Detail types: `ThrowableDetail` and `ErrorsDetail`.

The definition of `Detail[T]` is the following:

```
 trait Detail[T]  extends Product with Serializable {
  def detailMsg(implicit show: Show[T]): String
}
```

`detailMsg` takes a Typelevel Cats' [Show[T]](https://typelevel.org/cats/typeclasses/show.html) type class to generate detailed message.

#### Sample

```
implicit val throwableShow: Show[Throwable] =
(t: Throwable) =>
    p""""${t.getClass.getName} occurred${t.getMessage.|>(m => (m =!= "").fold(p", reason:`$m`", ""))}""""

final case class ThrowableDetail(t: Throwable) extends Detail[Throwable] {
    override def detailMsg(implicit show: Show[Throwable]): String = show.show(t)
}    
```

Generally, the above implementation should be packaged in an object, thus import that object can bring these implementation in the runtime context. You can refer to `ProblemResponseSpec` in the test for further study.
