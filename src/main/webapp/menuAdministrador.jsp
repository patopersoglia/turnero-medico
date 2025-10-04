<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="jakarta.servlet.http.HttpSession" %>
<%@ page import="Logica.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Menú del administrador para el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Menú del Administrador</title>
    <link href="${pageContext.request.contextPath}/css/menu-admin.css" rel="stylesheet" type="text/css">
</head>
<body>
    <%
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuario") == null) {
            response.sendRedirect(request.getContextPath() + "/login1.jsp");
            return;
        }
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        if (usuario == null || !"administrador".equalsIgnoreCase(usuario.getTipo())) {
            request.setAttribute("error", "Acceso denegado. Debes ser administrador.");
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
        <a href="${pageContext.request.contextPath}/ListarUsuariosServlet" class="btn btn-primary" title="Ver el listado de usuarios">Listado de Usuarios</a>
        <a href="${pageContext.request.contextPath}/registro.jsp?origen=admin" class="btn btn-primary" title="Crear un nuevo usuario">Crear Usuario</a>
        <a href="${pageContext.request.contextPath}/menuAdministrador.jsp?accion=salir" class="btn btn-secondary" title="Cerrar sesión">Salir</a>
    </div>
</body>
</html>