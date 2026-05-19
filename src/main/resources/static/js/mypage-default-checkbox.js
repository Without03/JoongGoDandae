document.addEventListener("DOMContentLoaded", () => {
    const checkboxes = document.querySelectorAll("input[type='checkbox'][data-default-group]");

    checkboxes.forEach((checkbox) => {
        checkbox.addEventListener("change", () => {
            if (!checkbox.checked) {
                return;
            }

            const group = checkbox.dataset.defaultGroup;
            document.querySelectorAll("input[type='checkbox'][data-default-group='" + group + "']").forEach((item) => {
                if (item !== checkbox) {
                    item.checked = false;
                }
            });
        });
    });
});
