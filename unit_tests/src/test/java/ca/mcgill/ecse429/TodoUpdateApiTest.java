package ca.mcgill.ecse429;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TodoUpdateApiTest {

    private static final String BASE_URL = "http://localhost:4567";

    private String todoId;
    private String originalTitle;

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

        originalTitle = "JUnit_update_original_" + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": false,
                "description": "Original test description"
            }
            """.formatted(originalTitle);

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        if (response.statusCode() != 201) {
            fail("Creating the temporary Todo should return 201.");
        }

        String location = response.getHeader("Location");
        assertNotNull(location,
            "Creation response should include a Location header.");

        todoId = location.substring(location.lastIndexOf('/') + 1);

        assertFalse(todoId.isBlank(),
            "Location header should contain a Todo ID.");
    }

    @AfterEach
    void tearDown() {
        if (todoId != null) {
            Response response = given().delete("/todos/" + todoId);

            assertTrue(
                response.statusCode() == 200
                        || response.statusCode() == 404,
                "Cleanup should delete the temporary Todo or confirm "
                        + "it is already absent. ID: " + todoId
            );

            todoId = null;
        }
    }

    @Test
    void postUpdatesTodoAndChangesPersist() {
        String updatedTitle = "JUnit_post_updated_" + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": true,
                "description": "Updated through POST"
            }
            """.formatted(updatedTitle);

        Response updateResponse = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos/" + todoId);

        assertEquals(200, updateResponse.statusCode(),
            "POST update should return 200.");

        Response retrieved = given()
            .accept(ContentType.JSON)
            .get("/todos/" + todoId);

        assertEquals(200, retrieved.statusCode());
        assertEquals(updatedTitle,
            retrieved.jsonPath().getString("todos[0].title"));
        assertEquals("true",
            retrieved.jsonPath().getString("todos[0].doneStatus"));
        assertEquals("Updated through POST",
            retrieved.jsonPath().getString("todos[0].description"));
    }

    @Test
    void putUpdatesTodoAndChangesPersist() {
        String updatedTitle = "JUnit_put_updated_" + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": true,
                "description": "Updated through PUT"
            }
            """.formatted(updatedTitle);

        Response updateResponse = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .put("/todos/" + todoId);

        assertEquals(200, updateResponse.statusCode(),
            "PUT update should return 200.");

        Response retrieved = given()
            .accept(ContentType.JSON)
            .get("/todos/" + todoId);

        assertEquals(200, retrieved.statusCode());
        assertEquals(updatedTitle,
            retrieved.jsonPath().getString("todos[0].title"));
        assertEquals("true",
            retrieved.jsonPath().getString("todos[0].doneStatus"));
        assertEquals("Updated through PUT",
            retrieved.jsonPath().getString("todos[0].description"));
    }

    @Test
    void postUpdateToNonexistentTodoReturnsNotFound() {
        String body = """
            {
                "title": "JUnit nonexistent update",
                "doneStatus": false,
                "description": "This Todo does not exist"
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos/999999999");

        assertEquals(404, response.statusCode(),
            "Updating a nonexistent Todo through POST should return 404.");
    }

    @Test
    void putUpdateToNonexistentTodoReturnsNotFound() {
        String body = """
            {
                "title": "JUnit nonexistent update",
                "doneStatus": false,
                "description": "This Todo does not exist"
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .put("/todos/999999999");

        assertEquals(404, response.statusCode(),
            "Updating a nonexistent Todo through PUT should return 404.");
    }
}