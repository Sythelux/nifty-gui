package de.lessvoid.nifty.controls.listbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ListBoxInsertItemInvalidTest {
  private ListBoxImpl<TestItem> listBox = new ListBoxImpl<TestItem>(null);
  private TestItem o1 = new TestItem("o1");
  private TestItem o2 = new TestItem("o2");

  @Test
  public void testInsertItemInvalid() {
    listBox.insertItem(o2, -1);
    assertListBoxContent();
  }

  @Test
  public void testInsertItemInvalidIndex() {
    listBox.insertItem(o1, 1);
    assertListBoxContent();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCantModifyGetItems() {
    listBox.getItems().add(o1);
  }

  private void assertListBoxContent(final TestItem... expected) {
    assertEquals(expected.length, listBox.getItems().size());
    int i = 0;
    for (TestItem e : expected) {
      assertEquals(e, listBox.getItems().get(i));
      i++;
    }
  }
}
