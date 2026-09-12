// Executa o código assim que a página terminar de carregar no navegador
document.addEventListener("DOMContentLoaded", () => {
    carregarUltimaCompra();
});

async function carregarUltimaCompra() {
    const listaContainer = document.getElementById("listaUltimaCompra");

    // Endpoint da sua API no WildFly
    const url = "http://localhost:8080/mercado-backend/api/produtos/buscar?termo=";

    try {
        const response = await fetch(url);
        const produtos = await response.json();

        // Limpa a lista antes de inserir
        listaContainer.innerHTML = "";

        // Se o banco não retornar nenhum item
        if (produtos.length === 0) {
            listaContainer.innerHTML = "<p>Nenhum item encontrado na última compra.</p>";
            return;
        }

        // Percorre cada produto do banco e monta o HTML mantendo as suas classes do CSS
        produtos.forEach(produto => {
            const categoriaNome = produto.categoria ? produto.categoria.nome.toLowerCase() : "alimentos";
            
            const cardHTML = `
                <li class="item-card">
                    <span class="nome-item">${produto.nome}</span>
                    <span class="tag-categoria ${categoriaNome}">${produto.categoria ? produto.categoria.nome : 'Geral'}</span>
                    <div class="quantidade-box">
                        <span class="numero">1</span>
                        <span class="unidade">un</span>
                    </div>
                </li>
            `;

            // Adiciona o card criado dentro da <ul>
            listaContainer.innerHTML += cardHTML;
        });

    } catch (erro) {
        console.error("Erro ao buscar itens:", erro);
        listaContainer.innerHTML = "<p>Erro ao carregar os itens do banco de dados.</p>";
    }
}