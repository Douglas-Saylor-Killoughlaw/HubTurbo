package guitests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import ui.GuiElement;
import ui.components.ScrollableListView;

import org.junit.Before;
import org.junit.Test;

import ui.components.FilterTextField;
import ui.listpanel.ListPanel;
import ui.listpanel.ListPanelCell;
import util.PlatformEx;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ContextMenuTests extends UITest {

    private static final int EVENT_DELAY = 1000;
    private static final int DIALOG_DELAY = 1500;

    @Before
    public void setup() {
        Platform.runLater(stage::show);
        Platform.runLater(stage::requestFocus);

        FilterTextField filterTextField = find("#dummy/dummy_col0_filterTextField");
        filterTextField.setText("");
        Platform.runLater(filterTextField::requestFocus);

        click("#dummy/dummy_col0_filterTextField");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
    }

    /**
     * Tests context menu when no item is selected
     * All menu items should be disabled
     */
    @Test
    public void contextMenuDisabling_noIssueInListView_contextMenuItemsDisabled() {
        ListPanel issuePanel = find("#dummy/dummy_col0");

        click("#dummy/dummy_col0_filterTextField");
        type("asdf");
        push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
        rightClick("#dummy/dummy_col0");
        sleep(EVENT_DELAY);

        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            assertTrue(menuItem.isDisable());
        }
    }

    /**
     * Tests selecting "Mark as read" and "Mark as unread"
     * context menu items
     */
    @Test
    public void test2() {
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_9");

        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Mark as read (E)");
        sleep(EVENT_DELAY);
        assertTrue(listPanelCell.getIssue().isCurrentlyRead());

        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Mark as unread (U)");
        sleep(EVENT_DELAY);
        assertFalse(listPanelCell.getIssue().isCurrentlyRead());
    }

    /**
     * Tests selecting "Mark all below as read" and "Mark all below as unread" context menu items
     */
    @Test
    public void markAllBelowAsReadUnread_twelveIssuesInListView_markIssue7andBelowReadUnread() {
        ListPanel issuePanel = find("#dummy/dummy_col0");
        issuePanel.listView.scrollAndShow(12);
        //checking for issue #7 and below twice to make sure issues are marked correctly for read/unread cases
        for (int i = 0; i < 2; i++) {
            markAndVerifyIssuesBelow(7, true);
        }
        for (int i = 0; i < 2; i++) {
            markAndVerifyIssuesBelow(7, false);
        }

        //checking for the last issue to ensure correct marking of issues on/below as read/unread when no issues below
        markAndVerifyIssuesBelow(1, true);
        markAndVerifyIssuesBelow(1, false);
    }

    /**
     * Tests selecting "Change labels" context menu item
     */
    @Test
    public void test3() {
        click("#dummy/dummy_col0_9");
        rightClick("#dummy/dummy_col0_9");
        sleep(EVENT_DELAY);
        click("Change labels (L)");
        sleep(DIALOG_DELAY);

        assertNotNull(find("#labelPickerTextField"));

        push(KeyCode.ESCAPE);
        sleep(EVENT_DELAY);
    }

    /**
     * Marks and tests issues in a panel of 12 issues
     * from Issue #{index} in the UI right to the end of the list as read/unread
     * @param isMarkAsRead If true, tests whether issues on/below the selected issue are being correctly marked read
     *                      If false, tests whether issues on/below the selected are being correctly marked unread
     * @param index The issue number in the panel
     */
    private void markAndVerifyIssuesBelow(int index, boolean isMarkAsRead){
        ListPanel issuePanel = find("#dummy/dummy_col0");
        click("#dummy/dummy_col0_" + index);
        rightClick("#dummy/dummy_col0_" + index);
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            awaitCondition(menuItem::isVisible);
            System.out.println(menuItem.getText() + "blah");
            assertTrue(menuItem.isVisible());
        }
        sleep(EVENT_DELAY);
        if (isMarkAsRead){
            click("Mark all below as read");
        } else {
            click("Mark all below as unread");
        }
        for (int i = index; i >= 1; i--){
            verifyReadStatusOfIssue(i, isMarkAsRead);
        }
    }

    /**
     * Tests whether a list panel cell corresponding to a particular index is marked as read/unread
     */
    private void verifyReadStatusOfIssue(int index, boolean isExpectedStatusRead){
        ListPanelCell listPanelCell = find("#dummy/dummy_col0_" + index);
        assertEquals(listPanelCell.getIssue().isCurrentlyRead(), isExpectedStatusRead);
    }

    /**
     * Scrolls to the end of the panel by clicking an issue and pressing the down key
     */
    private void scrollToTheEndOfThePanel() {
        click("#dummy/dummy_col0_9");
        for (int i = 0; i < 15; i++) {
            push(KeyCode.DOWN);
        }
    }

}
