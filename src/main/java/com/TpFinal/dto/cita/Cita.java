package com.TpFinal.dto.cita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.TpFinal.dto.BorradoLogico;
import com.TpFinal.dto.EstadoRegistro;
import com.TpFinal.dto.Identificable;
import com.TpFinal.dto.interfaces.Messageable;
import com.TpFinal.dto.persona.Empleado;

@Entity
@Table(name = "citas")
public class Cita implements Identificable, BorradoLogico, Messageable {
	private static Logger logger = Logger.getLogger(Cita.class);
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_cita")
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_registro")
	@NotNull
	private EstadoRegistro estadoRegistro = EstadoRegistro.ACTIVO;
	@Column(name = "randomKey")
	UUID randomKey;

	// Mod by agus(calendario)
	@Column(name = "fecha_hora")
	private LocalDateTime fechaInicio;
	@Column(name = "fecha_fin")
	private LocalDateTime fechaFin;
	@Column(name = "longTime")
	private boolean longTime;
	@Enumerated(EnumType.STRING)
	@Column(name = "state")
	State state;
	// Mod by agus (calendario)
	@Column(name = "direccion_lugar")
	private String direccionLugar;
	@Column(name = "citado")
	private String citado;
	@Column(name = "empleado")
	private String empleado;
	@Column(name = "observaciones")
	private String observaciones;
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_cita")
	TipoCita tipoDeCita;
	@OneToMany(mappedBy = "cita", fetch = FetchType.EAGER)
	@Cascade({ CascadeType.ALL })
	protected Set<Recordatorio> recordatorios = new HashSet<>();

	public enum State {
		empty,
		planned,
		confirmed
	}

	public Cita() {

	}

	private Cita(Builder b) {
		this.fechaInicio = b.fechahora;
		this.citado = b.citado;
		this.direccionLugar = b.direccionLugar;
		this.observaciones = b.observaciones;
		this.tipoDeCita = b.tipoDeCita;
		this.randomKey = UUID.randomUUID();
	}

	@Override
	public String getTriggerKey() {
		if (logger.isDebugEnabled()) {
			String idString = id != null ? id.toString() : "nulo";
			String randomKeyString = randomKey != null ? randomKey.toString() : "nulo";
			logger.debug("Id: " + idString);
			logger.debug("Random key: " + randomKeyString);
		}
		if (randomKey == null)
			randomKey = UUID.randomUUID();
		return this.id.toString() + "-" + this.randomKey.toString();
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDateTime fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getName() {
		return getDetails();
	}

	public String getDetails() {
		LocalDateTime i = this.getFechaInicio();

		return recortarStrings("Cita: "
				+ citado) + "<br>" + this.tipoDeCita + "<br>" + "Lugar:<br>" + recortarStrings(getDireccionLugar())
				+ "<br>Observaciones:<br>" +
				recortarStrings(getObservaciones());

	}

	public String recortarStrings(String m) {
		String ret = "";
		for (int i = 0; i < m.length(); i++) {
			ret = ret + m.charAt(i);
			if (i == 18) {
				ret = ret + "-<br>";
			} else if (i == 23) {

			} else if ((i % 23 == 0) && i != 1 && i != 0) {
				ret = ret + "-<br>";
			}

		}
		return ret;

	}

	public boolean isLongTimeEvent() {
		return longTime;
	}

	public void setLongTimeEvent(boolean b) {
		longTime = b;
	}

	public boolean isEditable() {
		return true;
	}

	public State getState() {
		return State.planned;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Set<Recordatorio> getRecordatorios() {
		return recordatorios;
	}

	public void addRecordatorio(Recordatorio recordatorio) {
		if (!this.recordatorios.contains(recordatorio)) {
			this.recordatorios.add(recordatorio);
			recordatorio.setCita(this);
		}
	}

	public void removeRecordatorio(Recordatorio recordatorio) {
		if (this.recordatorios.contains(recordatorio)) {
			this.recordatorios.remove(recordatorio);
			recordatorio.setCita(null);
		}
	}

	public String getEmpleado() {
		return empleado;
	}

	public void setEmpleado(String empleado) {
		this.empleado = empleado;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDateTime fechaHora) {
		this.fechaInicio = fechaHora;
	}

	public String getDireccionLugar() {
		return direccionLugar;
	}

	public void setDireccionLugar(String direccionLugar) {
		this.direccionLugar = direccionLugar;
	}

	public String getCitado() {
		return citado;
	}

	public void setCitado(String citado) {
		this.citado = citado;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public TipoCita getTipoDeCita() {
		return tipoDeCita;
	}

	public void setTipoDeCita(TipoCita tipoDeCita) {
		this.tipoDeCita = tipoDeCita;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getRandomKey() {
		return randomKey;
	}

	public void setRandomKey(UUID randomKey) {
		this.randomKey = randomKey;
	}

	@Override
	public void setEstadoRegistro(EstadoRegistro estado) {
		this.estadoRegistro = estado;
	}

	@Override
	public EstadoRegistro getEstadoRegistro() {
		return this.estadoRegistro;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Cita))
			return false;
		Cita other = (Cita) obj;
		return getId() != null && Objects.equals(getId(), other.getId());
	}

	@Override
	public String toString() {
		return "Cita [\nid=" + id + "\nestadoRegistro=" + estadoRegistro + "\nfechaHora=" + fechaInicio
				+ "\ndireccionLugar=" + direccionLugar + "\ncitado=" + citado + "\nobservaciones=" + observaciones
				+ "\ntipoDeCita=" + tipoDeCita + "\nrecordatorios=" + recordatorios + "\n]";
	}

	@Override
	public String getMessage() {

		return this.fechaInicio.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "AR")) + " "
				+ agregarCero(this.fechaInicio.getHour()) + ":" +
				agregarCero(this.fechaInicio.getMinute()) + ": " + this.tipoDeCita;
		/*
		 * return "Hora: "+ agregarCero(this.fechaInicio.getHour())+":"+
		 * agregarCero(this.fechaInicio.getMinute())
		 * +"\n"+"Direccion: "+this.direccionLugar+"\n"+"Tipo de cita: "+this.
		 * tipoDeCita;
		 */
	}
	
	@Override
	public void generateUUID() {
		this.randomKey=UUID.randomUUID();
	}

	@Override
	public String getTitulo() {
		return "Cita con " + this.citado;
	}

	private String agregarCero(int horaMinuto) {
		String ret = String.valueOf(horaMinuto);
		if (horaMinuto < 10)
			ret = "0" + String.valueOf(horaMinuto);
		return ret;
	}

	public static class Builder {

		private TipoCita tipoDeCita;
		private String observaciones;
		private String direccionLugar;
		private String citado;
		private LocalDateTime fechahora;
		private Empleado empleado;

		public Builder setTipoDeCita(TipoCita tipoDeCita) {
			this.tipoDeCita = tipoDeCita;
			return this;
		}

		public Builder setObservaciones(String observaciones) {
			this.observaciones = observaciones;
			return this;
		}

		public Builder setDireccionLugar(String direccionLugar) {
			this.direccionLugar = direccionLugar;
			return this;
		}

		public Builder setCitado(String citado) {
			this.citado = citado;
			return this;
		}

		public Builder setEmpleado(Empleado empleado) {
			this.empleado = empleado;
			return this;
		}

		public Builder setFechahora(LocalDateTime fechahora) {
			this.fechahora = fechahora;
			return this;
		}

		public Cita build() {
			return new Cita(this);
		}

	}

}
