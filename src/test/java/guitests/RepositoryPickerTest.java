package guitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.Preferences;
import ui.TestController;
import ui.UI;
import ui.components.pickers.PickerRepository;
import util.PlatformEx;
import util.events.testevents.PrimaryRepoChangedEventHandler;

public class RepositoryPickerTest extends UITest {

    private static String primaryRepo;

    protected static class RepositoryPickerTestUI extends UI {
        public RepositoryPickerTestUI() {
            super();
        }

        @Override
        public void start(Stage primaryStage) {
            super.start(primaryStage);
            STAGE_FUTURE.set(primaryStage);
        }

        @Override
        protected void registerTestEvents() {
            UI.events.registerEvent((PrimaryRepoChangedEventHandler) e -> primaryRepo = e.repoId);
        }
    }

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(RepositoryPickerTestUI.class, "--testconfig=true");
    }

    @Override
    public void beforeStageStarts() {
        // setup test json with last viewed repo "dummy/dummy"
        // obviously the json for that repo doesn't exist
        ConfigFileHandler configFileHandler =
                new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setLastLoginCredentials("test", "test");
        globalConfig.setLastViewedRepository("dummy/dummy");
        configFileHandler.saveGlobalConfig(globalConfig);
    }

    @Test
    public void repositoryPickerTest() {
        // check if test json is present
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        boolean testConfigExists = testConfig.exists() && testConfig.isFile();
        if (!testConfigExists) {
            fail();
        }

        VBox suggestedRepositoryList;
        TextField userInputField;

        // now we check if the login dialog pops up because the "dummy/dummy" json
        // doesn't exist and there are no other valid repo json files
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.ENTER);
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(1, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy/dummy", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if the "dummy2/dummy2" is added to the repository picker
        // but the primary repo isn't changed
        Platform.runLater(findOrWaitFor("#dummy/dummy_col0_filterTextField")::requestFocus);
        PlatformEx.waitOnFxThread();
        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(2, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy/dummy", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if "dummy3/dummy3" is added to the repository picker
        // and that the primary repo is also changed
        push(KeyCode.CONTROL, KeyCode.R);
        userInputField = findOrWaitFor("#repositoryPickerUserInputField");
        doubleClick(userInputField);
        doubleClick();
        type("dummy3/dummy3");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy3/dummy3", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check whether the UI is updated under various scenarios
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        userInputField = findOrWaitFor("#repositoryPickerUserInputField");
        click(userInputField);
        type("dummy");
        assertEquals(4, suggestedRepositoryList.getChildren().size());
        assertSelectedPickerRepositoryNode("dummy", suggestedRepositoryList.getChildren().get(0));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy/dummy", suggestedRepositoryList.getChildren().get(1));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy2/dummy2", suggestedRepositoryList.getChildren().get(2));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy3/dummy3", suggestedRepositoryList.getChildren().get(3));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy", suggestedRepositoryList.getChildren().get(0));
        push(KeyCode.UP);
        assertSelectedPickerRepositoryNode("dummy3/dummy3", suggestedRepositoryList.getChildren().get(3));
        doubleClick(userInputField);
        type("dummmy");
        assertEquals(1, suggestedRepositoryList.getChildren().size());
        assertSelectedPickerRepositoryNode("dummmy", suggestedRepositoryList.getChildren().get(0));
        push(KeyCode.ESCAPE);

        // we check if repo's id with white spaces are handled correctly
        push(KeyCode.CONTROL, KeyCode.R);
        userInputField = findOrWaitFor("#repositoryPickerUserInputField");
        doubleClick(userInputField);
        doubleClick();
        type(" dummy4 / dummy4 ");
        push(KeyCode.ENTER);
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(4, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy4/dummy4", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if deleting used repo does not remove it
        traverseMenu("Repos", "Remove", "dummy4/dummy4 [in use, not removable]"); // first used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(4, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check if delete repo works
        traverseMenu("Repos", "Remove", "dummy/dummy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check again if deleting used repo does not remove it
        traverseMenu("Repos", "Remove", "dummy2/dummy2 [in use, not removable]"); // second used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.CONTROL, KeyCode.R);
        suggestedRepositoryList = findOrWaitFor("#suggestedRepositoryList");
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // exit program
        traverseMenu("File", "Quit");
        push(KeyCode.ENTER);

        // testing that the correct repo was saved in the json
        // check if the test JSON is still there...
        if (!(testConfig.exists() && testConfig.isFile())) {
            fail();
        }

        // ...then check that the JSON file contents are correct.
        Preferences testPref = TestController.loadTestPreferences();
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy4/dummy4", lastViewedRepository.generateId());
    }

    private void assertSelectedPickerRepositoryNode(String repoId, Node node) {
        assertTrue(node instanceof Label);
        assertEquals(repoId, ((Label) node).getText());
        assertEquals(PickerRepository.SELECTED_REPO_LABEL_STYLE, node.getStyle());
    }
}
