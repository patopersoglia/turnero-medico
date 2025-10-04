package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Logica.Conexion;

@WebServlet("/CambiarEstadoUsuarioServlet")
public class CambiarEstadoUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idUsuario = Integer.parseInt(request.getParameter("id_usuario"));
        String estadoActual = request.getParameter("estado_actual");

        String nuevoEstado = "habilitado".equals(estadoActual) ? "deshabilitado" : "habilitado";

        try (Connection con = Conexion.conectar()) {
            String query = "UPDATE Usuario SET estado = ? WHERE id_usuario = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error al cambiar el estado: " + e.getMessage());
            return;
        }

        // Volver al listado
        response.sendRedirect("ListarUsuariosServlet");
    }
}
