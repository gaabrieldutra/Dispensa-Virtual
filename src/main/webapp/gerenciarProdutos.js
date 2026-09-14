document.addEventListener("DOMContentLoaded", () => {
    carregarProdutos("");

    const inputBusca = document.getElementById("input-busca-produto");
    inputBusca.addEventListener("input", (e) => {
        carregarProdutos(e.target.value.trim());
    });
});

async function carregarProdutos(termo) {
    const url = `http://localhost:8080/mercado-backend/api/produtos/buscar?termo=${encodeURIComponent(termo)}`;

    try {
        const response = await fetch(url);
        const produtos = await response.json();
        renderizarLista(produtos);
    } catch (erro) {
        console.error("Erro ao buscar produtos:", erro);
    }
}

function renderizarLista(produtos) {
    const container = document.getElementById("containerProdutos");
    container.innerHTML = "";

    if (produtos.length === 0) {
        container.innerHTML = `<div style="text-align:center; color:#666; padding:20px;">Nenhum produto encontrado.</div>`;
        return;
    }

    produtos.forEach(produto => {
        const nomeCategoria = produto.categoria ? produto.categoria.nome : "Geral";

        const div = document.createElement("div");
        div.className = "item-produto-gerenciar";
        div.innerHTML = `
            <span class="nome-item-gerenciar">${produto.nome}</span>
            <span class="tag-categoria">${nomeCategoria}</span>
            <button type="button" class="btn-excluir-produto" data-id="${produto.id}" data-nome="${produto.nome}">🗑️ Excluir</button>
        `;

        container.appendChild(div);
    });

    document.querySelectorAll(".btn-excluir-produto").forEach(btn => {
        btn.addEventListener("click", () => confirmarExclusao(btn));
    });
}

async function confirmarExclusao(btn) {
    const id = btn.getAttribute("data-id");
    const nome = btn.getAttribute("data-nome");

    const confirmado = confirm(`Tem certeza que deseja excluir "${nome}"? Isso também vai remover o histórico de compras desse produto.`);
    if (!confirmado) return;

    try {
        const response = await fetch(`http://localhost:8080/mercado-backend/api/produtos/buscar?id=${id}`, {
            method: "DELETE"
        });

        if (response.ok) {
            btn.closest(".item-produto-gerenciar").remove();
        } else {
            alert("Não foi possível excluir o produto.");
        }
    } catch (erro) {
        console.error("Erro ao excluir produto:", erro);
        alert("Erro de conexão ao tentar excluir.");
    }
}