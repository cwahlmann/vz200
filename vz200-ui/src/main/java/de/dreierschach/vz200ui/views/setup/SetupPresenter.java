package de.dreierschach.vz200ui.views.setup;

import com.hilerio.ace.AceTheme;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import com.vaadin.flow.theme.lumo.Lumo;
import de.dreierschach.vz200ui.config.Config;
import de.dreierschach.vz200ui.service.Vz200Service;
import de.dreierschach.vz200ui.util.ComponentFactory;
import de.dreierschach.vz200ui.views.Presenter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@SpringComponent
@VaadinSessionScope
public class SetupPresenter extends Presenter<SetupView> {
    public static final Logger log = LoggerFactory.getLogger(SetupPresenter.class);

    private final Vz200Service vz200Service;
    private final Config config;
    private Binder<Config> binder;
    private final Set<String> knownDevices = new HashSet<>() {{
        add("localhost");
    }};
    private int testDevicesProgress = 0;

    @Autowired
    public SetupPresenter(Vz200Service vz200Service, Config config) {
        this.vz200Service = vz200Service;
        this.config = config;
    }

    @Override
    protected void doBind() {
        switchTheme(getTheme(config));

        binder = new Binder<>();
        binder.setBean(config);

        binder.bind(view.themeVariantComboBox, this::getTheme, this::setTheme);

        view.selectHostComboBox.setItems(knownDevices);

        binder.bind(view.selectHostComboBox, c -> c.getOrDefault(Config.HOSTNAME, "localhost"), (c, v) -> {
            if (StringUtils.isNotEmpty(v)) {
                c.set(Config.HOSTNAME, v);
            }
        });

        view.selectHostComboBox.addCustomValueSetListener(event -> {
            knownDevices.add(event.getDetail());
            view.selectHostComboBox.setItems(knownDevices);
            view.selectHostComboBox.setValue(event.getDetail());
        });

        view.scanButton.addClickListener(event -> {
            String hostname = getBaseNet(view.selectHostComboBox.getValue());
            int port = StringUtils.isNumeric(view.portField.getValue()) ? Integer.parseInt(view.portField.getValue())
                                                                        : 8080;
            log.info("Scanning for emulator on http://" + hostname + ".*:" + port + "...");
            this.testDevicesProgress = 0;
            vz200Service.scanForDevices(hostname, port, this::onDeviceFound, this::onTestDevicesProgress);
        });

        binder.withValidator(c -> {
            String port = c.getOrDefault(Config.PORT, "8080");
            return StringUtils.isNotEmpty(port) && StringUtils.isNumeric(port);
        }, "port must be numeric")
              .bind(view.portField, c -> c.getOrDefault(Config.PORT, "8080"), (c, v) -> c.set(Config.PORT, v));

        view.applyButton.addClickListener(event -> saveConfig());
        view.undoButton.addClickListener(event -> loadConfig());

        view.testConnectionButton.addClickListener(event -> {
            try {
                ComponentFactory.info("Connection established: " + vz200Service.getJemuVersion());
            } catch (Exception e) {
                ComponentFactory.warning("Connection failed: " + e.getMessage());
            }
        });

        view.sourceEditor.setValue(SOURCE_EXAMPLE);
        view.themeComboBox.addValueChangeListener(event -> view.sourceEditor.setTheme(event.getValue()));
        binder.bind(view.themeComboBox,
                    c -> AceTheme.valueOf(c.getOrDefault(Config.ACE_THEME, AceTheme.ambiance.toString())),
                    (c, v) -> c.set(Config.ACE_THEME, v.name()));
        loadConfig();
    }

    private void onDeviceFound(String adr) {
        UI ui = view.getUI().orElse(null);
        if (ui == null) {
            return;
        }
        ui.access(() -> {
            this.knownDevices.add(adr);
            view.selectHostComboBox.setItems(knownDevices);
            view.selectHostComboBox.setValue(adr);
            ComponentFactory.info("device found at " + adr);
            ui.push();
        });
    }

    private void onTestDevicesProgress(String adr) {
        UI ui = view.getUI().orElse(null);
        if (ui == null) {
            return;
        }
        ui.access(() -> {
            testDevicesProgress++;
            if (testDevicesProgress < 254) {
                view.scanButton.setText(String.format("Scan %3d%%", 100 * testDevicesProgress / 254));
            } else {
                view.scanButton.setText("Scan");
            }
            ui.push();
        });
    }

    private String getBaseNet(String hostname) {
        if (!StringUtils.isEmpty(hostname)) {
            String[] parts = hostname.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2];
            }
        }
        return "192.168.1";
    }

    private void setTheme(Config config, String variant) {
        config.set(Config.APP_THEME_VARIANT, variant);
        switchTheme(variant);
    }

    private String getTheme(Config config) {
        return config.getOrDefault(Config.APP_THEME_VARIANT, Lumo.DARK);
    }

    private void switchTheme(String variant) {
        UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute(\"theme\",\"" + variant + "\")");
    }

    private void loadConfig() {
        try {
            config.load();
            binder.setBean(config);
            ComponentFactory.info("Configuration loaded.");
        } catch (IOException e) {
            ComponentFactory.danger("Unable to load configuration: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try {
            config.save();
            ComponentFactory.info("Configuration saved.");
        } catch (IOException e) {
            ComponentFactory.danger("Unable to save configuration: " + e.getMessage());
        }
    }

    public static final String SOURCE_EXAMPLE = "5 CLEAR5000\n" + "10 GOSUB 2500:REM TITEL\n" +
                                                "20 REM --- NEUES SPIEL ---\n" + "30 ST=12:R$=\"\"\n" +
                                                "40 GOSUB2400\n" + "60 GOSUB1000\n" + "70 PRINT@204,L$\n" +
                                                "72 IF W$=L$ THEN 200:REM GESCHAFFT\n" +
                                                "75 IF ST=0 THEN 300:REM VERLOREN\n" + "77 PRINT@256,R$\n" +
                                                "80 PRINT@320,\"WELCHEN BUCHSTABEN RAETST DU\";\n" +
                                                "90 INPUT B$:IF B$<\"A\" OR B$>\"Z\" THEN 80\n" +
                                                "95 GOSUB 2700:REM BUCHSTABEN MERKEN\n" + "100 GOSUB2600:REM CHECK\n" +
                                                "110 IF OK=0 THEN ST=ST-1\n" + "120 GOTO 60\n" +
                                                "200 REM --- GESCHAFFT\n" +
                                                "210 PRINT:PRINT\"DU HAST DAS WORT ERRATEN!\":PRINT\n" +
                                                "220 GOTO400\n" + "300 REM --- VERLOREN\n" +
                                                "310 PRINT:PRINT\"+++ DAS WARS. DU HAENGST +++\"\n" +
                                                "320 PRINT\"DAS WORT WAR '\";W$;\"'\":PRINT\n" + "330 GOTO400\n" +
                                                "400 REM --- ENDE\n" + "410 PRINT\"NOCH EIN SPIEL (J/N)?\"\n" +
                                                "420 A$=INKEY$\n" + "430 IF A$=\"J\" THEN 20\n" +
                                                "440 IF A$<>\"N\" THEN 420\n" + "450 CLS:PRINT\"BIS BALD!\"\n" +
                                                "460 END\n" + "1000 REM HANG THE MAN\n" + "1010 REM ST=[0..10]\n" +
                                                "1015 CLS\n" + "1020 IF ST=0 THEN 1100\n" + "1021 IF ST=1 THEN 1200\n" +
                                                "1022 IF ST=2 THEN 1300\n" + "1023 IF ST=3 THEN 1400\n" +
                                                "1024 IF ST=4 THEN 1500\n" + "1025 IF ST=5 THEN 1600\n" +
                                                "1026 IF ST=6 THEN 1700\n" + "1027 IF ST=7 THEN 1800\n" +
                                                "1028 IF ST=8 THEN 1900\n" + "1029 IF ST=9 THEN 2000\n" +
                                                "1030 IF ST=10 THEN 2100\n" + "1031 IF ST=11 THEN 2200\n" +
                                                "1032 IF ST=12 THEN 2300\n" + "1033 RETURN\n" +
                                                "1100 PRINT\"   ==== \"\n" + "1110 PRINT\"   I/ : \"\n" +
                                                "1120 PRINT\"   I  O \"\n" + "1130 PRINT\"   I /#\\\"\n" +
                                                "1140 PRINT\"   I  !\\\"\n" + "1150 PRINT\"  JIL   \"\n" +
                                                "1160 PRINT\"-=====---\"\n" + "1170 RETURN\n" +
                                                "1200 PRINT\"   ==== \"\n" + "1210 PRINT\"   I/ : \"\n" +
                                                "1220 PRINT\"   I  O \"\n" + "1230 PRINT\"   I /# \"\n" +
                                                "1240 PRINT\"   I  !\\\"\n" + "1250 PRINT\"  JIL   \"\n" +
                                                "1260 PRINT\"-=====---\"\n" + "1270 RETURN\n" +
                                                "1300 PRINT\"   ==== \"\n" + "1310 PRINT\"   I/ : \"\n" +
                                                "1320 PRINT\"   I  O \"\n" + "1330 PRINT\"   I  # \"\n" +
                                                "1340 PRINT\"   I  !\\\"\n" + "1350 PRINT\"  JIL   \"\n" +
                                                "1360 PRINT\"-=====---\"\n" + "1370 RETURN\n" +
                                                "1400 PRINT\"   ==== \"\n" + "1410 PRINT\"   I/ : \"\n" +
                                                "1420 PRINT\"   I  O \"\n" + "1430 PRINT\"   I  # \"\n" +
                                                "1440 PRINT\"   I   \\\"\n" + "1450 PRINT\"  JIL   \"\n" +
                                                "1460 PRINT\"-=====---\"\n" + "1470 RETURN\n" +
                                                "1500 PRINT\"   ==== \"\n" + "1510 PRINT\"   I/ : \"\n" +
                                                "1520 PRINT\"   I  O \"\n" + "1530 PRINT\"   I  # \"\n" +
                                                "1540 PRINT\"   I    \"\n" + "1550 PRINT\"  JIL   \"\n" +
                                                "1560 PRINT\"-=====---\"\n" + "1570 RETURN\n" +
                                                "1600 PRINT\"   ==== \"\n" + "1610 PRINT\"   I/ : \"\n" +
                                                "1620 PRINT\"   I  O \"\n" + "1630 PRINT\"   I    \"\n" +
                                                "1640 PRINT\"   I    \"\n" + "1650 PRINT\"  JIL   \"\n" +
                                                "1660 PRINT\"-=====---\"\n" + "1670 RETURN\n" +
                                                "1700 PRINT\"   ==== \"\n" + "1710 PRINT\"   I/ : \"\n" +
                                                "1720 PRINT\"   I    \"\n" + "1730 PRINT\"   I    \"\n" +
                                                "1740 PRINT\"   I    \"\n" + "1750 PRINT\"  JIL   \"\n" +
                                                "1760 PRINT\"-=====---\"\n" + "1770 RETURN\n" +
                                                "1800 PRINT\"   ==== \"\n" + "1810 PRINT\"   I/   \"\n" +
                                                "1820 PRINT\"   I    \"\n" + "1830 PRINT\"   I    \"\n" +
                                                "1840 PRINT\"   I    \"\n" + "1850 PRINT\"  JIL   \"\n" +
                                                "1860 PRINT\"-=====---\"\n" + "1870 RETURN\n" +
                                                "1900 PRINT\"   ==== \"\n" + "1910 PRINT\"   I    \"\n" +
                                                "1920 PRINT\"   I    \"\n" + "1930 PRINT\"   I    \"\n" +
                                                "1940 PRINT\"   I    \"\n" + "1950 PRINT\"  JIL   \"\n" +
                                                "1960 PRINT\"-=====---\"\n" + "1970 RETURN\n" +
                                                "2000 PRINT\"        \"\n" + "2010 PRINT\"   I    \"\n" +
                                                "2020 PRINT\"   I    \"\n" + "2030 PRINT\"   I    \"\n" +
                                                "2040 PRINT\"   I    \"\n" + "2050 PRINT\"  JIL   \"\n" +
                                                "2060 PRINT\"-=====---\"\n" + "2070 RETURN\n" +
                                                "2100 PRINT\"        \"\n" + "2110 PRINT\"   I    \"\n" +
                                                "2120 PRINT\"   I    \"\n" + "2130 PRINT\"   I    \"\n" +
                                                "2140 PRINT\"   I    \"\n" + "2150 PRINT\"  JI    \"\n" +
                                                "2160 PRINT\"-=====---\"\n" + "2170 RETURN\n" +
                                                "2200 PRINT\"        \"\n" + "2210 PRINT\"        \"\n" +
                                                "2220 PRINT\"        \"\n" + "2230 PRINT\"        \"\n" +
                                                "2240 PRINT\"        \"\n" + "2250 PRINT\"  J     \"\n" +
                                                "2260 PRINT\"-=====---\"\n" + "2270 RETURN\n" +
                                                "2300 PRINT\"        \"\n" + "2310 PRINT\"        \"\n" +
                                                "2320 PRINT\"        \"\n" + "2330 PRINT\"        \"\n" +
                                                "2340 PRINT\"        \"\n" + "2350 PRINT\"        \"\n" +
                                                "2360 PRINT\"-=====---\"\n" + "2370 RETURN\n" +
                                                "2400 REM --- WORT AUSSUCHEN ---\n" +
                                                "2401 CLS:PRINT\"WILLST DU...\"\n" +
                                                "2402 PRINT\"1: EIN EIGENES WORT EINGEBEN\"\n" +
                                                "2403 PRINT\"2: ZUFAELLIG EINS RATEN\"\n" +
                                                "2404 INPUT A$: IF A$<\"1\" OR A$>\"2\" THEN 2404\n" +
                                                "2405 IF A$=\"2\" THEN 2410\n" +
                                                "2406 PRINT \"WIE HEISST DEIN WORT\":INPUT W$\n" + "2407 GOTO2450\n" +
                                                "2410 REM --- WORT AUSSUCHEN ---\n" + "2415 RESTORE\n" +
                                                "2420 READ C\n" + "2430 N=RND(C)\n" +
                                                "2440 FOR I=1 TO N:READ W$:NEXT\n" + "2450 L$=\"\"\n" +
                                                "2460 FOR I=1 TO LEN(W$)\n" + "2470 B$=MID$(W$,I,1)\n" +
                                                "2480 IF B$>=\"A\" AND B$<=\"Z\" THEN 2482\n" +
                                                "2481 L$=L$+B$:GOTO 2490\n" + "2482 L$=L$+\"-\"\n" +
                                                "2490 NEXT:RETURN\n" + "2500 REM --- TITEL ---\n" + "2505 CLS\n" +
                                                "2510 PRINT:PRINT:PRINT:PRINT\n" +
                                                "2520 PRINT\"          GALGENMANN\"\n" + "2530 PRINT\n" +
                                                "2540 PRINT\"       (C) 2019 BY FR3D\"\n" +
                                                "2550 PRINT@480,\"  DRUECKE <SPACE> ZUM STARTEN\";\n" + "2555 I=0\n" +
                                                "2560 I=I+1:IF INKEY$<>\" \" THEN 2560\n" +
                                                "2565 REM SET RANDOM SEED\n" + "2570 POKE30890,I and 255\n" +
                                                "2575 POKE30891,I and 255\n" + "2580 POKE30892,I and 255\n" +
                                                "2585 POKE30893,I and 255\n" + "2599 RETURN\n" +
                                                "2600 REM --- CHECK ---\n" + "2610 OK=0\n" + "2620 L0$=\"\"\n" +
                                                "2630 FOR I=1 TO LEN(W$)\n" +
                                                "2640 IF MID$(L$,I,1) <> \"-\" THEN 2670\n" +
                                                "2650 IF MID$(W$,I,1) <> B$ THEN 2670\n" +
                                                "2660 L0$=L0$+MID$(W$,I,1):OK=1:GOTO2680\n" +
                                                "2670 L0$=L0$+MID$(L$,I,1)\n" + "2680 NEXT\n" + "2690 L$=L0$:RETURN\n" +
                                                "2700 REM --- BUCHSTABEN MERKEN\n" + "2710 FOR I=1 TO LEN(R$)\n" +
                                                "2720 IF B$=MID$(R$,I,1) THEN RETURN\n" + "2730 NEXT I\n" +
                                                "2740 R$=R$+B$\n" + "2750 RETURN\n" + "3000 REM WOERTER\n" +
                                                "3001 DATA26\n" + "3010 DATA \"ABACUS\"\n" +
                                                "3020 DATA \"BIRNBAUM\"\n" + "3030 DATA \"CHEFSESSEL\"\n" +
                                                "3040 DATA \"DUMPFBACKE\"\n" + "3050 DATA \"ESELSMILCH\"\n" +
                                                "3060 DATA \"FERNVERKEHR\"\n" + "3070 DATA \"GEIGERZAEHLER\"\n" +
                                                "3080 DATA \"HEINZELMAENNCHEN\"\n" +
                                                "3090 DATA \"IDENTITAETSKRISE\"\n" + "3100 DATA \"JEDIRITTER\"\n" +
                                                "3110 DATA \"KRUEMELMONSTER\"\n" + "3120 DATA \"LUMMERLAND\"\n" +
                                                "3130 DATA \"MILCHBUBI\"\n" + "3140 DATA \"NIEMANDSLAND\"\n" +
                                                "3150 DATA \"ORTHOGRAPHIE\"\n" + "3160 DATA \"PURZELBAUM\"\n" +
                                                "3170 DATA \"QUALLENPLAGE\"\n" + "3180 DATA \"RUESSELSCHWEIN\"\n" +
                                                "3190 DATA \"SAMMELALBUM\"\n" + "3200 DATA \"TINTENKLEKS\"\n" +
                                                "3210 DATA \"UNFALLVERHUETUNG\"\n" + "3220 DATA \"VORSCHLAGHAMMER\"\n" +
                                                "3230 DATA \"WUNSCHPUNSCH\"\n" + "3240 DATA \"XAVIAR\"\n" +
                                                "3250 DATA \"YOGHURT\"\n" + "3260 DATA \"ZINKSALBE\"";
}
