(function () {
    const copyButtons = document.querySelectorAll("[data-copy-url]");

    if (!copyButtons.length) {
        return;
    }

    function copyWithFallback(text) {
        const textarea = document.createElement("textarea");
        textarea.value = text;
        textarea.setAttribute("readonly", "");
        textarea.style.position = "fixed";
        textarea.style.opacity = "0";
        document.body.appendChild(textarea);
        textarea.select();

        try {
            document.execCommand("copy");
            return Promise.resolve();
        } catch (error) {
            return Promise.reject(error);
        } finally {
            document.body.removeChild(textarea);
        }
    }

    function copyText(text) {
        if (navigator.clipboard && window.isSecureContext) {
            return navigator.clipboard.writeText(text);
        }

        return copyWithFallback(text);
    }

    copyButtons.forEach(function (button) {
        const defaultLabel = button.textContent.trim();
        button.textContent = defaultLabel;

        button.addEventListener("click", function () {
            const text = button.getAttribute("data-copy-url") || "";

            copyText(text).then(function () {
                button.textContent = "Copied";
                button.classList.add("is-copied");

                window.setTimeout(function () {
                    button.textContent = defaultLabel;
                    button.classList.remove("is-copied");
                }, 1800);
            }).catch(function () {
                button.textContent = "Copy failed";

                window.setTimeout(function () {
                    button.textContent = defaultLabel;
                }, 1800);
            });
        });
    });
})();
