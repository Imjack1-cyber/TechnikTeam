<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="400 - Fehlerhafte Anfrage" />
</c:import>

<div class="error-page-container">
	<h1 class="error-code" style="color: var(--info-color);">400</h1>
	<h2>Fehlerhafte Anfrage</h2>
	<p class="error-message-text">Der Server konnte die Anfrage
		aufgrund einer fehlerhaften Syntax nicht verstehen. Das
		Protokolldroiden-System analysiert die Übertragung.</p>

	<div class="protocol-droid-console">
		<div class="droid-eye"></div>
		<pre id="droid-output"></pre>
	</div>

	<div id="redirect-container"
		style="margin-top: 1.5rem; opacity: 0; transition: opacity 0.5s;">
		<a href="javascript:history.back()" class="btn btn-secondary"> <i
			class="fas fa-arrow-left"></i> Einen Schritt zurück
		</a>
	</div>
</div>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    const output = document.getElementById('droid-output');
    const redirectContainer = document.getElementById('redirect-container');

    const lines = [
        'INITIATING DATASTREAM ANALYSIS...',
        'PACKET RECEIVED. SIZE: ${pageContext.request.contentLength > 0 ? pageContext.request.contentLength : "UNKNOWN"} BYTES.',
        'PARSING HEADER... [OK]',
        'ANALYSING PAYLOAD...',
        '    > SCANNING FOR SYNTAX VIOLATIONS...',
        '    > ERROR! UNEXPECTED TOKEN OR MALFORMED PARAMETER DETECTED.',
        '    > CORRUPTION LEVEL: MODERATE.',
        'CONCLUSION: ANFRAGE NICHT VERARBEITBAR. DATENINTEGRITÄT KOMPROMITTIERT.',
        'EMPFEHLUNG: ZURÜCKKEHREN UND ANFRAGE NEU FORMULIEREN.',
        ''
    ];

    let lineIndex = 0;
    
    function printLine() {
        if (lineIndex < lines.length) {
            const p = document.createElement('p');
            p.textContent = lines[lineIndex];
            output.appendChild(p);
            output.scrollTop = output.scrollHeight;
            lineIndex++;
            setTimeout(printLine, Math.random() * 200 + 50);
        } else {
             redirectContainer.style.opacity = '1';
        }
    }

    printLine();
});
</script>