package Logica;

public class Horario {

	private int id_horario;
	private String hora_inicio;
	private String hora_fin;


	public Horario() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Horario(int id_horario, String hora_inicio, String hora_fin) {
		super();
		this.id_horario = id_horario;
		this.hora_inicio = hora_inicio;
		this.hora_fin = hora_fin;
	}

	public int getId_horario() {
		return id_horario;
	}
	public void setId_horario(int id_horario) {
		this.id_horario = id_horario;
	}
	public String getHora_inicio() {
		return hora_inicio;
	}
	public void setHora_inicio(String hora_inicio) {
		this.hora_inicio = hora_inicio;
	}
	public String getHora_fin() {
		return hora_fin;
	}
	public void setHora_fin(String hora_fin) {
		this.hora_fin = hora_fin;
	}


}
