package com.dispensa.model;

public class PrecoMedioResponse {
    private double precoMedio;
    private double precoUltimaCompra;
    private double variacao;

    public double getPrecoMedio() { return precoMedio; }
    public void setPrecoMedio(double precoMedio) { this.precoMedio = precoMedio; }
    public double getPrecoUltimaCompra() { return precoUltimaCompra; }
    public void setPrecoUltimaCompra(double precoUltimaCompra) { this.precoUltimaCompra = precoUltimaCompra; }
    public double getVariacao() { return variacao; }
    public void setVariacao(double variacao) { this.variacao = variacao; }
}