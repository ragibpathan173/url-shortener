(function () {
    const video = document.querySelector(".hero-video");

    if (!video) {
        return;
    }

    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        video.pause();
        return;
    }

    const fadeDuration = 500;
    const fadeOutLeadTime = 0.55;
    const visibleOpacity = 0.42;

    let animationFrameId = null;
    let fadingOut = false;

    function getOpacity() {
        const opacity = Number.parseFloat(video.style.opacity);
        return Number.isFinite(opacity) ? opacity : 0;
    }

    function cancelFade() {
        if (animationFrameId !== null) {
            window.cancelAnimationFrame(animationFrameId);
            animationFrameId = null;
        }
    }

    function fadeTo(targetOpacity) {
        cancelFade();

        const startOpacity = getOpacity();
        const startTime = window.performance.now();

        function animate(now) {
            const elapsed = now - startTime;
            const progress = Math.min(elapsed / fadeDuration, 1);
            const nextOpacity = startOpacity + ((targetOpacity - startOpacity) * progress);

            video.style.opacity = String(nextOpacity);

            if (progress < 1) {
                animationFrameId = window.requestAnimationFrame(animate);
                return;
            }

            animationFrameId = null;
        }

        animationFrameId = window.requestAnimationFrame(animate);
    }

    function fadeIn() {
        fadingOut = false;
        fadeTo(visibleOpacity);
    }

    function fadeOut() {
        if (fadingOut) {
            return;
        }

        fadingOut = true;
        fadeTo(0);
    }

    function restartVideo() {
        cancelFade();
        video.style.opacity = "0";
        fadingOut = false;

        window.setTimeout(function () {
            video.currentTime = 0;
            video.play().catch(function () {
                // Autoplay can be blocked by the browser, but the page still works without motion.
            });
            fadeIn();
        }, 100);
    }

    video.style.opacity = "0";

    video.addEventListener("loadeddata", fadeIn, { once: true });
    video.addEventListener("play", fadeIn);
    video.addEventListener("timeupdate", function () {
        if (!Number.isFinite(video.duration) || video.duration <= 0) {
            return;
        }

        if (video.duration - video.currentTime <= fadeOutLeadTime) {
            fadeOut();
        }
    });
    video.addEventListener("ended", restartVideo);
})();
