document.addEventListener("DOMContentLoaded", () => {
    carregarGastoMensalTotal();
});

async function carregarGastoMensalTotal() {
    const usuarioId = 1; // ID de teste
    const url = `http://localhost:8080/mercado-backend/api/relatorios/gasto-mensal-total?usuarioId=${usuarioId}`;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            resetarTela();
            return;
        }

        const listaGastos = await response.json();

        if (!listaGastos || listaGastos.length === 0) {
            resetarTela();
            return;
        }

        calcularEExibirMetricas(listaGastos);
        renderizarGrafico(listaGastos);

    } catch (erro) {
        console.error("Erro ao carregar gasto mensal total:", erro);
        resetarTela();
    }
}

function calcularEExibirMetricas(lista) {
    const totalAcumulado = lista.reduce((acc, item) => acc + (item.valor || item.valorTotal || 0), 0);
    const mediaMensal = totalAcumulado / (lista.length || 1);
    
    // Pega o valor do último mês retornado na lista
    const ultimoItem = lista[lista.length - 1];
    const valorUltimoMes = ultimoItem ? (ultimoItem.valor || ultimoItem.valorTotal || 0) : 0;
    const nomeUltimoMes = ultimoItem ? (ultimoItem.mesAbreviado || ultimoItem.mes || "Último Mês") : "Último Mês";

    document.getElementById("valMediaMensal").textContent = `R$ ${mediaMensal.toFixed(2)}`;
    document.getElementById("labelMesAnterior").textContent = `Último Mês (${nomeUltimoMes})`;
    document.getElementById("valMesAnterior").textContent = `R$ ${valorUltimoMes.toFixed(2)}`;
    document.getElementById("valTotalRegistrado").textContent = `R$ ${totalAcumulado.toFixed(2)}`;
}

function renderizarGrafico(lista) {
    const containerGrafico = document.getElementById("containerGrafico");
    containerGrafico.innerHTML = "";

    // Maior valor para cálculo da altura proporcional (100%)
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
    document.getElementById("valMediaMensal").textContent = "R$ 0,00";
    document.getElementById("labelMesAnterior").textContent = "Último Mês";
    document.getElementById("valMesAnterior").textContent = "R$ 0,00";
    document.getElementById("valTotalRegistrado").textContent = "R$ 0,00";
    document.getElementById("containerGrafico").innerHTML = "<p style='color: #666; padding: 20px;'>Nenhum registro de compras encontrado.</p>";
}