package ca.mcgill.ecse429;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class TodoCreateApiTest {

    private static final String BASE_URL = "http://localhost:4567";

    private final List<String> createdTodoIds = new ArrayList<>();

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

    @AfterEach
    void tearDown() {
        for (String id : createdTodoIds) {
            Response response = given().delete("/todos/" + id);

            assertTrue(
                response.statusCode() == 200
                    || response.statusCode() == 404,
                "Cleanup should delete the temporary Todo or confirm "
                    + "it is already absent. ID: " + id
            );
        }

        createdTodoIds.clear();
    }

    @Test
    void createTodoWithJsonReturnsCreatedTodo() {
        String title = "JUnit_create_json_" + UUID.randomUUID();

        String body = """
            {
                "title": "%s",
                "doneStatus": false,
                "description": "Created by JSON unit test"
            }
            """.formatted(title);

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(201, response.statusCode(),
            "Valid JSON creation should return 201.");

        String location = response.getHeader("Location");
        assertNotNull(location);

        String id = location.substring(location.lastIndexOf('/') + 1);
        assertFalse(id.isBlank());
        createdTodoIds.add(id);

        Response retrieved = given()
            .accept(ContentType.JSON)
            .get("/todos/" + id);

        assertEquals(200, retrieved.statusCode());
        assertEquals(title, retrieved.jsonPath().getString("todos[0].title"));
        assertEquals("Created by JSON unit test",
            retrieved.jsonPath().getString("todos[0].description"));
    }

    @Test
    void createTodoWithXmlReturnsCreatedTodo() {
        String title = "JUnit_create_xml_" + UUID.randomUUID();

        String body = """
            <todo>
                <title>%s</title>
                <doneStatus>false</doneStatus>
                <description>Created by XML unit test</description>
            </todo>
            """.formatted(title);

        Response response = given()
            .contentType(ContentType.XML)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(201, response.statusCode(),
            "Valid XML creation should return 201.");

        String location = response.getHeader("Location");
        assertNotNull(location);

        String id = location.substring(location.lastIndexOf('/') + 1);
        assertFalse(id.isBlank());
        createdTodoIds.add(id);

        Response retrieved = given()
            .accept(ContentType.JSON)
            .get("/todos/" + id);

        assertEquals(200, retrieved.statusCode());
        assertEquals(title, retrieved.jsonPath().getString("todos[0].title"));
        assertEquals("Created by XML unit test",
            retrieved.jsonPath().getString("todos[0].description"));
    }

    @Test
    void createTodoWithoutTitleReturnsBadRequest() {
        String body = """
            {
                "doneStatus": false,
                "description": "Missing required title"
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "A missing title should return 400.");
    }

    @Test
    void createTodoWithEmptyTitleReturnsBadRequest() {
        String body = """
            {
                "title": "",
                "doneStatus": false,
                "description": "Empty title"
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "An empty title should return 400.");
    }

    @Test
    void createTodoWithMalformedXmlReturnsBadRequest() {
        String body = """
            <todo>
                <title>Malformed XML test
                <doneStatus>false</doneStatus>
                <description>Missing closing title tag</description>
            </todo>
            """;

        Response response = given()
            .contentType(ContentType.XML)
            .accept(ContentType.JSON)
            .body(body)
            .post("/todos");

        assertEquals(400, response.statusCode(),
            "Malformed XML should return 400.");
    }
}
