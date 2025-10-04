package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import Logica.Conexion;
import Logica.Usuario;

@WebServlet("/ListarUsuariosServlet")
public class ListarUsuariosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String error = request.getParameter("error");
        String exito = request.getParameter("exito");
        if (error != null) {
            request.setAttribute("error", error);
        }
        if (exito != null) {
            request.setAttribute("exito", exito);
        }

        List<Usuario> usuarios = new ArrayList<>();
        Connection con = null;
        try {
            con = Conexion.conectar();
            if (con == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }

            String query = """
                SELECT u.id_usuario,
                       u.rol AS rol_usuario,
                       u.estado,
                       p.nombre,
                       p.apellido
                FROM usuario u
                JOIN persona p ON u.id_usuario = p.id_usuario
                """;

            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId_usuario(rs.getInt("id_usuario"));
                    usuario.setNombre_usuario(rs.getString("nombre") + " " + rs.getString("apellido"));
                    usuario.setEstado(rs.getString("estado"));
                    usuario.setTipo(rs.getString("rol_usuario"));
                    usuarios.add(usuario);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Error: No se pudieron cargar los usuarios");
            usuarios = new ArrayList<>();
        } finally {
            if (con != null) {
                try { con.close(); }
                catch (SQLException e) { e.printStackTrace(); }
            }
        }

        request.setAttribute("usuarios", usuarios);
        request.getRequestDispatcher("/listarUsuarios.jsp")
               .forward(request, response);
    }
}
