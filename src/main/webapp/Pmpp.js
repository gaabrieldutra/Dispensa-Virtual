document.addEventListener("DOMContentLoaded", () => {
    const inputBusca = document.getElementById("input-busca-produto");
    const listaSugestoes = document.getElementById("listaSugestoes");

    // Evento ao digitar no campo de busca (Autocomplete)
    inputBusca.addEventListener("input", async (e) => {
        const termo = e.target.value.trim();

        if (termo.length < 2) {
            listaSugestoes.style.display = "none";
            listaSugestoes.innerHTML = "";
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/mercado-backend/api/produtos/buscar?termo=${encodeURIComponent(termo)}`);
            const produtos = await response.json();

            listaSugestoes.innerHTML = "";

            if (produtos.length === 0) {
                listaSugestoes.style.display = "none";
                return;
            }

            produtos.forEach(prod => {
                const li = document.createElement("li");
                li.className = "sugestao-item";
                li.textContent = prod.nome;
                
                // Clique na sugestão para carregar as métricas do produto
                li.addEventListener("click", () => {
                    inputBusca.value = prod.nome;
                    listaSugestoes.style.display = "none";
                    carregarPrecoMedio(prod.nome);
                });

                listaSugestoes.appendChild(li);
            });

            listaSugestoes.style.display = "block";
        } catch (erro) {
            console.error("Erro na busca de produtos:", erro);
        }
    });
});

async function carregarPrecoMedio(nomeProduto) {
    const usuarioId = 1; // ID padrão de teste
    const url = `http://localhost:8080/mercado-backend/api/produtos/preco-medio?usuarioId=${usuarioId}&nomeProduto=${encodeURIComponent(nomeProduto)}`;

    try {
        const response = await fetch(url);
        
        if (!response.ok) {
            alert("Não foi possível obter dados para este produto.");
            return;
        }

        const dados = await response.json();

        // Atualiza cabeçalho e cards
        document.getElementById("nomeProdutoDestaque").textContent = nomeProduto;
        document.getElementById("valPrecoMedio").textContent = `R$ ${dados.precoMedio ? dados.precoMedio.toFixed(2) : "0,00"}`;
        document.getElementById("valUltimaCompra").textContent = `R$ ${dados.precoUltimaCompra ? dados.precoUltimaCompra.toFixed(2) : "0,00"}`;
        
        // Atualiza a variação
        if (dados.variacao) {
            document.getElementById("valVariacao").textContent = `${dados.variacao > 0 ? '+' : ''}${dados.variacao.toFixed(1)}%`;
        }

    } catch (erro) {
        console.error("Erro ao carregar relatório:", erro);
    }
}