package com.example.iie_szarzy_2024;

public class Task {
    private int id;
    private String description;
    private String status;
    private String priority;
    private String category;
    private String employeeName;
    private String startDate;
    private String deadline;
    private String creationDate;
    private String modificationDate;

    public Task(int id, String description, String status, String priority,String category, String employeeName,String startDate, String deadline, String creationDate, String modificationDate) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.category = category;
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.deadline = deadline;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public void setStartDate(String deadline) {
        this.startDate = startDate;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


