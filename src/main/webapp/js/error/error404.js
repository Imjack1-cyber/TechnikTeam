document.addEventListener('DOMContentLoaded', () => {
    const terminalBody = document.getElementById('terminal-body');
    const homeLinkContainer = document.getElementById('home-link-container');
    const requestUri = "${fn:escapeXml(pageContext.errorData.requestURI)}";

    async function type(text, element, delay = 50) {
        for (const char of text) {
            element.textContent += char;
            terminalBody.scrollTop = terminalBody.scrollHeight;
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }
    
    async function addLine(text, className, delay = 20) {
        const p = document.createElement('p');
        if (className) p.className = className;
        terminalBody.appendChild(p);
        await type(text, p, delay);
    }

    async function runSequence() {
        const p1 = document.createElement('p');
        terminalBody.appendChild(p1);

        const prompt1 = document.createElement('span');
        prompt1.className = 'prompt';
        await type('user@technik-team:~$ ', prompt1, 20);
        p1.appendChild(prompt1);

        const command1 = document.createElement('span');
        command1.className = 'command';
        await type('ls -l ' + requestUri, command1, 50);
        p1.appendChild(command1);

        await new Promise(resolve => setTimeout(resolve, 500));

        await addLine('ls: cannot access \'' + requestUri + '\': No such file or directory', 'error', 15);
        await new Promise(resolve => setTimeout(resolve, 800));

        await addLine('Tipp: Kehren Sie mit dem folgenden Befehl zur Startseite zurück:', 'info', 25);
        await new Promise(resolve => setTimeout(resolve, 300));
        
        const p_link = document.createElement('p');
        terminalBody.appendChild(p_link);
        
        const prompt2 = document.createElement('span');
        prompt2.className = 'prompt';
        await type('user@technik-team:~$ ', prompt2, 20);
        p_link.appendChild(prompt2);

        const homeLink = document.createElement('a');
        homeLink.href = "${pageContext.request.contextPath}/home";
        homeLink.className = 'link';
        p_link.appendChild(homeLink);
        await type('cd /home', homeLink, 80);
        
        const cursor = document.createElement('span');
        cursor.className = 'cursor';
        cursor.innerHTML = ' ';
        p_link.appendChild(cursor);

        homeLinkContainer.style.opacity = '1';
    }

    runSequence();
});