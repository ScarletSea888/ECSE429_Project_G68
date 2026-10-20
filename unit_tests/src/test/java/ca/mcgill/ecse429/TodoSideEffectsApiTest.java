
package ca.mcgill.ecse429;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TodoSideEffectsApiTest {

    private final List<String> createdTodoIds = new ArrayList<>();

    @BeforeEach
    void checkServiceIsReady() {
        RestAssured.baseURI = "http://localhost:4567";

        given()
            .accept("application/json")
            .when()
            .get("/todos")
            .then()
            .statusCode(200);
    }

    @AfterEach
    void cleanUpCreatedTodos() {
        for (String id : createdTodoIds) {
            int statusCode = given()
                .when()
                .delete("/todos/" + id)
                .statusCode();

            assertTrue(
                statusCode == 200 || statusCode == 404,
                "Cleanup failed for Todo " + id
                    + "; received status " + statusCode
            );
        }
    }

    @Test
    void rejectedCreationDoesNotChangeTodoCount() {
        int countBefore = getTodoCount();

        given()
            .contentType("application/json")
            .body(Map.of("doneStatus", false))
            .when()
            .post("/todos")
            .then()
            .statusCode(400);

        int countAfter = getTodoCount();

        assertEquals(
            countBefore,
            countAfter,
            "A rejected creation request must not add a Todo"
        );
    }

    @Test
    void updatingOneTodoDoesNotChangeAnotherTodo() {
        String firstId = createTodo("Side effect test - first");
        String secondId = createTodo("Side effect test - second");

        Response secondBefore = given()
            .accept("application/json")
            .when()
            .get("/todos/" + secondId)
            .then()
            .statusCode(200)
            .extract()
            .response();

        String originalTitle = secondBefore.jsonPath()
            .getString("todos[0].title");
        String originalDoneStatus = secondBefore.jsonPath()
            .getString("todos[0].doneStatus");
        String originalDescription = secondBefore.jsonPath()
            .getString("todos[0].description");

        given()
            .contentType("application/json")
            .body(Map.of(
                "title", "Side effect test - first updated",
                "doneStatus", true,
                "description", "Updated first Todo only"
            ))
            .when()
            .put("/todos/" + firstId)
            .then()
            .statusCode(200);

        given()
            .accept("application/json")
            .when()
            .get("/todos/" + secondId)
            .then()
            .statusCode(200)
            .body("todos[0].title",
                org.hamcrest.Matchers.equalTo(originalTitle))
            .body("todos[0].doneStatus",
                org.hamcrest.Matchers.equalTo(originalDoneStatus))
            .body("todos[0].description",
                org.hamcrest.Matchers.equalTo(originalDescription));
    }

    private int getTodoCount() {
        List<Map<String, Object>> todos = given()
            .accept("application/json")
            .when()
            .get("/todos")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getList("todos");

        return todos.size();
    }

    private String createTodo(String title) {
        Response response = given()
            .contentType("application/json")
            .body(Map.of(
                "title", title,
                "doneStatus", false,
                "description", "Created for side-effect testing"
            ))
            .when()
            .post("/todos")
            .then()
            .statusCode(201)
            .extract()
            .response();

        String location = response.getHeader("Location");
        assertNotNull(location, "Created Todo should have a Location header");

        String id = location.substring(location.lastIndexOf('/') + 1);
        assertFalse(id.isBlank(), "Location header should contain a Todo ID");

        createdTodoIds.add(id);
        return id;
    }
}
