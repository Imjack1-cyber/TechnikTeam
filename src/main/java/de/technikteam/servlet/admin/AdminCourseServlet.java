// Pfad: src/main/java/de/technikteam/servlet/admin/AdminCourseServlet.java
package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Course;

@WebServlet("/admin/courses")
public class AdminCourseServlet extends HttpServlet {
	private CourseDAO courseDAO;

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
		switch (action) {
		case "edit":
		case "new":
			showForm(req, resp);
			break;
		default:
			listCourses(req, resp);
			break;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		if ("delete".equals(action)) {
			handleDelete(req, resp);
		} else {
			handleCreateOrUpdate(req, resp);
		}
	}

	private void listCourses(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Course> courseList = courseDAO.getAllCourses();
		req.setAttribute("courseList", courseList);
		req.getRequestDispatcher("/admin/admin_course_list.jsp").forward(req, resp);
	}

	private void showForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if ("edit".equals(req.getParameter("action"))) {
			int courseId = Integer.parseInt(req.getParameter("id"));
			Course course = courseDAO.getCourseById(courseId);
			req.setAttribute("course", course);
		}
		req.getRequestDispatcher("/admin/admin_course_form.jsp").forward(req, resp);
	}

	private void handleCreateOrUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Course course = new Course();
		course.setName(req.getParameter("name"));
		course.setType(req.getParameter("type"));
		course.setLeader(req.getParameter("leader"));
		course.setDescription(req.getParameter("description"));
		try {
			course.setCourseDateTime(LocalDateTime.parse(req.getParameter("courseDateTime")));
		} catch (DateTimeParseException e) {
			/* Fehlerbehandlung */ }

		String idParam = req.getParameter("id");
		if (idParam != null && !idParam.isEmpty()) {
			course.setId(Integer.parseInt(idParam));
			courseDAO.updateCourse(course);
		} else {
			courseDAO.createCourse(course);
		}
		resp.sendRedirect(req.getContextPath() + "/admin/courses");
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int courseId = Integer.parseInt(req.getParameter("id"));
		courseDAO.deleteCourse(courseId);
		resp.sendRedirect(req.getContextPath() + "/admin/courses");
	}
}