package Logica;

import java.sql.Date;
import java.util.List;

public class Medico extends  Persona {
	private int id_medico;
	private String especialidad;
	private Usuario unUsuario; //relacion 1 a 1 (si no hay responsable esta en null)
	private List <Turno> listaTurno; //relacion 1 a n
	private Horario unHorario;

	public Medico() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Medico(String nombre, String apellido, int dni, String telefono, String tipo, String estado,
			Date fecha_nacimiento) {
		super(nombre, apellido, dni, telefono, tipo, estado, fecha_nacimiento);
		// TODO Auto-generated constructor stub
	}
	public Medico(int id_medico, String especialidad, Usuario unUsuario, List<Turno> listaTurno, Horario unHorario) {
		super();
		this.id_medico = id_medico;
		this.especialidad = especialidad;
		this.unUsuario = unUsuario;
		this.listaTurno = listaTurno;
		this.unHorario = unHorario;
	}
	public int getId_medico() {
		return id_medico;
	}
	public void setId_medico(int id_medico) {
		this.id_medico = id_medico;
	}
	public String getEspecialidad() {
		return especialidad;
	}
	public void setEspecialidad(String especialidad) {
		this.especialidad = especialidad;
	}
	public Usuario getUnUsuario() {
		return unUsuario;
	}
	public void setUnUsuario(Usuario unUsuario) {
		this.unUsuario = unUsuario;
	}
	public List<Turno> getListaTurno() {
		return listaTurno;
	}
	public void setListaTurno(List<Turno> listaTurno) {
		this.listaTurno = listaTurno;
	}
	public Horario getUnHorario() {
		return unHorario;
	}
	public void setUnHorario(Horario unHorario) {
		this.unHorario = unHorario;
	}







}
