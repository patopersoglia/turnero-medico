package Logica;

public class Usuario {
	private int id_usuario;
	private String nombre_usuario;
	private String contrasenia;
	private String tipo;
	private String foto_perfil;
	private String estado;
	public Usuario() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Usuario(int id_usuario, String nombre_usuario, String contrasenia, String tipo, String foto_perfil, String estado) {
		super();
		this.id_usuario = id_usuario;
		this.nombre_usuario = nombre_usuario;
		this.contrasenia = contrasenia;
		this.tipo = tipo;
		this.foto_perfil= foto_perfil;
		this.estado= estado;

	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public int getId_usuario() {
		return id_usuario;
	}

	public void setId_usuario(int id_usuario) {
		this.id_usuario = id_usuario;
	}
	public String getNombre_usuario() {
		return nombre_usuario;
	}
	public void setNombre_usuario(String nombre_usuario) {
		this.nombre_usuario = nombre_usuario;
	}
	public String getContrasenia() {
		return contrasenia;
	}
	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String rol) {
		this.tipo = rol;
	}
	public String getFoto_perfil() {
		return foto_perfil;
	}
	public void setFoto_perfil(String foto_perfil) {
		this.foto_perfil = foto_perfil;
	}


}
