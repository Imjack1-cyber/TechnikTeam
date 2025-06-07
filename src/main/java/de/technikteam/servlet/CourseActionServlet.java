package de.technikteam.servlet;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.User;

@WebServlet("/course-action")
public class CourseActionServlet extends HttpServlet {
	private CourseDAO courseDAO;

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User user = (User) req.getSession().getAttribute("user");
		String action = req.getParameter("action");
		int courseId = Integer.parseInt(req.getParameter("courseId"));

		if ("signup".equals(action)) {
			courseDAO.signUpForCourse(user.getId(), courseId);
		} // ... else if for signoff

		resp.sendRedirect(req.getContextPath() + "/lehrgaenge");
	}
}