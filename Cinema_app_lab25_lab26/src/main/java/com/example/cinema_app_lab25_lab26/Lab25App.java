package com.example.cinema_app_lab25_lab26;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.Locale;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Toggle;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextFormatter;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.converter.NumberStringConverter;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.function.Consumer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class Lab25App extends Application {

    private static final List<String> GENRES =
            List.of("Toate", "Acțiune", "Dramă", "Animație", "Thriller", "SF");
    private static final Path POSTERS_DIR = Path.of("src", "main", "resources",
            "com", "example", "cinema_app_lab25_lab26", "posters");

    // --- starea comună + accesul la date (doar prin interfață) ---
    private final DataModel model = new DataModel();
    private volatile SeantaProvider provider;
    private TableView<Seanta> table;
    // --- elementele vizuale ---
    // hero
    private StackPane heroCard;
    private CoverImage heroPoster;
    private Label heroTitle, heroMeta, heroDesc;
    private Button heroBuy, heroDetails;
    // carduri
    private FlowPane cardsPane;
    // postere deja încărcate (nume fișier -> imagine; null = lipsește)
    private final Map<String, Image> posterCache = new HashMap<>();
    private FilteredList<Seanta> filtered;   // lista originală -> filtrată
    private SortedList<Seanta> sorted;       // filtrată -> sortată (aceasta merge în tabel și, la Etapa 7, în carduri)
    private BorderPane mainPanel;          // panoul mare: sus = bara, centru = vederea curentă, jos = starea
    private Node filmeView;
    private Node seanteView;
    private ToggleButton menuFilme;
    private ToggleButton menuSeante;
    private TextField searchField;
    private final ToggleGroup genreGroup = new ToggleGroup();
    private VBox upNextBox;               // zona „Urmează azi” (se umple la Etapa 7)
    private Label statusLabel;
    private Stage primaryStage;     // proprietarul modalelor
    private BorderPane appRoot;     // conținutul care primește blur
    private String css;             // adresa lui style.css (o refolosim în modale)

    /** SINGURUL loc unde se alege implementarea. Pentru testare cu memoria: return new MemorySeantaProvider(); */
    private static SeantaProvider createProvider() {
        return Database.isAvailable() ? new SqlServerSeantaProvider() : new MemorySeantaProvider();
    }

    @Override
    public void start(Stage stage) {
        filtered = new FilteredList<>(model.getSeante(), s -> true);
        sorted = new SortedList<>(filtered);
        filmeView = buildFilmeView();
        seanteView = buildSeanteView();

        mainPanel = new BorderPane();
        mainPanel.getStyleClass().add("main-panel");
        mainPanel.setTop(buildTopBar());
        mainPanel.setBottom(buildStatusBar());

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-bg");
        root.setPadding(new Insets(16));
        Node sidebar = buildSidebar();
        BorderPane.setMargin(sidebar, new Insets(0, 16, 0, 0));
        root.setLeft(sidebar);
        root.setCenter(mainPanel);

        menuFilme.setSelected(true);       // afișează vederea „Filme” (declanșează showView)

        Scene scene = new Scene(root, 1280, 800);
        // după ce creezi `root`:
        this.primaryStage = stage;
        this.appRoot = root;

        // înlocuiește linia cu stylesheet-ul:
        css = getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Lab 25 - Cinematograf ");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();

        loadData();
    }

    // ===================== ÎNCĂRCAREA DATELOR (pe fir separat) =====================

    /**
     * Conectarea la baza de date poate bloca (rețea, timeout). Pe firul interfeței grafice
     * fereastra ar îngheța, deci lucrăm într-un Task, iar rezultatul ajunge în model
     * pe firul grafic (onSucceeded rulează deja pe el).
     */
    private void loadData() {
        Task<List<Seanta>> task = new Task<>() {
            @Override
            protected List<Seanta> call() {
                provider = createProvider();
                return provider.findAll();
            }
        };
        task.setOnSucceeded(e -> model.setAll(task.getValue()));
        task.setOnFailed(e -> {                       // rezervă: dacă baza pică după conectare
            provider = new MemorySeantaProvider();
            model.setAll(provider.findAll());
        });
        Thread t = new Thread(task, "load-seante");
        t.setDaemon(true);
        t.start();
    }

    // ===================== SIDEBAR =====================

    private Node buildSidebar() {
        Label logo = new Label("CINEMA");
        logo.getStyleClass().add("logo");

        menuFilme = menuItem("Filme");
        menuSeante = menuItem("Seanțe");
        ToggleGroup menuGroup = new ToggleGroup();
        menuFilme.setToggleGroup(menuGroup);
        menuSeante.setToggleGroup(menuGroup);
        menuGroup.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) {
                old.setSelected(true);                // nu permitem să rămână nimic selectat
            } else {
                showView(now == menuFilme);
            }
        });

        Label upNextTitle = new Label("Urmează azi");
        upNextTitle.getStyleClass().add("side-heading");
        upNextBox = new VBox(10);
        model.getSeante().addListener((ListChangeListener<Seanta>) c -> refreshUpNext());
        refreshUpNext();
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox box = new VBox(8, logo, spacerOf(14), menuFilme, menuSeante,
                spacerOf(6), new Separator(), spacerOf(6), upNextTitle, upNextBox, spacer);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(24, 18, 24, 18));
        box.setPrefWidth(250);
        box.setMinWidth(250);
        box.setMaxWidth(250);
        return box;
    }

    private ToggleButton menuItem(String text) {
        ToggleButton b = new ToggleButton(text);
        Region dot = new Region();
        dot.getStyleClass().add("dot");               // punctul apare doar când e selectat
        b.setGraphic(dot);
        b.setContentDisplay(ContentDisplay.RIGHT);
        b.getStyleClass().add("menu-item");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    // ===================== BARA DE SUS =====================

    private Node buildTopBar() {
        Region icon = new Region();
        icon.getStyleClass().add("icon-search");

        searchField = new TextField();
        searchField.setPromptText("Caută film sau sală...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, old, now) -> updateFilter());
        HBox searchBox = new HBox(icon, searchField);
        searchBox.getStyleClass().add("search-box");
        searchBox.setPrefWidth(300);
        searchBox.setMinWidth(220);

        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        for (String g : GENRES) {
            ToggleButton chip = new ToggleButton(g);
            chip.getStyleClass().add("chip");
            chip.setToggleGroup(genreGroup);
            chip.setUserData(g);                      // genul se citește la Etapa 6
            chips.getChildren().add(chip);
        }
        genreGroup.selectedToggleProperty().addListener((obs, old, now) -> {
            if (now == null) {
                old.setSelected(true);
            } else {
                updateFilter();
            }
        });
        genreGroup.getToggles().get(0).setSelected(true);   // „Toate”
        genreGroup.getToggles().get(0).setSelected(true);   // „Toate”

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Adaugă seanță");
        addButton.getStyleClass().add("btn-accent");
        addButton.setOnAction(e -> new AddModal().open());

        HBox bar = new HBox(14, searchBox, chips, spacer, addButton);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // ===================== STAREA (jos) =====================

    private Node buildStatusBar() {
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.textProperty().bind(Bindings.format("Afisati %d din %d",
                Bindings.size(filtered),                 // X = câte trec de filtru
                Bindings.size(model.getSeante())));      // Y = câte există în total

        HBox bar = new HBox(statusLabel);
        bar.getStyleClass().add("status-bar");
        return bar;
    }

    // ===================== COMUTAREA VEDERILOR =====================

    /** Schimbă doar conținutul din centru; aceeași listă și același DataModel în ambele vederi. */
    private void showView(boolean filme) {
        mainPanel.setCenter(filme ? filmeView : seanteView);
    }
    // ===================== VEDEREA „SEANȚE” (TableView) =====================

    private Node buildSeanteView() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Nicio seanță de afișat"));

        TableColumn<Seanta, Number> colId     = new TableColumn<>("ID");
        TableColumn<Seanta, String> colFilm   = new TableColumn<>("Film");
        TableColumn<Seanta, String> colSala   = new TableColumn<>("Sală");
        TableColumn<Seanta, String> colOra    = new TableColumn<>("Ora");
        TableColumn<Seanta, Number> colPret   = new TableColumn<>("Preț");
        TableColumn<Seanta, Number> colLocuri = new TableColumn<>("Locuri libere");

        // setCellValueFactory: DE UNDE ia coloana valoarea (proprietatea din model)
        colId.setCellValueFactory(cd -> cd.getValue().idProperty());
        colFilm.setCellValueFactory(cd -> cd.getValue().filmProperty());
        colSala.setCellValueFactory(cd -> cd.getValue().salaProperty());
        colOra.setCellValueFactory(cd -> cd.getValue().oraProperty());
        colPret.setCellValueFactory(cd -> cd.getValue().pretProperty());
        colLocuri.setCellValueFactory(cd -> cd.getValue().locuriLibereProperty());

        colId.setPrefWidth(70);
        colFilm.setPrefWidth(300);
        colSala.setPrefWidth(110);
        colOra.setPrefWidth(90);
        colPret.setPrefWidth(110);
        colLocuri.setPrefWidth(150);
        colId.getStyleClass().add("col-center");
        colSala.getStyleClass().add("col-center");
        colOra.getStyleClass().add("col-center");
        colPret.getStyleClass().add("col-center");
        colLocuri.getStyleClass().add("col-center");

        // setCellFactory (preț): CUM se desenează celula – două zecimale, Locale.US
        colPret.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(Locale.US, "%.2f", item.doubleValue()));
            }
        });

        // setCellFactory (locuri libere): șablonul de celulă cerut de barem
        colLocuri.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);        // ÎNTOTDEAUNA apelat primul
                setStyle("");                         // resetăm stilul: celulele se refolosesc
                if (empty || item == null) {          // rând gol: fără text, fără stil
                    setText(null);
                    return;
                }
                int locuri = item.intValue();
                if (locuri == 0) {
                    setText("Sold out");
                    setStyle("-fx-background-color: #E9EAEE; -fx-text-fill: #8B909A; -fx-font-weight: bold;");
                } else if (locuri < 10) {
                    setText(String.valueOf(locuri));
                    setStyle("-fx-text-fill: #E8870E; -fx-font-weight: bold;");
                } else {
                    setText(String.valueOf(locuri));
                }
            }
        });
        table.setRowFactory(tv -> {
            TableRow<Seanta> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 2) {   // pune 2 dacă preferi dublu clic
                    onCardClick(row.getItem());
                }
            });
            return row;
        });
        table.getColumns().addAll(colId, colFilm, colSala, colOra, colPret, colLocuri);
        table.setItems(sorted);                                    // tabelul vede lista filtrată + sortată
        sorted.comparatorProperty().bind(table.comparatorProperty()); // clic pe antet = sortare          // la Etapa 6 devine SortedList-ul

        // Selecția din tabel actualizează „current” din DataModel
        table.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, now) -> model.setCurrent(now));

        VBox box = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        box.getStyleClass().add("content");
        return box;
    }
    // ===================== CĂUTARE + FILTRARE =====================

    /** Recalculează predicatul: text (film SAU sală) ȘI genul ales în chips. */
    private void updateFilter() {
        String q = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        Toggle selected = genreGroup.getSelectedToggle();
        String genre = selected == null ? "Toate" : (String) selected.getUserData();

        filtered.setPredicate(s -> matchesText(s, q) && matchesGenre(s, genre));
    }

    private boolean matchesText(Seanta s, String q) {
        if (q.isEmpty()) {
            return true;
        }
        return contains(s.getFilm(), q) || contains(s.getSala(), q);
    }

    private boolean matchesGenre(Seanta s, String genre) {
        return "Toate".equals(genre) || genre.equalsIgnoreCase(s.getGen());
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }
    // ===================== VEDEREA „FILME” (hero + carduri) =====================

    private Node buildFilmeView() {
        // ----- HERO -----
        heroPoster = new CoverImage();
        Region gradient = new Region();
        gradient.getStyleClass().add("hero-gradient");

        Label badge = new Label("Rulează acum");
        badge.getStyleClass().add("badge");

        heroTitle = new Label();
        heroTitle.getStyleClass().add("hero-title");
        heroTitle.setWrapText(true);

        heroMeta = new Label();
        heroMeta.getStyleClass().add("hero-meta");

        heroDesc = new Label();
        heroDesc.getStyleClass().add("hero-desc");
        heroDesc.setWrapText(true);
        heroDesc.setMaxWidth(520);
        heroDesc.setMaxHeight(54);                       // aprox. 3 rânduri, restul „...”

        heroBuy = new Button("Cumpără bilet");
        heroBuy.getStyleClass().add("btn-light");
        heroBuy.setOnAction(e -> { Seanta s = heroSeanta(); if (s != null) onCardClick(s); });

        heroDetails = new Button("Detalii");
        heroDetails.getStyleClass().add("btn-ghost");
        heroDetails.setOnAction(e -> { Seanta s = heroSeanta(); if (s != null) onCardClick(s); });

        VBox info = new VBox(12, badge, heroTitle, heroMeta, heroDesc, new HBox(12, heroBuy, heroDetails));
        info.setAlignment(Pos.CENTER_LEFT);
        info.setPadding(new Insets(0, 0, 0, 40));
        info.setMaxWidth(580);
        StackPane.setAlignment(info, Pos.CENTER_LEFT);

        heroCard = new StackPane(heroPoster, gradient, info);
        heroCard.getStyleClass().add("hero");
        heroCard.setMinHeight(280);
        heroCard.setPrefHeight(280);
        heroCard.setMaxHeight(280);
        roundClip(heroCard, 28);

        // ----- SECȚIUNEA „Rulează acum” -----
        Label sectionTitle = new Label("Rulează acum");
        sectionTitle.getStyleClass().add("section-title");

        cardsPane = new FlowPane(16, 16);
        cardsPane.setPadding(new Insets(6, 6, 14, 6));

        ScrollPane scroll = new ScrollPane(cardsPane);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox box = new VBox(16, heroCard, sectionTitle, scroll);
        box.getStyleClass().add("content");

        // Aceeași sursă ca tabelul: lista filtrată + sortată. Orice schimbare (căutare, gen,
        // sortare, adăugare, ștergere, modificare prin extractor) reconstruiește cardurile.
        sorted.addListener((ListChangeListener<Seanta>) c -> { refreshCards(); refreshHero(); });
        model.currentProperty().addListener((obs, old, now) -> refreshHero());
        refreshCards();
        refreshHero();
        return box;
    }

    /** Filmul din hero = seanța selectată (dacă e vizibilă în lista filtrată) sau prima din listă. */
    private Seanta heroSeanta() {
        Seanta c = model.getCurrent();
        if (c != null && sorted.contains(c)) {
            return c;
        }
        return sorted.isEmpty() ? null : sorted.get(0);
    }

    private void refreshHero() {
        Seanta s = heroSeanta();
        if (s == null) {
            heroPoster.setImage(null);
            heroTitle.setText("Nicio seanță găsită");
            heroMeta.setText("");
            heroDesc.setText("Încearcă alt text de căutare sau alt gen.");
            heroBuy.setDisable(true);
            heroDetails.setDisable(true);
            return;
        }
        heroPoster.setImage(loadPoster(s.getPoster()));
        heroTitle.setText(s.getFilm());
        heroMeta.setText(metaLine(s));
        heroDesc.setText(s.getDescriere());
        heroBuy.setDisable(s.getLocuriLibere() == 0);
        heroDetails.setDisable(false);
    }

    /** „gen • durată • IMDB rating” (modelul nu are câmp „an”, deci îl omitem). */
    private String metaLine(Seanta s) {
        List<String> parts = new ArrayList<>();
        if (s.getGen() != null && !s.getGen().isBlank()) parts.add(s.getGen());
        if (s.getDurataMin() > 0) parts.add(s.getDurataMin() + " min");
        if (s.getRating() > 0) parts.add(String.format(Locale.US, "IMDB %.1f", s.getRating()));
        return String.join(" • ", parts);
    }

    // ----- carduri -----

    private void refreshCards() {
        cardsPane.getChildren().clear();
        for (Seanta s : sorted) {
            cardsPane.getChildren().add(buildCard(s));
        }
    }

    private Node buildCard(Seanta s) {
        CoverImage cover = new CoverImage();
        cover.setImage(loadPoster(s.getPoster()));

        Region gradient = new Region();
        gradient.getStyleClass().add("card-gradient");

        Label title = new Label(s.getFilm());
        title.getStyleClass().add("card-title");
        title.setMaxWidth(118);
        Label meta = new Label(joinParts(s.getOra(), s.getSala(), s.getGen()));
        meta.getStyleClass().add("card-meta");
        meta.setMaxWidth(118);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bottom = new HBox(8, new VBox(2, title, meta), spacer, playButton(false));
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(12));
        bottom.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(bottom, Pos.BOTTOM_CENTER);

        StackPane card = new StackPane(cover, gradient, bottom);
        card.getStyleClass().add("poster-card");
        card.setPrefSize(190, 250);
        card.setMinSize(190, 250);
        card.setMaxSize(190, 250);
        roundClip(card, 22);

        if (s.getLocuriLibere() == 0) {                  // estompat + eticheta „Sold out”
            card.getStyleClass().add("sold-out");
            Label soldOut = new Label("Sold out");
            soldOut.getStyleClass().add("badge-soldout");
            StackPane.setAlignment(soldOut, Pos.TOP_LEFT);
            StackPane.setMargin(soldOut, new Insets(12));
            card.getChildren().add(soldOut);
        }

        card.setOnMouseClicked(e -> onCardClick(s));
        return card;
    }

    /** Buton rotund roșu cu iconița „play” (doar aspect; clicul îl primește cardul). */
    private Node playButton(boolean small) {
        Region icon = new Region();
        icon.getStyleClass().add("icon-play");
        StackPane b = new StackPane(icon);
        b.getStyleClass().add(small ? "round-btn-small" : "round-btn");
        return b;
    }

    // ----- „Urmează azi” (sidebar) -----

    private void refreshUpNext() {
        upNextBox.getChildren().clear();
        for (Seanta s : nextThree()) {
            upNextBox.getChildren().add(buildMiniItem(s));
        }
    }

    /** Următoarele 3 seanțe după ora curentă; dacă sunt mai puține, completăm cu cele de dimineață. */
    private List<Seanta> nextThree() {
        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        List<Seanta> byTime = model.getSeante().stream()
                .filter(s -> s.getOra() != null)
                .sorted(Comparator.comparing(Seanta::getOra))
                .toList();
        List<Seanta> result = new ArrayList<>(byTime.stream()
                .filter(s -> s.getOra().compareTo(now) >= 0).toList());
        for (Seanta s : byTime) {
            if (result.size() >= 3) break;
            if (!result.contains(s)) result.add(s);
        }
        return result.stream().limit(3).toList();
    }

    private Node buildMiniItem(Seanta s) {
        CoverImage poster = new CoverImage();
        poster.setImage(loadPoster(s.getPoster()));
        poster.getStyleClass().add("mini-poster");
        poster.setPrefSize(46, 58);
        poster.setMinSize(46, 58);
        poster.setMaxSize(46, 58);
        roundClip(poster, 10);

        Label title = new Label(s.getFilm());
        title.getStyleClass().add("mini-title");
        title.setMaxWidth(100);
        Label time = new Label(joinParts(s.getOra(), s.getSala()));
        time.getStyleClass().add("muted");
        time.setMaxWidth(100);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(10, poster, new VBox(2, title, time), spacer, playButton(true));
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("mini-item");
        row.setOnMouseClicked(e -> onCardClick(s));
        return row;
    }

    // ----- acțiuni comune -----

    /** Clic pe card, pe hero sau pe elementul din „Urmează azi”: selectează și deschide detaliile. */
    private void onCardClick(Seanta s) {
        model.setCurrent(s);
        showDetails(s);
    }

    private void showDetails(Seanta s) {
        new DetailsModal().open();       // lucrează cu model.getCurrent() (setat de onCardClick)
    }

    // ----- ajutătoare pentru postere -----

    private Image loadPoster(String name) {
        if (name == null || name.isBlank()) return null;
        if (posterCache.containsKey(name)) return posterCache.get(name);
        Image img = null;
        var url = getClass().getResource("posters/" + name);
        if (url != null) {
            Image loaded = new Image(url.toExternalForm());
            if (!loaded.isError()) img = loaded;
        } else {
            Path p = POSTERS_DIR.resolve(name);            // poster adăugat din aplicație
            if (Files.exists(p)) {
                Image loaded = new Image(p.toUri().toString());
                if (!loaded.isError()) img = loaded;
            }
        }
        posterCache.put(name, img);
        return img;
    }

    /** Colțuri rotunjite prin Rectangle clip, legat de dimensiunea regiunii. */
    private static void roundClip(Region r, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(r.widthProperty());
        clip.heightProperty().bind(r.heightProperty());
        r.setClip(clip);
    }

    private static String joinParts(String... parts) {
        List<String> ok = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.isBlank()) ok.add(p);
        }
        return String.join(" • ", ok);
    }
    // ===================== MODALE: ajutătoare comune =====================

    /**
     * O singură metodă pentru ambele modale (detalii și adăugare): Stage transparent, fără bară de sistem,
     * APPLICATION_MODAL, cu proprietarul ferestra principală; cât e deschis, conținutul principal e blurat.
     */
    private Stage showModal(Region card, Runnable onClosed) {
        card.getStyleClass().add("modal-card");
        StackPane shell = new StackPane(card);
        shell.getStyleClass().add("modal-shell");
        shell.setPadding(new Insets(24));                     // loc pentru umbră

        Stage stage = new Stage();
        stage.initOwner(primaryStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(css);
        scene.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) stage.close(); });
        stage.setScene(scene);

        // fără bară de titlu: fereastra se mută trăgând de card
        double[] drag = new double[2];
        card.setOnMousePressed(e -> {
            drag[0] = stage.getX() - e.getScreenX();
            drag[1] = stage.getY() - e.getScreenY();
        });
        card.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() + drag[0]);
            stage.setY(e.getScreenY() + drag[1]);
        });

        appRoot.setEffect(new GaussianBlur(8));               // blur cât e deschis modalul
        stage.setOnHidden(e -> {
            appRoot.setEffect(null);                          // la închidere scoatem efectul
            if (onClosed != null) onClosed.run();
        });
        stage.show();
        return stage;
    }

    /**
     * Operațiile cu baza de date pot bloca (rețea). Rulează pe un fir separat (Task);
     * onOk și onFail se execută pe firul interfeței grafice. Fără cod în plus față de atât.
     */
    private void runDb(Runnable work, Runnable onOk, Consumer<Throwable> onFail) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                work.run();
                return null;
            }
        };
        task.setOnSucceeded(e -> onOk.run());
        task.setOnFailed(e -> onFail.accept(task.getException()));
        Thread t = new Thread(task, "db-work");
        t.setDaemon(true);
        t.start();
    }

    private String friendly(Throwable ex) {
        return ex instanceof ProviderException ? ex.getMessage() : "Eroare neașteptată: " + ex;
    }

    private void errorAlert(Window owner, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        a.initOwner(owner);
        a.setHeaderText(null);
        a.getDialogPane().getStylesheets().add(css);
        a.showAndWait();
    }

    private boolean confirm(Window owner, String header, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        a.initOwner(owner);
        a.setHeaderText(header);
        a.getDialogPane().getStylesheets().add(css);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private Region spacerGrow() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
    private static Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;                    // nu e număr -> nu ajunge în model
        }
    }

    private static Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Copiază fișierul ales în folderul posters; întoarce numele fișierului (se salvează în model/baza de date). */
    private String copyPoster(File file) throws IOException {
        Files.createDirectories(POSTERS_DIR);
        Files.copy(file.toPath(), POSTERS_DIR.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        posterCache.remove(file.getName());                // forțează reîncărcarea imaginii
        return file.getName();
    }
    // ===================== AJUTĂTOARE =====================

    private Node placeholder(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("placeholder");
        VBox box = new VBox(l);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("content");
        return box;
    }

    private Label placeholderSmall(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("muted");
        return l;
    }

    private Region spacerOf(double height) {
        Region r = new Region();
        r.setMinHeight(height);
        r.setPrefHeight(height);
        return r;
    }

    public static void main(String[] args) {
        launch(args);
    }
    // ===================== MODALUL DETALII (Lucrarea 25: legare bidirecțională) =====================

    private class DetailsModal {
        private final CoverImage poster = new CoverImage();
        private final Label title = new Label();
        private final Label meta = new Label();
        private final Label desc = new Label();

        private final TextField tfFilm = field();
        private final TextField tfSala = field();
        private final TextField tfOra = field();
        private final TextField tfPret = field();
        private final TextField tfLocuri = field();

        private final SpinnerValueFactory.IntegerSpinnerValueFactory qty =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        private final Spinner<Integer> spinner = new Spinner<>();
        private final Label total = new Label();
        private final Button btnBuy = new Button("Cumpără");
        private final Label msg = new Label();

        private Seanta bound;      // elementul la care sunt legate câmpurile acum
        private Stage stage;

        /** Dacă „current” se schimbă cât e deschis modalul: dezlegăm vechiul, legăm noul. */
        private final ChangeListener<Seanta> currentListener = (obs, old, now) -> {
            if (now != null) bindTo(now);
        };

        private TextField field() {
            TextField tf = new TextField();
            tf.getStyleClass().add("form-field");
            return tf;
        }

        void open() {
            Seanta s = model.getCurrent();
            if (s == null) return;

            poster.setPrefSize(210, 300);
            poster.setMinSize(210, 300);
            poster.setMaxSize(210, 300);
            poster.getStyleClass().add("mini-poster");
            roundClip(poster, 20);

            title.getStyleClass().add("modal-title");
            title.setWrapText(true);
            meta.getStyleClass().add("muted");
            desc.getStyleClass().add("modal-desc");
            desc.setWrapText(true);
            desc.setMaxWidth(440);

            // protecție simplă la tastare (cifre); validarea completă vine la Lucrarea 26
            tfPret.setTextFormatter(new TextFormatter<>(c ->
                    c.getControlNewText().matches("\\d{0,6}([.]\\d{0,2})?") ? c : null));
            tfLocuri.setTextFormatter(new TextFormatter<>(c ->
                    c.getControlNewText().matches("\\d{0,3}") ? c : null));
            tfOra.setPromptText("HH:mm");

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(6);
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPercentWidth(50);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setPercentWidth(50);
            form.getColumnConstraints().addAll(c1, c2);
            form.add(fieldLabel("Film"), 0, 0, 2, 1);
            form.add(tfFilm, 0, 1, 2, 1);
            form.add(fieldLabel("Sală"), 0, 2);
            form.add(fieldLabel("Ora (HH:mm)"), 1, 2);
            form.add(tfSala, 0, 3);
            form.add(tfOra, 1, 3);
            form.add(fieldLabel("Preț bilet"), 0, 4);
            form.add(fieldLabel("Locuri libere"), 1, 4);
            form.add(tfPret, 0, 5);
            form.add(tfLocuri, 1, 5);

            spinner.setValueFactory(qty);
            spinner.setEditable(true);
            spinner.setPrefWidth(96);
            total.getStyleClass().add("total-label");
            btnBuy.getStyleClass().add("btn-accent");
            btnBuy.setOnAction(e -> onBuy());
            HBox buyRow = new HBox(12, new Label("Bilete"), spinner, new Label("Total:"), total,
                    spacerGrow(), btnBuy);
            buyRow.setAlignment(Pos.CENTER_LEFT);

            msg.getStyleClass().add("msg-ok");
            msg.visibleProperty().bind(msg.textProperty().isNotEmpty());
            msg.managedProperty().bind(msg.visibleProperty());

            Button btnSave = new Button("Salvează");
            btnSave.getStyleClass().add("btn-accent");
            btnSave.setOnAction(e -> onSave());
            Button btnDelete = new Button("Șterge");
            btnDelete.getStyleClass().add("btn-outline");
            btnDelete.setOnAction(e -> onDelete());
            Button btnClose = new Button("Închide");
            btnClose.getStyleClass().add("btn-text");
            btnClose.setOnAction(e -> closeModal());
            HBox actions = new HBox(10, btnSave, btnDelete, spacerGrow(), btnClose);
            actions.setAlignment(Pos.CENTER_LEFT);

            VBox right = new VBox(12, title, meta, desc, form,
                    fieldLabel("Cumpără bilet"), buyRow, msg, actions);
            right.setPrefWidth(440);
            HBox content = new HBox(28, poster, right);
            HBox.setHgrow(right, Priority.ALWAYS);

            bindTo(s);
            model.currentProperty().addListener(currentListener);
            stage = showModal(content, () -> {
                model.currentProperty().removeListener(currentListener);
                unbind();                                   // plasă de siguranță (ex.: închis cu ESC)
            });
        }

        /** Leagă câmpurile de seanța dată; întâi dezleagă elementul VECHI. */
        private void bindTo(Seanta s) {
            unbind();
            bound = s;

            tfFilm.textProperty().bindBidirectional(s.filmProperty());
            tfSala.textProperty().bindBidirectional(s.salaProperty());
            tfOra.textProperty().bindBidirectional(s.oraProperty());
            tfPret.textProperty().bindBidirectional(s.pretProperty(), new NumberStringConverter(Locale.US));
            tfLocuri.textProperty().bindBidirectional(s.locuriLibereProperty(), new NumberStringConverter("#"));

            title.textProperty().bind(s.filmProperty());
            meta.setText(metaLine(s));
            desc.setText(s.getDescriere());
            poster.setImage(loadPoster(s.getPoster()));

            // Cumpărare: Spinner 1..locuriLibere, cost total care se actualizează singur
            qty.maxProperty().bind(Bindings.createIntegerBinding(
                    () -> Math.max(1, s.getLocuriLibere()), s.locuriLibereProperty()));
            qty.setValue(1);
            spinner.disableProperty().bind(s.locuriLibereProperty().isEqualTo(0));
            btnBuy.disableProperty().bind(s.locuriLibereProperty().isEqualTo(0));
            total.textProperty().bind(Bindings.createStringBinding(
                    () -> String.format(Locale.US, "%.2f", qty.getValue() * s.getPret()),
                    qty.valueProperty(), s.pretProperty()));
            msg.setText("");
        }

        private void unbind() {
            if (bound == null) return;
            Seanta s = bound;
            tfFilm.textProperty().unbindBidirectional(s.filmProperty());
            tfSala.textProperty().unbindBidirectional(s.salaProperty());
            tfOra.textProperty().unbindBidirectional(s.oraProperty());
            tfPret.textProperty().unbindBidirectional(s.pretProperty());
            tfLocuri.textProperty().unbindBidirectional(s.locuriLibereProperty());
            title.textProperty().unbind();
            qty.maxProperty().unbind();
            spinner.disableProperty().unbind();
            btnBuy.disableProperty().unbind();
            total.textProperty().unbind();
            bound = null;
        }

        private void closeModal() {
            model.currentProperty().removeListener(currentListener);
            unbind();                                       // câmpurile se dezleagă ÎNAINTE de închidere
            stage.close();
        }

        private void onBuy() {
            Seanta s = bound;
            if (s == null) return;
            int n = qty.getValue();
            int old = s.getLocuriLibere();
            if (n < 1 || n > old) return;

            double sum = n * s.getPret();
            s.setLocuriLibere(old - n);                     // scădem în model: tabelul, cardurile și celula
            // gri se actualizează singure (extractor)
            runDb(() -> provider.save(s),                   // UPDATE în baza de date
                    () -> {
                        qty.setValue(1);
                        msg.setText(String.format(Locale.US, "Ai cumpărat %d bilet(e). Total: %.2f", n, sum));
                    },
                    ex -> {
                        s.setLocuriLibere(old);             // dacă salvarea eșuează, anulăm scăderea
                        errorAlert(stage, friendly(ex));
                    });
        }

        private void onSave() {
            Seanta s = bound;
            if (s == null) return;
            runDb(() -> provider.save(s),
                    () -> msg.setText("Modificările au fost salvate."),
                    ex -> errorAlert(stage, friendly(ex)));
        }

        private void onDelete() {
            Seanta s = bound;
            if (s == null) return;
            boolean ok = confirm(stage, "Ștergi seanța?",
                    s.getFilm() + " (" + s.getSala() + ", " + s.getOra() + "). Acțiunea nu poate fi anulată.");
            if (!ok) return;

            int id = s.getId();
            runDb(() -> provider.delete(id),                // 1) baza de date
                    () -> {
                        model.currentProperty().removeListener(currentListener);
                        unbind();
                        model.getSeante().remove(s);        // 2) lista ORIGINALĂ
                        if (model.getCurrent() == s) model.setCurrent(null);
                        stage.close();
                    },
                    ex -> errorAlert(stage, friendly(ex)));
        }
    }
    /** Afișează o imagine „cover”: umple regiunea, taie excesul, nu deformează. */
    // ===================== MODALUL ADĂUGARE (Lucrarea 25) =====================

    private class AddModal {
        private final TextField tfFilm = field();
        private final ComboBox<String> cbGen = new ComboBox<>();
        private final TextField tfSala = field();
        private final TextField tfOra = field();
        private final TextField tfPret = field();
        private final TextField tfLocuri = field();
        private final TextField tfDurata = field();
        private final TextField tfRating = field();
        private final TextArea taDesc = new TextArea();

        private final CoverImage preview = new CoverImage();
        private final Label posterName = new Label("Niciun poster ales");
        private final Label msg = new Label();

        private File posterFile;
        private Stage stage;

        private TextField field() {
            TextField tf = new TextField();
            tf.getStyleClass().add("form-field");
            return tf;
        }

        void open() {
            // gen: toate genurile din chips, fără „Toate”
            cbGen.getItems().addAll(GENRES.subList(1, GENRES.size()));
            cbGen.getSelectionModel().selectFirst();
            cbGen.getStyleClass().add("form-field");
            cbGen.setMaxWidth(Double.MAX_VALUE);

            tfOra.setPromptText("HH:mm");
            tfPret.setPromptText("ex. 85.50");
            // cifre la tastare; verificarea finală se face la Salvează
            tfPret.setTextFormatter(new TextFormatter<>(c ->
                    c.getControlNewText().matches("\\d{0,6}([.]\\d{0,2})?") ? c : null));
            tfLocuri.setTextFormatter(new TextFormatter<>(c ->
                    c.getControlNewText().matches("\\d{0,3}") ? c : null));
            tfDurata.setTextFormatter(new TextFormatter<>(c ->
                    c.getControlNewText().matches("\\d{0,3}") ? c : null));
            tfRating.setTextFormatter(new TextFormatter<>(c ->
                    c.getControlNewText().matches("\\d{0,2}([.]\\d?)?") ? c : null));

            taDesc.getStyleClass().add("form-field");
            taDesc.setWrapText(true);
            taDesc.setPrefRowCount(3);

            // ----- poster -----
            preview.setPrefSize(150, 215);
            preview.setMinSize(150, 215);
            preview.setMaxSize(150, 215);
            preview.getStyleClass().add("mini-poster");
            roundClip(preview, 18);
            posterName.getStyleClass().add("muted");
            posterName.setMaxWidth(150);
            Button btnPoster = new Button("Alege poster...");
            btnPoster.getStyleClass().add("btn-outline");
            btnPoster.setOnAction(e -> choosePoster());
            VBox left = new VBox(10, preview, btnPoster, posterName);

            // ----- formular -----
            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(6);
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPercentWidth(50);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setPercentWidth(50);
            form.getColumnConstraints().addAll(c1, c2);
            form.add(fieldLabel("Film"), 0, 0, 2, 1);
            form.add(tfFilm, 0, 1, 2, 1);
            form.add(fieldLabel("Gen"), 0, 2);
            form.add(fieldLabel("Sală"), 1, 2);
            form.add(cbGen, 0, 3);
            form.add(tfSala, 1, 3);
            form.add(fieldLabel("Ora (HH:mm)"), 0, 4);
            form.add(fieldLabel("Preț bilet"), 1, 4);
            form.add(tfOra, 0, 5);
            form.add(tfPret, 1, 5);
            form.add(fieldLabel("Locuri libere"), 0, 6);
            form.add(fieldLabel("Durată (min)"), 1, 6);
            form.add(tfLocuri, 0, 7);
            form.add(tfDurata, 1, 7);
            form.add(fieldLabel("Rating (ex. 8.2)"), 0, 8);
            form.add(tfRating, 0, 9);
            form.add(fieldLabel("Descriere"), 0, 10, 2, 1);
            form.add(taDesc, 0, 11, 2, 1);

            Label heading = new Label("Seanță nouă");
            heading.getStyleClass().add("modal-title");

            msg.getStyleClass().add("msg-error");
            msg.setWrapText(true);
            msg.visibleProperty().bind(msg.textProperty().isNotEmpty());
            msg.managedProperty().bind(msg.visibleProperty());

            Button btnSave = new Button("Salvează");
            btnSave.getStyleClass().add("btn-accent");
            btnSave.setOnAction(e -> onSave());
            Button btnCancel = new Button("Anulează");
            btnCancel.getStyleClass().add("btn-text");
            btnCancel.setOnAction(e -> stage.close());          // închide fără modificări
            HBox actions = new HBox(10, btnSave, btnCancel);
            actions.setAlignment(Pos.CENTER_LEFT);

            VBox right = new VBox(12, heading, form, msg, actions);
            right.setPrefWidth(440);
            HBox content = new HBox(28, left, right);
            HBox.setHgrow(right, Priority.ALWAYS);

            stage = showModal(content, null);
        }

        private void choosePoster() {
            FileChooser fc = new FileChooser();
            fc.setTitle("Alege posterul");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imagini", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
            File f = fc.showOpenDialog(stage);
            if (f == null) return;
            Image img = new Image(f.toURI().toString());
            if (img.isError()) {
                msg.setText("Fișierul ales nu este o imagine validă.");
                return;
            }
            posterFile = f;
            preview.setImage(img);
            posterName.setText(f.getName());
            msg.setText("");
        }

        private void onSave() {
            String film = tfFilm.getText().trim();
            String sala = tfSala.getText().trim();
            String ora = tfOra.getText().trim();
            Double pret = parseDouble(tfPret.getText());
            Integer locuri = parseInt(tfLocuri.getText());

            if (film.isEmpty() || sala.isEmpty() || ora.isEmpty()) {
                msg.setText("Completează filmul, sala și ora.");
                return;
            }
            if (pret == null) {                                  // text care nu e număr: NU intră în model
                msg.setText("Prețul trebuie să fie un număr (ex. 85.50).");
                return;
            }
            if (locuri == null) {
                msg.setText("Locurile libere trebuie să fie un număr întreg.");
                return;
            }
            Integer durata = parseInt(tfDurata.getText());       // câmpuri opționale: gol = 0
            Double rating = parseDouble(tfRating.getText());

            String poster = "";
            if (posterFile != null) {
                try {
                    poster = copyPoster(posterFile);
                } catch (IOException ex) {
                    errorAlert(stage, "Nu am putut copia posterul: " + ex.getMessage());
                    return;
                }
            }

            Seanta s = new Seanta(0, film, sala, ora, pret, locuri,
                    cbGen.getValue(), durata == null ? 0 : durata, rating == null ? 0 : rating,
                    taDesc.getText().trim(), poster);

            runDb(() -> provider.save(s),                        // id == 0 -> INSERT, se setează id-ul generat
                    () -> {
                        model.getSeante().add(s);                // în lista ORIGINALĂ, nu în SortedList
                        searchField.clear();                     // curățăm căutarea și genul,
                        genreGroup.getToggles().get(0).setSelected(true);   // ca rândul nou să fie vizibil
                        model.setCurrent(s);
                        table.getSelectionModel().select(s);     // îl selectăm
                        table.scrollTo(s);
                        stage.close();
                    },
                    ex -> msg.setText(friendly(ex)));            // ex.: sală + oră deja ocupate (UNIQUE)
        }
    }
    private static class CoverImage extends Region {
        private final ImageView view = new ImageView();

        CoverImage() {
            view.setPreserveRatio(false);
            view.setSmooth(true);
            getChildren().add(view);
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        void setImage(Image img) {
            view.setImage(img);
            requestLayout();
        }

        @Override
        protected void layoutChildren() {
            Image img = view.getImage();
            double w = getWidth(), h = getHeight();
            if (img == null || w <= 0 || h <= 0) {
                view.setVisible(false);
                return;
            }
            double iw = img.getWidth(), ih = img.getHeight();
            double target = w / h;
            if (iw / ih > target) {                       // imaginea e mai lată: tăiem pe lateral
                double vw = ih * target;
                view.setViewport(new Rectangle2D((iw - vw) / 2, 0, vw, ih));
            } else {                                      // mai înaltă: tăiem sus/jos
                double vh = iw / target;
                view.setViewport(new Rectangle2D(0, (ih - vh) / 2, iw, vh));
            }
            view.setFitWidth(w);
            view.setFitHeight(h);
            view.setVisible(true);
        }
    }
}