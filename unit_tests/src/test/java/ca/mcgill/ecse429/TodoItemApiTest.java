package ca.mcgill.ecse429;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TodoItemApiTest {

    private static final String BASE_URL = "http://localhost:4567";

    private String todoId;
    private String todoTitle;

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

        todoTitle = "JUnit_item_" + UUID.randomUUID();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", todoTitle);
        payload.put("doneStatus", false);
        payload.put("description", "Temporary record for item API tests.");

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(payload)
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

            assertEquals(200, response.statusCode(),
                "Temporary Todo should be deleted after the test.");

            todoId = null;
        }
    }

    @Test
    void getTodoAsJsonReturnsRequestedTodo() {
        Response response = given()
            .accept(ContentType.JSON)
            .get("/todos/" + todoId);

        assertEquals(200, response.statusCode());

        assertEquals(todoId,
            response.jsonPath().getString("todos[0].id"));
        assertEquals(todoTitle,
            response.jsonPath().getString("todos[0].title"));
    }

    @Test
    void getTodoAsXmlReturnsRequestedTodo() {
        Response response = given()
            .accept(ContentType.XML)
            .get("/todos/" + todoId);

        assertEquals(200, response.statusCode());
        assertTrue(response.getContentType().contains("xml"),
            "Response should use an XML content type.");
        assertTrue(response.asString().contains(todoTitle),
            "XML response should contain the requested Todo title.");
    }

    @Test
    void getNonexistentTodoReturnsNotFound() {
        Response response = given()
            .get("/todos/999999999");

        assertEquals(404, response.statusCode(),
            "Retrieving a nonexistent Todo should return 404.");
    }
}