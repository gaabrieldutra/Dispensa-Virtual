package com.dispensa.dao;

import com.dispensa.model.GastoMensal;
import com.dispensa.model.PrecoMedioResponse;
import com.dispensa.model.ProdutoRanking;
import com.dispensa.model.VariacaoPrecoItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelatorioDAO {

        public List<VariacaoPrecoItem> variacaoPrecoProduto(int usuarioId, int produtoId) throws SQLException {
    List<VariacaoPrecoItem> lista = new ArrayList<>();

    String sql = "SELECT ic.preco_unitario, ic.data_compra " +
                 "FROM item_compra ic " +
                 "JOIN nota_fiscal nf ON ic.nota_fiscal_id = nf.id " +
                 "WHERE nf.usuario_id = ? AND ic.produto_id = ? " +
                 "ORDER BY ic.data_compra ASC";

    try (Connection con = ConexaoBD.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        stmt.setInt(1, usuarioId);
        stmt.setInt(2, produtoId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                VariacaoPrecoItem item = new VariacaoPrecoItem();
                item.setPreco(rs.getDouble("preco_unitario"));

                Date data = rs.getDate("data_compra");
                item.setDataCompra(data != null ? data.toString() : null);

                lista.add(item);
            }
        }
    }
    return lista;
}

        public List<GastoMensal> gastoMensalPorCategoria(int usuarioId, int categoriaId) throws SQLException {
    List<GastoMensal> lista = new ArrayList<>();

    String sql = "SELECT YEAR(ic.data_compra) AS ano, MONTH(ic.data_compra) AS mes, " +
                 "       SUM(ic.quantidade * ic.preco_unitario) AS valorTotal " +
                 "FROM item_compra ic " +
                 "JOIN nota_fiscal nf ON ic.nota_fiscal_id = nf.id " +
                 "JOIN produto p ON ic.produto_id = p.id " +
                 "WHERE nf.usuario_id = ? AND p.categoria_id = ? " +
                 "GROUP BY ano, mes " +
                 "ORDER BY ano ASC, mes ASC " +
                 "LIMIT 12";

    try (Connection con = ConexaoBD.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        stmt.setInt(1, usuarioId);
        stmt.setInt(2, categoriaId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                GastoMensal item = new GastoMensal();
                item.setMesAbreviado(nomeMesAbreviado(rs.getInt("mes")));
                item.setValorTotal(rs.getDouble("valorTotal"));
                lista.add(item);
            }
        }
    }
    return lista;
}


    public List<GastoMensal> gastoMensalTotal(int usuarioId) throws SQLException {
    List<GastoMensal> lista = new ArrayList<>();

    String sql = "SELECT YEAR(ic.data_compra) AS ano, MONTH(ic.data_compra) AS mes, " +
                 "       SUM(ic.quantidade * ic.preco_unitario) AS valorTotal " +
                 "FROM item_compra ic " +
                 "JOIN nota_fiscal nf ON ic.nota_fiscal_id = nf.id " +
                 "WHERE nf.usuario_id = ? " +
                 "GROUP BY ano, mes " +
                 "ORDER BY ano ASC, mes ASC " +
                 "LIMIT 12";

    try (Connection con = ConexaoBD.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        stmt.setInt(1, usuarioId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int mes = rs.getInt("mes");

                GastoMensal item = new GastoMensal();
                item.setMesAbreviado(nomeMesAbreviado(mes));
                item.setValorTotal(rs.getDouble("valorTotal"));
                lista.add(item);
            }
        }
    }
    return lista;
}

private String nomeMesAbreviado(int mes) {
    String[] nomes = {"Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"};
    return nomes[mes - 1];
}

     public List<ProdutoRanking> produtosMaisComprados(int usuarioId) throws SQLException {
    List<ProdutoRanking> lista = new ArrayList<>();

    String sql = "SELECT p.id AS produtoId, p.nome AS nomeProduto, " +
                 "       COALESCE(c.nome, 'Geral') AS nomeCategoria, " +
                 "       COUNT(ic.id) AS quantidade, " +
                 "       SUM(ic.quantidade * ic.preco_unitario) AS totalGasto " +
                 "FROM item_compra ic " +
                 "JOIN nota_fiscal nf ON ic.nota_fiscal_id = nf.id " +
                 "JOIN produto p ON ic.produto_id = p.id " +
                 "LEFT JOIN categoria c ON p.categoria_id = c.id " +
                 "WHERE nf.usuario_id = ? " +
                 "GROUP BY p.id, p.nome, c.nome " +
                 "ORDER BY quantidade DESC, totalGasto DESC " +
                 "LIMIT 20";

    try (Connection con = ConexaoBD.getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

        stmt.setInt(1, usuarioId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ProdutoRanking item = new ProdutoRanking();
                item.setId(rs.getInt("produtoId"));
                item.setNomeProduto(rs.getString("nomeProduto"));
                item.setNomeCategoria(rs.getString("nomeCategoria"));
                item.setQuantidade(rs.getInt("quantidade"));
                item.setTotalGasto(rs.getDouble("totalGasto"));
                lista.add(item);
            }
        }
    }
    return lista;
}

    public PrecoMedioResponse calcularPrecoMedio(int usuarioId, String nomeProduto) throws SQLException {
        String sql = "SELECT ic.preco_unitario " +
                     "FROM item_compra ic " +
                     "JOIN nota_fiscal nf ON ic.nota_fiscal_id = nf.id " +
                     "JOIN produto p ON ic.produto_id = p.id " +
                     "WHERE nf.usuario_id = ? AND p.nome = ? " +
                     "ORDER BY ic.data_compra DESC"; // mais recente primeiro

        List<Double> precos = new ArrayList<>();

        try (Connection con = ConexaoBD.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setString(2, nomeProduto);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    precos.add(rs.getDouble("preco_unitario"));
                }
            }
        }

        PrecoMedioResponse response = new PrecoMedioResponse();

        if (precos.isEmpty()) {
            return response; // tudo zero, o front já trata isso mostrando "R$ 0,00"
        }

        double soma = 0;
        for (double preco : precos) soma += preco;
        double media = soma / precos.size();

        double ultimaCompra = precos.get(0); // já veio ordenado do mais recente pro mais antigo

        double variacao = media != 0 ? ((ultimaCompra - media) / media) * 100 : 0;

        response.setPrecoMedio(media);
        response.setPrecoUltimaCompra(ultimaCompra);
        response.setVariacao(variacao);

        return response;
    }
}