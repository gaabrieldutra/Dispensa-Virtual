package com.dispensa.controller;

import com.dispensa.dao.RelatorioDAO;
import com.dispensa.model.GastoMensal;
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

@WebServlet("/api/relatorios/gasto-mensal-total")
public class GastoMensalTotalServlet extends HttpServlet {

    private final RelatorioDAO dao = new RelatorioDAO();
    private final Gson gson = new Gson();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            int usuarioId = Integer.parseInt(req.getParameter("usuarioId"));
            List<GastoMensal> lista = dao.gastoMensalTotal(usuarioId);
            String json = gson.toJson(lista);

            try (PrintWriter out = resp.getWriter()) {
                out.print(json);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"erro\": \"usuarioId inválido.\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}