package com.TpFinal.view.reportes;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import com.TpFinal.utils.Utils;
import com.TpFinal.view.component.PDFComponent;
import com.vaadin.data.HasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import com.TpFinal.data.dao.DAOContratoAlquilerImpl;
import com.TpFinal.data.dao.interfaces.DAOContratoAlquiler;
import com.TpFinal.dto.cobro.EstadoCobro;
import com.TpFinal.dto.persona.Rol;
import com.TpFinal.services.ContratoService;
import com.TpFinal.services.PersonaService;
import com.TpFinal.view.component.DefaultLayout;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.View;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

/* User Interface written in Java.
 *
 * Define the user interface shown on the Vaadin generated web page by extending the UI class.
 * By default, a new UI instance is automatically created when the page is loaded. To reuse
 * the same instance, add @PreserveOnRefresh.
 */

@Title("Reportes")
@Theme("valo")
@Widgetset("com.vaadin.v7.Vaadin7WidgetSet")
public class ReportesView extends DefaultLayout implements View {

    private static DAOContratoAlquiler daoContratoAlquiler;
       
    private JasperReport reporte;
    private JasperPrint reporteLleno;
    Map<String, Object> parametersMap = new HashMap<String, Object>();
    private Button clearFilterTextBtn = new Button(VaadinIcons.CLOSE);
    PDFComponent pdfComponent = new PDFComponent();
    ComboBox<TipoReporte> tipoReporteCB = new ComboBox<TipoReporte>(
	    null, TipoReporte.toList());
    CheckBox checkbox;
    String reportName = "";
    Button newReport = new Button("Generar");
    Notification error;

    DateField fDesdeNuevo = null;
    DateField fHastaNuevo = null;
    
    DateField fDesde2 = null;

    List<Object> objects = null;
    boolean conCobrosPendientes;

    public enum TipoReporte {
	Propietario("ReportePropietarios.jasper"), AlquileresPorCobrar("ReporteAlquileresPorCobrar.jasper"), 
	AlquileresPorMes("ReporteAlquileresPorMesNuevo.jasper");

	private final String archivoReporte;

	TipoReporte(String archivoReporte) {
	    this.archivoReporte = archivoReporte;
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

	public String getArchivoReporte() {
	    return this.archivoReporte;
	}

    }

    public List<Object> getObjetos(TipoReporte tipo) {
	ArrayList<Object> objects = new ArrayList<>();
	ArrayList<Object> objectsSubReporte = new ArrayList<>();	
	List<ItemRepAlquileresACobrar> items = new ArrayList<ItemRepAlquileresACobrar>();

	System.out.println("Tipo" + tipo);
	switch (tipo) {
	case Propietario:
	    PersonaService servicePersona = new PersonaService();
	    objects.addAll(servicePersona.findForRole(
		    Rol.Propietario.toString()));
	    break;

	case AlquileresPorCobrar:
	    objects.addAll(filtrarPorRangos());
	    break;
	    
	case AlquileresPorMes:
		objects.addAll(filtrarPorMes());
		break;

	}

	return objects;

    }
    
    public ArrayList<Object> filtrarPorRangos() {
	ContratoService service = new ContratoService();
	ArrayList<Object> ret = new ArrayList<>();
	System.out.println(fDesdeNuevo.toString().length() + "" + fHastaNuevo.toString().length());
	if (fHastaNuevo == null || fDesdeNuevo == null) {
	    showErrorNotification("No hay Pagos en el rango de fecha seleccionado");
	    return new ArrayList<>(service.getCobrosOrdenadosPorAño());

	}

	if (fDesdeNuevo.getValue() == null && fHastaNuevo.getValue() == null) {

	    return new ArrayList<>(service.getCobrosOrdenadosPorAño());
	}

	for (ItemRepAlquileresACobrar item : service.getListadoAlquileresACobrar(fDesdeNuevo.getValue(), fHastaNuevo
		.getValue())) {

	    ret.add(item);

	}

	if (ret.size() == 0) {
	    showErrorNotification("No hay Pagos en el rango de fecha seleccionado");
	    return new ArrayList<>();
	}

	return ret;

    }
    
     
    public ArrayList<Object> filtrarPorMes(){
    	ContratoService service = new ContratoService();
    	ArrayList<Object> ret = new ArrayList<>();
    	
        
    	if (fDesde2.getValue() == null) {
    		showErrorNotification("Debes seleccionar una fecha");
   	   
    	}
    	
    	if (fDesde2.getValue() != null && conCobrosPendientes==false) {
    		
    		for (ItemRepAlquileresACobrar item2 : service.getListadoAlquileresCobradosPorMes(fDesde2.getValue())) {

    			    ret.add(item2);
    	
    		}
    		
    		
    		return ret;
    	}
    	
    	else {
    		
    		for (ItemRepAlquileresACobrar item2 : service.getListadoTodosLosAlquileresDeUnMes(fDesde2.getValue())) {
    			ret.add(item2);
    		}
    		
    		
    		return ret;
    		
    	}


    } 

    public ReportesView() {
	super();
	buildLayout();
	configureComponents();
	daoContratoAlquiler = new DAOContratoAlquilerImpl();
	newReport.click();

    }

    public void buildLayout() {
	//CssLayout filtering = new CssLayout();
	CssLayout filtering2 = new CssLayout();
	CssLayout filtering3 = new CssLayout();
	
	conCobrosPendientes = false;
	checkbox = new CheckBox("Incluir Cobros Pendientes",false);

	fDesdeNuevo = new DateField();
	fDesdeNuevo.setPlaceholder("Desde");
	fDesdeNuevo.setParseErrorMessage("Formato de fecha no reconocido");

	fHastaNuevo = new DateField();
	fHastaNuevo.setPlaceholder("Hasta");
	fHastaNuevo.setParseErrorMessage("Formato de fecha no reconocido");
	
	fDesde2 = new DateField();
	fDesde2.setPlaceholder("Fecha Mes");
	fDesde2.setParseErrorMessage("Formato de fecha no reconocido");

	tipoReporteCB.setSelectedItem(TipoReporte.Propietario);
	clearFilterTextBtn.setVisible(false);
	clearFilterTextBtn.setStyleName(ValoTheme.BUTTON_BORDERLESS);
	fDesdeNuevo.setVisible(false);
	fHastaNuevo.setVisible(false);
	fDesde2.setVisible(false);
	checkbox.setVisible(false);
	fDesdeNuevo.setStyleName(ValoTheme.DATEFIELD_BORDERLESS);
	fHastaNuevo.setStyleName(ValoTheme.DATEFIELD_BORDERLESS);
	fDesde2.setStyleName(ValoTheme.DATEFIELD_BORDERLESS);
	clearFilterTextBtn.addClickListener(new Button.ClickListener() {
	    @Override
	    public void buttonClick(Button.ClickEvent clickEvent) {
		fDesdeNuevo.setValue(null);
		fHastaNuevo.setValue(null);
	    }
	});

	generarReporte();
	//filtering.addComponents(fDesde, fHasta, clearFilterTextBtn, tipoReporteCB, newReport);
	filtering2.addComponents(fDesdeNuevo, fHastaNuevo, clearFilterTextBtn,fDesde2, checkbox);
	filtering3.addComponents(tipoReporteCB, newReport);
	tipoReporteCB.setStyleName(ValoTheme.COMBOBOX_BORDERLESS);
	tipoReporteCB.addValueChangeListener(new HasValue.ValueChangeListener<TipoReporte>() {
	    @Override
	    public void valueChange(HasValue.ValueChangeEvent<TipoReporte> valueChangeEvent) {
	    
	    	if (valueChangeEvent.getValue() == TipoReporte.Propietario) {
			    clearFilterTextBtn.setVisible(false);
			    fDesdeNuevo.setVisible(false);
			    fHastaNuevo.setVisible(false);
			    fDesde2.setVisible(false);
			    checkbox.setVisible(false);
			    
			} 
	    	
	    	if (valueChangeEvent.getValue() == TipoReporte.AlquileresPorMes) {
				checkbox.setVisible(true);
				fDesde2.setVisible(true);
				clearFilterTextBtn.setVisible(false);
			    fDesdeNuevo.setVisible(false);
			    fHastaNuevo.setVisible(false);
			}
	    	
	    	if (valueChangeEvent.getValue() == TipoReporte.AlquileresPorCobrar) {
			    clearFilterTextBtn.setVisible(true);
			    fDesdeNuevo.setVisible(true);
			    fHastaNuevo.setVisible(true);
			    fDesde2.setVisible(false);
			    checkbox.setVisible(false);
			}
			
		
	    }
	});
	

	checkbox.addValueChangeListener(event -> 
	conCobrosPendientes = Boolean.valueOf(event.getValue())
	
	);

		
	// tipoReporteCB.setWidth("100%");
	filtering2.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
	filtering3.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
	
	buildToolbar("Reportes", filtering3);
	buildToolbar("", filtering2);
	
	pdfComponent.setSizeFull();
	addComponent(pdfComponent);
	this.setExpandRatio(pdfComponent, 1);
	this.setSpacing(false);
	this.setMargin(false);
	this.setSizeFull();

    }

    private void configureComponents() {
	objects = new ArrayList<Object>();
	tipoReporteCB.setEmptySelectionAllowed(false);
	// tipoReporteCB.setSelectedItem(TipoReporte.Propietario);
	setComponentsVisible(true);
	newReport.setStyleName(ValoTheme.BUTTON_PRIMARY);

	newReport.addClickListener(e -> {

	    try {
		boolean success = generarReporte();
		pdfComponent.setPDF(reportName);
		// limpiamos la lista
		objects.clear();
	    } catch (Exception f) {
		f.printStackTrace();
		System.out.println(f);
		showErrorNotification("Error al generar el reporte");
	    }

	});

    }

    public boolean generarReporte() {
	TipoReporte tipoReporte = tipoReporteCB.getValue();
	System.out.println(tipoReporte);

	List<Object> objetos = getObjetos(tipoReporte);

	// Te trae el nombre del archivo en base a seleccion del combo
	File root = new File(File.separator + tipoReporte.getArchivoReporte());
	File root2 = new File(tipoReporte.getArchivoReporte());
	File webapp = new File(new Utils().resourcesPath() + tipoReporte.getArchivoReporte());

	try {
	    this.reporte = (JasperReport) JRLoader.loadObject(webapp);

	} catch (JRException e) {
	    try {
		this.reporte = (JasperReport) JRLoader.loadObject(root2);
	    } catch (Exception e1) {
		System.err.println("Error al cargar reporte");
		e1.printStackTrace();

	    }
	}

	try {
	    this.reporteLleno = JasperFillManager.fillReport(this.reporte, parametersMap,
		    new JRBeanCollectionDataSource(objetos, false));

	    return crearArchivo();
	} catch (Exception e) {
	    System.err.println("Error al cargar Reporte");
	    e.printStackTrace();
	    return false;
	}

    }

    private boolean crearArchivo() {
	reportName = Long.toString(new Date().getTime() / 1000) + ".pdf"; // Tiempo en segundos desde Epoch hasta ahora
									  // (no se repite)

	File dir = new File("Files");
	if (!dir.exists())
	    dir.mkdir();
	JRPdfExporter exporter = new JRPdfExporter();
	exporter.setExporterInput(new SimpleExporterInput(reporteLleno));
	exporter.setExporterOutput(new SimpleOutputStreamExporterOutput("Files" +
		File.separator + reportName));
	try {
	    exporter.exportReport();
	    return true;
	} catch (JRException e) {
	    System.err.println("Error al exportar a reporte");
	    e.printStackTrace();
	    return false;

	}

    }

    public void setComponentsVisible(boolean b) {
	newReport.setVisible(true);
    }

    public void showErrorNotification(String notification) {
	error = new Notification(
		notification);
	error.setDelayMsec(4000);
	error.setStyleName("bar error small");
	error.setPosition(Position.BOTTOM_CENTER);
	error.show(Page.getCurrent());
    }

    public boolean elRangoDeFechasElegidoEsValido() {
	if (fDesdeNuevo.isEmpty() || fHastaNuevo.isEmpty() ||
		fDesdeNuevo.getParseErrorMessage().equals("Formato de fecha no reconocido") ||
		fHastaNuevo.getParseErrorMessage().equals("Formato de fecha no reconocido") || (fHastaNuevo.getValue().isAfter(
			fDesdeNuevo.getValue())) ||
		(fDesdeNuevo.getValue().isBefore(fHastaNuevo.getValue())))
	    return false;
	return true;
    }

}
