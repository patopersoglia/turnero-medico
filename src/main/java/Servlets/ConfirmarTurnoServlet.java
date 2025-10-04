package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
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

@WebServlet("/ConfirmarTurnoServlet")
public class ConfirmarTurnoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ConfirmarTurnoServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (!"paciente".equalsIgnoreCase(usuario.getTipo())) {
            request.setAttribute("error", "Acceso denegado. Debes ser un paciente para confirmar un turno.");
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            return;
        }

        // Obtener parámetros del formulario
        String medicoIdStr = request.getParameter("medico");
        String horario = request.getParameter("horario");
        String fecha = request.getParameter("fecha");

        // Validar parámetros
        if (medicoIdStr == null || medicoIdStr.trim().isEmpty() ||
            horario == null || horario.trim().isEmpty() ||
            fecha == null || fecha.trim().isEmpty()) {
            request.setAttribute("error", "Todos los campos son obligatorios.");
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            return;
        }

        int medicoId;
        LocalDate fechaTurno;
        LocalTime horaTurno;
        try {
            medicoId = Integer.parseInt(medicoIdStr);
            fechaTurno = LocalDate.parse(fecha);
            horaTurno = LocalTime.parse(horario);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error al parsear medicoId: {0}", e.getMessage());
            request.setAttribute("error", "ID de médico inválido.");
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al parsear fecha u horario: {0}", e.getMessage());
            request.setAttribute("error", "Formato de fecha u horario inválido.");
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            return;
        }

        // Validar que la fecha y hora no sean pasadas
        LocalDateTime fechaHoraTurno = LocalDateTime.of(fechaTurno, horaTurno);
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaHoraTurno.isBefore(ahora)) {
            LOGGER.log(Level.WARNING, "Intento de crear turno con fecha y hora pasada: {0} {1}", new Object[]{fechaTurno.toString(), horaTurno.toString()});
            request.setAttribute("error", "No se puede agendar un turno en una fecha y hora pasada.");
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            return;
        }

        // Usar id_usuario directamente como id_paciente
        int pacienteId = usuario.getId_usuario();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = Conexion.conectar();
            if (con == null) {
                LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.");
                request.setAttribute("error", "No se pudo conectar a la base de datos. Intente de nuevo.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
                return;
            }

            // Validar que el médico exista y tenga rol 'medico'
            String checkMedicoQuery = "SELECT 1 FROM usuario WHERE id_usuario = ? AND rol = 'medico' AND estado = 'habilitado'";
            ps = con.prepareStatement(checkMedicoQuery);
            ps.setInt(1, medicoId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                request.setAttribute("error", "El médico seleccionado no es válido o no está habilitado.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
                return;
            }

            // Obtener los horarios del médico desde informacion_medico
            String queryHorarios = "SELECT hora_inicio, hora_fin FROM informacion_medico WHERE id_usuario = ?";
            ps = con.prepareStatement(queryHorarios);
            ps.setInt(1, medicoId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                request.setAttribute("error", "No se encontraron horarios para el médico seleccionado.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
                return;
            }

            LocalTime horaInicio = LocalTime.parse(rs.getString("hora_inicio"));
            LocalTime horaFin = LocalTime.parse(rs.getString("hora_fin"));

            // Validar que el horario seleccionado esté dentro del rango del médico
            LocalTime horaTurnoFin = horaTurno.plusHours(1);
            LOGGER.log(Level.INFO, "Validando horario: {0} - {1}, rango del médico: {2} - {3}", 
                       new Object[]{horaTurno.toString(), horaTurnoFin.toString(), horaInicio.toString(), horaFin.toString()});
            if (horaTurno.isBefore(horaInicio) || horaTurnoFin.isAfter(horaFin)) {
                request.setAttribute("error", "El horario seleccionado no está dentro del rango de trabajo del médico.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
                return;
            }

            // Validar que el horario no esté ocupado
            String queryDisponibilidad = "SELECT COUNT(*) FROM turno WHERE id_medico = ? AND fecha_turno = ? AND hora_turno = ?";
            ps = con.prepareStatement(queryDisponibilidad);
            ps.setInt(1, medicoId);
            ps.setString(2, fecha);
            ps.setString(3, horario);
            rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                LOGGER.log(Level.INFO, "Número de turnos existentes para médico {0}, fecha {1}, horario {2}: {3}", 
                           new Object[]{medicoId, fecha, horario, count});
                if (count > 0) {
                    request.setAttribute("error", "El horario seleccionado no está disponible para este médico.");
                    request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
                    return;
                }
            }

            // No permitir que un paciente tenga turnos con médicos distintos el mismo día y horario
            String checkPacienteQuery = "SELECT 1 FROM turno WHERE id_paciente = ? AND fecha_turno = ? AND hora_turno = ?";
            ps = con.prepareStatement(checkPacienteQuery);
            ps.setInt(1, pacienteId);
            ps.setString(2, fecha);
            ps.setString(3, horario);
            rs = ps.executeQuery();
            if (rs.next()) {
                request.setAttribute("error", "Ya tienes un turno reservado en este día y horario con otro médico.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
                return;
            }

            // Insertar el turno
            String query = "INSERT INTO turno (id_medico, id_paciente, fecha_turno, hora_turno) VALUES (?, ?, ?, ?)";
            ps = con.prepareStatement(query);
            ps.setInt(1, medicoId);
            ps.setInt(2, pacienteId);
            ps.setString(3, fecha);
            ps.setString(4, horario);
            int resultTurno = ps.executeUpdate();

            if (resultTurno > 0) {
                LOGGER.log(Level.INFO, "Turno confirmado exitosamente para paciente {0} con médico {1}", 
                           new Object[]{pacienteId, medicoId});
                request.setAttribute("exito", "Turno confirmado exitosamente.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "Error al confirmar el turno.");
                request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la consulta SQL", e);
            if (e.getSQLState().equals("23000")) {
                request.setAttribute("error", "El médico ya tiene un turno reservado en ese día y horario.");
            } else {
                request.setAttribute("error", "Error al registrar el turno. Intente de nuevo.");
            }
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            request.setAttribute("error", "Ocurrió un error inesperado. Intente de nuevo.");
            request.getRequestDispatcher("/crearTurno.jsp").forward(request, response);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}