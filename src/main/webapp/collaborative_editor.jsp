<%-- Path: src/main/webapp/collaborative_editor.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<c:import url="/WEB-INF/jspf/header.jspf">
    <c:param name="title" value="Gemeinsamer Editor"/>
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Gemeinsamer Notizblock</h1>
<p>Änderungen werden automatisch für alle Benutzer gespeichert und angezeigt.</p>

<div class="card">
    <textarea id="editor" style="width: 100%; height: 60vh; font-family: monospace; font-size: 16px;" placeholder="Lade Inhalt..."></textarea>
    <div id="status-indicator" style="text-align: right; font-style: italic; color: #888; padding-top: 5px;"></div>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const editor = document.getElementById('editor');
    const statusIndicator = document.getElementById('status-indicator');
    const apiUrl = "${pageContext.request.contextPath}/api/document";
    let lastKnownContent = "";

    // --- Function to SAVE content to server ---
    // We use a "debouncer" to avoid spamming the server with requests on every keystroke.
    // It waits until the user has stopped typing for 300ms before sending the update.
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
                if(response.ok) {
                    statusIndicator.textContent = 'Gespeichert';
                } else {
                    statusIndicator.textContent = 'Fehler beim Speichern!';
                }
            });
        }, 500); // Wait 500ms after last keystroke
    };

    // --- Function to FETCH content from server ---
    const fetchContent = () => {
        fetch(apiUrl)
            .then(response => response.text())
            .then(newContent => {
                // IMPORTANT: Only update the editor if the content has actually changed.
                // This prevents the user's cursor from jumping while they are typing.
                if (document.activeElement !== editor && editor.value !== newContent) {
                    editor.value = newContent;
                }
                lastKnownContent = newContent;
            });
    };
    
    // Add the event listener to save content when the user types
    editor.addEventListener('input', saveContent);

    // Start the polling: Fetch the content every 3 seconds
    setInterval(fetchContent, 3000);

    // Initial load of the document
    fetchContent();
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />