package guitests;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Before;
import org.junit.Test;
import ui.UI;
import ui.listpanel.ListPanel;
import util.events.IssueSelectedEventHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IssueSelectedEventTest extends UITest{

    static int eventTestCount;

    public static void increaseEventTestCount() {
        eventTestCount++;
    }

    private static void resetEventTestCount() {
        eventTestCount = 0;
    }

    @Before
    public void setup() {
        UI.events.registerEvent((IssueSelectedEventHandler) e -> IssueSelectedEventTest.increaseEventTestCount());
    }

    /**
     * Tests whether right click doesn't trigger IssueSelectedEvent
     */
    @Test
    public void noTriggerIssueSelectedOnRightClick_IssueInPanelRightClicked_IssueSelectedNotTriggered() {
        resetEventTestCount();

        ListPanel issuePanel = find("#dummy/dummy_col0");

        //testing whether right click occurred by checking the presence of context menu items
        rightClick("#dummy/dummy_col0_9");
        ContextMenu contextMenu = issuePanel.getContextMenu();
        for (MenuItem menuItem : contextMenu.getItems()){
            assertTrue(!menuItem.isDisable());
        }

        // testing IssueSelectedEvent not registered on right click
        assertEquals(0, eventTestCount);
    }

    /**
     * Tests whether left click and key press triggers IssueSelectedEvent
     */
    @Test
    public void triggerIssueSelectedOnLeftClickAndKey_IssueInPanelLeftClickedAndKeyed_IssueSelectedTriggered() {
        resetEventTestCount();

        click("#dummy/dummy_col0_9");

        // testing IssueSelectedEvent is triggered on left click
        assertEquals(1, eventTestCount);

        //testing IssueSelectedEvent is triggered on key down to a particular issue
        push(KeyCode.DOWN);
        assertEquals(2, eventTestCount);
    }

}
