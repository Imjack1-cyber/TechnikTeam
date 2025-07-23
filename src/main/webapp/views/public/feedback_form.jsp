<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Feedback geben" />
</c:import>

<div style="max-width: 700px; margin: auto;">
	<div class="card">
		<h1>
			Feedback für:
			<c:out value="${event.name}" />
		</h1>
		<p>Dein Feedback hilft uns, zukünftige Events zu verbessern.</p>

		<form action="${pageContext.request.contextPath}/feedback"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="action" value="submitEventFeedbackResponse"> <input
				type="hidden" name="formId" value="${form.id}">

			<div class="form-group">
				<label>Gesamteindruck (1 = schlecht, 5 = super)</label>
				<div class="star-rating">
					<input type="radio" id="star5" name="rating" value="5" required /><label
						for="star5" title="5 Sterne"></label> <input type="radio"
						id="star4" name="rating" value="4" /><label for="star4"
						title="4 Sterne"></label> <input type="radio" id="star3"
						name="rating" value="3" /><label for="star3" title="3 Sterne"></label>
					<input type="radio" id="star2" name="rating" value="2" /><label
						for="star2" title="2 Sterne"></label> <input type="radio"
						id="star1" name="rating" value="1" /><label for="star1"
						title="1 Stern"></label>
				</div>
			</div>

			<div class="form-group">
				<label for="comments">Kommentare & Verbesserungsvorschläge</label>
				<textarea id="comments" name="comments" rows="5"></textarea>
			</div>

			<button type="submit" class="btn">Feedback absenden</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />