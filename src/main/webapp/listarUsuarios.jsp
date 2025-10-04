<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="Logica.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Lista de usuarios para habilitar/deshabilitar en el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Lista de Usuarios</title>
    <link href="<%= request.getContextPath() %>/css/listar-usuarios.css" rel="stylesheet" type="text/css">
</head>
<body>
    <%-- Validar sesión y rol de administrador --%>
    <%
        jakarta.servlet.http.HttpSession sesion = request.getSession(false);
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
    %>

    <div class="container">
        <h2>Lista de Usuarios</h2>

        <%-- Mensaje de error --%>
        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
            <div class="alert-danger text-center" role="alert">
                <%= error %>
            </div>
        <% } %>

        <%-- Mensaje de éxito --%>
        <%
            String exito = (String) request.getAttribute("exito");
            if (exito != null) {
        %>
            <div class="alert-success text-center" role="alert">
                <%= exito %>
            </div>
        <% } %>

        <table class="table">
            <thead>
                <tr>
                    <th scope="col">Nombre y Apellido</th>
                    <th scope="col">Rol</th>
                    <th scope="col">Estado</th>
                    <th scope="col">Acciones</th>
                </tr>
            </thead>
            <tbody>
                <%
                    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
                    if (usuarios == null) {
                %>
                    <tr>
                        <td colspan="4">Error: No se pudieron cargar los usuarios.</td>
                    </tr>
                <% 
                    } else if (usuarios.isEmpty()) {
                %>
                    <tr>
                        <td colspan="4">No hay usuarios para mostrar.</td>
                    </tr>
                <% 
                    } else {
                        for (Usuario u : usuarios) {
                            String estadoClass = u.getEstado().equals("habilitado")
                                ? "estado-habilitado"
                                : "estado-deshabilitado";
                %>
                    <tr class="<%= estadoClass %>">
                        <td><%= u.getNombre_usuario() %></td>
                        <td><%= u.getTipo() != null ? u.getTipo() : "Desconocido" %></td>
                        <td><%= u.getEstado() %></td>
                        <td>
                            <%-- Formulario para habilitar/deshabilitar --%>
                            <form action="<%= request.getContextPath() %>/ActualizarEstadoUsuarioServlet"
                                  method="post" style="display:inline;">
                                <input type="hidden" name="id_usuario" value="<%= u.getId_usuario() %>">
                                <input type="hidden" name="estado"
                                       value="<%= u.getEstado().equals("habilitado")
                                           ? "deshabilitado" : "habilitado" %>">
                                <button type="submit" class="btn btn-action"
                                        title="<%= u.getEstado().equals("habilitado")
                                            ? "Deshabilitar usuario"
                                            : "Habilitar usuario" %>">
                                    <%= u.getEstado().equals("habilitado")
                                        ? "Deshabilitar"
                                        : "Habilitar" %>
                                </button>
                            </form>

                            <%-- Formulario para eliminar --%>
                            <form action="<%= request.getContextPath() %>/EliminarUsuarioServlet"
                                  method="post"
                                  style="display:inline;"
                                  onsubmit="return confirm('¿Estás seguro de que deseas eliminar este usuario?');">
                                <input type="hidden" name="id_usuario" value="<%= u.getId_usuario() %>">
                                <button type="submit" class="btn btn-danger" title="Eliminar usuario">
                                    Eliminar
                                </button>
                            </form>
                        </td>
                    </tr>
                <%
                        }
                    }
                %>
            </tbody>
        </table>

        <div class="text-center">
            <a href="<%= request.getContextPath() %>/menuAdministrador.jsp"
               class="btn btn-secondary"
               title="Volver al menú del administrador">
                Volver
            </a>
        </div>
    </div>
</body>
</html>
