document.addEventListener("DOMContentLoaded", () => {
    carregarProdutosMaisComprados();
});

async function carregarProdutosMaisComprados() {
    const usuarioId = 1;
    const url = `http://localhost:8080/mercado-backend/api/relatorios/produtos-mais-comprados?usuarioId=${usuarioId}`;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            resetarTela();
            return;
        }

        const produtos = await response.json();

        if (!produtos || produtos.length === 0) {
            resetarTela();
            return;
        }

        atualizarCards(produtos);
        renderizarRanking(produtos);

    } catch (erro) {
        console.error("Erro ao carregar ranking de produtos:", erro);
        resetarTela();
    }
}

function atualizarCards(produtos) {
    const topProduto = produtos[0];
    const nomeMaisFrequente = topProduto ? (topProduto.nomeProduto || topProduto.nome || topProduto.descricao || "-") : "-";

    // Encontra a categoria que mais se repete no top 20
    const categoriasCount = {};
    produtos.forEach(p => {
        const cat = p.nomeCategoria || p.categoria || "Outros";
        categoriasCount[cat] = (categoriasCount[cat] || 0) + 1;
    });

    let categoriaLider = "-";
    let maxCount = 0;
    for (const [cat, count] of Object.entries(categoriasCount)) {
        if (count > maxCount) {
            maxCount = count;
            categoriaLider = cat;
        }
    }

    document.getElementById("valMaisFrequente").textContent = nomeMaisFrequente;
    document.getElementById("valItensDistintos").textContent = `${produtos.length} produtos`;
    document.getElementById("valCategoriaLider").textContent = categoriaLider;
}

function renderizarRanking(produtos) {
    const container = document.getElementById("containerRanking");
    container.innerHTML = "";

    const classesPodio = ["ouro", "prata", "bronze"];

    produtos.forEach((prod, index) => {
        const posicao = index + 1;
        const classeMedalha = index < 3 ? classesPodio[index] : "comum";

        const nome = prod.nomeProduto || prod.nome || prod.descricao || "Produto Sem Nome";
        const categoria = prod.nomeCategoria || prod.categoria || "Geral";
        const quantidade = prod.quantidade || prod.qtdCompras || prod.qtdTotal || 0;
        const totalGasto = prod.totalGasto || prod.valorTotal || 0;

        // Classe CSS minúscula para a tag da categoria (ex: alimentos, bebidas)
        const classeTagCategoria = categoria.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");

        const itemDiv = document.createElement("div");
        itemDiv.className = "item-ranking";
        itemDiv.innerHTML = `
            <div class="posicao-ranking ${classeMedalha}">${posicao}º</div>
            <div class="info-produto-ranking">
                <span class="nome-prod-rank">${nome}</span>
                <span class="tag-categoria ${classeTagCategoria}">${categoria}</span>
            </div>
            <div class="estatisticas-ranking">
                <span class="qtd-compras">${quantidade} compras</span>
                <span class="total-gasto-rank">R$ ${totalGasto.toFixed(2)} total</span>
            </div>
        `;

        container.appendChild(itemDiv);
    });
}

function resetarTela() {
    document.getElementById("valMaisFrequente").textContent = "-";
    document.getElementById("valItensDistintos").textContent = "0 produtos";
    document.getElementById("valCategoriaLider").textContent = "-";

    document.getElementById("containerRanking").innerHTML = `
        <div style="text-align: center; color: #666; padding: 20px;">
            Nenhum produto cadastrado no histórico.
        </div>
    `;
}