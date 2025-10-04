package Logica;

import java.sql.Date;
import java.util.List;

public class Paciente extends Persona {
	private int id_paciente;
	private boolean tiene_os;
	
	private List <Turno> listaTurno; //relacion 1 a n
	private Usuario unUsuario;


	public Paciente() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Paciente(String nombre, String apellido, int dni, String telefono, String tipo, String estado,
			Date fecha_nacimiento) {
		super(nombre, apellido, dni, telefono, tipo, estado, fecha_nacimiento);
		// TODO Auto-generated constructor stub
	}

	public Paciente(int id_paciente, boolean tiene_os,  List<Turno> listaTurno,
			Usuario unUsuario) {
		super();
		this.id_paciente = id_paciente;
		this.tiene_os = tiene_os;
		
		this.listaTurno = listaTurno;
		this.unUsuario = unUsuario;
	}


	public int getId_paciente() {
		return id_paciente;
	}
	public void setId_paciente(int id_paciente) {
		this.id_paciente = id_paciente;
	}
	public boolean isTiene_os() {
		return tiene_os;
	}
	public void setTiene_os(boolean tiene_os) {
		this.tiene_os = tiene_os;
	}
	
	public List<Turno> getListaTurno() {
		return listaTurno;
	}
	public void setListaTurno(List<Turno> listaTurno) {
		this.listaTurno = listaTurno;
	}
	public Usuario getUnUsuario() {
		return unUsuario;
	}
	public void setUnUsuario(Usuario unUsuario) {
		this.unUsuario = unUsuario;
	}
}