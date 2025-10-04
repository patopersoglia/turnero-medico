<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="Logica.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Menú del médico para el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Menú del Médico</title>
    <link href="${pageContext.request.contextPath}/css/menu-medico.css" rel="stylesheet" type="text/css">
</head>
<body>
    <%
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        if (usuario == null || !"medico".equalsIgnoreCase(usuario.getTipo())) {
            request.setAttribute("error", "Acceso denegado: Debes ser un médico para acceder a esta página.");
            request.getRequestDispatcher("/login1.jsp").forward(request, response);
            return;
        }
        String nombreUsuario = usuario.getNombre_usuario();

        String accion = request.getParameter("accion");
        if ("salir".equals(accion)) {
            sesion.invalidate();
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }
    %>

    <% if (request.getAttribute("exito") != null) { %>
        <div class="alert-success text-center" role="alert">
            <%= request.getAttribute("exito") %>
        </div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <div class="alert-danger text-center" role="alert">
            <%= request.getAttribute("error") %>
        </div>
    <% } %>

    <div class="container">
        <h2>Hola, <%= nombreUsuario %>!</h2>
        <a href="${pageContext.request.contextPath}/ListadoTurnosServlet" class="btn btn-primary" title="Ver tus turnos">Listado de Turnos</a>
        <a href="${pageContext.request.contextPath}/menuMedico.jsp?accion=salir" class="btn btn-secondary" title="Cerrar sesión">Salir</a>
    </div>
</body>
</html>