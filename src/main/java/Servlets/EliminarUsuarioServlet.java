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

@WebServlet("/EliminarUsuarioServlet")
public class EliminarUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(EliminarUsuarioServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("error", "Método no permitido. Use el formulario para eliminar usuarios.");
        request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
    }

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

        String idUsuarioStr = request.getParameter("id_usuario");
        int idUsuario;
        try {
            idUsuario = Integer.parseInt(idUsuarioStr);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error al parsear id_usuario: {0}", e.getMessage());
            request.setAttribute("error", "ID de usuario inválido.");
            request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
            return;
        }

        // No permitir que un administrador se elimine a sí mismo
        if (idUsuario == usuario.getId_usuario()) {
            request.setAttribute("error", "No puedes eliminarte a ti mismo.");
            request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
            return;
        }

        Connection con = null;
        try {
            con = Conexion.conectar();
            if (con == null) {
                LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.");
                request.setAttribute("error", "No se pudo conectar a la base de datos. Intente de nuevo.");
                request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
                return;
            }

            // Verificar si el usuario existe y obtener su rol
            String queryUsuario = "SELECT rol FROM usuario WHERE id_usuario = ?";
            try (PreparedStatement psUsuario = con.prepareStatement(queryUsuario)) {
                psUsuario.setInt(1, idUsuario);
                ResultSet rs = psUsuario.executeQuery();
                if (rs.next()) {
                    String rol = rs.getString("rol");
                    if ("medico".equalsIgnoreCase(rol)) {
                        // Verificar si el médico tiene turnos pendientes
                        String queryTurnos = "SELECT COUNT(*) FROM turno WHERE id_medico = ? AND fecha_turno >= CURDATE()";
                        try (PreparedStatement psTurnos = con.prepareStatement(queryTurnos)) {
                            psTurnos.setInt(1, idUsuario);
                            ResultSet rsTurnos = psTurnos.executeQuery();
                            if (rsTurnos.next() && rsTurnos.getInt(1) > 0) {
                                request.setAttribute("error", "No se puede eliminar el médico porque tiene turnos pendientes.");
                                request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
                                return;
                            }
                        }
                    }
                } else {
                    LOGGER.log(Level.WARNING, "No se encontró el usuario con id {0}", idUsuario);
                    request.setAttribute("error", "No se encontró el usuario para eliminar.");
                    request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
                    return;
                }
            }

            // Eliminar datos asociados al usuario en el orden correcto
            // 1. Eliminar turnos asociados al usuario (como paciente o médico)
            try (PreparedStatement psTurnos = con.prepareStatement(
                    "DELETE FROM turno WHERE id_paciente = ? OR id_medico = ?")) {
                psTurnos.setInt(1, idUsuario);
                psTurnos.setInt(2, idUsuario);
                psTurnos.executeUpdate();
            }

            // 2. Eliminar horarios asociados al usuario (si es médico)
            try (PreparedStatement psHorario = con.prepareStatement(
                    "DELETE FROM horario WHERE id_usuario = ?")) {
                psHorario.setInt(1, idUsuario);
                psHorario.executeUpdate();
            }

            // 3. Eliminar información específica del paciente o médico
            try (PreparedStatement psPaciente = con.prepareStatement(
                    "DELETE FROM informacion_paciente WHERE id_usuario = ?")) {
                psPaciente.setInt(1, idUsuario);
                psPaciente.executeUpdate();
            }

            try (PreparedStatement psMedico = con.prepareStatement(
                    "DELETE FROM informacion_medico WHERE id_usuario = ?")) {
                psMedico.setInt(1, idUsuario);
                psMedico.executeUpdate();
            }

            // 4. Eliminar entrada de persona
            try (PreparedStatement psPersona = con.prepareStatement(
                    "DELETE FROM persona WHERE id_usuario = ?")) {
                psPersona.setInt(1, idUsuario);
                psPersona.executeUpdate();
            }

            // 5. Eliminar el usuario de la tabla usuario
            try (PreparedStatement psUsuario = con.prepareStatement(
                    "DELETE FROM usuario WHERE id_usuario = ?")) {
                psUsuario.setInt(1, idUsuario);
                int filasAfectadas = psUsuario.executeUpdate();
                if (filasAfectadas > 0) {
                    LOGGER.log(Level.INFO, "Usuario {0} eliminado exitosamente", idUsuario);
                    request.setAttribute("exito", "Usuario eliminado con éxito.");
                } else {
                    LOGGER.log(Level.WARNING, "No se encontró el usuario con id {0}", idUsuario);
                    request.setAttribute("error", "No se encontró el usuario para eliminar.");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la consulta SQL", e);
            request.setAttribute("error", "Error al eliminar el usuario. Intente de nuevo.");
            request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
            return;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            request.setAttribute("error", "Ocurrió un error inesperado. Intente de nuevo.");
            request.getRequestDispatcher("/ListarUsuariosServlet").forward(request, response);
            return;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error al cerrar la conexión", e);
                }
            }
        }

        // Redirigir a ListarUsuariosServlet para refrescar la lista
        response.sendRedirect(request.getContextPath() + "/ListarUsuariosServlet");
    }
}