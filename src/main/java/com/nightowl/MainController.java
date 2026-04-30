package com.nightowl;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private VBox sidebar;
    @FXML private Button toggleSidebarBtn;
    @FXML private Button btnResources;
    @FXML private Button btnMap;
    @FXML private Button btnEmergency;
    @FXML private Button btnStudyRooms;
    @FXML private Button btnWellness;
    @FXML private StackPane contentArea;
    @FXML private VBox panelResources;
    @FXML private VBox panelMap;
    @FXML private VBox panelEmergency;
    @FXML private VBox panelStudyRooms;
    @FXML private VBox panelWellness;
    @FXML private Label headerLabel;
    @FXML private VBox dscDetails;
    @FXML private Button dscToggleBtn;

    private boolean sidebarExpanded = true;
    private boolean dscExpanded = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showPanel("resources");
        highlightNavButton(btnResources);
    }

    @FXML
    private void toggleSidebar() {
        if (sidebarExpanded) {
            sidebar.setPrefWidth(60);
            sidebar.getStyleClass().add("sidebar-collapsed");
        } else {
            sidebar.setPrefWidth(220);
            sidebar.getStyleClass().remove("sidebar-collapsed");
        }
        sidebarExpanded = !sidebarExpanded;
    }

    @FXML
    private void toggleDSC() {
        dscExpanded = !dscExpanded;
        dscDetails.setVisible(dscExpanded);
        dscDetails.setManaged(dscExpanded);
        dscToggleBtn.setText(dscExpanded ? "▲  Hide Details" : "▼  View Details");
    }

    @FXML private void navResources() { showPanel("resources"); highlightNavButton(btnResources); headerLabel.setText("Resource Directory"); }
    @FXML private void navMap()       { showPanel("map");       highlightNavButton(btnMap);       headerLabel.setText("Campus Map and Shuttle Tracker"); }
    @FXML private void navEmergency() { showPanel("emergency"); highlightNavButton(btnEmergency); headerLabel.setText("Emergency Response"); }
    @FXML private void navStudyRooms(){ showPanel("studyrooms");highlightNavButton(btnStudyRooms);headerLabel.setText("Study Room Booking"); }
    @FXML private void navWellness()  { showPanel("wellness");  highlightNavButton(btnWellness);  headerLabel.setText("Personal Wellness Log"); }

    private void showPanel(String panelName) {
        panelResources.setVisible(false);
        panelMap.setVisible(false);
        panelEmergency.setVisible(false);
        panelStudyRooms.setVisible(false);
        panelWellness.setVisible(false);
        switch (panelName) {
            case "resources"  -> panelResources.setVisible(true);
            case "map"        -> panelMap.setVisible(true);
            case "emergency"  -> panelEmergency.setVisible(true);
            case "studyrooms" -> panelStudyRooms.setVisible(true);
            case "wellness"   -> panelWellness.setVisible(true);
        }
    }

    private void highlightNavButton(Button active) {
        for (Button btn : new Button[]{btnResources, btnMap, btnEmergency, btnStudyRooms, btnWellness}) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        active.getStyleClass().add("nav-btn-active");
    }

    @FXML
    private void openTestingForm() {
        try { Desktop.getDesktop().browse(new URI("https://farmingdale.qualtrics.com/jfe/form/SV_2ty60KESWFdm9L0")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openShuttleTracker() {
        try { Desktop.getDesktop().browse(new URI("https://farmingdale.downtownerapp.com/routes/")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openCampusMap() {
        try { Desktop.getDesktop().browse(new URI("https://map.farmingdale.edu/")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openShuttleEmail() { openMailTo("cataldak@farmingdale.edu"); }

    @FXML
    private void openGettingToKnowYou() {
        try { Desktop.getDesktop().browse(new URI("https://farmingdale-accommodate.symplicity.com/public_accommodation/")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openVisitingStudentForm() {
        try { Desktop.getDesktop().browse(new URI("https://farmingdale.qualtrics.com/jfe/form/SV_7amXZb5Z7SaAJKt")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openPlacementTesting() {
        try { Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/placement-testing/index.shtml")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openAccommodationHub() {
        try { Desktop.getDesktop().browse(new URI("https://farmingdale-accommodate.symplicity.com/")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openKurzweil() {
        try { Desktop.getDesktop().browse(new URI("https://www.kurzweil3000.com")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openDocumentation() {
        try { Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/disability-services-center/dsc_guidelines_for_documentation/index.shtml")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openMeetTeam() {
        try { Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/disability-services-center/disability_services_center-meet_the_team.shtml")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openReportIssue() {
        try { Desktop.getDesktop().browse(new URI("https://cm.maxient.com/reporting.php?SUNYFarmingdale")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openAttendancePolicy() {
        try { Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/disability-services-center/pdf/farmingdale_state_college_student_attendance_policy.pdf")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openWebAccessibility() {
        try { Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/accessibility/index.shtml")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openEvacuation() {
        try { Desktop.getDesktop().browse(new URI("https://www.farmingdale.edu/university-police/pdf/evacuation_guidelines.pdf")); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void openDSCEmail() { openMailTo("DSC@farmingdale.edu"); }

    @FXML
    private void openTestingEmail() { openMailTo("TESTING@farmingdale.edu"); }

    private void openMailTo(String email) {
        try {
            Desktop.getDesktop().mail(new URI("mailto:" + email));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}