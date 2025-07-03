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