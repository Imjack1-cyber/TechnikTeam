<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  collaborative_editor.jsp
  
  This page provides a real-time collaborative text editor. The core
  functionality is handled by client-side JavaScript which communicates
  with the DocumentApiServlet. It periodically fetches the latest content
  and saves changes back to the server after the user stops typing.
  
  - It is served by: CollaborativeEditorServlet.
  - Expected attributes: None.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Gemeinsamer Editor" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Gemeinsamer Notizblock</h1>
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

    // --- Function to SAVE content to the server ---
    // Uses a "debouncer" to avoid spamming the server with requests on every keystroke.
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
        }, 500); // Wait 500ms after last keystroke
    };

    // --- Function to FETCH content from the server ---
    const fetchContent = () => {
        fetch(apiUrl)
            .then(response => {
                if (!response.ok) throw new Error("Server response not OK");
                return response.text();
            })
            .then(newContent => {
                // IMPORTANT: Only update the editor if the content has actually changed
                // and the user is not currently typing in it. This prevents the cursor from jumping.
                if (document.activeElement !== editor && editor.value !== newContent) {
                    const cursorPos = editor.selectionStart; // Save cursor position
                    editor.value = newContent;
                    editor.selectionStart = editor.selectionEnd = cursorPos; // Restore cursor position
                }
            }).catch(err => {
                console.error("Error fetching document content:", err);
                statusIndicator.textContent = 'Fehler beim Laden!';
                statusIndicator.style.color = 'var(--danger-color)';
            });
    };
    
    // Attach the event listener to save content when the user types
    editor.addEventListener('input', saveContent);

    // Start polling: Fetch the content every 3 seconds
    setInterval(fetchContent, 3000);

    // Initial load of the document
    fetchContent();
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />