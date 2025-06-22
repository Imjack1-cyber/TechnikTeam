<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--
  login.jsp
  
  This is the main login page for the application. It provides a simple form
  for users to enter their username and password. The form is submitted to
  the LoginServlet for authentication.
  
  - It is served by: LoginServlet (doGet).
  - It can also be the welcome-file defined in web.xml.
  - Expected attributes:
    - 'errorMessage' (String): An error message to display if login fails (optional).
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Anmeldung" />
</c:import>

<div class="login-wrapper">
    <div class="login-box">
        <h1>Willkommen zurück</h1>

        <c:if test="${not empty errorMessage}">
            <p class="error-message">${errorMessage}</p>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group">
                <label for="username">Benutzername</label>
                <input type="text" id="username" name="username" required autocomplete="username" autofocus>
            </div>
            <div class="form-group">
                <label for="password">Passwort</label>
                <input type="password" id="password" name="password" required autocomplete="current-password">
            </div>
            <button type="submit" class="btn" style="width: 100%;">Anmelden</button>
        </form>
    </div>
</div>

<%-- The footer is omitted on the login page for a cleaner, focused look. --%>
</body>
</html>