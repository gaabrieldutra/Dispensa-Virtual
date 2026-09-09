document.addEventListener("DOMContentLoaded", () => {
    configurarBotoesCategoria();
    // Inicia carregando a primeira categoria (ID 1)
    carregarRelatorioCategoria(1, "Alimentos");
});

function configurarBotoesCategoria() {
    const botoes = document.querySelectorAll("#containerPills .pill-categoria");

    botoes.forEach(btn => {
        btn.addEventListener("click", (e) => {
            botoes.forEach(b => b.classList.remove("ativo"));
            e.target.classList.add("ativo");

            const categoriaId = e.target.getAttribute("data-categoria-id");
            const nomeCategoria = e.target.getAttribute("data-categoria");
            carregarRelatorioCategoria(categoriaId, nomeCategoria);
        });
    });
}

async function carregarRelatorioCategoria(categoriaId, nomeCategoria) {
    const usuarioId = 1; // ID de teste
    const url = `http://localhost:8080/mercado-backend/api/relatorios/gasto-mensal-categoria?usuarioId=${usuarioId}&categoriaId=${categoriaId}`;

    document.getElementById("tituloGrafico").textContent = `Evolução Mensal — ${nomeCategoria}`;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            resetarTela();
            return;
        }

        // Retorna a List<GastoMensalDTO> do Java
        const listaGastos = await response.json();

        if (!listaGastos || listaGastos.length === 0) {
            resetarTela();
            return;
        }

        // Calcula os totais e médias no próprio JS com base no retorno da lista
        calcularEExibirMetricas(listaGastos);
        renderizarGrafico(listaGastos);

    } catch (erro) {
        console.error("Erro ao carregar relatório por categoria:", erro);
        resetarTela();
    }
}

function calcularEExibirMetricas(lista) {
    const totalAno = lista.reduce((acc, item) => acc + (item.valor || item.valorTotal || 0), 0);
    const mediaMensal = totalAno / (lista.length || 1);
    
    // Encontra o mês com maior gasto
    const mesMaisAlto = lista.reduce((prev, current) => {
        const valPrev = prev.valor || prev.valorTotal || 0;
        const valCurr = current.valor || current.valorTotal || 0;
        return (valPrev > valCurr) ? prev : current;
    }, lista[0]);

    const valorMaisAlto = mesMaisAlto.valor || mesMaisAlto.valorTotal || 0;
    const nomeMesMaisAlto = mesMaisAlto.mesAbreviado || mesMaisAlto.mes || "-";

    document.getElementById("valMediaCategoria").textContent = `R$ ${mediaMensal.toFixed(2)}`;
    document.getElementById("labelMesMaisAlto").textContent = `Mês mais Alto (${nomeMesMaisAlto})`;
    document.getElementById("valMesMaisAlto").textContent = `R$ ${valorMaisAlto.toFixed(2)}`;
    document.getElementById("valTotalAno").textContent = `R$ ${totalAno.toFixed(2)}`;
}

function renderizarGrafico(lista) {
    const containerGrafico = document.getElementById("containerGrafico");
    containerGrafico.innerHTML = "";

    const maiorValor = Math.max(...lista.map(item => item.valor || item.valorTotal || 0), 1);

    lista.forEach(item => {
        const valor = item.valor || item.valorTotal || 0;
        const mes = item.mesAbreviado || item.mes || "";
        const porcentagem = Math.round((valor / maiorValor) * 100);

        const coluna = document.createElement("div");
        coluna.className = "coluna-grafico";
        coluna.innerHTML = `
            <span class="valor-barra">R$ ${Math.round(valor)}</span>
            <div class="barra" style="height: ${porcentagem}%;"></div>
            <span class="mes-barra">${mes}</span>
        `;

        containerGrafico.appendChild(coluna);
    });
}

function resetarTela() {
    document.getElementById("valMediaCategoria").textContent = "R$ 0,00";
    document.getElementById("labelMesMaisAlto").textContent = "Mês mais Alto";
    document.getElementById("valMesMaisAlto").textContent = "R$ 0,00";
    document.getElementById("valTotalAno").textContent = "R$ 0,00";
    document.getElementById("containerGrafico").innerHTML = "<p style='color: #666; padding: 20px;'>Sem dados cadastrados para esta categoria.</p>";
}