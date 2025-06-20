<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8"><title>Login - Technik Team</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <div class="login-container">
        <h1>Anmeldung</h1>
        <c:if test="${not empty errorMessage}"><p class="error-message">${errorMessage}</p></c:if>
        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group"><label for="username">Benutzername</label><input type="text" id="username" name="username" required></div>
            <div class="form-group"><label for="password">Passwort</label><input type="password" id="password" name="password" required></div>
            <button type="submit" class="btn">Anmelden</button>
        </form>
    </div>
    <script src="js/main.js"></script>
</body>
</html>