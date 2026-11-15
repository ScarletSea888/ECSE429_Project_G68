package ca.mcgill.ecse429.partb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Reusable steps for all five stories. Cucumber creates one instance per scenario. */
public class TodoSteps {
    private final TodoApiClient client = new TodoApiClient();
    private final Set<String> createdIds = new LinkedHashSet<>();
    private Map<String, JsonElement> originalCollection;
    private Map<String, JsonElement> beforeAction;
    private Response response;
    private String currentTodoId;
    private boolean ready;

    @Before
    public void startScenario() {
        client.checkAvailable();           // Fails, rather than skips, if server is offline.
        originalCollection = client.snapshot();
        ready = true;
    }

    @After
    public void restoreCollection() {
        if (!ready) return;
        try {
            for (String id : createdIds) {
                int status = client.delete(id).statusCode();
                TodoApiClient.require(status == 200 || status == 404,
                    "Cleanup failed to delete temporary Todo " + id + " (HTTP " + status + ")");
            }
        } finally {
            // Check both the number and complete JSON contents of original objects.
            TodoApiClient.require(originalCollection.equals(client.snapshot()),
                "Cleanup did not restore the original Todo collection");
        }
    }

    @Given("the Todo API is available")
    public void theTodoApiIsAvailable() {
        client.checkAvailable();
    }

    @Given("a temporary todo exists with title {string} status {string} description {string}")
    public void aTemporaryTodoExists(String title, String status, String description) {
        Response created = client.createJson(title, Boolean.parseBoolean(status), description);
        registerCreatedTodo(created);
    }

    @When("I create a todo using {string} with title {string} status {string} description {string}")
    public void createTodo(String format, String title, String status, String description) {
        beforeAction = client.snapshot();
        response = switch (format) {
            case "JSON" -> client.createJson(title, Boolean.parseBoolean(status), description);
            case "XML" -> client.createXml(title, Boolean.parseBoolean(status), description);
            default -> throw new IllegalArgumentException("Format must be JSON or XML");
        };
        if (response.statusCode() == 201) registerCreatedTodo(response);
    }

    @When("I attempt to create a todo with invalid input {string}")
    public void invalidCreation(String invalidType) {
        beforeAction = client.snapshot();
        response = client.createInvalid(invalidType);
    }

    @When("I set the todo status to {string} using {string}")
    public void setStatus(String status, String method) {
        JsonObject todo = client.firstTodo(client.getTodo(currentTodoId));
        response = client.update(currentTodoId, method,
            TodoApiClient.todoPayload(todo.get("title").getAsString(),
                Boolean.parseBoolean(status), todo.get("description").getAsString()));
    }

    @When("I try to set the todo status to invalid string {string}")
    public void invalidStatusUpdate(String invalidValue) {
        JsonObject todo = client.firstTodo(client.getTodo(currentTodoId));
        beforeAction = client.snapshot();
        response = client.update(currentTodoId, "POST",
            Map.of("title", todo.get("title").getAsString(),
                "doneStatus", invalidValue,
                "description", todo.get("description").getAsString()));
    }

    @When("I change the todo title to {string} and description to {string} using {string}")
    public void editTodo(String title, String description, String method) {
        JsonObject todo = client.firstTodo(client.getTodo(currentTodoId));
        boolean done = Boolean.parseBoolean(todo.get("doneStatus").getAsString());
        response = client.update(currentTodoId, method,
            TodoApiClient.todoPayload(title, done, description));
    }

    @When("I change nonexistent todo {string} using {string}")
    public void editMissingTodo(String id, String method) {
        TodoApiClient.require(client.getTodo(id).statusCode() == 404,
            "The test's nonexistent ID unexpectedly exists: " + id);
        beforeAction = client.snapshot();
        response = client.update(id, method,
            TodoApiClient.todoPayload("An update to a missing task", true, "No effect"));
    }

    @When("I retrieve the current todo by ID")
    public void retrieveCurrentTodo() {
        response = client.getTodo(currentTodoId);
    }

    @When("I search todos with exact title {string}")
    public void filterTodos(String title) {
        response = client.findByTitle(title);
    }

    @When("I retrieve nonexistent todo {string}")
    public void retrieveMissingTodo(String id) {
        beforeAction = client.snapshot();
        response = client.getTodo(id);
    }

    @When("I delete the current todo")
    public void deleteCurrentTodo() {
        response = client.delete(currentTodoId);
    }

    @When("I mark the current todo completed and then delete it")
    public void finishAndDelete() {
        JsonObject todo = client.firstTodo(client.getTodo(currentTodoId));
        Response completed = client.update(currentTodoId, "POST",
            TodoApiClient.todoPayload(todo.get("title").getAsString(), true,
                todo.get("description").getAsString()));
        TodoApiClient.require(completed.statusCode() == 200,
            "Marking task complete should return HTTP 200");
        JsonObject refreshed = client.firstTodo(client.getTodo(currentTodoId));
        TodoApiClient.require("true".equals(refreshed.get("doneStatus").getAsString()),
            "Task should be completed before deletion");
        response = client.delete(currentTodoId);
    }

    @When("I delete nonexistent todo {string}")
    public void deleteMissingTodo(String id) {
        beforeAction = client.snapshot();
        response = client.delete(id);
    }

    @Then("the HTTP response status is {int}")
    public void assertHttpStatus(int expectedStatus) {
        TodoApiClient.require(response != null, "No request response was recorded");
        TodoApiClient.require(response.statusCode() == expectedStatus,
            "Expected HTTP " + expectedStatus + " but got " + response.statusCode()
                + ": " + response.asString());
    }

    @Then("the created todo contains title {string} status {string} description {string}")
    public void assertCreatedFields(String title, String status, String description) {
        assertTodoFields(client.firstTodo(client.getTodo(currentTodoId)),
            title, status, description);
    }

    @Then("the current todo has status {string}")
    public void assertCurrentStatus(String status) {
        JsonObject todo = client.firstTodo(client.getTodo(currentTodoId));
        TodoApiClient.require(status.equals(todo.get("doneStatus").getAsString()),
            "Expected status " + status + ", got " + todo.get("doneStatus"));
    }

    @Then("the current todo has title {string} and description {string}")
    public void assertEditedFields(String title, String description) {
        JsonObject todo = client.firstTodo(client.getTodo(currentTodoId));
        TodoApiClient.require(title.equals(todo.get("title").getAsString()),
            "Updated Todo title does not match");
        TodoApiClient.require(description.equals(todo.get("description").getAsString()),
            "Updated Todo description does not match");
    }

    @Then("the retrieved todo has title {string} and status {string}")
    public void assertRetrievedTodo(String title, String status) {
        JsonObject todo = client.firstTodo(response);
        TodoApiClient.require(title.equals(todo.get("title").getAsString()),
            "Retrieved Todo title does not match");
        TodoApiClient.require(status.equals(todo.get("doneStatus").getAsString()),
            "Retrieved Todo status does not match");
    }

    @Then("the search results include a todo titled {string}")
    public void assertSearchContains(String title) {
        JsonArray list = JsonParser.parseString(response.asString())
            .getAsJsonObject().getAsJsonArray("todos");
        boolean found = false;
        for (JsonElement item : list) {
            if (title.equals(item.getAsJsonObject().get("title").getAsString())) {
                found = true;
                break;
            }
        }
        TodoApiClient.require(found, "Expected Todo not present in search results: " + title);
    }

    @Then("the current todo no longer exists")
    public void assertDeleted() {
        TodoApiClient.require(client.getTodo(currentTodoId).statusCode() == 404,
            "Deleted Todo should not be retrievable");
    }

    @Then("the API collection is unchanged by the failed operation")
    public void assertNoSideEffects() {
        TodoApiClient.require(beforeAction != null && beforeAction.equals(client.snapshot()),
            "A rejected request unexpectedly changed the Todo collection");
    }

    @Then("the error response contains a meaningful message")
    public void assertErrorMessage() {
        JsonObject body = JsonParser.parseString(response.asString()).getAsJsonObject();
        JsonArray messages = body.getAsJsonArray("errorMessages");
        TodoApiClient.require(messages != null && !messages.isEmpty()
            && !messages.get(0).getAsString().isBlank(),
            "Error response must include a nonempty errorMessages value");
    }

    private void registerCreatedTodo(Response created) {
        currentTodoId = TodoApiClient.idFromCreation(created);
        createdIds.add(currentTodoId);
    }

    private static void assertTodoFields(JsonObject todo, String title,
            String status, String description) {
        TodoApiClient.require(title.equals(todo.get("title").getAsString()),
            "Created title does not match");
        TodoApiClient.require(status.equals(todo.get("doneStatus").getAsString()),
            "Created status does not match");
        TodoApiClient.require(description.equals(todo.get("description").getAsString()),
            "Created description does not match");
    }
}
