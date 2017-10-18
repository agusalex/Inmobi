package com.TpFinal.Integracion.views.testBench.PublicacionTests;

import com.TpFinal.Integracion.views.pageobjects.TBLoginView;
import com.TpFinal.Integracion.views.pageobjects.TBMainView;
import com.TpFinal.Integracion.views.pageobjects.TBPublicacionView;
import com.TpFinal.Integracion.views.testBench.TBUtils;
import com.vaadin.testbench.Parameters;
import com.vaadin.testbench.ScreenshotOnFailureRule;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

/**
 * Created by Max on 10/12/2017.
 */
public class AddPublicacionIT extends TestBenchTestCase {


    private TBLoginView loginView;
    private TBMainView mainView;
    private TBPublicacionView publicacionView;
    @Rule
    public ScreenshotOnFailureRule screenshotOnFailureRule =
            new ScreenshotOnFailureRule(this, true);

    @Before
    public void setUp() throws Exception {
        Parameters.setScreenshotErrorDirectory(
                "File/errors");
        Parameters.setMaxScreenshotRetries(2);
        Parameters.setScreenshotComparisonTolerance(1.0);
        Parameters.setScreenshotRetryDelay(10);
        Parameters.setScreenshotComparisonCursorDetection(true);
        setDriver(TBUtils.initializeDriver());
        loginView = TBUtils.loginView(this.getDriver());
        mainView=loginView.login();

        publicacionView = mainView.getPublicacionView();
    }

    @Test
    public void addPublicacionTest(){
        getDriver().get(TBUtils.getUrl("publicaciones"));
        Assert.assertTrue($(GridElement.class).exists());

        //New Publication
        publicacionView.getNuevaButton().first().click();

        //RadioButtons
        publicacionView.getRadioButtonGroup().first().selectByText("Venta");
        publicacionView.getEstadodelapublicacionRadioButtonGroup().first().selectByText("Terminada");

        //DateField
        publicacionView.getFechapublicacionDateField().first().setValue("21/10/2017");

        //Inmueble ComboBox
        List<String> inmuebleList = publicacionView.getInmuebleComboBox().first().getPopupSuggestions();
        String selectedInmueble = inmuebleList.get(1);
        publicacionView.getInmuebleComboBox().first().selectByText(selectedInmueble);

        //MontoField
        publicacionView.getMontoTextField().first().setValue("200000");

        //Change type Combo
        publicacionView.getMonedaComboBox().first().selectByText("Pesos");

        publicacionView.getGuardarButton().first().click();

        //Verify the form is closed
        Assert.assertFalse(publicacionView.isFormDisplayed());
    }


}
