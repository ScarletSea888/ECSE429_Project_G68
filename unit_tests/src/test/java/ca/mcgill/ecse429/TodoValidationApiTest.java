package ca.mcgill.ecse429;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TodoValidationApiTest {

    private static final String BASE_URL = "http://localhost:4567";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URL;

        try {
            Response readiness = given()
                .accept(ContentType.JSON)
                .get("/todos");

            assertEquals(200, readiness.statusCode(),
                "Todo API readiness check failed.");
        } catch (Exception exception) {
            fail("Todo API is not reachable. Start the API JAR first.",
                exception);
        }
    }

    @Test
    void stringInsteadOfBooleanDoneStatusReturnsBadRequest() {
        String body = """
            {
                "title": "JUnit_invalid_status_%s",
                "doneStatus": "false",
                "description": "Invalid string Boolean"
            }
            """.formatted(UUID.randomUUID());

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "A string doneStatus should return 400.");
    }

    @Test
    void unexpectedFieldReturnsBadRequest() {
        String body = """
            {
                "title": "JUnit_unexpected_field_%s",
                "doneStatus": false,
                "unexpectedField": "not allowed"
            }
            """.formatted(UUID.randomUUID());

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "An unexpected field should return 400.");
    }

    @Test
    void clientSuppliedIdReturnsBadRequest() {
        String body = """
            {
                "id": "999",
                "title": "JUnit_client_id_%s",
                "doneStatus": false,
                "description": "Client supplied ID"
            }
            """.formatted(UUID.randomUUID());

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "A client-supplied ID should return 400.");
    }

    @Test
    void whitespaceOnlyTitleReturnsBadRequest() {
        String body = """
            {
                "title": "   ",
                "doneStatus": false,
                "description": "Whitespace-only title"
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "A whitespace-only title should return 400.");
    }

    @Test
    void malformedJsonReturnsBadRequest() {
        String body = """
            {
                "title": "JUnit_malformed_json",
                "doneStatus": false,
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "Malformed JSON should return 400.");
    }

    @Test
    void unicodeAndSpecialCharactersInTitleAreAccepted() {
        String title = "JUnit café !@#$%^&*()_" + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": false,
                "description": "Unicode title"
            }
            """.formatted(title);

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(201, response.statusCode(),
            "A Unicode/special-character title should be accepted.");

        String location = response.getHeader("Location");
        assertNotNull(location,
            "Successful creation should include a Location header.");

        String id = location.substring(location.lastIndexOf('/') + 1);
        assertFalse(id.isBlank(), "Location header should contain a Todo ID.");

        try {
            Response retrieved = given()
                .accept(ContentType.JSON)
                .get("/todos/" + id);

            assertEquals(200, retrieved.statusCode());
            assertEquals(title,
                retrieved.jsonPath().getString("todos[0].title"));
        } finally {
            Response cleanup = given().delete("/todos/" + id);
            assertTrue(cleanup.statusCode() == 200
                    || cleanup.statusCode() == 404,
                "Temporary Todo should be cleaned up.");
        }
    }

    @Test
    void longTitleIsAccepted() {
        String title = "L".repeat(1000) + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": false,
                "description": "Long title boundary test"
            }
            """.formatted(title);

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(201, response.statusCode(),
            "A long title was accepted during exploratory testing.");

        String location = response.getHeader("Location");
        assertNotNull(location,
            "Successful creation should include a Location header.");

        String id = location.substring(location.lastIndexOf('/') + 1);

        try {
            Response retrieved = given()
                .accept(ContentType.JSON)
                .get("/todos/" + id);

            assertEquals(200, retrieved.statusCode());
            assertEquals(title,
                retrieved.jsonPath().getString("todos[0].title"));
        } finally {
            Response cleanup = given().delete("/todos/" + id);
            assertTrue(cleanup.statusCode() == 200
                    || cleanup.statusCode() == 404,
                "Temporary long-title Todo should be cleaned up.");
        }
    }
}