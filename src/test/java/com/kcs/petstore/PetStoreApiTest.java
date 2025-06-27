package com.kcs.petstore;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.is;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetStoreApiTest {

    private static final String BASE_URL = "https://petstore.swagger.io/v2";
    private static long petId = 213456789;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(new AllureRestAssured());
    }

    @Test
    @Order(1)
    void createPet() {
        String requestBody = """
                {             "id": %d,
                              "name": "asdkutya1",
                              "status": "available"
                            }
                """.formatted(petId);

        petId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/pet")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    @Test
    @Order(2)
    void getPetByID() {
        await().atMost(10, SECONDS).pollInterval(500, MILLISECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/pet/" + petId)
                    .then()
                    .statusCode(200)
                    .body("id", is((int) petId))
                    .body("name", is("asdkutya1"));
        });
    }

    @Test
    @Order(3)
    void updatePet() {
        String requestBody = """
            {
              "id": %d,
              "category": {
                "id": 1,
                "name": "cat"
              },
              "name": "asdmacska1",
              "photoUrls": ["string"],
              "tags": [
                {
                  "id": 0,
                  "name": "string"
                }
              ],
              "status": "sold"
            }
            """.formatted(petId);

        await().atMost(5, SECONDS).pollInterval(1, SECONDS).untilAsserted(() -> {
            given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .put("/pet")
                    .then()
                    .statusCode(200)
                    .body("name", is("asdmacska1"))
                    .body("status", is("sold"));
        });
    }

    @Test
    @Order(4)
    void getNewPetByID() {
        await().atMost(10, SECONDS).pollInterval(500, MILLISECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/pet/" + petId)
                    .then()
                    .statusCode(200)
                    .body("id", is((int) petId))
                    .body("name", is("asdmacska1"))
                    .body("status", is("sold"));
        });
    }

    @Test
    @Order(5)
    void deletePet() {
        await().atMost(10, SECONDS).pollInterval(500, MILLISECONDS).untilAsserted(() -> {
            given()
                    .header("api_key", "special-key")
                    .when()
                    .delete("/pet/" + petId)
                    .then()
                    .statusCode(200)
                    .body("message", is(String.valueOf(petId)));
        });
        await().atMost(10, SECONDS).pollInterval(500, MILLISECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/pet/" + petId)
                    .then()
                    .statusCode(404);
        });
    }
}