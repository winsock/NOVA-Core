package nova.core.inventory;

import nova.core.item.Item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This interface provides inventory that can hold {@link Item Items}
 * @see InventorySimple
 * @see InventoryView
 */
public interface Inventory extends Iterable<Item> {
	Optional<Item> get(int slot);

	/**
	 * Sets {@link Item} in slot
	 * @param slot Slot number
	 * @param stack Stack to insert
	 * @return Whether succeed
	 */
	boolean set(int slot, Item stack);

	/**
	 * Gets count of slots
	 * @return Number of slots in this inventory
	 */
	int size();

	/**
	 * Tells this inventory that something has changed
	 */
	void markChanged();

	/**
	 * Adds items to this inventory at specified slot
	 * @param slot Slot to add items into
	 * @param stack {@link Item} containing items
	 * @return Amount of items left(did not fit inside this inventory)
	 */
	default int add(int slot, Item stack) {
		Optional<Item> o = get(slot);
		if (o.isPresent()) {
			if (stack.sameItemType(o.get())) {
				return stack.count() - o.get().addCount(stack.count());
			} else {
				return stack.count();
			}
		} else {
			set(slot, stack);
			return 0;
		}
	}

	/**
	 * Adds items to this inventory
	 * @param stack {@link Item} containing items
	 * @return Amount of items left(did not fit inside this inventory)
	 */
	default int add(Item stack) {
		int itemsLeft = stack.count();
		for (int i = 0; i < size(); i++) {
			itemsLeft = add(i, stack.withAmount(itemsLeft));
		}

		if (itemsLeft != stack.count()) {
			markChanged();
		}

		return itemsLeft;
	}

	/**
	 * Removes a one count of the item from a slot.
	 * @param slot The slot index to remove
	 * @return The items removed
	 */
	Optional<Item> remove(int slot);

	/**
	 * Removes a certain amount of items from a slot.
	 * @param slot The slot index to remove
	 * @param amount The amount of items to remove
	 * @return The items removed
	 */
	default Optional<Item> remove(int slot, int amount) {
		Optional<Item> o = get(slot);
		if (o.isPresent()) {
			Item item = o.get();
			item.setCount(item.count() - amount);

			if (item.count() <= 0) {
				remove(slot);
			}

			return Optional.of(item.withAmount(amount));
		}
		return Optional.empty();
	}

	/**
	 * Removes a certain item from a slot.
	 * @param check The item type to check with
	 * @return The items removed
	 */
	default Optional<Item> remove(Item check) {

		int left = check.count();

		for (int i = 0; i < size(); i++) {
			Optional<Item> opItem = get(i);

			if (opItem.isPresent()) {
				Optional<Item> removed = remove(i, check.count());

				if (removed.isPresent()) {
					left -= removed.get().count();
				}
			}
		}

		int removed = check.count() - left;

		if (removed > 0) {
			return Optional.of(check.withAmount(removed));
		}

		return Optional.empty();
	}

	/**
	 * Represents this inventory as list of {@link Item Items}
	 * @return This inventory as list of {@link Item Items}
	 */
	default List<Item> toList() {
		ArrayList<Item> list = new ArrayList<>();
		for (Item i : this) {
			list.add(i);
		}
		return list;
	}

	default Set<Item> toSet() {
		Set<Item> list = new HashSet<>();
		for (Item i : this) {
			list.add(i);
		}
		return list;
	}

	default Iterator<Item> iterator() {
		return new InventoryIterator(this);
	}

	default Spliterator<Item> spliterator() {
		return Spliterators.spliterator(iterator(), size(), Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SORTED);
	}

	/**
	 * Represents this inventory as {@link Item} {@link Stream}
	 * @return This inventory as {@link Item} {@link Stream}
	 */
	default Stream<Item> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
