<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- 
  This page is displayed for 404 Not Found errors.
  It informs the user that the requested page does not exist and provides a clear way back.
--%>
<!DOCTYPE html>
<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Seite nicht gefunden"/>
</c:import>

<%-- We don't include the main navigation here, as a 404 can be hit by unauthenticated users. --%>

<main>
    <div class="error-container">
        <h1>Fehler 404 - Seite nicht gefunden</h1>
        <p>
            Ups! Die von Ihnen angeforderte URL konnte auf diesem Server nicht gefunden werden.
            Möglicherweise haben Sie sich vertippt oder der Link ist veraltet.
        </p>
        <p>
            Bitte überprüfen Sie die Adresse oder kehren Sie zur Startseite zurück.
        </p>
        <a href="${pageContext.request.contextPath}/home" class="btn">Zur Startseite</a>
    </div>
</main>

<style>
    /* Specific styles for the error page content */
    .error-container {
        text-align: center;
        padding: 4rem 1rem;
        max-width: 600px;
        margin: 2rem auto;
        background-color: var(--card-bg);
        border-radius: 8px;
        border: 1px solid var(--border-color);
    }
    .error-container h1 {
        font-size: 2.5rem;
        color: var(--danger-color);
    }
    .error-container p {
        margin-bottom: 1.5rem;
        font-size: 1.1rem;
    }
    .error-container .btn {
        font-size: 1.2rem;
        padding: 0.8rem 2rem;
    }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />