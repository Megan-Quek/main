package seedu.address.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javafx.collections.ObservableList;
import seedu.address.commons.core.UnmodifiableObservableList;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.model.task.ReadOnlyFloatingTask;
import seedu.address.model.task.FloatingTask;
import seedu.address.model.task.UniqueTaskList;
import seedu.address.model.task.UniqueTaskList.DuplicateTaskException;

/**
 * Wraps all data at the address-book level
 * Duplicates are not allowed (by .equals comparison)
 */
public class TaskList implements ReadOnlyTaskList {

    private final UniqueTaskList tasks;
    private final UniqueTagList tags;

    /*
     * The 'unusual' code block below is an non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        tasks = new UniqueTaskList();
        tags = new UniqueTagList();
    }

    public TaskList() {}

    /**
     * Creates an AddressBook using the Persons and Tags in the {@code toBeCopied}
     */
    public TaskList(ReadOnlyTaskList toBeCopied) {
        this();
        resetData(toBeCopied);
    }

//// list overwrite operations

    public void setTasks(List<? extends ReadOnlyFloatingTask> persons)
            throws UniqueTaskList.DuplicateTaskException {
        this.tasks.setPersons(persons);
    }

    public void setTags(Collection<Tag> tags) throws UniqueTagList.DuplicateTagException {
        this.tags.setTags(tags);
    }

    public void resetData(ReadOnlyTaskList newData) {
        assert newData != null;
        try {
            setTasks(newData.getTaskList());
        } catch (UniqueTaskList.DuplicateTaskException e) {
            assert false : "TaskLists should not have duplicate tasks";
        }
        try {
            setTags(newData.getTagList());
        } catch (UniqueTagList.DuplicateTagException e) {
            assert false : "TaskLists should not have duplicate tags";
        }
        syncMasterTagListWith(tasks);
    }

//// task-level operations

    /**
     * Adds a person to the address book.
     * Also checks the new person's tags and updates {@link #tags} with any new tags found,
     * and updates the Tag objects in the person to point to those in {@link #tags}.
     *
     * @throws UniqueTaskList.DuplicateTaskException if an equivalent person already exists.
     */
    public void addTask(FloatingTask p) throws UniqueTaskList.DuplicateTaskException {
        syncMasterTagListWith(p);
        tasks.add(p);
    }

    /**
     * Updates the person in the list at position {@code index} with {@code editedReadOnlyPerson}.
     * {@code AddressBook}'s tag list will be updated with the tags of {@code editedReadOnlyPerson}.
     * @see #syncMasterTagListWith(FloatingTask)
     *
     * @throws DuplicateTaskException if updating the person's details causes the person to be equivalent to
     *      another existing person in the list.
     * @throws IndexOutOfBoundsException if {@code index} < 0 or >= the size of the list.
     */
    public void updateTask(int index, ReadOnlyFloatingTask editedReadOnlyPerson)
            throws UniqueTaskList.DuplicateTaskException {
        assert editedReadOnlyPerson != null;

        FloatingTask editedPerson = new FloatingTask(editedReadOnlyPerson);
        syncMasterTagListWith(editedPerson);
        // TODO: the tags master list will be updated even though the below line fails.
        // This can cause the tags master list to have additional tags that are not tagged to any person
        // in the person list.
        tasks.updatePerson(index, editedPerson);
    }

    /**
     * Ensures that every tag in this person:
     *  - exists in the master list {@link #tags}
     *  - points to a Tag object in the master list
     */
    private void syncMasterTagListWith(FloatingTask person) {
        final UniqueTagList personTags = person.getTags();
        tags.mergeFrom(personTags);

        // Create map with values = tag object references in the master list
        // used for checking person tag references
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        tags.forEach(tag -> masterTagObjects.put(tag, tag));

        // Rebuild the list of person tags to point to the relevant tags in the master tag list.
        final Set<Tag> correctTagReferences = new HashSet<>();
        personTags.forEach(tag -> correctTagReferences.add(masterTagObjects.get(tag)));
        person.setTags(new UniqueTagList(correctTagReferences));
    }

    /**
     * Ensures that every tag in these persons:
     *  - exists in the master list {@link #tags}
     *  - points to a Tag object in the master list
     *  @see #syncMasterTagListWith(FloatingTask)
     */
    private void syncMasterTagListWith(UniqueTaskList persons) {
        persons.forEach(this::syncMasterTagListWith);
    }

    public boolean removePerson(ReadOnlyFloatingTask key) throws UniqueTaskList.TaskNotFoundException {
        if (tasks.remove(key)) {
            return true;
        } else {
            throw new UniqueTaskList.TaskNotFoundException();
        }
    }

//// tag-level operations

    public void addTag(Tag t) throws UniqueTagList.DuplicateTagException {
        tags.add(t);
    }

//// util methods

    @Override
    public String toString() {
        return tasks.asObservableList().size() + " persons, " + tags.asObservableList().size() +  " tags";
        // TODO: refine later
    }

    @Override
    public ObservableList<ReadOnlyFloatingTask> getTaskList() {
        return new UnmodifiableObservableList<>(tasks.asObservableList());
    }

    @Override
    public ObservableList<Tag> getTagList() {
        return new UnmodifiableObservableList<>(tags.asObservableList());
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof TaskList // instanceof handles nulls
                && this.tasks.equals(((TaskList) other).tasks)
                && this.tags.equalsOrderInsensitive(((TaskList) other).tags));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(tasks, tags);
    }
}