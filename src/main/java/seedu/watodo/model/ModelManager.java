package seedu.watodo.model;

import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.transformation.FilteredList;
import seedu.watodo.commons.core.ComponentManager;
import seedu.watodo.commons.core.LogsCenter;
import seedu.watodo.commons.core.UnmodifiableObservableList;
import seedu.watodo.commons.events.model.TaskListChangedEvent;
import seedu.watodo.commons.util.CollectionUtil;
import seedu.watodo.commons.util.StringUtil;
import seedu.watodo.model.task.FloatingTask;
import seedu.watodo.model.task.ReadOnlyFloatingTask;
import seedu.watodo.model.task.UniqueTaskList;
import seedu.watodo.model.task.UniqueTaskList.TaskNotFoundException;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TaskList addressBook;
    private final FilteredList<ReadOnlyFloatingTask> filteredPersons;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyTaskList addressBook, UserPrefs userPrefs) {
        super();
        assert !CollectionUtil.isAnyNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new TaskList(addressBook);
        filteredPersons = new FilteredList<>(this.addressBook.getTaskList());
    }

    public ModelManager() {
        this(new TaskList(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyTaskList newData) {
        addressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyTaskList getAddressBook() {
        return addressBook;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateAddressBookChanged() {
        raise(new TaskListChangedEvent(addressBook));
    }

    @Override
    public synchronized void deleteTask(ReadOnlyFloatingTask target) throws TaskNotFoundException {
        addressBook.removePerson(target);
        indicateAddressBookChanged();
    }

    @Override
    public synchronized void addTask(FloatingTask person) throws UniqueTaskList.DuplicateTaskException {
        addressBook.addTask(person);
        updateFilteredListToShowAll();
        indicateAddressBookChanged();
    }

    @Override
    public void updateTask(int filteredPersonListIndex, ReadOnlyFloatingTask editedPerson)
            throws UniqueTaskList.DuplicateTaskException {
        assert editedPerson != null;

        int addressBookIndex = filteredPersons.getSourceIndex(filteredPersonListIndex);
        addressBook.updateTask(addressBookIndex, editedPerson);
        indicateAddressBookChanged();
    }

    //=========== Filtered Task List Accessors =============================================================

    @Override
    public UnmodifiableObservableList<ReadOnlyFloatingTask> getFilteredTaskList() {
        return new UnmodifiableObservableList<>(filteredPersons);
    }

    @Override
    public void updateFilteredListToShowAll() {
        filteredPersons.setPredicate(null);
    }

    @Override
    public void updateFilteredTaskList(Set<String> keywords) {
        updateFilteredPersonList(new PredicateExpression(new NameQualifier(keywords)));
    }

    private void updateFilteredPersonList(Expression expression) {
        filteredPersons.setPredicate(expression::satisfies);
    }

    //========== Inner classes/interfaces used for filtering =================================================

    interface Expression {
        boolean satisfies(ReadOnlyFloatingTask person);
        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyFloatingTask person) {
            return qualifier.run(person);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyFloatingTask person);
        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        @Override
        public boolean run(ReadOnlyFloatingTask person) {
            return nameKeyWords.stream()
                    .filter(keyword -> StringUtil.containsWordIgnoreCase(person.getDescription().fullDescription, keyword))
                    .findAny()
                    .isPresent();
        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }

}
