document.addEventListener("DOMContentLoaded", () => {
    const btnMenu = document.getElementById("btnAbrirMenu");
    const btnFechar = document.getElementById("btnFecharMenu");
    const menuLateral = document.getElementById("menuLateral");
    const overlay = document.getElementById("menuOverlay");

    function abrirMenu() {
        menuLateral.classList.add("aberto");
        overlay.classList.add("aberto");
    }

    function fecharMenu() {
        menuLateral.classList.remove("aberto");
        overlay.classList.remove("aberto");
    }

    btnMenu.addEventListener("click", abrirMenu);
    btnFechar.addEventListener("click", fecharMenu);
    overlay.addEventListener("click", fecharMenu);
});