<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="401 - Nicht autorisiert" />
</c:import>

<div class="error-page-container">
	<h1 class="error-code" style="color: var(--danger-color);">401</h1>
	<h2>Nicht Autorisiert</h2>
	<p class="error-message-text">
		Für den Zugriff auf diese Ressource ist eine Authentifizierung erforderlich. Ihre Anmeldeinformationen fehlen oder sind ungültig.
	</p>

	<div class="card-scanner">
        <div class="scanner-light" id="scanner-light"></div>
        <div class="card-slot">
            <div class="card-content">
                <p>STATUS:</p>
                <h3 id="scanner-status">BEREIT</h3>
                <div class="progress-bar-container" style="height:10px; margin-top: 1rem;">
			        <div id="scanner-progress" class="progress-bar"></div>
		        </div>
            </div>
        </div>
    </div>

	<div id="redirect-container" style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
		<a href="${pageContext.request.contextPath}/WEB-INF/views/auth//WEB-INF/views/auth/login.jsp" class="btn btn-success">
			<i class="fas fa-sign-in-alt"></i> Zur Anmeldeseite
		</a>
	</div>
</div>

<style>
/* Add these styles to your main CSS or keep them here */
.card-scanner {
    width: 100%;
    max-width: 350px;
    background: #343a40;
    padding: 1.5rem;
    border-radius: 10px;
    box-shadow: inset 0 0 15px #000;
}
.scanner-light {
    height: 10px;
    background: #28a745;
    border-radius: 5px;
    margin-bottom: 1.5rem;
    box-shadow: 0 0 10px #28a745;
}
.card-slot {
    background: #161b22;
    border: 2px solid #495057;
    border-radius: 8px;
    padding: 1.5rem;
    color: #fff;
}
#scanner-status {
    color: #ffc107;
    font-size: 2rem;
    letter-spacing: 2px;
    margin: 0;
}
</style>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    const statusText = document.getElementById('scanner-status');
    const light = document.getElementById('scanner-light');
    const progress = document.getElementById('scanner-progress');
    const redirectContainer = document.getElementById('redirect-container');

    const steps = [
        { text: 'SCANNE...', color: '#ffc107', lightColor: '#ffc107', duration: 1500, progress: 50 },
        { text: 'KEINE ID', color: '#dc3545', lightColor: '#dc3545', duration: 1000, progress: 80 },
        { text: 'ZUGRIFF?', color: '#dc3545', lightColor: '#dc3545', duration: 800, progress: 100 },
        { text: 'VERWEIGERT', color: '#dc3545', lightColor: '#dc3545', duration: 2000, progress: 100 }
    ];

    let currentStep = 0;

    function runScan() {
        if(currentStep >= steps.length) {
            redirectContainer.style.opacity = '1';
            return;
        }

        const step = steps[currentStep];
        statusText.textContent = step.text;
        statusText.style.color = step.color;
        light.style.background = step.lightColor;
        light.style.boxShadow = `0 0 10px ${step.lightColor}`;
        progress.style.width = `${step.progress}%`;
        progress.style.backgroundColor = step.lightColor;

        currentStep++;
        setTimeout(runScan, step.duration);
    }

    setTimeout(runScan, 500); // Initial delay
});
</script>