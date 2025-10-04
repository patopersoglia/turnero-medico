package Logica;

import java.sql.Date;

public class Persona {

    private String nombre;
    private String apellido;
    private int dni;
    private String telefono;
    private String tipo;
    private String estado;

    private Date fecha_nacimiento;


    public Persona() {}


    public Persona( String nombre, String apellido, int dni, String telefono, String tipo, String estado, Date fecha_nacimiento) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.telefono = telefono;
        this.tipo = tipo;
        this.estado = estado;
        this.fecha_nacimiento = fecha_nacimiento;

    }




	public Date getFecha_nacimiento() {
		return fecha_nacimiento;
	}


	public void setFecha_nacimiento(Date fecha_nacimiento) {
		this.fecha_nacimiento = fecha_nacimiento;
	}




	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public int getDni() {
		return dni;
	}

	public void setDni(int dni) {
		this.dni = dni;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}


    // Getters y setters para todos los atributos
    // ...
}