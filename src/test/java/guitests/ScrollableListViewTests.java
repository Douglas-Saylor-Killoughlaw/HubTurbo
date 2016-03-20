package guitests;

import javafx.scene.input.KeyCode;
import org.junit.Test;
import ui.IdGenerator;
import ui.UI;
import ui.listpanel.ListPanel;
import util.events.testevents.UILogicRefreshEvent;
import util.events.testevents.UpdateDummyRepoEvent;

import static org.junit.Assert.assertEquals;

public class ScrollableListViewTests extends UITest {

    @Test
    public void scrollAndShowTest() {
        for (int i = 0; i < 40; i++) {
            UI.events.triggerEvent(UpdateDummyRepoEvent.newIssue("dummy/dummy"));
        }
        UI.events.triggerEvent(new UILogicRefreshEvent());
        sleep(2000);
        ListPanel col0 = find(IdGenerator.getPanelIdForTest("dummy/dummy", 0));
        click(IdGenerator.getPanelCellIdForTest("dummy/dummy", 0, 49));
        for (int i = 0; i < 40; i++) {
            push(KeyCode.DOWN);
        }
        sleep(1000);
        assertEquals(true, col0.getSelectedElement().isPresent());
        assertEquals(9, col0.getSelectedElement().get().getIssue().getId());
        for (int i = 0; i < 40; i++) {
            push(KeyCode.UP);
        }
        sleep(1000);
        assertEquals(true, col0.getSelectedElement().isPresent());
        assertEquals(49, col0.getSelectedElement().get().getIssue().getId());
    }

}
