(function () {
    const selectAllCheckbox = document.getElementById("selectAll");
    const deleteSelectedButton = document.getElementById("deleteSelectedBtn");
    const deleteForm = document.getElementById("deleteForm");
    const selectionStatus = document.getElementById("selectedUrlsStatus");

    if (!selectAllCheckbox || !deleteSelectedButton || !deleteForm || !selectionStatus) {
        return;
    }

    const urlCheckboxes = Array.from(document.querySelectorAll(".url-checkbox"));

    function updateSelectionState() {
        const selectedCount = urlCheckboxes.filter(function (checkbox) {
            return checkbox.checked;
        }).length;

        deleteSelectedButton.disabled = selectedCount === 0;
        selectAllCheckbox.checked = selectedCount === urlCheckboxes.length;
        selectAllCheckbox.indeterminate = selectedCount > 0 && selectedCount < urlCheckboxes.length;
        selectionStatus.textContent = selectedCount === 0
            ? "Select links to manage them."
            : selectedCount + (selectedCount === 1 ? " link selected." : " links selected.");
    }

    selectAllCheckbox.addEventListener("change", function () {
        urlCheckboxes.forEach(function (checkbox) {
            checkbox.checked = selectAllCheckbox.checked;
        });
        updateSelectionState();
    });

    urlCheckboxes.forEach(function (checkbox) {
        checkbox.addEventListener("change", updateSelectionState);
    });

    deleteSelectedButton.addEventListener("click", function () {
        if (window.confirm("Delete the selected URLs? This action cannot be undone.")) {
            deleteForm.submit();
        }
    });

    updateSelectionState();
})();
