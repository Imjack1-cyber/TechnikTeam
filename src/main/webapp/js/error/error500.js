document.addEventListener('DOMContentLoaded', () => {
    const diagBtn = document.getElementById('diagnostic-btn');
    const output = document.getElementById('diagnostic-output');

    const steps = [
        { text: 'Initialisiere Diagnose-Protokoll...', type: 'info', icon: 'fas fa-cogs' },
        { text: 'Prüfe Speicher-Integrität...', type: 'info', icon: 'fas fa-memory' },
        { text: '[OK] RAM Module antworten.', type: 'ok', icon: 'fas fa-check-circle' },
        { text: 'Verbinde mit Kernel...', type: 'info', icon: 'fas fa-project-diagram' },
        { text: '[WARN] Kernel antwortet langsam. Kaffee-Level kritisch.', type: 'warn', icon: 'fas fa-exclamation-triangle' },
        { text: 'Lade KI-Logikmodule...', type: 'info', icon: 'fas fa-brain' },
        { text: '[FAIL] Logikmodul "Hamster im Laufrad" hat die Arbeit niedergelegt.', type: 'fail', icon: 'fas fa-times-circle' },
        { text: 'Versuche Workaround: Mehr Glitzer-Effekte laden...', type: 'warn', icon: 'fas fa-magic' },
        { text: '[FAIL] Glitzer-Puffer übergelaufen.', type: 'fail', icon: 'fas fa-times-circle' },
        { text: '--------------------------------', type: 'info', icon: '' },
        { text: 'Diagnose abgeschlossen. Fehler gefunden.', type: 'fail', icon: 'fas fa-skull-crossbones' }
    ];

    let isRunning = false;
    
    // Function to simulate typing text into an element
    async function typeText(element, text, delay = 20) {
        for (let i = 0; i < text.length; i++) {
            element.innerHTML += text.charAt(i);
            output.scrollTop = output.scrollHeight;
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }

    diagBtn.addEventListener('click', async () => {
        if (isRunning) return;
        
        isRunning = true;
        diagBtn.disabled = true;
        diagBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Diagnose läuft...';
        output.innerHTML = ''; // Clear previous output

        for (const step of steps) {
            const line = document.createElement('p');
            line.className = step.type;
            const iconHtml = step.icon ? `<i class="${step.icon}" style="margin-right: 8px;"></i>` : '';
            line.innerHTML = `${iconHtml}`;
            output.appendChild(line);
            
            // Type out the text for the current line
            await typeText(line, step.text);

            await new Promise(resolve => setTimeout(resolve, 300)); // Pause between lines
        }
        
        // Add final cursor
        const finalLine = document.createElement('p');
        finalLine.innerHTML = '> <span class="cursor"> </span>';
        output.appendChild(finalLine);
        output.scrollTop = output.scrollHeight;

        diagBtn.disabled = false;
        diagBtn.innerHTML = '<i class="fas fa-tasks"></i> Diagnose erneut starten';
        isRunning = false;
    });
});