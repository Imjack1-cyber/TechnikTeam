document.addEventListener('DOMContentLoaded', () => {
    // Data from JSP to JS
    const groupedFiles = {
        <c:forEach var="entry" items="${groupedFiles}" varStatus="status">
            "<c:out value="${entry.key}"/>": [
                <c:forEach var="file" items="${entry.value}" varStatus="fileStatus">
                    { id: ${file.id}, filename: "${fn:replace(file.filename, '"', '\\"')}", filepath: "${fn:replace(file.filepath, '"', '\\"')}", requiredRole: "${file.requiredRole}" }
                    ${!fileStatus.last ? ',' : ''}
                </c:forEach>
            ]
            ${!status.last ? ',' : ''}
        </c:forEach>
    };

    const allCategories = [
        <c:forEach var="cat" items="${allCategories}" varStatus="status">
            { id: ${cat.id}, name: "${fn:replace(cat.name, '"', '\\"')}" }
            ${!status.last ? ',' : ''}
        </c:forEach>
    ];

    const categoryListItems = document.querySelectorAll('.category-list li');
    const dynamicContentArea = document.getElementById('dynamic-content-area');
    const placeholder = document.getElementById('section-placeholder');
    const sectionTemplate = document.getElementById('category-section-template');
    const fileItemTemplate = document.getElementById('file-item-template');
    const contextPath = "${pageContext.request.contextPath}";

    const switchCategoryView = (categoryId) => {
        dynamicContentArea.innerHTML = '';
        placeholder.style.display = 'none';
        categoryListItems.forEach(item => item.classList.remove('active'));

        if (!categoryId) {
            placeholder.style.display = 'block';
            return;
        }

        const category = allCategories.find(c => c.id == categoryId);
        if (!category) return;

        // Activate the selected category list item
        const activeLi = document.querySelector(`.category-list li[data-category-id='${categoryId}']`);
        if(activeLi) activeLi.classList.add('active');

        // Clone and populate the main section template
        const sectionClone = sectionTemplate.content.cloneNode(true);
        sectionClone.querySelectorAll('.category-name').forEach(el => el.textContent = category.name);
        sectionClone.querySelectorAll('.category-id-input').forEach(el => el.value = category.id);
        sectionClone.querySelector('.category-name-input').value = category.name;

        // Populate the file list
        const fileList = sectionClone.querySelector('.file-list');
        const filesForCategory = groupedFiles[category.name] || [];

        if (filesForCategory.length > 0) {
            filesForCategory.forEach(file => {
                const fileItemClone = fileItemTemplate.content.cloneNode(true);
                const downloadLink = fileItemClone.querySelector('.file-download-link');
                // CORRECTED LINE: Use standard string concatenation to avoid EL conflict
                downloadLink.href = contextPath + '/download?file=' + encodeURIComponent(file.filepath);
                downloadLink.textContent = file.filename;
                fileItemClone.querySelector('.file-meta').textContent = `(Sichtbar für: ${file.requiredRole})`;
                fileItemClone.querySelector('.file-id-input').value = file.id;
                
                const fileForm = fileItemClone.querySelector('form');
                fileForm.dataset.confirmMessage = `Datei '${file.filename}' wirklich löschen?`;
                fileForm.addEventListener('submit', function(e) {
                    e.preventDefault();
                    showConfirmationModal(this.dataset.confirmMessage, () => this.submit());
                });
                
                fileList.appendChild(fileItemClone);
            });
        } else {
            fileList.innerHTML = '<li style="justify-content: center; color: var(--text-muted-color);">Keine Dateien in dieser Kategorie.</li>';
        }

        dynamicContentArea.appendChild(sectionClone);
        
        // Re-attach file size validation and confirmation to the newly added forms
        attachFileSizeValidator(dynamicContentArea.querySelector('.file-input'));
        dynamicContentArea.querySelectorAll('.js-confirm-form').forEach(form => {
             form.addEventListener('submit', function(e) {
                e.preventDefault();
                showConfirmationModal(this.dataset.confirmMessage, () => this.submit());
            });
        });
    };
    
    const attachFileSizeValidator = (input) => {
        if (!input) return;
        input.addEventListener('change', (e) => {
            const file = e.target.files[0];
            const maxSize = parseInt(e.target.dataset.maxSize, 10);
            const warningElement = e.target.nextElementSibling;
            if (file && file.size > maxSize) {
                warningElement.style.display = 'block';
                e.target.value = '';
            } else {
                warningElement.style.display = 'none';
            }
        });
    };

    categoryListItems.forEach(item => {
        item.addEventListener('click', () => {
            switchCategoryView(item.dataset.categoryId);
        });
    });
    
    // Initial view
    switchCategoryView(null);
});