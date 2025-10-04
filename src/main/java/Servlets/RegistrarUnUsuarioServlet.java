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
import Logica.Conexion;

@WebServlet("/RegistrarUnUsuarioServlet")
public class RegistrarUnUsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RegistrarUnUsuarioServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Obtener parámetros del formulario
        String nombreUsuario = request.getParameter("nombre_usuario");
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String dni = request.getParameter("dni");
        String telefono = request.getParameter("telefono");
        String tipo = request.getParameter("tipo");
        String contrasenia = request.getParameter("contrasenia");
        String fechaNac = request.getParameter("fecha_nac");
        String tieneOsStr = request.getParameter("tiene_os"); // Valor como cadena: 'si' o 'no'
        String especialidad = request.getParameter("especialidad");
        String horaInicio = request.getParameter("hora_inicio");
        String horaFin = request.getParameter("hora_fin");
        String origen = request.getParameter("origen");

        boolean esDesdeAdmin = "admin".equals(origen);

        // Convertir 'tiene_os' a entero: 'si' -> 1, 'no' -> 0
        int tieneOs = "si".equals(tieneOsStr) ? 1 : 0;

        // Depuración: Mostrar los valores recibidos
        LOGGER.log(Level.INFO, "Valores recibidos - hora_inicio: {0}, hora_fin: {1}", new Object[]{horaInicio, horaFin});

        Connection con = null;
        PreparedStatement psUsuario = null;
        PreparedStatement psPersona = null;
        PreparedStatement psPaciente = null;
        PreparedStatement psMedico = null;

        try {
            con = Conexion.conectar();
            if (con == null) {
                throw new SQLException("No se pudo conectar a la base de datos.");
            }

            con.setAutoCommit(false); // Iniciar transacción

            // Insertar en la tabla usuario
            String queryUsuario = "INSERT INTO usuario (nombre_usuario, contrasenia, rol, estado) VALUES (?, ?, ?, ?)";
            psUsuario = con.prepareStatement(queryUsuario, PreparedStatement.RETURN_GENERATED_KEYS);
            psUsuario.setString(1, nombreUsuario);
            psUsuario.setString(2, contrasenia);
            psUsuario.setString(3, tipo);
            psUsuario.setString(4, "habilitado");
            psUsuario.executeUpdate();

            // Obtener el ID generado para el usuario
            ResultSet rs = psUsuario.getGeneratedKeys();
            int idUsuario = -1;
            if (rs.next()) {
                idUsuario = rs.getInt(1);
            } else {
                throw new SQLException("No se pudo obtener el ID del usuario generado.");
            }

            // Insertar en la tabla persona
            String queryPersona = "INSERT INTO persona (id_usuario, nombre, apellido, dni, telefono, fecha_nac) VALUES (?, ?, ?, ?, ?, ?)";
            psPersona = con.prepareStatement(queryPersona);
            psPersona.setInt(1, idUsuario);
            psPersona.setString(2, nombre);
            psPersona.setString(3, apellido);
            psPersona.setString(4, dni);
            psPersona.setString(5, telefono);
            psPersona.setString(6, fechaNac);
            psPersona.executeUpdate();

            // Insertar datos adicionales según el tipo de usuario
            if ("paciente".equalsIgnoreCase(tipo)) {
                String queryPaciente = "INSERT INTO informacion_paciente (id_usuario, tiene_os) VALUES (?, ?)";
                psPaciente = con.prepareStatement(queryPaciente);
                psPaciente.setInt(1, idUsuario);
                psPaciente.setInt(2, tieneOs); // Usar setInt para insertar el valor entero
                psPaciente.executeUpdate();
            } else if ("medico".equalsIgnoreCase(tipo)) {
                String queryMedico = "INSERT INTO informacion_medico (id_usuario, especialidad, hora_inicio, hora_fin) VALUES (?, ?, ?, ?)";
                psMedico = con.prepareStatement(queryMedico);
                psMedico.setInt(1, idUsuario);
                psMedico.setString(2, especialidad != null ? especialidad : "");
                psMedico.setString(3, horaInicio); // Ahora es obligatorio, no debería ser null
                psMedico.setString(4, horaFin);    // Ahora es obligatorio, no debería ser null
                psMedico.executeUpdate();
            }

            con.commit(); // Confirmar transacción

            // Redirigir según el origen
            if (esDesdeAdmin) {
                request.setAttribute("exito", "Usuario registrado exitosamente.");
                request.getRequestDispatcher("/menuAdministrador.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/login1.jsp");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar usuario", e);
            try {
                if (con != null) con.rollback(); // Revertir transacción en caso de error
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error al hacer rollback", ex);
            }
            request.setAttribute("error", "Error al registrar el usuario: " + e.getMessage());
            request.getRequestDispatcher("/registro.jsp?origen=" + (esDesdeAdmin ? "admin" : "")).forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            request.setAttribute("error", "Ocurrió un error inesperado: " + e.getMessage());
            request.getRequestDispatcher("/registro.jsp?origen=" + (esDesdeAdmin ? "admin" : "")).forward(request, response);
        } finally {
            try {
                if (psUsuario != null) psUsuario.close();
                if (psPersona != null) psPersona.close();
                if (psPaciente != null) psPaciente.close();
                if (psMedico != null) psMedico.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al cerrar recursos", e);
            }
        }
    }
}