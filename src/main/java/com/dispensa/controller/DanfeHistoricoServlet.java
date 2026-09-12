package com.dispensa.controller;

import com.dispensa.dao.NotaFiscalDAO;
import com.dispensa.model.NotaFiscal;
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

@WebServlet("/api/danfe/historico/*")
public class DanfeHistoricoServlet extends HttpServlet {

    private final NotaFiscalDAO dao = new NotaFiscalDAO();
    private final Gson gson = new Gson();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo(); // vem como "/1"
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"erro\": \"ID do usuário não informado na URL.\"}");
            return;
        }

        try {
            int usuarioId = Integer.parseInt(pathInfo.substring(1)); // remove a "/" inicial

            List<NotaFiscal> notas = dao.listarHistorico(usuarioId);
            String json = gson.toJson(notas);

            try (PrintWriter out = resp.getWriter()) {
                out.print(json);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"erro\": \"ID de usuário inválido.\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
