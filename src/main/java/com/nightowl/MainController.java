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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // ── FXML Bindings ────────────────────────────────────────────────────────

    @FXML private VBox      sidebar;
    @FXML private Button    toggleSidebarBtn;
    @FXML private Button    btnResources;
    @FXML private Button    btnMap;
    @FXML private Button    btnEmergency;
    @FXML private Button    btnStudyRooms;
    @FXML private Button    btnWellness;
    @FXML private StackPane contentArea;
    @FXML private VBox      panelResources;
    @FXML private VBox      panelMap;
    @FXML private VBox      panelEmergency;
    @FXML private VBox      panelStudyRooms;
    @FXML private VBox      panelWellness;
    @FXML private Label     headerLabel;
    @FXML private VBox      dscDetails;
    @FXML private Button    dscToggleBtn;

    // ── State ─────────────────────────────────────────────────────────────────

    private boolean sidebarExpanded = true;
    private boolean dscExpanded     = false;

    // ── Sealed type: each nav section defined in one place ───────────────────

    /**
     * Represents a navigation destination.
     * Uses a Java 21 sealed interface + records so the compiler
     * enforces exhaustive pattern-matching in switch expressions.
     */
    sealed interface NavSection
            permits NavSection.Resources, NavSection.Map,
                    NavSection.Emergency, NavSection.StudyRooms,
                    NavSection.Wellness {

        record Resources  () implements NavSection {}
        record Map        () implements NavSection {}
        record Emergency  () implements NavSection {}
        record StudyRooms () implements NavSection {}
        record Wellness   () implements NavSection {}
    }

    // ── Icon maps for collapsed / expanded sidebar ────────────────────────────

    /** Full labels shown when the sidebar is expanded. */
    private static final Map<String, String> LABELS_EXPANDED = Map.of(
            "toggle",     "Menu",
            "resources",  "Resources",
            "map",        "Campus Map",
            "emergency",  "Emergency",
            "studyrooms", "Study Rooms",
            "wellness",   "Wellness"
    );

    /** Icon-only labels shown when the sidebar is collapsed to 60 px. */
    private static final Map<String, String> LABELS_COLLAPSED = Map.of(
            "toggle",     "☰",
            "resources",  "📋",
            "map",        "🗺",
            "emergency",  "🚨",
            "studyrooms", "📚",
            "wellness",   "💚"
    );

    // ── Initialisation ────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigateTo(new NavSection.Resources());
    }

    // ── Sidebar toggle ────────────────────────────────────────────────────────

    @FXML
    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;

        var labels = sidebarExpanded ? LABELS_EXPANDED : LABELS_COLLAPSED;

        sidebar.setPrefWidth(sidebarExpanded ? 220 : 60);

        if (sidebarExpanded) {
            sidebar.getStyleClass().remove("sidebar-collapsed");
        } else {
            sidebar.getStyleClass().add("sidebar-collapsed");
        }

        toggleSidebarBtn.setText(labels.get("toggle"));
        btnResources .setText(labels.get("resources"));
        btnMap       .setText(labels.get("map"));
        btnEmergency .setText(labels.get("emergency"));
        btnStudyRooms.setText(labels.get("studyrooms"));
        btnWellness  .setText(labels.get("wellness"));
    }

    // ── DSC expand / collapse ─────────────────────────────────────────────────

    @FXML
    private void toggleDSC() {
        dscExpanded = !dscExpanded;
        dscDetails.setVisible(dscExpanded);
        dscDetails.setManaged(dscExpanded);
        dscToggleBtn.setText(dscExpanded ? "▲  Hide Details" : "▼  View Details");
    }

    // ── Nav button handlers ───────────────────────────────────────────────────

    @FXML private void navResources () { navigateTo(new NavSection.Resources());  }
    @FXML private void navMap       () { navigateTo(new NavSection.Map());        }
    @FXML private void navEmergency () { navigateTo(new NavSection.Emergency());  }
    @FXML private void navStudyRooms() { navigateTo(new NavSection.StudyRooms()); }
    @FXML private void navWellness  () { navigateTo(new NavSection.Wellness());   }

    /**
     * Central navigation method.
     * Uses Java 21 pattern-matching switch — exhaustive, so the compiler
     * will error if a NavSection variant is ever added but not handled here.
     */
    private void navigateTo(NavSection section) {
        var target = switch (section) {
            case NavSection.Resources  s -> new NavTarget(panelResources,  btnResources,  "Resource Directory");
            case NavSection.Map        s -> new NavTarget(panelMap,        btnMap,        "Campus Map and Shuttle Tracker");
            case NavSection.Emergency  s -> new NavTarget(panelEmergency,  btnEmergency,  "Emergency Response");
            case NavSection.StudyRooms s -> new NavTarget(panelStudyRooms, btnStudyRooms, "Study Room Booking");
            case NavSection.Wellness   s -> new NavTarget(panelWellness,   btnWellness,   "Personal Wellness Log");
        };

        allPanels().forEach(p -> { p.setVisible(false); p.setManaged(false); });
        target.panel().setVisible(true);
        target.panel().setManaged(true);

        headerLabel.setText(target.title());

        allNavButtons().forEach(b -> b.getStyleClass().remove("nav-btn-active"));
        target.button().getStyleClass().add("nav-btn-active");
    }

    /** Simple record carrying resolved UI references for a nav destination. */
    private record NavTarget(VBox panel, Button button, String title) {}

    // ── Helper lists ──────────────────────────────────────────────────────────

    private List<VBox> allPanels() {
        return List.of(panelResources, panelMap, panelEmergency, panelStudyRooms, panelWellness);
    }

    private List<Button> allNavButtons() {
        return List.of(btnResources, btnMap, btnEmergency, btnStudyRooms, btnWellness);
    }

    // ── URL / Mail helpers ────────────────────────────────────────────────────

    private void openURL(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.printf("[NightOwl] Failed to open URL: %s%n  Cause: %s%n", url, e.getMessage());
        }
    }

    private void openMailTo(String address) {
        try {
            Desktop.getDesktop().mail(new URI("mailto:" + address));
        } catch (Exception e) {
            System.err.printf("[NightOwl] Failed to open mail client for: %s%n  Cause: %s%n", address, e.getMessage());
        }
    }

    // ── Resource Directory ────────────────────────────────────────────────────

    @FXML private void openTestingForm()         { openURL("https://farmingdale.qualtrics.com/jfe/form/SV_2ty60KESWFdm9L0"); }
    @FXML private void openGettingToKnowYou()    { openURL("https://farmingdale-accommodate.symplicity.com/public_accommodation/"); }
    @FXML private void openVisitingStudentForm() { openURL("https://farmingdale.qualtrics.com/jfe/form/SV_7amXZb5Z7SaAJKt"); }
    @FXML private void openPlacementTesting()    { openURL("https://www.farmingdale.edu/placement-testing/index.shtml"); }
    @FXML private void openAccommodationHub()    { openURL("https://farmingdale-accommodate.symplicity.com/"); }
    @FXML private void openKurzweil()            { openURL("https://www.kurzweil3000.com"); }
    @FXML private void openDocumentation()       { openURL("https://www.farmingdale.edu/disability-services-center/dsc_guidelines_for_documentation/index.shtml"); }
    @FXML private void openMeetTeam()            { openURL("https://www.farmingdale.edu/disability-services-center/disability_services_center-meet_the_team.shtml"); }
    @FXML private void openReportIssue()         { openURL("https://cm.maxient.com/reporting.php?SUNYFarmingdale"); }
    @FXML private void openAttendancePolicy()    { openURL("https://www.farmingdale.edu/disability-services-center/pdf/farmingdale_state_college_student_attendance_policy.pdf"); }
    @FXML private void openWebAccessibility()    { openURL("https://www.farmingdale.edu/accessibility/index.shtml"); }
    @FXML private void openEvacuation()          { openURL("https://www.farmingdale.edu/university-police/pdf/evacuation_guidelines.pdf"); }
    @FXML private void openDSCEmail()            { openMailTo("DSC@farmingdale.edu"); }
    @FXML private void openTestingEmail()        { openMailTo("TESTING@farmingdale.edu"); }

    // ── Campus Map & Shuttle ──────────────────────────────────────────────────

    @FXML private void openShuttleTracker() { openURL("https://farmingdale.downtownerapp.com/routes/"); }
    @FXML private void openCampusMap()      { openURL("https://map.farmingdale.edu/"); }
    @FXML private void openShuttleEmail()   { openMailTo("cataldak@farmingdale.edu"); }

    // ── Emergency Panel ───────────────────────────────────────────────────────

    @FXML private void callCampusPolice()       { openURL("tel:9344205765"); }
    @FXML private void call911()                { openURL("tel:911"); }
    @FXML private void openActiveShooterGuide() { openURL("https://www.farmingdale.edu/university-police/pdf/active_shooter.pdf"); }
    @FXML private void openEmergencyAlerts()    { openURL("https://www.farmingdale.edu/university-police/emergency-alerts.shtml"); }
    @FXML private void openCrisisSupport()      { openURL("https://www.farmingdale.edu/counseling/index.shtml"); }

    // ── Study Room Booking ────────────────────────────────────────────────────

    @FXML private void openStudyRoomBooking() { openURL("https://farmingdale.libcal.com/spaces"); }
    @FXML private void openLibraryHours()     { openURL("https://library.farmingdale.edu/hours"); }

    // ── Wellness Panel ────────────────────────────────────────────────────────

    @FXML private void openCounseling()        { openURL("https://www.farmingdale.edu/counseling/index.shtml"); }
    @FXML private void openHealthAndWellness() { openURL("https://www.farmingdale.edu/health-services/index.shtml"); }
    @FXML private void openTimelyCare()        { openURL("https://timelycare.com/farmingdale"); }
}
