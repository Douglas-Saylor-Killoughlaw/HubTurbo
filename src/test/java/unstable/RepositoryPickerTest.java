package unstable;

import static org.junit.Assert.*;
import static ui.components.pickers.PickerRepository.COMMON_REPO_LABEL_STYLE;
import static ui.components.pickers.PickerRepository.SELECTED_REPO_LABEL_STYLE;

import java.io.File;
import java.util.concurrent.TimeoutException;

import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.testfx.api.FxToolkit;

import guitests.UITest;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import ui.listpanel.ListPanel;
import util.PlatformEx;
import util.events.testevents.PrimaryRepoChangedEventHandler;

public class RepositoryPickerTest extends UITest {

    private static String primaryRepo;

    @Override
    public void setup() throws TimeoutException {
        FxToolkit.setupApplication(TestUI.class, "--testconfig=true");
        UI.events.registerEvent((PrimaryRepoChangedEventHandler) e -> primaryRepo = e.repoId);
    }

    @Override
    public void beforeStageStarts() {
        // setup test json with last viewed repo "dummy/dummy"
        // obviously the json for that repo doesn't exist
        Preferences prefs = TestController.createTestPreferences();
        prefs.setLastLoginCredentials("test", "test");
        prefs.setLastViewedRepository("dummy/dummy");
    }

    @Test
    public void repositoryPickerTest() {
        // check if test json is present
        File testConfig = new File(TestController.TEST_DIRECTORY, TestController.TEST_SESSION_CONFIG_FILENAME);
        boolean testConfigExists = testConfig.exists() && testConfig.isFile();
        assertTrue(testConfigExists);

        ListPanel listPanel;
        VBox suggestedRepositoryList;
        TextField userInputField;

        // now we check if the login dialog pops up because the "dummy/dummy" json
        // doesn't exist and there are no other valid repo json files
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.ENTER);
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears(IdGenerator.getRepositoryPickerTextFieldReference());
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(1, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy/dummy", primaryRepo);
        push(KeyCode.ESCAPE);
        PlatformEx.waitOnFxThread();

        // we check if the "dummy2/dummy2" is added to the repository picker
        // but the primary repo isn't changed
        listPanel = getPanel(0);
        listPanel.setFilterByString("repo:dummy2/dummy2");
        waitUntilNodeAppears(IdGenerator.getPanelCellIdReference(0, 11));
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(2, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy/dummy", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if "dummy3/dummy3" is added to the repository picker
        // and that the primary repo is also changed
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        userInputField = findOrWaitFor(IdGenerator.getRepositoryPickerTextFieldReference());
        type("dummy3/dummy3");
        clickOn("Add repository");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        assertEquals("dummy3/dummy3", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check whether the UI is updated under various scenarios
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        userInputField = findOrWaitFor(IdGenerator.getRepositoryPickerTextFieldReference());
        clickOn(userInputField);
        type("dummy");
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        assertSelectedPickerRepositoryNode("dummy/dummy", suggestedRepositoryList.getChildren().get(0));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy2/dummy2", suggestedRepositoryList.getChildren().get(1));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy3/dummy3", suggestedRepositoryList.getChildren().get(2));
        push(KeyCode.DOWN);
        assertSelectedPickerRepositoryNode("dummy/dummy", suggestedRepositoryList.getChildren().get(0));
        push(KeyCode.UP);
        assertSelectedPickerRepositoryNode("dummy3/dummy3", suggestedRepositoryList.getChildren().get(2));
        doubleClickOn(userInputField);
        type("dummmy");
        assertEquals(0, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check that the new repo created from user input is not selected anymore upon click
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        userInputField = findOrWaitFor(IdGenerator.getRepositoryPickerTextFieldReference());
        doubleClickOn(userInputField);
        type("d");
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        clickOn("dummy2/dummy2");
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        assertTrue(userInputField.isDisabled());
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals("dummy2/dummy2", primaryRepo);

        // we check if repo's id with white spaces are handled correctly
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears("Add repository");
        clickOn("Add repository");
        type(" dummy4 / dummy4 ");
        push(KeyCode.ENTER);
        userInputField = findOrWaitFor(IdGenerator.getRepositoryPickerTextFieldReference());
        doubleClickOn(userInputField);
        doubleClickOn();
        type(" dummy4 / dummy4 ");
        push(KeyCode.ENTER);
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        waitAndAssertEquals(4, suggestedRepositoryList.getChildren()::size);
        assertEquals("dummy4/dummy4", primaryRepo);
        push(KeyCode.ESCAPE);

        // we check if deleting used repo does not remove it
        traverseHubTurboMenu("Repos", "Remove", "dummy4/dummy4 [in use, not removable]"); // first used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(4, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check if delete repo works
        traverseHubTurboMenu("Repos", "Remove", "dummy/dummy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // we check again if deleting used repo does not remove it
        traverseHubTurboMenu("Repos", "Remove", "dummy2/dummy2 [in use, not removable]"); // second used repo
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        suggestedRepositoryList = findOrWaitFor(IdGenerator.getRepositoryPickerSuggestedRepoListReference());
        assertEquals(3, suggestedRepositoryList.getChildren().size());
        push(KeyCode.ESCAPE);

        // exit program
        traverseHubTurboMenu("App", "Quit");

        push(KeyCode.ENTER);

        // testing that the correct repo was saved in the json
        // check if the test JSON is still there...
        assertTrue(testConfig.exists() && testConfig.isFile());

        // ...then check that the JSON file contents are correct.
        Preferences testPref = TestController.loadTestPreferences();
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy4/dummy4", lastViewedRepository.generateId());
    }

    @Test
    public void addRepository_repoPickerTextFieldNotEmpty_inputCopiedToAndSelectedInRepoDialog() {
        File testConfig = new File(TestController.TEST_DIRECTORY, TestController.TEST_SESSION_CONFIG_FILENAME);
        boolean testConfigExists = testConfig.exists() && testConfig.isFile();
        assertTrue(testConfigExists);

        // now we check if the login dialog pops up because the "dummy/dummy" json
        // doesn't exist and there are no other valid repo json files
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.ENTER);

        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears("Confirm");
        PlatformEx.waitOnFxThread();
        type("atom/atom");
        clickOn("Add repository");
        waitUntilNodeAppears("OK");
        // try prepending text to test whether the text is selected
        push(KeyCode.LEFT);
        type("abcd-").press(KeyCode.ENTER);
        waitUntilNodeAppears("abcd-atom/atom");
        push(KeyCode.ESCAPE);

        traverseHubTurboMenu("Repos", "Show Repository Picker");
        PlatformEx.waitOnFxThread();
        waitUntilNodeAppears("Confirm");
        PlatformEx.waitOnFxThread();
        type("atom/atom");
        clickOn("Add repository");
        waitUntilNodeAppears("OK");
        // immediate typing should erase the whole string in addRepo dialog
        type("abcd-").press(KeyCode.ENTER);
        waitUntilNodeAppears("abcd-");
        push(KeyCode.ESCAPE);

        traverseHubTurboMenu("File", "Quit");
        push(KeyCode.ENTER);
    }

    private void assertSelectedPickerRepositoryNode(String repoId, Node node) {
        assertTrue(node instanceof Label);
        assertEquals(repoId, ((Label) node).getText());
        assertEquals(COMMON_REPO_LABEL_STYLE + SELECTED_REPO_LABEL_STYLE, node.getStyle());
    }
}
