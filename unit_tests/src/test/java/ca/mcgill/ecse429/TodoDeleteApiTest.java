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

class TodoDeleteApiTest {

    private static final String BASE_URL = "http://localhost:4567";

    private String todoId;

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

        String title = "JUnit_delete_" + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": false,
                "description": "Temporary record for delete tests"
            }
            """.formatted(title);

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
    void deleteTodoRemovesItFromTheApi() {
        Response response = given().delete("/todos/" + todoId);

        assertEquals(200, response.statusCode(),
            "Deleting an existing Todo should return 200.");

        Response retrieval = given()
            .accept(ContentType.JSON)
            .get("/todos/" + todoId);

        assertEquals(404, retrieval.statusCode(),
            "A deleted Todo should no longer be retrievable.");

        todoId = null;
    }

    @Test
    void deletingNonexistentTodoReturnsNotFound() {
        Response response = given()
            .delete("/todos/999999999");

        assertEquals(404, response.statusCode(),
            "Deleting a nonexistent Todo should return 404.");
    }

    @Test
    void deletingSameTodoTwiceReturnsNotFoundOnSecondAttempt() {
        Response firstDelete = given().delete("/todos/" + todoId);

        assertEquals(200, firstDelete.statusCode(),
            "The first deletion should return 200.");

        Response secondDelete = given().delete("/todos/" + todoId);

        assertEquals(404, secondDelete.statusCode(),
            "Deleting the same Todo again should return 404.");

        todoId = null;
    }
}