<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Lager"/>
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Lagerübersicht</h1>

<%-- Haupt-Schleife über die Standorte (z.B. 'Erdgeschoss', 'Obergeschoss') --%>
<c:forEach var="locationEntry" items="${storageData}">
    <div class="location-section">
        <h2>${locationEntry.key}</h2>
        <table class="styled-table">
            <thead>
                <tr>
                    <th>Gerät</th>
                    <th>Schrank</th>
                    <th>Regal</th>
                    <th>Fach</th>
                    <th>Anzahl</th>
                    <th>Bild</th>
                </tr>
            </thead>
            <tbody>
                <%-- Schleife über die Artikel am jeweiligen Standort --%>
                <c:forEach var="item" items="${locationEntry.value}">
                    <tr>
                        <td>${item.name}</td>
                        <td>${item.cabinet}</td>
                        <td>${item.shelf}</td>
                        <td>${item.compartment}</td>
                        <td>${item.quantity}</td>
                        <td>
                            <%-- Erstellt den klickbaren Link für die Lightbox --%>
                            <c:if test="${not empty item.imagePath}">
                                <a href="#img-${item.id}">
                                    <img src="${pageContext.request.contextPath}/image?file=${item.imagePath}" alt="${item.name}" width="50">
                                </a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <%-- =================================================================== --%>
    <%--                      DIES IST DER KORRIGIERTE TEIL                  --%>
    <%-- =================================================================== --%>
    <%-- Die Lightbox-Overlays werden jetzt HIER erstellt, NACH der Tabelle,
         aber immer noch INNERHALB der Schleife über die Standorte.
         Die innere Schleife ist dieselbe wie oben. Das ist sauber und korrekt. --%>
    <div class="lightbox-container">
        <c:forEach var="item" items="${locationEntry.value}">
            <c:if test="${not empty item.imagePath}">
                <a href="#_" class="lightbox" id="img-${item.id}">
                    <img src="${pageContext.request.contextPath}/image?file=${item.imagePath}" alt="Vergrößerte Ansicht von ${item.name}">
                </a>
            </c:if>
        </c:forEach>
    </div>
</c:forEach> <%-- Dies ist das schließende Tag für die äußere Schleife über die Standorte --%>


<%-- Die CSS-Regeln für die Lightbox sind in der globalen style.css korrekt aufgehoben. --%>
<style>
.location-section { margin-bottom: 2rem; }
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />