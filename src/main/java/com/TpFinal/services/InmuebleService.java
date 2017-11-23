package com.TpFinal.services;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.TpFinal.view.reportes.ItemFichaInmuebleSimple;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.TpFinal.data.dao.DAOInmuebleImpl;
import com.TpFinal.data.dao.interfaces.DAOInmueble;
import com.TpFinal.dto.EstadoRegistro;
import com.TpFinal.dto.contrato.Contrato;
import com.TpFinal.dto.contrato.EstadoContrato;
import com.TpFinal.dto.inmueble.ClaseInmueble;
import com.TpFinal.dto.inmueble.Coordenada;
import com.TpFinal.dto.inmueble.CriterioBusqInmueble;
import com.TpFinal.dto.inmueble.Direccion;
import com.TpFinal.dto.inmueble.EstadoInmueble;
import com.TpFinal.dto.inmueble.Imagen;
import com.TpFinal.dto.inmueble.Inmueble;
import com.TpFinal.dto.inmueble.TipoInmueble;
import com.TpFinal.dto.persona.Propietario;
import com.TpFinal.dto.publicacion.EstadoPublicacion;
import com.TpFinal.dto.publicacion.Publicacion;
import com.TpFinal.dto.publicacion.PublicacionAlquiler;
import com.TpFinal.dto.publicacion.PublicacionVenta;
import com.TpFinal.dto.publicacion.TipoPublicacion;
import com.TpFinal.view.inmuebles.FiltroInmueble;
import com.TpFinal.view.reportes.ItemFichaInmueble;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;

public class InmuebleService {
    private DAOInmueble dao;
    private Supplier<List<Inmueble>> supplier;
    private static final Logger logger = Logger.getLogger(InmuebleService.class);

    public InmuebleService() {
	dao = new DAOInmuebleImpl();
	supplier = () -> this.readAll();
    }

    public InmuebleService(Supplier<List<Inmueble>> supplier) {
	dao = new DAOInmuebleImpl();
	this.supplier = supplier;
    }

    public void setSupplier(Supplier<List<Inmueble>> supplier) {
	this.supplier = supplier;
    }

    public List<Inmueble> readAll() {
	List<Inmueble> ret = dao.readAllActives();
	ret.sort(Comparator.comparing(Inmueble::getId));
	return ret;
    }

    public List<Object> getListaFichaInmueble(Inmueble inmueble) {
	List<ItemFichaInmueble> lista = new ArrayList<>();

	ItemFichaInmueble item = new ItemFichaInmueble(inmueble);
	lista.add(item);
	return lista.stream().map(i -> (Object) i).collect(Collectors.toList());
    }

    public boolean merge(Inmueble inmueble) {
	Long id = dao.mergeAndGetId(inmueble);
	Boolean ret = id != null;
	if (id != null) {
	    Inmueble inmb = dao.findById(id);
	    List<Imagen> imagenes = inmb.getImagenes().stream().collect(Collectors.toList());
	    for (Imagen i : imagenes) {
		if (logger.isDebugEnabled())
		    logger.debug("Añadiendo imagen: " + i.getNombre() + i.getExtension());
		ret = ret && dao.addImagen(i, inmb);
	    }
	}
	return ret;

    }

    public boolean delete(Inmueble i) {
	return dao.logicalDelete(i);
    }

    public Inmueble findById(Long id) {
	return dao.findById(id);
    }

    public List<Inmueble> findByCaracteristicas(CriterioBusqInmueble criterio) {
	List<Inmueble> ret = dao.findInmueblesbyCaracteristicas(criterio);
	ret.sort(Comparator.comparing(Inmueble::getId).reversed());
	return ret;
    }

    public static Inmueble getInstancia() {
	return new Inmueble.Builder()
		.setaEstrenar(false)
		.setCantidadAmbientes(0)
		.setCantidadCocheras(0)
		.setCantidadDormitorios(0)
		.setClaseInmueble(ClaseInmueble.OtroInmueble)
		.setConAireAcondicionado(false)
		.setConJardin(false)
		.setConParilla(false)
		.setConPileta(false)
		.setDireccion(new Direccion.Builder()
			.setCalle("")
			.setCodPostal("")
			.setCoordenada(new Coordenada())
			.setLocalidad("")
			.setNro(0)
			.setPais("Argentina")
			.setProvincia("")
			.build())
		.setEstadoInmueble(EstadoInmueble.NoPublicado)
		.setPropietario(new Propietario())
		.setSuperficieCubierta(0)
		.setSuperficieTotal(0)
		.setTipoInmueble(TipoInmueble.Vivienda)
		.build();
    }

    public static Resource getPortada(Inmueble inmueble) {
	if (inmueble != null) {
	    if (new File("Files" + File.separator + inmueble.getNombreArchivoPortada()).exists()) {
		StreamResource str = new StreamResource(new StreamResource.StreamSource() {
		    @Override
		    public InputStream getStream() {
			try {

			    return new FileInputStream("Files" + File.separator + inmueble.getNombreArchivoPortada());
			} catch (FileNotFoundException e) {
			    System.out.println("Error al Buscar Portada de inmueble: " + inmueble);

			}
			return null;
		    }
		}, "Files" + File.separator + inmueble.getNombreArchivoPortada());

		return str;
	    }

	}
	return null;

    }

    public List<Resource> getFotos(Inmueble inmueble) {
	List<Resource> fotos = new ArrayList<>();

	if (inmueble != null && inmueble.getId() != null) {
	    fotos = inmueble.getPathImagenes().stream()
		    .filter(path -> {
			return new File("Files" + File.separator + path).exists();
		    })
		    .map(InmuebleService::GenerarStreamResource)
		    .collect(Collectors.toList());
	}
	return fotos.isEmpty() ? null : fotos;
    }

    public Map<String, Resource> getFotosYPath(Inmueble inmueble) {
	Map<String, Resource> fotos = new HashMap<>();
	if (inmueble != null && inmueble.getId() != null) {
	    fotos = inmueble.getPathImagenes().stream()
		    .filter(path -> {
			return new File("Files" + File.separator + path).exists();
		    })
		    .collect(Collectors.toMap(path -> path, InmuebleService::GenerarStreamResource));
	}
	return fotos.isEmpty() ? null : fotos;
    }

    public static Resource GenerarStreamResource(String path) {
	if (new File("Files" + File.separator + path).exists()) {
	    StreamResource str = new StreamResource(new StreamResource.StreamSource() {
		@Override
		public InputStream getStream() {
		    try {
			return new FileInputStream("Files" + File.separator + path);
		    } catch (FileNotFoundException e) {
			System.out.println("Error al Buscar Foto: " + path);
		    }
		    return null;
		}
	    }, "Files" + File.separator + path);
	    return str;
	}
	return null;
    }

    public List<Inmueble> filtrarPorCalle(String filtro) {
	List<Inmueble> inmuebles = dao.readAllActives().stream()
		.filter(i -> {
		    return filtro == null || filtro.isEmpty()
			    || i.getDireccion().getCalle().toLowerCase().contains(filtro.toLowerCase());
		})
		.collect(Collectors.toList());
	Collections.sort(inmuebles, new Comparator<Inmueble>() {

	    @Override
	    public int compare(Inmueble o1, Inmueble o2) {
		return o1.getDireccion().getCalle().compareTo(o2.getDireccion().getCalle());
	    }

	});
	return inmuebles;
    }

    /**
     * Revisa la lista de publicaciones. Si existe al menos una publicación activa,
     * cambia el estadoPublicación a activo sino, cambia el estadoPublicacion a
     * noPublicado
     * 
     * @param inmueble
     */
    public boolean actualizarEstadoInmuebleSegunPublicacion(Inmueble i) {
	boolean ret = true;
	Inmueble inmueble = this.findById(i.getId());
	List<Publicacion> publicaciones = getListadoDePublicaciones(inmueble);
	List<Publicacion> pubsActivas = publicaciones.stream().filter(this::estaActivoYNoFueBorrado)
		.collect(Collectors.toList());

	setEstadoInmuebleSegunPublicaciones(inmueble, pubsActivas);
	logger.debug("Actualizado estado inmueble a: " + inmueble.getEstadoInmueble());
	ret = ret && dao.merge(inmueble);

	return ret;

    }

    private boolean estaActivoYNoFueBorrado(Publicacion p) {
	return p.getEstadoRegistro() == EstadoRegistro.ACTIVO && p
		.getEstadoPublicacion() == EstadoPublicacion.Activa;
    }

    private void setEstadoInmuebleSegunPublicaciones(Inmueble inmueble, List<Publicacion> publicaciones) {

	boolean algunaVenta = publicaciones.stream()
		.anyMatch(p -> p.getTipoPublicacion() == TipoPublicacion.Venta);
	boolean algunAlquiler = publicaciones.stream()
		.anyMatch(p -> p.getTipoPublicacion() == TipoPublicacion.Alquiler);
	if (algunaVenta && algunAlquiler) {
	    inmueble.setEstadoInmueble(EstadoInmueble.EnAlquilerYVenta);
	} else if (algunaVenta) {
	    inmueble.setEstadoInmueble(EstadoInmueble.EnVenta);
	} else if (algunAlquiler) {
	    inmueble.setEstadoInmueble(EstadoInmueble.EnAlquiler);
	} else {
	    inmueble.setEstadoInmueble(EstadoInmueble.NoPublicado);
	}

    }

    public List<Publicacion> getListadoDePublicaciones(Inmueble inmueble) {
	List<Publicacion> ret = null;
	if (inmueble != null && inmueble.getPublicaciones() != null) {
	    ret = inmueble.getPublicaciones().stream().collect(Collectors.toList());
	}
	return ret;
    }

    public void desvincularContrato(Contrato contratoAntiguo) {
	Inmueble i = contratoAntiguo.getInmueble();
	i.removeContrato(contratoAntiguo);
	dao.saveOrUpdate(i);
    }

    public boolean inmueblePoseeContratoVigente(Inmueble inmueble) {
	boolean ret = false;
	if (inmueble != null) {
	    Set<Contrato> contratos = dao.findById(inmueble.getId()).getContratos();
	    if (contratos != null)
		ret = contratos.stream().anyMatch(contrato -> contrato.getEstadoContrato() == EstadoContrato.Vigente
			|| contrato.getEstadoContrato() == EstadoContrato.ProximoAVencer);
	}
	return ret;
    }

    public static List<Publicacion> getPublicacionesActivas(Inmueble i) {

	List<Publicacion> publicaciones = i.getPublicaciones().stream().collect(Collectors.toList());
	List<Publicacion> publicacionesActivas = publicaciones.stream()
		.filter(p -> p.getEstadoRegistro().equals(EstadoRegistro.ACTIVO))
		.filter(p -> p.getEstadoPublicacion().equals(EstadoPublicacion.Activa))
		.collect(Collectors.toList());

	return publicacionesActivas;
    }

    public List<Inmueble> findAll(FiltroInmueble filtro) {
	List<Inmueble> inmuebles = supplier.get()
		.stream()
		.filter(filtro.getFiltroCompuesto())
		.collect(Collectors.toList());
	inmuebles.sort(Comparator.comparing(Inmueble::getId));
	return inmuebles;
    }

    public List<Object> getListaFichaInmuebleSimple(Inmueble inmueble) {
	List<ItemFichaInmuebleSimple> lista = new ArrayList<>();

	ItemFichaInmuebleSimple item = new ItemFichaInmuebleSimple(inmueble);
	lista.add(item);
	return lista.stream().map(i -> (Object) i).collect(Collectors.toList());
    }

    public boolean inmueblePoseePubActivaDeTipo(Inmueble inmueble, TipoPublicacion tipoPublicacion) {
	boolean ret = false;
	logger.debug("Inmueble: " + inmueble);
	logger.debug("TipoPublicacion: " + tipoPublicacion);
	if (inmueble != null) {
	    Inmueble i = findById(inmueble.getId());
	    i.getPublicaciones().forEach(p -> logger.debug("Publicacion: " + p));
	    ret = i.getPublicaciones() == null || i.getPublicaciones().isEmpty() ? false
		    : i.getPublicaciones().stream()
			    .filter(p -> p.getEstadoRegistro() == EstadoRegistro.ACTIVO)
			    .filter(p -> {
				if (tipoPublicacion == TipoPublicacion.Alquiler)
				    return p instanceof PublicacionAlquiler;
				else
				    return p instanceof PublicacionVenta;
			    }).anyMatch(p -> p.getEstadoPublicacion() == EstadoPublicacion.Activa);
	}
	return ret;
    }

    /**
     * Devuelve la imagen de portada correspondiente al Inmueble. Si el inmueble no
     * posee una imagen de portada, devuelve nul
     * 
     * @param i
     * @return
     */
    public static Image getImagenPortada(Inmueble i) {
	Image image = null;
	if (logger.isDebugEnabled()) {
	    logger.debug("Nombre Archivo Portada: " + i.getNombreArchivoPortada());
	}
	if (i.getNombreArchivoPortada() != null && i.getNombreArchivoPortada() != "") {
	    try {
		File pathToFile = new File("Files" + File.separator + i.getNombreArchivoPortada());
		image = ImageIO.read(pathToFile);
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	    if (logger.isDebugEnabled()) {
		if (image != null)
		    logger.debug("Imagen cargada");
		else
		    logger.debug("Imagen null");
	    }
	}

	return image;
    }

    public static boolean addImagenToInmueble(Imagen img, Inmueble inmueble) {
	DAOInmuebleImpl dao = new DAOInmuebleImpl();
	return dao.addImagen(img, inmueble);
    }

    public static void cargarImagenesAFileSystem() {
	DAOInmuebleImpl dao = new DAOInmuebleImpl();
	dao.readAllActives().forEach(InmuebleService::cargarImagenesDeInmueble);

    }

    public static void cargarImagenesDeInmueble(Inmueble i) {
	i.getImagenes().stream()
		.filter(imagen -> imagen.getImagen() != null)
		.filter(imagen -> imagen.getPath() != null)
		.forEach(InmuebleService::cargarImagen);
    }

    private static void cargarImagen(Imagen imagen) {
	Path p = Paths.get("." + File.separator + imagen.getPath());
	if (!Files.exists(p)) {
	    File path = p.toFile();
	    if (!path.exists()) {
		logger.debug("Cargando archivo a file system: " + path);
		FileOutputStream fout = null;
		try {
		    fout = new FileOutputStream(path);
		    IOUtils.copy(imagen.getImagen().getBinaryStream(), fout);
		    fout.close();
		} catch (IOException | SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }
}
