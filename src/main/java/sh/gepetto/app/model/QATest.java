package sh.gepetto.app.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a test written in natural language
 */
public class QATest {
    private String name;
    private String description;
    private List<String> tags;
    private String author;
    private LocalDateTime created;
    private List<String> steps;

    public QATest() {
        this.tags = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    public QATest(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public void addStep(String step) {
        this.steps.add(step);
    }

    @Override
    public String toString() {
        return "Test{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", author='" + author + '\'' +
                ", created=" + created +
                ", steps=" + steps +
                '}';
    }
}