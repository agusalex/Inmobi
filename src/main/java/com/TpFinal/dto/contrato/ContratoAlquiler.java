package com.TpFinal.dto.contrato;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.TpFinal.dto.EstadoRegistro;
import com.TpFinal.dto.cobro.Cobro;
import com.TpFinal.dto.inmueble.Inmueble;
import com.TpFinal.dto.interfaces.Messageable;
import com.TpFinal.dto.movimiento.Movimiento;
import com.TpFinal.dto.persona.Inquilino;
import com.TpFinal.dto.persona.Persona;

/**
 * Created by Max on 9/30/2017.
 */
@Entity
@Table(name = "contratoAlquiler")
@PrimaryKeyJoinColumn(name = "id")
public class ContratoAlquiler extends Contrato implements Cloneable, Messageable {

	@Enumerated(EnumType.STRING)
	@Column(name = "tipointeresPunitorio")
	private TipoInteres tipoInteresPunitorio;
	@Enumerated(EnumType.STRING)
	@Column(name = "tipoIncrementoCuota")
	private TipoInteres tipoIncrementoCuota;
	@Column(name = "incrementoCuota")
	private Double porcentajeIncrementoCuota;
	@Column(name = "interesPunitorio")
	private Double interesPunitorio;
	@Column(name = "valorInicial")
	private BigDecimal valorInicial;
	@Column(name = "intervaloActualizacionCuota")
	private Integer intervaloActualizacion;
	@Column(name = "diaDePago")
	private Integer diaDePago;
	@Column(name = "cantidad_certificados_garantes")
	private Integer cantCertificadosGarantes=2;
	
	@Column(name="randomKey")
	UUID randomKey;

	@ManyToOne(fetch = FetchType.EAGER)
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE })
	@JoinColumn(name = "duracionContratoId")
	private ContratoDuracion duracionContrato;

	@ManyToOne(fetch = FetchType.EAGER)
	@Cascade({ CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@JoinColumn(name = "idRol")
	private Inquilino inquilinoContrato;

	@ManyToOne
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE })
	@JoinColumn(name = "id_propietario")
	private Persona propietario;

	public ContratoAlquiler() {
		super();
	}

	private ContratoAlquiler(Builder b) {
		super(b.id, b.fechaIngreso, b.documento, b.estadoRegistro, b.inmueble);
		this.interesPunitorio = b.interesPunitorio;
		this.valorInicial = b.valorInicial;
		this.intervaloActualizacion = b.intervaloActualizacion;
		this.diaDePago = b.diaDePago;
		this.inquilinoContrato = b.inquilinoContrato;
		this.tipoIncrementoCuota = b.tipoIncrementoCuota;
		this.tipoInteresPunitorio = b.tipoInteresPunitorio;
		this.duracionContrato = b.duracionContrato;
		this.porcentajeIncrementoCuota = b.porcentajeIncrementoCuota;
		this.fechaCelebracion=b.fechaCelebracion;
		this.cantCertificadosGarantes = b.cantCertificadosGarantes;
		this.setCobros(new HashSet<>());

		if (b.inmueble != null) {
			this.propietario = b.inmueble.getPropietario() != null ? b.inmueble.getPropietario().getPersona() : null;
		}
		this.randomKey=UUID.randomUUID();
	}
	

	public Integer getCantCertificadosGarantes() {
	    return cantCertificadosGarantes;
	}

	public void setCantCertificadosGarantes(Integer cantCertificadosGarantes) {
	    this.cantCertificadosGarantes = cantCertificadosGarantes;
	}

	public Double getInteresPunitorio() {
		return interesPunitorio;
	}

	public void setInteresPunitorio(Double interesPunitorio) {
		this.interesPunitorio = interesPunitorio;
	}

	public BigDecimal getValorInicial() {
		return valorInicial;
	}

	public void setValorInicial(BigDecimal valorInicial) {
		this.valorInicial = valorInicial;
		this.valorInicial = this.valorInicial.setScale(2, RoundingMode.CEILING);
	}

	public Integer getDiaDePago() {
		return diaDePago;
	}

	public void setDiaDePago(Integer diaDePago) {
		this.diaDePago = diaDePago;
	}

	public Inquilino getInquilinoContrato() {
		return inquilinoContrato;
	}

	public void setInquilinoContrato(Inquilino inquilinoContrato) {
		if (this.inquilinoContrato != null && !this.inquilinoContrato.equals(inquilinoContrato)) {
			this.inquilinoContrato.removeContrato(this);
		}
		this.inquilinoContrato = inquilinoContrato;
		if (inquilinoContrato != null && !inquilinoContrato.getContratos().contains(this))
			inquilinoContrato.addContrato(this);
	}

	public TipoInteres getTipoInteresPunitorio() {
		return tipoInteresPunitorio;
	}

	public void setTipoInteresPunitorio(TipoInteres tipoInteresPunitorio) {
		this.tipoInteresPunitorio = tipoInteresPunitorio;
	}

	public TipoInteres getTipoIncrementoCuota() {
		return tipoIncrementoCuota;
	}

	public void setTipoIncrementoCuota(TipoInteres tipoIncrementoCuota) {
		this.tipoIncrementoCuota = tipoIncrementoCuota;
	}

	public Double getPorcentajeIncrementoCuota() {
		return porcentajeIncrementoCuota;
	}

	public void setPorcentajeIncrementoCuota(Double porcentajeIncrementoCuota) {
		this.porcentajeIncrementoCuota = porcentajeIncrementoCuota;
	}

	public ContratoDuracion getDuracionContrato() {
		return duracionContrato;
	}

	public void setDuracionContrato(ContratoDuracion duracionContrato) {
		this.duracionContrato = duracionContrato;
	}

	public Persona getPropietario() {
		return propietario;
	}

	public void setPropietario(Persona propietario) {
		this.propietario = propietario;
	}

	public Integer getIntervaloActualizacion() {
		return intervaloActualizacion;
	}

	public void setIntervaloActualizacion(Integer intervaloActualizacion) {
		this.intervaloActualizacion = intervaloActualizacion;
	}

	
	public UUID getRandomKey() {
		return randomKey;
	}

	public void setRandomKey(UUID randomKey) {
		this.randomKey = randomKey;
	}

	@Override
	public String toString() {
		return inmueble.toString() + ", "
				+ this.propietario.toString() + ", " + this.inquilinoContrato;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ContratoAlquiler))
			return false;
		ContratoAlquiler contrato = (ContratoAlquiler) o;
		return getId() != null && Objects.equals(getId(), contrato.getId());
	}

	@Override
	public int hashCode() {
		return 3;
	}
	
	@Override
	public void generateUUID() {
		this.randomKey=UUID.randomUUID();
	}

	@Override
	public ContratoAlquiler clone() {
		ContratoAlquiler clon = new ContratoAlquiler();
		clon.setEstadoRegistro(EstadoRegistro.ACTIVO);
		clon.setId(null);
		clon.setDiaDePago(diaDePago.intValue());
		//clon.setDocumento(null);
		clon.setDuracionContrato(duracionContrato);
		clon.setInmueble(inmueble);
		clon.setEstadoContrato(EstadoContrato.EnProcesoDeCarga);
		clon.setInquilinoContrato(inquilinoContrato);
		clon.setInteresPunitorio(interesPunitorio);
		clon.setIntervaloActualizacion(intervaloActualizacion);
		clon.setMoneda(getMoneda());
		clon.setPorcentajeIncrementoCuota(porcentajeIncrementoCuota);
		clon.setPropietario(propietario);
		clon.setTipoIncrementoCuota(tipoIncrementoCuota);
		clon.setTipoInteresPunitorio(tipoInteresPunitorio);
		clon.setValorInicial(valorInicial);
		clon.setArchivo(new Archivo());
		return clon;
	}

	public static class Builder {

		private Integer cantCertificadosGarantes;
		private ContratoDuracion duracionContrato;
		private TipoInteres tipoInteresPunitorio;
		private TipoInteres tipoIncrementoCuota;
		private Inmueble inmueble;
		private Long id;
		private LocalDate fechaIngreso;
		private LocalDate fechaCelebracion;
		private Blob documento;
		private Inquilino inquilinoContrato;
		private BigDecimal valorInicial;
		private Double interesPunitorio;
		private Double porcentajeIncrementoCuota;
		private Integer intervaloActualizacion;
		private Integer diaDePago;
		private EstadoRegistro estadoRegistro = EstadoRegistro.ACTIVO;

		public Builder setId(Long dato) {
			this.id = dato;
			return this;
		}
		
		public Builder setCantCertificadosGarantes(Integer cant) {
		    this.cantCertificadosGarantes = cant;
		    return this;
		}

		public Builder setFechaIngreso(LocalDate dato) {
			this.fechaIngreso = dato;
			return this;
		}

		public Builder setFechaCelebracion(LocalDate dato) {
			this.fechaCelebracion = dato;
			return this;
		}

		public Builder setDocumento(Blob dato) {
			this.documento = dato;
			return this;
		}

		public Builder setInquilinoContrato(Inquilino dato) {
			this.inquilinoContrato = dato;
			return this;
		}

		public Builder setValorIncial(BigDecimal dato) {
			this.valorInicial = dato;
			return this;
		}

		public Builder setIntervaloActualizacion(Integer intervaloDuracion) {
			this.intervaloActualizacion = intervaloDuracion;
			return this;
		}

		public Builder setDiaDePago(Integer diaDePago) {
			this.diaDePago = diaDePago;
			return this;
		}

		public Builder setInteresPunitorio(Double interesPunitorio) {
			this.interesPunitorio = interesPunitorio;
			return this;
		}

		public Builder setPorcentajeIncremento(Double porcentaje) {
			this.porcentajeIncrementoCuota = porcentaje;
			return this;
		}

		public Builder setInmueble(Inmueble inmueble) {
			this.inmueble = inmueble;
			return this;
		}

		public Builder setEstadoRegistro(EstadoRegistro estadoRegistro) {
			this.estadoRegistro = estadoRegistro;
			return this;
		}

		public Builder setDuracionContrato(ContratoDuracion duracionContrato) {
			this.duracionContrato = duracionContrato;
			return this;
		}

		public Builder setTipoInteresPunitorio(TipoInteres tipoInteresPunitorio) {
			this.tipoInteresPunitorio = tipoInteresPunitorio;
			return this;
		}

		public Builder setTipoIncrementoCuota(TipoInteres tipoIncrementoCuota) {
			this.tipoIncrementoCuota = tipoIncrementoCuota;
			return this;
		}

		public ContratoAlquiler build() {
			return new ContratoAlquiler(this);
		}
	}

	@Override
	public String getTitulo() {
		return "Vencimiento de contrato";
	}

	@Override
	public String getMessage() {
		return "El contrato de: "+this.getInquilinoContrato().getPersona().toString()
				+" vence el: "+this.getFechaIngreso().plusMonths(this.duracionContrato.getDuracion())
				.format(new DateTimeFormatterBuilder().appendPattern("dd/MM/YYYY").toFormatter()).toString();
	}

	@Override
	public String getTriggerKey() {
		return this.id.toString()+"-"+this.randomKey.toString();
	}

}
