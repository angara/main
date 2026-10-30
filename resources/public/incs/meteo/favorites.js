(function () {
  "use strict";

  function init() {
    const container = document.querySelector(".b-meteo-brief");
    if (!container) return;

    container.addEventListener("click", async function (event) {
      const button = event.target.closest(".meteo-favorite");
      if (!button || !container.contains(button) || button.disabled) return;

      const favorite = button.getAttribute("aria-pressed") !== "true";
      const controller = new AbortController();
      const timeout = setTimeout(function () { controller.abort(); }, 15000);
      button.disabled = true;

      try {
        const response = await fetch("/meteo/favorite", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "x-csrf-token": container.getAttribute("data-csrf")
          },
          body: JSON.stringify({st: button.getAttribute("data-st"), favorite: favorite}),
          signal: controller.signal
        });
        const data = await response.json();
        if (!response.ok || !data.ok || typeof data.favorite !== "boolean") {
          throw new Error(data.msg);
        }

        const label = data.favorite ? "Убрать из избранного" : "В избранное";
        button.textContent = data.favorite ? "★" : "☆";
        button.setAttribute("aria-pressed", String(data.favorite));
        button.setAttribute("aria-label", label);
        button.setAttribute("title", label);
      } catch (error) {
        alert(error.message && error.name === "Error" ? error.message :
          "Не удалось сохранить отметку. Попробуйте ещё раз или обновите страницу.");
      } finally {
        clearTimeout(timeout);
        button.disabled = false;
      }
    });
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init, {once: true});
  } else {
    init();
  }
}());
