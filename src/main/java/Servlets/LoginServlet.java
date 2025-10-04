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

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Verificar si ya hay una sesión activa
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuario") != null) {
            String rol = ((Usuario) session.getAttribute("usuario")).getTipo();
            String redirectUrl;
            switch (rol.toLowerCase()) {
                case "medico":
                    redirectUrl = request.getContextPath() + "/menuMedico.jsp";
                    break;
                case "paciente":
                    redirectUrl = request.getContextPath() + "/menuPaciente.jsp";
                    break;
                case "administrador":
                    redirectUrl = request.getContextPath() + "/menuAdministrador.jsp";
                    break;
                default:
                    session.invalidate();
                    redirectUrl = request.getContextPath() + "/login1.jsp";
                    request.setAttribute("error", "Rol de usuario no válido.");
                    request.getRequestDispatcher("login1.jsp").forward(request, response);
                    return;
            }
            response.sendRedirect(redirectUrl);
            return;
        }

        // Obtener los datos del formulario
        String nombreUsuario = request.getParameter("usuario");
        String contrasenia = request.getParameter("contrasenia");

        LOGGER.log(Level.INFO, "Intento de inicio de sesión - Usuario: {0}", nombreUsuario);

        // Validar que los campos no estén vacíos
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() ||
            contrasenia == null || contrasenia.trim().isEmpty()) {
            request.setAttribute("error", "Por favor, complete ambos campos.");
            request.getRequestDispatcher("login1.jsp").forward(request, response);
            return;
        }

        // Conectar con la base de datos
        Connection con;
        try {
            con = Conexion.conectar();
            if (con == null) {
                LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.");
                request.setAttribute("error", "No se pudo conectar a la base de datos. Intente de nuevo más tarde.");
                request.getRequestDispatcher("login1.jsp").forward(request, response);
                return;
            }

            // Validar si el usuario existe en la base de datos y obtener su rol
            String sql = "SELECT id_usuario, nombre_usuario, contrasenia, estado, rol FROM usuario WHERE nombre_usuario = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombreUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        LOGGER.log(Level.INFO, "Usuario no encontrado: {0}", nombreUsuario);
                        request.setAttribute("error", "Usuario no encontrado.");
                        request.getRequestDispatcher("login1.jsp").forward(request, response);
                        return;
                    }

                    // Obtener datos del usuario
                    int idUsuario = rs.getInt("id_usuario");
                    String usuarioBD = rs.getString("nombre_usuario");
                    String contraseniaBD = rs.getString("contrasenia");
                    String estado = rs.getString("estado");
                    String rol = rs.getString("rol");

                    // Validar contraseña
                    if (!contraseniaBD.equals(contrasenia)) {
                        LOGGER.log(Level.INFO, "Contraseña incorrecta para el usuario: {0}", nombreUsuario);
                        request.setAttribute("error", "Contraseña incorrecta.");
                        request.getRequestDispatcher("login1.jsp").forward(request, response);
                        return;
                    }

                    // Validar estado del usuario
                    if ("deshabilitado".equalsIgnoreCase(estado)) {
                        LOGGER.log(Level.INFO, "Usuario deshabilitado: {0}", nombreUsuario);
                        request.setAttribute("error", "Su cuenta está deshabilitada. Contacte al administrador.");
                        request.getRequestDispatcher("login1.jsp").forward(request, response);
                        return;
                    }

                    // Crear sesión y guardar usuario
                    session = request.getSession(true);
                    Usuario usuario = new Usuario();
                    usuario.setId_usuario(idUsuario);
                    usuario.setNombre_usuario(usuarioBD);
                    usuario.setTipo(rol);
                    usuario.setEstado(estado);
                    session.setAttribute("usuario", usuario);
                    LOGGER.log(Level.INFO, "Inicio de sesión exitoso para {0}. Rol: {1}",
                            new Object[]{nombreUsuario, rol});

                    // Redirigir según el rol
                    String redirectUrl;
                    switch (rol.toLowerCase()) {
                        case "medico":
                            redirectUrl = request.getContextPath() + "/menuMedico.jsp";
                            break;
                        case "paciente":
                            redirectUrl = request.getContextPath() + "/menuPaciente.jsp";
                            break;
                        case "administrador":
                            redirectUrl = request.getContextPath() + "/menuAdministrador.jsp";
                            break;
                        default:
                            session.invalidate();
                            redirectUrl = request.getContextPath() + "/login1.jsp";
                            request.setAttribute("error", "Rol de usuario no válido.");
                            request.getRequestDispatcher("login1.jsp").forward(request, response);
                            return;
                    }
                    response.sendRedirect(redirectUrl);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la consulta SQL", e);
            request.setAttribute("error", "Error al conectar con la base de datos. Intente de nuevo.");
            request.getRequestDispatcher("login1.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            request.setAttribute("error", "Ocurrió un error inesperado. Intente de nuevo.");
            request.getRequestDispatcher("login1.jsp").forward(request, response);
        }
    }
}