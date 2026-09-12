package com.dispensa.model;

public class ItemCompra {
    private int produtoId;       // usado quando já sabemos o ID (form manual)
    private String produtoNome;  // usado quando vem do XML (ainda sem ID)
    private double quantidade;
    private double precoUnitario;
    private String dataCompra;

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public String getProdutoNome() { return produtoNome; }
    public void setProdutoNome(String produtoNome) { this.produtoNome = produtoNome; }
    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public String getDataCompra() { return dataCompra; }
    public void setDataCompra(String dataCompra) { this.dataCompra = dataCompra; }
}