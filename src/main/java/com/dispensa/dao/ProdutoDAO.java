package com.dispensa.dao;

import com.dispensa.model.Categoria;
import com.dispensa.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public int buscarOuCriarPorNome(Connection con, String nome) throws SQLException {
        String sqlBusca = "SELECT id FROM produto WHERE nome = ?";
        try (PreparedStatement stmt = con.prepareStatement(sqlBusca)) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        String sqlInsere = "INSERT INTO produto (nome, categoria_id) VALUES (?, NULL)";
            try (PreparedStatement stmt = con.prepareStatement(sqlInsere, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nome);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }

        throw new SQLException("Não foi possível criar o produto: " + nome);
    }   

    public List<Produto> buscarPorTermo(String termo) throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.nome, c.id AS categoria_id, c.nome AS categoria_nome " +
                     "FROM produto p LEFT JOIN categoria c ON p.categoria_id = c.id " +
                     "WHERE p.nome LIKE ?";

        try (Connection con = ConexaoBD.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, "%" + termo + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setId(rs.getInt("id"));
                    produto.setNome(rs.getString("nome"));

                    if (rs.getObject("categoria_id") != null) {
                        Categoria categoria = new Categoria();
                        categoria.setId(rs.getInt("categoria_id"));
                        categoria.setNome(rs.getString("categoria_nome"));
                        produto.setCategoria(categoria);
                    }

                    lista.add(produto);
                }
            }
        }
        return lista;
    }
}
