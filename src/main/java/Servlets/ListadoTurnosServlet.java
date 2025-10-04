package Servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Logica.Conexion;
import Logica.Turno;
import Logica.Usuario;

@WebServlet("/ListadoTurnosServlet")
public class ListadoTurnosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ListadoTurnosServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String tipoUsuario = usuario.getTipo();
        if (tipoUsuario == null || (!tipoUsuario.equals("administrador") && !tipoUsuario.equals("medico") && !tipoUsuario.equals("paciente"))) {
            request.setAttribute("error", "Acceso denegado: rol de usuario no válido.");
            request.getRequestDispatcher("/login1.jsp").forward(request, response);
            return;
        }

        List<Turno> turnos = new ArrayList<>();
        int entityId = usuario.getId_usuario(); // Usar directamente el ID del usuario

        Connection con;
        try {
            con = Conexion.conectar();
            if (con == null) {
                LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos.");
                request.setAttribute("error", "No se pudo conectar a la base de datos. Intente de nuevo.");
                request.getRequestDispatcher("/listadoTurnos.jsp").forward(request, response);
                return;
            }

            String queryTurnos;
            PreparedStatement psTurnos;

            if ("administrador".equals(tipoUsuario)) {
                queryTurnos = """
                    SELECT t.id_turno, t.fecha_turno, t.hora_turno,
                           pp.nombre AS nombre_paciente, pp.apellido AS apellido_paciente,
                           pm.nombre AS nombre_medico, pm.apellido AS apellido_medico
                    FROM turno t
                    LEFT JOIN usuario up ON t.id_paciente = up.id_usuario
                    LEFT JOIN persona pp ON up.id_usuario = pp.id_usuario
                    LEFT JOIN usuario um ON t.id_medico = um.id_usuario
                    LEFT JOIN persona pm ON um.id_usuario = pm.id_usuario
                """;
                psTurnos = con.prepareStatement(queryTurnos);

            } else if ("medico".equals(tipoUsuario)) {
                queryTurnos = """
                    SELECT t.id_turno, t.fecha_turno, t.hora_turno,
                           pp.nombre AS nombre_paciente, pp.apellido AS apellido_paciente,
                           '' AS nombre_medico, '' AS apellido_medico
                    FROM turno t
                    JOIN usuario up ON t.id_paciente = up.id_usuario
                    JOIN persona pp ON up.id_usuario = pp.id_usuario
                    WHERE t.id_medico = ?
                """;
                psTurnos = con.prepareStatement(queryTurnos);
                psTurnos.setInt(1, entityId);

            } else { // Paciente
                queryTurnos = """
                    SELECT t.id_turno, t.fecha_turno, t.hora_turno,
                           pm.nombre AS nombre_medico, pm.apellido AS apellido_medico,
                           '' AS nombre_paciente, '' AS apellido_paciente
                    FROM turno t
                    JOIN usuario um ON t.id_medico = um.id_usuario
                    JOIN persona pm ON um.id_usuario = pm.id_usuario
                    WHERE t.id_paciente = ?
                """;
                psTurnos = con.prepareStatement(queryTurnos);
                psTurnos.setInt(1, entityId);
            }

            try (ResultSet rsTurnos = psTurnos.executeQuery()) {
                while (rsTurnos.next()) {
                    Turno turno = new Turno();
                    turno.setId_turno(rsTurnos.getInt("id_turno"));
                    turno.setFecha_turno(rsTurnos.getDate("fecha_turno"));
                    turno.setHora_Turno(rsTurnos.getString("hora_turno"));

                    String nombrePaciente = rsTurnos.getString("nombre_paciente");
                    String apellidoPaciente = rsTurnos.getString("apellido_paciente");
                    String nombreMedico = rsTurnos.getString("nombre_medico");
                    String apellidoMedico = rsTurnos.getString("apellido_medico");

                    turno.setNombre_paciente((nombrePaciente != null && !nombrePaciente.isEmpty()) ? nombrePaciente + " " + apellidoPaciente : "Desconocido");
                    turno.setNombre_medico((nombreMedico != null && !nombreMedico.isEmpty()) ? nombreMedico + " " + apellidoMedico : "Desconocido");

                    turnos.add(turno);
                }
            }

            LOGGER.log(Level.INFO, "Turnos obtenidos exitosamente para usuario {0} (rol: {1})", 
                    new Object[]{usuario.getId_usuario(), tipoUsuario});
            request.setAttribute("turnos", turnos);
            request.setAttribute("tipoUsuario", tipoUsuario);
            request.getRequestDispatcher("/listadoTurnos.jsp").forward(request, response);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en la consulta SQL", e);
            request.setAttribute("error", "Error al obtener los turnos. Intente de nuevo.");
            request.getRequestDispatcher("/listadoTurnos.jsp").forward(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            request.setAttribute("error", "Ocurrió un error inesperado. Intente de nuevo.");
            request.getRequestDispatcher("/listadoTurnos.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            String idTurnoParam = request.getParameter("id_turno");
            if (idTurnoParam != null && !idTurnoParam.isEmpty()) {
                try (Connection con = Conexion.conectar()) {
                    int idTurno = Integer.parseInt(idTurnoParam);
                    String query = "DELETE FROM Turno WHERE id_turno = ?";
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setInt(1, idTurno);
                        int rowsAffected = ps.executeUpdate();
                        if (rowsAffected > 0) {
                            request.setAttribute("exito", "Turno eliminado exitosamente.");
                        } else {
                            request.setAttribute("error", "No se pudo eliminar el turno.");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al eliminar turno", e);
                    request.setAttribute("error", "Error al eliminar el turno: " + e.getMessage());
                }
            } else {
                request.setAttribute("error", "ID de turno no válido.");
            }
        }
        doGet(request, response); // Refrescar la lista de turnos y mostrar el mensaje
    }
}