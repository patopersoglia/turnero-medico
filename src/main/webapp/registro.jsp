<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Formulario de registro de usuarios para el sistema Turnero Médico">
    <meta name="author" content="Tu Nombre">
    <title>Registro de Usuario</title>
    <link href="${pageContext.request.contextPath}/css/registro.css" rel="stylesheet" type="text/css">
    <!-- Incluir flatpickr CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
    <!-- Incluir flatpickr JS -->
    <script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <!-- Incluir localización en español para flatpickr -->
    <script src="https://cdn.jsdelivr.net/npm/flatpickr/dist/l10n/es.js"></script>
    <script>
        function mostrarCamposMedico() {
            var tipoUsuario = document.getElementById("tipo").value;
            var camposMedico = document.getElementById("camposMedico");
            var campoObraSocial = document.getElementById("campoObraSocial");
            var especialidad = document.getElementById("especialidad");
            var horaInicio = document.getElementById("hora_inicio");
            var horaFin = document.getElementById("hora_fin");

            if (tipoUsuario === "medico") {
                camposMedico.classList.remove("hidden");
                campoObraSocial.classList.add("hidden");
                especialidad.setAttribute("required", "required");
                horaInicio.setAttribute("required", "required");
                horaFin.setAttribute("required", "required");
            } else {
                camposMedico.classList.add("hidden");
                campoObraSocial.classList.remove("hidden");
                especialidad.removeAttribute("required");
                horaInicio.removeAttribute("required");
                horaFin.removeAttribute("required");
            }
        }

        // Validar formulario antes de enviar
        function validarFormulario() {
            var tipoUsuario = document.getElementById("tipo").value;
            var dni = document.getElementById("dni").value;
            var telefono = document.getElementById("telefono").value;
            var horaInicio = document.getElementById("hora_inicio").value;
            var horaFin = document.getElementById("hora_fin").value;
            var especialidad = document.getElementById("especialidad").value;

            // Validar DNI (solo números, 8 dígitos)
            if (!/^\d{8}$/.test(dni)) {
                alert("El DNI debe contener exactamente 8 dígitos numéricos.");
                return false;
            }

            // Validar teléfono (solo números)
            if (!/^\d+$/.test(telefono)) {
                alert("El teléfono debe contener solo números.");
                return false;
            }

            // Validar campos de médico solo si el tipo es "medico"
            if (tipoUsuario === "medico") {
                if (!especialidad || especialidad.trim() === "") {
                    alert("La especialidad es obligatoria para médicos.");
                    return false;
                }
                if (!horaInicio || !horaFin) {
                    alert("Los horarios de inicio y fin son obligatorios para médicos.");
                    return false;
                }
                if (horaInicio >= horaFin) {
                    alert("La hora de inicio debe ser anterior a la hora de fin.");
                    return false;
                }
            }

            return true;
        }

        // Configurar flatpickr para el campo de fecha
        document.addEventListener("DOMContentLoaded", function() {
            flatpickr("#fecha_nac_display", {
                dateFormat: "d/m/Y",
                maxDate: "2007-05-27",
                locale: "es",
                onChange: function(selectedDates, dateStr, instance) {
                    if (selectedDates.length > 0) {
                        var date = selectedDates[0];
                        var formattedDate = date.getFullYear() + "-" + 
                                           String(date.getMonth() + 1).padStart(2, '0') + "-" + 
                                           String(date.getDate()).padStart(2, '0');
                        document.getElementById("fecha_nac").value = formattedDate;
                    }
                }
            });
        });
    </script>
</head>
<body>
    <%
        String origen = request.getParameter("origen");
        boolean esDesdeAdmin = "admin".equals(origen);
    %>

    <h2>Registro de Nuevo Usuario</h2>
    <% 
        String error = (String) request.getAttribute("error");
        if (error != null) { 
    %>
        <div class="alert-danger text-center" role="alert">
            <%= error %>
        </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/RegistrarUnUsuarioServlet" method="post" onsubmit="return validarFormulario()">
        <input type="hidden" name="origen" value="<%= origen %>">

        <label for="nombre_usuario">Nombre de usuario:</label>
        <input type="text" id="nombre_usuario" name="nombre_usuario" required minlength="3" maxlength="50">

        <label for="nombre">Nombre:</label>
        <input type="text" id="nombre" name="nombre" required maxlength="50">

        <label for="apellido">Apellido:</label>
        <input type="text" id="apellido" name="apellido" required maxlength="50">

        <label for="dni">DNI:</label>
        <input type="text" id="dni" name="dni" required>

        <label for="telefono">Teléfono:</label>
        <input type="text" id="telefono" name="telefono" required>

        <label for="tipo">Tipo de Usuario:</label>
        <select id="tipo" name="tipo" required onchange="mostrarCamposMedico()">
            <option value="paciente">Paciente</option>
            <% if (esDesdeAdmin) { %>
                <option value="medico">Médico</option>
            <% } %>
        </select>

        <div id="camposMedico" class="hidden">
            <label for="especialidad">Especialidad:</label>
            <input type="text" id="especialidad" name="especialidad">
            
            <label for="hora_inicio">Hora de inicio (seleccione horas completas):</label>
            <input type="time" id="hora_inicio" name="hora_inicio" step="3600">
            
            <label for="hora_fin">Hora de fin (seleccione horas completas):</label>
            <input type="time" id="hora_fin" name="hora_fin" step="3600">
        </div>

        <label for="contrasenia">Contraseña:</label>
        <input type="password" id="contrasenia" name="contrasenia" required minlength="3" maxlength="50">

        <label for="fecha_nac_display">Fecha de nacimiento:</label>
        <input type="text" id="fecha_nac_display" name="fecha_nac_display" required placeholder="dd/mm/aaaa">
        <input type="hidden" id="fecha_nac" name="fecha_nac">

        <div id="campoObraSocial">
            <label for="tiene_os">¿Tiene obra social?</label>
            <select id="tiene_os" name="tiene_os" required>
                <option value="si">Sí</option>
                <option value="no">No</option>
            </select>
        </div>

        <input type="submit" value="Registrar">
    </form>

    <div class="text-center">
        <a href="${pageContext.request.contextPath}/<%= esDesdeAdmin ? "menuAdministrador.jsp" : "login1.jsp" %>" class="btn btn-secondary">Volver</a>
    </div>
</body>
</html>