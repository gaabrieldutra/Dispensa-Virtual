package com.dispensa.model;

import java.util.List;

public class CompraRequest {
    private int usuarioId;
    private String numeroNota;
    private String serie;
    private String chaveAcesso;
    private String fornecedor;
    private String dataEmissao;
    private double valorTotal;
    private String origem = "MANUAL"; // MANUAL, XML ou OCR
    private List<ItemCompra> itens;

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getNumeroNota() { return numeroNota; }
    public void setNumeroNota(String numeroNota) { this.numeroNota = numeroNota; }
    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }
    public String getChaveAcesso() { return chaveAcesso; }
    public void setChaveAcesso(String chaveAcesso) { this.chaveAcesso = chaveAcesso; }
    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }
    public String getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(String dataEmissao) { this.dataEmissao = dataEmissao; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public List<ItemCompra> getItens() { return itens; }
    public void setItens(List<ItemCompra> itens) { this.itens = itens; }
}