package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._
import scala.concurrent.duration._        // <= necesario para .seconds

class LoginTest extends Simulation{

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition
  val scn = scenario("Login").
    exec(http("login")
      .post(s"users/login")
      .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
       //Recibir información de la cuenta
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("authToken"))
    )
  // Scenario: Add Contact (includes login)
  val scnAddContact = scenario("Add Contact")
    .exec(
      http("Create Contact")
        .post("/contacts")
        .header("Authorization", "Bearer ${token}")
        .body(StringBody(
          "{\n" +
          "  \"firstName\": \"John\",\n" +
          "  \"lastName\": \"Doe\",\n" +
          "  \"birthdate\": \"1970-01-01\",\n" +
          "  \"email\": \"jdoe@fake.com\",\n" +
          "  \"phone\": \"8005555555\",\n" +
          "  \"street1\": \"1 Main St.\",\n" +
          "  \"street2\": \"Apartment A\",\n" +
          "  \"city\": \"Anytown\",\n" +
          "  \"stateProvince\": \"KS\",\n" +
          "  \"postalCode\": \"12345\",\n" +
          "  \"country\": \"USA\"\n" +
          "}"
        )).asJson
        .check(status.is(201))
    )


  // 3 Load Scenario
  setUp(
    scn.inject(rampUsersPerSec(5).to(15).during(30)),
    scnAddContact.inject(rampUsersPerSec(5).to(15).during(30.seconds))
  ).protocols(httpConf);
}
