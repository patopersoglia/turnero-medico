package Logica;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Turno {
    private int id_turno;
    private Date fecha_turno;
    private String hora_Turno;
    private String nombre_medico;
    private String nombre_paciente;

    public int getId_turno() {
        return id_turno;
    }

    public void setId_turno(int id_turno) {
        this.id_turno = id_turno;
    }

    public Date getFecha_turno() {
        return fecha_turno;
    }

    public void setFecha_turno(Date fecha_turno) {
        this.fecha_turno = fecha_turno;
    }

    public String getHora_Turno() {
        return hora_Turno;
    }

    public void setHora_Turno(String hora_Turno) {
        this.hora_Turno = hora_Turno;
    }

    public String getNombre_medico() {
        return nombre_medico;
    }

    public void setNombre_medico(String nombre_medico) {
        this.nombre_medico = nombre_medico;
    }

    public String getNombre_paciente() {
        return nombre_paciente;
    }

    public void setNombre_paciente(String nombre_paciente) {
        this.nombre_paciente = nombre_paciente;
    }

 // Método para verificar si el turno se puede cancelar
    public boolean esCancelable() {
        if (fecha_turno == null || hora_Turno == null) {
            return false;
        }

        LocalDate fechaTurno = fecha_turno.toLocalDate();
        LocalTime horaTurno = LocalTime.parse(hora_Turno);
        LocalDateTime turnoDateTime = LocalDateTime.of(fechaTurno, horaTurno);

        LocalDateTime ahora = LocalDateTime.now();

        return turnoDateTime.isAfter(ahora); // Se puede cancelar si el turno es posterior al momento actual
    }
}


