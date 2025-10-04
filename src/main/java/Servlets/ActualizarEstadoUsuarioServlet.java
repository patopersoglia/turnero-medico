package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Logica.Conexion;
import Logica.Usuario;

@WebServlet("/ActualizarEstadoUsuarioServlet")
public class ActualizarEstadoUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ActualizarEstadoUsuarioServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (!"administrador".equalsIgnoreCase(usuario.getTipo())) {
            request.setAttribute("error", "Acceso denegado: Debes ser administrador para realizar esta acción.");
            request.getRequestDispatcher("/login1.jsp").forward(request, response);
            return;
        }

        // Obtener parámetros del formulario
        String idUsuarioStr = request.getParameter("id_usuario");
        String nuevoEstado = request.getParameter("estado");

        // Validar parámetros
        if (idUsuarioStr == null || idUsuarioStr.trim().isEmpty() || nuevoEstado == null || 
            (!nuevoEstado.equals("habilitado") && !nuevoEstado.equals("deshabilitado"))) {
            LOGGER.log(Level.WARNING, "Parámetros inválidos: id_usuario={0}, estado={1}", 
                    new Object[]{idUsuarioStr, nuevoEstado});
            response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=Parámetros inválidos.");
            return;
        }

        int idUsuario;
        try {
            idUsuario = Integer.parseInt(idUsuarioStr);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error al parsear id_usuario: {0}", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=ID de usuario inválido.");
            return;
        }

        // No permitir que un administrador se deshabilite a sí mismo
        if (idUsuario == usuario.getId_usuario()) {
            response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=No puedes cambiar tu propio estado.");
            return;
        }

        Connection con = null;
        try {
            con = Conexion.conectar();
            if (con == null) {
                LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.");
                response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=No se pudo conectar a la base de datos.");
                return;
            }

            // Verificar si el usuario es un médico y se intenta deshabilitar
            if ("deshabilitado".equals(nuevoEstado)) {
                String queryRol = "SELECT rol FROM usuario WHERE id_usuario = ?";
                try (PreparedStatement psRol = con.prepareStatement(queryRol)) {
                    psRol.setInt(1, idUsuario);
                    ResultSet rs = psRol.executeQuery();
                    if (rs.next()) {
                        String rol = rs.getString("rol");
                        if ("medico".equalsIgnoreCase(rol)) {
                            // Verificar si tiene turnos pendientes
                            String queryTurnos = "SELECT COUNT(*) FROM turno WHERE id_medico = ? AND fecha_turno >= CURDATE()";
                            try (PreparedStatement psTurnos = con.prepareStatement(queryTurnos)) {
                                psTurnos.setInt(1, idUsuario);
                                ResultSet rsTurnos = psTurnos.executeQuery();
                                if (rsTurnos.next() && rsTurnos.getInt(1) > 0) {
                                    response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=No se puede deshabilitar al medico porque tiene turnos pendientes.");
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            // Actualizar el estado del usuario
            try (PreparedStatement ps = con.prepareStatement("UPDATE usuario SET estado = ? WHERE id_usuario = ?")) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idUsuario);
                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    LOGGER.log(Level.INFO, "Estado del usuario {0} actualizado a {1} exitosamente", 
                            new Object[]{idUsuario, nuevoEstado});
                    response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?exito=Estado del usuario actualizado con éxito.");
                } else {
                    LOGGER.log(Level.WARNING, "No se encontró el usuario con id {0}", idUsuario);
                    response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=No se encontró el usuario para actualizar.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la consulta SQL", e);
            response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=Error al actualizar el estado del usuario.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet?error=Ocurrió un error inesperado.");
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar la conexión", e);
                }
            }
        }
    }
}