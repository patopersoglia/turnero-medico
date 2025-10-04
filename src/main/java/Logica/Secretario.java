package Logica;

import java.sql.Date;

public class Secretario extends Persona {
	private int id_secretario;
	private String sector;
	private Usuario unUsuario;

	public Secretario() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Secretario(String nombre, String apellido, int dni, String telefono, String tipo, String estado,
			Date fecha_nacimiento) {
		super(nombre, apellido, dni, telefono, tipo, estado, fecha_nacimiento);
		// TODO Auto-generated constructor stub
	}
	public Secretario(int id_secretario, String sector, Usuario unUsuario) {
		super();
		this.id_secretario = id_secretario;
		this.sector = sector;
		this.unUsuario = unUsuario;
	}
	public int getId_secretario() {
		return id_secretario;
	}
	public void setId_secretario(int id_secretario) {
		this.id_secretario = id_secretario;
	}
	public String getSector() {
		return sector;
	}
	public void setSector(String sector) {
		this.sector = sector;
	}
	public Usuario getUnUsuario() {
		return unUsuario;
	}
	public void setUnUsuario(Usuario unUsuario) {
		this.unUsuario = unUsuario;
	}


}
