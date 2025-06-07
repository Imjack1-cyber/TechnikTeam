<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- 
  This page is displayed for 500 Internal Server errors.
  It provides a user-friendly message and includes a hidden section with
  the stack trace for easy debugging during development.
  The 'isErrorPage="true"' attribute is crucial to make the 'exception' object available.
--%>
<!DOCTYPE html>
<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Interner Serverfehler"/>
</c:import>

<main>
    <div class="error-container">
        <h1>Fehler 500 - Interner Serverfehler</h1>
        <p>
            Es tut uns leid, aber auf dem Server ist ein unerwarteter Fehler aufgetreten.
        </p>
        <p>
            Unser Technik-Team wurde automatisch informiert und arbeitet an einer Lösung.
            Bitte versuchen Sie es später erneut oder kehren Sie zur Startseite zurück.
        </p>
        <a href="${pageContext.request.contextPath}/home" class="btn">Zur Startseite</a>
    </div>
</main>

<%-- 
  =============================================================================
  ==                DEVELOPER DEBUGGING INFORMATION (HIDDEN)                 ==
  =============================================================================
  This JSP comment block is processed on the server and is NEVER sent to the
  user's browser. It's a secure way to log the stack trace for developers
  to see when viewing the page source of the rendered error page is not possible.
  The actual stack trace will be visible in the Tomcat logs (catalina.out).
--%>
<%--
  Stack Trace:
  <%
    if (exception != null) {
      // This prints the stack trace to the server's log file, which is the best practice.
      // The exception is automatically logged by Tomcat, but this is an explicit way.
      exception.printStackTrace(new java.io.PrintWriter(out));
    }
  %>
--%>


<style>
    /* We can reuse the same styles from the 404 page */
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