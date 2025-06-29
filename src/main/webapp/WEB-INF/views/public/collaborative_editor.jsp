<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  FIX: This page now imports the standard header and footer to be
  fully integrated into the application's UI.
--%>

<c:import url="../../jspf/header.jspf">
	<c:param name="pageTitle" value="Gemeinsamer Editor" />
	<c:param name="navType" value="user" />
</c:import>

<h1>
	<i class="fas fa-edit"></i> Gemeinsamer Notizblock
</h1>
<p>Änderungen werden automatisch für alle Benutzer gespeichert und
	angezeigt.</p>

<div class="card">
	<textarea id="editor" class="form-group"
		style="width: 100%; height: 60vh; font-family: monospace; font-size: 16px; margin: 0; background-color: var(--surface-color);"
		placeholder="Lade Inhalt..."></textarea>
	<div id="status-indicator"
		style="text-align: right; font-style: italic; color: var(--text-muted-color); padding-top: 5px; min-height: 1.2em;"></div>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const editor = document.getElementById('editor');
    const statusIndicator = document.getElementById('status-indicator');
    const apiUrl = "${pageContext.request.contextPath}/api/document";
    let debounceTimer;

    const saveContent = () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            const content = editor.value;
            statusIndicator.textContent = 'Speichere...';
            fetch(apiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'text/plain; charset=utf-8' },
                body: content
            }).then(response => {
                if (response.ok) {
                    statusIndicator.textContent = 'Gespeichert';
                } else {
                    statusIndicator.textContent = 'Fehler beim Speichern!';
                    statusIndicator.style.color = 'var(--danger-color)';
                }
            }).catch(err => {
                 statusIndicator.textContent = 'Netzwerkfehler!';
                 statusIndicator.style.color = 'var(--danger-color)';
            });
        }, 500);
    };

    const fetchContent = () => {
        fetch(apiUrl)
            .then(response => {
                if (!response.ok) throw new Error("Server response not OK");
                return response.text();
            })
            .then(newContent => {
                if (document.activeElement !== editor && editor.value !== newContent) {
                    const cursorPos = editor.selectionStart;
                    editor.value = newContent;
                    editor.selectionStart = editor.selectionEnd = cursorPos;
                }
            }).catch(err => {
                console.error("Error fetching document content:", err);
                statusIndicator.textContent = 'Fehler beim Laden!';
                statusIndicator.style.color = 'var(--danger-color)';
            });
    };
    
    editor.addEventListener('input', saveContent);

    const pollInterval = setInterval(fetchContent, 3000);
    fetchContent(); 
    
    window.addEventListener('beforeunload', () => {
        clearInterval(pollInterval);
    });
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />