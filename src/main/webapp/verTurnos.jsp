<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="Logica.Turno" %>
<%@ page import="Logica.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Visualización de turnos futuros en el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Ver Turnos</title>
    <link href="${pageContext.request.contextPath}/css/ver-turnos.css" rel="stylesheet" type="text/css">
</head>
<body>
    <%
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        String tipoUsuario = usuario.getTipo();
        if (tipoUsuario == null || (!tipoUsuario.equals("administrador") && !tipoUsuario.equals("medico") && !tipoUsuario.equals("paciente"))) {
            request.setAttribute("error", "Acceso denegado: rol de usuario no válido.");
            request.getRequestDispatcher("/login1.jsp").forward(request, response);
            return;
        }
    %>

    <div class="container">
        <h2>Ver Turnos Futuros</h2>
        <% 
            String error = (String) request.getAttribute("error");
            if (error != null) { 
        %>
            <div class="alert-danger text-center" role="alert">
                <%= error %>
            </div>
        <% } %>

        <table>
            <thead>
                <tr>
                    <th scope="col">ID Turno</th>
                    <th scope="col">Nombre Paciente</th>
                    <th scope="col">Apellido Paciente</th>
                    <th scope="col">Nombre Médico</th>
                    <th scope="col">Apellido Médico</th>
                    <th scope="col">Fecha</th>
                    <th scope="col">Hora</th>
                </tr>
            </thead>
            <tbody>
                <%
                    Object turnosObj = request.getAttribute("turnos");
                    if (turnosObj == null) {
                %>
                    <tr>
                        <td colspan="7" class="center">Error: No se pudieron cargar los turnos.</td>
                    </tr>
                <%
                    } else if (turnosObj instanceof List) {
                        List<Turno> turnos = (List<Turno>) turnosObj;
                        if (!turnos.isEmpty()) {
                            for (Turno turno : turnos) {
                %>
                    <tr>
                        <td><%= turno.getId_turno() %></td>
                        <td><%= turno.getNombre_paciente().split(" ")[0] %></td>
                        <td><%= turno.getNombre_paciente().split(" ").length > 1 ? turno.getNombre_paciente().split(" ")[1] : "" %></td>
                        <td><%= turno.getNombre_medico().split(" ")[0] %></td>
                        <td><%= turno.getNombre_medico().split(" ").length > 1 ? turno.getNombre_medico().split(" ")[1] : "" %></td>
                        <td><%= turno.getFecha_turno() %></td>
                        <td><%= turno.getHora_Turno() %></td>
                    </tr>
                <%
                            }
                        } else {
                %>
                    <tr>
                        <td colspan="7" class="center">No hay turnos futuros disponibles.</td>
                    </tr>
                <%
                        }
                    } else {
                %>
                    <tr>
                        <td colspan="7" class="center">No hay turnos futuros disponibles.</td>
                    </tr>
                <%
                    }
                %>
            </tbody>
        </table>
    </div>

    <%
        String destino = "#";
        if ("medico".equals(tipoUsuario)) {
            destino = "menuMedico.jsp";
        } else if ("paciente".equals(tipoUsuario)) {
            destino = "menuPaciente.jsp";
        } else if ("administrador".equals(tipoUsuario)) {
            destino = "menuAdministrador.jsp";
        }
    %>

    <div class="button-container">
        <a href="<%= destino %>" class="btn-secondary" title="Volver al menú principal">Volver</a>
    </div>
</body>
</html>