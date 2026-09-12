package com.dispensa.model;

public class ProdutoRanking {
    private int id;
    private String nomeProduto;
    private String nomeCategoria;
    private int quantidade;
    private double totalGasto;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getTotalGasto() { return totalGasto; }
    public void setTotalGasto(double totalGasto) { this.totalGasto = totalGasto; }
}