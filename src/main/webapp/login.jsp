<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%-- HEADER MUST BE FIRST --%>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Anmeldung"/></c:import>

<div class="login-wrapper">
    <div class="login-box">
        <h1>Willkommen zurück</h1>
        
        <c:if test="${not empty errorMessage}">
            <p class="error-message" style="background-color: var(--danger-color); color: white; padding: 1rem; border-radius: 8px;">${errorMessage}</p>
        </c:if>
        
        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="form-group">
                <label for="username">Benutzername</label>
                <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
                <label for="password">Passwort</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit" class="btn" style="width: 100%;">Anmelden</button>
        </form>
    </div>
</div>

<%-- FOOTER MUST BE LAST --%>
<c:import url="/WEB-INF/jspf/footer.jspf"/>