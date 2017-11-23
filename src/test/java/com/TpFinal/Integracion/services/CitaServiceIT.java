package com.TpFinal.Integracion.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.TpFinal.data.conexion.ConexionHibernate;
import com.TpFinal.data.conexion.TipoConexion;
import com.TpFinal.data.dao.DAOCitaImpl;
import com.TpFinal.data.dao.DAOPersonaImpl;
import com.TpFinal.data.dao.interfaces.DAOCita;
import com.TpFinal.data.dao.interfaces.DAOPersona;
import com.TpFinal.dto.cita.Cita;
import com.TpFinal.dto.cita.TipoCita;
import com.TpFinal.dto.persona.CategoriaEmpleado;
import com.TpFinal.dto.persona.Credencial;
import com.TpFinal.dto.persona.Empleado;
import com.TpFinal.dto.persona.Persona;
import com.TpFinal.dto.persona.Rol;
import com.TpFinal.services.CitaService;

public class CitaServiceIT {
	CitaService service;
	List<Cita>Cita= new ArrayList<>();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		ConexionHibernate.setTipoConexion(TipoConexion.H2Test);
	}

	@Before
	public void set() {
		service=new CitaService();
		DAOCita dao = new DAOCitaImpl();
		this.desvicular();
		service.readAll().forEach(p -> dao.delete(p));
		DAOPersona daop = new DAOPersonaImpl();
		daop.readAll().forEach(p -> daop.delete(p));

		Cita.clear();
	}

	@After
	public void tearDown() {
		DAOCita dao = new DAOCitaImpl();
		this.desvicular();
		service.readAll().forEach(p -> dao.delete(p));
		DAOPersona daop = new DAOPersonaImpl();
		daop.readAll().forEach(p -> daop.delete(p));
	}

	private void desvicular() {
		DAOCita dao = new DAOCitaImpl();
		dao.readAll().forEach(c ->{
			c.setEmpleado(null);
			dao.saveOrUpdate(c);
		});
	}

	@Test
	public void agregar() {
		assertTrue(service.saveOrUpdate(instanciaCita(1)));
		assertTrue(service.saveOrUpdate(instanciaCita(2)));
		assertTrue(service.saveOrUpdate(instanciaCita(3)));
		assertTrue(service.saveOrUpdate(instanciaCita(4)));

		assertEquals(4, service.readAll().size());

		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 1")));
		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 2")));
		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 3")));
		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 4")));
		service.readAll().forEach(cita -> System.out.println("key: "+cita.getTriggerKey()));
	}

	@Test
	public void eliminar() {
		assertTrue(service.saveOrUpdate(instanciaCita(1)));
		assertTrue(service.saveOrUpdate(instanciaCita(2)));
		assertTrue(service.saveOrUpdate(instanciaCita(3)));
		assertTrue(service.saveOrUpdate(instanciaCita(4)));

		assertEquals(4, service.readAll().size());

		assertTrue(service.delete(service.getUltimaAgregada()));
		assertTrue(service.delete(service.getUltimaAgregada()));
		assertTrue(service.delete(service.getUltimaAgregada()));
		assertTrue(service.delete(service.getUltimaAgregada()));

		assertEquals(0, service.readAll().size());
	}

	@Test
	public void editar() {
		assertTrue(service.saveOrUpdate(instanciaCita(1)));
		assertTrue(service.saveOrUpdate(instanciaCita(2)));
		assertTrue(service.saveOrUpdate(instanciaCita(3)));
		assertTrue(service.saveOrUpdate(instanciaCita(4)));

		assertEquals(4, service.readAll().size());

		Cita cita4=service.getUltimaAgregada();
		cita4.setCitado("nuevo");

		service.saveOrUpdate(cita4);

		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 1")));
		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 2")));
		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("Señor 3")));
		assertTrue(service.readAll().stream().anyMatch(c -> c.getCitado().equals("nuevo")));
	}

	@Test
	public void getCitasDeUsuario() {
		Empleado e1=new Empleado.Builder()
				.setCategoriaEmpleado(CategoriaEmpleado.agenteInmobilario)
				.setCredencial(new Credencial.Builder().setUsuario("emp1").build()).build();
		Empleado e2=new Empleado.Builder()
				.setCategoriaEmpleado(CategoriaEmpleado.agenteInmobilario)
				.setCredencial(new Credencial.Builder().setUsuario("emp2").build()).build();
		
		for(int i=0; i<5; i++) {

			Cita c1=instanciaCita(i);
			c1.setEmpleado(e1.getCredencial().getUsuario());

			Cita c2=instanciaCita(i);
			c2.setEmpleado(e2.getCredencial().getUsuario());

			service.saveOrUpdate(c1);
			service.saveOrUpdate(c2);
		}
		
		for(int i=0; i<3; i++) {

			Cita c1=instanciaCita(i);
			c1.setEmpleado(e1.getCredencial().getUsuario());

			service.saveOrUpdate(c1);
		}

		assertEquals(8, service.readAllFromUser(e1).size());
		assertEquals(5, service.readAllFromUser(e2).size());

	}

	@Test
	public void errorDeCargaDuplicada(){
		System.out.println("Total al empezar "+service.readAll().size());
		Persona p1= instanciaEmpleadoAdministrador("1");
		Persona p2= instanciaEmpleadoAdministrador("2");
		p1.addRol(instanciaEmpleado());
		Empleado e = (Empleado)p1.getRol(Rol.Empleado);
		e.getCredencial().setUsuario("User nuevo");
		p2.addRol(instanciaEmpleado());

	}


	@Test
	public void getCitasDeUsuarioEliminando() {
		Empleado e1=new Empleado.Builder()
				.setCategoriaEmpleado(CategoriaEmpleado.agenteInmobilario)
				.setCredencial(new Credencial.Builder().setUsuario("emp1").build()).build();
		Empleado e2=new Empleado.Builder()
				.setCategoriaEmpleado(CategoriaEmpleado.agenteInmobilario)
				.setCredencial(new Credencial.Builder().setUsuario("emp2").build()).build();

		for(int i=0; i<5; i++) {

			Cita c1=instanciaCita(i);
			c1.setEmpleado(e1.getCredencial().getUsuario());

			Cita c2=instanciaCita(i);
			c2.setEmpleado(e2.getCredencial().getUsuario());

			service.saveOrUpdate(c1);
			service.saveOrUpdate(c2);
		}
		
		for(int i=0; i<3; i++) {

			Cita c1=instanciaCita(i);
			c1.setEmpleado(e1.getCredencial().getUsuario());

			service.saveOrUpdate(c1);
		}

		assertEquals(8, service.readAllFromUser(e1).size());
		assertEquals(5, service.readAllFromUser(e2).size());

		service.delete(service.readAllFromUser(e1).get(0));
		service.delete(service.readAllFromUser(e1).get(0));
		service.delete(service.readAllFromUser(e1).get(0));

		assertEquals(5, service.readAllFromUser(e1).size());
		assertEquals(5, service.readAllFromUser(e2).size());
		assertEquals(10, service.readAll().size());
	}
	
	@Test
	public void colisionCita() {
		//CITA NORMAL
		Cita citaNormal= instanciaCita(0);
		citaNormal.setFechaInicio(LocalDateTime.now().plusDays(1));
		citaNormal.setFechaFin(LocalDateTime.now().plusDays(1).plusMinutes(2));
		citaNormal.setEmpleado("admin");
		service.saveOrUpdate(citaNormal);
		//CITA DE OTRO USER
		Cita citaOtroUser= instanciaCita(0);
		citaOtroUser.setFechaInicio(LocalDateTime.now().plusDays(3));
		citaOtroUser.setFechaFin(LocalDateTime.now().plusDays(3).plusMinutes(2));
		citaOtroUser.setEmpleado("pepe");
		service.saveOrUpdate(citaOtroUser);
		//CITA NORMAL 2 NO COLISIONA
		Cita citaNormal2= instanciaCita(0);
		citaNormal2.setFechaInicio(LocalDateTime.now().plusDays(2));
		citaNormal2.setFechaFin(LocalDateTime.now().plusDays(2).plusMinutes(2));
		citaNormal2.setEmpleado("admin");
		service.saveOrUpdate(citaNormal);


		Empleado emp = new Empleado.Builder().setCategoriaEmpleado(CategoriaEmpleado.admin).setCredencial(new
				Credencial.Builder().setUsuario("admin").setContrasenia("admin").build()).build();
		//Testea con una propia
		Cita citaColisionTest= instanciaCita(1);
		citaColisionTest.setFechaInicio(LocalDateTime.now().minusDays(1));
		citaColisionTest.setFechaFin(LocalDateTime.now().plusDays(1).plusMinutes(2));
		assertTrue(service.colisionaConCitasUser(emp, citaColisionTest));
		//Testea con la de otro user
		Cita citaColisionTest2= instanciaCita(1);
		citaColisionTest2.setFechaInicio(LocalDateTime.now().plusDays(3));
		citaColisionTest2.setFechaFin(LocalDateTime.now().plusDays(3).plusMinutes(2));
		assertFalse(service.colisionaConCitasUser(emp, citaColisionTest2));


		
		
		
	}

	private Cita instanciaCita(int i) {

		Empleado e=instanciaEmpleado();

		Cita c = new Cita.Builder()
				.setCitado("Señor "+String.valueOf(i))
				.setDireccionLugar("sarasa: "+String.valueOf(i))
				.setFechahora(LocalDateTime.now().plusDays(i))
				.setObservaciones("obs"+String.valueOf(i))
				.setTipoDeCita(randomCita())
				.setEmpleado(e)
				.build();
		return c;
	}

	private Credencial instanciaCredencial() {
		Credencial c = new Credencial.Builder()
				.setUsuario("usuario")
				.setContrasenia("pass")
				.build();
		return c;
	}

	private Empleado instanciaEmpleado() {
		Empleado e = new Empleado.Builder()
				.setCategoriaEmpleado(CategoriaEmpleado.sinCategoria)
				.setCredencial(instanciaCredencial())
				.setFechaDeAlta(LocalDate.now())
				.build();
		return e;
	}

	public TipoCita randomCita() {
		Random r = new Random();
		int res=r.nextInt(4);
		TipoCita ret=null;
		if(res==0)
			ret=TipoCita.CelebContrato;
		else if(res==1)
			ret=TipoCita.ExhInmueble;
		else if(res==1)
			ret=TipoCita.Otros;
		else
			ret=TipoCita.Tasacion;
		return ret;
	}






	private static Persona instanciaEmpleadoAdministrador(String numero) {

		Persona p = new Persona.Builder()
				.setApellido(numero+"Aasdfafa")
				.setDNI(numero+32523)
				.setinfoAdicional("Un administrador del sistema")
				.setMail(numero+"@hotmail.com")
				.setNombre(numero+"Abdsa")
				.setTelefono(numero+"45345")
				.setTelefono2(numero+"34534")
				.build();

		return p;
	}

}
