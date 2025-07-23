<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Feedback geben" />
</c:import>

<div style="max-width: 800px; margin: auto;">
	<div class="card">
		<h1>
			<i class="fas fa-lightbulb"></i> Feedback & Wünsche
		</h1>
		<p>Hast du eine Idee für eine neue Funktion, einen
			Verbesserungsvorschlag oder ist dir ein Fehler aufgefallen? Teile es
			uns hier mit! Dein Feedback hilft uns, diese Anwendung besser zu
			machen.</p>

		<c:import url="/WEB-INF/jspf/message_banner.jspf" />

		<form action="${pageContext.request.contextPath}/feedback"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value="submitGeneralFeedback">

			<div class="form-group">
				<label for="subject">Betreff</label> <input type="text" id="subject"
					name="subject" required maxlength="255"
					placeholder="z.B. Feature-Wunsch: Dunkelmodus für die Packliste">
			</div>

			<div class="form-group">
				<label for="content">Deine Nachricht</label>
				<textarea id="content" name="content" rows="8" required
					placeholder="Bitte beschreibe deine Idee oder das Problem so detailliert wie möglich."></textarea>
			</div>

			<button type="submit" class="btn btn-success">
				<i class="fas fa-paper-plane"></i> Feedback absenden
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />