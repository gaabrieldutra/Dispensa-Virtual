package com.dispensa.controller;

import com.dispensa.dao.ProdutoDAO;
import com.dispensa.model.Produto;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/produtos/buscar")
public class ProdutoServlet extends HttpServlet {

    private final ProdutoDAO dao = new ProdutoDAO();
    private final Gson gson = new Gson();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String termo = req.getParameter("termo");
        if (termo == null) termo = "";

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<Produto> produtos = dao.buscarPorTermo(termo);
            String json = gson.toJson(produtos);
            try (PrintWriter out = resp.getWriter()) {
                out.print(json);
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    String idParam = req.getParameter("id");

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    if (idParam == null) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }

    try {
        int id = Integer.parseInt(idParam);
        boolean sucesso = dao.deletar(id);

        if (sucesso) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
