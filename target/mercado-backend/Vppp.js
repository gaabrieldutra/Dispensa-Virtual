const USUARIO_ID = 1;
const API_BASE_URL = "http://localhost:8080/mercado-backend/api/relatorios";

let listaProdutosAutoComplete = [];

document.addEventListener("DOMContentLoaded", () => {
    carregarListaProdutosParaBusca();
    configurarAutocomplete();
});

// 1. Carrega produtos para alimentar a busca
async function carregarListaProdutosParaBusca() {
    try {
        const response = await fetch(`${API_BASE_URL}/produtos-mais-comprados?usuarioId=${USUARIO_ID}`);
        if (response.ok) {
            listaProdutosAutoComplete = await response.json();
            
            // Se houver produtos, carrega o primeiro automaticamente para preencher a tela
            if (listaProdutosAutoComplete.length > 0) {
                const primeiro = listaProdutosAutoComplete[0];
                selecionarProduto(primeiro);
            }
        }
    } catch (erro) {
        console.error("Erro ao carregar lista de produtos:", erro);
    }
}

// 2. Configura a pesquisa por autocomplete no input
function configurarAutocomplete() {
    const inputBusca = document.getElementById("input-busca-produto");
    const ulSugestoes = document.getElementById("sugestoes-autocomplete");

    inputBusca.addEventListener("input", (e) => {
        const termo = e.target.value.toLowerCase().trim();

        if (termo.length === 0) {
            ulSugestoes.style.display = "none";
            ulSugestoes.innerHTML = "";
            return;
        }

        const filtrados = listaProdutosAutoComplete.filter(p => {
            const nome = p.nomeProduto || p.nome || p.descricao || "";
            return nome.toLowerCase().includes(termo);
        });

        ulSugestoes.innerHTML = "";

        if (filtrados.length === 0) {
            ulSugestoes.style.display = "none";
            return;
        }

        filtrados.forEach(prod => {
            const li = document.createElement("li");
            li.className = "sugestao-item";
            li.textContent = prod.nomeProduto || prod.nome || prod.descricao;
            li.addEventListener("click", () => {
                inputBusca.value = li.textContent;
                ulSugestoes.style.display = "none";
                selecionarProduto(prod);
            });
            ulSugestoes.appendChild(li);
        });

        ulSugestoes.style.display = "block";
    });

    // Esconde a lista de sugestões ao clicar fora do campo
    document.addEventListener("click", (e) => {
        if (!e.target.closest(".busca-produto-container")) {
            ulSugestoes.style.display = "none";
        }
    });
}

// 3. Seleciona o produto e busca o histórico de preços dele
async function selecionarProduto(produto) {
    const produtoId = produto.id || produto.produtoId;
    const nomeProduto = produto.nomeProduto || produto.nome || produto.descricao;
    const categoria = produto.nomeCategoria || produto.categoria || "Geral";

    document.getElementById("nomeProdutoDestaque").textContent = nomeProduto;
    
    const tagCat = document.getElementById("tagCategoria");
    tagCat.textContent = categoria;
    tagCat.style.display = "inline-block";
    const classeTag = categoria.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    tagCat.className = `tag-categoria ${classeTag}`;

    try {
        const url = `${API_BASE_URL}/variacao-preco-produto?usuarioId=${USUARIO_ID}&produtoId=${produtoId}`;
        const response = await fetch(url);

        if (!response.ok) {
            exibirDadosVazios();
            return;
        }

        const historico = await response.json();

        if (!historico || historico.length === 0) {
            exibirDadosVazios();
            return;
        }

        processarERenderizarDados(historico);

    } catch (erro) {
        console.error("Erro ao carregar variação de preço:", erro);
        exibirDadosVazios();
    }
}

// 4. Calcula min/max, preço atual e desenha os pontos e a linha no gráfico
function processarERenderizarDados(historico) {
    const precos = historico.map(h => h.preco || h.valorUnitario || h.valor || 0);

    const menorPreco = Math.min(...precos);
    const maiorPreco = Math.max(...precos);
    const precoAtual = precos[precos.length - 1];

    document.getElementById("valMenorPreco").textContent = `R$ ${menorPreco.toFixed(2)}`;
    document.getElementById("valMaiorPreco").textContent = `R$ ${maiorPreco.toFixed(2)}`;
    document.getElementById("valPrecoAtual").textContent = `R$ ${precoAtual.toFixed(2)}`;

    // Define os limites do eixo Y com margem
    const margem = (maiorPreco === menorPreco) ? (maiorPreco * 0.2 || 5) : (maiorPreco - menorPreco) * 0.2;
    const minY = Math.max(0, menorPreco - margem);
    const maxY = maiorPreco + margem;

    renderizarEixoY(minY, maxY);
    renderizarPontosELinha(historico, minY, maxY);
}

function renderizarEixoY(minY, maxY) {
    const eixoY = document.getElementById("eixoY");
    eixoY.innerHTML = "";

    const passos = 4;
    const delta = (maxY - minY) / (passos - 1);

    for (let i = passos - 1; i >= 0; i--) {
        const valorStep = minY + (delta * i);
        const span = document.createElement("span");
        span.className = "label-eixo";
        span.textContent = `R$ ${valorStep.toFixed(0)}`;
        eixoY.appendChild(span);
    }
}

function renderizarPontosELinha(historico, minY, maxY) {
    const container = document.getElementById("containerGrafico") || document.getElementById("containerPontos");
    
    // Remove pontos antigos mantendo o elemento SVG
    const pontosAntigos = container.querySelectorAll(".ponto-flutuacao");
    pontosAntigos.forEach(p => p.remove());

    const totalPontos = historico.length;
    const rangeY = (maxY - minY) || 1;
    const pontosSvg = [];

    historico.forEach((item, index) => {
        const preco = item.preco || item.valorUnitario || item.valor || 0;
        const dataStr = item.dataCompra || item.data || item.mesAbreviado || "";

        // Posição vertical em porcentagem (bottom)
        const pctBottom = ((preco - minY) / rangeY) * 100;
        // Posição para o SVG (0 é no topo, 100 é no fundo)
        const svgY = 100 - pctBottom;

        // Distribuição horizontal x
        const pctX = totalPontos > 1 ? ((index + 0.5) / totalPontos) * 100 : 50;
        pontosSvg.push(`${pctX.toFixed(1)},${svgY.toFixed(1)}`);

        // Cria a coluna e o ponto visual
        const divPonto = document.createElement("div");
        divPonto.className = "coluna-grafico ponto-flutuacao";
        divPonto.innerHTML = `
            <span class="valor-barra">R$ ${preco.toFixed(2)}</span>
            <div class="ponto-preco" style="bottom: ${pctBottom.toFixed(1)}%;"></div>
            <span class="mes-barra">${dataStr}</span>
        `;

        container.appendChild(divPonto);
    });

    // Atualiza a linha SVG que conecta os pontos
    const polyline = document.getElementById("linhaPolyline");
    if (polyline) {
        polyline.setAttribute("points", pontosSvg.join(" "));
    }
}

function exibirDadosVazios() {
    document.getElementById("valMenorPreco").textContent = "R$ 0,00";
    document.getElementById("valMaiorPreco").textContent = "R$ 0,00";
    document.getElementById("valPrecoAtual").textContent = "R$ 0,00";

    const container = document.getElementById("containerPontos");
    const pontosAntigos = container.querySelectorAll(".ponto-flutuacao");
    pontosAntigos.forEach(p => p.remove());

    const polyline = document.getElementById("linhaPolyline");
    if (polyline) polyline.setAttribute("points", "");
}