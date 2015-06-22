package guitests;

import com.google.common.util.concurrent.SettableFuture;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.junit.Before;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.UI;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class UITest extends GuiTest {

    private static final SettableFuture<Stage> stageFuture = SettableFuture.create();

    protected static class TestUI extends UI {
        public TestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            stageFuture.set(primaryStage);
        }
    }

    public void setupMethod() {
        // method to be overridden if anything needs to be done (e.g. to the json) before the stage starts
    }

    @Before
    @Override
    public void setupStage() throws Throwable {
        // delete test.json if it exists
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        if (testConfig.exists()) {
            testConfig.delete();
        }

        setupMethod();

        if (stage == null) {
            launchApp();
        }
        try {
            stage = targetWindow(stageFuture.get(25, TimeUnit.SECONDS));
            FXTestUtils.bringToFront(stage);
        } catch (Exception e) {
            throw new RuntimeException("Unable to show stage", e);
        }
    }

    // override this to change launch arguments
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--test=true", "--bypasslogin=true");
    }

    @Override
    protected Parent getRootNode() {
        return stage.getScene().getRoot();
    }
}