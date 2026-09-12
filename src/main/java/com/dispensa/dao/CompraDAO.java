package com.dispensa.dao;

import com.dispensa.model.CompraRequest;
import com.dispensa.model.ItemCompra;

import java.sql.*;

public class CompraDAO {

    private final ProdutoDAO produtoDao = new ProdutoDAO();

    public int registrarCompraCompleta(CompraRequest compra) throws SQLException {
        String sqlNota = "INSERT INTO nota_fiscal (usuario_id, numero_nota, serie, chave_acesso, fornecedor, data_emissao, valor_total, status, origem) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, 'Processada', ?)";

        String sqlItem = "INSERT INTO item_compra (nota_fiscal_id, produto_id, quantidade, preco_unitario, data_compra) " +
                          "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;

        try {
            con = ConexaoBD.getConnection();
            con.setAutoCommit(false);

            int notaId;

            try (PreparedStatement stmtNota = con.prepareStatement(sqlNota, Statement.RETURN_GENERATED_KEYS)) {
                stmtNota.setInt(1, compra.getUsuarioId());
                stmtNota.setString(2, compra.getNumeroNota());
                stmtNota.setString(3, compra.getSerie());
                stmtNota.setString(4, compra.getChaveAcesso());
                stmtNota.setString(5, compra.getFornecedor());
                stmtNota.setDate(6, Date.valueOf(compra.getDataEmissao()));
                stmtNota.setDouble(7, compra.getValorTotal());
                stmtNota.setString(8, compra.getOrigem());
                stmtNota.executeUpdate();

                try (ResultSet rs = stmtNota.getGeneratedKeys()) {
                    if (rs.next()) notaId = rs.getInt(1);
                    else throw new SQLException("Falha ao obter o ID da nota fiscal.");
                }
            }

            try (PreparedStatement stmtItem = con.prepareStatement(sqlItem)) {
                for (ItemCompra item : compra.getItens()) {

                    int produtoId = item.getProdutoId();
                    if (produtoId <= 0 && item.getProdutoNome() != null) {
                        produtoId = produtoDao.buscarOuCriarPorNome(con, item.getProdutoNome());
                    }

                    stmtItem.setInt(1, notaId);
                    stmtItem.setInt(2, produtoId);
                    stmtItem.setDouble(3, item.getQuantidade());
                    stmtItem.setDouble(4, item.getPrecoUnitario());
                    stmtItem.setDate(5, Date.valueOf(item.getDataCompra()));
                    stmtItem.addBatch();
                }
                stmtItem.executeBatch();
            }

            con.commit();
            return notaId;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
}