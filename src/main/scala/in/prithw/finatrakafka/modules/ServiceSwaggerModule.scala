package in.prithw.finatrakafka.modules

import com.google.inject.Provides
import com.jakehschwartz.finatra.swagger.SwaggerModule
import io.swagger.models.{Contact, Info, Swagger}
import io.swagger.models.auth.BasicAuthDefinition

object ServiceSwaggerModule extends SwaggerModule {
  val swaggerUI      = new Swagger()
  val serviceVersion = flag[String]("service.version", "NA", "the version of service")

  @Provides
  def swagger: Swagger = {

    val info = new Info()
      .contact(new Contact().name("Prithwin Rajeeva").email("prithwin@outlook.com"))
      .description(
        "**finatra kafka** - This service would allow me to go through a small service that would  let me know everything that I have to know about kafka and slick and everything..")
      .version(serviceVersion())
      .title("finatra kafka API")

    swaggerUI
      .info(info)
      .addSecurityDefinition("sampleBasic", {
        val d = new BasicAuthDefinition()
        d.setType("basic")
        d
      })

    swaggerUI
  }
}
