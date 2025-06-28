package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation {

  // ========================================
  // CONFIGURACIÓN HTTP
  // ========================================
  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .shareConnections

  // ========================================
  // ESCENARIO 1: LOGIN
  // ========================================
  val loginScenario = scenario("Login Scenario")
    .exec(
      http("Login Request")
        .post(s"users/login")
        .body(StringBody(
          s"""{
             |  "email": "$email",
             |  "password": "$password"
             |}""".stripMargin
        )).asJson
        .check(
          status.is(200),
          jsonPath("$.token").saveAs("authToken")
        )
    )

  // ========================================
  // ESCENARIO 2: CREAR CONTACTO
  // ========================================
  val createContactScenario = scenario("Create Contact Scenario")
    .exec(
      http("Create Contact Request")
        .post("contacts")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody(
          s"""{
             |  "firstName": "Jair Santiago",
             |  "lastName": "Leal Miranda",
             |  "birthdate": "2002-10-07",
             |  "email": "jair@email.com",
             |  "phone": "3001234567",
             |  "street1": "Calle 10 # 45-67",
             |  "street2": "Apartamento 302",
             |  "city": "Medellín",
             |  "stateProvince": "Antioquia",
             |  "postalCode": "050001",
             |  "country": "Colombia"
             |}""".stripMargin
        )).asJson
        .check(status.is(201))
    )

  // ========================================
  // ESCENARIO COMPUESTO: LOGIN + CREAR CONTACTO
  // ========================================
  val completeWorkflow = scenario("Complete Login and Contact Creation Workflow")
    .exec(loginScenario)
    .exec(createContactScenario)

  // ========================================
  // CONFIGURACIÓN DE CARGA
  // ========================================
  setUp(
    completeWorkflow.inject(
      rampUsers(10).during(50)
    )
  ).protocols(httpConf)
}
