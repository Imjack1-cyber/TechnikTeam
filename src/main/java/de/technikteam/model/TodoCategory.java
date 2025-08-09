package de.technikteam.model;

import java.util.ArrayList;
import java.util.List;

public class TodoCategory {
	private int id;
	private String name;
	private int displayOrder;
	private List<TodoTask> tasks = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public List<TodoTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<TodoTask> tasks) {
		this.tasks = tasks;
	}
}