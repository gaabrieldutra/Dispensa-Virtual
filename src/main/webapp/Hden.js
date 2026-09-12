const USUARIO_ID = 1;
const API_URL = `http://localhost:8080/mercado-backend/api/danfe/historico/${USUARIO_ID}`;


let listaDanfesCompleta = [];

document.addEventListener("DOMContentLoaded", () => {
    carregarHistoricoDanfes();
    configurarBusca();
});

async function carregarHistoricoDanfes() {
    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            resetarTela();
            return;
        }

        listaDanfesCompleta = await response.json();

        if (!listaDanfesCompleta || listaDanfesCompleta.length === 0) {
            resetarTela();
            return;
        }

        atualizarCardsResumo(listaDanfesCompleta);
        renderizarListaDanfes(listaDanfesCompleta);

    } catch (erro) {
        console.error("Erro ao buscar histórico de DANFEs:", erro);
        resetarTela();
    }
}

function atualizarCardsResumo(lista) {
    const totalNotas = lista.length;
    const volumeTotal = lista.reduce((acc, item) => acc + (item.valorTotal || item.valor || 0), 0);
    
    // Assume que a API devolve do mais recente para o mais antigo ou pega o primeiro item
    const ultimaNota = lista[0];
    const dataUltimoEnvio = ultimaNota ? (ultimaNota.dataEnvio || ultimaNota.dataEmissao || "Recentemente") : "-";

    document.getElementById("valTotalEnviadas").textContent = `${totalNotas} notas`;
    document.getElementById("valVolumeProcessado").textContent = `R$ ${volumeTotal.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById("valUltimoEnvio").textContent = dataUltimoEnvio;
}

function renderizarListaDanfes(lista) {
    const container = document.getElementById("containerListaDanfes");
    container.innerHTML = "";

    if (lista.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; color: #666; padding: 25px;">
                Nenhuma nota fiscal encontrada com o termo pesquisado.
            </div>
        `;
        return;
    }

    lista.forEach(nota => {
        const numeroNota = nota.numeroNota || nota.numero || "Sem Número";
        const serie = nota.serie ? `— Série ${nota.serie}` : "";
        const fornecedor = nota.fornecedor || nota.emitente || nota.nomeFantasia || "Fornecedor não identificado";
        const dataEnvio = nota.dataEnvio || nota.dataEmissao || "";
        const valorTotal = nota.valorTotal || nota.valor || 0;
        const status = nota.status || "Processada";

        const itemDiv = document.createElement("div");
        itemDiv.className = "item-ranking";
        itemDiv.innerHTML = `
            <div class="icone-doc">📄</div>
            <div class="info-produto-ranking">
                <span class="nome-prod-rank">NF-e ${numeroNota} ${serie}</span>
                <span class="fornecedor-danfe">${fornecedor}</span>
                <span class="tag-data">Enviada em: ${dataEnvio}</span>
            </div>
            <div class="estatisticas-ranking">
                <span class="status-badge ${status.toLowerCase() === 'processada' ? 'sucesso' : 'pendente'}">${status}</span>
                <span class="qtd-compras" style="color: #1e293b;">R$ ${valorTotal.toFixed(2)}</span>
            </div>
        `;

        container.appendChild(itemDiv);
    });
}

function configurarBusca() {
    const inputBusca = document.getElementById("input-busca-danfe");

    inputBusca.addEventListener("input", (e) => {
        const termo = e.target.value.toLowerCase().trim();

        if (!termo) {
            renderizarListaDanfes(listaDanfesCompleta);
            return;
        }

        const filtradas = listaDanfesCompleta.filter(nota => {
            const numero = String(nota.numeroNota || nota.numero || "").toLowerCase();
            const fornecedor = String(nota.fornecedor || nota.emitente || "").toLowerCase();
            const chave = String(nota.chaveAcesso || "").toLowerCase();

            return numero.includes(termo) || fornecedor.includes(termo) || chave.includes(termo);
        });

        renderizarListaDanfes(filtradas);
    });
}

function resetarTela() {
    document.getElementById("valTotalEnviadas").textContent = "0 notas";
    document.getElementById("valVolumeProcessado").textContent = "R$ 0,00";
    document.getElementById("valUltimoEnvio").textContent = "-";

    document.getElementById("containerListaDanfes").innerHTML = `
        <div style="text-align: center; color: #666; padding: 25px;">
            Nenhuma DANFE cadastrada no histórico.
        </div>
    `;
}