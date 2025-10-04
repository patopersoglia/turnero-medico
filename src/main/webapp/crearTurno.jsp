<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*" %>
<%@ page import="Logica.Conexion" %>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="Logica.Usuario" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Calendar" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Crear un turno médico en el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Crear Turno</title>
    <link href="${pageContext.request.contextPath}/css/crear-turno.css" rel="stylesheet" type="text/css">
    <!-- Incluir flatpickr CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <!-- Incluir flatpickr JS -->
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <!-- Incluir localización en español para flatpickr -->
    <script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/es.js"></script>
</head>
<body>
    <%
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        if (usuario == null || !"paciente".equalsIgnoreCase(usuario.getTipo())) {
            request.setAttribute("error", "Acceso denegado: Debes ser un paciente para crear un turno.");
            request.getRequestDispatcher("/login1.jsp").forward(request, response);
            return;
        }

        // Calcular la fecha mínima (hoy) y máxima (fin de año)
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String minDate = sdf.format(today);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.MONTH, 11); // Diciembre
        calendar.set(Calendar.DAY_OF_MONTH, 31); // 31 de diciembre
        String maxDate = sdf.format(calendar.getTime());
    %>

    <div class="container">
        <h2>Crear Turno</h2>
        <% 
            String error = (String) request.getAttribute("error");
            if (error != null) { 
        %>
            <div class="alert-danger text-center" role="alert">
                <%= error %>
            </div>
        <% } %>
        <% 
            String exito = (String) request.getAttribute("exito");
            if (exito != null) { 
        %>
            <div class="alert-success text-center" role="alert">
                <%= exito %>
            </div>
        <% } %>

        <form id="formulario-turno" action="${pageContext.request.contextPath}/ConfirmarTurnoServlet" method="post">
            <div class="form-group">
                <label for="medico">Médico:</label>
                <select id="medico" name="medico" required>
                    <option value="">Seleccione un médico</option>
                    <%
                        Connection con = null;
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        try {
                            con = Conexion.conectar();
                            if (con == null) {
                                throw new SQLException("No se pudo conectar a la base de datos.");
                            }
                            String query = "SELECT u.id_usuario, p.nombre, p.apellido, im.especialidad " +
                                           "FROM usuario u " +
                                           "JOIN persona p ON u.id_usuario = p.id_usuario " +
                                           "JOIN informacion_medico im ON u.id_usuario = im.id_usuario " +
                                           "WHERE u.rol = 'medico' AND u.estado = 'habilitado'";
                            ps = con.prepareStatement(query);
                            rs = ps.executeQuery();
                            while (rs.next()) {
                                int idUsuario = rs.getInt("id_usuario");
                                String nombre = rs.getString("nombre");
                                String apellido = rs.getString("apellido");
                                String especialidad = rs.getString("especialidad");
                    %>
                    <option value="<%= idUsuario %>">
                        <%= nombre %> <%= apellido %> (<%= especialidad %>)
                    </option>
                    <%
                            }
                            if (!rs.isBeforeFirst()) { // Si no hay médicos
                    %>
                    <option value="">No hay médicos disponibles</option>
                    <%
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            request.setAttribute("error", "Error al cargar la lista de médicos: " + e.getMessage());
                    %>
                    <option value="">Error al cargar médicos</option>
                    <%
                        } finally {
                            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
                            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
                        }
                    %>
                </select>
            </div>

            <div class="form-group">
                <label for="fecha_display">Fecha:</label>
                <input type="text" id="fecha_display" name="fecha_display" required placeholder="dd/mm/aaaa">
                <input type="hidden" id="fecha" name="fecha">
            </div>

            <div class="form-group">
                <label for="horario">Horario:</label>
                <select id="horario" name="horario" required>
                    <option value="">Seleccione un horario</option>
                </select>
            </div>

            <button type="submit" id="submit-btn" class="btn btn-primary">Confirmar Turno</button>
            <a href="${pageContext.request.contextPath}/menuPaciente.jsp" class="btn btn-secondary" title="Volver al menú principal">Volver</a>
        </form>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Configurar flatpickr para el campo de fecha
            flatpickr("#fecha_display", {
                dateFormat: "d/m/Y",
                maxDate: "2007-05-27",
                locale: "es",
                onChange: function(selectedDates, dateStr, instance) {
                    if (selectedDates.length > 0) {
                        var date = selectedDates[0];
                        var formattedDate = date.getFullYear() + "-" + 
                                           String(date.getMonth() + 1).padStart(2, '0') + "-" + 
                                           String(date.getDate()).padStart(2, '0');
                        document.getElementById("fecha").value = formattedDate;
                        actualizarHorarios();
                    }
                }
            });

            // Función para actualizar horarios
            function actualizarHorarios() {
                const medicoId = document.getElementById('medico').value;
                const fecha = document.getElementById('fecha').value;
                const horarioSelect = document.getElementById('horario');

                if (medicoId && fecha) {
                    fetch('ObtenerHorariosServlet?medicoId=' + medicoId + '&fecha=' + fecha)
                        .then(response => response.json())
                        .then(data => {
                            if (data.error) {
                                horarioSelect.innerHTML = '<option value="">' + data.error + '</option>';
                            } else if (data.length === 0) {
                                horarioSelect.innerHTML = '<option value="">No hay horarios disponibles</option>';
                            } else {
                                horarioSelect.innerHTML = '<option value="">Seleccione un horario</option>';
                                data.forEach(horario => {
                                    horarioSelect.innerHTML += '<option value="' + horario.hora_inicio + '">' + horario.hora_inicio + ' - ' + horario.hora_fin + '</option>';
                                });
                            }
                        })
                        .catch(error => {
                            console.error('Error al cargar horarios:', error);
                            horarioSelect.innerHTML = '<option value="">Error al cargar horarios</option>';
                        });
                } else {
                    horarioSelect.innerHTML = '<option value="">Seleccione un horario</option>';
                }
            }

            // Actualizar horarios cuando se cambia el médico
            document.getElementById('medico').addEventListener('change', function() {
                const fecha = document.getElementById('fecha').value;
                if (fecha) {
                    actualizarHorarios();
                }
            });

            // Función para validar fecha y hora
            function validarFechaYHora() {
                const fechaSeleccionadaStr = document.getElementById('fecha').value; // Formato: YYYY-MM-DD
                const horaSeleccionadaStr = document.getElementById('horario').value; // Formato: HH:MM:SS

                if (!fechaSeleccionadaStr || !horaSeleccionadaStr) {
                    alert("Por favor, seleccione una fecha y un horario.");
                    return false;
                }

                // Combinar fecha y hora en un objeto Date
                const [year, month, day] = fechaSeleccionadaStr.split('-');
                const [hour, minute, second] = horaSeleccionadaStr.split(':');
                const fechaHoraSeleccionada = new Date(year, month - 1, day, hour, minute, second);

                // Obtener la fecha y hora actual
                const ahora = new Date();

                // Comparar
                if (fechaHoraSeleccionada <= ahora) {
                    alert("No se puede seleccionar una fecha y hora pasada. Por favor, elija una fecha y hora futura.");
                    return false;
                }

                return true;
            }

            // Agregar validación al formulario
            document.getElementById('formulario-turno').addEventListener('submit', function(event) {
                if (!validarFechaYHora()) {
                    event.preventDefault();
                }
            });
        });
    </script>
</body>
</html>