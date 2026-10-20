
package ca.mcgill.ecse429;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TodoCollectionBehaviorApiTest {

    private final List<String> createdTodoIds = new ArrayList<>();

    @BeforeEach
    void checkServiceIsReady() {
        RestAssured.baseURI = "http://localhost:4567";

        try {
            given()
                .accept(ContentType.JSON)
                .get("/todos")
                .then()
                .statusCode(200);
        } catch (Exception exception) {
            fail("Todo API is not reachable. Start the API JAR first.",
                    exception);
        }
    }

    @AfterEach
    void cleanUp() {
        for (String id : createdTodoIds) {
            int status = given()
                .delete("/todos/" + id)
                .statusCode();

            assertTrue(status == 200 || status == 404,
                "Cleanup failed for Todo " + id);
        }
    }

    @Test
    void filteringByTitleReturnsMatchingTodo() {
        String title = "FilterTitle_" + UUID.randomUUID();
        createTodo(title, false, "Filter description");

        Response response = given()
            .queryParam("title", title)
            .accept(ContentType.JSON)
            .get("/todos");

        assertEquals(200, response.statusCode());
        assertTrue(
            response.jsonPath()
                .getList("todos.title", String.class)
                .contains(title),
            "Title filter should return the matching Todo."
        );
    }

    @Test
    void filteringByDescriptionReturnsMatchingTodo() {
        String description = "FilterDescription_" + UUID.randomUUID();
        String title = "DescriptionFilter_" + UUID.randomUUID();
        createTodo(title, false, description);

        Response response = given()
            .queryParam("description", description)
            .accept(ContentType.JSON)
            .get("/todos");

        assertEquals(200, response.statusCode());
        assertTrue(
            response.jsonPath()
                .getList("todos.description", String.class)
                .contains(description),
            "Description filter should return the matching Todo."
        );
    }

    @Test
    void filteringByDoneStatusReturnsMatchingTodo() {
        String title = "DoneFilter_" + UUID.randomUUID();
        createTodo(title, true, "Completed filter test");

        Response response = given()
            .queryParam("doneStatus", "true")
            .accept(ContentType.JSON)
            .get("/todos");

        assertEquals(200, response.statusCode());

        List<Map<String, Object>> todos =
            response.jsonPath().getList("todos");

        assertTrue(
            todos.stream().anyMatch(todo ->
                title.equals(todo.get("title"))
                    && "true".equals(
                        String.valueOf(todo.get("doneStatus")))),
            "Done-status filter should return the completed Todo."
        );
    }

    @Test
    void filterWithNoMatchesReturnsEmptyCollection() {
        Response response = given()
            .queryParam("title", "NoMatch_" + UUID.randomUUID())
            .accept(ContentType.JSON)
            .get("/todos");

        assertEquals(200, response.statusCode());
        assertTrue(
            response.jsonPath().getList("todos").isEmpty(),
            "A filter with no matches should return an empty collection."
        );
    }

    @Test
    void headTodosReturnsSuccessWithoutResponseBody() {
        Response response = given()
            .head("/todos");

        assertEquals(200, response.statusCode());
        assertTrue(
            response.asString().isEmpty(),
            "HEAD should not return a response body."
        );
    }

    @Test
    void optionsTodosReturnsAllowedMethods() {
        Response response = given()
            .options("/todos");

        assertEquals(200, response.statusCode());

        String allow = response.getHeader("Allow");

        assertNotNull(allow, "OPTIONS response should include Allow.");

        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("HEAD"));
        assertTrue(allow.contains("OPTIONS"));
    }

    @Test
    void patchTodosReturnsMethodNotAllowed() {
        Response response = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "title", "Patch should not be supported",
                "doneStatus", false
            ))
            .patch("/todos");

        assertEquals(405, response.statusCode());
    }
    
    @Test
    void patchTodoItemReturnsMethodNotAllowed() {
        String title = "PatchItem_" + UUID.randomUUID();
        String id = createTodo(title, false, "PATCH item method test");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "title", "PATCH should not be supported",
                        "doneStatus", false
                ))
                .patch("/todos/" + id);

        assertEquals(405, response.statusCode());

        // A rejected PATCH must not modify the existing Todo.
        Response afterPatch = given()
                .accept(ContentType.JSON)
                .get("/todos/" + id);

        assertEquals(200, afterPatch.statusCode());
        assertEquals(
                title,
                afterPatch.jsonPath().getString("todos[0].title")
        );
        assertEquals(
                "false",
                afterPatch.jsonPath().getString("todos[0].doneStatus")
        );
    }

    @Test
    void ascendingAndDescendingSortParametersMatchObservedBehavior() {
        createTodo("SortTest_" + UUID.randomUUID(),
            false, "Sort test");

        List<String> ascending = given()
            .queryParam("_sortBy", "id")
            .accept(ContentType.JSON)
            .get("/todos")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("todos.id", String.class);

        List<String> descending = given()
            .queryParam("_sortBy", "-id")
            .accept(ContentType.JSON)
            .get("/todos")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("todos.id", String.class);

        assertEquals(
            ascending,
            descending,
            "Record the observed behavior: both sort parameters "
                + "currently return the same order."
        );
    }

    private String createTodo(
            String title, boolean doneStatus, String description) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("doneStatus", doneStatus);
        payload.put("description", description);

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(payload)
            .post("/todos");

        assertEquals(201, response.statusCode(),
            "Creating a test Todo should return 201.");

        String location = response.getHeader("Location");
        assertNotNull(location,
            "Creation response should include Location.");

        String id = location.substring(location.lastIndexOf('/') + 1);
        assertFalse(id.isBlank(),
            "Location header should contain a Todo ID.");

        createdTodoIds.add(id);
        return id;
    }
}
