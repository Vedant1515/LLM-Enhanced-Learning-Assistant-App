package com.learningassistant.app.models;

import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private String topic;
    private boolean completed;
    private long createdAt;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.completed = false;
    }

    public Task(String id, String title, String description, String topic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.topic = topic;
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
    }

    public static Task createDummy(String topic) {
        Task task = new Task();
        task.setTopic(topic);
        task.setTitle("Master " + topic + ": Core Concepts");
        task.setDescription("AI has generated a quiz to test your understanding of the fundamental principles of " + topic + ".");
        return task;
    }

    public static Task createAdvanced(String topic) {
        Task task = new Task();
        task.setTopic(topic);
        task.setTitle("Advanced " + topic + " Challenges");
        task.setDescription("Challenge yourself with intermediate-level questions on " + topic + " to strengthen your skills.");
        return task;
    }

    public static Task createPractice(String topic) {
        Task task = new Task();
        task.setTopic(topic);
        task.setTitle("Practice " + topic + " Problems");
        task.setDescription("Reinforce your knowledge of " + topic + " through targeted practice problems and quizzes.");
        return task;
    }

    public static Task createReview(String topic) {
        Task task = new Task();
        task.setTopic(topic);
        task.setTitle("Review " + topic + " Fundamentals");
        task.setDescription("Revisit the core fundamentals of " + topic + " with this AI-curated quiz session.");
        return task;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
