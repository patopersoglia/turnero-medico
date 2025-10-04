package Servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Logica.Conexion;
import Logica.Usuario;

@WebServlet("/ObtenerHorariosServlet")
public class ObtenerHorariosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ObtenerHorariosServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject error = new JSONObject();
            error.put("error", "Sesión no iniciada. Por favor, inicia sesión.");
            out.print(error);
            out.flush();
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (!"paciente".equalsIgnoreCase(usuario.getTipo())) {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject error = new JSONObject();
            error.put("error", "Acceso denegado: Debes ser un paciente para solicitar horarios.");
            out.print(error);
            out.flush();
            return;
        }

        String medicoIdStr = request.getParameter("medicoId");
        String fechaStr = request.getParameter("fecha");

        // Validar parámetros
        int medicoId;
        LocalDate fecha;
        try {
            medicoId = Integer.parseInt(medicoIdStr);
            fecha = LocalDate.parse(fechaStr);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error al parsear medicoId: {0}", e.getMessage());
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject error = new JSONObject();
            error.put("error", "ID de médico inválido.");
            out.print(error);
            out.flush();
            return;
        } catch (DateTimeParseException e) {
            LOGGER.log(Level.WARNING, "Error al parsear fecha: {0}", e.getMessage());
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject error = new JSONObject();
            error.put("error", "Formato de fecha inválido.");
            out.print(error);
            out.flush();
            return;
        }

        List<JSONObject> horarios = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = Conexion.conectar();
            if (con == null) {
                LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.");
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                JSONObject error = new JSONObject();
                error.put("error", "No se pudo conectar a la base de datos.");
                out.print(error);
                out.flush();
                return;
            }

            // Validar que el médico exista y tenga rol 'medico'
            String checkQuery = "SELECT 1 FROM usuario WHERE id_usuario = ? AND rol = 'medico' AND estado = 'habilitado'";
            try (PreparedStatement psCheck = con.prepareStatement(checkQuery)) {
                psCheck.setInt(1, medicoId);
                ResultSet rsCheck = psCheck.executeQuery();
                if (!rsCheck.next()) {
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    JSONObject error = new JSONObject();
                    error.put("error", "El médico seleccionado no es válido o no está habilitado.");
                    out.print(error);
                    out.flush();
                    return;
                }
            }

            // Obtener los horarios del médico desde informacion_medico
            String query = "SELECT hora_inicio, hora_fin FROM informacion_medico WHERE id_usuario = ?";
            ps = con.prepareStatement(query);
            ps.setInt(1, medicoId);
            rs = ps.executeQuery();

            if (rs.next()) {
                LocalTime horaInicio = LocalTime.parse(rs.getString("hora_inicio"));
                LocalTime horaFin = LocalTime.parse(rs.getString("hora_fin"));

                // Validar que hora_inicio sea anterior a hora_fin
                if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
                    response.setContentType("application/json");
                    PrintWriter out = response.getWriter();
                    JSONObject error = new JSONObject();
                    error.put("error", "Horario del médico inválido: la hora de inicio debe ser anterior a la hora de fin.");
                    out.print(error);
                    out.flush();
                    return;
                }

                // Verificar franjas ocupadas
                String queryOcupados = "SELECT hora_turno FROM turno WHERE id_medico = ? AND fecha_turno = ?";
                try (PreparedStatement psOcupados = con.prepareStatement(queryOcupados)) {
                    psOcupados.setInt(1, medicoId);
                    psOcupados.setString(2, fecha.toString());
                    ResultSet rsOcupados = psOcupados.executeQuery();
                    List<LocalTime> horariosOcupados = new ArrayList<>();
                    while (rsOcupados.next()) {
                        horariosOcupados.add(LocalTime.parse(rsOcupados.getString("hora_turno")));
                    }

                    LocalDate fechaActual = LocalDate.now();
                    LocalTime horaActual = LocalTime.now();

                    while (horaInicio.isBefore(horaFin)) {
                        if (!horariosOcupados.contains(horaInicio)) {
                            LocalTime siguienteHora = horaInicio.plusHours(1);
                            if (siguienteHora.isAfter(horaFin)) {
                                siguienteHora = horaFin;
                            }

                            // Validar que no se muestren horarios anteriores a la hora actual si la fecha es hoy
                            if (fecha.isAfter(fechaActual) || (fecha.isEqual(fechaActual) && horaInicio.isAfter(horaActual))) {
                                JSONObject horario = new JSONObject();
                                horario.put("hora_inicio", horaInicio.toString());
                                horario.put("hora_fin", siguienteHora.toString());
                                horarios.add(horario);
                            }
                        }
                        horaInicio = horaInicio.plusHours(1);
                    }
                }
            } else {
                LOGGER.log(Level.INFO, "No se encontraron horarios para el médico {0}", medicoId);
            }

            LOGGER.log(Level.INFO, "Horarios obtenidos exitosamente para médico {0} en fecha {1}", new Object[]{medicoId, fecha});
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(new JSONArray(horarios));
            out.flush();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la consulta SQL", e);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject error = new JSONObject();
            error.put("error", "Error al obtener los horarios. Intente de nuevo.");
            out.print(error);
            out.flush();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            JSONObject error = new JSONObject();
            error.put("error", "Ocurrió un error inesperado. Intente de nuevo.");
            out.print(error);
            out.flush();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}