package seedu.address.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.tag.Tag;
import seedu.address.model.task.ReadOnlyFloatingTask;
import seedu.address.model.task.FloatingTask;
import seedu.address.testutil.TypicalTestTasks;

public class TaskListTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final TaskList taskList = new TaskList();

    @Test
    public void constructor() {
        assertEquals(Collections.emptyList(), taskList.getTaskList());
        assertEquals(Collections.emptyList(), taskList.getTagList());
    }

    @Test
    public void resetData_null_throwsAssertionError() {
        thrown.expect(AssertionError.class);
        taskList.resetData(null);
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        TaskList newData = new TypicalTestTasks().getTypicalTaskManager();
        taskList.resetData(newData);
        assertEquals(newData, taskList);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsAssertionError() {
        TypicalTestTasks td = new TypicalTestTasks();
        // Repeat td.alice twice
        List<FloatingTask> newPersons = Arrays.asList(new FloatingTask(td.alice), new FloatingTask(td.alice));
        List<Tag> newTags = td.alice.getTags().asObservableList();
        AddressBookStub newData = new AddressBookStub(newPersons, newTags);

        thrown.expect(AssertionError.class);
        taskList.resetData(newData);
    }

    @Test
    public void resetData_withDuplicateTags_throwsAssertionError() {
        TaskList typicalAddressBook = new TypicalTestTasks().getTypicalTaskManager();
        List<ReadOnlyFloatingTask> newPersons = typicalAddressBook.getTaskList();
        List<Tag> newTags = new ArrayList<>(typicalAddressBook.getTagList());
        // Repeat the first tag twice
        newTags.add(newTags.get(0));
        AddressBookStub newData = new AddressBookStub(newPersons, newTags);

        thrown.expect(AssertionError.class);
        taskList.resetData(newData);
    }

    /**
     * A stub ReadOnlyAddressBook whose persons and tags lists can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyTaskList {
        private final ObservableList<ReadOnlyFloatingTask> persons = FXCollections.observableArrayList();
        private final ObservableList<Tag> tags = FXCollections.observableArrayList();

        AddressBookStub(Collection<? extends ReadOnlyFloatingTask> persons, Collection<? extends Tag> tags) {
            this.persons.setAll(persons);
            this.tags.setAll(tags);
        }

        @Override
        public ObservableList<ReadOnlyFloatingTask> getTaskList() {
            return persons;
        }

        @Override
        public ObservableList<Tag> getTagList() {
            return tags;
        }
    }

}