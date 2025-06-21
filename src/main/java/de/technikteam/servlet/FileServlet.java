package de.technikteam.servlet; 

import java.io.IOException;
import java.util.ArrayList; // <-- Add this import
import java.util.List;
import java.util.Map;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.File; // Your model class
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dateien")
public class FileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		// 1. Fetch the real files from the database as before
		Map<String, List<File>> fileData = fileDAO.getAllFilesGroupedByCategory();

		// 2. Create our "virtual" collaborative file object
		File collaborativeFile = new File();
		collaborativeFile.setId(-1); // Use a special ID to identify it
		collaborativeFile.setFilename("Gemeinsamer Notizblock (Live-Editor)");
		// No filepath is needed as it's not a download

		// 3. Add it to a specific category (e.g., "Allgemein")
		String virtualCategoryName = "Allgemeine Dokumente";
		
		// Get the list for the category, or create a new list if it doesn't exist
		List<File> generalFiles = fileData.computeIfAbsent(virtualCategoryName, k -> new ArrayList<>());
		
		// Add our new file to the beginning of that list
		generalFiles.add(0, collaborativeFile);
		
		// 4. Send the modified map to the JSP
		request.setAttribute("fileData", fileData);
		request.getRequestDispatcher("/dateien.jsp").forward(request, response);
	}
}