package com.dispensa.model;

import java.util.List;

public class OcrSugestao {
    private String textoBruto;
    private String fornecedorSugerido;
    private String dataSugerida;
    private double valorSugerido;
    private String numeroNotaSugerido;
    private String serieSugerida;
    private List<ItemOcrSugestao> itensSugeridos;

    public String getTextoBruto() { return textoBruto; }
    public void setTextoBruto(String textoBruto) { this.textoBruto = textoBruto; }
    public String getFornecedorSugerido() { return fornecedorSugerido; }
    public void setFornecedorSugerido(String fornecedorSugerido) { this.fornecedorSugerido = fornecedorSugerido; }
    public String getDataSugerida() { return dataSugerida; }
    public void setDataSugerida(String dataSugerida) { this.dataSugerida = dataSugerida; }
    public double getValorSugerido() { return valorSugerido; }
    public void setValorSugerido(double valorSugerido) { this.valorSugerido = valorSugerido; }
    public String getNumeroNotaSugerido() { return numeroNotaSugerido; }
    public void setNumeroNotaSugerido(String numeroNotaSugerido) { this.numeroNotaSugerido = numeroNotaSugerido; }
    public String getSerieSugerida() { return serieSugerida; }
    public void setSerieSugerida(String serieSugerida) { this.serieSugerida = serieSugerida; }
    public List<ItemOcrSugestao> getItensSugeridos() { return itensSugeridos; }
    public void setItensSugeridos(List<ItemOcrSugestao> itensSugeridos) { this.itensSugeridos = itensSugeridos; }
}