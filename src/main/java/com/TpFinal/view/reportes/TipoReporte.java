package com.TpFinal.view.reportes;

import java.util.ArrayList;
import java.util.List;

public enum TipoReporte {
    Propietario("ReportePropietarios.jasper"),
	AlquileresPorCobrar("ReporteAlquileresPorCobrar.jasper"),
	AlquileresPorMes("ReporteAlquileresPorMesNuevo.jasper"),
	FichaInmuebleSimple("FichaInmuebleSimple.jasper");

    private final String archivoGeneradorDeReporte;

    TipoReporte(String archivoGeneradorDeReporte) {
	this.archivoGeneradorDeReporte = archivoGeneradorDeReporte;
    }

    @Override
    public String toString() {
	switch (this) {
	case Propietario:
	    return "Propietario";
	case AlquileresPorCobrar:
	    return "Alquileres a Cobrar";
	case AlquileresPorMes:
	    return "Alquileres por Mes";
	case FichaInmuebleSimple:
		return "Ficha inmueble simple";
	default:
	    return super.toString();

	}
    }
    
    public String getPrefijoArchivo() {
	switch (this) {
	case Propietario:
	    return "rep_props_";
	case AlquileresPorCobrar:
	    return "rep_alq_a_cobrar_";
	case AlquileresPorMes:
	    return "rep_alq_x_mes_";
	case FichaInmuebleSimple:
		return "ficha_inm_simple";
	default:
	    return super.toString();

	}
    }

    public static List<TipoReporte> toList() {
	TipoReporte[] clases = TipoReporte.values();
	List<TipoReporte> ret = new ArrayList<>();
	for (TipoReporte c : clases) {
	    ret.add(c);
	}
	return ret;
    }

    public String getGeneradorDeReporte() {
	return this.archivoGeneradorDeReporte;
    }

}