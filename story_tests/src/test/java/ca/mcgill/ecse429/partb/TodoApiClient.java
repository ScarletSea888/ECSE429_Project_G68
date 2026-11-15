package ca.mcgill.ecse429.partb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/** Small API adapter shared by all the story-test step definitions. */
final class TodoApiClient {
    private static final String BASE_URL = "http://localhost:4567";

    Response allTodos() {
        return given().baseUri(BASE_URL).accept(ContentType.JSON).get("/todos");
    }

    void checkAvailable() {
        try {
            Response response = allTodos();
            require(response.statusCode() == 200,
                "Todo API must be running and GET /todos must return 200, but got "
                    + response.statusCode());
        } catch (Exception error) {
            throw new AssertionError(
                "Todo API not reachable. Start runTodoManagerRestAPI-1.5.5.jar first.", error);
        }
    }

    Response getTodo(String id) {
        return given().baseUri(BASE_URL).accept(ContentType.JSON).get("/todos/" + id);
    }

    Response findByTitle(String title) {
        return given().baseUri(BASE_URL).accept(ContentType.JSON)
            .queryParam("title", title).get("/todos");
    }

    Response createJson(String title, boolean done, String description) {
        return given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(todoPayload(title, done, description))
            .post("/todos");
    }

    Response createXml(String title, boolean done, String description) {
        String xml = "<todo><title>" + escapeXml(title) + "</title>"
            + "<doneStatus>" + done + "</doneStatus>"
            + "<description>" + escapeXml(description) + "</description></todo>";
        return given().baseUri(BASE_URL).contentType(ContentType.XML)
            .accept(ContentType.JSON).body(xml).post("/todos");
    }

    Response createInvalid(String invalidType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("doneStatus", false);
        switch (invalidType) {
            case "missing-title":
                break;
            case "empty-title":
                data.put("title", "");
                break;
            case "string-status":
                data.put("title", "An invalid task");
                data.put("doneStatus", "false");
                break;
            default:
                throw new IllegalArgumentException("Unknown invalid input: " + invalidType);
        }
        return given().baseUri(BASE_URL).contentType(ContentType.JSON)
            .accept(ContentType.JSON).body(data).post("/todos");
    }

    Response update(String id, String method, Object body) {
        var request = given().baseUri(BASE_URL)
            .contentType(ContentType.JSON).accept(ContentType.JSON).body(body);
        switch (method) {
            case "POST": return request.post("/todos/" + id);
            case "PUT": return request.put("/todos/" + id);
            default: throw new IllegalArgumentException("Unsupported update method: " + method);
        }
    }

    Response delete(String id) {
        return given().baseUri(BASE_URL).accept(ContentType.JSON)
            .delete("/todos/" + id);
    }

    JsonObject firstTodo(Response response) {
        require(response.statusCode() == 200,
            "Expected a readable Todo, got HTTP " + response.statusCode());
        JsonArray todos = JsonParser.parseString(response.asString())
            .getAsJsonObject().getAsJsonArray("todos");
        require(todos != null && !todos.isEmpty(), "Response should contain a Todo");
        return todos.get(0).getAsJsonObject();
    }

    Map<String, JsonElement> snapshot() {
        Response response = allTodos();
        require(response.statusCode() == 200,
            "Cannot snapshot API collection: HTTP " + response.statusCode());
        JsonArray todos = JsonParser.parseString(response.asString())
            .getAsJsonObject().getAsJsonArray("todos");
        Map<String, JsonElement> byId = new LinkedHashMap<>();
        for (JsonElement todo : todos) {
            JsonObject object = todo.getAsJsonObject();
            byId.put(object.get("id").getAsString(), object.deepCopy());
        }
        return byId;
    }

    static Map<String, Object> todoPayload(String title, boolean done, String description) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", title);
        data.put("doneStatus", done);
        data.put("description", description);
        return data;
    }

    static String idFromCreation(Response response) {
        require(response.statusCode() == 201,
            "Expected creation HTTP 201; got " + response.statusCode()
                + ": " + response.asString());
        String location = response.getHeader("Location");
        require(location != null && !location.isBlank(),
            "Creation response must include Location header");
        String id = location.substring(location.lastIndexOf('/') + 1);
        require(!id.isBlank(), "Location header contains no Todo ID");
        return id;
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static String escapeXml(String input) {
        return input.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;")
            .replace("'", "&apos;");
    }
}
