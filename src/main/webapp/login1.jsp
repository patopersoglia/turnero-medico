<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Página de inicio de sesión para el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Iniciar Sesión - Turnero Médico</title>
    <link href="${pageContext.request.contextPath}/css/login.css" rel="stylesheet" type="text/css">
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-xl-10 col-lg-12 col-md-9">
                <div class="card">
                    <div class="card-body">
                        <div class="text-center">
                            <h1 class="h4 text-gray-900 mb-4">Bienvenido</h1>
                        </div>
                        <% if (request.getAttribute("exito") != null) { %>
                            <div class="alert-success text-center" role="alert">
                                <%= request.getAttribute("exito") %>
                            </div>
                        <% } %>
                        <% String mensajeError = (String) request.getAttribute("error"); %>
                        <% if (mensajeError != null) { %>
                            <div class="alert-danger text-center" role="alert">
                                <%= mensajeError %>
                            </div>
                        <% } %>

                        <form class="user" method="post" action="${pageContext.request.contextPath}/LoginServlet">
                            <div class="form-group">
                                <label for="usuario">Usuario:</label>
                                <input type="text" name="usuario" class="form-control form-control-user"
                                    id="usuario" placeholder="Ingrese su usuario..." required
                                    minlength="3" maxlength="50">
                            </div>
                            <div class="form-group">
                                <label for="contrasenia">Contraseña:</label>
                                <input type="password" name="contrasenia" class="form-control form-control-user"
                                    id="contrasenia" placeholder="Ingrese su contraseña..." required
                                    minlength="3" maxlength="50">
                            </div>
                            <button type="submit" class="btn btn-primary btn-user btn-block">
                                Iniciar Sesión
                            </button>
                            <hr>
                        </form>
                        <div class="text-center">
                            <a class="small" href="${pageContext.request.contextPath}/registro.jsp?origen=login">Crear una cuenta</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>