package com.nightowl;

import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
    @FXML private VBox      panelTips;
    @FXML private Button    btnTips;
    @FXML private VBox      panelMyResources;
    @FXML private Button    btnMyResources;
    @FXML private VBox      panelAdmin;
    @FXML private Button    btnAdmin;
    @FXML private Label     headerLabel;
    @FXML private Label     headerUserLabel;

    @FXML private Slider sliderMood;
    @FXML private Slider sliderSleep;
    @FXML private Slider sliderStress;
    @FXML private Slider sliderStudy;
    @FXML private Label  labelMood;
    @FXML private Label  labelSleep;
    @FXML private Label  labelStress;
    @FXML private Label  labelStudy;
    @FXML private Label  labelWellnessSaved;
    @FXML private VBox   wellnessChartContainer;
    @FXML private VBox   wellnessSummaryContainer;

    // ── State ─────────────────────────────────────────────────────────────────

    private boolean sidebarExpanded = true;
    private UserProfile currentUser;
    private CampusResources.Campus campus;

    // ── Resource entry model ──────────────────────────────────────────────────

    record ResourceEntry(String title, String category, String location,
                         String phone, String hours, String description,
                         String url, String email) {
        boolean matches(String query, String activeCategory) {
            boolean categoryMatch = activeCategory.equals("All") || category.equals(activeCategory);
            if (!categoryMatch) return false;
            if (query.isEmpty()) return true;
            String q = query.toLowerCase();
            return title.toLowerCase().contains(q)
                || category.toLowerCase().contains(q)
                || description.toLowerCase().contains(q)
                || location.toLowerCase().contains(q);
        }
    }

    private List<ResourceEntry> allResources = new ArrayList<>();
    private String activeCategory = "All";
    private String searchQuery = "";
    private VBox resourceCardsContainer;

    // ── Nav ───────────────────────────────────────────────────────────────────

    sealed interface NavSection
            permits NavSection.Resources, NavSection.Map,
                    NavSection.Emergency, NavSection.StudyRooms,
                    NavSection.Wellness, NavSection.Tips, NavSection.MyResources, NavSection.Admin {
        record Resources  () implements NavSection {}
        record Map        () implements NavSection {}
        record Emergency  () implements NavSection {}
        record StudyRooms () implements NavSection {}
        record Wellness   () implements NavSection {}
        record Tips       () implements NavSection {}
        record MyResources() implements NavSection {}
        record Admin     () implements NavSection {}
    }

    private static final Map<String, String> LABELS_EXPANDED = Map.of(
            "toggle", "Menu", "resources", "Resources", "map", "Campus Map",
            "emergency", "Emergency", "studyrooms", "Study Rooms", "wellness", "Wellness",
            "tips", "Submit a Tip",
            "myresources", "My Resources",
            "admin", "Admin Console"
    );
    private static final Map<String, String> LABELS_COLLAPSED = Map.of(
            "toggle", "☰", "resources", "📋", "map", "🗺",
            "emergency", "🚨", "studyrooms", "📚", "wellness", "💚",
            "tips", "💡",
            "myresources", "⭐",
            "admin", "🔧"
    );

    // ── User setup ────────────────────────────────────────────────────────────

    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
        if (user != null) {
            campus = CampusResources.get(user.getSchool());
            headerLabel.setText("Welcome, " + user.getUsername());
            headerUserLabel.setText(user.getUsername() + (user.isAdmin() ? "  🔧" : ""));
            buildAllResources();
            buildResourcesPanel();
            buildMapPanel();
            buildEmergencyPanel();
            buildStudyRoomsPanel();
            buildTipsPanel();
            buildMyResourcesPanel();
            // Show admin button only for admins
            btnAdmin.setVisible(user.isAdmin());
            btnAdmin.setManaged(user.isAdmin());
            if (user.isAdmin()) buildAdminPanel();
        }
    }

    private CampusResources.Campus campus() {
        return campus != null ? campus : CampusResources.get("Farmingdale State College");
    }

    // ── Build resource list from campus data ──────────────────────────────────

    private void buildAllResources() {
        var c = campus();
        allResources.clear();
        allResources.addAll(List.of(
            new ResourceEntry(
                c.disabilityServicesName(), "Accessibility",
                c.disabilityServicesLocation(),
                c.disabilityServicesPhone(),
                c.disabilityServicesHours(),
                "Academic accommodations for students with documented disabilities.",
                c.disabilityServices(), c.disabilityEmail()
            ),
            new ResourceEntry(
                c.counselingName(), "Mental Health",
                "See website for location",
                c.counselingPhone(),
                "Monday – Friday, business hours",
                "Free confidential counseling and psychological services for enrolled students.",
                c.counseling(), ""
            ),
            new ResourceEntry(
                c.healthName(), "Health",
                "See website for location",
                c.healthPhone(),
                "Monday – Friday, business hours",
                "On-campus health services including medical care, referrals, and wellness programs.",
                c.healthAndWellness(), ""
            ),
            new ResourceEntry(
                "TimelyCare", "Mental Health",
                "Virtual (24/7)",
                "",
                "24/7 virtual care",
                "Free telehealth and mental health support available anytime, anywhere.",
                c.timelyCare(), ""
            ),
            new ResourceEntry(
                c.libraryName(), "Academic",
                "See campus map",
                c.libraryPhone(),
                "Varies by semester",
                "Library resources, study spaces, research support, and room bookings.",
                c.libraryHours(), ""
            ),
            new ResourceEntry(
                "Campus Map & Directions", "Campus",
                c.name(),
                "",
                "Available online",
                "Interactive campus map and directions to buildings and departments.",
                c.campusMap(), ""
            ),
            new ResourceEntry(
                "Shuttle & Transportation", "Campus",
                "See campus map for stops",
                "",
                "Varies by semester",
                "Campus shuttle routes, schedules, and transportation services.",
                c.shuttleTracker(), ""
            ),
            new ResourceEntry(
                "Emergency Alerts", "Safety",
                "Online signup",
                "",
                "24/7",
                "Sign up for emergency notifications and campus safety alerts.",
                c.emergencyAlerts(), ""
            ),
            new ResourceEntry(
                "University / Campus Police", "Safety",
                "See campus map",
                c.policePhone(),
                "24/7",
                "Campus law enforcement and emergency response. Call 911 for emergencies.",
                c.activeShooter(), ""
            ),
            new ResourceEntry(
                "Study Room Booking", "Academic",
                c.libraryName(),
                c.libraryPhone(),
                "During library hours",
                "Reserve group or individual study spaces on campus.",
                c.studyRoomBooking(), ""
            )
        ));
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigateTo(new NavSection.Resources());
        bindSliderLabel(sliderMood,   labelMood);
        bindSliderLabel(sliderSleep,  labelSleep);
        bindSliderLabel(sliderStress, labelStress);
        bindSliderLabel(sliderStudy,  labelStudy);
    }

    // ── Resources panel ───────────────────────────────────────────────────────

    private void buildResourcesPanel() {
        panelResources.getChildren().clear();
        panelResources.setPadding(new Insets(30));
        panelResources.setSpacing(16);

        // Title
        panelResources.getChildren().addAll(
            makeTitle("Resource Directory"),
            makeSubtitle("Campus services available to you at " + campus().name() + ".")
        );

        // Search bar
        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Search resources...");
        searchField.setStyle("""
            -fx-background-color: #120720;
            -fx-text-fill: #E0C3FC;
            -fx-prompt-text-fill: #5B3A8A;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 10 14 10 14;
            -fx-font-size: 13px;
            """);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchQuery = newVal.trim();
            refreshResourceCards();
        });
        panelResources.getChildren().add(searchField);

        // Filter chips
        String[] categories = {"All", "Accessibility", "Mental Health", "Health", "Academic", "Campus", "Safety"};
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        List<Button> chipBtns = new ArrayList<>();

        for (String cat : categories) {
            Button chip = new Button(cat);
            chip.setStyle(chipStyle(cat.equals("All")));
            chip.setOnAction(e -> {
                activeCategory = cat;
                chipBtns.forEach(b -> b.setStyle(chipStyle(b.getText().equals(cat))));
                refreshResourceCards();
            });
            chipBtns.add(chip);
            chips.getChildren().add(chip);
        }
        panelResources.getChildren().add(chips);

        // Cards container
        resourceCardsContainer = new VBox(14);
        panelResources.getChildren().add(resourceCardsContainer);

        refreshResourceCards();
    }

    private void refreshResourceCards() {
        resourceCardsContainer.getChildren().clear();
        List<ResourceEntry> filtered = allResources.stream()
            .filter(r -> r.matches(searchQuery, activeCategory))
            .toList();

        if (filtered.isEmpty()) {
            Label none = new Label("No resources found for \"" + searchQuery + "\"");
            none.setStyle("-fx-text-fill: #5B3A8A; -fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 20 0 0 0;");
            resourceCardsContainer.getChildren().add(none);
            return;
        }

        for (ResourceEntry r : filtered) {
            resourceCardsContainer.getChildren().add(buildResourceCard(r));
        }
    }

    private VBox buildResourceCard(ResourceEntry r) {
        return buildResourceCard(r, false);
    }

    private VBox buildResourceCard(ResourceEntry r, boolean inMyResources) {
        return makeCard(card -> {
            boolean[] expanded = {false};

            // Category badge
            Label badge = new Label(r.category());
            badge.setStyle("""
                -fx-background-color: #2D1050;
                -fx-text-fill: #C084FC;
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-background-radius: 4;
                -fx-padding: 2 8 2 8;
                """);

            var titleLabel = makeCardTitle(r.title());
            Button toggleBtn = makeScheduleBtn("▼  Details");
            toggleBtn.setStyle(toggleBtn.getStyle() + "-fx-font-size: 12px; -fx-padding: 6 12 6 12;");

            // Bookmark button
            boolean isBookmarked = currentUser != null &&
                DatabaseManager.getInstance().isBookmarked(currentUser.getId(), r.title());
            Button bookmarkBtn = new Button(isBookmarked ? "⭐" : "☆");
            bookmarkBtn.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: #C084FC;
                -fx-font-size: 18px;
                -fx-cursor: hand;
                -fx-padding: 2 6 2 6;
                """);
            bookmarkBtn.setOnAction(e -> {
                if (currentUser == null) return;
                boolean currently = DatabaseManager.getInstance().isBookmarked(currentUser.getId(), r.title());
                if (currently) {
                    DatabaseManager.getInstance().removeBookmark(currentUser.getId(), r.title());
                    bookmarkBtn.setText("☆");
                    if (inMyResources) buildMyResourcesPanel();
                } else {
                    DatabaseManager.getInstance().addBookmark(currentUser.getId(), r.title());
                    bookmarkBtn.setText("⭐");
                }
            });

            VBox leftCol = new VBox(4, badge, titleLabel);
            if (!r.location().isEmpty()) leftCol.getChildren().add(makeCardValue(r.location()));
            if (!r.phone().isEmpty())    leftCol.getChildren().add(makeCardValueHighlight(r.phone()));
            HBox.setHgrow(leftCol, Priority.ALWAYS);

            HBox headerRow = new HBox(10, leftCol, bookmarkBtn, toggleBtn);
            headerRow.setAlignment(Pos.CENTER_LEFT);

            // Expandable details
            VBox details = new VBox(10);
            details.setVisible(false);
            details.setManaged(false);

            if (!r.hours().isEmpty())
                details.getChildren().add(makeHBoxRow("Hours:", makeCardValue(r.hours())));
            if (!r.description().isEmpty())
                details.getChildren().add(makeCardNote(r.description()));
            if (!r.email().isEmpty())
                details.getChildren().add(makeHBoxRow("Email:", makeLink(r.email(), () -> openMailTo(r.email()))));

            HBox btnRow = new HBox(10);
            if (!r.url().isEmpty())
                btnRow.getChildren().add(makeLinkBtn("Visit Website", () -> openURL(r.url())));
            if (!r.email().isEmpty())
                btnRow.getChildren().add(makeLinkBtn("Send Email", () -> openMailTo(r.email())));
            if (!btnRow.getChildren().isEmpty())
                details.getChildren().add(btnRow);

            toggleBtn.setOnAction(e -> {
                expanded[0] = !expanded[0];
                details.setVisible(expanded[0]);
                details.setManaged(expanded[0]);
                toggleBtn.setText(expanded[0] ? "▲  Close" : "▼  Details");
            });

            card.getChildren().addAll(headerRow, details);
        });
    }

    private String chipStyle(boolean active) {
        if (active) return """
            -fx-background-color: #7C3AED;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-background-radius: 20;
            -fx-padding: 5 14 5 14;
            -fx-cursor: hand;
            """;
        return """
            -fx-background-color: #120720;
            -fx-text-fill: #A78BCA;
            -fx-font-size: 12px;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 20;
            -fx-background-radius: 20;
            -fx-padding: 5 14 5 14;
            -fx-cursor: hand;
            """;
    }

    // ── Map panel ─────────────────────────────────────────────────────────────

    private void buildMapPanel() {
        var c = campus();
        panelMap.getChildren().clear();
        panelMap.setPadding(new Insets(30));
        panelMap.setSpacing(20);

        HBox mapBtns = new HBox(12,
            makeScheduleBtn("Open Shuttle Tracker", () -> openURL(c.shuttleTracker())),
            makeScheduleBtn("Open Campus Map", () -> openURL(c.campusMap()))
        );

        panelMap.getChildren().addAll(
            makeTitle("Campus Map & Shuttle"),
            makeSubtitle("Transportation and navigation resources for " + c.name() + "."),
            mapBtns
        );
    }

    // ── Emergency panel ───────────────────────────────────────────────────────

    private void buildEmergencyPanel() {
        var c = campus();
        panelEmergency.getChildren().clear();
        panelEmergency.setPadding(new Insets(30));
        panelEmergency.setSpacing(20);

        HBox emergencyBtns = new HBox(12,
            makeScheduleBtn("🚨  Call 911", () -> openURL("tel:911")),
            makeScheduleBtn("📞  Call Campus Police", () ->
                openURL("tel:" + c.policePhone().replaceAll("[^0-9]", "")))
        );

        panelEmergency.getChildren().addAll(
            makeTitle("Emergency Response"),
            makeSubtitle("Campus emergency contacts, safety procedures, and crisis resources."),
            emergencyBtns,
            makeCard(card -> card.getChildren().addAll(
                makeCardTitle("University / Campus Police"),
                makeHBoxRow("Emergency:", makeCardValueHighlight("911")),
                makeHBoxRow("Non-Emergency:", makeCardValueHighlight(c.policePhone())),
                makeHBoxRow("Hours:", makeCardValue("24 hours a day, 7 days a week")),
                new HBox(10,
                    makeLinkBtn("Emergency Alerts", () -> openURL(c.emergencyAlerts())),
                    makeLinkBtn("Active Shooter Guide", () -> openURL(c.activeShooter())),
                    makeLinkBtn("Evacuation Procedures", () -> openURL(c.evacuation()))
                )
            )),
            makeCard(card -> card.getChildren().addAll(
                makeCardTitle("Crisis and Mental Health Support"),
                makeHBoxRow("988 Lifeline:", makeCardValueHighlight("Call or text 988")),
                makeHBoxRow("Crisis Text:", makeCardValueHighlight("Text HOME to 741741")),
                makeHBoxRow(c.counselingName() + ":", makeCardValueHighlight(c.counselingPhone())),
                makeCardNote(c.counselingName() + " is available Monday–Friday during business hours. "
                        + "For after-hours emergencies, call 988 or go to your nearest emergency room."),
                makeLinkBtn("Visit Counseling Services", () -> openURL(c.mentalHealth()))
            ))
        );
    }

    // ── Study Rooms panel ─────────────────────────────────────────────────────

    private void buildStudyRoomsPanel() {
        var c = campus();
        panelStudyRooms.getChildren().clear();
        panelStudyRooms.setPadding(new Insets(30));
        panelStudyRooms.setSpacing(20);

        panelStudyRooms.getChildren().addAll(
            makeTitle("Study Room Booking"),
            makeSubtitle("Reserve a study space at " + c.libraryName() + "."),
            makeScheduleBtn("📚  Book a Study Room", () -> openURL(c.studyRoomBooking())),
            makeCard(card -> card.getChildren().addAll(
                makeCardTitle(c.libraryName()),
                makeHBoxRow("Phone:", makeCardValueHighlight(c.libraryPhone())),
                makeCardNote("Hours may vary during finals, holidays, and intersession."),
                makeLinkBtn("Current Library Hours", () -> openURL(c.libraryHours()))
            ))
        );
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private Label makeTitle(String text) {
        var l = new Label(text); l.getStyleClass().add("panel-title"); return l;
    }
    private Label makeSubtitle(String text) {
        var l = new Label(text); l.getStyleClass().add("panel-subtitle"); l.setWrapText(true); return l;
    }
    private Label makeCardTitle(String text) {
        var l = new Label(text); l.getStyleClass().add("card-title"); return l;
    }
    private Label makeCardValue(String text) {
        var l = new Label(text); l.getStyleClass().add("card-value"); l.setWrapText(true); return l;
    }
    private Label makeCardValueHighlight(String text) {
        var l = new Label(text); l.getStyleClass().add("card-value-highlight"); return l;
    }
    private Label makeCardNote(String text) {
        var l = new Label(text); l.getStyleClass().add("card-note");
        l.setWrapText(true); l.setMaxWidth(500); return l;
    }
    private Button makeLink(String text, Runnable action) {
        var b = new Button(text); b.getStyleClass().add("card-link-btn");
        b.setOnAction(e -> action.run()); return b;
    }
    private Button makeLinkBtn(String text, Runnable action) { return makeLink(text, action); }
    private Button makeScheduleBtn(String text) {
        var b = new Button(text); b.getStyleClass().add("schedule-btn"); return b;
    }
    private Button makeScheduleBtn(String text, Runnable action) {
        var b = makeScheduleBtn(text); b.setOnAction(e -> action.run()); return b;
    }
    private HBox makeHBoxRow(String labelText, javafx.scene.Node value) {
        var lbl = new Label(labelText); lbl.getStyleClass().add("card-label"); lbl.setMinWidth(110);
        var row = new HBox(10, lbl, value); row.setAlignment(Pos.CENTER_LEFT); return row;
    }

    @FunctionalInterface interface CardBuilder { void build(VBox card); }
    private VBox makeCard(CardBuilder builder) {
        var card = new VBox(12); card.getStyleClass().add("resource-card");
        card.setPadding(new Insets(20)); builder.build(card); return card;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    @FXML
    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        var labels = sidebarExpanded ? LABELS_EXPANDED : LABELS_COLLAPSED;
        sidebar.setPrefWidth(sidebarExpanded ? 220 : 60);
        if (sidebarExpanded) sidebar.getStyleClass().remove("sidebar-collapsed");
        else sidebar.getStyleClass().add("sidebar-collapsed");
        toggleSidebarBtn.setText(labels.get("toggle"));
        btnResources .setText(labels.get("resources"));
        btnMap       .setText(labels.get("map"));
        btnEmergency .setText(labels.get("emergency"));
        btnStudyRooms.setText(labels.get("studyrooms"));
        btnWellness  .setText(labels.get("wellness"));
        btnTips      .setText(labels.get("tips"));
        btnMyResources.setText(labels.get("myresources"));
        if (currentUser != null && currentUser.isAdmin()) btnAdmin.setText(labels.get("admin"));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void navResources () { navigateTo(new NavSection.Resources());  }
    @FXML private void navMap       () { navigateTo(new NavSection.Map());        }
    @FXML private void navEmergency () { navigateTo(new NavSection.Emergency());  }
    @FXML private void navStudyRooms() { navigateTo(new NavSection.StudyRooms()); }
    @FXML private void navWellness  () { navigateTo(new NavSection.Wellness());   }
    @FXML private void navTips       () { navigateTo(new NavSection.Tips());        }
    @FXML private void navMyResources () { buildMyResourcesPanel(); navigateTo(new NavSection.MyResources()); }
    @FXML private void navAdmin       () { buildAdminPanel(); navigateTo(new NavSection.Admin()); }

    private void navigateTo(NavSection section) {
        var target = switch (section) {
            case NavSection.Resources  s -> new NavTarget(panelResources,  btnResources,  "Resource Directory");
            case NavSection.Map        s -> new NavTarget(panelMap,        btnMap,        "Campus Map and Shuttle Tracker");
            case NavSection.Emergency  s -> new NavTarget(panelEmergency,  btnEmergency,  "Emergency Response");
            case NavSection.StudyRooms s -> new NavTarget(panelStudyRooms, btnStudyRooms, "Study Room Booking");
            case NavSection.Wellness   s -> new NavTarget(panelWellness,   btnWellness,   "Personal Wellness Log");
            case NavSection.Tips       s -> new NavTarget(panelTips,       btnTips,       "Submit a Resource Tip");
            case NavSection.MyResources s -> new NavTarget(panelMyResources, btnMyResources, "My Resources");
            case NavSection.Admin       s -> new NavTarget(panelAdmin,       btnAdmin,       "Admin Console");
        };
        allPanels().forEach(p -> { p.setVisible(false); p.setManaged(false); });
        target.panel().setVisible(true);
        target.panel().setManaged(true);
        headerLabel.setText(target.title());
        allNavButtons().forEach(b -> b.getStyleClass().remove("nav-btn-active"));
        target.button().getStyleClass().add("nav-btn-active");
    }

    private record NavTarget(VBox panel, Button button, String title) {}
    private List<VBox> allPanels() {
        return List.of(panelResources, panelMap, panelEmergency, panelStudyRooms, panelWellness, panelTips, panelMyResources, panelAdmin);
    }
    private List<Button> allNavButtons() {
        return List.of(btnResources, btnMap, btnEmergency, btnStudyRooms, btnWellness, btnTips, btnMyResources, btnAdmin);
    }

    // ── URL helpers ───────────────────────────────────────────────────────────

    private void openURL(String url) {
        if (url == null || url.isEmpty()) return;
        try { Desktop.getDesktop().browse(new URI(url)); }
        catch (Exception e) { System.err.println("[NightOwl] Failed to open: " + url); }
    }
    private void openMailTo(String address) {
        if (address == null || address.isEmpty()) return;
        try { Desktop.getDesktop().mail(new URI("mailto:" + address)); }
        catch (Exception e) { System.err.println("[NightOwl] Failed to open mail: " + address); }
    }

    // ── Wellness ──────────────────────────────────────────────────────────────

    private void bindSliderLabel(Slider slider, Label label) {
        label.setText(String.format("%.0f", slider.getValue()));
        slider.valueProperty().addListener((obs, oldVal, newVal) ->
            label.setText(String.format("%.0f", newVal.doubleValue())));
    }

    @FXML
    private void saveWellnessEntry() {
        int mood   = (int) sliderMood.getValue();
        int sleep  = (int) sliderSleep.getValue();
        int stress = (int) sliderStress.getValue();
        int study  = (int) sliderStudy.getValue();
        var db = DatabaseManager.getInstance();
        if (db.hasEntryForToday()) {
            labelWellnessSaved.setText("⚠  You already logged an entry today.");
            return;
        }
        db.saveWellnessEntry(mood, sleep, stress, study);
        labelWellnessSaved.setText("✓  Entry saved for today.");
    }


    // ── Tips Panel ────────────────────────────────────────────────────────────

    private void buildTipsPanel() {
        panelTips.getChildren().clear();
        panelTips.setPadding(new Insets(30));
        panelTips.setSpacing(20);

        panelTips.getChildren().addAll(
            makeTitle("Submit a Resource Tip"),
            makeSubtitle("Help keep NightOwl accurate. Flag outdated info or suggest a missing resource for " + campus().name() + ".")
        );

        // Tip type selector
        Label typeLabel = new Label("What type of tip is this?");
        typeLabel.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 13px; -fx-font-weight: bold;");

        javafx.scene.control.ToggleGroup typeGroup = new javafx.scene.control.ToggleGroup();
        HBox typeRow = new HBox(12);

        String[] types = {"Outdated Info", "Missing Resource", "Wrong Link", "Other"};
        String[] typeVals = {"OUTDATED", "MISSING", "WRONG_LINK", "OTHER"};
        javafx.scene.control.RadioButton[] radios = new javafx.scene.control.RadioButton[types.length];

        for (int i = 0; i < types.length; i++) {
            radios[i] = new javafx.scene.control.RadioButton(types[i]);
            radios[i].setToggleGroup(typeGroup);
            radios[i].setStyle("-fx-text-fill: #E0C3FC; -fx-font-size: 13px;");
            final String val = typeVals[i];
            typeRow.getChildren().add(radios[i]);
        }
        radios[0].setSelected(true);

        // Resource name field
        Label resourceLabel = new Label("Resource name (optional)");
        resourceLabel.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 13px; -fx-font-weight: bold;");
        javafx.scene.control.TextField resourceField = new javafx.scene.control.TextField();
        resourceField.setPromptText("e.g. Disability Services Center");
        styleInputField(resourceField);

        // Description
        Label descLabel = new Label("Describe the issue or suggestion *");
        descLabel.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 13px; -fx-font-weight: bold;");
        javafx.scene.control.TextArea descField = new javafx.scene.control.TextArea();
        descField.setPromptText("Be as specific as possible — what's wrong or what's missing?");
        descField.setPrefRowCount(4);
        descField.setWrapText(true);
        descField.setStyle("""
            -fx-background-color: #120720;
            -fx-text-fill: #E0C3FC;
            -fx-prompt-text-fill: #5B3A8A;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-font-size: 13px;
            """);

        // Contact email
        Label emailLabel = new Label("Your email (optional, for follow-up)");
        emailLabel.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 13px; -fx-font-weight: bold;");
        javafx.scene.control.TextField emailField = new javafx.scene.control.TextField();
        emailField.setPromptText("your@email.com");
        styleInputField(emailField);

        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 13px;");
        statusLabel.setWrapText(true);

        // Submit button
        Button submitBtn = makeScheduleBtn("📩  Submit Tip");
        submitBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            if (desc.isEmpty()) {
                statusLabel.setText("⚠  Please describe the issue before submitting.");
                statusLabel.setStyle("-fx-text-fill: #E05555; -fx-font-size: 13px;");
                return;
            }

            // Get selected type
            String selectedType = "OTHER";
            for (int i = 0; i < radios.length; i++) {
                if (radios[i].isSelected()) { selectedType = typeVals[i]; break; }
            }

            String username = currentUser != null ? currentUser.getUsername() : "anonymous";
            boolean ok = DatabaseManager.getInstance().submitTip(
                username, selectedType,
                resourceField.getText().trim(),
                desc,
                emailField.getText().trim()
            );

            if (ok) {
                statusLabel.setText("✓  Tip submitted! Thank you for helping keep NightOwl accurate.");
                statusLabel.setStyle("-fx-text-fill: #34D399; -fx-font-size: 13px;");
                descField.clear();
                resourceField.clear();
                emailField.clear();
                radios[0].setSelected(true);
            } else {
                statusLabel.setText("✗  Something went wrong. Please try again.");
                statusLabel.setStyle("-fx-text-fill: #E05555; -fx-font-size: 13px;");
            }
        });

        VBox form = new VBox(14,
            typeLabel, typeRow,
            resourceLabel, resourceField,
            descLabel, descField,
            emailLabel, emailField,
            submitBtn, statusLabel
        );
        form.getStyleClass().add("resource-card");
        form.setPadding(new Insets(24));

        // Past submissions info card
        VBox infoCard = makeCard(card -> card.getChildren().addAll(
            makeCardTitle("How tips work"),
            makeCardNote("Tips are reviewed by NightOwl admins and used to keep resource listings accurate."),
            makeCardNote("You don't need to provide your email, but it helps us follow up if we need more details."),
            makeCardNote("Thank you for contributing to NightOwl! 🦉")
        ));

        panelTips.getChildren().addAll(form, infoCard);
    }

    private void styleInputField(javafx.scene.control.TextField field) {
        field.setStyle("""
            -fx-background-color: #120720;
            -fx-text-fill: #E0C3FC;
            -fx-prompt-text-fill: #5B3A8A;
            -fx-border-color: #3D1F6B;
            -fx-border-width: 1;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            -fx-padding: 10 14 10 14;
            -fx-font-size: 13px;
            """);
    }


    // ── My Resources Panel ────────────────────────────────────────────────────

    private void buildMyResourcesPanel() {
        panelMyResources.getChildren().clear();
        panelMyResources.setPadding(new Insets(30));
        panelMyResources.setSpacing(20);

        panelMyResources.getChildren().addAll(
            makeTitle("My Resources"),
            makeSubtitle("Your bookmarked resources for quick access.")
        );

        if (currentUser == null) {
            panelMyResources.getChildren().add(makeCardNote("Sign in to bookmark resources."));
            return;
        }

        var bookmarkedTitles = DatabaseManager.getInstance().getBookmarks(currentUser.getId());

        if (bookmarkedTitles.isEmpty()) {
            Label empty = new Label("No bookmarks yet. Hit ☆ on any resource to save it here.");
            empty.setStyle("-fx-text-fill: #5B3A8A; -fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 10 0 0 0;");
            panelMyResources.getChildren().add(empty);
            return;
        }

        for (String title : bookmarkedTitles) {
            allResources.stream()
                .filter(r -> r.title().equals(title))
                .findFirst()
                .ifPresent(r -> panelMyResources.getChildren().add(buildResourceCard(r, true)));
        }
    }


    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void logout() {
        currentUser = null;
        campus = null;
        var stage = (javafx.stage.Stage) sidebar.getScene().getWindow();
        new MainApp().restart(stage);
    }

    // ── Admin Console ─────────────────────────────────────────────────────────

    private void buildAdminPanel() {
        panelAdmin.getChildren().clear();
        panelAdmin.setPadding(new Insets(30));
        panelAdmin.setSpacing(20);

        panelAdmin.getChildren().addAll(
            makeTitle("Admin Console"),
            makeSubtitle("Manage users and review submitted resource tips.")
        );

        // ── Users section ──────────────────────────────────────────────────
        Label usersTitle = makeCardTitle("👤  User Management");
        VBox usersCard = new VBox(0);
        usersCard.getStyleClass().add("resource-card");
        usersCard.setPadding(new Insets(20));
        usersCard.setSpacing(0);
        usersCard.getChildren().add(usersTitle);

        var users = DatabaseManager.getInstance().getAllUsers();

        if (users.isEmpty()) {
            usersCard.getChildren().add(makeCardNote("No users found."));
        } else {
            // Header row
            HBox header = new HBox();
            header.setStyle("-fx-background-color: #2D1050; -fx-padding: 8 0 8 0;");
            header.setSpacing(0);
            for (String col : new String[]{"Username", "School", "Role"}) {
                Label lbl = new Label(col);
                lbl.setStyle("-fx-text-fill: #E0C3FC; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 0 0 0 10;");
                lbl.setPrefWidth(col.equals("Username") ? 180 : col.equals("School") ? 240 : 120);
                header.getChildren().add(lbl);
            }
            usersCard.getChildren().add(header);

            for (var u : users) {
                HBox row = new HBox();
                row.setStyle("-fx-border-color: transparent transparent #3D1F6B transparent; -fx-border-width: 1; -fx-padding: 8 0 8 0;");
                row.setAlignment(Pos.CENTER_LEFT);

                Label nameLbl = new Label(u.getUsername());
                nameLbl.setStyle("-fx-text-fill: #E0C3FC; -fx-font-size: 12px; -fx-padding: 0 0 0 10;");
                nameLbl.setPrefWidth(180);

                Label schoolLbl = new Label(u.getSchool() == null || u.getSchool().isEmpty() ? "—" : u.getSchool());
                schoolLbl.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 12px; -fx-padding: 0 0 0 10;");
                schoolLbl.setPrefWidth(240);

                Button roleBtn = new Button(u.isAdmin() ? "Admin" : "User");
                roleBtn.setStyle(u.isAdmin()
                    ? "-fx-background-color: #7C3AED; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 3 10 3 10; -fx-cursor: hand;"
                    : "-fx-background-color: #2D1050; -fx-text-fill: #A78BCA; -fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 3 10 3 10; -fx-cursor: hand;"
                );
                // Don't let admin demote themselves
                if (u.getId() == currentUser.getId()) {
                    roleBtn.setDisable(true);
                } else {
                    roleBtn.setOnAction(e -> {
                        boolean nowAdmin = !u.isAdmin();
                        DatabaseManager.getInstance().setAdmin(u.getId(), nowAdmin);
                        buildAdminPanel();
                    });
                }

                row.getChildren().addAll(nameLbl, schoolLbl, roleBtn);
                usersCard.getChildren().add(row);
            }
        }

        // ── Tips section ───────────────────────────────────────────────────
        Label tipsTitle = makeCardTitle("💡  Submitted Tips");
        VBox tipsCard = new VBox(0);
        tipsCard.getStyleClass().add("resource-card");
        tipsCard.setPadding(new Insets(20));
        tipsCard.setSpacing(0);
        tipsCard.getChildren().add(tipsTitle);

        var tips = DatabaseManager.getInstance().getAllTips();

        if (tips.isEmpty()) {
            tipsCard.getChildren().add(makeCardNote("No tips submitted yet."));
        } else {
            for (var tip : tips) {
                // tip: [ID, SUBMITTED_AT, USERNAME, TIP_TYPE, RESOURCE_NAME, DESCRIPTION, CONTACT_EMAIL, STATUS]
                String id           = tip[0];
                String submittedAt  = tip[1] != null ? tip[1].substring(0, 16) : "—";
                String username     = tip[2] != null ? tip[2] : "anonymous";
                String type         = tip[3] != null ? tip[3].replace("_", " ") : "";
                String resourceName = tip[4] != null && !tip[4].isEmpty() ? tip[4] : "—";
                String desc         = tip[5] != null ? tip[5] : "";
                String email        = tip[6] != null && !tip[6].isEmpty() ? tip[6] : "—";
                String status       = tip[7] != null ? tip[7] : "PENDING";

                VBox tipBox = new VBox(6);
                tipBox.setStyle("-fx-border-color: transparent transparent #3D1F6B transparent; -fx-border-width: 1; -fx-padding: 12 0 12 0;");

                Label tipHeader = new Label("#" + id + "  •  " + type + "  •  " + username + "  •  " + submittedAt);
                tipHeader.setStyle("-fx-text-fill: #C084FC; -fx-font-size: 12px; -fx-font-weight: bold;");

                Label tipResource = new Label("Resource: " + resourceName + "  |  Contact: " + email);
                tipResource.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 11px;");

                Label tipDesc = new Label(desc);
                tipDesc.setStyle("-fx-text-fill: #E0C3FC; -fx-font-size: 12px;");
                tipDesc.setWrapText(true);
                tipDesc.setMaxWidth(560);

                // Status buttons
                HBox statusRow = new HBox(8);
                String[] statuses = {"PENDING", "REVIEWED", "RESOLVED", "DISMISSED"};
                for (String s : statuses) {
                    Button sb = new Button(s);
                    boolean active = s.equals(status);
                    sb.setStyle(active
                        ? "-fx-background-color: #7C3AED; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 3 8 3 8; -fx-cursor: hand;"
                        : "-fx-background-color: #1A0A2E; -fx-text-fill: #5B3A8A; -fx-font-size: 10px; -fx-border-color: #3D1F6B; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 3 8 3 8; -fx-cursor: hand;"
                    );
                    sb.setOnAction(e -> {
                        DatabaseManager.getInstance().updateTipStatus(Integer.parseInt(id), s);
                        buildAdminPanel();
                    });
                    statusRow.getChildren().add(sb);
                }

                tipBox.getChildren().addAll(tipHeader, tipResource, tipDesc, statusRow);
                tipsCard.getChildren().add(tipBox);
            }
        }

        panelAdmin.getChildren().addAll(usersCard, tipsCard);
    }

    @FXML private void openCounseling()        { openURL(campus().counseling()); }
    @FXML private void openHealthAndWellness() { openURL(campus().healthAndWellness()); }
    @FXML private void openTimelyCare()        { openURL(campus().timelyCare()); }

    // ── Wellness Chart ────────────────────────────────────────────────────────

    @FXML
    private void refreshWellnessChart() {
        buildWellnessChart();
    }

    private void buildWellnessChart() {
        wellnessChartContainer.getChildren().clear();
        wellnessSummaryContainer.getChildren().clear();

        var entries = DatabaseManager.getInstance().getRecentEntries(14);

        if (entries.isEmpty()) {
            var empty = new Label("No wellness data yet. Start logging entries to see your report.");
            empty.setStyle("-fx-text-fill: #5B3A8A; -fx-font-size: 13px; -fx-font-style: italic;");
            wellnessChartContainer.getChildren().add(empty);
            return;
        }

        var sorted = new java.util.ArrayList<>(entries);
        java.util.Collections.reverse(sorted);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        xAxis.setStyle("-fx-tick-label-fill: #A78BCA;");

        NumberAxis yAxis = new NumberAxis(0, 12, 1);
        yAxis.setLabel("Value");
        yAxis.setStyle("-fx-tick-label-fill: #A78BCA;");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Wellness Trends — Last " + sorted.size() + " Entries");
        chart.setLegendVisible(true);
        chart.setAnimated(false);
        chart.setPrefHeight(320);

        XYChart.Series<String, Number> moodSeries   = new XYChart.Series<>(); moodSeries.setName("Mood");
        XYChart.Series<String, Number> sleepSeries  = new XYChart.Series<>(); sleepSeries.setName("Sleep (hrs)");
        XYChart.Series<String, Number> stressSeries = new XYChart.Series<>(); stressSeries.setName("Stress");
        XYChart.Series<String, Number> studySeries  = new XYChart.Series<>(); studySeries.setName("Study (hrs)");

        double totalMood = 0, totalSleep = 0, totalStress = 0, totalStudy = 0;

        for (var entry : sorted) {
            String date = entry.date().getMonthValue() + "/" + entry.date().getDayOfMonth();
            moodSeries.getData().add(new XYChart.Data<>(date, entry.mood()));
            sleepSeries.getData().add(new XYChart.Data<>(date, entry.sleep()));
            stressSeries.getData().add(new XYChart.Data<>(date, entry.stress()));
            studySeries.getData().add(new XYChart.Data<>(date, entry.study()));
            totalMood   += entry.mood();
            totalSleep  += entry.sleep();
            totalStress += entry.stress();
            totalStudy  += entry.study();
        }

        chart.getData().addAll(moodSeries, sleepSeries, stressSeries, studySeries);

        String[] colors = {"#C084FC", "#60A5FA", "#F87171", "#34D399"};
        chart.applyCss();
        for (int i = 0; i < chart.getData().size(); i++) {
            var series = chart.getData().get(i);
            String color = colors[i];
            if (series.getNode() != null)
                series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            for (var d : series.getData()) {
                if (d.getNode() != null)
                    d.getNode().setStyle("-fx-background-color: " + color + ", white; -fx-background-radius: 4px;");
            }
        }

        wellnessChartContainer.getChildren().add(chart);

        int n = sorted.size();
        Label summaryTitle = makeCardTitle("Averages (" + n + " entries)");
        HBox summaryRow = new HBox(12);
        summaryRow.getChildren().addAll(
            makeStat("Mood",   String.format("%.1f / 10", totalMood   / n)),
            makeStat("Sleep",  String.format("%.1f hrs",  totalSleep  / n)),
            makeStat("Stress", String.format("%.1f / 10", totalStress / n)),
            makeStat("Study",  String.format("%.1f hrs",  totalStudy  / n))
        );
        wellnessSummaryContainer.getChildren().addAll(summaryTitle, summaryRow);
    }

    private VBox makeStat(String label, String value) {
        var lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #A78BCA; -fx-font-size: 11px;");
        var val = new Label(value);
        val.setStyle("-fx-text-fill: #C084FC; -fx-font-size: 16px; -fx-font-weight: bold;");
        var box = new VBox(4, lbl, val);
        box.setStyle("-fx-background-color: #1A0A2E; -fx-border-color: #3D1F6B; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 16 12 16;");
        return box;
    }

}
