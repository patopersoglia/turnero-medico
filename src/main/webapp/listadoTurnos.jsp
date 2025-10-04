<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="Logica.Turno" %>
<%@ page import="Logica.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Lista de turnos médicos en el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Listado de Turnos</title>
    <link href="${pageContext.request.contextPath}/css/listado-turnos.css" rel="stylesheet" type="text/css">
</head>
<body>
    <%
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        String tipoUsuario = (String) request.getAttribute("tipoUsuario");
        if (tipoUsuario == null || (!tipoUsuario.equals("administrador") && !tipoUsuario.equals("medico") && !tipoUsuario.equals("paciente"))) {
            request.setAttribute("error", "Acceso denegado: rol de usuario no válido.");
            request.getRequestDispatcher("/login1.jsp").forward(request, response);
            return;
        }
    %>

    <div class="container">
        <h2>Listado de Turnos</h2>
        <% 
            String exito = (String) request.getAttribute("exito");
            String error = (String) request.getAttribute("error");
            if (exito != null) { 
        %>
            <div class="alert-success text-center" role="alert">
                <%= exito %>
            </div>
        <% } else if (error != null) { %>
            <div class="alert-danger text-center" role="alert">
                <%= error %>
            </div>
        <% } %>

        <table class="table">
            <thead>
                <tr>
                    <th scope="col">ID Turno</th>
                    <th scope="col">Fecha</th>
                    <th scope="col">Hora</th>
                    <% if (!"medico".equals(tipoUsuario)) { %>
                        <th scope="col">Médico</th>
                    <% } %>
                    <% if (!"paciente".equals(tipoUsuario)) { %>
                        <th scope="col">Paciente</th>
                    <% } %>
                    <th scope="col">Acción</th>
                </tr>
            </thead>
            <tbody>
                <%
                Object turnosObj = request.getAttribute("turnos");
                if (turnosObj == null) {
                %>
                <tr>
                    <td colspan="<%= (!"medico".equals(tipoUsuario) && !"paciente".equals(tipoUsuario)) ? 6 : 5 %>" class="center">Error: No se pudieron cargar los turnos.</td>
                </tr>
                <%
                } else if (turnosObj instanceof List) {
                    List<Turno> turnos = (List<Turno>) turnosObj;
                    if (!turnos.isEmpty()) {
                        for (Turno turno : turnos) {
                            String turnoClass = turno.esCancelable() ? "turno-cancelable" : "turno-no-cancelable";
                %>
                <tr class="<%= turnoClass %>">
                    <td><%= turno.getId_turno() %></td>
                    <td><%= turno.getFecha_turno() %></td>
                    <td><%= turno.getHora_Turno() %></td>
                    <% if (!"medico".equals(tipoUsuario)) { %>
                        <td><%= turno.getNombre_medico() %></td>
                    <% } %>
                    <% if (!"paciente".equals(tipoUsuario)) { %>
                        <td><%= turno.getNombre_paciente() %></td>
                    <% } %>
                    <td>
                        <form method="post" action="ListadoTurnosServlet">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id_turno" value="<%= turno.getId_turno() %>">
                            <button type="submit" class="btn-danger" <% if (!turno.esCancelable()) { %>disabled<% } %> title="Eliminar turno">Borrar Turno</button>
                        </form>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="<%= (!"medico".equals(tipoUsuario) && !"paciente".equals(tipoUsuario)) ? 6 : 5 %>" class="center">No hay turnos disponibles.</td>
                </tr>
                <%
                    }
                } else {
                %>
                <tr>
                    <td colspan="<%= (!"medico".equals(tipoUsuario) && !"paciente".equals(tipoUsuario)) ? 6 : 5 %>" class="center">No hay turnos disponibles.</td>
                </tr>
                <%
                }
                %>
            </tbody>
        </table>
    </div>

    <%
        String destino = "#"; // Valor por defecto
        if ("medico".equals(tipoUsuario)) {
            destino = "menuMedico.jsp";
        } else if ("paciente".equals(tipoUsuario)) {
            destino = "menuPaciente.jsp";
        } else if ("administrador".equals(tipoUsuario)) {
            destino = "menuAdministrador.jsp";
        }
    %>

    <div class="text-center">
        <a href="${pageContext.request.contextPath}/<%= destino %>" class="btn btn-secondary" title="Volver al menú principal">Volver</a>
    </div>
</body>
</html>